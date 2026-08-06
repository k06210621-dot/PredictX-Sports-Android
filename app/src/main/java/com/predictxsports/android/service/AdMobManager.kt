package com.predictxsports.android.service

import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.FullScreenContentCallback
import com.predictxsports.android.BuildConfig

object AdMobManager {
    private const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
    private const val PROD_REWARDED_AD_UNIT_ID = "ca-app-pub-6186518924141006/3633970707"
    private const val MAX_RETRY = 2

    var isTestMode = BuildConfig.DEBUG
    var rewardedAd: RewardedAd? = null
    private var retryCount = 0

    private val adUnitId: String
        get() = if (isTestMode) TEST_REWARDED_AD_UNIT_ID else PROD_REWARDED_AD_UNIT_ID

    fun initialize(context: Context) {
        MobileAds.initialize(context) { }
    }

    fun loadRewardedAd(
        context: Context,
        onLoaded: () -> Unit,
        onFailed: (String) -> Unit
    ) {
        val request = AdRequest.Builder().build()
        RewardedAd.load(context, adUnitId, request, object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                rewardedAd = ad
                retryCount = 0
                onLoaded()
            }
            override fun onAdFailedToLoad(error: LoadAdError) {
                rewardedAd = null
                if (retryCount < MAX_RETRY) {
                    retryCount++
                    android.util.Log.w("AdMobManager", "loadRewardedAd failed (attempt $retryCount/$MAX_RETRY): ${error.message}, retrying...")
                    // Exponential backoff: 1s, 3s
                    val delayMs = if (retryCount == 1) 1000L else 3000L
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        loadRewardedAd(context, onLoaded, onFailed)
                    }, delayMs)
                } else {
                    retryCount = 0
                    onFailed("廣告載入失敗，請稍後再試")
                }
            }
        })
    }

    fun showRewardedAd(
        activity: android.app.Activity,
        onEarned: () -> Unit,
        onFailed: (String) -> Unit
    ) {
        rewardedAd?.let { ad ->
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() { rewardedAd = null }
                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    rewardedAd = null
                    onFailed(error.message)
                }
            }
            ad.show(activity) { onEarned() }
        } ?: onFailed("廣告尚未載入，請稍後再試")
    }
}