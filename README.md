[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-AuthManager-brightgreen.svg?style=flat)]()

# Auth Manager

AuthManager is a library which eliminates the boilerplate of Google SignIn and SmartLock integration.

# Usage

```groovy
compile 'com.github.charbgr:authmanager:1.0'
```

<br/>
AuthManager includes all of the managers below.

```kotlin
AuthManager
    .Builder(this)
    .withGoogleApiClient(googleApiClient)
    .withGoogle(this)
    .withHints(this, hintRequest)
    .withSmartLock(this, smartlockRequest)
    .build()
    
override fun onDestroy() {
    super.onDestroy()
    authManager.destroy()
}

override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    authManager?.handle(requestCode, resultCode, data)
}
    
```

# Standalone Managers

## Google Manager
_Functionality included: Google Sign-In, Google Sign-Out, Google Revoke Access_

```kotlin
GoogleAuthManager
    .Builder(this)
    .withGoogleApiClient(googleApiClient)
    .withGoogleView(this)
    .build()
    
override fun onDestroy() {
    super.onDestroy()
    googleAuthManager?.clear()
}

override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    when (requestCode) {
        RC_SIGN_IN -> googleAuthManager?.handle(resultCode, data)
    }
}
```

### API Overview

```kotlin
fun signInWithGoogle()
fun signOutOfGoogle()
fun revokeGoogleAccess()
fun handle(resultCode: Int, data: Intent?)
fun clear()
```

## Smartlock Manager
_Functionality included: SmartLock Credentials Request, SmartLock Credentials Save, SmartLock Credentials Delete_

```kotlin
SmartLockManager
    .Builder(this)
    .withGoogleApiClient(googleApiClient)
    .withSmartLockView(this)
    .build()
    
    
override fun onDestroy() {
    super.onDestroy()
    smartLockManager?.clear()
}

override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    when (requestCode) {
        RC_CREDENTIAL_SAVE -> smartLockManager?.handleCredentialSave(resultCode)
        RC_CREDENTIALS_REQUEST -> smartLockManager?.handleCredentialRequest(resultCode, data)
    }
} 
```
### API Overview

```kotlin
fun requestCredentials()
fun saveCredential(credential: Credential)
fun deleteCredential(credential: Credential?)
fun handleCredentialSave(resultCode: Int)
fun handleCredentialRequest(resultCode: Int, data: Intent?)
fun clear()
```

## Hints Manager
_Functionality included: E-mail Addresses Hints_

```kotlin
HintsManager
    .Builder(this)
    .withGoogleApiClient(googleApiClient)
    .withHintsView(this)
    .build()
    
    
override fun onDestroy() {
    super.onDestroy()
    hintsManager?.clear()
}

override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    when (requestCode) {
        RC_HINT_REQUEST -> hintsManager?.handle(resultCode, data)
    }
}
```
### API Overview

```kotlin
fun requestEmailHints()
fun handle(resultCode: Int, data: Intent?)
fun clear()
```

You need to call in ```Activity.onDestroy()``` to clear view references otherwise you risk having a memory leak.

