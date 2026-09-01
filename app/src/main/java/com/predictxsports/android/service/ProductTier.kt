package com.predictxsports.android.service

import androidx.compose.ui.graphics.Color

/**
 * ProductTier — 對應 iOS SubscribeView.swift ProductTier enum
 *
 * 使用 3-tier 規格：
 * - Free：每天上限 60 分析點數
 * - Basic：NT$100/月，每天 120 點且可無上限累計
 * - Standard：NT$290/月，無限點數 + 模型驗證率儀表板 + 收藏賽事分析
 */
enum class ProductTier {
    FREE,
    BASIC,
    STANDARD;

    val displayName: String
        get() = when (this) {
            FREE -> "Free"
            BASIC -> "一般"
            STANDARD -> "高級"
        }

    val tagline: String
        get() = when (this) {
            FREE -> "每日上限 60 分析點數"
            BASIC -> "每天 120 點，可無上限累計"
            STANDARD -> "無限點數 + 驗證率儀表板 + 收藏賽事分析"
        }

    val tint: Color
        get() = when (this) {
            FREE -> Color.Gray
            BASIC -> Color(0xFF1FBF73)
            STANDARD -> Color(0xFFD8923B)
        }

    val benefits: List<String>
        get() = when (this) {
            FREE -> listOf(
                "每天 60 分析點數上限",
                "期滿後點數歸零",
                "基礎賽事資訊"
            )
            BASIC -> listOf(
                "每天 120 分析點數",
                "點數可無上限累計",
                "基礎賽事分析"
            )
            STANDARD -> listOf(
                "無限 AI 分析點數",
                "模型驗證率儀表板",
                "收藏賽事分析"
            )
        }

    val monthlyPriceTWD: Int
        get() = when (this) {
            FREE -> 0
            BASIC -> 100
            STANDARD -> 290
        }

    val yearlyPriceTWD: Int
        get() = when (this) {
            FREE -> 0
            BASIC -> 990
            STANDARD -> 2990
        }

    fun currentPriceTWD(isAnnual: Boolean): Int = if (isAnnual) yearlyPriceTWD else monthlyPriceTWD

    fun productID(isAnnual: Boolean): String {
        val suffix = if (isAnnual) "yearly" else "monthly"
        val raw = when (this) {
            FREE -> "free"
            BASIC -> "basic"
            STANDARD -> "standard"
        }
        return "predictx_${raw}_$suffix"
    }

    companion object {
        val paidCases = listOf(BASIC, STANDARD)
    }
}