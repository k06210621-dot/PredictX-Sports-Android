package com.predictxsports.android.ui.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.predictxsports.android.ui.theme.SportsColors
import com.predictxsports.android.ui.theme.PredictXTextSize

@Composable
fun SupportCenterView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("客服中心", fontSize = PredictXTextSize.xxxl, color = SportsColors.primaryText)
        Spacer(Modifier.height(16.dp))
        Text("常見問題", fontSize = PredictXTextSize.xl, color = SportsColors.primaryText)
        Spacer(Modifier.height(8.dp))
        Text("Q: 如何查看 AI 分析？\nA: 在「智能分析」頁面選擇賽事卡片，點擊即可查看詳細 AI 分析。", fontSize = PredictXTextSize.md, color = SportsColors.secondaryText)
        Spacer(Modifier.height(16.dp))
        Text("Q: 分析點數如何取得？\\nA: 免費試用期間每日補滿 60 點。期滿後點數歸零，請升級方案享用無限點數。", fontSize = PredictXTextSize.md, color = SportsColors.secondaryText)
        Spacer(Modifier.height(16.dp))
        Text("意見回饋", fontSize = PredictXTextSize.xl, color = SportsColors.primaryText)
        Spacer(Modifier.height(8.dp))
        Text("請寄送電子郵件至：support@predictxsports.com\n我們將在 24 小時內回覆您的問題。", fontSize = PredictXTextSize.md, color = SportsColors.secondaryText)
    }
}