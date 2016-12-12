package com.github.charbgr.authmanager.views

import com.google.android.gms.auth.api.credentials.Credential

interface HintView : BaseView {
    fun emailHintRequestCancelled()
    fun emailHintRequestFailure()
    fun emailHintSelected(credential: Credential?)
}
