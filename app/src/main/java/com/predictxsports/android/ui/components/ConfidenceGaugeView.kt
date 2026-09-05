package com.predictxsports.android.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.geometry.Offset

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path

import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.predictxsports.android.ui.theme.SportsColors
import com.predictxsports.android.ui.theme.PredictXTextSize

/**
 * ConfidenceGaugeView — 對應 iOS ConfidenceGaugeView.swift
 *
 * 半圓形信心指數 Gauge：
 * - 背景弧（淺灰）+ 前景弧（紅→橙→黃→綠 漸變，隨 progress 走）
 * - 中心：信心度百分比 + 推演比分
 * - 底部：主客隊標籤
 */
@Composable
fun ConfidenceGaugeView(
    confidence: Double,  // 0.0 .. 1.0
    scorePrediction: String?,
    homeTeam: String,
    awayTeam: String,
    modifier: Modifier = Modifier
) {
    val safeConfidence = confidence.coerceIn(0.0, 1.0)
    val gaugeColor = gaugeColorFor(safeConfidence)
    val label = confidenceLabel(safeConfidence)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SportsColors.cardSecondaryBackground, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
    ) {
        // 標題區
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Memory,
                contentDescription = null,
                tint = gaugeColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = "AI 推論信心",
                fontSize = PredictXTextSize.md,
                fontWeight = FontWeight.SemiBold,
                color = SportsColors.primaryText
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = label,
                fontSize = PredictXTextSize.sm,
                color = gaugeColor,
                modifier = Modifier
                    .background(gaugeColor.copy(alpha = 0.12f), RoundedCornerShape(50))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }

        // 半圓 Gauge
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                val w = size.width
                val h = size.height
                val strokeW = 14f
                val radius = minOf(w / 2f - strokeW, h - 10f)
                val centerX = w / 2f
                val centerY = h - strokeW / 2f

                // 背景弧（淺灰）
                drawPath(
                    path = gaugeArcPath(centerX, centerY, radius, 180f, 0f),
                    color = Color.Gray.copy(alpha = 0.2f),
                    style = Stroke(width = strokeW, cap = StrokeCap.Round)
                )

                // 前景弧（漸變）
                val endAngle = (180f - (safeConfidence * 180f)).toFloat()
                drawPath(
                    path = gaugeArcPath(centerX, centerY, radius, 180f, endAngle),
                    brush = androidx.compose.ui.graphics.Brush.sweepGradient(
                        colors = listOf(
                            SportsColors.dangerRed,    // 紅
                            SportsColors.warningOrange, // 橙
                            Color(0xFFE8C53B),          // 黃（暫無對應 token）
                            SportsColors.successGreen   // 綠
                        ),
                        center = Offset(centerX, centerY)
                    ),
                    style = Stroke(width = strokeW, cap = StrokeCap.Round)
                )
            }

            // 中心數據面板
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    text = "%.0f%%".format(safeConfidence * 100),
                    fontSize = PredictXTextSize.heroXl,
                    fontWeight = FontWeight.Bold,
                    color = gaugeColor
                )
                if (!scorePrediction.isNullOrEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "推演比分",
                            fontSize = PredictXTextSize.sm,
                            color = SportsColors.secondaryText
                        )
                        Text(
                            text = scorePrediction,
                            fontSize = PredictXTextSize.md,
                            fontWeight = FontWeight.Bold,
                            color = SportsColors.primaryText
                        )
                    }
                }
            }
        }

        // 底部主客標籤
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp)
            ) {
                Box(modifier = Modifier.size(8.dp).background(SportsColors.brandPrimary, CircleShape))
                Text(text = homeTeam, fontSize = PredictXTextSize.sm, color = SportsColors.secondaryText)
            }
            Spacer(Modifier.weight(1f))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp)
            ) {
                Box(modifier = Modifier.size(8.dp).background(SportsColors.dangerRed, CircleShape))
                Text(text = awayTeam, fontSize = PredictXTextSize.sm, color = SportsColors.secondaryText)
            }
        }
    }
}

private fun gaugeColorFor(confidence: Double): Color = when {
    confidence >= 0.85 -> SportsColors.successGreen   // 深綠
    confidence >= 0.70 -> SportsColors.brandPrimary   // 藍
    confidence >= 0.55 -> SportsColors.warningOrange  // 橙
    else -> SportsColors.dangerRed                    // 紅
}

private fun confidenceLabel(confidence: Double): String = when {
    confidence >= 0.85 -> "極高信心"
    confidence >= 0.70 -> "中高信心"
    confidence >= 0.55 -> "中等信心"
    else -> "低信心"
}

/**
 * 半圓弧 path — 對應 iOS GaugeTrackShape.path()
 * center 在底部中央，角度 180°(左) → 0°(右) 逆時針繪製
 */
private fun gaugeArcPath(
    centerX: Float,
    centerY: Float,
    radius: Float,
    startDegrees: Float,
    endDegrees: Float
): Path {
    val path = Path()
    // Compose：角度以 rad 計，0 = 3 點鐘方向，負 = 上方
    // 180° = 9 點鐘（左），0° = 3 點鐘（右）
    val startRad = Math.toRadians(startDegrees.toDouble()).toFloat()
    val endRad = Math.toRadians(endDegrees.toDouble()).toFloat()
    path.arcTo(
        rect = androidx.compose.ui.geometry.Rect(
            left = centerX - radius,
            top = centerY - radius,
            right = centerX + radius,
            bottom = centerY + radius
        ),
        startAngleDegrees = startDegrees,
        sweepAngleDegrees = endDegrees - startDegrees,
        forceMoveTo = false
    )
    return path
}