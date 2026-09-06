package com.predictxsports.android.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin
import com.predictxsports.android.ui.theme.PredictXTextSize
import com.predictxsports.android.ui.theme.SportsColors

/**
 * RadarChartView — 對應 iOS RadarChartView.swift
 *
 * 體育分析雷達圖：主隊藍 / 客隊紅
 * ⚠️ 數值已由後端 _normalize_radar_chart() 統一歸一化到 1.0-10.0，Android 端不再二次正規化
 */
@Composable
fun RadarChartView(
    categories: List<String>,
    homeValues: List<Double>,
    awayValues: List<Double>,
    homeTeamName: String = "主隊",
    awayTeamName: String = "客隊",
    homeColor: Color = SportsColors.brandPrimary,
    awayColor: Color = SportsColors.dangerRed,
    modifier: Modifier = Modifier
) {
    val textColorInt = if (MaterialTheme.colorScheme.background == SportsColors.darkBg) {
        android.graphics.Color.WHITE
    } else {
        android.graphics.Color.parseColor("#1C1C1E")
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(25.dp)
        ) {
            LegendItem(color = homeColor, name = homeTeamName)
            LegendItem(color = awayColor, name = awayTeamName)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(308.dp)
            ) {
                if (categories.isEmpty()) return@Canvas

                val w = size.width
                val h = size.height
                val cx = w / 2f
                val cy = h / 2f
                val radius = minOf(w, h) / 2f * 0.7f
                val n = categories.size

                // 1. 4 層網格
                for (level in 1..4) {
                    val scale = level * 0.25f
                    val gridPath = buildRadarPathConst(n, cx, cy, radius * scale)
                    drawPath(
                        path = gridPath,
                        color = Color.Gray.copy(alpha = 0.4f),
                        style = Stroke(width = 2f)
                    )
                }

                // 2. 軸線
                for (i in 0 until n) {
                    val (dx, dy) = axisPoint(i, n, cx, cy, radius)
                    val path = Path().apply {
                        moveTo(cx, cy)
                        lineTo(dx, dy)
                    }
                    drawPath(
                        path = path,
                        color = Color.Gray.copy(alpha = 0.6f),
                        style = Stroke(width = 2f)
                    )
                }

                // 3. 客隊資料
                val awayPath = buildRadarPath(awayValues, cx, cy, radius)
                drawPath(path = awayPath, color = awayColor.copy(alpha = 0.2f))
                drawPath(path = awayPath, color = awayColor, style = Stroke(width = 2f))

                // 4. 主隊資料
                val homePath = buildRadarPath(homeValues, cx, cy, radius)
                drawPath(path = homePath, color = homeColor.copy(alpha = 0.2f))
                drawPath(path = homePath, color = homeColor, style = Stroke(width = 2f))

                // 5. 標籤 + 資料點
                for (i in 0 until n) {
                    val angle = angleF(i, n)
                    val cosA = cos(angle)
                    val sinA = sin(angle)

                    val labelX = cx + cosA * (radius + 40f)
                    val labelY = cy + sinA * (radius + 40f)
                    drawTextNative(
                        text = categories[i],
                        position = Offset(labelX, labelY),
                        color = textColorInt,
                        textSize = 40f,
                        bold = true,
                        yOffset = 12f
                    )

                    val hRadius = radius * (homeValues[i] / 10.0).toFloat()
                    val hPt = Offset(cx + cosA * hRadius, cy + sinA * hRadius)
                    drawCircle(color = homeColor, radius = 5f, center = hPt)

                    val aRadius = radius * (awayValues[i] / 10.0).toFloat()
                    val aPt = Offset(cx + cosA * aRadius, cy + sinA * aRadius)
                    drawCircle(color = awayColor, radius = 5f, center = aPt)

                    val hLabel = if (i < homeValues.size) "%.0f".format(homeValues[i]) else "—"
                    val aLabel = if (i < awayValues.size) "%.0f".format(awayValues[i]) else "—"
                    drawTextNative(
                        text = hLabel,
                        position = Offset(hPt.x, hPt.y - 14f),
                        color = android.graphics.Color.parseColor("#0F4C81"),
                        textSize = 32f,
                        bold = true,
                        yOffset = 0f
                    )
                    drawTextNative(
                        text = aLabel,
                        position = Offset(aPt.x, aPt.y + 36f),
                        color = android.graphics.Color.parseColor("#D93B3B"),
                        textSize = 32f,
                        bold = true,
                        yOffset = 0f
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, name: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )
        Spacer(Modifier.padding(start = 4.dp))
        Text(
            text = name,
            fontSize = PredictXTextSize.md,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/** Float 角度（弧度），-π/2 起算（12 點鐘方向） */
private fun angleF(index: Int, total: Int): Float {
    return (Math.PI * 2 / total * index - Math.PI / 2).toFloat()
}

/** 軸線端點 */
private fun axisPoint(index: Int, total: Int, cx: Float, cy: Float, radius: Float): Pair<Float, Float> {
    val a = angleF(index, total)
    return Pair(cx + cos(a) * radius, cy + sin(a) * radius)
}

/** 建構固定半徑的多邊形（網格用） */
private fun buildRadarPathConst(count: Int, cx: Float, cy: Float, radius: Float): Path {
    val path = Path()
    for (i in 0 until count) {
        val a = angleF(i, count)
        val x = cx + cos(a) * radius
        val y = cy + sin(a) * radius
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}

/** 建構資料多邊形（半徑依 values 變化） */
private fun buildRadarPath(values: List<Double>, cx: Float, cy: Float, radius: Float): Path {
    val path = Path()
    for (i in values.indices) {
        val a = angleF(i, values.size)
        val r = radius * (values[i] / 10.0).toFloat()
        val x = cx + cos(a) * r
        val y = cy + sin(a) * r
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}

private fun normalize(vals: List<Double>, otherVals: List<Double>): List<Double> {
    if (vals.isEmpty()) return emptyList()
    val combined = vals + otherVals
    val minVal = combined.minOrNull() ?: 0.0
    val maxVal = combined.maxOrNull() ?: 100.0
    val range = maxVal - minVal
    if (range <= 0) return vals.map { 5.0 }
    return vals.map { 1.0 + ((it - minVal) / range) * 9.0 }
}

private fun DrawScope.drawTextNative(
    text: String,
    position: Offset,
    color: Int,
    textSize: Float,
    bold: Boolean = false,
    yOffset: Float = 0f
) {
    drawIntoCanvas { canvas ->
        val paint = android.graphics.Paint().apply {
            this.color = color
            this.textSize = textSize
            textAlign = android.graphics.Paint.Align.CENTER
            isFakeBoldText = bold
            isAntiAlias = true
        }
        canvas.nativeCanvas.drawText(text, position.x, position.y + yOffset, paint)
    }
}