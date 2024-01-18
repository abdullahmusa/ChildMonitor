package hu.aut.bme.childmonitor.domain.auth

import kotlinx.serialization.Serializable

@Serializable
data class FirebaseAuthEmailAndPasswordRequest(
    val email: String,
    val password: String,
    val returnSecureToken: Boolean = true,
)