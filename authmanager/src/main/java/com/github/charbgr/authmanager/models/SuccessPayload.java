package com.github.charbgr.authmanager.models;

import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

public class SuccessPayload {

    private GoogleSignInAccount googleSignInAccount;
    private Credential credential;

    public SuccessPayload(GoogleSignInAccount googleSignInAccount, Credential credential) {
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
