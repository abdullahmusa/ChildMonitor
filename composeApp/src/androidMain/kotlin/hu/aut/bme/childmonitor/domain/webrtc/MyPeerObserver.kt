package hu.aut.bme.childmonitor.domain.webrtc

import android.util.Log
import org.webrtc.*

open class MyPeerObserver : PeerConnection.Observer {
    override fun onSignalingChange(p0: PeerConnection.SignalingState?) {
        Log.d("MINEonSignalingChange: ", "onSignalingChange: $p0")
    }

    override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
        Log.d("MINEonIceConnectionChange: ", "onIceConnectionChange: $p0")
    }

    override fun onIceConnectionReceivingChange(p0: Boolean) {
        Log.d("MINEonIceConnectionReceivingChange: ", "onIceConnectionReceivingChange: ")
    }

    override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
        Log.d("MINEonIceGatheringChange: ", "onIceGatheringChange: ")
    }

    override fun onIceCandidate(p0: IceCandidate?) {
        Log.d("MINEonIceCandidate: ", "onIceCandidate: ")
    }

    override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {
        Log.d("MINEonIceCandidatesRemoved: ", "onIceCandidatesRemoved: ")
    }

    override fun onAddStream(p0: MediaStream?) {
        Log.d("MINEonAddStream: ", "onAddStream: ")
    }

    override fun onRemoveStream(p0: MediaStream?) {
        Log.d("MINEonRemoveStream: ", "onRemoveStream: ")
    }

    override fun onDataChannel(p0: DataChannel?) {
        Log.d("MINEonDataChannel: ", "onDataChannel: ")
    }

    override fun onRenegotiationNeeded() {
        Log.d("MINEonRenegotiationNeeded: ", "onRenegotiationNeeded: ")
    }

    override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {
        Log.d("MINEonAddTrack: ", "onAddTrack: ")
    }
}