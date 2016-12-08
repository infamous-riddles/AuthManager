package com.github.pavlospt.signindemo

interface SmartLockView : BaseView{

    fun credentialSaveResolutionCancelled()
    fun credentialSaveResolutionFailure()

    fun credentialRequestFailure()
    fun credentialRequestResolutionFailure()
    fun credentialRequestCancelled()

    fun credentialSaveFailure()
    fun credentialSaveSuccess()

}
