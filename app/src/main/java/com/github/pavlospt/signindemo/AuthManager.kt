package com.github.pavlospt.signindemo

import android.content.Intent
import android.content.IntentSender
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.CredentialRequest
import com.google.android.gms.auth.api.credentials.CredentialRequestResult
import com.google.android.gms.auth.api.credentials.HintRequest
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Result
import com.google.android.gms.common.api.Status
import java.lang.ref.WeakReference


class AuthManager private constructor(builder: AuthManagerBuilder) {

    companion object {

        @JvmStatic
        fun Builder(activity: AppCompatActivity): AuthManagerBuilder = AuthManager.AuthManagerBuilder(activity)

        @JvmStatic
        fun withBuilder(smartLockManagerBuilder: AuthManagerBuilder): AuthManager {
            return AuthManager(smartLockManagerBuilder)
        }
    }

    private var activity: AppCompatActivity

    private var googleView: WeakReference<GoogleView>
    private var googleApiClient: GoogleApiClient? = null


    private var smartLockView: WeakReference<SmartLockView>
    private var smartlockCredentialsRequest: CredentialRequest? = null

    private var hintView: WeakReference<HintView>
    private var hintRequest: HintRequest? = null


    /*
     * Google related variables
     * */
    private val RC_SIGN_IN = 1
    private val RC_CREDENTIALS_REQUEST = 2
    private val RC_HINT_REQUEST = 3
    private val RC_CREDENTIAL_SAVE = 4

    init {

        activity = builder.activity

        googleView = WeakReference(builder.googleView)
        googleApiClient = builder.googleApiClient

        smartLockView = WeakReference(builder.smartLockView)
        smartlockCredentialsRequest = builder.smartlockCredentialsRequest

        hintView = WeakReference(builder.hintView)
        hintRequest = builder.hintRequest
    }


    fun signInWithGoogle() {
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient)
        activity.startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    fun signOutOfGoogle() {
        Auth
            .GoogleSignInApi
            .signOut(googleApiClient)
            .setResultCallback {
                googleView.get()?.googleSignOut(it)
            }
    }

    fun revokeGoogleAccess() {
        Auth
            .GoogleSignInApi
            .revokeAccess(googleApiClient)
            .setResultCallback {
                googleView.get()?.googleAccessRevoked(it)
            }
    }

    fun saveCredential(credential: Credential) {
        Auth
            .CredentialsApi
            .save(googleApiClient, credential)
            .setResultCallback({
                handleCredentialSaveResult(it)
            })
    }

    /*
   * Handle Credential Save Result
   * */
    private fun handleCredentialSaveResult(result: Result) {
        val status: Status = result.status

        if (status.isSuccess) {
            smartLockView.get()?.credentialSaveSuccess()
        } else {
            handlePossibleCredentialSaveResolution(status)
        }
    }


    /*
    * Handle Possible Credential Save Resolution
    * */
    private fun handlePossibleCredentialSaveResolution(status: Status) {
        if (status.hasResolution()) {
            try {
                status.startResolutionForResult(activity, RC_CREDENTIAL_SAVE)
            } catch (e: IntentSender.SendIntentException) {
                smartLockView.get()?.credentialSaveResolutionFailure()
            }
        } else {
            smartLockView.get()?.credentialSaveFailure()
        }
    }


    /*
    * Handle Credential Save Resolution
    * */
    private fun handleCredentialSaveResolution(resultCode: Int, data: Intent?) {
        if (resultCode == AppCompatActivity.RESULT_CANCELED) {
            smartLockView.get()?.credentialSaveResolutionCancelled()
        } else {
            smartLockView.get()?.credentialSaveSuccess()
        }
    }

    /*
     * Handle Hint Request Resolution
     * */
    private fun handleEmailHintRequestResolution(resultCode: Int, data: Intent?) {
        if (resultCode == AppCompatActivity.RESULT_CANCELED) {
            hintView.get()?.emailHintRequestCancelled()
        } else {
            val credential: Credential? = data?.getParcelableExtra(Credential.EXTRA_KEY)
            credential?.let {
                hintView.get()?.signInSuccess(SignInSuccess(null, it))
            }
        }
    }

