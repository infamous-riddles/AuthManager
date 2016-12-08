package com.github.pavlospt.signindemo

import com.google.android.gms.common.api.Status

interface GoogleView : BaseView{
    fun googleSignInResultFailure()
    fun userCancelledGoogleSignIn()
    fun googleSignOut(status: Status)
    fun googleAccessRevoked(status: Status)
}
