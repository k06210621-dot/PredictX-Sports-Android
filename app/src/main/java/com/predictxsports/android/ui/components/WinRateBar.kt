package com.predictxsports.android.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.MaterialTheme
import com.predictxsports.android.ui.theme.PredictXTextSize
import com.predictxsports.android.ui.theme.SportsColors

/**
 * WinRateBar — 對應 iOS WinRateBar (Swift)
 *
 * 雙向勝率條：左側主隊% / 右側客隊%
 * - isLocked=true → 顯示灰底 + 鎖頭提示
 */
@Composable
fun WinRateBar(
    homeWinRate: Double,
    homeTeam: String,
    awayTeam: String,
    isLocked: Boolean = false,
    onUnlockTapped: (() -> Unit)? = null,
    costHint: Int = 0,
    homeColor: Color = SportsColors.brandPrimary,
    awayColor: Color = SportsColors.brandSecondary
) {
    val safeHome = homeWinRate.coerceIn(0.0, 1.0)
    val awayRate = 1.0 - safeHome

    // Animated progress on first composition
    var animProgress by remember { mutableStateOf(0f) }
    LaunchedEffect(safeHome) {
        animProgress = safeHome.toFloat()
    }
    val animatedHome by animateFloatAsState(
        targetValue = animProgress,
        animationSpec = tween(800, delayMillis = 100),
        label = "winRateHome"
    )
    val animatedAway = 1f - animatedHome

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .let {
                if (isLocked && onUnlockTapped != null) it.clickable { onUnlockTapped() } else it
            }
            .padding(vertical = 2.dp)
    ) {
        if (isLocked) {
            // 鎖定狀態：顯示 prompt 與花費
            val promptText = if (costHint > 0) "花費 $costHint 點解鎖勝率分析" else "解鎖查看勝率分析"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "解鎖",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = promptText,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        fontSize = PredictXTextSize.base
                    )
                }
            }
        } else {
            // 對齊 iOS：同一行顯示「隊伍名稱 %」，而非分兩行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${homeTeam} ${(animatedHome * 100).toInt()}%",
                    fontSize = PredictXTextSize.sm,
                    fontWeight = FontWeight.Bold,
                    color = homeColor
                )
                Text(
                    text = "${(animatedAway * 100).toInt()}% ${awayTeam}",
                    fontSize = PredictXTextSize.sm,
                    fontWeight = FontWeight.Bold,
                    color = awayColor
                )
            }
            Spacer(Modifier.height(2.dp))
            // 主隊（由中央往兩側 grow）
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            ) {
                Box(
                    modifier = Modifier
                        .weight(animatedHome.coerceAtLeast(0.001f))
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(homeColor)
                )
                Box(
                    modifier = Modifier
                        .weight(animatedAway.coerceAtLeast(0.001f))
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(awayColor)
                )
            }
        }
    }
}