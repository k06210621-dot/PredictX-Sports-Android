package com.predictxsports.android.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * SkeletonView — 對應 iOS SkeletonLoadingView.swift
 *
 * 包含：
 * - SkeletonCardView: 單一骨架塊 + shimmer 動畫
 * - AnalysisSkeletonView: 分析詳情頁骨架
 * - HomeSkeletonView: 首頁骨架
 */
@Composable
fun SkeletonCardView(
    modifier: Modifier = Modifier,
    width: androidx.compose.ui.unit.Dp? = null,
    height: androidx.compose.ui.unit.Dp = 16.dp
) {
    val baseColor = Color.Gray.copy(alpha = 0.20f)
    val shimmerColorLight = Color.White.copy(alpha = 0.55f)
    val shimmerColorPeak = Color.White.copy(alpha = 0.12f)
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    val sizeMod = if (width != null) Modifier.size(width, height) else Modifier.fillMaxWidth().height(height)

    Box(
        modifier = modifier
            .then(sizeMod)
            .clip(RoundedCornerShape(10.dp))
            .background(baseColor)
            .drawWithCache {
                // 在父 layout 的座標系內畫 shimmer 漸層帶，確保必定跨過整個 box
                val w = size.width
                val bandWidth = w * 0.45f
                val start = -bandWidth + (w + bandWidth) * progress
                val brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        shimmerColorPeak,
                        shimmerColorLight,
                        shimmerColorPeak,
                        Color.Transparent
                    ),
                    start = Offset(start, 0f),
                    end = Offset(start + bandWidth, 0f)
                )
                onDrawBehind {
                    drawRect(brush)
                }
            }
    )
}

@Composable
fun AnalysisSkeletonView() {
    androidx.compose.foundation.lazy.LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(20.dp)
    ) {
        item {
            // 勝率推論卡片骨架
            androidx.compose.foundation.layout.Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(15.dp)
            ) {
                SkeletonCardView(width = 120.dp, height = 20.dp)
                SkeletonCardView(height = 50.dp)
                androidx.compose.foundation.layout.Row {
                    SkeletonCardView(width = 100.dp, height = 14.dp)
                    androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
                    SkeletonCardView(width = 100.dp, height = 14.dp)
                }
            }
        }
        item {
            // 雷達圖卡片骨架
            androidx.compose.foundation.layout.Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
            ) {
                SkeletonCardView(width = 160.dp, height = 20.dp)
                SkeletonCardView(height = 200.dp)
            }
        }
        item {
            // 深度分析卡片骨架
            androidx.compose.foundation.layout.Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
            ) {
                SkeletonCardView(width = 180.dp, height = 20.dp)
                SkeletonCardView(height = 60.dp)
                SkeletonCardView(width = 140.dp, height = 18.dp)
                SkeletonCardView(height = 16.dp)
                SkeletonCardView(height = 16.dp)
                SkeletonCardView(height = 16.dp)
            }
        }
    }
}