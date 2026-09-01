package com.predictxsports.android

import android.app.Application
import android.util.Log

import com.predictxsports.android.service.BillingViewModel
import com.predictxsports.android.service.MembershipTier

class PredictXApplication : Application() {
    companion object {
        const val TAG = "PredictX"
    }

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "PredictX Sports Android — Application started")
        }

        // BillingManager 初始化（PushServiceManager 已停用）
        com.predictxsports.android.service.BillingManager.initialize(this)

        // 開發者測試開關（debug-only）：透過 adb shell am broadcast 可切換訂閱層級
        // 目的：emulator 無 Play Services 時驗證 STANDARD 解鎖邏輯
        if (BuildConfig.DEBUG) {
            setupDeveloperTestReceiver()
        }
    }

    private fun setupDeveloperTestReceiver() {
        val filter = android.content.IntentFilter("com.predictxsports.android.SET_TIER")
        val receiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(context: android.content.Context, intent: android.content.Intent) {
                val tierName = intent.getStringExtra("tier") ?: return
                val tier = when (tierName.uppercase()) {
                    "FREE" -> MembershipTier.FREE
                    "BASIC" -> MembershipTier.BASIC
                    "STANDARD" -> MembershipTier.STANDARD
                    else -> {
                        Log.w(TAG, "Unknown tier: $tierName")
                        return
                    }
                }
                val prefs = getSharedPreferences("predictx_billing", android.content.Context.MODE_PRIVATE)
                prefs.edit().putString("membership_tier", tier.rawValue).apply()
                Log.d(TAG, "[DevTest] Tier set to $tier via broadcast. Please restart app.")
            }
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, filter, android.content.Context.RECEIVER_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(receiver, filter)
        }
    }
}