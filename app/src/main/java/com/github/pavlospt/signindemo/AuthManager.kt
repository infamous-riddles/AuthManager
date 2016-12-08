package com.github.pavlospt.signindemo

import android.content.Intent
import android.content.IntentSender
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.CredentialRequest
import com.google.android.gms.auth.api.credentials.CredentialRequestResult
import com.google.android.gms.auth.api.credentials.HintRequest
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Result
import com.google.android.gms.common.api.Status

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

    private var googleView: GoogleView? = null
    private var googleApiClient: GoogleApiClient? = null


    private var smartLockView: SmartLockView? = null
    private var smartlockCredentialsRequest: CredentialRequest? = null

    private var hintView: HintView? = null
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

        googleView = builder.googleView
        googleApiClient = builder.googleApiClient

        smartLockView = builder.smartLockView
        smartlockCredentialsRequest = builder.smartlockCredentialsRequest

        hintView = builder.hintView
        hintRequest = builder.hintRequest
    }


    fun signInWithGoogle() {
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient)
        activity.startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    fun saveCredential(credential: Credential) {
        Auth.CredentialsApi
                .save(googleApiClient, credential)
                .setResultCallback({
                    result ->
                    handleCredentialSaveResult(result)
                })
    }

    /*
   * Handle Credential Save Result
   * */
    private fun handleCredentialSaveResult(result: Result) {
        val status: Status = result.status

        if (status.isSuccess) {
            smartLockView?.credentialSaveSuccess()
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
                smartLockView?.credentialSaveResolutionFailure()
            }
        } else {
            smartLockView?.credentialSaveFailure()
        }
    }


    /*
    * Handle Credential Save Resolution
    * */
    private fun handleCredentialSaveResolution(resultCode: Int, data: Intent?) {
        if (resultCode == AppCompatActivity.RESULT_CANCELED) {
            smartLockView?.credentialSaveResolutionCancelled()
        } else {
            smartLockView?.credentialSaveSuccess()
        }
    }

    /*
* Handle Hint Request Resolution
* */
    private fun handleEmailHintRequestResolution(resultCode: Int, data: Intent?) {
        if (resultCode == AppCompatActivity.RESULT_CANCELED) {
            hintView?.emailHintRequestCancelled()
        } else {
            hintView?.emailHintRequestSuccess(data)
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
            hintView?.emailHintRequestFailure()
        }
    }


    /*
    * Handle Credential Request Result
    * */
    private fun handleCredentialRequestResult(credentialRequestResult: CredentialRequestResult) {
        if (credentialRequestResult.status.isSuccess) {
            smartLockView?.signInSuccess(credentialRequestResult.credential.id)
        } else {
            resolveCredentialRequest(credentialRequestResult.status)
        }
    }


    private fun resolveCredentialRequest(status: Status?) {
        if (status?.statusCode == CommonStatusCodes.RESOLUTION_REQUIRED) {
            initiateCredentialRequestResolution(status)
        } else {
            smartLockView?.credentialRequestFailure()
        }
    }


    /*
    * Handle Credential Request Resolution
    * */
    private fun handleCredentialRequestResolution(resultCode: Int, data: Intent?) {
        if (resultCode == AppCompatActivity.RESULT_CANCELED) {
            smartLockView?.credentialRequestCancelled()
        } else {
            smartLockView?.credentialRequestResolutionSuccess(data)
        }
    }


    /*
    * Initiate Credential Request Resolution
    * */
    private fun initiateCredentialRequestResolution(status: Status?) {
        try {
            status?.startResolutionForResult(activity, RC_CREDENTIALS_REQUEST)
        } catch (sendIntentException: IntentSender.SendIntentException) {
            smartLockView?.credentialRequestResolutionFailure()
        }
    }


    /*
    * Handle Sign In Resolution
    * */
    private fun handleGoogleSignInResolution(resultCode: Int, data: Intent?) {
        if (resultCode == AppCompatActivity.RESULT_CANCELED) {
            googleView?.userCancelledGoogleSignIn()
        } else {
            googleView?.userGoogleSignInSuccess(data)
        }
    }


    /* [Request Credentials] */
    fun requestCredentials() {
        Auth.CredentialsApi
                .request(googleApiClient, smartlockCredentialsRequest)
                .setResultCallback({ credentialRequestResult ->
                    handleCredentialRequestResult(credentialRequestResult)
                })
    }

    fun handle(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            RC_SIGN_IN -> handleGoogleSignInResolution(resultCode, data)
            RC_CREDENTIALS_REQUEST -> handleCredentialRequestResolution(resultCode, data)
            RC_HINT_REQUEST -> handleEmailHintRequestResolution(resultCode, data)
            RC_CREDENTIAL_SAVE -> handleCredentialSaveResolution(resultCode, data)
        }
    }


    class AuthManagerBuilder internal constructor(val activity: AppCompatActivity) {

        var googleView: GoogleView? = null
            private set

        var googleApiClient: GoogleApiClient? = null
            private set

        var smartLockView: SmartLockView? = null
            private set

        var smartlockCredentialsRequest: CredentialRequest? = null
            private set

        var hintView: HintView? = null
            private set

        var hintRequest: HintRequest? = null
            private set


        fun withGoogle(googleView: GoogleView, googleApiClient: GoogleApiClient) = apply {
            this.googleApiClient = googleApiClient
        }

        fun withSmartlock(smartLockView: SmartLockView, smartlockCredentialsRequest: CredentialRequest?) = apply {
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

