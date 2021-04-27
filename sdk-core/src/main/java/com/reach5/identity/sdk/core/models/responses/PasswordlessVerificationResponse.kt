package com.reach5.identity.sdk.core.models.responses

import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PasswordlessVerificationResponse(
    @SerializedName("code")
    val authCode: String
)
