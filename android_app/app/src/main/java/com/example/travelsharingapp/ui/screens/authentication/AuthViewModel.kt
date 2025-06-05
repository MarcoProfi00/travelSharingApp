package com.example.travelsharingapp.ui.screens.authentication

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialInterruptedException
import androidx.credentials.exceptions.GetCredentialUnsupportedException
import androidx.credentials.exceptions.NoCredentialException
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.travelsharingapp.data.repository.AuthPreferenceKeys
import com.example.travelsharingapp.data.repository.UserRepository
import com.example.travelsharingapp.data.repository.dataStoreInstance
import com.example.travelsharingapp.ui.screens.user_profile.UserProfileViewModel
import com.example.travelsharingapp.utils.FcmTokenManager
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.IOException

sealed class AuthState {
    object Initializing : AuthState()
    object Unauthenticated : AuthState()
    object LoggedOut : AuthState()
    object Loading : AuthState()
    data class Authenticated(val user: FirebaseUser) : AuthState()
    data class ProfileSetupRequired(val user: FirebaseUser) : AuthState()
    data class EmailVerificationRequired(val user: FirebaseUser) : AuthState()
    data class AccountCollisionDetected(val email: String, val attemptedPasswordForLinking: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

sealed class PasswordChangeState {
    object Idle : PasswordChangeState()
    object Loading : PasswordChangeState()
    data class Success(val message: String) : PasswordChangeState()
    data class Error(val message: String) : PasswordChangeState()
}

sealed class AccountDeleteState {
    object Idle : AccountDeleteState()
    object Loading : AccountDeleteState()
    data class Success(val message: String) : AccountDeleteState()
    data class Error(val message: String) : AccountDeleteState()
}

sealed class PasswordResetEmailState {
    object Idle : PasswordResetEmailState()
    object Loading : PasswordResetEmailState()
    object Success : PasswordResetEmailState()
    data class Error(val message: String) : PasswordResetEmailState()
}

class AuthViewModel(
    private val userRepository: UserRepository,
    private val applicationContext: Context
) : ViewModel() {

    // Web Application CLient ID from GCP
    private val serverClientId = "52459642476-94u3f73onb8vrvdbq6g24dn3dckti0c3.apps.googleusercontent.com"

    // Firebase authentication
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initializing)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _passwordChangeState = MutableStateFlow<PasswordChangeState>(PasswordChangeState.Idle)
    val passwordChangeState: StateFlow<PasswordChangeState> = _passwordChangeState.asStateFlow()

    private val _accountDeleteState = MutableStateFlow<AccountDeleteState>(AccountDeleteState.Idle)
    val accountDeleteState: StateFlow<AccountDeleteState> = _accountDeleteState.asStateFlow()

    private val _passwordResetEmailState = MutableStateFlow<PasswordResetEmailState>(PasswordResetEmailState.Idle)
    val passwordResetEmailState: StateFlow<PasswordResetEmailState> = _passwordResetEmailState.asStateFlow()

    private val _linkedProviders = MutableStateFlow<List<com.google.firebase.auth.UserInfo>>(emptyList())
    val linkedProviders: StateFlow<List<com.google.firebase.auth.UserInfo>> = _linkedProviders.asStateFlow()

    private val _accountLinkOperationStatus = MutableStateFlow<String?>(null)
    val accountLinkOperationStatus: StateFlow<String?> = _accountLinkOperationStatus.asStateFlow()

    private var pendingEmailForLinking: String? = null
    private var pendingPasswordForLinking: String? = null

    init {
        checkInitialAuthStateOptimistically()
    }

    private fun processSuccessfulAuthentication(user: FirebaseUser) {
        _authState.value = AuthState.Loading
        refreshLinkedProviders()

        FcmTokenManager.registerTokenForCurrentUser()

        viewModelScope.launch {
            try {
                val profileExists = userRepository.doesUserProfileExist(user.uid)
                setLocalProfileExistsFlag(user.uid, profileExists)
                if (profileExists) {
                    _authState.value = AuthState.Authenticated(user)
                } else {
                    _authState.value = AuthState.ProfileSetupRequired(user)
                }
            } catch (_: Exception) {
                _authState.value = AuthState.Error("Failed to verify user profile")
                if (user.uid.isNotBlank()) {
                    clearLocalProfileExistsFlag(user.uid)
                }
            }
        }
    }

    fun refreshLinkedProviders() {
        firebaseAuth.currentUser?.let {
            _linkedProviders.value = it.providerData
        } ?: run {
            _linkedProviders.value = emptyList()
        }
    }

    fun clearAccountLinkOperationStatus() {
        _accountLinkOperationStatus.value = null
    }

    fun linkGoogleAccount(activity: Activity) {
        _authState.value = AuthState.Loading

        val signInWithGoogleOption: GetSignInWithGoogleOption = GetSignInWithGoogleOption.Builder(serverClientId)
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()

        viewModelScope.launch {
            try {
                val credentialManager = CredentialManager.create(activity.applicationContext)
                val result: GetCredentialResponse = credentialManager.getCredential(activity, request)
                handleGoogleLinkSuccess(result)
            } catch (e: GetCredentialException) {
                _authState.value = AuthState.Error("Google Linking failed")
                _accountLinkOperationStatus.value = "Failed to initiate Google linking: ${e.message}"
            }
        }
    }

    private fun handleGoogleLinkSuccess(result: GetCredentialResponse) {
        val credential = result.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            try {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val googleIdToken = googleIdTokenCredential.idToken

                val firebaseGoogleCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                firebaseAuth.currentUser?.linkWithCredential(firebaseGoogleCredential)
                    ?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            _accountLinkOperationStatus.value = "Google account linked successfully!"
                            refreshLinkedProviders()
                            checkIfUserIsAuthenticated()
                        } else {
                            val exception = task.exception
                            when (exception) {
                                is FirebaseAuthUserCollisionException -> {
                                    _accountLinkOperationStatus.value = "This Google account is already linked to another user."
                                    _authState.value = AuthState.Error("This Google account is already linked to another user.")
                                }
                                is FirebaseAuthRecentLoginRequiredException -> {
                                    _accountLinkOperationStatus.value = "Please sign in again to link your Google account."
                                    _authState.value = AuthState.Error("Please sign in again to link your Google account.")
                                }
                                else -> {
                                    _accountLinkOperationStatus.value = "Failed to link Google account: ${exception?.message}"
                                    _authState.value = AuthState.Error("Failed to link Google account")
                                }
                            }
                        }
                    }
            } catch (e: GoogleIdTokenParsingException) {
                _accountLinkOperationStatus.value = "Google ID Token parsing error: ${e.message}"
                _authState.value = AuthState.Error("Failed to link Google account")
            }
        } else {
            _accountLinkOperationStatus.value = "Unexpected credential type for linking."
            _authState.value = AuthState.Error("Failed to link Google account")
        }
    }

    fun unlinkGoogleAccount() {
        val currentUser = firebaseAuth.currentUser ?: return
        val googleProviderId = GoogleAuthProvider.PROVIDER_ID

        if (currentUser.providerData.size <= 1 && currentUser.providerData.any { it.providerId == googleProviderId }) {
            val hasPasswordProvider = currentUser.providerData.any { it.providerId == EmailAuthProvider.PROVIDER_ID }
            if (!hasPasswordProvider && currentUser.email != null) {
                _accountLinkOperationStatus.value = "Cannot disconnect Google. Please set a password for your account first."
                return
            } else if (!hasPasswordProvider) {
                _accountLinkOperationStatus.value = "Cannot disconnect the only sign-in method."
                return
            }
        }

        currentUser.unlink(googleProviderId)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _accountLinkOperationStatus.value = "Google account disconnected."
                    refreshLinkedProviders()
                    checkIfUserIsAuthenticated()
                } else {
                    val exception = task.exception
                    if (exception is FirebaseAuthRecentLoginRequiredException) {
                        _accountLinkOperationStatus.value = "Please sign in again to disconnect your Google account."
                        _authState.value = AuthState.Error("Please sign in again to disconnect your Google account.")
                    } else {
                        _accountLinkOperationStatus.value = "Failed to disconnect Google account: ${exception?.message}"
                        _authState.value = AuthState.Error("Failed to disconnect Google account")
                    }
                }
            }
    }

    fun isGoogleTheOnlyProvider(): Boolean {
        val currentUser = firebaseAuth.currentUser ?: return false
        return currentUser.providerData.size == 1 && currentUser.providerData.any { it.providerId == GoogleAuthProvider.PROVIDER_ID }
    }

    fun checkIfUserIsAuthenticated() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            processSuccessfulAuthentication(currentUser)
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    private fun checkInitialAuthStateOptimistically() {
        viewModelScope.launch {
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                val localProfileExists = getLocalProfileExistsFlag(currentUser.uid).firstOrNull()

                if (localProfileExists == true) {
                    _authState.value = AuthState.Authenticated(currentUser)
                    val serverProfileActuallyExists = userRepository.doesUserProfileExist(currentUser.uid)
                    if (!serverProfileActuallyExists) {
                        setLocalProfileExistsFlag(currentUser.uid, false)
                        _authState.value = AuthState.ProfileSetupRequired(currentUser)
                    } else {
                        refreshLinkedProviders()
                    }
                    return@launch
                }
            }

            performFullAuthenticationCheck()
        }
    }

    private fun performFullAuthenticationCheck() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            _authState.value = AuthState.Loading
            refreshLinkedProviders()
            viewModelScope.launch {
                try {
                    val profileExists = userRepository.doesUserProfileExist(currentUser.uid)
                    setLocalProfileExistsFlag(currentUser.uid, profileExists)
                    if (profileExists) {
                        _authState.value = AuthState.Authenticated(currentUser)
                    } else {
                        _authState.value = AuthState.ProfileSetupRequired(currentUser)
                    }
                } catch (_: Exception) {
                    if(currentUser.uid.isNotBlank()) {
                        clearLocalProfileExistsFlag(currentUser.uid)
                    }
                    _authState.value = AuthState.Error("Failed to verify user profile")
                }
            }
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun handleProfileSetupCompleted(user: FirebaseUser) {
        viewModelScope.launch {
            setLocalProfileExistsFlag(user.uid, true)
        }
        _authState.value = AuthState.Authenticated(user)
    }

    fun refreshAuthState() {
        performFullAuthenticationCheck()
    }

    fun resetLoginState() {
        _authState.value = AuthState.Unauthenticated
    }

    fun loginWithEmailAndPassword(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email or password cannot be empty")
            return
        }

        _authState.value = AuthState.Loading
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful && firebaseAuth.currentUser != null) {
                    processSuccessfulAuthentication(firebaseAuth.currentUser!!)
                } else {
                    _authState.value = AuthState.Error("Invalid email or password.")
                }
            }
    }

    fun signupWithEmailAndPassword(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email or password cannot be empty")
            return
        }

        _authState.value = AuthState.Loading
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    firebaseAuth.currentUser?.let { user ->
                        user.sendEmailVerification()
                            .addOnCompleteListener { verificationTask ->
                                if (verificationTask.isSuccessful) {
                                    _authState.value = AuthState.EmailVerificationRequired(user)
                                } else {
                                    _authState.value = AuthState.Error("Failed to send verification email.")
                                }
                            }
                    } ?: run {
                        _authState.value = AuthState.Error("Firebase sign-up succeeded but user is null.")
                    }
                } else {
                    if (task.exception is FirebaseAuthUserCollisionException) {
                        _authState.value = AuthState.Loading
                        val currentUser = firebaseAuth.currentUser
                        if (currentUser != null && currentUser.providerData.any { it.providerId == GoogleAuthProvider.PROVIDER_ID } && currentUser.email == email) {
                            val credential = EmailAuthProvider.getCredential(email, password)
                            currentUser.linkWithCredential(credential)
                                .addOnCompleteListener { linkTask ->
                                    if (linkTask.isSuccessful) {
                                        processSuccessfulAuthentication(currentUser)
                                    } else {
                                        _authState.value = AuthState.Error("Failed to link email/password")
                                    }
                                }
                        } else {
                            _authState.value = AuthState.AccountCollisionDetected(email, password)
                        }

                    } else if (task.exception is FirebaseAuthWeakPasswordException) {
                        _authState.value = AuthState.Error("Password is too weak.")
                    } else {
                        _authState.value = AuthState.Error("Sign-up failed. Please try again later.")
                    }
                }
            }
    }

    fun initiateGoogleSignInForLinking(activity: Activity, email: String, passwordToLink: String) {
        _authState.value = AuthState.Loading

        pendingEmailForLinking = email
        pendingPasswordForLinking = passwordToLink

        val signInWithGoogleOption: GetSignInWithGoogleOption = GetSignInWithGoogleOption.Builder(serverClientId)
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()

        viewModelScope.launch {
            try {
                val credentialManager = CredentialManager.create(activity.applicationContext)
                val result: GetCredentialResponse = credentialManager.getCredential(activity, request)
                handleSignInSuccess(result)
            } catch (e: GetCredentialException) {
                handleSignInFailure(e)
            }
        }
    }

    fun initiateSignIn(activity: Activity) {
        _authState.value = AuthState.Loading

        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)    // first pass true
            .setFilterByAuthorizedAccounts(false)   // then false
            .setServerClientId(serverClientId)
            .setAutoSelectEnabled(true)
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        viewModelScope.launch {
            try {
                val credentialManager = CredentialManager.create(activity.applicationContext)
                val result: GetCredentialResponse = credentialManager.getCredential(activity, request)
                handleSignInSuccess(result)
            } catch (e: GetCredentialException) {
                handleSignInFailure(e)
            }
        }
    }

    fun initiateSignInWithGoogleButtonFlow(activity: Activity) {
        _authState.value = AuthState.Loading
        val signInWithGoogleOption: GetSignInWithGoogleOption = GetSignInWithGoogleOption.Builder(serverClientId)
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()

        viewModelScope.launch {
            try {
                val credentialManager = CredentialManager.create(activity.applicationContext)
                val result: GetCredentialResponse = credentialManager.getCredential(activity, request)
                handleSignInSuccess(result)
            } catch (e: GetCredentialException) {
                handleSignInFailure(e)
            }
        }
    }

    private fun handleSignInSuccess(result: GetCredentialResponse) {
        val credential = result.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            try {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
            } catch (_: GoogleIdTokenParsingException) {
                _authState.value = AuthState.Error("Failed to sign in")
            }
        } else {
            _authState.value = AuthState.Error("Failed to sign in")
        }
    }

    private fun handleSignInFailure(e: GetCredentialException) {
        val wasLinkingAttempt = pendingEmailForLinking != null

        pendingEmailForLinking = null
        pendingPasswordForLinking = null

        if (e is NoCredentialException || e is GetCredentialInterruptedException ||
            e is GetCredentialCancellationException || e is GetCredentialUnsupportedException) {
            _authState.value = AuthState.Unauthenticated
            return
        }

        val baseMessage = if (wasLinkingAttempt) {
            "Google Sign-In for linking failed"
        } else {
            "Google Sign-in failed"
        }
        _authState.value = AuthState.Error("$baseMessage. Please try again.")
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val googleAuthCredential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(googleAuthCredential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful && firebaseAuth.currentUser != null) {
                    val currentUser = firebaseAuth.currentUser!!
                    val emailToLink = pendingEmailForLinking
                    val passwordToLink = pendingPasswordForLinking

                    pendingEmailForLinking = null
                    pendingPasswordForLinking = null

                    if (emailToLink != null && passwordToLink != null && currentUser.email == emailToLink) {
                        val emailPasswordCredential = EmailAuthProvider.getCredential(emailToLink, passwordToLink)

                        currentUser.linkWithCredential(emailPasswordCredential)
                            .addOnCompleteListener { linkTask ->
                                processSuccessfulAuthentication(currentUser)
                                if (!linkTask.isSuccessful) {
                                    _accountLinkOperationStatus.value = "Signed in with Google, but failed to link email/password: ${linkTask.exception?.message}"
                                }
                            }
                    } else {
                        if (emailToLink != null) {
                            _authState.value = AuthState.Error("Signed in with ${currentUser.email}. The password for $emailToLink was not linked because a different Google account was selected.")
                            resetLoginState()
                        } else {
                            processSuccessfulAuthentication(currentUser)
                        }
                    }
                } else {
                    _authState.value = AuthState.Error("Sign-in failed")
                    pendingEmailForLinking = null
                    pendingPasswordForLinking = null
                }
            }
    }

    fun signOut(userProfileViewModel: UserProfileViewModel,context: Context) {
        val userIdToSignOut = firebaseAuth.currentUser?.uid

        if (userIdToSignOut != null) {
            FcmTokenManager.unregisterTokenForCurrentUser { tokenUnregistered ->
                performFirebaseSignOut(context, userIdToSignOut, tokenUnregistered)
            }
        } else {
            performFirebaseSignOut(context, null, true)
        }

        userProfileViewModel.clearUserSessionData()
    }

    private fun performFirebaseSignOut(context: Context, signedOutUserId: String?, tokenWasHandled: Boolean) {
        viewModelScope.launch {
            try {
                firebaseAuth.signOut()

                val credentialManager = CredentialManager.create(context)
                credentialManager.clearCredentialState(ClearCredentialStateRequest())

                pendingEmailForLinking = null
                pendingPasswordForLinking = null

                if (signedOutUserId != null && signedOutUserId.isNotBlank()) {
                    clearLocalProfileExistsFlag(signedOutUserId)
                }

                _authState.value = AuthState.LoggedOut
                Log.d("AuthViewModel", "User signed out. Token handling success: $tokenWasHandled")

            } catch (e: Exception) {
                Log.e("AuthViewModel", "Sign out failed", e)
                pendingEmailForLinking = null
                pendingPasswordForLinking = null
                _authState.value = AuthState.Error("Sign out failed: ${e.message}")
            }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        _passwordChangeState.value = PasswordChangeState.Loading
        val user = firebaseAuth.currentUser

        if (user == null) {
            _passwordChangeState.value = PasswordChangeState.Error("No user logged in.")
            return
        }

        if (user.email == null) {
            _passwordChangeState.value = PasswordChangeState.Error("User email not found. Cannot re-authenticate for password change.")
            return
        }

        val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
        user.reauthenticate(credential)
            .addOnCompleteListener { reauthTask ->
                if (reauthTask.isSuccessful) {
                    user.updatePassword(newPassword)
                        .addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                _passwordChangeState.value = PasswordChangeState.Success("Password updated successfully.")
                            } else {
                                _passwordChangeState.value = PasswordChangeState.Error("Failed to update password")
                            }
                        }
                } else {
                    when (reauthTask.exception) {
                        is FirebaseAuthInvalidCredentialsException -> {
                            _passwordChangeState.value = PasswordChangeState.Error("Incorrect current password.")
                        }
                        is FirebaseAuthRecentLoginRequiredException -> {
                            _passwordChangeState.value = PasswordChangeState.Error("Session expired. Please log out and log in again to change your password.")
                        }
                        else -> {
                            _passwordChangeState.value = PasswordChangeState.Error("Re-authentication failed")
                        }
                    }
                }
            }
    }

    fun resetPasswordChangeState() {
        _passwordChangeState.value = PasswordChangeState.Idle
    }

    fun deleteCurrentUserAccount(userProfileViewModel: UserProfileViewModel, currentPassword: String, context: Context) {
        _accountDeleteState.value = AccountDeleteState.Loading
        val user = firebaseAuth.currentUser

        if (user == null) {
            _accountDeleteState.value = AccountDeleteState.Error("No user logged in.")
            return
        }

        if (user.email == null || user.providerData.none { it.providerId == EmailAuthProvider.PROVIDER_ID }) {
            _accountDeleteState.value = AccountDeleteState.Error("Account deletion with password is for email/password accounts. For other providers, manage your account through them.")
            return
        }

        val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
        user.reauthenticate(credential)
            .addOnCompleteListener { reauthTask ->
                if (reauthTask.isSuccessful) {
                    user.delete()
                        .addOnCompleteListener { deleteTask ->
                            if (deleteTask.isSuccessful) {
                                signOut(userProfileViewModel, context)
                                _accountDeleteState.value = AccountDeleteState.Success("Account deleted successfully.")
                            } else {
                                if (deleteTask.exception is FirebaseAuthRecentLoginRequiredException) {
                                    _accountDeleteState.value = AccountDeleteState.Error("Security check failed. Please log out and log in again before deleting your account.")
                                } else {
                                    _accountDeleteState.value = AccountDeleteState.Error("Failed to delete account")
                                }
                            }
                        }
                } else {
                    when (reauthTask.exception) {
                        is FirebaseAuthInvalidCredentialsException -> {
                            _accountDeleteState.value = AccountDeleteState.Error("Incorrect password. Please try again.")
                        }
                        is FirebaseAuthRecentLoginRequiredException -> {
                            _accountDeleteState.value = AccountDeleteState.Error("Session expired. Please log out and log in again to delete your account.")
                        }
                        else -> {
                            _accountDeleteState.value = AccountDeleteState.Error("Re-authentication failed")
                        }
                    }
                }
            }
    }

    fun resetAccountDeleteState() {
        _accountDeleteState.value = AccountDeleteState.Idle
    }

    fun sendPasswordResetEmail(email: String) {
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _passwordResetEmailState.value = PasswordResetEmailState.Error("Please enter a valid email address.")
            return
        }
        _passwordResetEmailState.value = PasswordResetEmailState.Loading
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _passwordResetEmailState.value = PasswordResetEmailState.Success
                } else {
                    _passwordResetEmailState.value = PasswordResetEmailState.Error("Failed to send password reset email. Please try again.")
                }
            }
    }

    fun resetPasswordResetEmailState() {
        _passwordResetEmailState.value = PasswordResetEmailState.Idle
    }

    private fun getLocalProfileExistsFlag(userId: String): Flow<Boolean?> {
        if (userId.isBlank()) return kotlinx.coroutines.flow.flowOf(null)
        return applicationContext.dataStoreInstance.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[AuthPreferenceKeys.profileExistsKey(userId)]
            }
    }

    private suspend fun setLocalProfileExistsFlag(userId: String, exists: Boolean) {
        if (userId.isBlank()) return
        applicationContext.dataStoreInstance.edit { settings ->
            settings[AuthPreferenceKeys.profileExistsKey(userId)] = exists
        }
    }

    private suspend fun clearLocalProfileExistsFlag(userId: String) {
        if (userId.isBlank()) return
        applicationContext.dataStoreInstance.edit { settings ->
            settings.remove(AuthPreferenceKeys.profileExistsKey(userId))
        }
    }
}

class AuthViewModelFactory(
    private val userRepository: UserRepository,
    private val applicationContext: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel > create(modelClass: Class<T>): T {
        return modelClass
            .getConstructor(UserRepository::class.java, Context::class.java)
            .newInstance(userRepository, applicationContext)
    }
}
