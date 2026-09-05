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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material.icons.filled.SportsBaseball
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.CreditCard

import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.automirrored.filled.Forward
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import com.predictxsports.android.ui.theme.SportsColors
import com.predictxsports.android.ui.theme.PredictXTextSize

data class DisclaimerSection(
    val id: String,
    val icon: ImageVector,
    val title: String,
    val color: Color,
    val paragraphs: List<ParagraphItem>
)

sealed class ParagraphItem {
    data class TextItem(val text: String) : ParagraphItem()
    data class BulletItem(val text: String, val icon: ImageVector, val color: Color) : ParagraphItem()
    data class LinkItem(val text: String, val url: String) : ParagraphItem()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalDisclaimerView(
    onNavigateBack: () -> Unit
) {
    var expandedSectionId by remember { mutableStateOf<String?>(null) }
    val uriHandler: UriHandler = LocalUriHandler.current
    
    val sections = remember { buildDisclaimerSections() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("法律聲明") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(SportsColors.cardBackground)
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
        ) {
            // Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "法律聲明",
                        fontWeight = FontWeight.Bold,
                        fontSize = PredictXTextSize.display,
                        color = SportsColors.primaryText
                    )
                    Text(
                        "PredictX Sports Legal Disclaimer",
                        fontSize = PredictXTextSize.sm,
                        color = SportsColors.secondaryText
                    )
                    Spacer(Modifier.height(8.dp))
                    Spacer(
                        modifier = Modifier
                            .width(60.dp)
                            .height(3.dp)
                            .background(Color.Blue.copy(alpha = 0.3f), RoundedCornerShape(1.5f))
                    )
                }
            }

            // Sections
            items(sections, key = { it.id }) { section ->
                val isExpanded = expandedSectionId == section.id
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = SportsColors.cardSecondaryBackground
                    ),
                    shape = RoundedCornerShape(16.dp),
                    onClick = {
                        expandedSectionId = if (isExpanded) null else section.id
                    }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Section header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        section.color.copy(alpha = 0.12f),
                                        RoundedCornerShape(10.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    section.icon,
                                    contentDescription = null,
                                    tint = section.color,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(
                                section.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = PredictXTextSize.xl,
                                color = SportsColors.primaryText,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = null,
                                tint = SportsColors.secondaryText
                            )
                        }
                        
                        // Section content
                        if (isExpanded) {
                            Spacer(Modifier.height(12.dp))
                            section.paragraphs.forEach { item ->
                                when (item) {
                                    is ParagraphItem.TextItem -> {
                                        Text(
                                            item.text,
                                            fontSize = PredictXTextSize.base,
                                            color = SportsColors.secondaryText,
                                            lineHeight = PredictXTextSize.lineHeightSm
                                        )
                                        Spacer(Modifier.height(8.dp))
                                    }
                                    is ParagraphItem.BulletItem -> {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Icon(
                                                item.icon,
                                                contentDescription = null,
                                                tint = item.color,
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .padding(top = 3.dp)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                item.text,
                                                fontSize = PredictXTextSize.base,
                                                color = SportsColors.secondaryText,
                                                lineHeight = PredictXTextSize.lineHeightSm,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        Spacer(Modifier.height(4.dp))
                                    }
                                    is ParagraphItem.LinkItem -> {
                                        Text(
                                            item.text,
                                            fontSize = PredictXTextSize.base,
                                            color = Color.Blue,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    try {
                                                        uriHandler.openUri(item.url)
                                                    } catch (e: Exception) {
                                                        // URL 開啟失敗，忽略
                                                    }
                                                }
                                                .padding(vertical = 6.dp)
                                        )
                                        Spacer(Modifier.height(4.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Footer
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(0.6f),
                        color = SportsColors.secondaryText.copy(alpha = 0.2f)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "© 2026 PredictX Sports",
                        fontWeight = FontWeight.Bold,
                        fontSize = PredictXTextSize.sm,
                        color = SportsColors.primaryText
                    )
                    Text(
                        "All Rights Reserved.",
                        fontSize = PredictXTextSize.sm,
                        color = SportsColors.secondaryText
                    )
                    Text(
                        "Version 1.0.0",
                        fontSize = PredictXTextSize.sm,
                        color = SportsColors.secondaryText.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

private fun buildDisclaimerSections(): List<DisclaimerSection> {
    return listOf(
        DisclaimerSection(
            id = "section1",
            icon = Icons.Filled.Info,
            title = "1. 服務性質說明",
            color = Color.Blue,
            paragraphs = listOf(
                ParagraphItem.TextItem("PredictX Sports 為 AI 運動數據分析平台，提供以下服務："),
                ParagraphItem.BulletItem("運動賽事數據分析", Icons.Filled.BarChart, Color.Blue),
                ParagraphItem.BulletItem("歷史統計資料查詢", Icons.Filled.History, Color.Blue),
                ParagraphItem.BulletItem("AI 模型推論結果", Icons.Filled.Info, Color.Blue),
                ParagraphItem.BulletItem("球隊與球員資訊", Icons.Filled.People, Color.Blue),
                ParagraphItem.TextItem("本平台所提供之所有資訊、數據、分析結果及 AI 推論內容，僅供使用者參考，不作為任何形式之決策依據。使用者應自行評估並承擔使用本平台服務所產生之所有風險。")
            )
        ),
        DisclaimerSection(
            id = "section2",
            icon = Icons.Filled.Info,
            title = "2. AI 分析免責聲明",
            color = Color(0xFF8B5CF6),
            paragraphs = listOf(
                ParagraphItem.TextItem("AI 推論結果係依據以下資料產生："),
                ParagraphItem.BulletItem("歷史賽事數據", Icons.AutoMirrored.Filled.List, Color(0xFF8B5CF6)),
                ParagraphItem.BulletItem("球員與球隊資訊", Icons.Filled.People, Color(0xFF8B5CF6)),
                ParagraphItem.BulletItem("過往賽事紀錄", Icons.Filled.EmojiEvents, Color(0xFF8B5CF6)),
                ParagraphItem.BulletItem("統計數學模型", Icons.Filled.Functions, Color(0xFF8B5CF6)),
                ParagraphItem.BulletItem("機器學習演算法", Icons.Filled.Settings, Color(0xFF8B5CF6)),
                ParagraphItem.TextItem("本平台不保證 AI 推論結果之："),
                ParagraphItem.BulletItem("正確性（Accuracy）", Icons.Filled.Warning, Color(0xFFFF9800)),
                ParagraphItem.BulletItem("完整性（Completeness）", Icons.Filled.Warning, Color(0xFFFF9800)),
                ParagraphItem.BulletItem("即時性（Timeliness）", Icons.Filled.Warning, Color(0xFFFF9800)),
                ParagraphItem.BulletItem("精準率（Precision）", Icons.Filled.Warning, Color(0xFFFF9800)),
                ParagraphItem.TextItem("任何 AI 分析結果均不構成對賽事結果之保證。實際賽事結果受多種不可預測因素影響，包括但不限於球員狀態、天氣條件、裁判判決及突發事件。")
            )
        ),
        DisclaimerSection(
            id = "section3",
            icon = Icons.Filled.Close,
            title = "3. 非博彩工具聲明",
            color = Color.Red,
            paragraphs = listOf(
                ParagraphItem.TextItem("PredictX Sports 明確聲明本平台："),
                ParagraphItem.BulletItem("不提供任何投注功能", Icons.Filled.Cancel, Color.Red),
                ParagraphItem.BulletItem("不提供任何下注服務", Icons.Filled.Cancel, Color.Red),
                ParagraphItem.BulletItem("不提供任何資金交易", Icons.Filled.Cancel, Color.Red),
                ParagraphItem.BulletItem("不提供任何賠率資訊", Icons.Filled.Cancel, Color.Red),
                ParagraphItem.TextItem("本平台之所有功能及內容，僅限於運動賽事數據分析與統計研究用途。使用者須自行了解並遵守所在地之相關法律規範。若使用者所在地法律禁止或限制運動數據分析工具之使用，使用者應立即停止使用本平台服務。"),
                ParagraphItem.TextItem("嚴禁未成年人使用本平台進行任何與博弈相關之行為。")
            )
        ),
        DisclaimerSection(
            id = "section4",
            icon = Icons.Filled.Warning,
            title = "4. 投資與決策風險聲明",
            color = Color(0xFFFF9800),
            paragraphs = listOf(
                ParagraphItem.TextItem("使用者因依據本平台提供之以下內容所作出的任何決策："),
                ParagraphItem.BulletItem("AI 分析結果", Icons.Filled.Info, Color(0xFFFF9800)),
                ParagraphItem.BulletItem("統計數據", Icons.Filled.BarChart, Color(0xFFFF9800)),
                ParagraphItem.BulletItem("AI 推論分析", Icons.AutoMirrored.Filled.Forward, Color(0xFFFF9800)),
                ParagraphItem.BulletItem("歷史資料", Icons.Filled.History, Color(0xFFFF9800)),
                ParagraphItem.TextItem("包括但不限於以下行為所產生之任何損失："),
                ParagraphItem.BulletItem("投資決策", Icons.Filled.MonetizationOn, Color.Red),
                ParagraphItem.BulletItem("博彩行為", Icons.Filled.Casino, Color.Red),
                ParagraphItem.BulletItem("商業決策", Icons.Filled.Business, Color.Red),
                ParagraphItem.BulletItem("個人選擇", Icons.Filled.Person, Color.Red),
                ParagraphItem.TextItem("PredictX Sports、其開發團隊、關聯公司及員工，均不負任何法律責任。使用者應對自身決策負完全責任。")
            )
        ),
        DisclaimerSection(
            id = "section5",
            icon = Icons.Filled.Wifi,
            title = "5. 數據來源聲明",
            color = Color.Green,
            paragraphs = listOf(
                ParagraphItem.TextItem("本平台之資料可能來自以下來源："),
                ParagraphItem.BulletItem("官方聯盟提供之數據", Icons.Filled.Business, Color.Green),
                ParagraphItem.BulletItem("公開資訊與統計資料", Icons.Filled.Book, Color.Green),
                ParagraphItem.BulletItem("合法授權之第三方 API", Icons.Filled.Link, Color.Green),
                ParagraphItem.BulletItem("經授權之數據合作夥伴", Icons.Filled.Handshake, Color.Green),
                ParagraphItem.TextItem("本平台提及之聯賽、球隊及組織包含但不限於："),
                ParagraphItem.BulletItem("NBA（美國職業籃球協會）", Icons.Filled.SportsBasketball, Color(0xFFFF9800)),
                ParagraphItem.BulletItem("MLB（美國職業棒球大聯盟）", Icons.Filled.SportsBaseball, Color.Blue),
                ParagraphItem.BulletItem("NPB（日本職業棒球組織）", Icons.Filled.Flag, Color(0xFFFFC107)),
                ParagraphItem.BulletItem("CPBL（中華職業棒球聯盟）", Icons.Filled.Flag, Color.Green),
                ParagraphItem.TextItem("上述聯盟之名稱、商標、標誌、球隊名稱、隊徽、球員姓名及相關智慧財產權，均屬各權利人所有。本平台使用該等資訊僅基於數據分析與資訊呈現目的，不代表與各聯盟有任何合作或隸屬關係。")
            )
        ),
        DisclaimerSection(
            id = "section6",
            icon = Icons.Filled.Star,
            title = "6. Premium 會員與虛擬商品條款",
            color = Color(0xFFFFC107),
            paragraphs = listOf(
                ParagraphItem.TextItem("本平台提供以下付費服務與虛擬商品："),
                ParagraphItem.BulletItem("Premium 會員訂閱服務", Icons.Filled.Star, Color(0xFFFFC107)),
                ParagraphItem.BulletItem("AI 額度儲值中心（虛擬點數）", Icons.Filled.ShoppingBag, Color(0xFFFFC107)),
                ParagraphItem.BulletItem("AI 分析使用額度", Icons.Filled.Info, Color(0xFFFFC107)),
                ParagraphItem.BulletItem("其他數位內容", Icons.Filled.GridOn, Color(0xFFFFC107)),
                ParagraphItem.TextItem("數位商品與虛擬點數一經購買、開通或使用後，除法律另有規定外，不得要求退款、轉讓或兌換現金。"),
                ParagraphItem.TextItem("Premium 會員訂閱將依 Apple App Store 或 Google Play 之訂閱機制進行週期性扣款。使用者可隨時透過帳號設定管理或取消訂閱。取消訂閱後，已付費期間之會員權益仍持續至該期結束。"),
                ParagraphItem.TextItem("本平台保留調整商品價格、內容及權益之權利，變更時將依相關法規進行公告。")
            )
        ),
        DisclaimerSection(
            id = "section7",
            icon = Icons.Filled.WifiOff,
            title = "7. 系統可用性聲明",
            color = Color.Gray,
            paragraphs = listOf(
                ParagraphItem.TextItem("本平台之服務可能因以下因素而中斷、延遲或出現異常："),
                ParagraphItem.BulletItem("網路連線異常", Icons.Filled.WifiOff, Color.Gray),
                ParagraphItem.BulletItem("API 服務故障", Icons.Filled.CloudOff, Color.Gray),
                ParagraphItem.BulletItem("系統定期維護", Icons.Filled.Settings, Color.Gray),
                ParagraphItem.BulletItem("第三方服務中斷", Icons.Filled.LinkOff, Color.Gray),
                ParagraphItem.BulletItem("資料傳輸延遲", Icons.Filled.AccessTime, Color.Gray),
                ParagraphItem.TextItem("上述因素可能導致："),
                ParagraphItem.BulletItem("AI 分析失敗", Icons.Filled.Error, Color(0xFFFF9800)),
                ParagraphItem.BulletItem("推論結果延遲", Icons.Filled.HourglassEmpty, Color(0xFFFF9800)),
                ParagraphItem.BulletItem("賽事資料缺漏", Icons.Filled.SearchOff, Color(0xFFFF9800)),
                ParagraphItem.BulletItem("部分功能暫停", Icons.Filled.PauseCircle, Color(0xFFFF9800)),
                ParagraphItem.TextItem("PredictX Sports 不保證服務全年無中斷運行，亦不就因服務中斷所致之任何損失負賠償責任。本平台將盡合理努力維持服務穩定性，惟不構成保證義務。")
            )
        ),
        DisclaimerSection(
            id = "section8",
            icon = Icons.Filled.Lock,
            title = "8. 智慧財產權聲明",
            color = Color(0xFF4B0082),
            paragraphs = listOf(
                ParagraphItem.TextItem("PredictX Sports 應用程式內之所有內容，包括但不限於："),
                ParagraphItem.BulletItem("使用者介面設計與佈局", Icons.Filled.GridView, Color(0xFF4B0082)),
                ParagraphItem.BulletItem("AI 分析內容與演算法", Icons.Filled.Info, Color(0xFF4B0082)),
                ParagraphItem.BulletItem("應用程式原始碼", Icons.Filled.Code, Color(0xFF4B0082)),
                ParagraphItem.BulletItem("數據圖表與視覺化呈現", Icons.Filled.PieChart, Color(0xFF4B0082)),
                ParagraphItem.BulletItem("文字內容與編排", Icons.Filled.Description, Color(0xFF4B0082)),
                ParagraphItem.TextItem("均受著作權法、商標法及相關智慧財產權法律之保護。"),
                ParagraphItem.TextItem("未經 PredictX Sports 明確書面授權，任何人不得："),
                ParagraphItem.BulletItem("重製、改作或翻譯本平台內容", Icons.Filled.ContentCopy, Color.Red),
                ParagraphItem.BulletItem("散布、公開傳輸或展示", Icons.Filled.Share, Color.Red),
                ParagraphItem.BulletItem("出租、租賃或為商業目的使用", Icons.Filled.ShoppingCart, Color.Red),
                ParagraphItem.BulletItem("逆向工程、解編譯或拆解", Icons.Filled.Build, Color.Red),
                ParagraphItem.TextItem("違反上述規定者，本平台將依法追究相關法律責任。")
            )
        ),
        DisclaimerSection(
            id = "section9",
            icon = Icons.Filled.Security,
            title = "9. 隱私權政策",
            color = Color(0xFF00CED1),
            paragraphs = listOf(
                ParagraphItem.TextItem("PredictX Sports 重視您的隱私權。本節摘要說明我們如何收集、使用及保護您的資料。"),
                ParagraphItem.TextItem("我們收集的資料類型："),
                ParagraphItem.BulletItem("APNs 推播識別碼：您開啟推播時系統產生的 device token", Icons.Filled.Notifications, Color(0xFF00CED1)),
                ParagraphItem.BulletItem("訂閱資訊：透過 Apple StoreKit / Google Play Billing 處理的訂閱等級", Icons.Filled.CreditCard, Color(0xFF00CED1)),
                ParagraphItem.TextItem("我們不會收集的資料："),
                ParagraphItem.BulletItem("個人身分識別資訊（email、姓名、Apple ID 個資）", Icons.Filled.PersonOff, Color.Green),
                ParagraphItem.BulletItem("地理位置資訊", Icons.Filled.LocationOff, Color.Green),
                ParagraphItem.BulletItem("聯絡人、照片或裝置儲存內容", Icons.Filled.Lock, Color.Green),
                ParagraphItem.BulletItem("iOS 版本、機型等裝置資訊", Icons.Filled.PhoneAndroid, Color.Green),
                ParagraphItem.BulletItem("App 內點擊行為、瀏覽歷史、使用模式等使用者行為", Icons.Filled.TouchApp, Color.Green),
                ParagraphItem.TextItem("您的權利：您可隨時透過App設定管理訂閱。"),
                ParagraphItem.LinkItem("查看完整隱私權政策（外部連結）", "https://k06210621-dot.github.io/privacy/"),
                ParagraphItem.LinkItem("查看完整使用條款（Apple 標準 EULA）", "https://www.apple.com/legal/internet-services/itunes/dev/stdeula/")
            )
        ),
        DisclaimerSection(
            id = "section10",
            icon = Icons.Filled.Business,
            title = "10. 法律適用與管轄",
            color = Color(0xFF4B0082),
            paragraphs = listOf(
                ParagraphItem.TextItem("本協議之解釋、效力及爭議解決，均適用中華民國（台灣）法律。"),
                ParagraphItem.TextItem("因本協議所生之任何爭議，雙方應先本於誠信原則協商解決。協商不成時，以台灣台北地方法院為第一審管轄法院。"),
                ParagraphItem.TextItem("若本協議之部分條款被認定為無效或無法執行，不影響其他條款之效力。")
            )
        ),
        DisclaimerSection(
            id = "section11",
            icon = Icons.Filled.Description,
            title = "11. 協議修改與不可抗力",
            color = Color.Gray,
            paragraphs = listOf(
                ParagraphItem.TextItem("協議修改權："),
                ParagraphItem.TextItem("PredictX Sports 保留隨時修改本協議條款之權利。修改後之條款將於 App 更新時生效。使用者繼續使用本服務即視為同意修改後之條款。"),
                ParagraphItem.TextItem("不可抗力免責："),
                ParagraphItem.TextItem("因天災、戰爭、政府行為、疫情、第三方服務中斷、網路攻擊、系統異常等不可抗力因素，導致服務中斷、資料遺失或功能異常，PredictX Sports 不負賠償責任。"),
                ParagraphItem.TextItem("本平台將盡合理努力在不可抗力事件發生後儘速恢復服務，惟不構成保證義務。")
            )
        )
    )
}