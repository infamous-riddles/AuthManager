package com.github.pavlospt.signindemo;

import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

public class SignInSuccess {

    private GoogleSignInAccount googleSignInAccount;
    private Credential credential;

    public SignInSuccess(GoogleSignInAccount googleSignInAccount, Credential credential) {
        this.googleSignInAccount = googleSignInAccount;
        this.credential = credential;
    }

    public boolean hasGoogleSignInAccount() {
        return googleSignInAccount != null;
    }

    public GoogleSignInAccount getGoogleSignInAccount() {
        return googleSignInAccount;
    }

    public boolean hasCredential() {
        return credential != null;
    }

    public Credential getCredential() {
        return credential;
    }
}
