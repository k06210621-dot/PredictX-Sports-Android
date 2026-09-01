package com.predictxsports.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.predictxsports.android.ui.splash.SplashScreen
import com.predictxsports.android.ui.theme.PredictXTheme

/**
 * SplashActivity - 啟動畫面（包含 Splash video）
 *
 * 重要更新 (2026-08-26)：
 * - 加入 enableEdgeToEdge()：符合 Android 15+ (targetSdk 36) 預設行為，
 *   避免 Play Console 警告「目標版本為 SDK 35 的應用程式未妥善處理插邊」
 * - 移除舊的 window.setFlags(FLAG_LAYOUT_NO_LIMITS) 和 statusBarColor/navigationBarColor
 *   （這些 API 在 Android 15 已淘汰，Play Console 警告「已淘汰的無邊框 API」）
 * - 改用現代的 enableEdgeToEdge() 自動處理 system bars 透明化與插邊
 */
class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // P0-1: 必須在 super.onCreate 之前呼叫，確保 Activity 採用 edge-to-edge 佈局
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            PredictXTheme {
                SplashScreen(
                    modifier = Modifier.fillMaxSize(),
                    onSplashFinished = {
                        startActivity(android.content.Intent(this, MainActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}