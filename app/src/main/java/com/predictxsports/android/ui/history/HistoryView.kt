package com.predictxsports.android.ui.history

import androidx.compose.foundation.background

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Search

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField

import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus

import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.predictxsports.android.data.model.LeagueType
import com.predictxsports.android.data.model.Match
import com.predictxsports.android.data.model.MatchStatus
import com.predictxsports.android.ui.theme.LeagueTheme
import com.predictxsports.android.ui.home.HomeViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.predictxsports.android.ui.theme.PredictXTextSize
import com.predictxsports.android.ui.theme.SportsColors
/**
 * HistoryView — Modern sports history UI
 *
 * Features:
 * - Dark sports background with glassmorphism cards
 * - League tabs with active league accent
 * - Search bar with glass effect
 * - Date filter chips
 * - Match cards with layout: date | league | teams | score | AI badge
 */
@Composable
fun HistoryView(
    viewModel: HomeViewModel = viewModel()
) {
    var selectedLeague by remember { mutableStateOf(LeagueType.MLB) }
    var searchText by remember { mutableStateOf("") }
    var dateRangeDays by remember { mutableStateOf(30) }

    val historicalMatches by viewModel.historicalMatches.collectAsState()
    val isHistoryLoading by viewModel.isHistoryLoading.collectAsState()
    val historyError by viewModel.historyError.collectAsState()

    // 進入頁面時自動觸發載入
    LaunchedEffect(Unit) {
        viewModel.loadHistoryForAllLeagues()
    }

    val disabledTextToolbar = remember {
        object : TextToolbar {
            override val status: TextToolbarStatus = TextToolbarStatus.Hidden
            override fun showMenu(
                rect: androidx.compose.ui.geometry.Rect,
                onCopyRequested: (() -> Unit)?,
                onPasteRequested: (() -> Unit)?,
                onCutRequested: (() -> Unit)?,
                onSelectAllRequested: (() -> Unit)?
            ) {
                // No-op: 禁用浮動文字選單與 Gboard Floating Toolbar
            }
            override fun hide() { /* no-op */ }
        }
    }

    CompositionLocalProvider(LocalTextToolbar provides disabledTextToolbar) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.surfaceVariant)
                    )
                )
        ) {
        HistoryFilterBar(
            selectedLeague = selectedLeague,
            onLeagueSelect = { selectedLeague = it },
            searchText = searchText,
            onSearchChange = { searchText = it },
            dateRangeDays = dateRangeDays,
            onDateRangeChange = { dateRangeDays = it }
        )

        val allMatches = historicalMatches[selectedLeague] ?: emptyList()
        val matches = filteredMatches(allMatches, searchText, dateRangeDays)

        when {
            historyError != null && allMatches.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            historyError ?: "載入失敗",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = PredictXTextSize.md
                        )
                        androidx.compose.material3.TextButton(onClick = { viewModel.loadHistoryForAllLeagues() }) {
                            Text("重新載入", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
            isHistoryLoading && allMatches.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            strokeWidth = 3.dp
                        )
                        Text("載入歷史賽事中...", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = PredictXTextSize.md)
                    }
                }
            }
            allMatches.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("暫無歷史賽事", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = PredictXTextSize.md)
                        androidx.compose.material3.TextButton(onClick = { viewModel.loadHistoryForAllLeagues() }) {
                            Text("重新整理", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
            matches.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("沒有符合條件的賽事", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = PredictXTextSize.md)
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(matches, key = { _, match -> match.id }) { index, match ->
                        HistoricalMatchCardView(match = match, index = index)
                    }
                }
            }
        }
        }
    }
}

