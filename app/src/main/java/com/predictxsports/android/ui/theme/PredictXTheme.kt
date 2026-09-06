package com.predictxsports.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.predictxsports.android.data.model.LeagueType

// ── 聯盟主題色 (iOS LeagueTheme.color(for:)) ──

object LeagueTheme {
    fun color(league: LeagueType): Color = when (league) {
        LeagueType.MLB  -> Color(0.12f, 0.35f, 0.75f)
        LeagueType.NBA  -> Color(0.85f, 0.40f, 0.05f)
        LeagueType.WNBA -> Color(0.75f, 0.20f, 0.50f)
        LeagueType.NPB  -> Color(0.85f, 0.65f, 0.13f)
        LeagueType.CPBL -> Color(0.15f, 0.65f, 0.25f)
    }

    fun gradient(league: LeagueType): Brush {
        val c = color(league)
        return Brush.linearGradient(
            colors = listOf(c, c.copy(alpha = 0.7f))
        )
    }

    fun unselectedBg(league: LeagueType): Color = color(league).copy(alpha = 0.18f)
    fun shadowColor(league: LeagueType): Color = color(league).copy(alpha = 0.25f)
}

// ── 深色運動色盤 (High contrast sports UI) ──

object SportsColors {
    val sportsBackground @Composable get() = MaterialTheme.colorScheme.background

    val cardBackground @Composable get() = MaterialTheme.colorScheme.surface
    val cardSecondaryBackground @Composable get() = MaterialTheme.colorScheme.surfaceVariant

    val energyOrange @Composable get() = Color(0xFFFF6B35)
    val energyCyan @Composable get() = Color(0xFF22D3EE)

    val glassBackground @Composable get() = if (isSystemInDarkTheme()) Color(0x26FFFFFF) else Color(0x1A000000)
    val glassBorder @Composable get() = if (isSystemInDarkTheme()) Color(0x3DFFFFFF) else Color(0x33000000)

    val primaryText @Composable get() = MaterialTheme.colorScheme.onSurface
    val secondaryText @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
    val tertiaryText @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)

    // ── 品牌色（P0-3 顏色 token 化） ──
    val brandPrimary    = Color(0xFF0F4C81)   // 主藍 — 標題、圖示、Focus 框架
    val brandSecondary  = Color(0xFFD4A843)   // 金/棕 — 收藏星星、次要強調
    val brandTertiary   = Color(0xFFFF6B35)   // 活力橘 — 原 energyOrange，同名保留
    val brandCyan       = Color(0xFF22D3EE)   // 活力 Cyan — 原 energyCyan，同名保留

    // ── 語意色（P0-3 顏色 token 化） ──
    val successGreen    = Color(0xFF1FBF73)   // 命中／成功／刷新吐司綠
    val warningOrange   = Color(0xFFE8923B)   // 警告／橘 — 最佳優惠、標籤、通知
    val dangerRed       = Color(0xFFD93B3B)   // 未命中／錯誤／敗 北
    val cyanHighlight   = Color(0xFF00E5FF)   // Cyan 高亮 — 記憶卡、藍色標籤框

    // ── 灰階/中性色（P0-1 補齊） ──
    val textLight       = Color(0xFFD1D5DB)   // 內文淺灰（高對比文字）
    val textMuted       = Color(0xFF9CA3AF)   // 次要文字（白名單 ✅ 中文標籤）
    val textLightGreen  = Color(0xFF6EE7B7)   // 淺綠強調（白名單 ✅ / 包含）

    // ── 互動色（P0-1 補齊） ──
    val actionBlue      = Color(0xFF3B82F6)   // 行動藍（升級按鈕、CTA）

    // ── Tab/導覽色（P0-1 補齊） ──
    val tabGlowYellow   = Color(0xFFFFC857)   // 分析頁 Tab 發光
    val tabGlowOrange   = Color(0xFFFFB37C)   // 個人頁 Tab 發光
    val inactiveText    = Color(0xFF8A8A93)   // 非選中狀態（對齊 iOS .secondaryLabel）

    // ── 圖表色（P0-1 補齊） ──
    val confidenceMid   = Color(0xFFE8C53B)   // 信心度中段（黃）
    val chartLine       = Color(0xFF00A8FF)   // 趨勢線淺藍（專屬）
    val darkBg          = Color(0xFF0F1220)   // 深色背景錨點（用於雷達圖判斷）

    // ── 付費牆專屬背景（P0-1 補齊） ──
    val cardBgDeep      = Color(0xFF1A1A25)   // 付費牆深底
    val cardBgDeepAlt   = Color(0xFF252538)   // 付費牆深底（漸層末）
    val borderSubtle    = Color(0xFF3A3A4D)   // 付費牆細邊框
    val premiumGradientStart = Color(0xFF7C3AED) // 付費牆鎖頭漸層起（紫）
    val premiumGradientEnd   = Color(0xFFA78BFA) // 付費牆鎖頭漸層末（淺紫）

    // ── 漸層色（P0-1 補齊） ──
    val gradientWinStart   = Color(0xFF0EA95F) // 贏漸層起
    val gradientWinEnd     = Color(0xFF16A34A) // 贏漸層末
    val gradientLossStart  = Color(0xFFDC2626) // 輸漸層起
    val gradientLossEnd    = Color(0xFFEF4444) // 輸漸層末
}

