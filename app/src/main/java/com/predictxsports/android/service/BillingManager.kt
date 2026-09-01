package com.predictxsports.android.service

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Google Play Billing Manager — PredictX Sports
 *
 * 支援 Google Play Billing Library 8.0+
 * - 移除已棄用的 SkuDetails/skuDetailsList
 * - 使用 ProductDetails / ProductDetailsResponseListener
 * - 使用 SubscriptionOfferDetails 替代舊版定價資訊
 */
object BillingManager {
    const val TAG = "BillingManager"

    val SUBSCRIPTION_SKUS = listOf(
        "predictx_basic_monthly",
        "predictx_basic_yearly",
        "predictx_standard_monthly",
        "predictx_standard_yearly"
    )

    /** 購買完成回呼 — BillingViewModel 註冊以接收購買/恢復事件 */
    interface PurchaseListener {
        fun onPurchaseCompleted(productId: String)
    }

    private var purchaseListener: PurchaseListener? = null

    fun setPurchaseListener(listener: PurchaseListener?) {
        purchaseListener = listener
    }

    /** 暴露 BillingClient 給 BillingViewModel 做產品查詢（避免重複建立實例） */
    fun getBillingClient(): BillingClient? = billingClient

    private var billingClient: BillingClient? = null

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _skus = MutableStateFlow<List<SkuInfo>>(emptyList())
    val skus: StateFlow<List<SkuInfo>> = _skus.asStateFlow()

    /** 包裝後的 SKU 資訊（用於 UI 顯示價格 + 啟動購買） */
    data class SkuInfo(
        val productId: String,
        val productType: String,
        val formattedPrice: String,
        val productDetails: ProductDetails,
        val offerToken: String? = null
    )

    private var applicationContext: Context? = null

    /** 初始化 Billing Client — 從 Application.onCreate() 呼叫 */
    fun initialize(context: Context) {
        if (billingClient != null && _isReady.value) return

        applicationContext = context.applicationContext

        billingClient = BillingClient.newBuilder(context)
            .setListener(PurchasesUpdatedListener { billingResult: BillingResult, purchases: MutableList<Purchase>? ->
                handlePurchases(billingResult, purchases)
            })
            .enablePendingPurchases(
                com.android.billingclient.api.PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .build()
            )
            .build()

        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    _isReady.value = true
                    Log.d(TAG, "BillingClient 已就緒")
                    querySubscriptions()
                } else {
                    Log.w(TAG, "BillingClient 連接失敗: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "BillingClient 連線中斷，嘗試重連")
                _isReady.value = false
                initialize(context)
            }
        })
    }

    /** 查詢可訂閱 SKU 詳情 (Billing Library 8.0+ 使用 ProductDetails) */
    private fun querySubscriptions() {
        val client = billingClient ?: return
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                SUBSCRIPTION_SKUS.map { sku ->
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(sku)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                }
            ).build()

        client.queryProductDetailsAsync(params) { billingResult, queryProductDetailsResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val list: List<ProductDetails> = queryProductDetailsResult.productDetailsList ?: emptyList()
                _skus.value = list.map { p: ProductDetails ->
                    val offers = p.subscriptionOfferDetails
                    val offer = if (offers != null) offers.firstOrNull() else null
                    val offerToken = offer?.offerToken
                    val pricingPhases = offer?.pricingPhases
                    val pricingPhase = pricingPhases?.pricingPhaseList?.firstOrNull()
                    val formattedPrice = pricingPhase?.formattedPrice ?: "—"
                    SkuInfo(
                        productId = p.productId,
                        productType = p.productType,
                        formattedPrice = formattedPrice,
                        productDetails = p,
                        offerToken = offerToken
                    )
                }
            } else {
                Log.w(TAG, "查詢 SKU 失敗: ${billingResult.debugMessage}")
            }
        }
    }

    /** 啟動購買流程 — 從 SubscribeView 點擊「訂閱」按鈕時呼叫 */
    fun launchPurchaseFlow(activity: Activity, skuId: String) {
        val client = billingClient ?: run {
            Log.w(TAG, "BillingClient 尚未就緒")
            return
        }

        val skuInfo = _skus.value.firstOrNull { it.productId == skuId }
        if (skuInfo == null) {
            Log.w(TAG, "找不到 SKU: $skuId — 可能 Play Console 尚未建立")
            return
        }

        val offerToken = skuInfo.offerToken
        val productDetails = skuInfo.productDetails
        if (offerToken == null || productDetails == null) {
            Log.w(TAG, "SKU $skuId 缺少 offerToken / productDetails")
            return
        }

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .setOfferToken(offerToken)
                        .build()
                )
            )
            .build()

        client.launchBillingFlow(activity, flowParams)
    }

    /** 處理 onPurchasesUpdated callback */
    private fun handlePurchases(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                        && !purchase.isAcknowledged
                    ) {
                        acknowledgePurchase(purchase.purchaseToken)
                    }
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        purchase.products.firstOrNull()?.let { productId ->
                            purchaseListener?.onPurchaseCompleted(productId)
                        }
                    }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Log.d(TAG, "使用者取消購買")
            }
            else -> {
                Log.w(TAG, "購買錯誤: ${billingResult.debugMessage}")
            }
        }
    }

    /** 確認/acknowledge 購買 — 必須在 3 天內完成，否則 Google 自動退費 */
    fun acknowledgePurchase(token: String) {
        val client = billingClient ?: return
        val params = com.android.billingclient.api.AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(token)
            .build()

        client.acknowledgePurchase(params) { result ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "購買已驗證: $token")
            }
        }
    }

    /** 查詢既有訂閱（用於「恢復購買」） */
    fun queryExistingPurchases(callback: (List<Purchase>) -> Unit) {
        val client = billingClient ?: return
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        client.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val list = purchases ?: emptyList()
                callback(list)
            } else {
                callback(emptyList())
            }
        }
    }
}

data class SkuInfo(
    val productId: String,
    val productType: String,
    val formattedPrice: String,
    val productDetails: ProductDetails,
    val offerToken: String? = null
)