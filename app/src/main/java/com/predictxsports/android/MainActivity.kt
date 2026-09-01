package com.predictxsports.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.predictxsports.android.service.BillingViewModel
import com.predictxsports.android.ui.navigation.MainTabView
import com.predictxsports.android.ui.splash.SplashScreen
import com.predictxsports.android.ui.theme.PredictXTheme
import com.predictxsports.android.ui.theme.ThemeController

/**
 * MainActivity - App 入口
 *
 * 重要更新 (2026-08-26)：
 * - 加入 enableEdgeToEdge()：符合 Android 15+ (targetSdk 36) 預設行為
 *   避免 Play Console 警告「目標版本為 SDK 35 的應用程式未妥善處理插邊」
 *   和「應用程式使用已淘汰的無邊框 API 或參數」
 * - enableEdgeToEdge() 自動將 status bar / navigation bar 設為透明，
 *   並透過 Compose 的 WindowInsets.safeDrawing 等 modifier 處理插邊
 */
class MainActivity : ComponentActivity() {
    // P0-2 修復：使用 by viewModels() 由 Activity ViewModelStore 管理生命週期，
    // 確保旋轉/重建時 BillingViewModel 狀態（點數/訂閱/Favorites）不丟失。
    private val billingViewModel: BillingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // P0-3: 必須在 super.onCreate 之前呼叫，確保 Activity 採用 edge-to-edge 佈局
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Initialize ThemeController (persists dark/light preference)
        ThemeController.init(applicationContext)

        // Initialize BillingViewModel early (before SplashScreen before any UI reads prefs)
        billingViewModel.init(applicationContext)

        // Initialize Retrofit HTTP cache (10 MB, app-private cacheDir)
        com.predictxsports.android.data.remote.RetrofitClient.init(applicationContext)

        setContent {
            PredictXTheme(darkTheme = ThemeController.isDark) {
                var showSplash by remember { mutableStateOf(true) }

                Crossfade(
                    targetState = showSplash,
                    animationSpec = tween(400)
                ) { isSplash ->
                    if (isSplash) {
                        SplashScreen(
                            onSplashFinished = { showSplash = false }
                        )
                    } else {
                        MainTabView(billingViewModel = billingViewModel)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // App 從背景回前景時重新檢查每日點數是否需補滿
        billingViewModel.onAppResume()
    }
}
