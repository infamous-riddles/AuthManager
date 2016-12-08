package com.github.pavlospt.signindemo

import android.content.Intent

interface SmartLockView : BaseView{

    fun credentialSaveResolutionCancelled()
    fun credentialSaveResolutionFailure()

    fun credentialRequestFailure()
    fun credentialRequestResolutionFailure()
    fun credentialRequestCancelled()
    fun credentialRequestResolutionSuccess(data: Intent?)

    fun credentialSaveFailure()
    fun credentialSaveSuccess()

}
