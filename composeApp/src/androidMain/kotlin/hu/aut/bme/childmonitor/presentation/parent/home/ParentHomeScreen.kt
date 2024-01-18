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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import hu.aut.bme.childmonitor.data.model.DeviceStatus
import hu.aut.bme.childmonitor.domain.webrtc.ParentScreenModel
import hu.aut.bme.childmonitor.presentation.common.components.HomeMenu
import hu.aut.bme.childmonitor.presentation.common.components.VideoItem
import hu.aut.bme.childmonitor.presentation.parent.videoplayer.VideoPlayerScreen

actual class ParentHomeScreen actual constructor(
    private val deviceName: String,
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val parentScreenModel = getScreenModel<ParentScreenModel>().apply {
            if (deviceID.isEmpty()) {
                deviceID = deviceName
                initFirebase()
                observeChildrenStatus()
                updateDeviceStatus(DeviceStatus.ONLINE)
            }
        }

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
                                Firebase.auth.signOut()
                            },
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
                items(parentScreenModel.childrenStatuses) {
                    VideoItem(
                        deviceID = it.first,
                        deviceStatus = it.second,
                        onClick = {
                            if (it.second == DeviceStatus.ONLINE) {
                                navigator.push(VideoPlayerScreen(it.first))
                            }
                        },
                    )
                }
            }
        }
    }
}