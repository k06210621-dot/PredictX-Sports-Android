package com.predictxsports.android.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.Lock

import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.predictxsports.android.data.model.LeagueType
import com.predictxsports.android.data.model.Match
import com.predictxsports.android.ui.theme.LeagueTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.predictxsports.android.ui.theme.PredictXTextSize
import com.predictxsports.android.ui.theme.SportsColors
import androidx.compose.foundation.layout.widthIn

@Composable
fun PredictionRowView(
    match: Match,
    isLocked: Boolean = true,
    costHint: Int = 0,
    isFavorited: Boolean = false,
    canFavorite: Boolean = false,
    onFavoriteToggle: (() -> Unit)? = null,
    onCardClick: (() -> Unit)? = null,
    onUnlockTapped: (() -> Unit)? = null
) {
    val themeColor = LeagueTheme.color(match.league)
    val df = SimpleDateFormat("MM/dd", Locale.TAIWAN)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        themeColor.copy(alpha = 0.5f),
                        themeColor.copy(alpha = 0.15f)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Row 1: league + date + confidence + favorite
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(themeColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        match.league.rawValue,
                        fontSize = PredictXTextSize.sm,
                        fontWeight = FontWeight.Bold,
                        color = themeColor
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    df.format(Date(match.startTime)),
                    fontSize = PredictXTextSize.sm,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "%.1f/10".format(match.aiConfidence ?: 0.0),
                    fontSize = PredictXTextSize.xxl,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.weight(1f))
                if (canFavorite) {
                    IconButton(onClick = { onFavoriteToggle?.invoke() }, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = if (isFavorited) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = "收藏",
                            tint = if (isFavorited) SportsColors.brandSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Row 2: teams
            TeamsRow(match = match, onClick = onCardClick)

            // Row 3: win rate bar (only if unlocked) or unlock button
            if (isLocked) {
                if (costHint > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .clickable { onUnlockTapped?.invoke() }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = "解鎖",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            } else {
                // Win rate bar
                val homeWinRate = match.aiWinRateHome ?: 0.5
                com.predictxsports.android.ui.components.WinRateBar(
                    homeWinRate = homeWinRate,
                    homeTeam = match.homeTeamCN,
                    awayTeam = match.awayTeamCN
                )

                // 聯賽特定進階數據面板
                LeagueSpecificFactorsPanel(match = match)
            }
        }
    }
}

@Composable
private fun TeamsRow(match: Match, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = match.homeTeam,
                fontSize = PredictXTextSize.xl,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Text(
                text = match.homeTeamCN,
                fontSize = PredictXTextSize.sm,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }

        Text(
            text = "VS",
            fontSize = PredictXTextSize.xl,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
            Text(
                text = match.awayTeam,
                fontSize = PredictXTextSize.xl,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Text(
                text = match.awayTeamCN,
                fontSize = PredictXTextSize.sm,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun CompactPredictionRowView(
    match: Match,
    onCardClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val themeColor = LeagueTheme.color(match.league)
    val dateFmt = SimpleDateFormat("MM/dd", Locale.TAIWAN)

    // 🎨 強化美工：信心度分級色（≥8 橘金熱門、6-8 品牌藍、<6 中性灰藍）
    val conf = match.aiConfidence ?: 0.0
    val confColor = when {
        conf >= 8.0 -> SportsColors.brandTertiary
        conf >= 6.0 -> SportsColors.brandPrimary
        else -> SportsColors.inactiveText
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(2.dp)
            .background(
                // 🎨 強化美工：頂部主題色暈染 → 底部純 surface 的柔和漸層底
                Brush.verticalGradient(
                    colors = listOf(
                        themeColor.copy(alpha = 0.14f),
                        MaterialTheme.colorScheme.surface
                    )
                ),
                RoundedCornerShape(18.dp)
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        themeColor.copy(alpha = 0.65f),
                        themeColor.copy(alpha = 0.18f)
                    )
                ),
                shape = RoundedCornerShape(18.dp)
            )
            .then(if (onCardClick != null) Modifier.clickable { onCardClick() } else Modifier)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        // ── Row 1: 聯盟徽章 + 日期 + 信心度徽章（信心度依分級變色）──
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                match.league.rawValue,
                fontSize = PredictXTextSize.sm,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(themeColor, themeColor.copy(alpha = 0.75f))
                        )
                    )
                    .padding(horizontal = 9.dp, vertical = 3.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                dateFmt.format(Date(match.startTime)),
                fontSize = PredictXTextSize.sm,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.weight(1f))
            // 信心度徽章：分級色底 + 白字（≥8 橘金＝AI 熱門）
            Text(
                "%.1f/10".format(conf),
                fontSize = PredictXTextSize.sm,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(confColor)
                    .padding(horizontal = 9.dp, vertical = 3.dp)
            )
        }

        // ── Row 2: 英文隊名行（主行）── 主隊左對齊 / VS 置中 / 客隊右對齊
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                match.homeTeam,
                fontSize = PredictXTextSize.base,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                "VS",
                fontSize = PredictXTextSize.sm,
                fontWeight = FontWeight.Black,
                color = themeColor.copy(alpha = 0.55f)
            )
            Text(
                match.awayTeam,
                fontSize = PredictXTextSize.base,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                textAlign = androidx.compose.ui.text.style.TextAlign.End,
                modifier = Modifier.weight(1f)
            )
        }

        // ── Row 3: 中文隊名行（輔行，次要色）──
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                match.homeTeamCN,
                fontSize = PredictXTextSize.sm,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                "vs",
                fontSize = PredictXTextSize.sm,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Text(
                match.awayTeamCN,
                fontSize = PredictXTextSize.sm,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = androidx.compose.ui.text.style.TextAlign.End,
                modifier = Modifier.weight(1f)
            )
        }

        // ── Row 4 已依產品指示移除：不再顯示收藏星與「解鎖 N 點」按鈕，
        //     整卡可點擊（已解鎖→進詳情；未解鎖→由 HomeView 跳「同意，扣除 20 點」對話框）──
    }
}

