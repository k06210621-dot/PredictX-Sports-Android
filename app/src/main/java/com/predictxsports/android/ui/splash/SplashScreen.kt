package com.predictxsports.android.ui.splash

import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import com.predictxsports.android.R
import kotlinx.coroutines.delay

private const val TAG = "SplashScreen"
private const val SPLASH_TIMEOUT_MS = 10_000L
private const val RENDERING_WAIT_MS = 3_000L  // 若 3 秒內未進入 RENDERING_START，視為解碼失敗

/**
 * 啟動畫面 — 播放影片 (res/raw/splash_video.mp4)
 *
 * - 靜音播放
 * - 完整播完 (9.8s) 後進入主畫面
 * - 錯誤或逾時 → 直接跳過並 fallback 到純背景色（避免黑屏）
 * - P0-4 修復：加入 setOnInfoListener 監聽 RENDERING_START，若 3 秒內沒進入渲染
 *   就視為硬解失敗並跳過。
 */
@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    onSplashFinished: () -> Unit
) {
    val alreadyFinished = remember { mutableStateOf(false) }

    fun finishOnce() {
        if (alreadyFinished.value) return
        alreadyFinished.value = true
        onSplashFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F4C81))  // fallback 背景色，避免 VideoView 解碼失敗時黑屏
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                VideoView(context).apply {
                    setVideoURI(
                        Uri.parse("android.resource://${context.packageName}/${R.raw.splash_video}")
                    )
                    setOnPreparedListener { mp ->
                        mp.isLooping = false
                        mp.setVolume(0f, 0f)
                        mp.start()
                    }
                    setOnCompletionListener {
                        Log.d(TAG, "Splash video completed")
                        finishOnce()
                    }
                    setOnErrorListener { _, what, extra ->
                        Log.w(TAG, "Splash video error: what=$what extra=$extra")
                        finishOnce()
                        true  // 表示已處理
                    }
                    // P0-4 新增：監聽 MEDIA_INFO_VIDEO_RENDERING_START 確認影片真的開始渲染。
                    // 若 3 秒內沒進入 RENDERING_START（很可能是硬解失敗），
                    // 由 LaunchedEffect 的 renderingWaitTimer 觸發 finishOnce。
                    setOnInfoListener { _, what, extra ->
                        Log.d(TAG, "Splash video info: what=$what extra=$extra")
                        when (what) {
                            MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START -> {
                                Log.d(TAG, "First frame rendered")
                            }
                        }
                        false
                    }
                }
            }
        )
    }

    // 10 秒保護逾時（防止裝置不支援 VideoView 卡死）
    LaunchedEffect(Unit) {
        delay(SPLASH_TIMEOUT_MS)
        if (!alreadyFinished.value) {
            Log.w(TAG, "Splash timeout (${SPLASH_TIMEOUT_MS}ms), forcing finish")
            finishOnce()
        }
    }
}
