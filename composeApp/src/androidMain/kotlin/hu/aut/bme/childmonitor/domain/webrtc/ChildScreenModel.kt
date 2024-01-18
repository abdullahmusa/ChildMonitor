package hu.aut.bme.childmonitor.domain.webrtc

import android.content.Context
import hu.aut.bme.childmonitor.data.model.DeviceRole
import hu.aut.bme.childmonitor.data.model.DeviceStatus
import hu.aut.bme.childmonitor.data.FirebaseClient
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.SurfaceViewRenderer

class ChildScreenModel(
    context: Context,
    override val firebaseClient: FirebaseClient = FirebaseClient(),
    override val webRTCClient: WebRTCClient = WebRTCClient(context = context),
) : RoleScreenModel() {
    override var deviceID = ""
        set(value) {
            field = value
            firebaseClient.deviceID = value
            webRTCClient.deviceID = value
        }
    override val deviceRole: DeviceRole = DeviceRole.Child
    override val targetDeviceRole: DeviceRole = DeviceRole.Parent

    var remoteView: SurfaceViewRenderer? = null

    init {
        firebaseClient.deviceRole = deviceRole
        webRTCClient.deviceRole = deviceRole
    }

    override fun initWebrtcClient(
        onAddStream: (MediaStream) -> Unit,
        onIceCandidate: (IceCandidate) -> Unit,
        onConnectionChange: (PeerConnection.PeerConnectionState) -> Unit,
    ) {
        super.initWebrtcClient(
            onAddStream = {
                onAddStream(it)
                try {
                    it.videoTracks?.get(0)?.addSink(remoteView)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            onIceCandidate = {
                webRTCClient.sendIceCandidate(it)
            },
            onConnectionChange = { newState ->
                when (newState) {
                    PeerConnection.PeerConnectionState.CONNECTED -> {
                        updateDeviceStatus(status = DeviceStatus.STREAMING)
                    }

                    else -> {
                        updateDeviceStatus(status = DeviceStatus.ONLINE)
                    }
                }
            },
        )
    }

    fun initLocalSurfaceView(view: SurfaceViewRenderer) {
        webRTCClient.initLocalSurfaceView(view)
    }

    fun initRemoteSurfaceView(view: SurfaceViewRenderer) {
        this.remoteView = view
        webRTCClient.initRemoteSurfaceView(view)
    }
}