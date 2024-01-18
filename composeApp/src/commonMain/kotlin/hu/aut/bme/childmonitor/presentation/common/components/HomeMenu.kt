package hu.aut.bme.childmonitor.presentation.common.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import hu.aut.bme.childmonitor.presentation.common.login.LoginScreen
import hu.aut.bme.childmonitor.presentation.common.roleselector.RoleSelectorScreen
import hu.aut.bme.childmonitor.theme.LocalThemeIsDark

@Composable
fun HomeMenu(
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier,
    icon: @Composable () -> Unit = { MoreVertIcon() },
    onChangeRole: () -> Unit = {},
    onSwitchTheme: () -> Unit = {},
    onLogOut: () -> Unit = {},
) {
    val navigator = LocalNavigator.currentOrThrow
    var isDark by LocalThemeIsDark.current
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
    ) {
        IconButton(
            modifier = iconModifier,
            onClick = { expanded = true },
        ) {
            icon()
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(text = "Change role") },
                onClick = {
                    onChangeRole()
                    navigator.replaceAll(RoleSelectorScreen())
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Change role"
                    )
                }
            )
            DropdownMenuItem(
                text = { Text(text = "Switch theme") },
                onClick = {
                    onSwitchTheme()
                    isDark = !isDark
                },
                leadingIcon = {
                    Icon(
                        imageVector = if (isDark) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                        contentDescription = "Switch theme"
                    )
                }
            )
            DropdownMenuItem(
                text = { Text(text = "Log out") },
                onClick = {
                    onLogOut()
                    navigator.replaceAll(LoginScreen())
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Log out"
                    )
                }
            )
        }
    }
}

@Composable
private fun MoreVertIcon() {
    Icon(
        imageVector = Icons.Default.MoreVert,
        contentDescription = "More options"
    )
}
