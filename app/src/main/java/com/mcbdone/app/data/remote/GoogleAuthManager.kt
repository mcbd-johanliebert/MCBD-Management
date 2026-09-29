package com.mcbdone.app.data.remote

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.mcbdone.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GoogleAuthManager(private val context: Context) {

    private val credentialManager = CredentialManager.create(context)
    private val webClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID

    /**
     * Initiates Google Sign-In via Android Credential Manager.
     * Returns the Google ID Token or throws Exception.
     */
    suspend fun signInWithGoogle(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val response = credentialManager.getCredential(
                request = request,
                context = context
            )

            val credential = response.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                Log.d("GoogleAuthManager", "Google ID token acquired successfully!")
                return@withContext Result.success(idToken)
            } else {
                return@withContext Result.failure(Exception("Unsupported credential type received from Google."))
            }
        } catch (e: GetCredentialCancellationException) {
            Log.i("GoogleAuthManager", "User canceled Google sign-in flow.")
            return@withContext Result.failure(Exception("Sign in was cancelled."))
        } catch (e: GetCredentialException) {
            Log.e("GoogleAuthManager", "Google Credential error: ${e.message}", e)
            return@withContext Result.failure(Exception(e.message ?: "Google Sign-In failed."))
        } catch (e: Exception) {
            Log.e("GoogleAuthManager", "Unexpected Google Sign-In exception", e)
            return@withContext Result.failure(e)
        }
    }
}
