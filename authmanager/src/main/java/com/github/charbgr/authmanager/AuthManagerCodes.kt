package com.github.charbgr.authmanager


class AuthManagerCodes private constructor() {

    companion object {
        @JvmField
        val RC_SIGN_IN = 1

        @JvmField
        val RC_CREDENTIALS_REQUEST = 2

        @JvmField
        val RC_HINT_REQUEST = 3

        @JvmField
        val RC_CREDENTIAL_SAVE = 4
    }

}

