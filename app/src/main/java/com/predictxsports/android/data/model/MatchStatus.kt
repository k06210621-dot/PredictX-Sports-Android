package com.predictxsports.android.data.model

/**
 * 跨聯賽標準化賽事狀態
 *
 * iOS 原始碼：MatchStatus.swift line 4-10
 */
enum class MatchStatus(val rawValue: String) {
    SCHEDULED("SCHEDULED"),
    LIVE("LIVE"),
    COMPLETED("COMPLETED"),
    POSTPONED("POSTPONED"),
    CANCELLED("CANCELLED");

    companion object {
        fun fromString(statusStr: String?): MatchStatus {
            if (statusStr == null) return SCHEDULED
            return when (statusStr.uppercase()) {
                "SCHEDULED" -> SCHEDULED
                "LIVE" -> LIVE
                "COMPLETED", "FINAL", "FINISHED" -> COMPLETED
                "POSTPONED" -> POSTPONED
                "CANCELLED" -> CANCELLED
                else -> SCHEDULED
            }
        }
    }
}