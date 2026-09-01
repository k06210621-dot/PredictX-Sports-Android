package com.predictxsports.android.ui.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.predictxsports.android.ui.theme.PredictXTextSize
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * TrendChartView — 對應 iOS AnalyticsView.swift 的 TrendChartSection
 *
 * 用 Compose Canvas 自繪折線圖（不引入第三方 chart 依賴），
 * 顯示單一聯賽近 50 場的每日命中率趨勢。
 *
 * iOS 原始設計：
 * - 標題：「{league} 驗證率趨勢 (近 50 場)」
 * - Y 軸：0% ~ 100%
 * - X 軸：日期 MM/dd（desiredCount 5 個標籤）
 * - 折線：藍色漸層 + 資料點
 */
@Composable
fun TrendChartView(
    league: String,
    trends: List<WinRateTrend>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 標題列
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "$league 驗證率趨勢 (近 50 場)",
                    fontWeight = FontWeight.Bold,
                    fontSize = PredictXTextSize.lg,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.weight(1f))
                Text(
                    "日波動",
                    fontSize = PredictXTextSize.sm,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(12.dp))

            if (trends.isEmpty()) {
                Text(
                    "尚無趨勢資料",
                    fontSize = PredictXTextSize.sm,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            } else {
                TrendLineChart(trends = trends)
            }
        }
    }
}

@Composable
private fun TrendLineChart(trends: List<WinRateTrend>) {
    val lineColor = Color(0xFF0F4C81)
    val gridColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    // 排序（依日期升冪）
    val sorted = trends.sortedBy { it.date }

    Column {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            val w = size.width
            val h = size.height
            val leftPad = 40f   // Y 軸標籤空間
            val rightPad = 12f
            val topPad = 16f
            val bottomPad = 32f  // X 軸標籤空間
            val chartW = w - leftPad - rightPad
            val chartH = h - topPad - bottomPad

            // 1. 水平網格線（0% / 25% / 50% / 75% / 100%）
            for (i in 0..4) {
                val yRatio = i / 4f
                val y = topPad + chartH * (1f - yRatio)
                drawLine(
                    color = gridColor,
                    start = Offset(leftPad, y),
                    end = Offset(leftPad + chartW, y),
                    strokeWidth = 1f
                )
                // Y 軸標籤
                val pct = (yRatio * 100).toInt()
                drawContext.canvas.nativeCanvas.drawText(
                    "$pct%",
                    leftPad - 34f,
                    y + 4f,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#8A8A93")
                        textSize = 24f
                        isAntiAlias = true
                    }
                )
            }

            // 2. 資料點座標
            val n = sorted.size
            if (n == 0) return@Canvas
            val points = sorted.mapIndexed { index, trend ->
                val x = if (n == 1) leftPad + chartW / 2f
                        else leftPad + chartW * (index.toFloat() / (n - 1))
                val y = topPad + chartH * (1f - trend.hitRate.toFloat().coerceIn(0f, 1f))
                Offset(x, y)
            }

            // 3. 折線（藍色）
            if (points.size > 1) {
                val path = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) {
                        lineTo(points[i].x, points[i].y)
                    }
                }
                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(
                        width = 6f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }

            // 4. 資料點（藍色圓點）
            points.forEach { p ->
                drawCircle(color = lineColor, radius = 7f, center = p)
            }

            // 5. X 軸日期標籤（最多 5 個）
            val labelCount = minOf(5, n)
            val df = SimpleDateFormat("MM/dd", Locale.TAIWAN)
            for (i in 0 until labelCount) {
                val dataIndex = if (labelCount == 1) 0
                                else (i * (n - 1) / (labelCount - 1))
                val trend = sorted[dataIndex]
                val x = points[dataIndex].x
                val label = df.format(java.util.Date(trend.date))
                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    x - 20f,
                    h - 8f,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#8A8A93")
                        textSize = 22f
                        isAntiAlias = true
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
            }
        }
    }
}
