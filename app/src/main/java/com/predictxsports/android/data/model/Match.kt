package com.predictxsports.android.data.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/**
 * 萬能賽事資料模型 — 100% 對齊 iOS Match struct
 *
 * iOS 原始碼：Match.swift (148 行)
 */
data class Match(
    val id: String,
    val league: LeagueType,
    val homeTeam: String,
    val awayTeam: String,
    val homeTeamCN: String,
    val awayTeamCN: String,
    val homeScore: Int?,
    val awayScore: Int?,
    val startTime: Long,  // epoch millis（對應 iOS Date）
    val location: String,
    val status: MatchStatus,

    // AI 預測欄位
    val aiWinRateHome: Double? = null,
    val aiConfidence: Double? = null,
    val aiRecommendation: String? = null,
    val aiTotalScorePredict: String? = null,
    val aiIsHit: Boolean? = null,
    val aiActualScore: String? = null,

    // 聯賽特徵標籤（後續 Phase 實作）
    val nbaFeatures: NBASpecificTags? = null,
    val mlbFeatures: MLBSpecificTags? = null,
    val cpblFeatures: CPBLSpecificTags? = null,
    val npbFeatures: NPBSpecificTags? = null
) {
    val hasAnalysis: Boolean
        get() = aiConfidence != null || aiWinRateHome != null

    companion object {
        /**
         * 從 Railway API GameModel + LeagueType 建立 Match
         * 對應 iOS：init(from model: MatchModel, leagueType: LeagueType)
         */
        fun fromGameModel(model: GameModel, leagueType: LeagueType): Match {
            val parsedDate = parseDate(model.matchDate)
            val statusStr = model.status ?: ""

            val status = MatchStatus.fromString(statusStr)

            return Match(
                id = model.gameId,
                league = leagueType,
                homeTeam = model.homeTeam,
                awayTeam = model.awayTeam,
                homeTeamCN = TeamNameMap.getChineseName(model.homeTeam),
                awayTeamCN = TeamNameMap.getChineseName(model.awayTeam),
                homeScore = model.homeTeamScore?.toInt(),
                awayScore = model.awayTeamScore?.toInt(),
                startTime = parsedDate,
                location = "Unknown",
                status = status,
                aiConfidence = model.aiConfidence,
                aiWinRateHome = model.aiHomeProb,
                aiTotalScorePredict = sanitizePredictedScore(model.aiPredictedScore),
                aiIsHit = model.aiIsHit,
                aiActualScore = model.aiActualScore
            )
        }
    }
}

/**
 * 日期解析 — 100% 對齊 iOS parseDate()
 *
 * iOS 原始碼：Match.swift line 119-147
 *
 * 規則：
 * 1. 有時區資訊（GMT/+/-/Z）→ 用 ISO8601 格式以 UTC 解析
 * 2. 純日期（YYYY-MM-DD）→ 強制設為 UTC 中午，避免時區偏移誤判
 */
fun parseDate(dateString: String): Long {
    if (dateString.isEmpty()) return 0L

    val hasTimezone = dateString.contains("GMT") ||
        dateString.contains("+") ||
        dateString.contains("Z")

    // 有時區 → UTC 解析
    if (hasTimezone) {
        val timeFormats = listOf(
            "yyyy-MM-dd'T'HH:mm:ssX",
            "yyyy-MM-dd'T'HH:mm:ss",
            "EEE, dd MMM yyyy HH:mm:ss ZZZZ",
            "EEE, dd MMM yyyy HH:mm:ss zzz"
        )
        val sdf = SimpleDateFormat("", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        for (fmt in timeFormats) {
            try {
                sdf.applyPattern(fmt)
                val date = sdf.parse(dateString)
                if (date != null) return date.time
            } catch (_: Exception) {
                // try next format
            }
        }
        return 0L
    }

    // 純日期 → 設為 UTC 中午
    val dateFormats = listOf("yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd")
    val fallbackFormat = "yyyy-MM-dd"
    val sdf = SimpleDateFormat(fallbackFormat, Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    for (fmt in dateFormats) {
        try {
            sdf.applyPattern(fmt)
            val date = sdf.parse(dateString)
            if (date != null) {
                // 強制設為 UTC 中午 12:00
                val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                    timeInMillis = date.time
                    set(Calendar.HOUR_OF_DAY, 12)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                return cal.timeInMillis
            }
        } catch (_: Exception) {
            // try next format
        }
    }

    return 0L
}

/**
 * 清洗 AI 預測比分 — 100% 對齊 iOS sanitizePredictedScore()
 *
 * iOS 原始碼：Match.swift line 71-90
 *
 * 僅保留 "整數-整數" 格式（如 "5-4"），不合規回傳 null
 */
fun sanitizePredictedScore(raw: String?): String? {
    if (raw == null) return null
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return null

    // 各種 dash → 半形
    val normalized = trimmed
        .replace("－", "-")   // 全形
        .replace("—", "-")    // em-dash
        .replace("–", "-")    // en-dash
        .replace(" ", "")

    // 嚴格正則：^\d+-\d+$
    val regex = Regex("^(\\d+)-(\\d+)$")
    val match = regex.matchEntire(normalized) ?: return null

    val home = match.groupValues[1]
    val away = match.groupValues[2]
    return "$home-$away"
}