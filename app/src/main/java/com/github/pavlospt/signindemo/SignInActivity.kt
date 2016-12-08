package com.github.pavlospt.signindemo

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.github.pavlospt.authmanager.models.SignInSuccess
import com.github.pavlospt.authmanager.views.GoogleView
import com.github.pavlospt.authmanager.views.HintView
import com.github.pavlospt.authmanager.views.SmartLockView
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.CredentialPickerConfig
import com.google.android.gms.auth.api.credentials.CredentialRequest
import com.google.android.gms.auth.api.credentials.HintRequest
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Status


class SignInActivity : AppCompatActivity(),
    GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks,
    GoogleView, SmartLockView, HintView {

    private var authManager: AuthManager? = null

    /*
    * Views
    * */
    private lateinit var signInButton: SignInButton
    private lateinit var signOutButton: Button
    private lateinit var revokeAccessButton: Button
    private lateinit var requestHintsButton: Button
    private lateinit var requestUserCredentials: Button
    private lateinit var saveCredentialsButton: Button
    private lateinit var emailAddressTextInput: TextInputLayout
    private lateinit var passwordTextInput: TextInputLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_sign_in)

        initViews()
        initAuthManager()
    }

    private fun initViews() {
        saveCredentialsButton = findViewById(R.id.save_credentials_button) as Button
        requestUserCredentials = findViewById(R.id.request_user_credentials_button) as Button
        requestHintsButton = findViewById(R.id.request_hints_button) as Button

        signInButton = findViewById(R.id.sign_in_button) as SignInButton
        signOutButton = findViewById(R.id.sign_out_google) as Button
        revokeAccessButton = findViewById(R.id.revoke_access_button) as Button

        emailAddressTextInput = findViewById(R.id.email_address_text_input) as TextInputLayout
        passwordTextInput = findViewById(R.id.password_text_input) as TextInputLayout

        emailAddressTextInput.clearFocus()
        passwordTextInput.clearFocus()

        signInButton.setSize(SignInButton.SIZE_WIDE)

        requestUserCredentials.setOnClickListener {
            authManager?.requestCredentials()
        }

        requestHintsButton.setOnClickListener {
            authManager?.requestEmailHints()
        }

        signInButton.setOnClickListener {
            authManager?.signInWithGoogle()
        }

        signOutButton.setOnClickListener {
            authManager?.signOutOfGoogle()
        }

        revokeAccessButton.setOnClickListener {
            authManager?.revokeGoogleAccess()
        }

        saveCredentialsButton.setOnClickListener {
            saveCredentials()
        }
    }

    //region Initializations

    private fun initAuthManager() {
        val googleApiClient = createGoogleApiClient()
        val hintRequest = createHintRequest()
        val smartlockReq = createSmartlockCredentialsRequest()

        authManager = AuthManager
            .Builder(this)
            .withGoogle(this, googleApiClient)
            .withHints(this, hintRequest)
            .withSmartlock(this, smartlockReq)
            .build()
    }

    /*
    * Initialize Hint request
    * */
    private fun createHintRequest() = HintRequest.Builder()
        .setHintPickerConfig(
            CredentialPickerConfig.Builder()
                .setShowCancelButton(true)
                .setPrompt(CredentialPickerConfig.Prompt.SIGN_IN)
                .build()
        )
        .setEmailAddressIdentifierSupported(true)
        .build()

    /*
    * Initialize Google Sign-in options
    * */
    private fun createGoogleSignInOptions() = GoogleSignInOptions
        .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .build()

    /*
    * Initialize Smartlock credentials request
    * */
    private fun createSmartlockCredentialsRequest() = CredentialRequest.Builder()
        .setPasswordLoginSupported(true)
        .build()

    /*
    * Initialize Google API Client
    * */
    private fun createGoogleApiClient() = GoogleApiClient.Builder(this)
        .addConnectionCallbacks(this)
        .enableAutoManage(this, this)
        .addApi(Auth.GOOGLE_SIGN_IN_API, createGoogleSignInOptions())
        .addApi(Auth.CREDENTIALS_API)
        .build()

    //endregion

    //region GoogleSignIn

    /*
    * Google Sign-In Failure
    * */
    override fun googleSignInResultFailure() {
        Toast.makeText(this, "Authentication failure", Toast.LENGTH_SHORT).show()
    }

    /*
    * User Cancelled Google Sign-In
    * */
    override fun userCancelledGoogleSignIn() {
        Toast.makeText(this, "User cancelled Google Sign-In flow", Toast.LENGTH_SHORT).show()
    }
    //endregion

    //region CredentialsRequest


    /*
    * Credential Request Cancelled
    * */
    override fun credentialRequestCancelled() {
        Toast.makeText(this, "Credential Request cancelled", Toast.LENGTH_SHORT).show()
    }

    /*
    * Credential Request Resolution Failed
    * */
    override fun credentialRequestResolutionFailure() {
        Toast.makeText(this, "Credential Request Resolution failure", Toast.LENGTH_SHORT).show()
    }

    /*
    * Credential Request Failed
    * */
    override fun credentialRequestFailure() {
        Toast.makeText(this, "Credential Request failure", Toast.LENGTH_SHORT).show()
    }

    //endregion

    /*
    * Hint Request Cancelled
    * */
    override fun emailHintRequestCancelled() {
        Toast.makeText(this, "Hint Request cancelled by user", Toast.LENGTH_SHORT).show()
    }

    /*
    * Hint Request Failed
    * */
    override fun emailHintRequestFailure() {
        Toast.makeText(this, "Hint Request failure", Toast.LENGTH_SHORT).show()
    }

    //endregion

    //region CredentialsSave

    /*
    * Initiate Credential save flow
    * */
    private fun saveCredentials() {

        val emailInvalid: Boolean = emailAddressTextInput.editText?.text.toString().trim().isNullOrEmpty() ?: false
        val passwordInvalid: Boolean = passwordTextInput.editText?.text.toString().trim().isNullOrEmpty() ?: false

        if (emailInvalid) {
            emailRequirementError()
            return
        }

        if (passwordInvalid) {
            passwordRequirementError()
            return
        }

        val credentialToSave: Credential =
            Credential
                .Builder(emailAddressTextInput.editText?.text.toString())
                .setPassword(passwordTextInput.editText?.text.toString().trim())
                .build()

        authManager?.saveCredential(credentialToSave)
    }

    /*
    * User Cancelled Credential Save Resolution
    * */
    override fun credentialSaveResolutionCancelled() {
        Toast.makeText(this, "User cancelled credential save resolution", Toast.LENGTH_SHORT).show()
    }

    /*
    * Credential Save Resolution Failed
    * */
    override fun credentialSaveResolutionFailure() {
        Toast.makeText(this, "Credential save resolution failed", Toast.LENGTH_SHORT).show()
    }

    /*
    * Credential Save Failed
    * */
    override fun credentialSaveFailure() {
        Toast.makeText(this, "Credential save failed", Toast.LENGTH_SHORT).show()
    }

    /*
    * Credential Save Success
    * */
    override fun credentialSaveSuccess() {
        Toast.makeText(this, "Credentials successfully saved", Toast.LENGTH_SHORT).show()
    }

    /*
    * Require Email Address Not Empty
    * */
    private fun emailRequirementError() {
        Toast.makeText(this, "Email address must not be empty", Toast.LENGTH_SHORT).show()
    }

    /*
    * Require Password Not Empty
    * */
    private fun passwordRequirementError() {
        Toast.makeText(this, "Password must not be empty", Toast.LENGTH_SHORT).show()
    }
    //endregion

    override fun signInSuccess(signInSuccess: SignInSuccess) {
        if (signInSuccess.hasCredential()) {
            showCredentialDialog(signInSuccess.credential)
        } else if (signInSuccess.hasGoogleSignInAccount()) {
            MainActivity.startActivity(this, signInSuccess.googleSignInAccount.email)
        }
    }

    private fun showCredentialDialog(credential: Credential?) {
        val alertDialogBuilder: AlertDialog.Builder = AlertDialog
            .Builder(this)
            .setTitle(R.string.credential_received)
            .setMessage(getString(R.string.what_do_you_want_to_do_with_credential, credential?.id))
            .setPositiveButton(getString(R.string.use_credential), { dialogInterface, i ->
                MainActivity.startActivity(this@SignInActivity, credential?.id)
            })
            .setNegativeButton(getString(R.string.delete_credential), { dialogInterface, i ->
                authManager?.deleteCredential(credential)
                dialogInterface.dismiss()
                dialogInterface.cancel()
            })

        val alertDialog: AlertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    override fun googleSignOut(status: Status) {
        Toast.makeText(this, "Google Sign-out with status: $status", Toast.LENGTH_SHORT).show()
    }

    override fun googleAccessRevoked(status: Status) {
        Toast.makeText(this, "Google Access Revoked with status: $status", Toast.LENGTH_SHORT).show()
    }


    override fun credentialDelete(status: Status) {
        Toast.makeText(this, "Credential deleted with status: $status", Toast.LENGTH_SHORT).show()
    }

    /*
    * Google API Connection Failed
    * */
    override fun onConnectionFailed(p0: ConnectionResult) {
        Log.e(localClassName, "GoogleApiClient connection failed")
    }

    override fun onConnected(p0: Bundle?) {
        Log.e(localClassName, "GoogleApiClient connected")
    }

    override fun onConnectionSuspended(p0: Int) {
        Log.e(localClassName, "GoogleApiClient connection suspended")
    }

    /*
    * Handle activity result after Google Sign-In, Credential Request or Hints requested resolution
    * */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        authManager?.handle(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        super.onDestroy()
        authManager?.destroy()
    }
}
