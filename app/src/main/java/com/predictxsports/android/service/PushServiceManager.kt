package com.predictxsports.android.service

/**
 * PushServiceManager — 已停用（Android 不使用推送功能）
 * 
 * 原本負責 FCM token 註冊與推播偏好設定，
 * 現改為空實作避免編譯錯誤。调用方需自行移除相關引用。
 */
object PushServiceManager {
    fun init(context: android.content.Context) {
        // no-op
    }
    
    fun onNewToken(token: String, tier: String) {
        // no-op
    }
    
    fun setPushEnabled(enabled: Boolean, tier: String) {
        // no-op
    }
    
    fun onSubscriptionChanged(tier: String) {
        // no-op
    }
}
