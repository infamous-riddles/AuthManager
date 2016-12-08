package com.github.pavlospt.signindemo

import android.content.Intent

interface HintView : BaseView {
    fun emailHintRequestSuccess(data: Intent?)
    fun emailHintRequestCancelled()
    fun emailHintRequestFailure()
}