@Composable
private fun HistoryFilterBar(
    selectedLeague: LeagueType,
    onLeagueSelect: (LeagueType) -> Unit,
    searchText: String,
    onSearchChange: (String) -> Unit,
    dateRangeDays: Int,
    onDateRangeChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
                        MaterialTheme.colorScheme.background.copy(alpha = 0.9f)
                    )
                )
            )
            .padding(vertical = 10.dp)
    ) {
        val leagues = LeagueType.activeCases
        Spacer(modifier = Modifier.height(12.dp))

        // 五大聯盟 chips — 比照智能分析 LeagueChips 設計
        val chipListState = androidx.compose.foundation.lazy.rememberLazyListState()
        val chipScope = androidx.compose.runtime.rememberCoroutineScope()
        var chipUserInteracting by remember { mutableStateOf(false) }
        val chipCount = leagues.size

        LaunchedEffect(chipListState) {
            while (true) {
                if (!chipUserInteracting) {
                    val lastVisible = chipListState.layoutInfo.visibleItemsInfo.lastOrNull()
                    val totalSize = chipListState.layoutInfo.totalItemsCount
                    if (lastVisible != null && lastVisible.index < totalSize - 1) {
                        chipListState.animateScrollToItem(
                            index = (lastVisible.index + 1).coerceAtMost(totalSize - 1),
                            scrollOffset = 0
                        )
                    } else {
                        chipListState.animateScrollToItem(0)
                    }
                }
                kotlinx.coroutines.delay(1800)
            }
        }

        LazyRow(
            state = chipListState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(leagues) { league ->
                val isSelected = league == selectedLeague
                val accent = LeagueTheme.color(league)
                Box(
                    modifier = Modifier
                        .background(
                            color = if (isSelected) accent else accent.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(50)
                        )
                        .clickable {
                            chipUserInteracting = true
                            chipScope.launch {
                                chipListState.animateScrollToItem(index = leagues.indexOf(league))
                                kotlinx.coroutines.delay(2500)
                                chipUserInteracting = false
                            }
                            onLeagueSelect(league)
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = league.displayName,
                        fontSize = PredictXTextSize.base,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else accent
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = searchText,
            onValueChange = onSearchChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(14.dp))
                .shadow(4.dp, RoundedCornerShape(14.dp), spotColor = Color.Black.copy(alpha = 0.25f))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), RoundedCornerShape(14.dp)),
            placeholder = {
                Text(
                    text = "搜尋球隊 (中/英文)",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = PredictXTextSize.md
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Search,
                autoCorrectEnabled = false
            ),
            keyboardActions = KeyboardActions(
                onSearch = { /* 即時過濾已生效，Enter 不需額外動作 */ }
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                cursorColor = LeagueTheme.color(selectedLeague)
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf(7 to "近 7 天", 30 to "近 30 天", 0 to "全部").forEach { (days, label) ->
                val isSelected = dateRangeDays == days
                val accent = LeagueTheme.color(selectedLeague)
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            brush = if (isSelected) {
                                Brush.linearGradient(listOf(accent, accent.copy(alpha = 0.75f)))
                            } else {
                                Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        MaterialTheme.colorScheme.surface
                                    )
                                )
                            }
                        )
                        .clickable { onDateRangeChange(days) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = PredictXTextSize.sm,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun HistoricalMatchCardView(match: Match, index: Int = 0) {
    val themeColor = LeagueTheme.color(match.league)
    val df = SimpleDateFormat("MM/dd (EEE)", Locale.TAIWAN)

    val hasFinalScore = match.homeScore != null && match.awayScore != null
    val homeWin = hasFinalScore && (match.homeScore ?: 0) > (match.awayScore ?: 0)
    val awayWin = hasFinalScore && (match.awayScore ?: 0) > (match.homeScore ?: 0)
    val winColor = SportsColors.successGreen
    val lossColor = SportsColors.dangerRed

    val gradient = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surfaceVariant
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        themeColor.copy(alpha = 0.6f),
                        themeColor.copy(alpha = 0.2f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(1.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = df.format(java.util.Date(match.startTime)),
                    fontSize = PredictXTextSize.base,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(themeColor.copy(alpha = 0.18f))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = leagueLabel(match.league),
                        fontSize = PredictXTextSize.sm,
                        fontWeight = FontWeight.SemiBold,
                        color = themeColor
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = match.homeTeam,
                        fontSize = PredictXTextSize.xl,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            homeWin -> winColor
                            awayWin && hasFinalScore -> lossColor
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                    if (match.homeTeamCN.isNotBlank()) {
                        Text(
                            text = match.homeTeamCN,
                            fontSize = PredictXTextSize.base,
                            color = when {
                                homeWin -> winColor.copy(alpha = 0.85f)
                                awayWin && hasFinalScore -> lossColor.copy(alpha = 0.85f)
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (match.homeScore != null && match.awayScore != null) {
                        Text(
                            text = "${match.homeScore} - ${match.awayScore}",
                            fontSize = PredictXTextSize.display,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "最終比分",
                            fontSize = PredictXTextSize.sm,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Text(
                            text = "VS",
                            fontSize = PredictXTextSize.xxl,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = statusDescription(match),
                            fontSize = PredictXTextSize.sm,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                    Text(
                        text = match.awayTeam,
                        fontSize = PredictXTextSize.xl,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            awayWin -> winColor
                            homeWin -> lossColor
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                        textAlign = TextAlign.End
                    )
                    if (match.awayTeamCN.isNotBlank()) {
                        Text(
                            text = match.awayTeamCN,
                            fontSize = PredictXTextSize.base,
                            color = when {
                                awayWin -> winColor.copy(alpha = 0.85f)
                                homeWin -> lossColor.copy(alpha = 0.85f)
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            textAlign = TextAlign.End
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                val hasFinalScore = match.homeScore != null && match.awayScore != null
                // 已結算 → 顯示 準確 / 未中；未結算（無比分）→ 顯示 等待 AI 結算
                if (hasFinalScore) {
                    AiBadge(isHit = match.aiIsHit)
                } else {
                    AiBadge(isHit = null)
                }
            }
        }
    }
}

@Composable
private fun AiBadge(isHit: Boolean?) {
    when (isHit) {
        true -> {
            val brush = Brush.linearGradient(listOf(SportsColors.gradientWinStart, SportsColors.gradientWinEnd))
            AiPill(
                text = "AI 分析準確",
                icon = Icons.Filled.CheckCircle,
                brush = brush,
                iconTint = Color.White,
                textColor = Color.White
            )
        }
        false -> {
            val brush = Brush.linearGradient(listOf(SportsColors.gradientLossStart, SportsColors.gradientLossEnd))
            AiPill(
                text = "AI 分析未中",
                icon = Icons.Filled.Close,
                brush = brush,
                iconTint = Color.White,
                textColor = Color.White
            )
        }
        null -> {
            val brush = Brush.linearGradient(listOf(SportsColors.warningOrange, SportsColors.brandSecondary))
            AiPill(
                text = "等待 AI 結算",
                icon = Icons.Filled.HourglassEmpty,
                brush = brush,
                iconTint = Color.White,
                textColor = Color.White
            )
        }
    }
}

@Composable
private fun AiPill(
    text: String,
    icon: ImageVector? = null,
    brush: Brush,
    iconTint: Color = Color.White,
    textColor: Color = Color.White
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(brush)
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
        }
        Text(
            text = text,
            fontSize = PredictXTextSize.sm,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

private fun leagueLabel(league: LeagueType): String {
    return when (league) {
        LeagueType.NBA -> "NBA 籃球"
        LeagueType.MLB -> "MLB 棒球"
        LeagueType.NPB -> "NPB 日職"
        LeagueType.CPBL -> "CPBL 中職"
        LeagueType.WNBA -> "WNBA 籃球"
    }
}

private fun statusDescription(match: Match): String {
    if (match.status == MatchStatus.SCHEDULED) return "尚未開打"
    return when (match.status) {
        MatchStatus.LIVE -> "進行中"
        MatchStatus.POSTPONED -> "延期"
        MatchStatus.CANCELLED -> "取消"
        else -> "比分未紀錄"
    }
}

private fun filteredMatches(
    matches: List<Match>,
    searchText: String,
    dateRangeDays: Int
): List<Match> {
    var result = matches.sortedByDescending { it.startTime }
    if (dateRangeDays > 0) {
        val cutoff = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -dateRangeDays)
        }.timeInMillis
        result = result.filter { it.startTime >= cutoff }
    }
    val trimmed = searchText.trim()
    if (trimmed.isNotEmpty()) {
        val needle = trimmed.lowercase()
        result = result.filter { m ->
            m.homeTeam.lowercase().contains(needle) ||
                    m.awayTeam.lowercase().contains(needle) ||
                    m.homeTeamCN.lowercase().contains(needle) ||
                    m.awayTeamCN.lowercase().contains(needle)
        }
    }
    return result
}
