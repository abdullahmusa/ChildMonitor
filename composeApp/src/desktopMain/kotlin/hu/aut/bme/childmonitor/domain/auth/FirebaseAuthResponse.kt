package hu.aut.bme.childmonitor.domain.auth

import kotlinx.serialization.Serializable

@Serializable
sealed class FirebaseAuthResponse {
    data class Success(
        val email: String,
        val expiresIn: String,
        val idToken: String,
        val localId: String,
        val refreshToken: String,
    ) : FirebaseAuthResponse()

    data class Failure(
        val error: FirebaseError?,
    ) : FirebaseAuthResponse()
}

data class FirebaseError(
    val code: Int,
    val message: String?,
)