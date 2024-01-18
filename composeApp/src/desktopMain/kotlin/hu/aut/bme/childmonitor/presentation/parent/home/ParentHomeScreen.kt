package hu.aut.bme.childmonitor.presentation.parent.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.lifecycle.LifecycleEffect
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import hu.aut.bme.childmonitor.data.model.DeviceRole
import hu.aut.bme.childmonitor.data.model.DeviceStatus
import hu.aut.bme.childmonitor.domain.webrtc.ParentRepository
import hu.aut.bme.childmonitor.presentation.common.components.HomeMenu
import hu.aut.bme.childmonitor.presentation.common.components.VideoItem
import hu.aut.bme.childmonitor.presentation.parent.videoplayer.VideoPlayerScreen

actual class ParentHomeScreen actual constructor(private val deviceName: String) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val parentRepository = remember { ParentRepository() }

        LifecycleEffect(
            onStarted = {
                parentRepository.apply {
                    deviceID = deviceName
                    deviceRole = DeviceRole.Parent
                    initFirebase()
                    observeChildrenStatus()
                    updateDeviceStatus(DeviceStatus.ONLINE)
                }
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
                    actions = {
                        HomeMenu(
                            onLogOut = {

                            }
                        )
                    },
                )
            }
        ) { paddingValues ->
            LazyVerticalGrid(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                contentPadding = paddingValues,
                columns = GridCells.Adaptive(200.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(parentRepository.childrenStatuses) {
                    VideoItem(
                        deviceID = it.first,
                        deviceStatus = it.second,
                        onClick = {
                            if (it.second == DeviceStatus.ONLINE) {
                                navigator.push(VideoPlayerScreen(parentRepository, it.first))
                            }
                        },
                    )
                }
            }
        }
    }
}