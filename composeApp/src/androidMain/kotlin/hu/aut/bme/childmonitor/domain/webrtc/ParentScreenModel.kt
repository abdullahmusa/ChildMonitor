package hu.aut.bme.childmonitor.domain.webrtc

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import hu.aut.bme.childmonitor.data.model.DeviceRole
import hu.aut.bme.childmonitor.data.FirebaseClient
import hu.aut.bme.childmonitor.data.model.DeviceStatus
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.SurfaceViewRenderer

class ParentScreenModel(
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
    override val deviceRole: DeviceRole = DeviceRole.Parent
    override val targetDeviceRole: DeviceRole = DeviceRole.Child

    var remoteView: SurfaceViewRenderer? = null
    var peerConnectionState: PeerConnection.PeerConnectionState by mutableStateOf(PeerConnection.PeerConnectionState.NEW)
    val childrenStatuses = firebaseClient.childrenStatuses

    init {
        firebaseClient.deviceRole = deviceRole
        webRTCClient.deviceRole = deviceRole
    }

    override fun initWebrtcClient(
        onAddStream: (MediaStream) -> Unit,
        onIceCandidate: (IceCandidate) -> Unit,
        onConnectionChange: (PeerConnection.PeerConnectionState) -> Unit,
    ) {
        peerConnectionState = PeerConnection.PeerConnectionState.NEW

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
                peerConnectionState = newState

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

    fun connectToTarget(targetID: String) {
        webRTCClient.createOffer(targetID)
    }

    fun observeChildrenStatus() {
        firebaseClient.observeChildrenStatus()
    }
}