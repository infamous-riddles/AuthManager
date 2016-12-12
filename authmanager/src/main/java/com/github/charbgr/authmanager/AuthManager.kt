package com.github.charbgr.authmanager

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import com.github.charbgr.authmanager.views.GoogleView
import com.github.charbgr.authmanager.views.HintView
import com.github.charbgr.authmanager.views.SmartLockView
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.CredentialRequest
import com.google.android.gms.auth.api.credentials.HintRequest
import com.google.android.gms.common.api.GoogleApiClient

class AuthManager private constructor(builder: AuthManagerBuilder) {

    companion object {

        @JvmStatic
        fun Builder(activity: AppCompatActivity): AuthManagerBuilder = AuthManagerBuilder(activity)

        @JvmStatic
        fun withBuilder(authManagerBuilder: AuthManagerBuilder): AuthManager {
            return AuthManager(authManagerBuilder)
        }
    }

    private var activity: AppCompatActivity

    private var googleApiClient: GoogleApiClient? = null

    private var googleAuthManager: GoogleAuthManager? = null
    private var smartLockManager: SmartLockManager? = null
    private var hintsManager: HintsManager? = null

    init {

        activity = builder.activity

        googleApiClient = builder.googleApiClient

        googleAuthManager = builder.googleAuthManager
        smartLockManager = builder.smartLockManager
        hintsManager = builder.hintsManager
    }

    fun signInWithGoogle() {
        googleAuthManager?.signInWithGoogle()
    }

    fun signOutOfGoogle() {
        googleAuthManager?.signOutOfGoogle()
    }

    fun revokeGoogleAccess() {
        googleAuthManager?.revokeGoogleAccess()
    }

    fun requestCredential() {
        smartLockManager?.requestCredentials()
    }

    fun saveCredential(credential: Credential) {
        smartLockManager?.saveCredential(credential)
    }

    fun deleteCredential(credential: Credential?) {
        smartLockManager?.deleteCredential(credential)
    }

    /**
     * Request Email Hints
     * */
    fun requestEmailHints() {
        hintsManager?.requestEmailHints()
    }

    fun handle(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            AuthManagerCodes.RC_SIGN_IN -> googleAuthManager?.handle(resultCode, data)
            AuthManagerCodes.RC_HINT_REQUEST -> hintsManager?.handle(resultCode, data)
            AuthManagerCodes.RC_CREDENTIAL_SAVE -> smartLockManager?.handleCredentialSave(resultCode)
            AuthManagerCodes.RC_CREDENTIALS_REQUEST -> smartLockManager?.handleCredentialRequest(resultCode, data)
        }
    }

    fun destroy() {
        googleAuthManager?.clear()
        smartLockManager?.clear()
        hintsManager?.clear()
    }

    class AuthManagerBuilder internal constructor(val activity: AppCompatActivity) {

        lateinit var googleAuthManager: GoogleAuthManager
            private set

        lateinit var smartLockManager: SmartLockManager
            private set

        lateinit var hintsManager: HintsManager
            private set

        lateinit var googleApiClient: GoogleApiClient
            private set

        fun withGoogleApiClient(googleApiClient: GoogleApiClient) = apply {
            this.googleApiClient = googleApiClient
        }

        fun withGoogle(googleView: GoogleView) = apply {
            this.googleAuthManager = GoogleAuthManager
                .Builder(activity)
                .withGoogleApiClient(googleApiClient)
                .withGoogleView(googleView)
                .build()
        }

        fun withSmartLock(smartLockView: SmartLockView, smartLockCredentialRequest: CredentialRequest) = apply {
            this.smartLockManager = SmartLockManager
                .Builder(activity)
                .withGoogleApiClient(googleApiClient)
                .withSmartLockView(smartLockView)
                .withSmartLockCredentialRequest(smartLockCredentialRequest)
                .build()
        }

        fun withHints(hintView: HintView, hintRequest: HintRequest) = apply {
            this.hintsManager = HintsManager
                .Builder(activity)
                .withGoogleApiClient(googleApiClient)
                .withHintsView(hintView)
                .withHintRequest(hintRequest)
                .build()
        }

        fun build(): AuthManager = withBuilder(this)
    }

}

