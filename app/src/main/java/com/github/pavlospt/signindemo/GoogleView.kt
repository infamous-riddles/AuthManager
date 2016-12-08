package com.github.pavlospt.signindemo

import android.content.Intent

interface GoogleView : BaseView{
    fun googleSignInResultFailure()
    fun userGoogleSignInSuccess(data: Intent?)
    fun userCancelledGoogleSignIn()
}
