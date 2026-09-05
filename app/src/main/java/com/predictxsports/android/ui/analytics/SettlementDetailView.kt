package com.predictxsports.android.ui.analytics

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.predictxsports.android.data.model.AIAnalysisModel
import com.predictxsports.android.data.remote.RetrofitClient
import com.predictxsports.android.ui.components.AnalysisSkeletonView
import com.predictxsports.android.ui.components.RadarChartView
import com.predictxsports.android.ui.theme.PredictXTextSize
import com.predictxsports.android.ui.theme.SportsColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * SettlementDetailView — 對應 iOS SettlementDetailSheet.swift
 *
 * 從「AI 模型 10 場驗證紀錄」的 O/X 方塊點擊後開啟，
 * 顯示該場比賽的實際比分 + AI 分析卡片詳細資訊。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettlementDetailView(
    settlement: RecentSettlement,
    onBack: () -> Unit
) {
    var analysis by remember { mutableStateOf<AIAnalysisModel?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    androidx.activity.compose.BackHandler(enabled = !isLoading) {
        onBack()
    }

    fun loadAnalysis() {
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                val result = withContext(Dispatchers.IO) {
                    RetrofitClient.api.fetchAIAnalysis(settlement.id)
                }
                analysis = result
            } catch (e: Exception) {
                errorMessage = e.message ?: "載入失敗"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(settlement.id) {
        loadAnalysis()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("賽事驗證詳情") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            when {
                isLoading -> AnalysisSkeletonView()
                errorMessage != null -> SettlementErrorView(
                    message = errorMessage ?: "",
                    onRetry = { loadAnalysis() }
                )
                else -> SettlementContent(settlement = settlement, analysis = analysis)
            }
        }
    }
}

@Composable
private fun SettlementErrorView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = null,
            tint = SportsColors.warningOrange,
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text("無法載入 AI 分析", fontWeight = FontWeight.SemiBold)
        Text(message, fontSize = PredictXTextSize.md, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(16.dp))
        androidx.compose.material3.Button(onClick = onRetry) {
            Text("重試")
        }
    }
}

@Composable
private fun SettlementContent(settlement: RecentSettlement, analysis: AIAnalysisModel?) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. 賽事結果標題卡
        item {
            HeaderCard(settlement = settlement)
        }

        // 2. 比分對照
        item {
            ScoreComparisonCard(settlement = settlement)
        }

        // 3. AI 分析摘要
        analysis?.analysis?.summary?.takeIf { it.isNotEmpty() }?.let { summary ->
            item {
                SummaryCard(summary = summary)
            }
        }

        // 4. 機率與信心
        analysis?.prediction?.let { prediction ->
            item {
                ConfidenceCard(settlement = settlement, prediction = prediction)
            }
        }

        // 5. 關鍵因素
        analysis?.analysis?.keyFactors?.takeIf { it.isNotEmpty() }?.let { factors ->
            item {
                FactorsCard(title = "關鍵因素", factors = factors, isRisk = false)
            }
        }

        // 6. 風險因素
        analysis?.analysis?.riskFactors?.takeIf { it.isNotEmpty() }?.let { risks ->
            item {
                FactorsCard(title = "風險因素", factors = risks, isRisk = true)
            }
        }

        // 7. 雷達圖
        analysis?.radarChart?.let { radar ->
            if (radar.categories != null && radar.homeTeam != null && radar.awayTeam != null) {
                val categories = radar.categories.orEmpty()
                val homeValues = radar.homeTeam.orEmpty()
                val awayValues = radar.awayTeam.orEmpty()
                if (categories.isNotEmpty() && homeValues.isNotEmpty() && awayValues.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("六維能力雷達圖", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                Spacer(Modifier.height(12.dp))
                                RadarChartView(
                                    categories = categories,
                                    homeValues = homeValues,
                                    awayValues = awayValues,
                                    homeTeamName = settlement.homeTeam,
                                    awayTeamName = settlement.awayTeam
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderCard(settlement: RecentSettlement) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(leagueIcon(settlement.league), fontSize = PredictXTextSize.hero)
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        "${settlement.awayTeam} @ ${settlement.homeTeam}",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "${settlement.league} · ${formatDate(settlement.matchDate)}",
                        fontSize = PredictXTextSize.sm,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (settlement.isHit) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                    contentDescription = null,
                    tint = if (settlement.isHit) SportsColors.successGreen else SportsColors.dangerRed,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    if (settlement.isHit) "AI 推論正確" else "AI 推論錯誤",
                    fontWeight = FontWeight.Bold,
                    color = if (settlement.isHit) SportsColors.successGreen else SportsColors.dangerRed
                )
            }
        }
    }
}

