package com.predictxsports.android.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 對應 Railway API /api/games 回傳單一賽事 JSON
 *
 * ⚠️ json key 全部 snake_case，Android model 保持與 iOS MatchModel 100% 對齊
 * iOS 原始碼：APIService.swift line 11-41 (MatchModel)
 */
@Serializable
data class GameModel(
    @SerialName("game_id")
    val gameId: String,

    @SerialName("match_date")
    val matchDate: String,

    val status: String? = null,

    @SerialName("home_team")
    val homeTeam: String,

    @SerialName("away_team")
    val awayTeam: String,

    @SerialName("home_team_score")
    val homeTeamScore: Double? = null,

    @SerialName("away_team_score")
    val awayTeamScore: Double? = null,

    @SerialName("ai_confidence")
    val aiConfidence: Double? = null,

    @SerialName("ai_home_prob")
    val aiHomeProb: Double? = null,

    @SerialName("ai_predicted_score")
    val aiPredictedScore: String? = null,

    @SerialName("ai_is_hit")
    val aiIsHit: Boolean? = null,

    @SerialName("ai_actual_score")
    val aiActualScore: String? = null
)

/**
 * 對應 Railway API /api/game_analysis/{gameId} 回傳 JSON
 *
 * iOS 原始碼：APIService.swift line 43-66 (AIAnalysisModel)
 */
@Serializable
data class AIAnalysisModel(
    val prediction: Prediction? = null,
    val analysis: Analysis? = null,

    @SerialName("radar_chart")
    val radarChart: RadarChart? = null
) {
    @Serializable
    data class Prediction(
        @SerialName("home_win_probability")
        val homeWinProbability: Double? = null,

        @SerialName("away_win_probability")
        val awayWinProbability: Double? = null,

        val confidence: Double? = null,

        @SerialName("predicted_score")
        val predictedScore: String? = null
    )

    @Serializable
    data class Analysis(
        val summary: String? = null,

        @SerialName("key_factors")
        val keyFactors: List<String>? = null,

        @SerialName("risk_factors")
        val riskFactors: List<String>? = null
    )

    @Serializable
    data class RadarChart(
        val categories: List<String>? = null,

        @SerialName("home_team")
        val homeTeam: List<Double>? = null,

        @SerialName("away_team")
        val awayTeam: List<Double>? = null
    )
}

/**
 * 對應 Railway API /analytics/overall 回傳 JSON
 *
 * iOS 原始碼：APIService.swift line 69-74 (LeagueAccuracyModel)
 */
@Serializable
data class LeagueAccuracyModel(
    val league: String,

    @SerialName("total_analyzed")
    val totalAnalyzed: Int,

    @SerialName("total_hits")
    val totalHits: Int,

    @SerialName("hit_rate")
    val hitRate: Double
)

/**
 * 對應 Railway API /analytics/trend 回傳 JSON
 *
 * iOS 原始碼：APIService.swift line 76-80 (HitRateTrendModel)
 */
@Serializable
data class HitRateTrendModel(
    val date: String,

    @SerialName("games_count")
    val gamesCount: Int,

    @SerialName("daily_hit_rate")
    val dailyHitRate: Double
)

// ============================================================
// 球員資料模型（對應 APIService.swift line 232-289）
// TheSportsDB 回傳格式
// ============================================================

@Serializable
data class TeamRosterResponse(
    @SerialName("team_id")
    val teamId: String,

    val count: Int,
    val players: List<PlayerBasic>
)

@Serializable
data class PlayerBasic(
    val id: String,
    val name: String,
    val position: String? = null,
    val nationality: String? = null,

    @SerialName("birth_date")
    val birthDate: String? = null,

    val height: String? = null,
    val weight: String? = null,

    @SerialName("photo_url")
    val photoUrl: String? = null,

    @SerialName("cutout_url")
    val cutoutUrl: String? = null
)

@Serializable
data class PlayerDetailResponse(
    val player: PlayerDetail,
    val contracts: List<PlayerContract>? = null,
    val honours: List<PlayerHonour>? = null
)

@Serializable
data class PlayerDetail(
    val id: String,
    val name: String,
    val team: String? = null,

    @SerialName("team_id")
    val teamId: String? = null,

    val nationality: String? = null,
    val position: String? = null,

    @SerialName("birth_date")
    val birthDate: String? = null,

    @SerialName("birth_location")
    val birthLocation: String? = null,

    val height: String? = null,
    val weight: String? = null,

    @SerialName("jersey_number")
    val jerseyNumber: String? = null,

    @SerialName("photo_url")
    val photoUrl: String? = null,

    @SerialName("cutout_url")
    val cutoutUrl: String? = null,

    val description: String? = null
)

@Serializable
data class PlayerContract(
    val id: Int? = null,
    val idPlayer: String? = null,
    val idTeam: String? = null,

    @SerialName("strTeam")
    val strTeam: String? = null,

    @SerialName("strBadge")
    val strBadge: String? = null,

    @SerialName("strYearStart")
    val strYearStart: String? = null,

    @SerialName("strYearEnd")
    val strYearEnd: String? = null
)

@Serializable
data class PlayerHonour(
    val id: Int? = null,
    val idPlayer: String? = null,

    @SerialName("strHonour")
    val strHonour: String? = null,

    @SerialName("strSport")
    val strSport: String? = null,

    @SerialName("strSeason")
    val strSeason: String? = null
)