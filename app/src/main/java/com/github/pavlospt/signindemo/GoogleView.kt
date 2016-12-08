package com.github.pavlospt.signindemo

interface GoogleView : BaseView{
    fun googleSignInResultFailure()
    fun userCancelledGoogleSignIn()
}
