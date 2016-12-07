package com.github.pavlospt.signindemo

import android.content.Intent
import android.content.IntentSender
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.credentials.*
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.*


class SignInActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    /*
    * Google related variables
    * */
    private val RC_SIGN_IN = 1
    private val RC_CREDENTIALS_REQUEST = 2
    private val RC_HINT_REQUEST = 3
    private val RC_CREDENTIAL_SAVE = 4

    private lateinit var googleApiClient: GoogleApiClient
    private lateinit var googleSignInOptions: GoogleSignInOptions
    private lateinit var smartlockCredentialsRequest: CredentialRequest
    private lateinit var hintRequest: HintRequest

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

        initGoogleSignInOptions()
        initSmartlockCredentialsRequest()
        initHintRequest()
        initGoogleApiClient()
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
            requestCredentials()
        }

        requestHintsButton.setOnClickListener {
            requestEmailHints()
        }

        signInButton.setOnClickListener {
            initiateGoogleSignIn()
        }

        saveCredentialsButton.setOnClickListener {
            saveCredentials()
        }
    }

    //region Initializations
    /*
    * Initialize Hint request
    * */
    private fun initHintRequest() {
        hintRequest = HintRequest.Builder()
                .setHintPickerConfig(
                        CredentialPickerConfig.Builder()
                                .setShowCancelButton(true)
                                .setPrompt(CredentialPickerConfig.Prompt.SIGN_IN)
                                .build()
                )
                .setEmailAddressIdentifierSupported(true)
                .build()
    }

    /*
    * Initialize Google Sign-in options
    * */
    private fun initGoogleSignInOptions() {
        googleSignInOptions = GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
    }

    /*
    * Initialize Smartlock credentials request
    * */
    private fun initSmartlockCredentialsRequest() {
        smartlockCredentialsRequest = CredentialRequest.Builder()
                .setPasswordLoginSupported(true)
                .build()
    }

    /*
    * Initialize Google API Client
    * */
    private fun initGoogleApiClient() {
        googleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                .addApi(Auth.CREDENTIALS_API)
                .build()
    }
    //endregion

    //region GoogleSignIn

    /*
    * Initiate Sign In
    * */
    private fun initiateGoogleSignIn() {
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient)
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    /*
    * Handle Sign In Resolution
    * */
    private fun handleGoogleSignInResolution(resultCode: Int, data: Intent?) {
        if (resultCode == AppCompatActivity.RESULT_CANCELED) {
            userCancelledGoogleSignIn()
        } else {
            userGoogleSignInSuccess(data)
        }
    }

    /*
    * User Google Sign-In Success
    * */
    private fun userGoogleSignInSuccess(data: Intent?) {
        val googleSignInResult: GoogleSignInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data)

        if (googleSignInResult.isSuccess) {
            val googleSignInAccount: GoogleSignInAccount? = googleSignInResult.signInAccount
            proceedOnMainScreen(googleSignInAccount?.email)
        } else {
            googleSignInResultFailure()
        }
    }

    /*
    * Google Sign-In Failure
    * */
    private fun googleSignInResultFailure() {
        Toast.makeText(this, "Authentication failure", Toast.LENGTH_SHORT).show()
    }

    /*
    * User Cancelled Google Sign-In
    * */
    private fun userCancelledGoogleSignIn() {
        Toast.makeText(this, "User cancelled Google Sign-In flow", Toast.LENGTH_SHORT).show()
    }
    //endregion

    //region CredentialsRequest

    /*
    * Handle Credential Request Resolution
    * */
    private fun handleCredentialRequestResolution(resultCode: Int, data: Intent?) {
        if (resultCode == AppCompatActivity.RESULT_CANCELED) {
            credentialRequestCancelled()
        } else {
            credentialRequestResolutionSuccess(data)
        }
    }

    /*
    * Credential Request Resolution Success
    * */
    private fun credentialRequestResolutionSuccess(data: Intent?) {
        val credential: Credential? = data?.getParcelableExtra(Credential.EXTRA_KEY)
        credential?.let {
            proceedOnMainScreen(it.id)
        }
    }

    /*
    * Request Credentials
    * */
    private fun requestCredentials() {
        Auth
                .CredentialsApi
                .request(googleApiClient, smartlockCredentialsRequest)
                .setResultCallback({ credentialRequestResult ->
                    handleCredentialRequestResult(credentialRequestResult)
                })
    }

    /*
    * Handle Credential Request Result
    * */
    private fun handleCredentialRequestResult(credentialRequestResult: CredentialRequestResult) {
        if (credentialRequestResult.status.isSuccess) {
            proceedOnMainScreen(credentialRequestResult.credential.id)
        } else {
            resolveCredentialRequest(credentialRequestResult.status)
        }
    }

    private fun resolveCredentialRequest(status: Status?) {
        if (status?.statusCode == CommonStatusCodes.RESOLUTION_REQUIRED) {
            initiateCredentialRequestResolution(status)
        } else {
            credentialRequestFailure()
        }
    }

    /*
    * Initiate Credential Request Resolution
    * */
    private fun initiateCredentialRequestResolution(status: Status?) {
        try {
            status?.startResolutionForResult(this, RC_CREDENTIALS_REQUEST)
        } catch (sendIntentException: IntentSender.SendIntentException) {
            credentialRequestResolutionFailure()
        }
    }

    /*
    * Credential Request Cancelled
    * */
    private fun credentialRequestCancelled() {
        Toast.makeText(this, "Credential Request cancelled", Toast.LENGTH_SHORT).show()
    }

    /*
    * Credential Request Resolution Failed
    * */
    private fun credentialRequestResolutionFailure() {
        Toast.makeText(this, "Credential Request Resolution failure", Toast.LENGTH_SHORT).show()
    }

    /*
    * Credential Request Failed
    * */
    private fun credentialRequestFailure() {
        Toast.makeText(this, "Credential Request failure", Toast.LENGTH_SHORT).show()
    }

    //endregion

    //region EmailHintRequest
    /*
    * Request Email Hints
    * */
    private fun requestEmailHints() {
        val intent = Auth.CredentialsApi.getHintPickerIntent(googleApiClient, hintRequest)
        try {
            startIntentSenderForResult(intent.intentSender, RC_HINT_REQUEST, null, 0, 0, 0)
        } catch (e: IntentSender.SendIntentException) {
            emailHintRequestFailure()
        }
    }

    /*
    * Handle Hint Request Resolution
    * */
    private fun handleEmailHintRequestResolution(resultCode: Int, data: Intent?) {
        if (resultCode == AppCompatActivity.RESULT_CANCELED) {
            emailHintRequestCancelled()
        } else {
            emailHintRequestSuccess(data)
        }
    }

    /*
    * Handle Hint Request Success
    * */
    private fun emailHintRequestSuccess(data: Intent?) {
        val credential: Credential? = data?.getParcelableExtra(Credential.EXTRA_KEY)
        credential?.let {
            proceedOnMainScreen(it.id)
        }
    }

    /*
    * Hint Request Cancelled
    * */
    private fun emailHintRequestCancelled() {
        Toast.makeText(this, "Hint Request cancelled by user", Toast.LENGTH_SHORT).show()
    }

    /*
    * Hint Request Failed
    * */
    private fun emailHintRequestFailure() {
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

        Auth
                .CredentialsApi
                .save(googleApiClient, credentialToSave)
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
            credentialSaveSuccess()
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
                status.startResolutionForResult(this, RC_CREDENTIAL_SAVE)
            } catch (e: IntentSender.SendIntentException) {
                credentialSaveResolutionFailure()
            }
        } else {
            credentialSaveFailure()
        }
    }

    /*
    * Handle Credential Save Resolution
    * */
    private fun handleCredentialSaveResolution(resultCode: Int, data: Intent?) {
        if (resultCode == AppCompatActivity.RESULT_CANCELED) {
            credentialSaveResolutionCancelled()
        } else {
            credentialSaveSuccess()
        }
    }

    /*
    * User Cancelled Credential Save Resolution
    * */
    private fun credentialSaveResolutionCancelled() {
        Toast.makeText(this, "User cancelled credential save resolution", Toast.LENGTH_SHORT).show()
    }

    /*
    * Credential Save Resolution Failed
    * */
    private fun credentialSaveResolutionFailure() {
        Toast.makeText(this, "Credential save resolution failed", Toast.LENGTH_SHORT).show()
    }

    /*
    * Credential Save Failed
    * */
    private fun credentialSaveFailure() {
        Toast.makeText(this, "Credential save failed", Toast.LENGTH_SHORT).show()
    }

    /*
    * Credential Save Success
    * */
    private fun credentialSaveSuccess() {
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

    /*
    * Move to next screen with the user's email
    * */
    private fun proceedOnMainScreen(userEmail: String?) {
        MainActivity.startActivity(this, userEmail)
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

        when (requestCode) {
            RC_SIGN_IN -> handleGoogleSignInResolution(resultCode, data)
            RC_CREDENTIALS_REQUEST -> handleCredentialRequestResolution(resultCode, data)
            RC_HINT_REQUEST -> handleEmailHintRequestResolution(resultCode, data)
            RC_CREDENTIAL_SAVE -> handleCredentialSaveResolution(resultCode, data)
        }
    }
}
