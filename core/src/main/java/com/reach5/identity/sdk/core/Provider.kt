package com.reach5.identity.sdk.core

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.reach5.identity.sdk.core.api.ReachFiveApi
import com.reach5.identity.sdk.core.models.OpenIdTokenResponse
import com.reach5.identity.sdk.core.models.ProviderConfig
import com.reach5.identity.sdk.core.models.ReachFiveError
import com.reach5.identity.sdk.core.models.SdkConfig
import com.reach5.identity.sdk.core.utils.Failure
import com.reach5.identity.sdk.core.utils.Success

interface ProviderCreator {
    val name: String
    fun create(providerConfig: ProviderConfig, sdkConfig: SdkConfig, reachFiveApi: ReachFiveApi, context: Context): Provider
}

/**
 * Common interface of the provider
 */
interface Provider {
    val name: String
    /**
     * Is an identifier of the request, tha identifies the return of an activity
     */
    val requestCode: Int
    fun login(origin: String, activity: Activity)
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?, success: Success<OpenIdTokenResponse>, failure: Failure<ReachFiveError>)
    fun onStop() {}
    fun logout() { /* TODO */ }
}



