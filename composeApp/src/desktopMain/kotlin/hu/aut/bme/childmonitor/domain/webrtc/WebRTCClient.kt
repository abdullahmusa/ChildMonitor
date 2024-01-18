package hu.aut.bme.childmonitor.domain.webrtc

import com.google.gson.Gson
import dev.onvoid.webrtc.CreateSessionDescriptionObserver
import dev.onvoid.webrtc.PeerConnectionFactory
import dev.onvoid.webrtc.PeerConnectionObserver
import dev.onvoid.webrtc.RTCAnswerOptions
import dev.onvoid.webrtc.RTCConfiguration
import dev.onvoid.webrtc.RTCIceCandidate
import dev.onvoid.webrtc.RTCIceServer
import dev.onvoid.webrtc.RTCOfferOptions
import dev.onvoid.webrtc.RTCPeerConnection
import dev.onvoid.webrtc.RTCSessionDescription
import dev.onvoid.webrtc.SetSessionDescriptionObserver
import hu.aut.bme.childmonitor.data.model.DeviceRole

class WebRTCClient {
    companion object {
        private const val serverUri = "" // TODO: add your TURN/STUN server URI here
        private const val serverUsername = "" // TODO: add your TURN/STUN server username here
        private const val serverPassword = "" // TODO: add your TURN/STUN server password here
    }

    init {
        assert(serverUri.isNotEmpty()) {
            "TURN URL is empty!"
        }
        assert(serverUsername.isNotEmpty()) {
            "TURN username is empty!"
        }
        assert(serverPassword.isNotEmpty()) {
            "TURN password is empty!"
        }
    }

    var listener: Listener? = null
    var deviceID: String = ""
    lateinit var deviceRole: DeviceRole
    private var targetDeviceID: String? = null

    var peerConnection: RTCPeerConnection? = null
    private val iceServer = listOf(
        RTCIceServer().apply {
            urls.add(serverUri)
            username = serverUsername
            password = serverPassword
        }
    )

    fun observePeerConnection(observer: PeerConnectionObserver) {
        val rtcConfiguration = RTCConfiguration().apply {
            iceServers = iceServer
        }
        peerConnection = PeerConnectionFactory().createPeerConnection(rtcConfiguration, observer)
    }

    fun createOffer(targetID: String) {
        targetDeviceID = targetID
        peerConnection?.createOffer(
            RTCOfferOptions().apply { voiceActivityDetection = false },
            object : CreateSessionDescriptionObserver {
                override fun onSuccess(description: RTCSessionDescription?) {
                    peerConnection?.setLocalDescription(
                        description,
                        object : SetSessionDescriptionObserver {
                            override fun onSuccess() {
                                listener?.runCatching {
                                    onTransferEventToSocket(
                                        DataModel(
                                            type = DataModelType.RequestOffer,//Offer,
                                            sender = deviceID,
                                            target = targetID,
                                            data = description?.sdp?.replace(
                                                oldValue = "\r\na=extmap-allow-mixed",
                                                newValue = ""
                                            )
                                        )
                                    )
                                }
                            }

                            override fun onFailure(error: String?) {
                                println("createOffer: onSetFailure\n$error")
                            }
                        })
                }

                override fun onFailure(error: String?) {
                    println("createOffer: onCreateFailure\n$error")
                }
            },
        )
    }

    fun answer(targetID: String) {
        targetDeviceID = targetID
        peerConnection?.createAnswer(
            RTCAnswerOptions().apply { voiceActivityDetection = false },
            object : CreateSessionDescriptionObserver {
                override fun onSuccess(description: RTCSessionDescription?) {
                    peerConnection?.setLocalDescription(
                        description,
                        object : SetSessionDescriptionObserver {
                            override fun onSuccess() {
                                listener?.runCatching {
                                    onTransferEventToSocket(
                                        DataModel(
                                            type = DataModelType.Answer,
                                            sender = deviceID,
                                            target = targetID,
                                            data = description?.sdp
                                        )
                                    )
                                }
                            }

                            override fun onFailure(error: String?) {
                                println("answer: onSetFailure\n$error")
                            }
                        },
                    )
                }

                override fun onFailure(error: String?) {
                    println("answer: onCreateFailure\n$error")
                }
            },
        )
    }

    fun onRemoteSessionReceived(sessionDescription: RTCSessionDescription) {
        peerConnection?.setRemoteDescription(
            sessionDescription,
            object : SetSessionDescriptionObserver {
                override fun onSuccess() {
                    println("onRemoteSessionReceived: onSetSuccess")
                }

                override fun onFailure(error: String?) {
                    println("onRemoteSessionReceived: onSetFailure\n$error")
                }
            })
    }

    fun addIceCandidateToPeer(iceCandidate: RTCIceCandidate) {
        peerConnection?.addIceCandidate(iceCandidate)
    }

    fun sendIceCandidate(iceCandidate: RTCIceCandidate) {
        if (targetDeviceID == null) return
        addIceCandidateToPeer(iceCandidate)
        listener?.runCatching {
            onTransferEventToSocket(
                DataModel(
                    type = DataModelType.IceCandidates,
                    sender = deviceID,
                    target = targetDeviceID!!,
                    data = Gson().toJson(iceCandidate)
                )
            )
        }
    }

    fun closeConnection() {
        runCatching {
            peerConnection?.close()
        }
    }

    interface Listener {
        fun onTransferEventToSocket(data: DataModel)
    }
}