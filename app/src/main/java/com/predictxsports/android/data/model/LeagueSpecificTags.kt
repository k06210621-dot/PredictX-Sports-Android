package com.predictxsports.android.data.model

// 聯賽特徵標籤 — 完整版，對應 iOS 版本
// iOS 原始碼對應：CPBLSpecificTags.swift, NPBSpecificTags.swift, NBASpecificTags.swift, MLBSpecificTags.swift

data class MLBSpecificTags(
    val homeStarterFIP: Double? = null,
    val awayStarterFIP: Double? = null,
    val homeStarterWHIP: Double? = null,
    val awayStarterWHIP: Double? = null,
    val homeTeamwOBA: Double? = null,
    val awayTeamwOBA: Double? = null,
    val parkFactor: Double? = null,
    val windDirection: String? = null
)

data class NBASpecificTags(
    val homeEFG: Double? = null,
    val homeTOV: Double? = null,
    val homeORB: Double? = null,
    val homeFTRate: Double? = null,
    val awayEFG: Double? = null,
    val awayTOV: Double? = null,
    val awayORB: Double? = null,
    val awayFTRate: Double? = null,
    val pace: Double? = null,
    val homeNetRating: Double? = null,
    val awayNetRating: Double? = null
)

data class CPBLSpecificTags(
    val localPitcherFIP: Double? = null,
    val foreignPitcherFIP: Double? = null,
    val bullpenWHIP: Double? = null,
    val homeRunDerbyRate: Double? = null,
    val isDomeStadium: Boolean? = null,
    val localizedHumidity: Double? = null
)

data class NPBSpecificTags(
    val aceStarterWHIP: Double? = null,
    val aceStarterERA: Double? = null,
    val teamFieldingPercentage: Double? = null,
    val sacrificeAndSmallBallRate: Double? = null,
    val npbParkFactor: Double? = null,
    val travelFatigueIndex: Double? = null
)