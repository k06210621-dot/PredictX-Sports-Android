package com.predictxsports.android.ui.analytics

import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.predictxsports.android.service.BillingViewModel
import com.predictxsports.android.service.MembershipTier
import com.predictxsports.android.ui.components.AnalysisSkeletonView
import com.predictxsports.android.ui.theme.LeagueTheme
import com.predictxsports.android.ui.theme.PredictXTextSize
import com.predictxsports.android.ui.theme.SportsColors
import androidx.compose.material3.MaterialTheme
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * AnalyticsView — 對應 iOS AnalyticsView.swift
 */
@Composable
fun AnalyticsView(
    viewModel: AnalyticsViewModel = viewModel(),
    billingViewModel: BillingViewModel? = null,
    onUpgradeClick: (() -> Unit)? = null,
    onSettlementClick: ((RecentSettlement) -> Unit)? = null
) {
    // 修正：原本 isPremium 永遠是 false（呼叫端沒傳），導致 STANDARD 訂閱者也被鎖。
    // 改為直接訂閱 BillingViewModel.tier。
    // 對齊 iOS AnalyticsView.swift:44 的「負面清單」語意：
    //   只有 free 或 basic 才鎖，standard（及未來 premium）都解鎖。
    val effectiveBilling = billingViewModel ?: viewModel<BillingViewModel>()
    val tier by effectiveBilling.tier.collectAsState()
    val isLocked = tier == MembershipTier.FREE || tier == MembershipTier.BASIC

    val isLoading by viewModel.isLoading.collectAsState()
    val leagueAccuracies by viewModel.leagueAccuracies.collectAsState()
    val overallAccuracy by viewModel.overallAccuracy.collectAsState()
    val recentSettlements by viewModel.recentSettlements.collectAsState()
    val winRateTrends by viewModel.winRateTrends.collectAsState()
    val selectedLeague by viewModel.selectedLeague.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (errorMessage != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f))
                    .padding(16.dp)
            ) {
                Text(errorMessage ?: "", fontSize = PredictXTextSize.sm, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        if (isLoading && leagueAccuracies.isEmpty()) {
            AnalysisSkeletonView()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(25.dp)
            ) {
                // 1. 綜合驗證率卡片
                item {
                    OverallAccuracyCard(accuracy = overallAccuracy)
                }

                // Free/Basic 鎖定（對齊 iOS：只有 free/basic 鎖，standard/premium 解鎖）
                if (isLocked) {
                    item { LockedAnalyticsContent(onUpgradeClick = onUpgradeClick) }
                } else {
                    // 2. 最近 10 場
                    item {
                        RecentFormSection(
                            settlements = recentSettlements,
                            onSettlementClick = onSettlementClick
                        )
                    }
                    // 3. 聯賽選擇
                    item {
                        LeagueSelectionSection(
                            accuracies = leagueAccuracies,
                            onLeagueClick = { league -> viewModel.updateTrendForLeague(league) }
                        )
                    }
                    // 4. 趨勢圖（對齊 iOS TrendChartSection）
                    item {
                        TrendChartView(
                            league = selectedLeague,
                            trends = winRateTrends
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OverallAccuracyCard(accuracy: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "PredictX AI 模型綜合驗證率",
                    fontSize = PredictXTextSize.sm,
                    fontWeight = FontWeight.Bold,
                    color = SportsColors.brandPrimary
                )
                Spacer(Modifier.weight(1f))
                Text("全體聯賽加權計算", fontSize = PredictXTextSize.sm, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    "%.1f%%".format(accuracy * 100),
                    fontSize = PredictXTextSize.heroLg,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text("平均驗證率", fontSize = PredictXTextSize.base, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 6.dp, start = 8.dp))
            }
            Text("抓取數據：各聯盟最近 100 場的加權平均（上限 500 場）", fontSize = PredictXTextSize.sm, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun LockedAnalyticsContent(onUpgradeClick: (() -> Unit)?) {
    // 🆕 P0-2 優化：付費牆升級為價值傳達型設計
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(listOf(SportsColors.cardBgDeep, SportsColors.cardBgDeepAlt)),
                RoundedCornerShape(16.dp)
            )
            .border(
                BorderStroke(1.dp, SportsColors.borderSubtle),
                RoundedCornerShape(16.dp)
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 鎖頭 icon 視覺
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    Brush.linearGradient(listOf(SportsColors.premiumGradientStart, SportsColors.premiumGradientEnd)),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("🔒", fontSize = 32.sp)
        }
        Spacer(Modifier.height(14.dp))

        Text(
            "解鎖完整 AI 模型驗證",
            fontSize = PredictXTextSize.xxl,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "10 場完整紀錄 · 聯賽分佈 · 命中率趨勢圖",
            fontSize = PredictXTextSize.md,
            color = SportsColors.textLight
        )
        Spacer(Modifier.height(18.dp))

        // 方案對比卡
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("✓ Standard 月訂", color = SportsColors.successGreen, fontWeight = FontWeight.SemiBold, fontSize = PredictXTextSize.md)
                Text("NT$290", color = Color.White, fontWeight = FontWeight.Bold, fontSize = PredictXTextSize.md)
            }
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("　└ 無限 AI 分析點數", color = SportsColors.textMuted, fontSize = PredictXTextSize.sm)
                Text("包含", color = SportsColors.textLightGreen, fontSize = PredictXTextSize.sm)
            }
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("　└ 完整模型驗證報告", color = SportsColors.textMuted, fontSize = PredictXTextSize.sm)
                Text("包含", color = SportsColors.textLightGreen, fontSize = PredictXTextSize.sm)
            }
        }
        Spacer(Modifier.height(16.dp))

        // 主 CTA
        androidx.compose.material3.Button(
            onClick = { onUpgradeClick?.invoke() },
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = SportsColors.actionBlue
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                "立即升級 · NT$290/月",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = PredictXTextSize.lg
            )
        }

        // 安心承諾（已移除：免費試用相關承諾未在正式設定中）
    }
}

