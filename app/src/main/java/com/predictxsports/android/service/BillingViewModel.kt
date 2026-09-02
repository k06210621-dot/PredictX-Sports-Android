package com.predictxsports.android.service

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams

import com.predictxsports.android.data.model.LeagueType
import com.predictxsports.android.data.model.Match
import com.predictxsports.android.data.model.MatchStatus

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import java.util.Calendar

/**
 * BillingViewModel - maps to iOS SubscriptionManager.swift
 *
 * Google Play Billing 6 migration:
 * - StoreKit 2 product.purchase() -> BillingClient.launchBillingFlow()
 * - Transaction.currentEntitlements -> queryPurchasesAsync()
 * - AppStore.sync() -> queryPurchasesAsync() (restore)
 * - UserDefaults -> SharedPreferences
 * - MembershipTier enum direct port
 */
enum class MembershipTier(val rawValue: String) {
    FREE("Free Trial"),
    BASIC("Basic"),
    STANDARD("Standard")
}
@Serializable
data class FavoriteEntry(
    val id: String,
    val league: String,
    val homeTeam: String,
    val awayTeam: String,
    val homeTeamCN: String,
    val awayTeamCN: String,
    val homeScore: Int? = null,
    val awayScore: Int? = null,
    val matchDate: Long = 0L,
    val aiConfidence: Double? = null,
    val aiHomeProb: Double? = null,
    val aiTotalScorePredict: String? = null,
    val savedAt: Long
)

class BillingViewModel : ViewModel() {

    // State (corresponds to @Published)
    private val _tier = MutableStateFlow(MembershipTier.FREE)
    val tier: StateFlow<MembershipTier> = _tier

    private val _diamonds = MutableStateFlow(0)
    val diamonds: StateFlow<Int> = _diamonds

    private val _diamondDailyCap = MutableStateFlow(60)
    /**
     * 每日點數上限 — 對外只讀 API（保留擴充彈性，未來設定頁可能顯示）
     * 若 IDE 標示 unused，可安全忽略：因為內部 save/load 仍用 [MutableStateFlow]
     */
    @Suppress("unused")
    val diamondDailyCap: StateFlow<Int> = _diamondDailyCap

    private val _unlockedAnalysisIds = MutableStateFlow<Set<String>>(emptySet())
    val unlockedAnalysisIds: StateFlow<Set<String>> = _unlockedAnalysisIds

    // Unlock events (for UI toast / navigation)
    enum class UnlockResult { SUCCESS, INSUFFICIENT_POINTS, TRIAL_EXPIRED }
    private val _lastUnlockResult = MutableStateFlow<UnlockResult?>(null)
    val lastUnlockResult: StateFlow<UnlockResult?> = _lastUnlockResult

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing

    // Favorites storage: List<FavoriteEntry> (id + metadata), sorted by savedAt
    private val _favoriteMatches = MutableStateFlow<List<FavoriteEntry>>(emptyList())
    val favoriteMatches: StateFlow<List<FavoriteEntry>> = _favoriteMatches

    // Derived Set for UI query (isFavorited)
    val favoriteMatchIds: StateFlow<Set<String>> =
        MutableStateFlow<Set<String>>(emptySet()).also { sf ->
            viewModelScope.launch {
                _favoriteMatches.collect { list ->
                    sf.value = list.map { it.id }.toSet()
                }
            }
        }.asStateFlow()

    val canUseFavorites: StateFlow<Boolean> = _tier
        .map { it != MembershipTier.FREE }
        .stateIn(viewModelScope, SharingStarted.Eagerly, _tier.value != MembershipTier.FREE)

    /**
     * Toggle favorite (paid members only).
     * When limit 50 reached, FIFO removes oldest entry by savedAt.
     * 對應 iOS FavoritesStore.toggle(match:)
     */
    fun toggleFavorite(match: Match) {
        if (_tier.value == MembershipTier.FREE) return
        val now = System.currentTimeMillis()
        val current = _favoriteMatches.value.toMutableList()
        val existingIdx = current.indexOfFirst { it.id == match.id }
        if (existingIdx >= 0) {
            current.removeAt(existingIdx)
        } else {
            while (current.size >= MAX_FAVORITES) {
                current.removeAt(0)
            }
            current.add(
                FavoriteEntry(
                    id = match.id,
                    league = match.league.rawValue,
                    homeTeam = match.homeTeam,
                    awayTeam = match.awayTeam,
                    homeTeamCN = match.homeTeamCN,
                    awayTeamCN = match.awayTeamCN,
                    homeScore = match.homeScore,
                    awayScore = match.awayScore,
                    matchDate = match.startTime,
                    aiConfidence = match.aiConfidence,
                    aiHomeProb = match.aiWinRateHome,
                    aiTotalScorePredict = match.aiTotalScorePredict,
                    savedAt = now
                )
            )
        }
        _favoriteMatches.value = current
        save()
    }

