package com.predictxsports.android.ui.navigation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue

import androidx.compose.foundation.layout.padding
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.predictxsports.android.ui.analytics.AnalyticsView
import com.predictxsports.android.ui.history.HistoryView
import com.predictxsports.android.ui.home.HomeView
import com.predictxsports.android.ui.home.AIAnalysisDetailView
import com.predictxsports.android.ui.home.HomeViewModel
import com.predictxsports.android.ui.profile.ProfileView
import com.predictxsports.android.ui.profile.SubscribeView
import com.predictxsports.android.ui.profile.AiInfoScreen
import com.predictxsports.android.ui.profile.LegalDisclaimerView
import com.predictxsports.android.ui.profile.HelpView
import com.predictxsports.android.service.BillingViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.foundation.background
import com.predictxsports.android.ui.theme.PredictXTextSize

/**
 * 每個分頁注入專屬運動風格主題色：
 * - 智能分析 → 預測藍
 * - AI 模型驗證 → 金牌黃
 * - 歷史賽事 → 戰績綠
 * - 個人資訊 → 能量橙
 */
private val tabColors = mapOf(
    "home" to TabPalette(accent = Color(0xFF0F4C81), glow = Color(0xFF00E5FF)),
    "analytics" to TabPalette(accent = Color(0xFFD4A843), glow = Color(0xFFFFC857)),
    "history" to TabPalette(accent = Color(0xFF1FBF73), glow = Color(0xFF22D3EE)),
    "profile" to TabPalette(accent = Color(0xFFFF6B35), glow = Color(0xFFFFB37C))
)

private data class TabPalette(val accent: Color, val glow: Color)

@Composable
fun MainTabView(billingViewModel: BillingViewModel? = null) {
    val navController = rememberNavController()
    val screens = listOf(Screen.Home, Screen.Analytics, Screen.History, Screen.Profile)
    val homeViewModel: HomeViewModel = viewModel()
    // Shared BillingViewModel instance (provided by MainActivity for shared prefs persistence)
    val effectiveBilling = billingViewModel ?: viewModel<BillingViewModel>().also {
        val context = LocalContext.current
        LaunchedEffect(it) { it.init(context) }
    }
    val inactiveColor = Color(0xFF8A8A93)

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                screens.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    val palette = tabColors[screen.route] ?: TabPalette(Color(0xFF0F4C81), Color(0xFF00E5FF))
                    val iconScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.25f else 1.0f,
                        animationSpec = spring(dampingRatio = 0.55f, stiffness = 380f),
                        label = "navIconScale"
                    )
                    val contentColor = if (isSelected) palette.accent else inactiveColor
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .then(
                                    if (isSelected) Modifier.background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                palette.accent.copy(alpha = 0.30f),
                                                palette.glow.copy(alpha = 0.10f)
                                            )
                                        ),
                                        shape = RoundedCornerShape(16.dp)
                                    ) else Modifier
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                screen.icon,
                                contentDescription = screen.label,
                                tint = contentColor,
                                modifier = Modifier
                                    .size(26.dp)
                                    .scale(iconScale)
                            )
                        }
                        Text(
                            screen.label,
                            color = contentColor,
                            fontSize = PredictXTextSize.base,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.padding(top = 3.dp)
                        )
                        // 底部 accent 指示條
                        Box(
                            modifier = Modifier
                                .padding(top = 3.dp)
                                .width(if (isSelected) 24.dp else 0.dp)
                                .height(3.dp)
                                .clip(RoundedCornerShape(50))
                                .background(palette.accent)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeView(
                    viewModel = homeViewModel,
                    billingViewModel = effectiveBilling,
                    onMatchClick = { match ->
                        navController.navigate(Screen.AIAnalysisDetail.routeWithId(match.id))
                    }
                )
            }
            composable(Screen.Analytics.route) {
                AnalyticsView(
                    billingViewModel = effectiveBilling,
                    onUpgradeClick = { navController.navigate(Screen.Subscribe.route) }
                )
            }
            composable(Screen.History.route) { HistoryView() }
            composable(Screen.Profile.route) {
                ProfileView(navController = navController, billingViewModel = effectiveBilling)
            }
            composable(Screen.Subscribe.route) {
                SubscribeView(
                    billingViewModel = effectiveBilling,
                    onClose = { navController.popBackStack() }
                )
            }
            composable(Screen.AiInfo.route) {
                AiInfoScreen(
                    onNavigateSubscribe = { navController.navigate(Screen.Subscribe.route) },
                    onClose = { navController.popBackStack() }
                )
            }
            composable(Screen.Legal.route) {
                LegalDisclaimerView(onNavigateBack = { navController.popBackStack() })
            }
            composable(Screen.Help.route) {
                HelpView(onNavigateBack = { navController.popBackStack() })
            }
            composable(
                route = Screen.AIAnalysisDetail.route,
                arguments = listOf(
                    androidx.navigation.navArgument("matchId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val matchId = backStackEntry.arguments?.getString("matchId") ?: ""
                val match = homeViewModel.findMatchById(matchId)
                if (match != null) {
                    AIAnalysisDetailView(
                        match = match,
                        onBack = { navController.popBackStack() }
                    )
                } else {
                    LaunchedEffect(Unit) { navController.popBackStack() }
                }
            }
        }
    }
}