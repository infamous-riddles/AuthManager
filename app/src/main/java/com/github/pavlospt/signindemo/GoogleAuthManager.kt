package com.github.pavlospt.signindemo

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.github.pavlospt.signindemo.models.SignInSuccess
import com.github.pavlospt.signindemo.views.GoogleView
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.GoogleApiClient
import java.lang.ref.WeakReference

class GoogleAuthManager private constructor(builder: GoogleAuthManagerBuilder) {

    companion object {

        @JvmStatic
        fun Builder(activity: AppCompatActivity): GoogleAuthManagerBuilder
                = GoogleAuthManagerBuilder(activity)

        @JvmStatic
        fun withBuilder(googleAuthManagerBuilder: GoogleAuthManagerBuilder): GoogleAuthManager {
            return GoogleAuthManager(googleAuthManagerBuilder)
        }
    }

    private var activity: AppCompatActivity

    private var googleView: WeakReference<GoogleView>
    private var googleApiClient: GoogleApiClient? = null


    private val RC_SIGN_IN = 1

    init {
        activity = builder.activity

        googleView = WeakReference(builder.googleView)
        googleApiClient = builder.googleApiClient
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

    /*
   * Handle Sign In Resolution
   * */
    private fun handleGoogleSignInResolution(resultCode: Int, data: Intent?) {

        val googleSignInResult: GoogleSignInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data)

        Log.d("TAG", GoogleSignInStatusCodes.getStatusCodeString(googleSignInResult.status.statusCode))

        if (resultCode == AppCompatActivity.RESULT_CANCELED) {
            googleView.get()?.userCancelledGoogleSignIn()
        } else {


            if (googleSignInResult.isSuccess) {
                val googleSignInAccount: GoogleSignInAccount? = googleSignInResult.signInAccount
                googleView.get()?.signInSuccess(SignInSuccess(googleSignInAccount, null))
            } else {
                googleView.get()?.googleSignInResultFailure()
            }
        }
    }

    fun handle(resultCode: Int, data: Intent?) {
        handleGoogleSignInResolution(resultCode, data)
    }

    fun clear() {
        googleView.clear()
    }

    class GoogleAuthManagerBuilder internal constructor(val activity: AppCompatActivity) {

        lateinit var googleView: GoogleView
            private set

        lateinit var googleApiClient: GoogleApiClient
            private set

        fun withGoogleApiClient(googleApiClient: GoogleApiClient) = apply {
            this.googleApiClient = googleApiClient
        }

        fun withGoogleView(googleView: GoogleView) = apply {
            this.googleView = googleView
        }

        fun build(): GoogleAuthManager = Companion.withBuilder(this)

    }

}
