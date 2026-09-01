package com.predictxsports.android.data.remote

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory

import com.predictxsports.android.data.model.AIAnalysisModel
import com.predictxsports.android.data.model.GameModel
import com.predictxsports.android.data.model.HitRateTrendModel
import com.predictxsports.android.data.model.LeagueAccuracyModel

import com.predictxsports.android.data.model.PlayerDetailResponse
import com.predictxsports.android.data.model.TeamRosterResponse

import okhttp3.MediaType.Companion.toMediaType

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit API interface — 100% 對齊 iOS APIService 6 個 endpoint
 *
 * Base URL: https://predictx-sports-production.up.railway.app
 * iOS 原始碼：APIService.swift
 */
interface PredictXApi {

    // ── 賽事列表 ──
    /** GET /api/games?league={}&days={} → [GameModel] */
    @GET("/api/games")
    suspend fun fetchGames(
        @Query("league") league: String,
        @Query("days") days: Int = 14
    ): List<GameModel>

    // ── AI 分析詳情 ──
    /** GET /api/game_analysis/{gameId} → AIAnalysisModel */
    @GET("/api/game_analysis/{gameId}")
    suspend fun fetchAIAnalysis(
        @Path("gameId") gameId: String
    ): AIAnalysisModel

    // ── 整體命中率 ──
    /** GET /analytics/overall → [LeagueAccuracyModel] */
    @GET("/analytics/overall")
    suspend fun fetchOverallStats(): List<LeagueAccuracyModel>

    // ── 命中率趨勢 ──
    /** GET /analytics/trend?league={} → [HitRateTrendModel] */
    @GET("/analytics/trend")
    suspend fun fetchHitRateTrend(
        @Query("league") league: String? = null
    ): List<HitRateTrendModel>

    // ── 裝置註冊（APNs / 後續再加 FCM） ──
    /** POST /api/register_device */
    @POST("/api/register_device")
    suspend fun registerDevice(
        @Body body: RegisterDeviceRequest
    )

    // ── 推播偏好更新 ──
    /** POST /api/update_push_preference */
    @POST("/api/update_push_preference")
    suspend fun updatePushPreference(
        @Body body: UpdatePushPreferenceRequest
    )

    // ── 球員資料 ──
    /** GET /api/players/roster?team_id={} → TeamRosterResponse */
    @GET("/api/players/roster")
    suspend fun fetchTeamRoster(
        @Query("team_id") teamId: String
    ): TeamRosterResponse

    /** GET /api/players/{playerId} → PlayerDetailResponse */
    @GET("/api/players/{playerId}")
    suspend fun fetchPlayerDetail(
        @Path("playerId") playerId: String
    ): PlayerDetailResponse
}

// ── Request body models (與 iOS `[String: Any]` dict 對齊) ──

@kotlinx.serialization.Serializable
data class RegisterDeviceRequest(
    val token: String,
    val tier: String,
    @kotlinx.serialization.SerialName("push_enabled")
    val pushEnabled: Boolean,
    val platform: String = "android"
)

@kotlinx.serialization.Serializable
data class UpdatePushPreferenceRequest(
    val token: String,
    @kotlinx.serialization.SerialName("push_enabled")
    val pushEnabled: Boolean
)

// ── Singleton 建立 Retrofit instance ──

object RetrofitClient {
    private const val BASE_URL = "https://predictx-sports-production.up.railway.app/"
    private const val CACHE_SIZE_BYTES = 10L * 1024 * 1024 // 10 MB

    // kotlinx.serialization JSON 設定：寬容模式（忽略 iOS 端 JSON 的非核心欄位）
    val json = kotlinx.serialization.json.Json {
        ignoreUnknownKeys = true
        coerceInputValues = true  // null → 預設值，避免 NPE
    }

    @Volatile private var _okHttpClient: okhttp3.OkHttpClient? = null

    /** 從 Application context 初始化 OkHttp cache。應在 MainActivity.onCreate 中呼叫一次。 */
    fun init(appContext: android.content.Context) {
        if (_okHttpClient != null) return
        synchronized(this) {
            if (_okHttpClient != null) return
            val cacheDir = java.io.File(appContext.cacheDir, "predictx_http_cache")
            val cache = okhttp3.Cache(cacheDir, CACHE_SIZE_BYTES)
            _okHttpClient = okhttp3.OkHttpClient.Builder()
                .cache(cache)
                .addInterceptor(okhttp3.logging.HttpLoggingInterceptor().apply {
                    // P1-7：release 模式完全不輸出（避免 token 等敏感 header 進 logcat），
                    // debug 模式仍輸出 BODY 方便開發。
                    level = if (com.predictxsports.android.BuildConfig.DEBUG)
                        okhttp3.logging.HttpLoggingInterceptor.Level.BODY
                    else
                        okhttp3.logging.HttpLoggingInterceptor.Level.NONE
                })
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build()
        }
    }

    private fun okHttpClient(): okhttp3.OkHttpClient {
        return _okHttpClient ?: throw IllegalStateException("RetrofitClient.init() must be called first (see MainActivity.onCreate)")
    }

    val api: PredictXApi by lazy {
        retrofit2.Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(PredictXApi::class.java)
    }
}