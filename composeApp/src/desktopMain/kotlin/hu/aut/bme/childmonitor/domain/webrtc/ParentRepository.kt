package hu.aut.bme.childmonitor.domain.webrtc

import com.google.gson.Gson
import dev.onvoid.webrtc.PeerConnectionObserver
import dev.onvoid.webrtc.RTCDataChannel
import dev.onvoid.webrtc.RTCIceCandidate
import dev.onvoid.webrtc.RTCIceConnectionState
import dev.onvoid.webrtc.RTCPeerConnectionState
import dev.onvoid.webrtc.RTCRtpReceiver
import dev.onvoid.webrtc.RTCRtpTransceiver
import dev.onvoid.webrtc.RTCSdpType
import dev.onvoid.webrtc.RTCSessionDescription
import dev.onvoid.webrtc.media.MediaStream
import dev.onvoid.webrtc.media.MediaStreamTrack
import dev.onvoid.webrtc.media.video.I420Buffer
import dev.onvoid.webrtc.media.video.VideoTrack
import hu.aut.bme.childmonitor.data.FirebaseClient
import hu.aut.bme.childmonitor.data.model.DeviceRole
import hu.aut.bme.childmonitor.data.model.DeviceStatus
import java.awt.Image
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.SwingConstants


class ParentRepository : WebRTCClient.Listener {
    val imageLabel: JLabel = JLabel()
    val firebaseClient: FirebaseClient = FirebaseClient()
    val webRTCClient: WebRTCClient = WebRTCClient()
    var deviceID: String = ""
        set(value) {
            field = value
            firebaseClient.deviceID = value
            webRTCClient.deviceID = value
        }
    var deviceRole: DeviceRole = DeviceRole.Parent
    val targetDeviceRole: DeviceRole = DeviceRole.Child
    val childrenStatuses = firebaseClient.childrenStatuses

    init {
        imageLabel.apply {
            horizontalAlignment = SwingConstants.CENTER
            verticalAlignment = SwingConstants.CENTER
            border = null
        }
        firebaseClient.deviceRole = deviceRole
        webRTCClient.deviceRole = deviceRole
    }

    fun initFirebase() {
        firebaseClient.apply {
            deviceID = this@ParentRepository.deviceID
            deviceRole = this@ParentRepository.deviceRole

            observeLatestEvent(object : FirebaseClient.Listener {
                override fun onLatestEventReceived(event: DataModel) {
                    firebaseClient.clearLatestEvent()

                    when (event.type) {
                        DataModelType.Offer -> {
                            webRTCClient.onRemoteSessionReceived(
                                RTCSessionDescription(
                                    RTCSdpType.OFFER,
                                    event.data.toString()
                                )
                            )
                            webRTCClient.answer(event.sender)
                        }

                        DataModelType.Answer -> {
                            webRTCClient.onRemoteSessionReceived(
                                RTCSessionDescription(
                                    RTCSdpType.ANSWER,
                                    event.data.toString()
                                )
                            )
                        }

                        DataModelType.IceCandidates -> {
                            val candidate: RTCIceCandidate? = try {
                                Gson().fromJson(event.data.toString(), RTCIceCandidate::class.java)
                            } catch (e: Exception) {
                                null
                            }
                            candidate?.let {
                                webRTCClient.addIceCandidateToPeer(it)
                            }
                        }

                        else -> {}
                    }
                }
            })
        }
    }

