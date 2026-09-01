package com.predictxsports.android.ui.profile

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Email

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.predictxsports.android.ui.theme.PredictXTextSize

/**
 * 客服中心頁面
 * - FAQ 常見問題（6 條可展開/收合）
 * - 「聯絡客服」按鈕（Email / Instagram / Facebook）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpView(onNavigateBack: () -> Unit) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("客服中心", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "常見問題",
                fontSize = PredictXTextSize.xl,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
            )

            faqList.forEach { item ->
                FAQCard(item = item)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "聯絡客服",
                fontSize = PredictXTextSize.xl,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )

            ContactButton(
                icon = Icons.Filled.Email,
                label = "Email：k06210621@gmail.com",
                accent = Color(0xFF0F4C81),
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:k06210621@gmail.com")
                        putExtra(Intent.EXTRA_SUBJECT, "PredictX Sports 客戶支援")
                    }
                    runCatching { context.startActivity(intent) }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "客服時間：週一至週五 09:00 - 18:00（台灣時間）",
                fontSize = PredictXTextSize.sm,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private data class FAQItem(
    val question: String,
    val answer: String
)

private val faqList = listOf(
    FAQItem(
        "如何獲得 AI 分析點數？",
        "免費試用期每天自動補滿 60 點。訂閱會員依方案享有更多點數；期滿後點數歸零，請升級方案享用無限點數。"
    ),
    FAQItem(
        "AI 預測的準確率如何？",
        "我們的 AI 模型整合 50+ 項特徵因子，依不同聯盟與賽事強度，歷史驗證率約 60-72%。詳細數據請參考「AI 模型驗證」分頁。"
    ),
    FAQItem(
        "AI 推論機率是如何計算的？",
        "AI 推論機率是根據即時數據（球隊近況、對戰紀錄、投手/打擊數據、天氣等 50+ 項特徵）輸入語言模型後，經由 Prompt 推論得出的分析結果。系統會持續與實際比賽結果比對，計算長期驗證率。"
    ),
    FAQItem(
        "AI 信心值代表什麼？",
        "信心值為 1-10 的整數評分，代表 AI 模型對該場推論的確信程度。信心值越高（≥8），表示模型掌握的數據越充分、勝負傾向越明確。僅信心值 ≥9 的推論才會顯示於「AI 重點觀察賽事」焦點區。"
    ),
    FAQItem(
        "為什麼推論結果會改變？",
        "賽前數據會持續更新（例如先發投手變動、最新傷兵消息、天氣變化等），AI 模型會根據最新的即時數據重新分析，因此推論結果可能隨時間微調。"
    ),
    FAQItem(
        "為什麼實際比賽結果和推論不同？",
        "運動比賽本身存在不可預測性（運氣、裁判判決、突發傷病等）。AI 推論是基於統計與數據模型的觀察，並非保證結果。本平台的目標是提供長期高於隨機基準的模型驗證率，而非每場 100% 準確。"
    ),
    FAQItem(
        "可以取消訂閱嗎？",
        "可以。請至 Google Play 商店 >「訂閱項目」，選擇 PredictX Sports 後點擊「取消訂閱」即可。取消後仍可使用剩餘的訂閱期間。"
    ),
    FAQItem(
        "如何恢復之前的購買？",
        "若已付費但未解鎖 Premium，請點擊「訂閱中心」>「恢復購買」，系統會自動驗證您的 Google Play 帳號歷史交易記錄。"
    ),
    FAQItem(
        "Premium 會員有哪些功能？",
        "Premium 會員可享有：① AI 賽事分析可全部查看 ② AI 重點觀察賽事查看 ③ 歷史驗證率圖表 ④ 優先體驗新功能。更多功能將陸續推出。"
    ),
    FAQItem(
        "訂閱後多久生效？",
        "訂閱完成後立即生效，不需等待。您可在「個人資訊」頁面查看會員卡片上的有效期限。"
    ),
    FAQItem(
        "為什麼我的賽事數據顯示延遲？",
        "賽事數據由第三方資料源（TheSportsDB 等）提供，可能有 30 秒 - 2 分鐘的延遲。AI 預測則在賽前分析完成，與即時比分無關。"
    ),
    FAQItem(
        "賽事多久更新一次？",
        "賽事前 24 小時即會載入排程。通常每日更新兩次，實際頻率依各聯賽 API 而定。"
    ),
    FAQItem(
        "支援哪些聯賽？",
        "目前支援：MLB（美國職棒）、NPB（日本職棒）、CPBL（中華職棒）、NBA（美國職籃）、WNBA（美國女籃）五大聯賽。更多聯賽將陸續新增。"
    ),
    FAQItem(
        "為什麼某些比賽沒有 AI 分析？",
        "原因可能為：① 比賽尚未進入可分析的時間窗口（通常賽前 24 小時內）② 該場資料不足（例如新成立的隊伍尚無歷史數據）③ 該聯賽 API 暫時無法取得所需數據。"
    ),
    FAQItem(
        "預測結果可以做為投注依據嗎？",
        "不可以。PredictX Sports 為運動數據分析工具，所有 AI 推論結果僅供參考，不構成任何投注建議。請理性看待預測內容。"
    )
)

@Composable
private fun FAQCard(item: FAQItem) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            .clickable { expanded = !expanded }
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    item.question,
                    fontSize = PredictXTextSize.md,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    item.answer,
                    fontSize = PredictXTextSize.base,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun ContactButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    accent: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(accent.copy(alpha = 0.18f), accent.copy(alpha = 0.06f))
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(accent.copy(alpha = 0.20f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                label,
                fontSize = PredictXTextSize.md,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}