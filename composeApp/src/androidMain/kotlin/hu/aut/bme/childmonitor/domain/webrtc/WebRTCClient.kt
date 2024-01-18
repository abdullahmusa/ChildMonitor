package hu.aut.bme.childmonitor.domain.webrtc

import android.content.Context
import com.google.gson.Gson
import hu.aut.bme.childmonitor.data.model.DeviceRole
import org.webrtc.*

class WebRTCClient(
    private val context: Context,
) {
    companion object {
        private const val serverUri = "" // TODO: add your TURN/STUN server URI here
        private const val serverUsername = "" // TODO: add your TURN/STUN server username here
        private const val serverPassword = "" // TODO: add your TURN/STUN server password here
    }

    var listener: Listener? = null
    var deviceID: String = ""
    lateinit var deviceRole: DeviceRole
    private var targetDeviceID: String? = null

    private val eglBaseContext = EglBase.create().eglBaseContext
    private val peerConnectionFactory by lazy { createPeerConnectionFactory() }
    private var peerConnection: PeerConnection? = null
    private val iceServer = listOf(
        PeerConnection.IceServer
            .builder(serverUri)
            .setUsername(serverUsername)
            .setPassword(serverPassword)
            .createIceServer()
    )
    private val localVideoSource by lazy { peerConnectionFactory.createVideoSource(false) }
    private val localAudioSource by lazy { peerConnectionFactory.createAudioSource(MediaConstraints()) }
    private val videoCapturer = getVideoCapturer(context)
    private var surfaceTextureHelper: SurfaceTextureHelper? = null
    private val mediaConstraint = MediaConstraints().apply {
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
    }

    private var localSurfaceView: SurfaceViewRenderer? = null
    private var remoteSurfaceView: SurfaceViewRenderer? = null
    private var localStream: MediaStream? = null
    private val localTrackId
        get() = "${deviceID}_track"
    private val localStreamId
        get() = "${deviceID}_stream"
    private var localAudioTrack: AudioTrack? = null
    private var localVideoTrack: VideoTrack? = null

    init {
        assert(serverUri.isNotEmpty()) {
            "TURN/STUN server URI is empty!"
        }
        assert(serverUsername.isNotEmpty()) {
            "TURN/STUN server username is empty!"
        }
        assert(serverPassword.isNotEmpty()) {
            "TURN/STUN server password is empty!"
        }
        initPeerConnectionFactory()
    }

    private fun initPeerConnectionFactory() {
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .setEnableInternalTracer(true).setFieldTrials("WebRTC-H264HighProfile/Enabled/")
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)
    }

    private fun createPeerConnectionFactory(): PeerConnectionFactory {
        return PeerConnectionFactory.builder()
            .setVideoDecoderFactory(
                DefaultVideoDecoderFactory(eglBaseContext)
            ).setVideoEncoderFactory(
                DefaultVideoEncoderFactory(
                    eglBaseContext, true, true
                )
            ).setOptions(PeerConnectionFactory.Options().apply {
                disableNetworkMonitor = false
                disableEncryption = false
            }).createPeerConnectionFactory()
    }

    fun observePeerConnection(observer: PeerConnection.Observer) {
        peerConnection = createPeerConnection(observer)
    }

    private fun createPeerConnection(observer: PeerConnection.Observer): PeerConnection? {
        return peerConnectionFactory.createPeerConnection(iceServer, observer)
    }

    fun createOffer(targetID: String) {
        targetDeviceID = targetID
        peerConnection?.createOffer(object : MySdpObserver() {
            override fun onCreateSuccess(desc: SessionDescription?) {
                super.onCreateSuccess(desc)
                peerConnection?.setLocalDescription(object : MySdpObserver() {
                    override fun onSetSuccess() {
                        super.onSetSuccess()
                        listener?.runCatching {
                            onTransferEventToSocket(
                                DataModel(
                                    type = DataModelType.Offer,
                                    sender = deviceID,
                                    target = targetID,
                                    data = desc?.description?.replace(
                                        oldValue = "\r\na=extmap-allow-mixed",
                                        newValue = ""
                                    )
                                )
                            )
                        }
                    }
                }, desc)
            }
        }, mediaConstraint)
    }

    fun answer(targetID: String) {
        targetDeviceID = targetID
        peerConnection?.createAnswer(object : MySdpObserver() {
            override fun onCreateSuccess(desc: SessionDescription?) {
                super.onCreateSuccess(desc)
                peerConnection?.setLocalDescription(object : MySdpObserver() {
                    override fun onSetSuccess() {
                        super.onSetSuccess()
                        listener?.runCatching {
                            onTransferEventToSocket(
                                DataModel(
                                    type = DataModelType.Answer,
                                    sender = deviceID,
                                    target = targetID,
                                    data = desc?.description
                                )
                            )
                        }
                    }
                }, desc)
            }
        }, mediaConstraint)
    }

    fun onRemoteSessionReceived(sessionDescription: SessionDescription) {
        peerConnection?.setRemoteDescription(MySdpObserver(), sessionDescription)
    }

    fun addIceCandidateToPeer(iceCandidate: IceCandidate) {
        peerConnection?.addIceCandidate(iceCandidate)
    }

    fun sendIceCandidate(iceCandidate: IceCandidate) {
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
            videoCapturer.dispose()
            localStream?.dispose()
            peerConnection?.close()
            peerConnection = null
        }
    }

    fun switchCamera() {
        videoCapturer.switchCamera(null)
    }

    fun toggleAudio(shouldBeMuted: Boolean) {
        if (shouldBeMuted) {
            localStream?.removeTrack(localAudioTrack)
        } else {
            localStream?.addTrack(localAudioTrack)
        }
    }

    fun toggleVideo(shouldBeMuted: Boolean) {
        runCatching {
            if (shouldBeMuted) {
                stopCapturingCamera()
            } else {
                startCapturingCamera()
            }
        }
    }

    //streaming section
    private fun initSurfaceView(view: SurfaceViewRenderer) {
        view.run {
            setMirror(false)
            setEnableHardwareScaler(true)
            init(eglBaseContext, null)
        }
    }

    fun initRemoteSurfaceView(remoteView: SurfaceViewRenderer) {
        this.remoteSurfaceView = remoteView
        initSurfaceView(remoteView)
    }

    fun initLocalSurfaceView(localView: SurfaceViewRenderer) {
        this.localSurfaceView = localView
        initSurfaceView(localView)
    }

    fun startLocalStreaming() {
        localStream = peerConnectionFactory.createLocalMediaStream(localStreamId)
        localAudioTrack =
            peerConnectionFactory.createAudioTrack(localTrackId + "_audio", localAudioSource)
        peerConnection?.addStream(localStream)
    }

    private fun startCapturingCamera() {
        localSurfaceView?.let { localView ->
            surfaceTextureHelper = SurfaceTextureHelper.create(
                Thread.currentThread().name, eglBaseContext
            )

            videoCapturer.initialize(
                surfaceTextureHelper, context, localVideoSource.capturerObserver
            )

            videoCapturer.startCapture(
                720, 480, 20
            )

            localVideoTrack =
                peerConnectionFactory.createVideoTrack(localTrackId + "_video", localVideoSource)
            localVideoTrack?.addSink(localView)
            localStream?.addTrack(localVideoTrack)
        }
    }

    private fun getVideoCapturer(context: Context): CameraVideoCapturer =
        Camera2Enumerator(context).run {
            deviceNames.find {
                isFrontFacing(it)
            }?.let {
                createCapturer(it, null)
            } ?: throw IllegalStateException()
        }

    private fun stopCapturingCamera() {
        videoCapturer.dispose()
        localVideoTrack?.removeSink(localSurfaceView)
        localSurfaceView?.clearImage()
        localStream?.removeTrack(localVideoTrack)
        localVideoTrack?.dispose()
    }

    interface Listener {
        fun onTransferEventToSocket(data: DataModel)
    }
}