package com.predictxsports.android.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.predictxsports.android.data.model.LeagueType
import com.predictxsports.android.data.model.Match
import com.predictxsports.android.service.BillingViewModel
import com.predictxsports.android.service.BillingViewModel.UnlockResult
import com.predictxsports.android.service.MembershipTier
import com.predictxsports.android.ui.theme.LeagueTheme
import androidx.compose.material3.MaterialTheme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton

import androidx.compose.ui.draw.shadow

import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.predictxsports.android.ui.theme.PredictXTextSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeView(
    viewModel: HomeViewModel = viewModel(),
    billingViewModel: BillingViewModel = viewModel(),
    onMatchClick: ((Match) -> Unit)? = null
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val focusMatches by viewModel.focusMatches.collectAsState()
    val filteredPredictions by viewModel.filteredPredictions.collectAsState()
    val selectedLeague by viewModel.selectedLeague.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val favoriteMatchIds by billingViewModel.favoriteMatchIds.collectAsState()
    val unlockedIds by billingViewModel.unlockedAnalysisIds.collectAsState()
    val diamonds by billingViewModel.diamonds.collectAsState()
    val tier by billingViewModel.tier.collectAsState()

    val unlockResult by billingViewModel.lastUnlockResult.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()  // P2-1：lifecycle-aware scope，取代 MainScope()

    // 扣點確認彈窗
    var showConfirmDialog by remember { mutableStateOf(false) }
    var matchToConfirm by remember { mutableStateOf<Match?>(null) }

    // 解鎖成功 toast
    var showUnlockToast by remember { mutableStateOf(false) }
    var unlockToastMessage by remember { mutableStateOf("") }

    // 扣點 toast
    var showSpendToast by remember { mutableStateOf(false) }
    var spendToastMessage by remember { mutableStateOf("") }

    // 卡片點擊縮放
    val tapScale = remember { mutableStateMapOf<String, Float>() }

    // 下拉刷新成功/失敗 toast
    var showRefreshToast by remember { mutableStateOf(false) }
    var refreshToastMessage by remember { mutableStateOf("") }
    var refreshToastColor by remember { mutableStateOf(Color(0xFF1FBF73)) }

    // API 載入錯誤提示（一次性 Toast）
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }
    LaunchedEffect(unlockResult) {
        if (unlockResult != null) {
            when (unlockResult) {
                BillingViewModel.UnlockResult.SUCCESS -> {
                    // 解鎖成功 toast
                    unlockToastMessage = "解鎖成功！AI 分析已開啟"
                    showUnlockToast = true
                    // 扣點回饋 toast
                    if (diamonds >= 0) {
                        spendToastMessage = "已花費 20 點分析點數，剩餘 $diamonds 點"
                        showSpendToast = true
                    }
                }
                BillingViewModel.UnlockResult.INSUFFICIENT_POINTS -> {
                    Toast.makeText(context, "點數不足，請升級方案", Toast.LENGTH_SHORT).show()
                }
                BillingViewModel.UnlockResult.TRIAL_EXPIRED -> {
                    Toast.makeText(context, "試用已過期，請升級方案", Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
            billingViewModel.consumeUnlockResult()
        }
    }

    // Toast 自動消失
    LaunchedEffect(showUnlockToast) {
        if (showUnlockToast) {
            kotlinx.coroutines.delay(2000)
            showUnlockToast = false
        }
    }
    LaunchedEffect(showSpendToast) {
        if (showSpendToast) {
            kotlinx.coroutines.delay(2000)
            showSpendToast = false
        }
    }
    LaunchedEffect(showRefreshToast) {
        if (showRefreshToast) {
            kotlinx.coroutines.delay(2000)
            showRefreshToast = false
        }
    }

    // 扣點確認彈窗
    if (showConfirmDialog && matchToConfirm != null) {
        val formattedRemaining = when {
            tier == MembershipTier.STANDARD -> "∞"
            diamonds >= 99999 -> "∞"
            else -> "$diamonds"
        }
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false; matchToConfirm = null },
            title = { Text("同意・扣除 20 點", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "點選同意後將扣除 20 點分析點數查看本場賽事 AI 詳情分析。\n\n目前剩餘：$formattedRemaining 點",
                    fontSize = PredictXTextSize.md,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val match = matchToConfirm
                    showConfirmDialog = false
                    matchToConfirm = null
                    if (match != null) {
                        billingViewModel.unlockMatch(match.id)
                        // 卡片點擊縮放動畫
                        tapScale[match.id] = 0.95f
                        scope.launch {
                            kotlinx.coroutines.delay(120)
                            tapScale[match.id] = 1.0f
                        }
                    }
                }) {
                    Text("同意", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false; matchToConfirm = null }) {
                    Text("取消", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 聯賽篩選（頂部常駐）
        LeagueChips(
            selected = selectedLeague,
            onSelect = { viewModel.setSelectedLeague(it) }
        )

        if (isLoading && filteredPredictions.isEmpty() && focusMatches.isEmpty()) {
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
            ) {
                items(5) {
                    com.predictxsports.android.ui.components.SkeletonCardView(height = 120.dp)
                }
            }
            return@Column
        }

        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = 4.dp,
                    bottom = 88.dp
                ),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // AI 重點觀察賽事（水平卡片列）
                if (focusMatches.isNotEmpty()) {
                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            SectionHeader(
                                title = "AI 重點觀察賽事",
                                icon = Icons.Filled.LocalFireDepartment,
                                iconTint = Color(0xFFE8923B)
                            )
                            Spacer(Modifier.height(6.dp))
                            androidx.compose.foundation.lazy.LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp),
                                userScrollEnabled = true
                            ) {
                                items(focusMatches) { match ->
                                    val isUnlocked = billingViewModel.isUnlocked(match.id)
                                    val isPaid = tier == MembershipTier.STANDARD
                                    CompactPredictionRowView(
                                        match = match,
                                        isLocked = !isUnlocked && !isPaid,
                                        costHint = if (!isPaid) 20 else 0,
                                        isFavorited = favoriteMatchIds.contains(match.id),
                                        canFavorite = true,
                                        onFavoriteToggle = { billingViewModel.toggleFavorite(match) },
                                        onCardClick = if (isUnlocked || isPaid) onMatchClick?.let { {
                                            Log.d("HomeView", "onMatchClick forwarding for ${match.id}")
                                            it(match)
                                        } } else null,
                                        onUnlockTapped = {
                                            // 跳出確認彈窗，而非直接扣點
                                            matchToConfirm = match
                                            showConfirmDialog = true
                                        },
                                        modifier = Modifier.width(300.dp)
                                    )
                                }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(6.dp)) }
                }

                // ${League} AI 數據預報
                item {
                    SectionHeader(
                        title = "${selectedLeague.displayName} AI 數據預報",
                        icon = Icons.Filled.Memory,
                        iconTint = Color(0xFF00E5FF)
                    )
                }
                if (filteredPredictions.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "近期沒有 ${selectedLeague.displayName} 的賽事",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                items(filteredPredictions) { match ->
                    Box(modifier = Modifier.padding(horizontal = 12.dp)) {
                        val isUnlocked = billingViewModel.isUnlocked(match.id)
                        val scale by animateFloatAsState(
                            targetValue = tapScale[match.id] ?: 1.0f,
                            animationSpec = spring(dampingRatio = 0.6f, stiffness = 380f),
                            label = "cardScale"
                        )
                        Box(modifier = Modifier.graphicsLayer { scaleX = scale; scaleY = scale }) {
                        PredictionRowView(
                            match = match,
                            isLocked = !isUnlocked,
                            costHint = 20,
                            isFavorited = favoriteMatchIds.contains(match.id),
                            canFavorite = true,
                            onFavoriteToggle = { billingViewModel.toggleFavorite(match) },
                            onCardClick = if (isUnlocked && onMatchClick != null) {
                                {
                                    tapScale[match.id] = 0.95f
                                    scope.launch {
                                        kotlinx.coroutines.delay(120)
                                        tapScale[match.id] = 1.0f
                                    }
                                    onMatchClick(match)
                                }
                            } else null,
                            onUnlockTapped = {
                                // 跳出確認彈窗，而非直接扣點
                                matchToConfirm = match
                                showConfirmDialog = true
                            }
                        )
                        }
                    }
                }
            }
        }

        if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = null,
                            tint = Color(0xFFE8923B),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    androidx.compose.material3.TextButton(
                        onClick = { viewModel.refresh() }
                    ) {
                        Text("重新載入", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            return@Column
        }
    }

    // 解鎖成功 toast overlay
    if (showUnlockToast) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            androidx.compose.material3.Surface(
                shape = RoundedCornerShape(50),
                color = Color(0xFF1FBF73).copy(alpha = 0.95f),
                shadowElevation = 8.dp,
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color.White, RoundedCornerShape(50)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF1FBF73), modifier = Modifier.size(20.dp))
                    }
                    Text(unlockToastMessage, fontSize = PredictXTextSize.md, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }
        }
    }

    // 扣點回饋 toast overlay
    if (showSpendToast) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 160.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            androidx.compose.material3.Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.Memory, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(18.dp))
                    Text(spendToastMessage, fontSize = PredictXTextSize.base, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }

    // 下拉刷新 toast overlay
    if (showRefreshToast) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            androidx.compose.material3.Surface(
                shape = RoundedCornerShape(50),
                color = refreshToastColor.copy(alpha = 0.95f),
                shadowElevation = 8.dp
            ) {
                Text(
                    refreshToastMessage,
                    fontSize = PredictXTextSize.base,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 16.dp, top = 10.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 圖示左側淺藍垂直條
        Box(
            modifier = Modifier
                .width(5.dp)
                .height(22.dp)
                .background(Color(0xFF00E5FF).copy(alpha = 0.85f))
        )
        Spacer(Modifier.width(10.dp))
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = PredictXTextSize.xxl,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun LeagueChips(
    selected: LeagueType,
    onSelect: (LeagueType) -> Unit
) {
    // 跑馬燈：使用者手動滑動時暫停自動捲動
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    var isUserInteracting by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    val itemCount = LeagueType.activeCases.size

    androidx.compose.runtime.LaunchedEffect(listState) {
        while (true) {
            if (!isUserInteracting) {
                val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()
                val totalSize = listState.layoutInfo.totalItemsCount
                if (lastVisible != null && lastVisible.index < totalSize - 1) {
                    listState.animateScrollToItem(
                        index = (lastVisible.index + 1).coerceAtMost(totalSize - 1),
                        scrollOffset = 0
                    )
                } else {
                    listState.animateScrollToItem(0)
                }
            }
            delay(1800) // 每個位置暫停 1.8 秒
        }
    }

    LazyRow(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp)
    ) {
        items(LeagueType.activeCases) { league ->
            val color = LeagueTheme.color(league)
            val isSelected = league == selected
            Box(
                modifier = Modifier
                    .then(
                        if (isSelected) Modifier
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(50),
                                ambientColor = color.copy(alpha = 0.6f),
                                spotColor = color.copy(alpha = 0.4f)
                            )
                        else Modifier
                    )
                    .background(
                        color = if (isSelected) color else color.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(50)
                    )
                    .clickable {
                        isUserInteracting = true
                        coroutineScope.launch {
                            listState.animateScrollToItem(index = LeagueType.activeCases.indexOf(league))
                            kotlinx.coroutines.delay(2500)
                            isUserInteracting = false
                        }
                        onSelect(league)
                    }
                    .pointerInput(league) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                if (event.changes.any { it.pressed }) {
                                    isUserInteracting = true
                                }
                            }
                        }
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = league.displayName,
                    fontSize = PredictXTextSize.base,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White else color
                )
            }
        }
    }
}