// ── 深色運動背景 (iOS SportsDarkBackground) ──

@Composable
fun SportsDarkBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    Surface(
        modifier = modifier.fillMaxSize(),
        color = if (isDark) Color(0xFF0F1220) else Color(0xFFF2F2F7)
    ) {
        content()
    }
}

// ── Material3 Color Scheme ──

private val DarkColors = darkColorScheme(
    primary = Color(0xFF0F4C81),
    secondary = Color(0xFFD4A843),
    background = Color(0xFF0F1220),
    surface = Color(0xFF1C1C1E),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFFF2F2F7),
    onSurface = Color(0xFFF2F2F7)
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF0F4C81),
    primaryContainer = Color(0xFFD6E8FF),
    onPrimaryContainer = Color(0xFF001D3A),
    secondary = Color(0xFFD4A843),
    secondaryContainer = Color(0xFFFFF3D6),
    onSecondaryContainer = Color(0xFF3D2E00),
    tertiary = Color(0xFF4CAF50),
    background = Color(0xFFF2F2F7),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFEAEAEC),
    onSurface = Color(0xFF1C1C1E),
    onSurfaceVariant = Color(0xFF4A4A4C),
    outline = Color(0xFFC6C6C8),
    outlineVariant = Color(0xFFE3E3E3)
)

// ── 字型 (iOS FontStyle 對應) ──

private val PredictXTypography = Typography(
    displayLarge = TextStyle(fontSize = 48.sp, fontWeight = FontWeight.Bold, lineHeight = 56.sp),
    headlineMedium = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.SemiBold, lineHeight = 34.sp),
    titleLarge = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.SemiBold, lineHeight = 28.sp),
    bodyLarge = TextStyle(fontSize = 19.sp, fontWeight = FontWeight.Normal, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal, lineHeight = 22.sp),
    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 18.sp)
)

/**
 * PredictX 標準化字級 token (P0-2 優化)
 * 統一全 App 的字體大小，避免 sp 散落 197 處無法維護
 *
 * 使用方式：
 *   fontSize = PredictXTextSize.md      // 14.sp
 *   fontSize = PredictXTextSize.xl      // 18.sp
 *   或直接讀 sp 值：PredictXTextSize.md.value
 */
object PredictXTextSize {
    val xs = 11.sp       // 圖標輔助文字
    val sm = 12.sp       // 卡片副標題、時間戳
    val base = 13.sp     // 內文小字、列表項
    val md = 14.sp       // 內文
    val lg = 15.sp       // 區塊內文強調
    val xl = 16.sp       // 內容主文
    val xxl = 18.sp      // 區塊標題（19.sp 併入）
    val xxxl = 20.sp     // 卡片主標
    val display = 22.sp  // TopAppBar、頁面大標
    val hero = 28.sp     // 數字儀表板
    val heroXl = 38.sp   // 信心度儀表板大數字
    val heroLg = 40.sp   // 大數字（會員卡點數，42.sp 併入）
    val displayLg = 48.sp // 顯示器級（如驗證率 %）

    // ── 行高 token（P1 字體 token 化） ──
    val lineHeightSm = 20.sp  // 對話框 / 內文短敘述 / 列表項行高
}

// ── 應用主題入口 ──

@Composable
fun PredictXTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = PredictXTypography,
        shapes = androidx.compose.material3.Shapes(
            small = RoundedCornerShape(12.dp),
            medium = RoundedCornerShape(16.dp),
            large = RoundedCornerShape(22.dp)
        ),
        content = content
    )
}
