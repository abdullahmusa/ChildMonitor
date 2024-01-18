package hu.aut.bme.childmonitor.domain.auth

import com.google.gson.Gson
import hu.aut.bme.childmonitor.data.model.User
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow

actual class FirebaseAuthService : AuthService {
    companion object {
        private const val webApiKey = "" // TODO: add your Firebase Web API key here
        private const val logInUrl =
            "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key="
        private const val signUpUrl =
            "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key="
    }

    init {
        assert(webApiKey.isNotEmpty()) {
            "Firebase Web API key is empty!"
        }
    }

    private val httpClient = HttpClient()
    private val gson = Gson()

    private val _authenticationState =
        MutableStateFlow<AuthenticationState>(AuthenticationState.LoggedOut)
    override val authenticationState: Flow<AuthenticationState> = _authenticationState

    override suspend fun signUpWithEmailAndPassword(
        email: String,
        password: String,
    ): Flow<AuthResult> = callbackFlow {
        _authenticationState.value = AuthenticationState.Loading
        httpClient.post(signUpUrl + webApiKey) {
            contentType(ContentType.Application.Json)
            setBody(
                gson.toJson(
                    FirebaseAuthEmailAndPasswordRequest(
                        email = email,
                        password = password,
                        returnSecureToken = true,
                    )
                )
            )
        }.let { response ->
            when (response.status.value) {
                200 -> {
                    _authenticationState.value = AuthenticationState.LoggedIn
                    gson.fromJson(
                        response.bodyAsText(),
                        FirebaseAuthResponse.Success::class.java
                    ).let { successResponse ->
                        AuthService.currentUser = User(
                            uid = successResponse.localId,
                            email = successResponse.email,
                            token = successResponse.idToken,
                        )
                        trySend(AuthResult.Success(data = successResponse))
                    }.onFailure { exception ->
                        trySend(AuthResult.Failure(error = exception?.message ?: "Unknown error"))
                    }
                }

                else -> {
                    _authenticationState.value = AuthenticationState.LoggedOut
                    val errorMessage = gson.fromJson(
                        response.bodyAsText(),
                        FirebaseAuthResponse.Failure::class.java
                    ).error?.message ?: "Unknown error"

                    with(errorMessage) {
                        when {
                            contains(other = "email", ignoreCase = true) -> {
                                trySend(AuthResult.EmailFailure(error = errorMessage))
                            }

                            contains(other = "password", ignoreCase = true) -> {
                                trySend(AuthResult.PasswordFailure(error = errorMessage))
                            }

                            else -> {
                                trySend(AuthResult.Failure(error = errorMessage))
                            }
                        }
                    }
                }
            }
        }
        awaitClose { }
    }

    override suspend fun loginWithEmailAndPassword(
        email: String,
        password: String,
    ): Flow<AuthResult> = callbackFlow {
        _authenticationState.value = AuthenticationState.Loading
        httpClient.post(logInUrl + webApiKey) {
            contentType(ContentType.Application.Json)
            setBody(
                gson.toJson(
                    FirebaseAuthEmailAndPasswordRequest(
                        email = email,
                        password = password,
                        returnSecureToken = true,
                    )
                )
            )
        }.let { response ->
            when (response.status.value) {
                200 -> {
                    _authenticationState.value = AuthenticationState.LoggedIn
                    gson.fromJson(
                        response.bodyAsText(),
                        FirebaseAuthResponse.Success::class.java
                    ).let { successResponse ->
                        AuthService.currentUser = User(
                            uid = successResponse.localId,
                            email = successResponse.email,
                            token = successResponse.idToken,
                        )
                        trySend(AuthResult.Success(data = successResponse))
                    }.onFailure { exception ->
                        trySend(AuthResult.Failure(error = exception?.message ?: "Unknown error"))
                    }
                }

                else -> {
                    _authenticationState.value = AuthenticationState.LoggedOut
                    val errorMessage = gson.fromJson(
                        response.bodyAsText(),
                        FirebaseAuthResponse.Failure::class.java
                    ).error?.message ?: "Unknown error"

                    with(errorMessage) {
                        when {
                            contains(other = "email", ignoreCase = true) -> {
                                trySend(AuthResult.EmailFailure(error = errorMessage))
                            }

                            contains(other = "password", ignoreCase = true) -> {
                                trySend(AuthResult.PasswordFailure(error = errorMessage))
                            }

                            else -> {
                                trySend(AuthResult.Failure(error = errorMessage))
                            }
                        }
                    }
                }
            }
        }
        awaitClose { }
    }

    override suspend fun deleteAccount() {
        TODO("Not yet implemented")
    }

    override suspend fun logOut() {
        TODO("Not yet implemented")
    }

    override suspend fun resetPassword(email: String) {
        TODO("Not yet implemented")
    }
}