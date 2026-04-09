package com.d4vram.taptranslate

import android.content.Context
import android.content.SharedPreferences

/**
 * [Profe]: Esta clase (PreferencesManager) es nuestra "memoria a largo plazo" muy sencilla.
 */
class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("TapTranslatePrefs", Context.MODE_PRIVATE)

    // ============================================
    // 1. GESTIÓN DEL ONBOARDING
    // ============================================
    fun haVistoOnboarding(): Boolean {
        return prefs.getBoolean("VISTO_ONBOARDING", false)
    }

    fun marcarOnboardingComoVisto() {
        val editor = prefs.edit()
        editor.putBoolean("VISTO_ONBOARDING", true)
        editor.apply()
    }

    // ============================================
    // 2. GESTIÓN DEL IDIOMA
    // ============================================
    
    /**
     * Devuelve "ES_EN" (Español a Inglés) o "EN_ES" (Inglés a Español). 
     * Por defecto asume Español a Inglés para mandar comentarios al extranjero.
     */
    fun getSentidoTraduccion(): String {
        return prefs.getString("SENTIDO_TRADUCCION", "ES_EN") ?: "ES_EN"
    }

    /**
     * Guarda la selección del usuario cuando elija el switch en MainActivity.
     */
    fun setSentidoTraduccion(sentido: String) {
        prefs.edit().putString("SENTIDO_TRADUCCION", sentido).apply()
    }
}
