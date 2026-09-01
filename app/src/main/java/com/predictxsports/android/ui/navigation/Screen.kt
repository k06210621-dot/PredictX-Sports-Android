package com.predictxsports.android.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    object Home : Screen("home", "智能分析", Icons.Filled.Memory)
    object Analytics : Screen("analytics", "AI 模型驗證", Icons.Filled.BarChart)
    object History : Screen("history", "歷史賽事", Icons.Filled.History)
    object Profile : Screen("profile", "個人資訊", Icons.Filled.Person)
    object Subscribe : Screen("subscribe", "訂閱中心", Icons.Filled.Memory)
    object AiInfo : Screen("ai_info", "AI 使用額度說明", Icons.Filled.Memory)
    object Legal : Screen("legal", "法律聲明", Icons.Filled.Memory)
    object Help : Screen("help", "客服中心", Icons.Filled.Memory)
    object AIAnalysisDetail : Screen("ai_detail/{matchId}", "AI 分析詳情", Icons.Filled.Memory) {
        fun routeWithId(matchId: String) = "ai_detail/$matchId"
    }
    object SettlementDetail : Screen("settlement_detail/{gameId}", "賽事驗證詳情", Icons.Filled.Memory) {
        fun routeWithId(gameId: String) = "settlement_detail/$gameId"
    }
}