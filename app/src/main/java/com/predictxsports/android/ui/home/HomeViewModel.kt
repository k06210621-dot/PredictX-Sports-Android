package com.predictxsports.android.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.predictxsports.android.data.model.LeagueType
import com.predictxsports.android.data.model.Match
import com.predictxsports.android.data.model.MatchStatus
import com.predictxsports.android.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone

class HomeViewModel : ViewModel() {

    private val _focusMatches = MutableStateFlow<List<Match>>(emptyList())
    val focusMatches: StateFlow<List<Match>> = _focusMatches

    private val _filteredPredictions = MutableStateFlow<List<Match>>(emptyList())
    val filteredPredictions: StateFlow<List<Match>> = _filteredPredictions

    private val _historicalMatches = MutableStateFlow<Map<LeagueType, List<Match>>>(emptyMap())
    val historicalMatches: StateFlow<Map<LeagueType, List<Match>>> = _historicalMatches

    private val _isHistoryLoading = MutableStateFlow(false)
    val isHistoryLoading: StateFlow<Boolean> = _isHistoryLoading

    private val _historyError = MutableStateFlow<String?>(null)
    val historyError: StateFlow<String?> = _historyError

    private val _selectedLeague = MutableStateFlow(LeagueType.MLB)
    val selectedLeague: StateFlow<LeagueType> = _selectedLeague

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private var allMatches: List<Match> = emptyList()

    init {
        viewModelScope.launch {
            refresh()
        }
    }

    fun setSelectedLeague(league: LeagueType) {
        _selectedLeague.value = league
        updateUIElements()
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            var lastError: Throwable? = null
            for (attempt in 1..2) {
                try {
                    doImportAllSportsData()
                    lastError = null
                    break
                } catch (e: Exception) {
                    lastError = e
                    android.util.Log.w("HomeViewModel", "refresh attempt $attempt failed: ${e.message}")
                    if (attempt < 2) {
                        // Exponential backoff: 1.5s
                        kotlinx.coroutines.delay(1500L)
                    }
                }
            }
            _errorMessage.value = lastError?.let { friendlyError(it) }
            _isLoading.value = false
        }
    }

    fun loadHistoryForAllLeagues() {
        viewModelScope.launch {
            _isHistoryLoading.value = true
            _historyError.value = null
            var firstError: Throwable? = null
            try {
                val todayStartUTC = utcStartOfDayMillis()
                for (lg in LeagueType.activeCases) {
                    try {
                        val models = RetrofitClient.api.fetchGames(lg.rawValue, days = 30)
                        val mapped = models.map { Match.fromGameModel(it, lg) }
                        val history = mapped.filter { m ->
                            m.startTime < todayStartUTC || m.status == MatchStatus.COMPLETED
                        }.sortedByDescending { it.startTime }
                            .distinctBy { it.id }
                        _historicalMatches.value += lg to history
                    } catch (e: Exception) {
                        if (firstError == null) firstError = e
                    }
                }
                if (firstError != null && _historicalMatches.value.isEmpty()) {
                    _historyError.value = friendlyError(firstError)
                }
            } finally {
                _isHistoryLoading.value = false
            }
        }
    }

    fun loadHistoryForLeague(league: LeagueType) {
        loadHistoryForAllLeagues()
    }

    private suspend fun doImportAllSportsData() {
        // P2-3：5 個聯盟改為 async 並行 fetch。
        // 原本循序 fetch 需 ~5 秒；改並行可降至最慢單一 API 的時間 (~1.5 秒)。
        val combined = kotlinx.coroutines.coroutineScope {
            LeagueType.activeCases.map { lg ->
                async(Dispatchers.IO) {
                    runCatching {
                        val models = RetrofitClient.api.fetchGames(lg.rawValue, days = 14)
                        models.map { Match.fromGameModel(it, lg) }
                    }
                }
            }.awaitAll().flatMap { result ->
                result.getOrElse { emptyList() }
            }
        }

        val unique = combined.distinctBy { it.id }
            .sortedBy { it.startTime }

        allMatches = unique

        val todayUTC = utcStartOfDayMillis()
        val history = unique.filter { it.startTime < todayUTC }
        val newHistory = mutableMapOf<LeagueType, List<Match>>()
        for (lg in LeagueType.activeCases) {
            val leagueHistory = history.filter { it.league == lg }
            val existing = _historicalMatches.value[lg]
            if (existing.isNullOrEmpty()) {
                newHistory[lg] = leagueHistory
            }
        }
        if (newHistory.isNotEmpty()) {
            _historicalMatches.value += newHistory
        }

        updateUIElements()
    }

    private fun updateUIElements() {
        val now = System.currentTimeMillis()
        val todayStartUTC = utcStartOfDayMillis()
        val yesterdayStartUTC = todayStartUTC - 86400_000L
        val tomorrowEndUTC = todayStartUTC + 172800_000L

        val league = _selectedLeague.value

        val upcoming = allMatches.filter {
            it.league == league && it.startTime >= todayStartUTC
        }.sortedBy { it.startTime }

        _filteredPredictions.value = upcoming

        val focus = allMatches.filter {
            it.league == league && it.startTime in yesterdayStartUTC until tomorrowEndUTC && (it.aiConfidence ?: 0.0) >= FOCUS_CONFIDENCE_THRESHOLD
        }.sortedByDescending { it.aiConfidence ?: 0.0 }

        _focusMatches.value = focus.take(5)
    }

    private fun utcStartOfDayMillis(): Long {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun friendlyError(e: Throwable): String {
        return when (e) {
            is java.net.UnknownHostException -> "無法連線至伺服器，請檢查網路"
            is java.net.SocketTimeoutException -> "伺服器回應逾時，請稍後再試"
            else -> "載入失敗：${e.message ?: "未知錯誤"}"
        }
    }

    fun findMatchById(matchId: String): Match? {
        return allMatches.find { it.id == matchId }
    }

    companion object {
        // ⚠️ [2026-08-17] 與 iOS HomeStore.swift、push_service.CONFIDENCE_THRESHOLD(=8)、ProfileView 文字統一
        // 「AI 重點觀察賽事」焦點區的最低信心度門檻
        private const val FOCUS_CONFIDENCE_THRESHOLD = 8.0
    }
}
