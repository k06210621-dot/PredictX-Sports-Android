package com.predictxsports.android.ui.home

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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.rememberCoroutineScope
import com.predictxsports.android.data.model.AIAnalysisModel
import com.predictxsports.android.data.model.Match
import com.predictxsports.android.data.remote.RetrofitClient
import com.predictxsports.android.ui.components.AnalysisSkeletonView
import com.predictxsports.android.ui.components.RadarChartView
import androidx.compose.material3.MaterialTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.predictxsports.android.ui.theme.PredictXTextSize

/**
 * AIAnalysisDetailView — 對應 iOS AIAnalysisDetailView.swift
 *
 * 結構：
 * 1. 勝率推論卡片（home/away win prob + confidence + score）
 * 2. 能力維度分析（RadarChartView）
 * 3. 深度分析文字（summary + key_factors + risk_factors）
 * 4. 合規免責聲明
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIAnalysisDetailView(
    match: Match,
    onBack: () -> Unit,
    isFavorited: Boolean = false,
    onToggleFavorite: (() -> Unit)? = null
) {
    var analysis by remember { mutableStateOf<AIAnalysisModel?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // 處理 system back button，與 TopAppBar 的返回按鈕一致
    androidx.activity.compose.BackHandler(enabled = !isLoading) {
        onBack()
    }

    fun loadAnalysis() {
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                val result = withContext(Dispatchers.IO) {
                    RetrofitClient.api.fetchAIAnalysis(match.id)
                }
                analysis = result
            } catch (e: Exception) {
                errorMessage = e.message ?: "載入失敗"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(match.id) {
        loadAnalysis()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI 分析詳情") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (onToggleFavorite != null) {
                        IconButton(onClick = onToggleFavorite) {
                            Icon(
                                imageVector = if (isFavorited) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = "收藏",
                                tint = if (isFavorited) Color(0xFFD4A843) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(padding)
        ) {
            when {
                isLoading -> AnalysisSkeletonView()
                errorMessage != null -> ErrorRetryView(
                    message = errorMessage ?: "",
                    onRetry = { loadAnalysis() }
                )
                analysis != null -> analysis?.let { AnalysisContent(match = match, analysis = it) }
                else -> EmptyAnalysisView()
            }
        }
    }
}

@Composable
private fun ErrorRetryView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Info,
            contentDescription = null,
            tint = Color(0xFFE8923B),
            modifier = Modifier
                .size(48.dp)
                .padding(bottom = 16.dp)
        )
        Text(text = "分析載入失敗", fontWeight = FontWeight.SemiBold)
        Text(text = message, fontSize = PredictXTextSize.xl, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(16.dp))
        androidx.compose.material3.Button(onClick = onRetry) {
            Text("重試")
        }
    }
}

@Composable
private fun EmptyAnalysisView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Info,
            contentDescription = null,
            tint = Color(0xFFE8923B),
            modifier = Modifier
                .size(48.dp)
                .padding(bottom = 16.dp)
        )
        Text(text = "暫無分析資料", fontWeight = FontWeight.SemiBold)
        Text(text = "此賽事的 AI 分析尚未生成，或資料無效。", fontSize = PredictXTextSize.xl, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun AnalysisContent(match: Match, analysis: AIAnalysisModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. 勝率推論卡片
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        BorderStroke(1.5.dp, Color(0xFF0F4C81).copy(alpha = 0.45f)),
                        RoundedCornerShape(16.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    Text("AI 勝率推論", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    val homeProb = analysis.prediction?.homeWinProbability ?: 0.0
                    val awayProb = analysis.prediction?.awayWinProbability ?: 0.0
                    val confidence = analysis.prediction?.confidence ?: 0.0
                    val score = analysis.prediction?.predictedScore ?: "N/A"

                    // 雙向勝率條
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "%.0f%%".format(homeProb * 100),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F4C81)
                            )
                            Spacer(Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .background(Color(0xFF0F4C81).copy(alpha = 0.25f), CircleShape)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth((homeProb / (homeProb + awayProb)).toFloat().coerceIn(0f, 1f))
                                        .height(8.dp)
                                        .background(Color(0xFF0F4C81), CircleShape)
                                )
                            }
                        }
                        Text(" vs ", fontSize = PredictXTextSize.xl, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 8.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                "%.0f%%".format(awayProb * 100),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD93B3B)
                            )
                            Spacer(Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .background(Color(0xFFD93B3B).copy(alpha = 0.25f), CircleShape)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth((awayProb / (homeProb + awayProb)).toFloat().coerceIn(0f, 1f))
                                        .height(8.dp)
                                        .background(Color(0xFFD93B3B), CircleShape)
                                )
                            }
                        }
                    }

                    // 主客隊標籤
                    Row {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(8.dp).background(Color(0xFF0F4C81), CircleShape))
                            Spacer(Modifier.width(4.dp))
                            Text(match.homeTeam, fontSize = PredictXTextSize.xl, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(Modifier.weight(1f))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(8.dp).background(Color(0xFFD93B3B), CircleShape))
                            Spacer(Modifier.width(4.dp))
                            Text(match.awayTeam, fontSize = PredictXTextSize.xl, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // confidence + score
                    Row {
                        Text("信心度：%.1f".format(confidence), fontSize = PredictXTextSize.xl, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.weight(1f))
                        Text("推演比分：$score", fontSize = PredictXTextSize.xl, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    // 免責提示
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Filled.Info, contentDescription = null, tint = Color(0xFFE8923B), modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "AI 分析僅供參考，請理性判斷。",
                            fontSize = PredictXTextSize.xl,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 2. 雷達圖
        analysis.radarChart?.let { radar ->
            if (radar.categories != null && radar.homeTeam != null && radar.awayTeam != null) {
                item {
                    val categories = radar.categories.orEmpty()
                    val homeValues = radar.homeTeam.orEmpty()
                    val awayValues = radar.awayTeam.orEmpty()
                    if (categories.isNotEmpty() && homeValues.isNotEmpty() && awayValues.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    BorderStroke(1.5.dp, Color(0xFF0F4C81).copy(alpha = 0.45f)),
                                    RoundedCornerShape(16.dp)
                                ),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("能力維度分析", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                Spacer(Modifier.height(12.dp))
                                RadarChartView(
                                    categories = categories,
                                    homeValues = homeValues,
                                    awayValues = awayValues,
                                    homeTeamName = match.homeTeam,
                                    awayTeamName = match.awayTeam
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. 深度分析
        analysis.analysis?.let { content ->
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            BorderStroke(1.5.dp, Color(0xFF0F4C81).copy(alpha = 0.45f)),
                            RoundedCornerShape(16.dp)
                        ),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("深度分析", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.height(12.dp))
                        Text(content.summary ?: "（暫無分析內容）", fontSize = PredictXTextSize.xxl, color = MaterialTheme.colorScheme.onSurface, lineHeight = 20.sp)

                        content.keyFactors?.takeIf { it.isNotEmpty() }?.let { factors ->
                            Text("關鍵因素", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(top = 12.dp))
                            factors.forEach { factor ->
                                Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(vertical = 2.dp)) {
                                    Text("✓ ", color = Color(0xFF0F4C81))
                                    Text(factor, fontSize = PredictXTextSize.xl, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }

                        content.riskFactors?.takeIf { it.isNotEmpty() }?.let { risks ->
                            Text("風險因素", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(top = 8.dp))
                            risks.forEach { risk ->
                                Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(vertical = 2.dp)) {
                                    Icon(
                                        imageVector = Icons.Filled.Warning,
                                        contentDescription = null,
                                        tint = Color(0xFFE8923B),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(risk, fontSize = PredictXTextSize.xl, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. 合規免責
        item {
            Text(
                "本分析由 AI 模型自動生成，不構成任何投注建議。賽事結果具有不確定性，請自行承擔風險。",
                fontSize = PredictXTextSize.xl,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 20.dp, horizontal = 16.dp)
            )
        }
    }
}