package com.eleonorez.cunny.data.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BillingManager(private val context: Context) {

    companion object {
        private const val TAG = "BillingManager"
        const val PRODUCT_ID_PREMIUM_MONTHLY = "cunny_premium_monthly"
        private const val MAX_RETRY_ATTEMPTS = 3
        private const val RETRY_DELAY_MS = 2000L

        @Volatile
        private var instance: BillingManager? = null

        fun getInstance(context: Context): BillingManager {
            return instance ?: synchronized(this) {
                instance ?: BillingManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val _availableProducts = MutableStateFlow<List<ProductDetails>>(emptyList())
    val availableProducts: StateFlow<List<ProductDetails>> = _availableProducts.asStateFlow()

    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
    val purchaseState: StateFlow<PurchaseState> = _purchaseState.asStateFlow()

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    handlePurchase(purchase)
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Log.d(TAG, "User cancelled the purchase")
                _purchaseState.value = PurchaseState.Cancelled
            }
            else -> {
                Log.e(TAG, "Purchase failed: ${billingResult.debugMessage}")
                _purchaseState.value = PurchaseState.Error(billingResult.debugMessage)
            }
        }
    }

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .enablePrepaidPlans()
                .build()
        )
        .build()

    private var retryCount = 0

    fun startConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing client connected successfully")
                    retryCount = 0
                    // Query existing purchases to restore state
                    scope.launch {
                        queryExistingPurchases()
                        querySubscriptionProducts(PRODUCT_ID_PREMIUM_MONTHLY)
                    }
                } else {
                    Log.e(TAG, "Billing setup failed: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Billing service disconnected")
                retryConnection()
            }
        })
    }

    private fun retryConnection() {
        if (retryCount < MAX_RETRY_ATTEMPTS) {
            retryCount++
            Log.d(TAG, "Retrying billing connection (attempt $retryCount/$MAX_RETRY_ATTEMPTS)")
            scope.launch {
                delay(RETRY_DELAY_MS * retryCount)
                startConnection()
            }
        } else {
            Log.e(TAG, "Max retry attempts reached for billing connection")
        }
    }

    suspend fun querySubscriptionProducts(productId: String) {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                _availableProducts.value = productDetailsList
                Log.d(TAG, "Found ${productDetailsList.size} subscription products")
            } else {
                Log.e(TAG, "Failed to query products: ${billingResult.debugMessage}")
            }
        }
    }

    fun launchPurchaseFlow(activity: Activity, productDetails: ProductDetails) {
        // For subscriptions, get the first available offer token
        val offerToken = productDetails.subscriptionOfferDetails
            ?.firstOrNull()?.offerToken

        if (offerToken == null) {
            Log.e(TAG, "No offer token found for subscription")
            _purchaseState.value = PurchaseState.Error("No subscription offers available")
            return
        }

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(offerToken)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        _purchaseState.value = PurchaseState.Loading
        val billingResult = billingClient.launchBillingFlow(activity, billingFlowParams)

        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            Log.e(TAG, "Failed to launch billing flow: ${billingResult.debugMessage}")
            _purchaseState.value = PurchaseState.Error(billingResult.debugMessage)
        }
    }

    suspend fun queryExistingPurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val hasActiveSub = purchases.any { purchase ->
                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                        purchase.products.contains(PRODUCT_ID_PREMIUM_MONTHLY)
                }
                _isPremium.value = hasActiveSub
                Log.d(TAG, "Premium status: $hasActiveSub (${purchases.size} subscriptions found)")

                // Acknowledge any un-acknowledged purchases
                purchases.filter {
                    it.purchaseState == Purchase.PurchaseState.PURCHASED && !it.isAcknowledged
                }.forEach { purchase ->
                    scope.launch { acknowledgePurchase(purchase.purchaseToken) }
                }
            } else {
                Log.e(TAG, "Failed to query purchases: ${billingResult.debugMessage}")
            }
        }
    }

    suspend fun acknowledgePurchase(purchaseToken: String) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()

        billingClient.acknowledgePurchase(params) { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "Purchase acknowledged successfully")
            } else {
                Log.e(TAG, "Failed to acknowledge purchase: ${billingResult.debugMessage}")
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        when (purchase.purchaseState) {
            Purchase.PurchaseState.PURCHASED -> {
                Log.d(TAG, "Purchase completed: ${purchase.products}")
                _isPremium.value = true
                _purchaseState.value = PurchaseState.Success

                // Acknowledge if not already done
                if (!purchase.isAcknowledged) {
                    scope.launch { acknowledgePurchase(purchase.purchaseToken) }
                }

                // Verify with backend (fire-and-forget for MVP)
                scope.launch {
                    verifyPurchaseWithBackend(
                        purchaseToken = purchase.purchaseToken,
                        productId = purchase.products.firstOrNull() ?: PRODUCT_ID_PREMIUM_MONTHLY
                    )
                }
            }
            Purchase.PurchaseState.PENDING -> {
                Log.d(TAG, "Purchase pending: ${purchase.products}")
                _purchaseState.value = PurchaseState.Pending
            }
            else -> {
                Log.d(TAG, "Purchase unspecified state: ${purchase.purchaseState}")
                _purchaseState.value = PurchaseState.Error("Unspecified purchase state")
            }
        }
    }

    private suspend fun verifyPurchaseWithBackend(purchaseToken: String, productId: String) {
        try {
            val apiService = com.eleonorez.cunny.data.retrofit.ApiConfig.getApiService()
            apiService.verifyPurchase(
                mapOf(
                    "purchaseToken" to purchaseToken,
                    "productId" to productId,
                    "packageName" to context.packageName
                )
            )
            Log.d(TAG, "Backend purchase verification successful")
        } catch (e: Exception) {
            Log.e(TAG, "Backend purchase verification failed: ${e.message}", e)
            // Don't revert premium status — billing client is the source of truth
        }
    }

    fun endConnection() {
        billingClient.endConnection()
    }

    sealed class PurchaseState {
        data object Idle : PurchaseState()
        data object Loading : PurchaseState()
        data object Success : PurchaseState()
        data object Pending : PurchaseState()
        data object Cancelled : PurchaseState()
        data class Error(val message: String) : PurchaseState()
    }
}
