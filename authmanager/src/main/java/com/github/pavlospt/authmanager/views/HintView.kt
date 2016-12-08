package com.github.pavlospt.authmanager.views

import com.github.pavlospt.signindemo.BaseView

interface HintView : BaseView {
    fun emailHintRequestCancelled()
    fun emailHintRequestFailure()
}
