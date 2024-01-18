package hu.aut.bme.childmonitor.domain.auth

import hu.aut.bme.childmonitor.domain.auth.AuthService.Companion.currentUser
import hu.aut.bme.childmonitor.data.model.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow

class MockAuthService : AuthService {
    private val _authenticationState =
        MutableStateFlow<AuthenticationState>(AuthenticationState.LoggedOut)
    override val authenticationState: Flow<AuthenticationState> = _authenticationState

    init {
        currentUser = User(
            uid = "mockUserId",
            email = "mock@example.com",
            token = "mockToken",
        )
    }

    override suspend fun signUpWithEmailAndPassword(
        email: String,
        password: String,
    ): Flow<AuthResult> = flow {
        _authenticationState.value = AuthenticationState.Loading
        delay(800)
        _authenticationState.value = AuthenticationState.LoggedIn
        emit(AuthResult.Success())
    }

    override suspend fun loginWithEmailAndPassword(
        email: String,
        password: String,
    ): Flow<AuthResult> = flow { emit(AuthResult.Success()) }

    override suspend fun deleteAccount() {}
    override suspend fun logOut() {}
    override suspend fun resetPassword(email: String) {}
}