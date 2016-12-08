package com.github.pavlospt.signindemo

interface HintView : BaseView {
    fun emailHintRequestCancelled()
    fun emailHintRequestFailure()
}
