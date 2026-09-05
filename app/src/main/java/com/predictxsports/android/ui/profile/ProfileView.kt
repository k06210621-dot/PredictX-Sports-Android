package com.predictxsports.android.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Restore

import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf

import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavHostController
import com.predictxsports.android.R
import com.predictxsports.android.service.BillingViewModel
import com.predictxsports.android.service.FavoriteEntry
import com.predictxsports.android.service.MembershipTier
import com.predictxsports.android.service.toMatch
import com.predictxsports.android.ui.home.PredictionRowView
import com.predictxsports.android.ui.navigation.Screen
import com.predictxsports.android.ui.theme.ThemeController
import com.predictxsports.android.ui.theme.ThemeRevealController
import com.predictxsports.android.ui.theme.ThemeRevealOverlay
import com.predictxsports.android.ui.theme.PredictXTextSize
import com.predictxsports.android.ui.theme.SportsColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileView(
    viewModel: BillingViewModel = viewModel(),
    navController: NavHostController? = null,
    billingViewModel: BillingViewModel? = null
) {
    val effectiveViewModel = billingViewModel ?: viewModel
    val tier by effectiveViewModel.tier.collectAsState()
    val diamonds by effectiveViewModel.diamonds.collectAsState()
    val trialExpired by effectiveViewModel.trialExpired.collectAsState()
    val trialDaysRemaining by effectiveViewModel.trialDaysRemaining.collectAsState()
    val favoriteMatchIds by effectiveViewModel.favoriteMatchIds.collectAsState()
    val canUseFavorites by effectiveViewModel.canUseFavorites.collectAsState()

    var isDarkMode by rememberSaveable { mutableStateOf(ThemeController.isDark) }
    var pushEnabled by rememberSaveable { mutableStateOf(false) }
    var showFavorites by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(isDarkMode) {
        if (isDarkMode != ThemeController.isDark) {
            ThemeController.setDarkTheme(isDarkMode)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        androidx.compose.foundation.Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = "PredictX LOGO",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                        )
                        Text(
                            "個人資訊",
                            fontWeight = FontWeight.Bold,
                            fontSize = PredictXTextSize.display,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    Box(modifier = Modifier.clickable {
                        ThemeRevealController.reveal()
                    }) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Filled.Brightness7 else Icons.Filled.Brightness4,
                            contentDescription = if (isDarkMode) "切換亮色模式" else "切換暗色模式",
                            tint = SportsColors.brandSecondary
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

            MembershipCardView(
                tier = tier,
                diamonds = diamonds,
                trialExpired = trialExpired,
                trialDaysRemaining = trialDaysRemaining,
                onUpgradeClick = { navController?.navigate(Screen.Subscribe.route) }
            )

            Box(modifier = Modifier.clickable { navController?.navigate(Screen.Subscribe.route) }) {
                ProfileMenuRow(
                    iconVector = Icons.Filled.WorkspacePremium,
                    iconColor = SportsColors.brandSecondary,
                    title = "訂閱中心",
                    subtitle = "管理您的訂閱方案"
                )
            }

            AiQuotaCard(
                tier = tier,
                diamonds = diamonds,
                trialExpired = trialExpired,
                trialDaysRemaining = trialDaysRemaining,
                onInfoClick = { navController?.navigate(Screen.AiInfo.route) }
            )
            if (canUseFavorites) {
                Box(
                    modifier = Modifier
                        .clickable { showFavorites = !showFavorites }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("AI 推論分析收藏", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                when {
                                    favoriteMatchIds.isEmpty() -> "尚無收藏資料"
                                    else -> "${ favoriteMatchIds.size } 筆收藏 / 上限 50"
                                },
                                fontSize = PredictXTextSize.sm,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = if (showFavorites) Icons.Filled.KeyboardArrowDown else Icons.Filled.ChevronRight,
                            contentDescription = if (showFavorites) "收合" else "展開",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (showFavorites && canUseFavorites) {
                FavoritesListView(
                    viewModel = effectiveViewModel,
                    onNavigateHome = { navController?.navigate(Screen.Home.route) }
                )
            }

            Box(modifier = Modifier.clickable { navController?.navigate(Screen.Help.route) }) {
                ProfileMenuRow(
                    iconVector = Icons.AutoMirrored.Filled.Help,
                    iconColor = SportsColors.successGreen,
                    title = "客服中心",
                    subtitle = "常見問題、意見回饋、聯絡客服"
                )
            }

            Box(modifier = Modifier.clickable { effectiveViewModel.restorePurchases() }) {
                ProfileMenuRow(
                    iconVector = Icons.Filled.Restore,
                    iconColor = SportsColors.brandPrimary,
                    title = "恢復購買",
                    subtitle = "恢復您之前的訂閱或購買項目"
                )
            }

            Box(modifier = Modifier.clickable { navController?.navigate(Screen.Legal.route) }) {
                ProfileMenuRow(
                    iconVector = Icons.Filled.Description,
                    iconColor = Color.Gray,
                    title = "法律聲明",
                    subtitle = "數據來源與研究性質聲明"
                )
            }

            ProfileMenuRow(
                iconVector = Icons.Filled.Info,
                iconColor = Color.Gray,
                title = "APP 版本資訊",
                subtitle = "v1.0.0"
            )
        }
        }  // close Column
        ThemeRevealOverlay(
            onPeak = { isDarkMode = !isDarkMode }
        )
    }
}

@Composable
fun FavoritesListView(
    viewModel: BillingViewModel,
    onNavigateHome: () -> Unit
) {
    val favorites by viewModel.favoriteMatches.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (favorites.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(
                    "尚無收藏資料。請在智能分析內比賽卡片右上角點擊星星收藏。",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = PredictXTextSize.base
                )
            }
        } else {
            favorites.forEach { entry ->
                val match = entry.toMatch()
                PredictionRowView(
                    match = match,
                    isLocked = false,
                    costHint = 0,
                    isFavorited = true,
                    canFavorite = true,
                    onFavoriteToggle = { viewModel.toggleFavorite(match) },
                    onCardClick = { onNavigateHome() }
                )
            }
        }
    }
}

