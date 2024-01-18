package hu.aut.bme.childmonitor.domain.webrtc

import android.util.Log
import cafe.adriel.voyager.core.model.ScreenModel
import com.google.gson.Gson
import hu.aut.bme.childmonitor.data.model.DeviceRole
import hu.aut.bme.childmonitor.data.FirebaseClient
import hu.aut.bme.childmonitor.data.model.DeviceStatus
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.SessionDescription

abstract class RoleScreenModel : WebRTCClient.Listener, ScreenModel {
    protected abstract val firebaseClient: FirebaseClient
    protected abstract val webRTCClient: WebRTCClient
    abstract var deviceID: String
    protected abstract val deviceRole: DeviceRole
    protected abstract val targetDeviceRole: DeviceRole

    fun initFirebase() {
        firebaseClient.apply {
            deviceID = this@RoleScreenModel.deviceID
            deviceRole = this@RoleScreenModel.deviceRole

            observeLatestEvent(object : FirebaseClient.Listener {
                override fun onLatestEventReceived(event: DataModel) {
                    firebaseClient.clearLatestEvent()

                    when (event.type) {
                        DataModelType.Offer -> {
                            webRTCClient.onRemoteSessionReceived(
                                SessionDescription(
                                    SessionDescription.Type.OFFER,
                                    event.data.toString()
                                )
                            )
                            webRTCClient.answer(event.sender)
                        }

                        DataModelType.Answer -> {
                            webRTCClient.onRemoteSessionReceived(
                                SessionDescription(
                                    SessionDescription.Type.ANSWER,
                                    event.data.toString()
                                )
                            )
                        }

                        DataModelType.IceCandidates -> {
                            val candidate: IceCandidate? = try {
                                Gson().fromJson(event.data.toString(), IceCandidate::class.java)
                            } catch (e: Exception) {
                                null
                            }
                            candidate?.let {
                                webRTCClient.addIceCandidateToPeer(it)
                            }
                        }

                        DataModelType.RequestOffer -> {
                            webRTCClient.createOffer(event.sender)
                        }
                    }
                }
            })
        }
    }

    open fun initWebrtcClient(
        onAddStream: (MediaStream) -> Unit = {},
        onIceCandidate: (IceCandidate) -> Unit = {},
        onConnectionChange: (PeerConnection.PeerConnectionState) -> Unit = {},
    ) {
        if (firebaseClient.currentUID == null) {
            throw Exception("FirebaseClient is not initialized")
        }

        webRTCClient.apply {
            listener = this@RoleScreenModel
            deviceID = this@RoleScreenModel.deviceID
            deviceRole = this@RoleScreenModel.deviceRole

            observePeerConnection(
                observer = object : MyPeerObserver() {
                    override fun onAddStream(p0: MediaStream?) {
                        super.onAddStream(p0)
                        p0?.let {
                            onAddStream(it)
                        }
                    }

                    override fun onIceCandidate(p0: IceCandidate?) {
                        super.onIceCandidate(p0)
                        p0?.let {
                            onIceCandidate(it)
                        }
                    }

                    override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
                        super.onConnectionChange(newState)
                        newState?.let {
                            onConnectionChange(it)
                        }
                    }
                },
            )
        }
    }

    fun updateDeviceStatus(status: DeviceStatus) {
        firebaseClient.updateDeviceStatus(status)
    }

    fun startLocalStreaming() {
        webRTCClient.startLocalStreaming()
    }

    fun closeConnection() {
        webRTCClient.closeConnection()
        updateDeviceStatus(DeviceStatus.ONLINE)
    }

    fun switchCamera() {
        webRTCClient.switchCamera()
    }

    fun toggleVideo(shouldBeMuted: Boolean) {
        webRTCClient.toggleVideo(shouldBeMuted)
    }

    fun toggleAudio(shouldBeMuted: Boolean) {
        webRTCClient.toggleAudio(shouldBeMuted)
    }

    override fun onTransferEventToSocket(data: DataModel) {
        firebaseClient.sendMessageToDevice(
            targetRole = targetDeviceRole,
            message = data,
        )
    }

    override fun onDispose() {
        super.onDispose()
        closeConnection()
        updateDeviceStatus(DeviceStatus.OFFLINE)
    }
}