/**
 * 聯賽特定進階數據面板
 * 根據 match.league 顯示對應因子（MLB/NBA/CPBL/NPB）
 */
@Composable
fun LeagueSpecificFactorsPanel(match: Match) {
    val themeColor = LeagueTheme.color(match.league)
    val factors = leagueFactors(match)
    if (factors.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .padding(10.dp)
    ) {
        Text(
            text = leagueFactorsTitle(match.league),
            fontSize = PredictXTextSize.sm,
            fontWeight = FontWeight.Bold,
            color = themeColor,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            factors.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    row.forEach { (label, value) ->
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "$label:",
                                fontSize = PredictXTextSize.sm,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                            Text(
                                text = value,
                                fontSize = PredictXTextSize.sm,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                        }
                    }
                    if (row.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

private fun leagueFactorsTitle(league: LeagueType): String = when (league) {
    LeagueType.MLB -> "⚾ MLB 關鍵分析因子"
    LeagueType.NBA -> "🏀 NBA 四大效率因子"
    LeagueType.CPBL -> "⚾ CPBL 中職關鍵指標"
    LeagueType.NPB -> "⚾ NPB 王牌壓制與戰術指標"
    LeagueType.WNBA -> "🏀 WNBA 關鍵分析因子"
}

private fun leagueFactors(match: Match): List<Pair<String, String>> = when (match.league) {
    LeagueType.MLB -> listOfNotNull(
        match.mlbFeatures?.homeStarterFIP?.let { "主投 FIP" to "%.2f".format(it) },
        match.mlbFeatures?.awayStarterFIP?.let { "客投 FIP" to "%.2f".format(it) },
        match.mlbFeatures?.homeTeamwOBA?.let { "主隊 wOBA" to "%.3f".format(it) },
        match.mlbFeatures?.awayTeamwOBA?.let { "客隊 wOBA" to "%.3f".format(it) },
        match.mlbFeatures?.parkFactor?.let { "球場修正" to "%.2f".format(it) },
        match.mlbFeatures?.windDirection?.let { "球場環境" to it }
    )
    LeagueType.NBA -> listOfNotNull(
        match.nbaFeatures?.homeEFG?.let { "主隊 eFG%" to "%.1f".format(it) },
        match.nbaFeatures?.awayEFG?.let { "客隊 eFG%" to "%.1f".format(it) },
        match.nbaFeatures?.homeTOV?.let { "主隊 TOV%" to "%.1f".format(it) },
        match.nbaFeatures?.awayTOV?.let { "客隊 TOV%" to "%.1f".format(it) },
        match.nbaFeatures?.pace?.let { "預估 Pace" to "%.1f".format(it) }
    )
    LeagueType.CPBL -> listOfNotNull(
        match.cpblFeatures?.localPitcherFIP?.let { "本土 FIP" to "%.2f".format(it) },
        match.cpblFeatures?.foreignPitcherFIP?.let { "洋投 FIP" to "%.2f".format(it) },
        match.cpblFeatures?.bullpenWHIP?.let { "牛棚 WHIP" to "%.2f".format(it) },
        match.cpblFeatures?.homeRunDerbyRate?.let { "HR 產出率" to "%.2f".format(it) }
    )
    LeagueType.NPB -> listOfNotNull(
        match.npbFeatures?.aceStarterWHIP?.let { "王牌 WHIP" to "%.2f".format(it) },
        match.npbFeatures?.aceStarterERA?.let { "王牌 ERA" to "%.2f".format(it) },
        match.npbFeatures?.teamFieldingPercentage?.let { "團隊守備率" to "%.3f".format(it) },
        match.npbFeatures?.sacrificeAndSmallBallRate?.let { "戰術推進率" to "%.2f".format(it) },
        match.npbFeatures?.npbParkFactor?.let { "球場修正" to "%.2f".format(it) },
        match.npbFeatures?.travelFatigueIndex?.let { "遠征疲勞" to "%.2f".format(it) }
    )
    else -> emptyList()
}