@Composable
private fun MembershipCardView(
    tier: MembershipTier,
    diamonds: Int,
    trialExpired: Boolean,
    trialDaysRemaining: Int,
    onUpgradeClick: () -> Unit = {}
) {
    // 🆕 P0 優化：FREE 卡片改為紫色發光漸層（強化 CTA 視覺）
    val colors = when (tier) {
        MembershipTier.STANDARD -> listOf(Color(0xFF3366CC), Color(0xFF4D80E6))
        MembershipTier.BASIC -> listOf(Color(0xFF4CAF50), Color(0xFF66BB6A))
        MembershipTier.FREE -> listOf(Color(0xFF4C1D95), Color(0xFF7C3AED), Color(0xFFA78BFA))
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (tier == MembershipTier.FREE) Modifier.border(
                    BorderStroke(2.dp, Color(0xFFA78BFA)),
                    RoundedCornerShape(16.dp)
                ) else Modifier
            ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(colors), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (tier == MembershipTier.FREE && !trialExpired) {
                                Text("✨ ", fontSize = PredictXTextSize.xxxl)
                            }
                            Text(tier.rawValue, color = Color.White, fontWeight = FontWeight.Bold, fontSize = PredictXTextSize.xxxl)
                        }
                        Text(
                            when {
                                tier == MembershipTier.FREE && trialExpired -> "試用已過期・請升級方案"
                                tier == MembershipTier.FREE -> "試用期剩餘 $trialDaysRemaining 天"
                                else -> "訂閱中・已解鎖所有權限"
                            },
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = PredictXTextSize.sm
                        )
                        if (tier == MembershipTier.FREE && !trialExpired) {
                            Text("每日 AI 分析點數補滿 60 點", color = Color.White.copy(alpha = 0.8f), fontSize = PredictXTextSize.base)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                when (tier) {
                                    MembershipTier.FREE -> "$diamonds"
                                    MembershipTier.BASIC -> if (diamonds >= 99999) "∞" else "$diamonds"
                                    MembershipTier.STANDARD -> "∞"
                                },
                                color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = PredictXTextSize.heroLg
                            )
                            Text("分析點數", color = Color.White.copy(alpha = 0.8f), fontSize = PredictXTextSize.base)
                        }
                    }
                }

                // 🆕 P0 優化：FREE 狀態顯示「立即升級」CTA（iOS 對齊）
                if (tier == MembershipTier.FREE) {
                    Spacer(Modifier.height(14.dp))
                    androidx.compose.material3.Button(
                        onClick = onUpgradeClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF97316)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            "立即升級 · 解鎖無限分析  →",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = PredictXTextSize.lg
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AiQuotaCard(
    tier: MembershipTier,
    diamonds: Int,
    trialExpired: Boolean,
    trialDaysRemaining: Int,
    onInfoClick: () -> Unit
) {
    val subtitle = when (tier) {
        MembershipTier.FREE -> if (!trialExpired) "試用期剩餘 $trialDaysRemaining 天・剩餘 $diamonds 點" else "剩餘 $diamonds 點（試用已過期）"
        MembershipTier.BASIC -> if (diamonds >= 99999) "無限觀看" else "今天剩餘 $diamonds 點・每天 +120"
        MembershipTier.STANDARD -> "無限觀看"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onInfoClick() }
            .border(
                width = 0.5.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f),
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.0f)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                SportsColors.brandPrimary.copy(alpha = 0.20f),
                                SportsColors.brandPrimary.copy(alpha = 0.08f)
                            )
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = 0.5.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                SportsColors.brandPrimary.copy(alpha = 0.45f),
                                SportsColors.brandPrimary.copy(alpha = 0.15f)
                            )
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Memory,
                    contentDescription = null,
                    tint = SportsColors.brandPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text("AI 使用額度", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    subtitle,
                    fontSize = PredictXTextSize.sm,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ProfileMenuRow(
    iconVector: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    action: (@Composable () -> Unit)? = { Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            .border(
                width = 0.5.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f),
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.0f)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 美化 icon 容器：漸層背景 + 圓角 + 細光澤邊框
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            iconColor.copy(alpha = 0.20f),
                            iconColor.copy(alpha = 0.08f)
                        )
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                .border(
                    width = 0.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            iconColor.copy(alpha = 0.45f),
                            iconColor.copy(alpha = 0.15f)
                        )
                    ),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = iconVector,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, fontSize = PredictXTextSize.sm, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        action?.invoke()
    }
}

