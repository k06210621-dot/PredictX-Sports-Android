package com.predictxsports.android.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.predictxsports.android.data.remote.RetrofitClient
import com.predictxsports.android.data.model.LeagueType

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import java.text.SimpleDateFormat

import java.util.Locale
import java.util.TimeZone

class AnalyticsViewModel : ViewModel() {

    private val _leagueAccuracies = MutableStateFlow<List<LeagueAccuracy>>(emptyList())
    val leagueAccuracies: StateFlow<List<LeagueAccuracy>> = _leagueAccuracies

    private val _winRateTrends = MutableStateFlow<List<WinRateTrend>>(emptyList())
    val winRateTrends: StateFlow<List<WinRateTrend>> = _winRateTrends

    private val _selectedLeague = MutableStateFlow("MLB")
    val selectedLeague: StateFlow<String> = _selectedLeague

    private val _overallAccuracy = MutableStateFlow(0.0)
    val overallAccuracy: StateFlow<Double> = _overallAccuracy

    private val _recentSettlements = MutableStateFlow<List<RecentSettlement>>(emptyList())
    val recentSettlements: StateFlow<List<RecentSettlement>> = _recentSettlements

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        viewModelScope.launch {
            loadRealAnalyticsData()
        }
    }

    fun updateTrendForLeague(league: String) {
        _selectedLeague.value = league
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val trends = RetrofitClient.api.fetchHitRateTrend(league = league)
                val mapped = trends.mapNotNull { trend ->
                    val date = parseDate(trend.date) ?: return@mapNotNull null
                    WinRateTrend(date = date, hitRate = trend.dailyHitRate)
                }
                _winRateTrends.value = mapped
            } catch (e: Exception) {
                _errorMessage.value = "無法載入 $league 驗證率趨勢"
            }
        }
    }

    // ── 資料載入 ──

    private suspend fun loadRealAnalyticsData() {
        _isLoading.value = true
        try {
            val realLeagues = RetrofitClient.api.fetchOverallStats()

            // 排除 FIFA（已停用）
            val filtered = realLeagues.filter { it.league != "FIFA" }

            _leagueAccuracies.value = filtered.map {
                LeagueAccuracy(league = it.league, hitRate = it.hitRate, totalAnalyzed = it.totalAnalyzed)
            }

            val totalHits = filtered.sumOf { it.totalHits }
            val totalGames = filtered.sumOf { it.totalAnalyzed }
            _overallAccuracy.value = if (totalGames > 0) totalHits.toDouble() / totalGames else 0.0

            val defaultLeague = _leagueAccuracies.value.firstOrNull()?.league ?: "MLB"
            updateTrendForLeague(defaultLeague)

            // 並行抓所有聯盟近 30 天 settlement（背景跑，不擋趨勢圖）
            loadRecentSettlements()

            _isLoading.value = false
        } catch (e: Exception) {
            _errorMessage.value = "無法載入驗證率統計"
            _isLoading.value = false
        }
    }

    /**
     * 並行抓所有聯盟近 30 天賽事，過濾已完成且已結算的，取最近 10 場
     * 對應 iOS loadRecentSettlements() + withTaskGroup
     */
    private fun loadRecentSettlements() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val deferredResults = LeagueType.activeCases.map { lg ->
                    viewModelScope.async(Dispatchers.IO) {
                        try {
                            val models = RetrofitClient.api.fetchGames(lg.rawValue, days = 30)
                            models.mapNotNull { m ->
                                val isHit = m.aiIsHit ?: return@mapNotNull null
                                if (m.homeTeamScore == null && m.awayTeamScore == null) return@mapNotNull null
                                Quad(
                                    league = lg,
                                    id = m.gameId,
                                    homeTeam = m.homeTeam,
                                    awayTeam = m.awayTeam,
                                    dateString = m.matchDate,
                                    homeScore = m.homeTeamScore?.toInt(),
                                    awayScore = m.awayTeamScore?.toInt(),
                                    predictedScore = m.aiPredictedScore,
                                    isHit = isHit
                                )
                            }
                        } catch (_: Exception) {
                            emptyList()
                        }
                    }
                }

                val rawHits = deferredResults.awaitAll().flatten()

                val settlements = rawHits.mapNotNull { item ->
                    val date = parseDate(item.dateString) ?: return@mapNotNull null
                    RecentSettlement(
                        id = item.id,
                        league = item.league.rawValue,
                        homeTeam = item.homeTeam,
                        awayTeam = item.awayTeam,
                        matchDate = date,
                        homeScore = item.homeScore,
                        awayScore = item.awayScore,
                        predictedScore = item.predictedScore,
                        isHit = item.isHit
                    )
                }

                // 按日期降冪排序
                val sorted = settlements.sortedByDescending { it.matchDate }

                // 去重：同一場比賽保留最新一筆
                val seen = mutableSetOf<String>()
                val deduped = sorted.filter { seen.add(it.id) }

                _recentSettlements.value = deduped.take(10)
            } catch (e: Exception) {
                // silent
            }
        }
    }

    val recentFormRate: Double
        get() {
            val list = _recentSettlements.value
            if (list.isEmpty()) return 0.0
            val hits = list.count { it.isHit }
            return hits.toDouble() / list.size
        }

    // ── 輔助方法 ──

    private fun parseDate(dateString: String): Long? {
        if (dateString.isEmpty()) return null
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("Asia/Taipei")
        return sdf.parse(dateString)?.time
    }
}

// ── 資料結構 ──

data class LeagueAccuracy(
    val league: String,
    val hitRate: Double,
    val totalAnalyzed: Int
)

data class WinRateTrend(
    val date: Long,  // epoch millis
    val hitRate: Double
)

data class RecentSettlement(
    val id: String,
    val league: String,
    val homeTeam: String,
    val awayTeam: String,
    val matchDate: Long,
    val homeScore: Int?,
    val awayScore: Int?,
    val predictedScore: String?,
    val isHit: Boolean
)

/** loadRecentSettlements 內部用的臨時 tuple */
private data class Quad(
    val league: LeagueType,
    val id: String,
    val homeTeam: String,
    val awayTeam: String,
    val dateString: String,
    val homeScore: Int?,
    val awayScore: Int?,
    val predictedScore: String?,
    val isHit: Boolean
)