package hu.aut.bme.childmonitor.presentation.child.home

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen

actual class ChildHomeScreen actual constructor(
    deviceName: String,
    startWithVideo: Boolean,
) :
    Screen {
    @Composable
    override fun Content() {
        Box(
            contentAlignment = androidx.compose.ui.Alignment.Center,
        ) {
            Text(text = "Such emptiness...")
        }
    }
}