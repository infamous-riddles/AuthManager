package com.github.pavlospt.signindemo.views

import com.github.pavlospt.signindemo.views.BaseView
import com.google.android.gms.auth.api.credentials.Credential

interface HintView : BaseView {
    fun emailHintRequestCancelled()
    fun emailHintRequestFailure()
    fun emailHintSelected(credential: Credential?)
}
