package com.d4vram.taptranslate

import android.content.Context
import android.content.SharedPreferences

/**
 * [Profe - PBL Fix #4]: Aplicamos AppConstants.
 *
 * ANTES: prefs.getBoolean("VISTO_ONBOARDING", false) — magic string ❌
 * DESPUÉS: prefs.getBoolean(AppConstants.KEY_ONBOARDING_SEEN, false) — constante ✅
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
}
