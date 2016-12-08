package com.github.pavlospt.signindemo

import android.content.Intent
import android.content.IntentSender
import android.support.v7.app.AppCompatActivity
import com.github.pavlospt.signindemo.models.SignInSuccess
import com.github.pavlospt.signindemo.views.SmartLockView
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.CredentialRequest
import com.google.android.gms.auth.api.credentials.CredentialRequestResult
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Result
import com.google.android.gms.common.api.Status
import java.lang.ref.WeakReference

class SmartLockManager private constructor(builder: SmartLockManagerBuilder) {

    companion object {

        @JvmStatic
        fun Builder(activity: AppCompatActivity): SmartLockManagerBuilder
                = SmartLockManagerBuilder(activity)

        @JvmStatic
        fun withBuilder(smartLockManagerBuilder: SmartLockManagerBuilder): SmartLockManager {
            return SmartLockManager(smartLockManagerBuilder)
        }
    }

    private var activity: AppCompatActivity

    private var googleApiClient: GoogleApiClient? = null

    private var smartLockView: WeakReference<SmartLockView>
    private var smartLockCredentialsRequest: CredentialRequest? = null

    private val RC_CREDENTIALS_REQUEST = 2
    private val RC_CREDENTIAL_SAVE = 4

    init {
        activity = builder.activity

        googleApiClient = builder.googleApiClient

        smartLockView = WeakReference(builder.smartLockView)
        smartLockCredentialsRequest = builder.smartLockCredentialRequest
    }

    fun saveCredential(credential: Credential) {
        Auth
                .CredentialsApi
                .save(googleApiClient, credential)
                .setResultCallback({
                    handleCredentialSaveResult(it)
                })
    }

    fun deleteCredential(credential: Credential?) {
        credential?.let {
            Auth
                    .CredentialsApi
                    .delete(googleApiClient, credential)
                    .setResultCallback {
                        smartLockView.get()?.credentialDelete(it)
                    }
        }
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
    fun handleCredentialSave(resultCode: Int) {
        if (resultCode == AppCompatActivity.RESULT_CANCELED) {
            smartLockView.get()?.credentialSaveResolutionCancelled()
        } else {
            smartLockView.get()?.credentialSaveSuccess()
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
        if (eligibleForResolution(status)) {
            initiateCredentialRequestResolution(status)
        } else {
            smartLockView.get()?.credentialRequestFailure()
        }
    }

    private fun eligibleForResolution(status: Status?) =
            status?.statusCode == CommonStatusCodes.RESOLUTION_REQUIRED
                    || status?.statusCode == CommonStatusCodes.SIGN_IN_REQUIRED

    /*
    * Handle Credential Request Resolution
    * */
    fun handleCredentialRequest(resultCode: Int, data: Intent?) {
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

    /* [Request Credentials] */
    fun requestCredentials() {
        Auth.CredentialsApi
                .request(googleApiClient, smartLockCredentialsRequest)
                .setResultCallback {
                    handleCredentialRequestResult(it)
                }
    }

    fun clear() {
        smartLockView.clear()
    }

    class SmartLockManagerBuilder internal constructor(val activity: AppCompatActivity) {

        lateinit var smartLockView: SmartLockView
            private set

        lateinit var smartLockCredentialRequest: CredentialRequest
            private set

        lateinit var googleApiClient: GoogleApiClient
            private set

        fun withGoogleApiClient(googleApiClient: GoogleApiClient) = apply {
            this.googleApiClient = googleApiClient
        }

        fun withSmartLockView(smartLockView: SmartLockView) = apply {
            this.smartLockView = smartLockView
        }

        fun withSmartLockCredentialRequest(smartLockCredentialRequest: CredentialRequest) = apply {
            this.smartLockCredentialRequest = smartLockCredentialRequest
        }

        fun build(): SmartLockManager = withBuilder(this)
    }

}