    /**
     * 單筆查詢某賽事是否已被收藏（Helper API）。
     * 若使用 [favoriteMatchIds] StateFlow 已足夠可忽略此方法；保留以提供 O(n) 對 O(1) 選擇彈性。
     */
    @Suppress("unused")
    fun isFavorited(matchId: String): Boolean = _favoriteMatches.value.any { it.id == matchId }

    /**
     * Get list sorted by savedAt descending (newest first), for FavoritesListView.
     */
    fun favoriteMatchesSorted(): List<FavoriteEntry> =
        _favoriteMatches.value.sortedByDescending { it.savedAt }

    companion object {
        const val MAX_FAVORITES = 50
    }

    private val _showSubscribeView = MutableStateFlow(false)
    val showSubscribeView: StateFlow<Boolean> = _showSubscribeView

    private val _lastPurchaseError = MutableStateFlow<String?>(null)
    val lastPurchaseError: StateFlow<String?> = _lastPurchaseError

    private val _lastPurchaseSucceeded = MutableStateFlow(false)
    val lastPurchaseSucceeded: StateFlow<Boolean> = _lastPurchaseSucceeded

    // Trial period
    private val _trialStartDate = MutableStateFlow<Long?>(null)
    val trialStartDate: StateFlow<Long?> = _trialStartDate

    private val _trialDaysRemaining = MutableStateFlow(30)
    val trialDaysRemaining: StateFlow<Int> = _trialDaysRemaining

    private val _trialExpired = MutableStateFlow(false)
    val trialExpired: StateFlow<Boolean> = _trialExpired

    val diamondCostPerAnalysis: Int = 20
    val trialDurationDays: Int = 30

    private val _productDetails = MutableStateFlow<List<ProductDetails>>(emptyList())
    val productDetails: StateFlow<List<ProductDetails>> = _productDetails

    // Product IDs (Google Play Console format)
    // P0-1 fix: use BillingManager.SUBSCRIPTION_SKUS to avoid naming inconsistency
    val allProductIDs: List<String> get() = BillingManager.SUBSCRIPTION_SKUS

    private lateinit var prefs: SharedPreferences
    private val favoritesJson = Json { ignoreUnknownKeys = true }

    // Init (idempotent, safe to call multiple times)
    // P1-4 fix: init() is main thread sync (called from Activity.onCreate),
    // but SharedPreferences first read usually < 5ms, triggers background load.
    // loadFromPrefs runs on IO dispatcher to avoid blocking startup.
    fun init(context: Context) {
        if (!::prefs.isInitialized) {
            prefs = context.getSharedPreferences("predictx_billing", Context.MODE_PRIVATE)
            // Sync init once (ensure getString has defaults)
            loadFromPrefsSync()
            // Trial must start BEFORE querying existing purchases,
            // so async callback reads correct FREE tier default state
            // 修復：只在 FREE 且無 trial_start 時才 startTrial，
            // 避免 STANDARD/BASIC 訂閱者被 startTrial 覆寫回 FREE
            if (_trialStartDate.value == null && _tier.value == MembershipTier.FREE) startTrial()
            // Check on every app launch / resume (fixes daily reset issue)
            checkDailyReset()
            // Then background full load (handles favorites JSON parsing)
            viewModelScope.launch(Dispatchers.IO) {
                loadFromPrefs()
            }
            setupBillingClient(context)
        } else {
            // Already initialized (e.g. activity recreate)
            if (_trialStartDate.value == null && _tier.value == MembershipTier.FREE) startTrial()
            checkDailyReset()
        }
    }

    /** Called when app returns from background (fixes cross-day reset issue) */
    fun onAppResume() {
        android.util.Log.d("BillingViewModel", "onAppResume: checking daily reset (diamonds=${_diamonds.value}, lastReset=${prefs.getLong("last_diamond_reset", 0L)})")
        if (::prefs.isInitialized) {
            checkDailyReset()
        }
    }

    // P0-1 fix: no longer create own BillingClient, use BillingManager.
    // BillingManager initialized in Application.onCreate, here only register listener.
    private fun setupBillingClient(context: Context) {
        BillingManager.setPurchaseListener(object : BillingManager.PurchaseListener {
            override fun onPurchaseCompleted(productId: String) {
                viewModelScope.launch {
                    applyTier(productId)
                    _lastPurchaseSucceeded.value = true
                }
            }
        })
        // Use BillingManager's BillingClient for product queries
        val client = BillingManager.getBillingClient()
        if (client != null && client.isReady) {
            queryProducts()
            queryExistingPurchases()
        }
    }

