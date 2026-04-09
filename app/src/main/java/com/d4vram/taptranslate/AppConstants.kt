package com.d4vram.taptranslate

/**
 * [Profe - PBL]: Un 'object' en Kotlin es un Singleton automático.
 * Aquí centralizamos TODAS las cadenas mágicas del proyecto.
 *
 * ANTES (❌ magic strings dispersas en 4 archivos):
 *   prefs.getString("SENTIDO_TRADUCCION", "ES_EN")
 *
 * DESPUÉS (✅ constante única):
 *   prefs.getString(AppConstants.KEY_TRANSLATION_DIR, AppConstants.SENTIDO_ES_EN)
 *
 * Ventaja: Si mañana cambias el nombre de la clave, lo cambias aquí y
 * el compilador te avisa en cada sitio que lo use.
 */
object AppConstants {

    // ─── SharedPreferences ────────────────────────────────────────────────
    /** Nombre del archivo de preferencias en el disco del teléfono */
    const val PREFS_NAME = "TapTranslatePrefs"

    /** Clave para saber si el usuario ya vio el Onboarding */
    const val KEY_ONBOARDING_SEEN = "VISTO_ONBOARDING"

    /** Clave para guardar la dirección de traducción seleccionada */
    const val KEY_TRANSLATION_DIR = "SENTIDO_TRADUCCION"

    // ─── Valores de dirección de traducción ──────────────────────────────
    /** Español → Inglés (Modo Escribir) */
    const val SENTIDO_ES_EN = "ES_EN"

    /** Inglés → Español (Modo Leer) */
    const val SENTIDO_EN_ES = "EN_ES"

    // ─── Broadcast ───────────────────────────────────────────────────────
    /** Acción del broadcast que le ordena al AutoPasteService que pegue */
    const val ACTION_PASTE_NOW = "com.d4vram.taptranslate.PASTE_NOW"
}
