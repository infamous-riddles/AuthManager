package com.github.pavlospt.signindemo.views

import com.github.pavlospt.signindemo.views.BaseView
import com.google.android.gms.common.api.Status

interface SmartLockView : BaseView {

    fun credentialSaveResolutionCancelled()
    fun credentialSaveResolutionFailure()

    fun credentialRequestFailure()
    fun credentialRequestResolutionFailure()
    fun credentialRequestCancelled()

    fun credentialSaveFailure()
    fun credentialSaveSuccess()
    fun credentialDelete(status: Status)
}
