package com.github.pavlospt.signindemo

import com.github.pavlospt.authmanager.models.SignInSuccess

interface BaseView {
    fun signInSuccess(signInSuccess: SignInSuccess)
}