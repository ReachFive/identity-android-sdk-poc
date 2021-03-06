package com.reach5.identity.sdk.core.api

import com.google.gson.Gson
import com.reach5.identity.sdk.core.models.ReachFiveApiError
import com.reach5.identity.sdk.core.models.ReachFiveError
import com.reach5.identity.sdk.core.utils.Failure
import com.reach5.identity.sdk.core.utils.Success
import com.reach5.identity.sdk.core.utils.SuccessWithNoContent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReachFiveApiCallback<T>(
    val success: Success<T> = { Unit },
    val successWithNoContent: SuccessWithNoContent<Unit> = { Unit },
    val failure: Failure<ReachFiveError>
) : Callback<T> {
    override fun onFailure(call: Call<T>, t: Throwable) {
        failure(ReachFiveError.from(t))
    }

    override fun onResponse(call: Call<T>, response: Response<T>) {
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) success(body)
            else successWithNoContent(Unit)
        } else {
            val data = tryOrNull { parseErrorBody(response) }
            failure(
                ReachFiveError(
                    message = data?.error ?: "ReachFive API response error",
                    code = response.code(),
                    data = data
                )
            )
        }
    }

    private fun <T> tryOrNull(callback: () -> T): T? {
        return try {
            callback()
        } catch (e: Exception) {
            null
        }
    }

    private fun <T> parseErrorBody(response: Response<T>): ReachFiveApiError {
        return Gson().fromJson(response.errorBody()?.string(), ReachFiveApiError::class.java)
    }
}