@Composable
private fun ScoreComparisonCard(settlement: RecentSettlement) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("比分對照", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(modifier = Modifier.fillMaxWidth()) {
                ScoreColumn(
                    title = "模型推演比分",
                    score = settlement.predictedScore ?: "—",
                    tint = SportsColors.brandPrimary,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(40.dp)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                )
                ScoreColumn(
                    title = "實際比分",
                    score = formatActualScore(settlement),
                    tint = if (settlement.isHit) SportsColors.successGreen else SportsColors.dangerRed,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ScoreColumn(title: String, score: String, tint: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(title, fontSize = PredictXTextSize.sm, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(score, fontSize = PredictXTextSize.hero, fontWeight = FontWeight.Black, color = tint)
    }
}

@Composable
private fun SummaryCard(summary: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("AI 分析摘要", fontWeight = FontWeight.SemiBold, color = SportsColors.brandPrimary)
            Text(summary, fontSize = PredictXTextSize.md, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun ConfidenceCard(settlement: RecentSettlement, prediction: AIAnalysisModel.Prediction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("AI 推論機率與信心", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)

            val homeProb = prediction.homeWinProbability ?: 0.0
            val awayProb = prediction.awayWinProbability ?: 0.0

            // 機率條
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(settlement.homeTeam, fontSize = PredictXTextSize.sm, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.weight(1f))
                Text("${(homeProb * 100).toInt()}%", fontWeight = FontWeight.Bold, color = SportsColors.brandPrimary)
                Text(" vs ", fontSize = PredictXTextSize.sm, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${(awayProb * 100).toInt()}%", fontWeight = FontWeight.Bold, color = SportsColors.dangerRed)
                Spacer(Modifier.weight(1f))
                Text(settlement.awayTeam, fontSize = PredictXTextSize.sm, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }

            // 雙向機率條
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(SportsColors.dangerRed, CircleShape)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(homeProb.toFloat().coerceIn(0f, 1f))
                        .height(8.dp)
                        .background(SportsColors.brandPrimary, CircleShape)
                )
            }

            // 信心指數
            prediction.confidence?.let { confidence ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("信心指數：", fontSize = PredictXTextSize.sm, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "${confidence.toInt()} / 10",
                        fontWeight = FontWeight.Bold,
                        color = confidenceColor(confidence)
                    )
                }
            }
        }
    }
}

@Composable
private fun FactorsCard(title: String, factors: List<String>, isRisk: Boolean) {
    val tint = if (isRisk) SportsColors.warningOrange else SportsColors.successGreen
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold, color = tint)
            factors.forEach { factor ->
                Row(verticalAlignment = Alignment.Top) {
                    Text("• ", color = tint, fontWeight = FontWeight.Bold)
                    Text(factor, fontSize = PredictXTextSize.md, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

// ── 工具函式 ──

private fun formatActualScore(settlement: RecentSettlement): String {
    val h = settlement.homeScore
    val a = settlement.awayScore
    if (h == null || a == null) return "—"
    return "$h - $a"
}

private fun formatDate(matchDate: Long): String {
    val df = SimpleDateFormat("yyyy/MM/dd", Locale.TAIWAN)
    return df.format(java.util.Date(matchDate))
}

private fun confidenceColor(value: Double): Color = when {
    value < 4.0 -> SportsColors.dangerRed
    value < 6.0 -> SportsColors.warningOrange
    value < 8.0 -> SportsColors.brandSecondary
    else -> SportsColors.successGreen
}

private fun leagueIcon(league: String): String = when (league) {
    "MLB" -> "⚾"
    "NBA" -> "🏀"
    "WNBA" -> "🏀"
    "NPB" -> "🇯🇵"
    "CPBL" -> "🇹🇼"
    else -> "🏟"
}
