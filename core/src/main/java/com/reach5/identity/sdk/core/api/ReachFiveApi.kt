package com.reach5.identity.sdk.core.api

import android.util.Log
import com.google.gson.Gson
import com.reach5.identity.sdk.core.utils.Failure
import com.reach5.identity.sdk.core.utils.Success
import com.reach5.identity.sdk.core.models.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.QueryMap
import retrofit2.Response
import java.lang.Exception

interface ReachFiveApi {
    @GET("/api/v1/providers")
    fun providersConfigs(@QueryMap options: Map<String, String>): Call<ProvidersConfigsResult>

    @POST("/identity/v1/oauth/provider/token")
    fun loginWithProvider(@Body loginProviderRequest: LoginProviderRequest, @QueryMap options: Map<String, String>): Call<OpenIdTokenResponse>

    @POST("/identity/v1/signup-token")
    fun signupWithPassword(@Body signupRequest: SignupRequest, @QueryMap options: Map<String, String>): Call<OpenIdTokenResponse>

    @POST("/oauth/token")
    fun loginWithPassword(@Body loginRequest: LoginRequest, @QueryMap options: Map<String, String>): Call<OpenIdTokenResponse>

    companion object {
        fun create(config: SdkConfig): ReachFiveApi {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://${config.domain}")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(ReachFiveApi::class.java)
        }
    }
}

class ReachFiveApiCallback<T>(val success: Success<T>, val failure: Failure<ReachFiveError>): Callback<T> {
    override fun onFailure(call: Call<T>, t: Throwable) {
        Log.d("Reach5_ApiCallB", "ReachFiveApiCallback onFailure $t")
        failure(ReachFiveError.from(t.message ?: t.toString()))
    }

    override fun onResponse(call: Call<T>, response: retrofit2.Response<T>) {
        val body = response.body()
        val status = response.code()
        Log.d("Reach5_ApiCallB", "ReachFiveApiCallback onResponse $status")
        if (response.isSuccessful && body != null) {
            success(body)
        } else if (status in 300..400) {
            failure(ReachFiveError(
                message = "Bad Request",
                data = tryOrNull { parseErrorBody(response) }
            ))
        } else if (status in 400..600) {
            failure(ReachFiveError(
                message = "Technical Error",
                data = tryOrNull { parseErrorBody(response) }
            ))
        }
    }

    // TODO find better solution
    private fun<T> tryOrNull(callback: () -> T): T? {
        return try {
            callback()
        } catch (e: Exception) {
            null
        }
    }

    private fun<T> parseErrorBody(response: Response<T>): ReachFiveApiError {
        return Gson().fromJson(response.errorBody()?.string(), ReachFiveApiError::class.java)
    }
}