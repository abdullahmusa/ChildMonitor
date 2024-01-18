package hu.aut.bme.childmonitor.domain.webrtc

import android.util.Log
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription

open class MySdpObserver : SdpObserver {
    override fun onCreateSuccess(desc: SessionDescription?) {
        Log.d("onCreateSuccess: ", "onCreateSuccess ${desc?.description}")
    }

    override fun onSetSuccess() {
        Log.d("onSetSuccess: ", "onSetSuccess")
    }

    override fun onCreateFailure(p0: String?) {
        Log.d("onCreateFailure: ", p0.toString())
    }

    override fun onSetFailure(p0: String?) {
        Log.d("onSetFailure: ", p0.toString())
    }
}