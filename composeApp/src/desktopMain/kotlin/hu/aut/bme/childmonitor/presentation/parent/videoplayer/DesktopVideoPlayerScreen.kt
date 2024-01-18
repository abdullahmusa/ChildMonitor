package hu.aut.bme.childmonitor.presentation.parent.videoplayer

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.lifecycle.LifecycleEffect
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import hu.aut.bme.childmonitor.domain.webrtc.ParentRepository
import hu.aut.bme.childmonitor.data.model.DeviceStatus
import hu.aut.bme.childmonitor.rememberDarkMode
import hu.aut.bme.childmonitor.rememberLightMode
import hu.aut.bme.childmonitor.theme.LocalThemeIsDark

class VideoPlayerScreen constructor(
    private val parentRepository: ParentRepository,
    private val targetDeviceID: String,
) :
    Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        LifecycleEffect(
            onStarted = {
                parentRepository.initWebrtcClient()
                parentRepository.connectToTarget(targetDeviceID)
            },
            onDisposed = {
                parentRepository.closeConnection()
                parentRepository.updateDeviceStatus(DeviceStatus.OFFLINE)
            },
        )

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Text(text = "Child Monitor")
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { navigator.pop() }
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
            },
        ) { paddingValues ->
            SwingPanel(
                modifier = Modifier
                    .padding(paddingValues = paddingValues)
                    .fillMaxSize(),
                factory = {
                    parentRepository.imageLabel
                },
                update = {

                },
            )
        }
    }
}