    fun initWebrtcClient(
        onAddStream: (MediaStream) -> Unit = {},
        onIceCandidate: (RTCIceCandidate) -> Unit = {},
        onConnectionChange: (RTCPeerConnectionState) -> Unit = {},
    ) {
        if (firebaseClient.currentUID == null) {
            throw Exception("FirebaseClient is not initialized")
        }

        webRTCClient.apply {
            listener = this@ParentRepository
            deviceID = this@ParentRepository.deviceID
            deviceRole = this@ParentRepository.deviceRole
            observePeerConnection(
                observer = object : PeerConnectionObserver {
                    override fun onAddStream(p0: MediaStream?) {
                        super.onAddStream(p0)
                        p0?.let {
                            onAddStream(it)
                        }
                    }

                    override fun onRemoveStream(stream: MediaStream?) {
                        super.onRemoveStream(stream)
                    }

                    override fun onDataChannel(dataChannel: RTCDataChannel?) {
                        super.onDataChannel(dataChannel)
                    }

                    override fun onAddTrack(
                        receiver: RTCRtpReceiver?,
                        mediaStreams: Array<out MediaStream>?,
                    ) {
                        super.onAddTrack(receiver, mediaStreams)
                    }

                    override fun onRemoveTrack(receiver: RTCRtpReceiver?) {
                        super.onRemoveTrack(receiver)
                    }

                    override fun onTrack(transceiver: RTCRtpTransceiver?) {
                        super.onTrack(transceiver)
                        val track = transceiver?.receiver?.track ?: return

                        if (track.kind == MediaStreamTrack.VIDEO_TRACK_KIND) {
                            val videoTrack = track as VideoTrack
                            videoTrack.addSink {
                                it?.let { frame ->
                                    try {
                                        displayImage(frame.buffer.toI420())
                                    } catch (e: Exception) {
                                        println("Exception: ${e.message}")
                                    } finally {
                                    }
                                }
                            }
                        }
                    }

                    override fun onIceCandidate(p0: RTCIceCandidate?) {
                        p0?.let {
                            onIceCandidate(it)
                            webRTCClient.sendIceCandidate(it)
                        }
                    }

                    override fun onConnectionChange(newState: RTCPeerConnectionState?) {
                        super.onConnectionChange(newState)
                        newState?.let {
                            onConnectionChange(it)
                        }
                    }

                    override fun onStandardizedIceConnectionChange(state: RTCIceConnectionState?) {
                        super.onStandardizedIceConnectionChange(state)
                    }

                    override fun onIceConnectionReceivingChange(receiving: Boolean) {
                        super.onIceConnectionReceivingChange(receiving)
                    }
                },
            )
        }
    }

    fun updateDeviceStatus(status: DeviceStatus) {
        firebaseClient.updateDeviceStatus(status)
    }

    fun closeConnection() {
        webRTCClient.closeConnection()
        updateDeviceStatus(DeviceStatus.ONLINE)
    }

    override fun onTransferEventToSocket(data: DataModel) {
        firebaseClient.sendMessageToDevice(
            targetRole = targetDeviceRole,
            message = data,
        )
    }

    fun connectToTarget(targetID: String) {
        firebaseClient.sendMessageToDevice(
            targetRole = targetDeviceRole,
            message = DataModel(
                type = DataModelType.RequestOffer,
                sender = deviceID,
                target = targetID,
                data = null
            ),
        )
    }

    fun observeChildrenStatus() {
        firebaseClient.observeChildrenStatus()
    }

    fun displayImage(buffer: I420Buffer) {
        val width = buffer.width
        val height = buffer.height
        val image = BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR)
        val targetPixels = (image.raster.dataBuffer as DataBufferByte).data

        for (y in 0 until height) {
            for (x in 0 until width) {
                val Y = buffer.dataY.get(y * buffer.strideY + x).toInt() and 0xFF
                val U = buffer.dataU.get(y / 2 * buffer.strideU + x / 2).toInt() and 0xFF
                val V = buffer.dataV.get(y / 2 * buffer.strideV + x / 2).toInt() and 0xFF

                val C = Y - 16
                val D = U - 128
                val E = V - 128

                val R = ((298 * C + 409 * E + 128) shr 8).coerceIn(0, 255)
                val G = ((298 * C - 100 * D - 208 * E + 128) shr 8).coerceIn(0, 255)
                val B = ((298 * C + 516 * D + 128) shr 8).coerceIn(0, 255)

                targetPixels[(y * width + x) * 3] = B.toByte()
                targetPixels[(y * width + x) * 3 + 1] = G.toByte()
                targetPixels[(y * width + x) * 3 + 2] = R.toByte()
            }
        }

        imageLabel.icon = ImageIcon(
            image.getScaledInstance(
                imageLabel.width,
                imageLabel.height,
                Image.SCALE_DEFAULT
            )
        )
    }
}