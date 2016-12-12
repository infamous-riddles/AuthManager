package com.github.charbgr.authmanager.views

import com.github.charbgr.authmanager.models.SuccessPayload

interface BaseView {
    fun signInSuccess(signInSuccess: SuccessPayload)
}