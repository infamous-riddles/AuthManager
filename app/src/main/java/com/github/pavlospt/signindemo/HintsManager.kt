package com.github.pavlospt.signindemo

import android.content.Intent
import android.content.IntentSender
import android.support.v7.app.AppCompatActivity
import com.github.pavlospt.signindemo.views.HintView
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.HintRequest
import com.google.android.gms.common.api.GoogleApiClient
import java.lang.ref.WeakReference


class HintsManager private constructor(builder: HintsManagerBuilder) {

    companion object {

        @JvmStatic
        fun Builder(activity: AppCompatActivity): HintsManagerBuilder
                = HintsManagerBuilder(activity)

        @JvmStatic
        fun withBuilder(hintsManagerBuilder: HintsManagerBuilder): HintsManager {
            return HintsManager(hintsManagerBuilder)
        }
    }

    private var activity: AppCompatActivity

    private var googleApiClient: GoogleApiClient? = null

    private var hintView: WeakReference<HintView>
    private var hintRequest: HintRequest? = null

    private val RC_HINT_REQUEST = 3

    init {
        activity = builder.activity

        googleApiClient = builder.googleApiClient

        hintView = WeakReference(builder.hintView)
        hintRequest = builder.hintRequest
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
                hintView.get()?.emailHintSelected(it)
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

    fun handle(resultCode: Int, data: Intent?) {
        handleEmailHintRequestResolution(resultCode, data)
    }

    fun clear() {
        hintView.clear()
    }

    class HintsManagerBuilder internal constructor(val activity: AppCompatActivity) {

        lateinit var hintView: HintView
            private set

        lateinit var hintRequest: HintRequest
            private set

        lateinit var googleApiClient: GoogleApiClient
            private set

        fun withGoogleApiClient(googleApiClient: GoogleApiClient) = apply {
            this.googleApiClient = googleApiClient
        }

        fun withHintsView(hintView: HintView) = apply {
            this.hintView = hintView
        }

        fun withHintRequest(hintRequest: HintRequest) = apply {
            this.hintRequest = hintRequest
        }

        fun build(): HintsManager = Companion.withBuilder(this)
    }

}


