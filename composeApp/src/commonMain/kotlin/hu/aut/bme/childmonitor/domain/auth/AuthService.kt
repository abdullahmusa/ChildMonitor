package hu.aut.bme.childmonitor.domain.auth

import hu.aut.bme.childmonitor.data.model.User
import kotlinx.coroutines.flow.Flow

sealed class AuthResult {
    data class Success(val data: Any? = null) : AuthResult()
    data class Failure(val error: String) : AuthResult()
    data class EmailFailure(val error: String) : AuthResult()
    data class PasswordFailure(val error: String) : AuthResult()
    data class Cancelled(val error: String? = null) : AuthResult()
}

sealed class AuthenticationState {
    data object Loading : AuthenticationState()
    data object LoggedIn : AuthenticationState()
    data object LoggedOut : AuthenticationState()
}

interface AuthService {
    companion object {
        var currentUser: User? = null
    }

    val authenticationState: Flow<AuthenticationState>

    suspend fun signUpWithEmailAndPassword(
        email: String,
        password: String,
    ): Flow<AuthResult>

    suspend fun loginWithEmailAndPassword(
        email: String,
        password: String,
    ): Flow<AuthResult>

    suspend fun deleteAccount()

    suspend fun logOut()
    suspend fun resetPassword(email: String)
}