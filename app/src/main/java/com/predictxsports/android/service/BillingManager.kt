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

    /** 🐛 B3：購買流程錯誤訊息（UI 訂閱顯示），null = 無錯誤 */
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /** 清除錯誤訊息（進入訂閱頁或重試時呼叫） */
    fun clearError() {
        _errorMessage.value = null
    }

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
        _errorMessage.value = null  // 🐛 B3：每次嘗試前清除舊錯誤
        val client = billingClient ?: run {
            Log.w(TAG, "BillingClient 尚未就緒")
            _errorMessage.value = "訂閱服務尚未連線，請稍候再試"
            return
        }

        val skuInfo = _skus.value.firstOrNull { it.productId == skuId }
        if (skuInfo == null) {
            Log.w(TAG, "找不到 SKU: $skuId — 可能 Play Console 尚未建立")
            _errorMessage.value = "此訂閱方案尚未開通（$skuId），請稍後再試或聯絡開發者"
            return
        }

        val offerToken = skuInfo.offerToken
        if (offerToken == null) {
            Log.w(TAG, "SKU $skuId 缺少 offerToken")
            _errorMessage.value = "此訂閱方案的定價資訊尚未就緒，請稍後再試"
            return
        }
        val productDetails = skuInfo.productDetails

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
                _errorMessage.value = null  // 🐛 B3：取消不算錯誤，清掉舊訊息
            }
            else -> {
                Log.w(TAG, "購買錯誤: ${billingResult.debugMessage}")
                // 🐛 B3：把 Play 回傳的錯誤碼轉成使用者可讀訊息
                _errorMessage.value = when (billingResult.responseCode) {
                    BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> "您已擁有此訂閱方案"
                    BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> "此訂閱方案目前無法購買，請稍後再試"
                    BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> "您的裝置無法使用 Google Play 付款服務"
                    BillingClient.BillingResponseCode.DEVELOPER_ERROR -> "發生設定錯誤，請聯絡開發者"
                    BillingClient.BillingResponseCode.ERROR -> "購買發生錯誤，請稍後再試"
                    BillingClient.BillingResponseCode.NETWORK_ERROR -> "網路連線失敗，請檢查網路後再試"
                    else -> "購買失敗（錯誤代碼 ${billingResult.responseCode}），請稍後再試"
                }
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