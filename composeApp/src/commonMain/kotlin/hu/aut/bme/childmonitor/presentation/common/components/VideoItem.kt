package hu.aut.bme.childmonitor.presentation.common.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import hu.aut.bme.childmonitor.data.model.DeviceStatus

@Composable
fun VideoItem(
    deviceID: String,
    deviceStatus: DeviceStatus,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clickable(onClick = onClick)
            .background(Color.Black)
            .aspectRatio(16 / 9f),
    ) {
        Text(
            text = deviceID,
            color = Color.White,
            modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
        )
        Text(
            text = deviceStatus.name,
            color = Color.White,
            modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
        )
        Image(
            modifier = Modifier.align(Alignment.Center).size(48.dp),
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Video item",
            colorFilter = ColorFilter.tint(Color.White)
        )
    }
}