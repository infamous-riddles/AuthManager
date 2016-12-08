package com.github.pavlospt.signindemo.views

import com.github.pavlospt.signindemo.models.SignInSuccess

interface BaseView {
    fun signInSuccess(signInSuccess: SignInSuccess)
}