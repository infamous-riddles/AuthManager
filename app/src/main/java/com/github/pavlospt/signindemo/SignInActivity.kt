package com.github.pavlospt.signindemo

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.CredentialPickerConfig
import com.google.android.gms.auth.api.credentials.CredentialRequest
import com.google.android.gms.auth.api.credentials.HintRequest
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.GoogleApiClient


class SignInActivity : AppCompatActivity(),
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks,
        GoogleView, SmartLockView, HintView {


    private var authManager: AuthManager? = null


    /*
    * Views
    * */
    private lateinit var signInButton: SignInButton
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
            MainActivity.startActivity(this, signInSuccess.credential.id)
        } else if (signInSuccess.hasGoogleSignInAccount()) {
            MainActivity.startActivity(this, signInSuccess.googleSignInAccount.email)
        }
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
}
