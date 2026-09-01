package com.predictxsports.android.data.model

/**
 * 五大聯賽型別 — 與 iOS LeagueType enum 100% 對齊
 *
 * iOS 原始碼：LeagueType.swift line 4-42
 */
enum class LeagueType(val rawValue: String, val displayName: String, val shortLabel: String) {
    MLB("MLB", "MLB 棒球", "棒球"),
    NPB("NPB", "日本職棒", "日職"),
    CPBL("CPBL", "中華職棒", "中職"),
    NBA("NBA", "NBA 籃球", "籃球"),
    WNBA("WNBA", "WNBA 籃球", "女籃");

    companion object {
        /** 5 個有效聯賽（iOS activeCases 對應） */
        val activeCases: List<LeagueType> = entries.toList()

        fun fromRaw(raw: String): LeagueType? {
            return entries.firstOrNull { it.rawValue == raw }
        }
    }
}