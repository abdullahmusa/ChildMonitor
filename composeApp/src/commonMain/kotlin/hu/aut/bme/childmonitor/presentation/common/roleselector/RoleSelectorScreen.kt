package hu.aut.bme.childmonitor.presentation.common.roleselector

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import hu.aut.bme.childmonitor.data.model.DeviceRole
import hu.aut.bme.childmonitor.presentation.child.home.ChildHomeScreen
import hu.aut.bme.childmonitor.presentation.parent.home.ParentHomeScreen
import hu.aut.bme.childmonitor.rememberDarkMode
import hu.aut.bme.childmonitor.rememberLightMode
import hu.aut.bme.childmonitor.theme.LocalThemeIsDark
import java.awt.SystemColor.text

class RoleSelectorScreen : Screen {
    @OptIn(
        ExperimentalLayoutApi::class, ExperimentalMaterial3WindowSizeClassApi::class
    )
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        var deviceRole by remember { mutableStateOf<DeviceRole?>(null) }
        var startWithVideo by remember { mutableStateOf(true) }

        val windowSizeClass = calculateWindowSizeClass()
        val windowHeight = windowSizeClass.heightSizeClass
        val windowWidth = windowSizeClass.widthSizeClass
        val (titleTextSize, bodyFontSize) =
            when {
                (windowWidth == WindowWidthSizeClass.Compact ||
                        windowHeight == WindowHeightSizeClass.Compact) -> {
                    listOf(
                        MaterialTheme.typography.displaySmall.fontSize,
                        MaterialTheme.typography.titleSmall.fontSize
                    )
                }

                (windowWidth == WindowWidthSizeClass.Medium ||
                        windowHeight == WindowHeightSizeClass.Medium) -> {
                    listOf(
                        MaterialTheme.typography.displayMedium.fontSize,
                        MaterialTheme.typography.titleMedium.fontSize
                    )
                }

                (windowWidth == WindowWidthSizeClass.Expanded ||
                        windowHeight == WindowHeightSizeClass.Expanded) -> {
                    listOf(
                        MaterialTheme.typography.displayLarge.fontSize,
                        MaterialTheme.typography.titleLarge.fontSize
                    )
                }

                else -> {
                    listOf(
                        MaterialTheme.typography.displaySmall.fontSize,
                        MaterialTheme.typography.titleSmall.fontSize
                    )
                }
            }

        var deviceName by remember { mutableStateOf("") }
        val deviceNamePlaceholder = when (deviceRole) {
            DeviceRole.Child -> "Child_1"
            DeviceRole.Parent -> "Mom"
            else -> ""
        }
        var deviceNameError by remember { mutableStateOf<String?>(null) }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            floatingActionButton = {
                var isDark by LocalThemeIsDark.current
                FloatingActionButton(
                    onClick = { isDark = !isDark }
                ) {
                    Icon(
                        modifier = Modifier.padding(8.dp).size(20.dp),
                        imageVector = if (isDark) rememberLightMode() else rememberDarkMode(),
                        contentDescription = "Theme switcher",
                    )
                }
            }
        )
        { paddingValues ->
            LazyColumn(
                modifier = Modifier.fillMaxSize()
                    .padding(paddingValues = paddingValues),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                item {
                    Text(
                        text = "Choose a role for this device.",
                        fontSize = titleTextSize,
                        textAlign = TextAlign.Center,
                        lineHeight = titleTextSize,
                    )
                }

                item {
                    FlowRow(
                        modifier = Modifier.padding(vertical = 24.dp),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        DeviceRole.entries.forEach { role ->
                            if (role == deviceRole) {
                                Button(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    onClick = { }
                                ) {
                                    Text(text = role.name, fontSize = bodyFontSize)
                                }
                            } else {
                                OutlinedButton(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    onClick = { deviceRole = role }
                                ) {
                                    Text(text = role.name, fontSize = bodyFontSize)
                                }
                            }
                        }
                    }
                }

                item {
                    AnimatedVisibility(
                        visible = deviceRole != null,
                        enter = fadeIn() + expandVertically()
                    ) {
                        val roleDescription = when (deviceRole) {
                            DeviceRole.Child -> "The Child role lets this device share " +
                                    "live video streams with the Parent devices within the group."

                            DeviceRole.Parent -> "The Parent role lets this device interact " +
                                    "with the Child devices within the group. " +
                                    "(eg. watching live video streams)"

                            else -> ""
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            AnimatedContent(
                                targetState = roleDescription,
                                transitionSpec = {
                                    fadeIn() togetherWith fadeOut()
                                }
                            ) {
                                Text(
                                    modifier = Modifier.fillMaxWidth(fraction = 0.6f),
                                    text = it,
                                    fontSize = bodyFontSize,
                                    textAlign = TextAlign.Center,
                                )
                            }
                            AnimatedVisibility(visible = deviceRole == DeviceRole.Child) {
                                Row {
                                    Row(
                                        modifier = Modifier
                                            .padding(horizontal = 4.dp)
                                            .clickable { startWithVideo = true },
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        RadioButton(
                                            selected = startWithVideo,
                                            onClick = { startWithVideo = true },
                                        )
                                        Text(text = "Enable video", fontSize = bodyFontSize)
                                    }
                                    Row(
                                        modifier = Modifier
                                            .padding(horizontal = 4.dp)
                                            .clickable { startWithVideo = false },
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        RadioButton(
                                            selected = !startWithVideo,
                                            onClick = { startWithVideo = false },
                                        )
                                        Text(text = "Audio only", fontSize = bodyFontSize)
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = deviceName,
                                onValueChange = {
                                    deviceName = it.replace(Regex("^[a-zA-Z0-9_-]*$"), "")
                                    deviceNameError = null
                                },
                                singleLine = true,
                                label = { Text(text = "Device name") },
                                isError = deviceNameError != null,
                                colors = if (deviceNameError != null) {
                                    OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.error,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.error,
                                    )
                                } else {
                                    OutlinedTextFieldDefaults.colors()
                                },
                                placeholder = { Text(text = deviceNamePlaceholder) },
                                supportingText = {
                                    if (deviceNameError != null) {
                                        Text(text = deviceNameError!!)
                                    }
                                },
                                trailingIcon = {
                                    if (deviceNameError != null) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = "Device name error"
                                        )
                                    }
                                },
                            )
                        }
                    }
                }

                item {
                    Button(
                        modifier = Modifier.padding(vertical = 24.dp),
                        enabled = deviceRole != null,
                        onClick = {
                            when (deviceRole) {
                                DeviceRole.Parent -> {
                                    navigator.replace(
                                        ParentHomeScreen(
                                            deviceName.ifEmpty { deviceNamePlaceholder }
                                        )
                                    )
                                }

                                DeviceRole.Child -> {
                                    navigator.replace(
                                        ChildHomeScreen(
                                            deviceName = deviceName.ifEmpty { deviceNamePlaceholder },
                                            startWithVideo = startWithVideo
                                        )
                                    )
                                }

                                else -> {}
                            }
                        },
                    ) {
                        Text(text = "Let's go!", fontSize = bodyFontSize)
                    }
                }
            }
        }
    }
}