package com.example.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

data class AuthUserState(
    val isAuthenticated: Boolean = false,
    val uid: String? = null,
    val displayName: String? = null,
    val email: String? = null,
    val photoUrl: String? = null,
    val isAnonymous: Boolean = false
)

object AuthManager {

    private const val TAG = "AuthManager"

    // Safe Firebase Auth reference
    private val firebaseAuth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseAuth not configured or google-services.json missing: ${e.message}")
            null
        }
    }

    private val _userState = MutableStateFlow(AuthUserState())
    val userState: StateFlow<AuthUserState> = _userState.asStateFlow()

    init {
        try {
            firebaseAuth?.addAuthStateListener { auth ->
                val user = auth.currentUser
                updateUserState(user)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not attach AuthStateListener", e)
        }
    }

    private fun updateUserState(user: FirebaseUser?) {
        if (user != null) {
            _userState.value = AuthUserState(
                isAuthenticated = true,
                uid = user.uid,
                displayName = user.displayName ?: user.email?.substringBefore("@") ?: "Client",
                email = user.email,
                photoUrl = user.photoUrl?.toString(),
                isAnonymous = user.isAnonymous
            )
        } else {
            _userState.value = AuthUserState(isAuthenticated = false)
        }
    }

    /**
     * Signs in with Google using Credential Manager and Firebase Auth.
     */
    suspend fun signInWithGoogle(
        context: Context,
        serverClientId: String = "dummy-client-id.apps.googleusercontent.com"
    ): Result<AuthUserState> = withContext(Dispatchers.IO) {
        try {
            val credentialManager = CredentialManager.create(context)

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = context
            )

            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                val auth = firebaseAuth
                if (auth != null) {
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    val authResult = auth.signInWithCredential(firebaseCredential).await()
                    updateUserState(authResult.user)
                    Result.success(_userState.value)
                } else {
                    // Fallback to local authenticated profile if Firebase config is pending
                    _userState.value = AuthUserState(
                        isAuthenticated = true,
                        uid = googleIdTokenCredential.id,
                        displayName = googleIdTokenCredential.displayName ?: "Client",
                        email = googleIdTokenCredential.id,
                        photoUrl = googleIdTokenCredential.profilePictureUri?.toString()
                    )
                    Result.success(_userState.value)
                }
            } else {
                Result.failure(Exception("Unsupported credential type"))
            }
        } catch (e: GetCredentialCancellationException) {
            Log.d(TAG, "User cancelled Google Sign-In")
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Google Sign-In failed", e)
            // If running in development without valid web client ID, provide client access simulation
            Result.failure(e)
        }
    }

    /**
     * Client sign-in with phone/name for wedding guests
     */
    fun signInAsClient(name: String, emailOrPhone: String) {
        _userState.value = AuthUserState(
            isAuthenticated = true,
            uid = "client_${System.currentTimeMillis()}",
            displayName = name.ifBlank { "ওয়েডিং কাস্টমার" },
            email = emailOrPhone,
            isAnonymous = false
        )
    }

    /**
     * Signs out of Firebase Auth and resets local user state.
     */
    fun signOut() {
        try {
            firebaseAuth?.signOut()
        } catch (e: Exception) {
            Log.w(TAG, "Sign out error", e)
        }
        _userState.value = AuthUserState(isAuthenticated = false)
    }
}
