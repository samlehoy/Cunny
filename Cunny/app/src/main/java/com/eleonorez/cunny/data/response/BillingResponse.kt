package com.eleonorez.cunny.data.response

import com.google.gson.annotations.SerializedName

data class BillingVerifyResponse(
    @SerializedName("error")
    val error: Boolean,
    @SerializedName("message")
    val message: String?,
    @SerializedName("purchase")
    val purchase: PurchaseRecord?
)

data class PurchaseRecord(
    @SerializedName("id")
    val id: Int?,
    @SerializedName("user_id")
    val userId: Int?,
    @SerializedName("product_id")
    val productId: String?,
    @SerializedName("purchase_token")
    val purchaseToken: String?,
    @SerializedName("status")
    val status: String?,
    @SerializedName("purchased_at")
    val purchasedAt: String?,
    @SerializedName("expires_at")
    val expiresAt: String?
)
