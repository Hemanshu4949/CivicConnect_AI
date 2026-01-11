package com.example.civicconnectai.authentication

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.example.civicconnectai.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.AuthCredential

suspend fun getGoogleLoginCredential(context: Context): AuthCredential? {
    try {
        // 1. Setup Credential Manager
        val credentialManager = CredentialManager.create(context)

        // 2. Setup Google Option
        // IMPORTANT: Replace with your actual WEB CLIENT ID from Firebase Console
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(context.getString(R.string.default_web_client_id))
            .setAutoSelectEnabled(false)
            .build()

        // 3. Create Request
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        // 4. Show Selector & Get Result
        val result = credentialManager.getCredential(context, request)

        // 5. Parse Result
        val credential = result.credential
        Log.e("GoogleSIgnIn", credential.toString())
        Log.e("GoogleSignIn", GoogleIdTokenCredential.toString())

        // Option 1: It comes as the correct class (Best Case)
        if (credential is GoogleIdTokenCredential) {
            val idToken = credential.idToken
            return GoogleAuthProvider.getCredential(idToken, null)
        }
        // Option 2: It comes as a "CustomCredential" wrapper (Your Current Case)
        else if (credential is CustomCredential) {
            if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                try {
                    // Force extract the Google data from the generic box
                    val googleIdTokenCredential =
                        GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken
                    return GoogleAuthProvider.getCredential(idToken, null)
                } catch (e: GoogleIdTokenParsingException) {
                    Log.e("GoogleSignIn", "Failed to parse Google ID from CustomCredential", e)
                    return null
                }
            }
        else {
            Log.e("GoogleSignIn", "Unexpected credential type")
            return null
        }
    }
    else {
        Log.e("GoogleSignIn", "Unexpected credential class: ${credential.javaClass.name}")
        return null
    }
}
    catch (e: Exception) {
        Log.e("GoogleSignIn", "Error: ${e.message}")
        return null
    }
}