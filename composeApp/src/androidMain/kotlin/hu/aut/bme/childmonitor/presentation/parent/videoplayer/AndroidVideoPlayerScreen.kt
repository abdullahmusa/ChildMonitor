package hu.aut.bme.childmonitor.presentation.parent.videoplayer

import android.content.res.Configuration
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import cafe.adriel.voyager.core.lifecycle.LifecycleEffect
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import hu.aut.bme.childmonitor.data.model.DeviceStatus
import hu.aut.bme.childmonitor.domain.webrtc.ParentScreenModel
import hu.aut.bme.childmonitor.rememberDarkMode
import hu.aut.bme.childmonitor.rememberLightMode
import hu.aut.bme.childmonitor.theme.LocalThemeIsDark
import org.webrtc.PeerConnection
import org.webrtc.SurfaceViewRenderer

class VideoPlayerScreen constructor(
    private val targetDeviceID: String,
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val parentScreenModel = getScreenModel<ParentScreenModel>()
        val orientation = LocalConfiguration.current.orientation
        val onBackNavigation = {
            parentScreenModel.closeConnection()
            parentScreenModel.updateDeviceStatus(DeviceStatus.ONLINE)
            navigator.pop()
        }

        LifecycleEffect(
            onStarted = {
                parentScreenModel.initWebrtcClient()
            }
        )

        BackHandler { onBackNavigation() }
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar =
            {
                when (orientation) {
                    Configuration.ORIENTATION_LANDSCAPE -> {}
                    else -> {
                        TopAppBar(
                            title = {
                                Text(text = "Child Monitor")
                            },
                            navigationIcon = {
                                IconButton(
                                    onClick = { onBackNavigation() }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "Back navigation",
                                    )
                                }
                            },
                            actions = {
                                var isDark by LocalThemeIsDark.current
                                IconButton(
                                    onClick = { isDark = !isDark }
                                ) {
                                    Icon(
                                        modifier = Modifier.padding(8.dp).size(20.dp),
                                        imageVector = if (isDark) rememberLightMode() else rememberDarkMode(),
                                        contentDescription = "Theme switcher",
                                    )
                                }
                            },
                        )
                    }
                }
            },
        )
        { paddingValues ->
            when (parentScreenModel.peerConnectionState) {
                PeerConnection.PeerConnectionState.FAILED,
                PeerConnection.PeerConnectionState.CLOSED,
                PeerConnection.PeerConnectionState.DISCONNECTED,
                -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = "Can't reach $targetDeviceID cam")
                    }
                }

                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .background(color = Color.Black),
                        contentAlignment = Alignment.Center,
                    ) {
                        AndroidView(
                            modifier = Modifier.fillMaxSize(),
                            factory = { context ->
                                SurfaceViewRenderer(context).apply {
                                    parentScreenModel.initRemoteSurfaceView(this)
                                    parentScreenModel.connectToTarget(targetDeviceID)
                                }
                            },
                        )
                        if (parentScreenModel.peerConnectionState != PeerConnection.PeerConnectionState.CONNECTED) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                CircularProgressIndicator()
                                Text(text = "Connecting...")
                            }
                        }
                    }
                }
            }
        }
    }
}