package com.predictxsports.android.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

object ThemeController {
    private const val PREF_NAME = "predictx_theme"
    private const val KEY_IS_DARK = "is_dark"
    @Volatile private var applicationContext: Context? = null
    @Volatile private var prefsCache: SharedPreferences? = null

    private val _isDark = mutableStateOf(true)
    val isDarkState: State<Boolean> = _isDark
    val isDark: Boolean get() = _isDark.value

    /** 在 MainActivity.onCreate 中呼叫一次，完成 prefs 讀取 */
    fun init(context: Context) {
        if (applicationContext != null) return
        applicationContext = context.applicationContext
        val p = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefsCache = p
        _isDark.value = p.getBoolean(KEY_IS_DARK, true)
    }

    /** P2-2：lazy getter 防護 — 若 init 未被呼叫，自動從 Application context 取 */
    private fun prefs(): SharedPreferences {
        prefsCache?.let { return it }
        val ctx = applicationContext
            ?: throw IllegalStateException("ThemeController.init(context) must be called first")
        val p = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefsCache = p
        return p
    }

    fun toggle() {
        _isDark.value = !_isDark.value
        prefs().edit().putBoolean(KEY_IS_DARK, _isDark.value).apply()
    }

    fun setDarkTheme(dark: Boolean) {
        _isDark.value = dark
        prefs().edit().putBoolean(KEY_IS_DARK, dark).apply()
    }
}