@Composable
private fun RecentFormSection(
    settlements: List<RecentSettlement>,
    onSettlementClick: ((RecentSettlement) -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("AI 模型 10 場驗證紀錄", fontWeight = FontWeight.Bold, fontSize = PredictXTextSize.lg, color = MaterialTheme.colorScheme.onSurface)

            if (settlements.isEmpty()) {
                Text("正在從已結算賽事載入驗證紀錄...", fontSize = PredictXTextSize.sm, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 12.dp))
            } else {
                // W/L 圓角方塊（可點擊 → 開啟 SettlementDetailView）
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    items(settlements) { item ->
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (item.isHit) SportsColors.successGreen else SportsColors.dangerRed)
                                .then(
                                    if (onSettlementClick != null) Modifier.clickable { onSettlementClick(item) } else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(if (item.isHit) "O" else "X", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface, fontSize = PredictXTextSize.md)
                        }
                    }
                }

                // 最近 3 場明細
                settlements.take(3).forEach { item ->
                    FormDetailRow(item = item)
                }
            }
        }
    }
}

@Composable
private fun FormDetailRow(item: RecentSettlement) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            if (item.isHit) "✓" else "✕",
            color = if (item.isHit) SportsColors.successGreen else SportsColors.dangerRed,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.width(10.dp))
        Column {
            Text("${item.homeTeam} vs ${item.awayTeam}", fontSize = PredictXTextSize.sm, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            val df = SimpleDateFormat("MM/dd", Locale.TAIWAN)
            Text("${item.league} · ${df.format(java.util.Date(item.matchDate))}", fontSize = PredictXTextSize.sm, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.End) {
            if (item.homeScore != null && item.awayScore != null) {
                Text("${item.homeScore}-${item.awayScore}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
            if (!item.predictedScore.isNullOrEmpty()) {
                Text("模型推演 ${item.predictedScore}", fontSize = PredictXTextSize.sm, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun LeagueSelectionSection(
    accuracies: List<LeagueAccuracy>,
    onLeagueClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("點選聯賽查看模型趨勢分析", fontWeight = FontWeight.Bold, fontSize = PredictXTextSize.lg, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(12.dp))
            accuracies.forEach { accuracy ->
                val leagueType = com.predictxsports.android.data.model.LeagueType.fromRaw(accuracy.league)
                val themeColor = if (leagueType != null) LeagueTheme.color(leagueType) else Color.Gray
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLeagueClick(accuracy.league) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(accuracy.league, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("已分析 ${accuracy.totalAnalyzed} 場", fontSize = PredictXTextSize.sm, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.weight(1f))
                    Text("%.1f%%".format(accuracy.hitRate * 100), fontWeight = FontWeight.Bold, color = themeColor)
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}