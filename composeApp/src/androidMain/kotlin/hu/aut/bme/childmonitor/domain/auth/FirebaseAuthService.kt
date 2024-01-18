package hu.aut.bme.childmonitor.domain.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.UserProfileChangeRequest
import hu.aut.bme.childmonitor.domain.auth.AuthService.Companion.currentUser
import hu.aut.bme.childmonitor.data.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

actual class FirebaseAuthService(
    private val firebaseAuth: FirebaseAuth,
) : AuthService {
    init {
        firebaseAuth.currentUser?.let { user ->
            CoroutineScope(Dispatchers.IO).launch {
                currentUser = User(
                    uid = user.uid,
                    email = user.email ?: "",
                    token = firebaseAuth.currentUser?.getIdToken(false)
                        ?.await()?.token ?: "",
                )
            }
        }
    }

    private val _authenticationState =
        MutableStateFlow<AuthenticationState>(AuthenticationState.LoggedOut)
    override val authenticationState: Flow<AuthenticationState> = _authenticationState

    init {
        _authenticationState.value = if (firebaseAuth.currentUser == null)
            AuthenticationState.LoggedOut
        else
            AuthenticationState.LoggedIn
    }

    override suspend fun signUpWithEmailAndPassword(
        email: String,
        password: String,
    ): Flow<AuthResult> = callbackFlow {
        _authenticationState.value = AuthenticationState.Loading
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                it.user?.let { user ->
                    launch {
                        currentUser = User(
                            uid = user.uid,
                            email = user.email ?: "",
                            token = user.getIdToken(false).await()?.token ?: "",
                        )
                    }
                    val profileChangeRequest = UserProfileChangeRequest.Builder()
                        .setDisplayName(user.email?.substringBefore('@'))
                        .build()
                    user.updateProfile(profileChangeRequest)
                }

                _authenticationState.value = AuthenticationState.LoggedIn
                trySend(AuthResult.Success())
            }.addOnFailureListener { exception ->
                _authenticationState.value = AuthenticationState.LoggedOut
                when (exception) {
                    is FirebaseAuthWeakPasswordException -> {
                        trySend(AuthResult.PasswordFailure(error = exception.message.toString()))
                    }

                    is FirebaseAuthInvalidCredentialsException,
                    is FirebaseAuthUserCollisionException,
                    -> {
                        trySend(AuthResult.EmailFailure(error = exception.message.toString()))
                    }

                    else -> {
                        trySend(AuthResult.Failure(error = exception.message.toString()))
                    }
                }
            }.addOnCanceledListener {
                _authenticationState.value = AuthenticationState.LoggedOut
                trySend(AuthResult.Cancelled())
            }
        awaitClose { }
    }

    override suspend fun loginWithEmailAndPassword(
        email: String,
        password: String,
    ): Flow<AuthResult> = callbackFlow {
        _authenticationState.value = AuthenticationState.Loading
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                it.user?.let { user ->
                    launch {
                        currentUser = User(
                            uid = user.uid,
                            email = user.email ?: "",
                            token = user.getIdToken(false).await()?.token ?: "",
                        )
                    }
                }

                _authenticationState.value = AuthenticationState.LoggedIn
                trySend(AuthResult.Success())
            }.addOnFailureListener { exception ->
                _authenticationState.value = AuthenticationState.LoggedOut
                when (exception) {
                    is FirebaseAuthWeakPasswordException -> {
                        trySend(AuthResult.PasswordFailure(error = exception.message.toString()))
                    }

                    is FirebaseAuthInvalidCredentialsException,
                    is FirebaseAuthUserCollisionException,
                    -> {
                        trySend(AuthResult.EmailFailure(error = exception.message.toString()))
                    }

                    else -> {
                        trySend(AuthResult.Failure(error = exception.message.toString()))
                    }
                }
            }.addOnCanceledListener {
                _authenticationState.value = AuthenticationState.LoggedOut
                trySend(AuthResult.Cancelled())
            }
        awaitClose { }
    }

    override suspend fun deleteAccount() {
        firebaseAuth.currentUser?.delete()
    }

    override suspend fun logOut() {
        firebaseAuth.signOut()
    }

    override suspend fun resetPassword(email: String) {
        firebaseAuth.sendPasswordResetEmail(email)
    }
}