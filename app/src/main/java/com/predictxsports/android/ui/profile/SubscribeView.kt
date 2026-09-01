package com.predictxsports.android.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.predictxsports.android.service.BillingManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.predictxsports.android.service.BillingViewModel
import com.predictxsports.android.service.ProductTier
import com.predictxsports.android.ui.theme.SportsColors
import com.predictxsports.android.ui.theme.PredictXTextSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscribeView(
    viewModel: BillingViewModel? = null,
    billingViewModel: BillingViewModel? = null,
    onClose: () -> Unit,
    onRestore: () -> Unit = {}
) {
    val effectiveViewModel = billingViewModel ?: viewModel ?: viewModel()
    // P1-3 修復：改用 rememberSaveable 確保旋轉螢幕/跨頁切換時使用者選的月/年方案不丟失。
    var selectedTier by rememberSaveable { mutableStateOf(ProductTier.STANDARD) }
    var isAnnual by rememberSaveable { mutableStateOf(false) }
    val isProcessing by effectiveViewModel.isProcessing.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI 額度儲值中心") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Filled.Close, contentDescription = "關閉")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SportsColors.cardBackground)
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header brand
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("👑", fontSize = PredictXTextSize.display, fontWeight = FontWeight.Bold, color = SportsColors.primaryText)
                Text("解鎖完整 AI 分析引擎", fontWeight = FontWeight.ExtraBold, fontSize = PredictXTextSize.xxl, color = SportsColors.primaryText, textAlign = TextAlign.Center)
                Text("四大運動聯盟 50+ 項特徵因子 • 即時推論 • 模型驗證率公開透明", fontSize = PredictXTextSize.sm, color = SportsColors.secondaryText, textAlign = TextAlign.Center)
            }

            Spacer(Modifier.height(16.dp))

            // 月/年切換
            val options = listOf(false to "Monthly", true to "Yearly")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50))
                    .background(SportsColors.cardSecondaryBackground)
                    .padding(4.dp)
            ) {
                options.forEach { (annual, label) ->
                    val selected = isAnnual == annual
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50))
                            .background(if (selected) Color(0xFF0F4C81) else Color.Transparent)
                            .clickable { isAnnual = annual }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, color = if (selected) Color.White else SportsColors.primaryText, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // 方案卡片並排
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val basicTier = ProductTier.BASIC
                val premiumTier = ProductTier.STANDARD
                TierCardHorizontal(
                    tier = basicTier,
                    isSelected = selectedTier == basicTier,
                    isAnnual = isAnnual,
                    onClick = { selectedTier = basicTier },
                    modifier = Modifier.weight(1f)
                )
                TierCardHorizontal(
                    tier = premiumTier,
                    isSelected = selectedTier == premiumTier,
                    isAnnual = isAnnual,
                    onClick = { selectedTier = premiumTier },
                    isBestOffer = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(24.dp))

            // 購買按鈕
            val price = selectedTier.currentPriceTWD(isAnnual)
            val context = LocalContext.current
            val skuId = when (selectedTier) {
                ProductTier.BASIC -> if (isAnnual) "predictx_basic_yearly" else "predictx_basic_monthly"
                ProductTier.STANDARD -> if (isAnnual) "predictx_standard_yearly" else "predictx_standard_monthly"
                ProductTier.FREE -> ""
            }
            androidx.compose.material3.Button(
                onClick = {
                    // 觸發 Google Play Billing 購買流程
                    (context as? android.app.Activity)?.let { activity ->
                        BillingManager.launchPurchaseFlow(activity, skuId)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isProcessing && BillingManager.isReady.value,
                shape = RoundedCornerShape(50),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFF0F4C81))
            ) {
                if (isProcessing) {
                    androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("處理中...", color = Color.White)
                } else {
                    Text("NT$ $price ${if (isAnnual) "/年" else "/月"}", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(Modifier.height(8.dp))

            // 恢復購買
            androidx.compose.material3.OutlinedButton(
                onClick = onRestore,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50)
            ) {
                Text("恢復購買")
            }

            Spacer(Modifier.height(20.dp))

            // Free 方案說明
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("Free：每天 60 分析點數上限。", fontSize = PredictXTextSize.sm, color = SportsColors.tertiaryText, textAlign = TextAlign.Center)
                Text("試用期 30 天，期滿後點數歸零。", fontSize = PredictXTextSize.sm, color = SportsColors.tertiaryText, textAlign = TextAlign.Center)
                Text("訂閱會自動續訂・可在 Google Play「訂閱項目」中隨時取消", fontSize = PredictXTextSize.sm, color = SportsColors.tertiaryText, textAlign = TextAlign.Center)
                Text("PredictX Sports 為運動數據分析工具・所有 AI 推論結果僅供參考・不構成任何投注建議", fontSize = PredictXTextSize.sm, color = SportsColors.tertiaryText, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun TierCardHorizontal(
    tier: ProductTier,
    isSelected: Boolean,
    isAnnual: Boolean,
    onClick: () -> Unit,
    isBestOffer: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = SportsColors.cardBackground),
        shape = RoundedCornerShape(18.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, tier.tint) else androidx.compose.foundation.BorderStroke(1.dp, tier.tint.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(tier.displayName, fontWeight = FontWeight.Bold, fontSize = PredictXTextSize.base, color = SportsColors.primaryText)
                Spacer(Modifier.weight(1f))
                if (isBestOffer) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFFE8923B))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text("Best Offer", fontSize = PredictXTextSize.sm, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
            Spacer(Modifier.height(3.dp))
            Text(tier.tagline, fontSize = PredictXTextSize.sm, color = SportsColors.secondaryText)
            Spacer(Modifier.height(8.dp))
            Text("NT$ ${tier.currentPriceTWD(isAnnual)}", fontSize = PredictXTextSize.xxxl, fontWeight = FontWeight.ExtraBold, color = tier.tint)
            Text(if (isAnnual) "/ 年" else "/ 月", fontSize = PredictXTextSize.sm, color = SportsColors.secondaryText)
            Spacer(Modifier.height(8.dp))
            tier.benefits.take(3).forEach { benefit ->
                Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(vertical = 2.dp)) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = tier.tint, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.size(4.dp))
                    Text(benefit, fontSize = PredictXTextSize.sm, color = SportsColors.primaryText)
                }
            }
            if (isSelected) {
                Spacer(Modifier.height(6.dp))
                Text("已選擇", fontSize = PredictXTextSize.sm, fontWeight = FontWeight.Bold, color = tier.tint)
            }
        }
    }
}
