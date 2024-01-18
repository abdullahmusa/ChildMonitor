package hu.aut.bme.childmonitor.presentation.child.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import hu.aut.bme.childmonitor.data.model.DeviceStatus
import hu.aut.bme.childmonitor.domain.webrtc.ChildScreenModel
import hu.aut.bme.childmonitor.presentation.common.components.HomeMenu
import org.webrtc.SurfaceViewRenderer

actual class ChildHomeScreen actual constructor(
    private val deviceName: String,
    private val startWithVideo: Boolean,
) : Screen {

    @Composable
    override fun Content() {
        var isVideoMuted by remember { mutableStateOf(!startWithVideo) }
        var isAudioMuted by remember { mutableStateOf(false) }
        val childScreenModel = getScreenModel<ChildScreenModel>().apply {
            deviceID = deviceName
            initFirebase()
            initWebrtcClient()
            updateDeviceStatus(DeviceStatus.ONLINE)
        }

        Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                factory = { context ->
                    SurfaceViewRenderer(context).also {
                        childScreenModel.apply {
                            initLocalSurfaceView(it)
                            startLocalStreaming()
                            toggleVideo(shouldBeMuted = isVideoMuted)
                            toggleAudio(shouldBeMuted = isAudioMuted)
                        }
                    }
                },
            )
            Row(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(bottom = 16.dp)
                    .fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom,
            ) {
                IconButton(
                    modifier = Modifier.drawBehind {
                        drawCircle(Color.Black.copy(alpha = 0.5f))
                    },
                    onClick = {
                        childScreenModel.switchCamera()
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        tint = Color.White,
                        contentDescription = "Switch",
                    )
                }
                IconButton(
                    modifier = Modifier.drawBehind {
                        drawCircle(Color.Black.copy(alpha = 0.5f))
                    },
                    onClick = {
                        isVideoMuted = !isVideoMuted
                        childScreenModel.toggleVideo(shouldBeMuted = isVideoMuted)
                    },
                ) {
                    Icon(
                        imageVector = if (isVideoMuted) Icons.Default.VideocamOff else Icons.Default.Videocam,
                        tint = Color.White,
                        contentDescription = "Toggle Video",
                    )
                }
                IconButton(
                    modifier = Modifier.drawBehind {
                        drawCircle(Color.Black.copy(alpha = 0.5f))
                    },
                    onClick = {
                        isAudioMuted = !isAudioMuted
                        childScreenModel.toggleAudio(shouldBeMuted = isAudioMuted)
                    },
                ) {
                    Icon(
                        imageVector = if (isAudioMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        tint = Color.White,
                        contentDescription = "Toggle Audio",
                    )
                }
                HomeMenu(
                    iconModifier = Modifier.drawBehind {
                        drawCircle(Color.Black.copy(alpha = 0.5f))
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            tint = Color.White,
                            contentDescription = "More options",
                        )
                    },
                    onLogOut = {
                        Firebase.auth.signOut()
                    },
                )
            }
        }
    }
}