    /*
    * Request Email Hints
    * */
    fun requestEmailHints() {
        val intent = Auth.CredentialsApi.getHintPickerIntent(googleApiClient, hintRequest)
        try {
            activity.startIntentSenderForResult(intent.intentSender, RC_HINT_REQUEST, null, 0, 0, 0)
        } catch (e: IntentSender.SendIntentException) {
            hintView.get()?.emailHintRequestFailure()
        }
    }


    /*
    * Handle Credential Request Result
    * */
    private fun handleCredentialRequestResult(credentialRequestResult: CredentialRequestResult) {
        if (credentialRequestResult.status.isSuccess) {
            smartLockView.get()?.signInSuccess(SignInSuccess(null, credentialRequestResult.credential))
        } else {
            resolveCredentialRequest(credentialRequestResult.status)
        }
    }


    private fun resolveCredentialRequest(status: Status?) {
        if (status?.statusCode == CommonStatusCodes.RESOLUTION_REQUIRED) {
            initiateCredentialRequestResolution(status)
        } else {
            smartLockView.get()?.credentialRequestFailure()
        }
    }


    /*
    * Handle Credential Request Resolution
    * */
    private fun handleCredentialRequestResolution(resultCode: Int, data: Intent?) {
        if (resultCode == AppCompatActivity.RESULT_CANCELED) {
            smartLockView.get()?.credentialRequestCancelled()
        } else {
            val credential: Credential? = data?.getParcelableExtra(Credential.EXTRA_KEY)
            credential?.let {
                smartLockView.get()?.signInSuccess(SignInSuccess(null, it))
            }
        }
    }


    /*
    * Initiate Credential Request Resolution
    * */
    private fun initiateCredentialRequestResolution(status: Status?) {
        try {
            status?.startResolutionForResult(activity, RC_CREDENTIALS_REQUEST)
        } catch (sendIntentException: IntentSender.SendIntentException) {
            smartLockView.get()?.credentialRequestResolutionFailure()
        }
    }


    /*
    * Handle Sign In Resolution
    * */
    private fun handleGoogleSignInResolution(resultCode: Int, data: Intent?) {
        if (resultCode == AppCompatActivity.RESULT_CANCELED) {
            googleView.get()?.userCancelledGoogleSignIn()
        } else {

            val googleSignInResult: GoogleSignInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data)

            if (googleSignInResult.isSuccess) {
                val googleSignInAccount: GoogleSignInAccount? = googleSignInResult.signInAccount
                googleView.get()?.signInSuccess(SignInSuccess(googleSignInAccount, null))
            } else {
                googleView.get()?.googleSignInResultFailure()
            }
        }
    }


    /* [Request Credentials] */
    fun requestCredentials() {
        Auth.CredentialsApi
            .request(googleApiClient, smartlockCredentialsRequest)
            .setResultCallback {
                handleCredentialRequestResult(it)
            }
    }

    fun handle(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            RC_SIGN_IN -> handleGoogleSignInResolution(resultCode, data)
            RC_CREDENTIALS_REQUEST -> handleCredentialRequestResolution(resultCode, data)
            RC_HINT_REQUEST -> handleEmailHintRequestResolution(resultCode, data)
            RC_CREDENTIAL_SAVE -> handleCredentialSaveResolution(resultCode, data)
        }
    }

    fun destroy() {
        googleView.clear()
        smartLockView.clear()
        hintView.clear()
    }

    class AuthManagerBuilder internal constructor(val activity: AppCompatActivity) {

        lateinit var googleView: GoogleView
            private set

        lateinit var googleApiClient: GoogleApiClient
            private set

        lateinit var smartLockView: SmartLockView
            private set

        lateinit var smartlockCredentialsRequest: CredentialRequest
            private set

        lateinit var hintView: HintView
            private set

        lateinit var hintRequest: HintRequest
            private set


        fun withGoogle(googleView: GoogleView, googleApiClient: GoogleApiClient) = apply {
            this.googleView = googleView
            this.googleApiClient = googleApiClient
        }

        fun withSmartlock(smartLockView: SmartLockView, smartlockCredentialsRequest: CredentialRequest) = apply {
            this.smartLockView = smartLockView
            this.smartlockCredentialsRequest = smartlockCredentialsRequest
        }

        fun withHints(hintView: HintView, hintRequest: HintRequest) = apply {
            this.hintView = hintView
            this.hintRequest = hintRequest
        }

        fun build(): AuthManager = AuthManager.withBuilder(this)

    }

}

