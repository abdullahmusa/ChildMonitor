package hu.aut.bme.childmonitor.presentation.common.login

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import hu.aut.bme.childmonitor.domain.auth.AuthResult
import hu.aut.bme.childmonitor.domain.auth.AuthService
import hu.aut.bme.childmonitor.domain.auth.AuthenticationState
import hu.aut.bme.childmonitor.presentation.common.roleselector.RoleSelectorScreen
import hu.aut.bme.childmonitor.rememberDarkMode
import hu.aut.bme.childmonitor.rememberLightMode
import hu.aut.bme.childmonitor.theme.LocalThemeIsDark
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class LoginScreen : Screen, KoinComponent {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        var email by remember { mutableStateOf("") }
        var emailError by remember { mutableStateOf<String?>(null) }
        var password by remember { mutableStateOf("") }
        var passwordError by remember { mutableStateOf<String?>(null) }
        val authService by inject<AuthService>()
        val authenticationState by authService.authenticationState.collectAsState(initial = AuthenticationState.LoggedOut)
        var isSignUp by remember { mutableStateOf(true) }
        val interactionSource = remember { MutableInteractionSource() }

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
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues = paddingValues),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                item {
                    Row(
                        modifier = Modifier.padding(bottom = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        Text(
                            modifier = Modifier.clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) {
                                isSignUp = true
                            },
                            text = "Sign Up",
                            fontSize = MaterialTheme.typography.displaySmall.fontSize,
                            fontWeight = if (isSignUp) FontWeight.ExtraBold else FontWeight.ExtraLight,
                        )
                        Text(
                            modifier = Modifier.clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) {
                                isSignUp = false
                            },
                            text = "Log In",
                            fontSize = MaterialTheme.typography.displaySmall.fontSize,
                            fontWeight = if (isSignUp) FontWeight.ExtraLight else FontWeight.ExtraBold,
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = null
                        },
                        singleLine = true,
                        label = { Text(text = "Email") },
                        isError = emailError != null,
                        colors = if (emailError != null) {
                            OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.error,
                                unfocusedBorderColor = MaterialTheme.colorScheme.error,
                            )
                        } else {
                            OutlinedTextFieldDefaults.colors()
                        },
                        supportingText = {
                            if (emailError != null) {
                                Text(text = emailError!!)
                            }
                        },
                        trailingIcon = {
                            if (emailError != null) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Email error"
                                )
                            }
                        },
                    )
                }

                item {
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = null
                        },
                        singleLine = true,
                        label = { Text(text = "Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        isError = passwordError != null,
                        colors = if (passwordError != null) {
                            OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.error,
                                unfocusedBorderColor = MaterialTheme.colorScheme.error,
                            )
                        } else {
                            OutlinedTextFieldDefaults.colors()
                        },
                        supportingText = {
                            if (passwordError != null) {
                                Text(text = passwordError!!)
                            }
                        },
                        trailingIcon = {
                            if (passwordError != null) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Password error"
                                )
                            }
                        },
                    )
                }

                item {
                    when (authenticationState) {
                        is AuthenticationState.Loading -> {
                            CircularProgressIndicator()
                        }

                        else -> {
                            Button(
                                modifier = Modifier
                                    .padding(vertical = 24.dp)
                                    .animateContentSize(),
                                enabled = isEmailValid(email) && isPasswordValid(password),
                                onClick = {
                                    scope.launch {
                                        val resultHandler: (AuthResult) -> Unit = { result ->
                                            when (result) {
                                                is AuthResult.Success -> navigator.replace(
                                                    RoleSelectorScreen()
                                                )

                                                is AuthResult.EmailFailure -> {
                                                    emailError = result.error
                                                }

                                                is AuthResult.PasswordFailure -> {
                                                    passwordError = result.error
                                                }

                                                is AuthResult.Failure -> {
                                                    emailError = result.error
                                                    passwordError = result.error
                                                }

                                                else -> {

                                                }
                                            }
                                        }

                                        if (isSignUp) {
                                            authService.signUpWithEmailAndPassword(
                                                email = email,
                                                password = password,
                                            ).first().also(resultHandler)
                                        } else {
                                            authService.loginWithEmailAndPassword(
                                                email = email,
                                                password = password,
                                            ).first().also(resultHandler)
                                        }
                                    }
                                },
                            ) {
                                Text(text = if (isSignUp) "Sign Up" else "Log In")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun isEmailValid(email: String): Boolean {
        return email.contains('@')
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length >= 6
    }
}