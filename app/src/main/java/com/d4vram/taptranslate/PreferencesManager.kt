package com.d4vram.taptranslate

import android.content.Context
import android.content.SharedPreferences

/**
 * Thin wrapper around SharedPreferences for app-level persistence.
 */
class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE)

    fun haVistoOnboarding(): Boolean =
        prefs.getBoolean(AppConstants.KEY_ONBOARDING_SEEN, false)

    fun marcarOnboardingComoVisto() =
        prefs.edit().putBoolean(AppConstants.KEY_ONBOARDING_SEEN, true).apply()

    fun getSentidoTraduccion(): String =
        prefs.getString(AppConstants.KEY_TRANSLATION_DIR, AppConstants.SENTIDO_ES_EN)
            ?: AppConstants.SENTIDO_ES_EN

    fun setSentidoTraduccion(sentido: String) =
        prefs.edit().putString(AppConstants.KEY_TRANSLATION_DIR, sentido).apply()

    fun isHostileModeEnabled(): Boolean =
        prefs.getBoolean(AppConstants.KEY_HOSTILE_MODE_ENABLED, false)

    fun setHostileModeEnabled(enabled: Boolean) =
        prefs.edit().putBoolean(AppConstants.KEY_HOSTILE_MODE_ENABLED, enabled).apply()

    fun getHostilePackages(): Set<String> =
        prefs.getStringSet(AppConstants.KEY_HOSTILE_PACKAGES, emptySet()).orEmpty()

    fun setHostilePackages(packages: Set<String>) =
        prefs.edit().putStringSet(AppConstants.KEY_HOSTILE_PACKAGES, packages).apply()
}