    // Product query
    private fun queryProducts() {
        val client = BillingManager.getBillingClient() ?: return
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                allProductIDs.map { id ->
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(id)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                }
            ).build()
        client.queryProductDetailsAsync(params) { billingResult, queryProductDetailsResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                // productDetailsList 非空且永不為 null，elvis 分支為 dead code
                val list: List<ProductDetails> = queryProductDetailsResult.productDetailsList.orEmpty()
                _productDetails.value = list
            }
        }
    }

    // Query existing purchases (restore)
    private fun queryExistingPurchases() {
        val client = BillingManager.getBillingClient() ?: return
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
        client.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                @Suppress("UNCHECKED_CAST")
                val list = (purchases ?: emptyList()) as List<Purchase>
                var foundValid = false
                list.forEach { purchase ->
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        applyTier(purchase.products.first())
                        if (!purchase.isAcknowledged) acknowledgePurchase(purchase.purchaseToken)
                        foundValid = true
                    }
                }
                if (!foundValid && _tier.value != MembershipTier.FREE) {
                    restoreFreeTier("restore_no_valid_transaction")
                }
            } else {
                // No valid purchases found
            }
        }
    }

    private fun acknowledgePurchase(token: String) {
        val client = BillingManager.getBillingClient() ?: return
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(token)
            .build()
        client.acknowledgePurchase(params) {}
    }

    // Purchase flow
    // P0-2 fix: use BillingManager.launchPurchaseFlow() to ensure offerToken passed correctly
    fun purchase(productDetails: ProductDetails, activity: android.app.Activity) {
        viewModelScope.launch {
            _isProcessing.value = true
            _lastPurchaseError.value = null
            _lastPurchaseSucceeded.value = false
            BillingManager.launchPurchaseFlow(activity, productDetails.productId)
            _isProcessing.value = false
        }
    }

    fun restorePurchases() {
        viewModelScope.launch {
            _isProcessing.value = true
            queryExistingPurchases()
            _isProcessing.value = false
            _lastPurchaseSucceeded.value = true
        }
    }

    // Trial management
    private fun startTrial() {
        _trialStartDate.value = System.currentTimeMillis()
        _diamonds.value = 60
        _diamondDailyCap.value = 60
        _tier.value = MembershipTier.FREE
        _trialDaysRemaining.value = trialDurationDays
        _trialExpired.value = false
        save()
    }

    private fun checkTrialExpiry() {
        val start = _trialStartDate.value ?: return
        if (_tier.value != MembershipTier.FREE) return
        val elapsed = daysBetween(start, System.currentTimeMillis())
        _trialDaysRemaining.value = maxOf(0, trialDurationDays - elapsed.toInt())
        if (elapsed >= trialDurationDays) {
            _trialExpired.value = true
            _diamonds.value = 0
            save()
        }
    }

    // Daily reset
    // P0-2 fix: 嚴格用「精準時間戳」比對，避免 lastReset 跨日重置後又被誤判
    private fun checkDailyReset() {
        val today = startOfDay(System.currentTimeMillis())
        val lastReset = prefs.getLong("last_diamond_reset", 0L)
        // 只有在「今天已經補滿過」才 return
        // lastReset == 0 表示從未補滿或被 applyTier 重置，必須執行補滿
        if (lastReset != 0L && lastReset == today) return

        checkTrialExpiry()
        when (_tier.value) {
            MembershipTier.FREE -> {
                _diamonds.value = if (_trialExpired.value) 0 else 60
            }
            MembershipTier.BASIC -> _diamonds.value += 120
            MembershipTier.STANDARD -> _diamonds.value = Int.MAX_VALUE
        }
        prefs.edit().putLong("last_diamond_reset", today).apply()
        save()
    }

    // Analysis points
    fun canWatchAnalysis(): Boolean {
        return when (_tier.value) {
            MembershipTier.STANDARD -> true
            MembershipTier.BASIC -> _diamonds.value >= diamondCostPerAnalysis
            MembershipTier.FREE -> if (_trialExpired.value) false else _diamonds.value >= diamondCostPerAnalysis
        }
    }

    fun spendDiamond(): Boolean {
        if (!canWatchAnalysis()) {
            if (_tier.value == MembershipTier.FREE && _trialExpired.value) {
                _showSubscribeView.value = true
            }
            return false
        }
        when (_tier.value) {
            MembershipTier.STANDARD -> return true
            MembershipTier.BASIC, MembershipTier.FREE -> {
                _diamonds.value -= diamondCostPerAnalysis
                save()
                return true
            }
        }
    }

    fun unlockAnalysis(gameId: String) {
        _unlockedAnalysisIds.value = _unlockedAnalysisIds.value + gameId
        save()
    }

    /**
     * One-shot: spend 20 points + add to unlock list. Returns UnlockResult for UI CTA.
     */
    fun unlockMatch(gameId: String): UnlockResult {
        if (isUnlocked(gameId)) {
            _lastUnlockResult.value = UnlockResult.SUCCESS
            android.util.Log.d("BillingViewModel", "unlockMatch: already unlocked $gameId")
            return UnlockResult.SUCCESS
        }
        if (_tier.value == MembershipTier.STANDARD) {
            unlockAnalysis(gameId)
            _lastUnlockResult.value = UnlockResult.SUCCESS
            android.util.Log.d("BillingViewModel", "unlockMatch: tier unlock $gameId")
            return UnlockResult.SUCCESS
        }
        // FREE/BASIC check trial first
        if (_tier.value == MembershipTier.FREE && _trialExpired.value) {
            _lastUnlockResult.value = UnlockResult.TRIAL_EXPIRED
            _showSubscribeView.value = true
            android.util.Log.d("BillingViewModel", "unlockMatch: trialExpired $gameId")
            return UnlockResult.TRIAL_EXPIRED
        }
        android.util.Log.d("BillingViewModel", "unlockMatch: diamonds=${_diamonds.value}, cost=$diamondCostPerAnalysis, gameId=$gameId")
        if (_diamonds.value >= diamondCostPerAnalysis) {
            _diamonds.value -= diamondCostPerAnalysis
            unlockAnalysis(gameId)
            _lastUnlockResult.value = UnlockResult.SUCCESS
            save()
            android.util.Log.d("BillingViewModel", "unlockMatch: SUCCESS, new balance=${_diamonds.value}")
            return UnlockResult.SUCCESS
        }
        android.util.Log.d("BillingViewModel", "unlockMatch: INSUFFICIENT_POINTS, diamonds=${_diamonds.value}")
        _lastUnlockResult.value = UnlockResult.INSUFFICIENT_POINTS
        return UnlockResult.INSUFFICIENT_POINTS
    }

    fun consumeUnlockResult() {
        _lastUnlockResult.value = null
    }

    fun isUnlocked(gameId: String): Boolean {
        return when (_tier.value) {
            MembershipTier.STANDARD -> true
            MembershipTier.BASIC, MembershipTier.FREE -> _unlockedAnalysisIds.value.contains(gameId)
        }
    }

    fun canSeeHighConfidence(): Boolean = _tier.value == MembershipTier.STANDARD

    // productID -> tier mapping
    // P1-2 fix: prefix check instead of string replace
    // Play Console SKU format: predictx_<basic|standard|premium>_<monthly|yearly|annual>
    // P2-5 fix: use MutableStateFlow.update {} for atomic read/write
    // 2026-08-26 fix: 重置 _diamonds + per-tier daily cap，避免切換方案時 Int.MAX_VALUE 殘留
    private fun applyTier(productID: String) {
        android.util.Log.d("BillingViewModel", "applyTier called with productID=$productID")
        val tier = when {
            productID.startsWith("predictx_basic_") -> TierMapping.BASIC
            productID.startsWith("predictx_standard_") -> TierMapping.STANDARD
            else -> null
        }
        if (tier != null) {
            val prevTier = _tier.value
            _tier.update { tier.tier }
            // 每個 tier 對應自己的 dailyCap（避免切換時殘留 Int.MAX_VALUE）
            _diamondDailyCap.update { tier.dailyCap }
            // 切換 tier 時重置 _diamonds 為新 tier 的初始值
            // 這樣即使之前是 STANDARD (Int.MAX_VALUE)，切到 BASIC 也會重置為 0
            // 然後下一次 checkDailyReset() 會為 BASIC 補滿每日 +120
            _diamonds.update {
                when {
                    tier.unlimitedDiamonds -> Int.MAX_VALUE
                    prevTier != tier.tier -> 0  // 從其他 tier 切換過來 → 從 0 開始（下次 checkDailyReset 補滿）
                    else -> it  // 同 tier 內 ack 二次 → 保留現值
                }
            }
            _trialExpired.update { false }
            // 2026-08-26 fix: tier 切換時強制重置 lastReset，讓 checkDailyReset 補滿對應 tier 的點數
            // 避免「購買後立即看到 0 點」的不直覺狀態
            if (prevTier != tier.tier) {
                prefs.edit().putLong("last_diamond_reset", 0L).apply()
            }
            android.util.Log.d("BillingViewModel", "applyTier: productID=$productID prev=$prevTier new=${tier.tier} diamonds=${_diamonds.value}")
        } else {
            android.util.Log.w("BillingViewModel", "applyTier: unknown productID=$productID")
        }
        save()
    }

    private enum class TierMapping(
        val tier: MembershipTier,
        val unlimitedDiamonds: Boolean,
        val dailyCap: Int
    ) {
        BASIC(MembershipTier.BASIC, false, 120),
        STANDARD(MembershipTier.STANDARD, true, Int.MAX_VALUE)
    }

    private fun restoreFreeTier(reason: String) {
        _tier.value = MembershipTier.FREE
        _diamondDailyCap.value = 60
        val start = _trialStartDate.value
        if (start != null) {
            val elapsed = daysBetween(start, System.currentTimeMillis())
            if (elapsed >= trialDurationDays) {
                _trialExpired.value = true
                _trialDaysRemaining.value = 0
                _diamonds.value = 0
            } else {
                _trialExpired.value = false
                _trialDaysRemaining.value = maxOf(0, trialDurationDays - elapsed.toInt())
                _diamonds.value = 60
            }
        } else {
            _trialExpired.value = true
            _trialDaysRemaining.value = 0
            _diamonds.value = 0
        }
        _unlockedAnalysisIds.value = emptySet()
        save()
    }

    // Persistence
    /**
     * P1-4: sync load core fields at init (< 5ms).
     * SharedPreferences first read triggers background load, subsequent reads fast.
     */
    private fun loadFromPrefsSync() {
        val tierRaw = prefs.getString("membership_tier", null)
        android.util.Log.d("BillingViewModel", "loadFromPrefsSync: tierRaw=$tierRaw")
        _tier.value = MembershipTier.values().find { it.rawValue == tierRaw } ?: MembershipTier.FREE
        _diamonds.value = prefs.getInt("diamonds", 0)
        _diamondDailyCap.value = prefs.getInt("diamond_cap", 0).let { if (it == 0) 60 else it }
        _unlockedAnalysisIds.value = prefs.getStringSet("unlocked_analyses", emptySet()) ?: emptySet()
        _trialStartDate.value = prefs.getLong("trial_start", 0L).let { if (it == 0L) null else it }
    }

    /**
     * Full load (includes favorites JSON parse), run on IO dispatcher.
     */
    private fun loadFromPrefs() {
        // Re-read core fields (safety: refresh after IO load completes)
        loadFromPrefsSync()
        // Favorites: kotlinx.serialization JSON (P1-5 fix)
        val favJson = prefs.getString("favorites_json", "")
        if (!favJson.isNullOrEmpty()) {
            try {
                _favoriteMatches.value = favoritesJson.decodeFromString<List<FavoriteEntry>>(favJson)
            } catch (_: Exception) {
                _favoriteMatches.value = emptyList()
            }
        }
    }

    private fun save() {
        prefs.edit().apply {
            putString("membership_tier", _tier.value.rawValue)
            putInt("diamonds", _diamonds.value)
            putInt("diamond_cap", _diamondDailyCap.value)
            putStringSet("unlocked_analyses", _unlockedAnalysisIds.value)
            _trialStartDate.value?.let { putLong("trial_start", it) }
            // Favorites serialized as JSON array (P1-5: avoids split failure on special chars)
            putString("favorites_json", favoritesJson.encodeToString(_favoriteMatches.value))
            apply()
        }
    }

    // Utilities
    private fun startOfDay(ms: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = ms
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun daysBetween(startMs: Long, endMs: Long): Long {
        return (startOfDay(endMs) - startOfDay(startMs)) / (24 * 60 * 60 * 1000)
    }
}
/**
 * FavoriteEntry → Match 還原
 * 用於 FavoritesListView 直接渲染 PredictionRowView
 */
fun FavoriteEntry.toMatch(): Match = Match(
    id = id,
    league = runCatching { LeagueType.valueOf(league.uppercase()) }.getOrDefault(LeagueType.MLB),
    homeTeam = homeTeam,
    awayTeam = awayTeam,
    homeTeamCN = homeTeamCN,
    awayTeamCN = awayTeamCN,
    homeScore = homeScore,
    awayScore = awayScore,
    startTime = matchDate,
    location = "",
    status = MatchStatus.SCHEDULED,
    aiConfidence = aiConfidence,
    aiWinRateHome = aiHomeProb,
    aiTotalScorePredict = aiTotalScorePredict
)
