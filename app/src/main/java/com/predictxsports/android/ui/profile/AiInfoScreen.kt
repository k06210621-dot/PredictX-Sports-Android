package com.predictxsports.android.ui.profile

import androidx.compose.foundation.layout.fillMaxWidth

import androidx.compose.foundation.layout.size

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.predictxsports.android.ui.theme.PredictXTextSize
import com.predictxsports.android.ui.theme.SportsColors

/**
 * AI 分析點數說明（彈窗）
 * 用戶在個人資訊按下「AI 使用額度」卡片時彈出。
 * 提供「了解」與「升級方案」兩種操作。
 */
@Composable
fun AiInfoScreen(
    onNavigateSubscribe: () -> Unit,
    onClose: () -> Unit = {}
) {
    var showDialog by remember { mutableStateOf(true) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                onClose()
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            icon = {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = SportsColors.warningOrange,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "AI 分析點數說明",
                    fontWeight = FontWeight.Bold,
                    fontSize = PredictXTextSize.xxl,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "分析點數可用於解鎖 Basic 方案的 AI 詳細分析。升級 Standard 或 Premium 方案即可享有無限分析額度。",
                    fontSize = PredictXTextSize.md,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = PredictXTextSize.lineHeightSm,
                    textAlign = TextAlign.Start
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        onNavigateSubscribe()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = SportsColors.brandPrimary)
                ) {
                    Text("升級方案", fontWeight = FontWeight.Bold, fontSize = PredictXTextSize.md)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                    onClose()
                }) {
                    Text("了解", fontWeight = FontWeight.Medium, fontSize = PredictXTextSize.md)
                }
            }
        )
    }
}
