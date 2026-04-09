package com.d4vram.taptranslate

/**
 * Centralizes all magic strings used across the project.
 * Changing a key or value here propagates automatically everywhere it's used.
 */
object AppConstants {

    // ─── SharedPreferences ────────────────────────────────────────────────
    /** Nombre del archivo de preferencias en el disco del teléfono */
    const val PREFS_NAME = "TapTranslatePrefs"

    /** Clave para saber si el usuario ya vio el Onboarding */
    const val KEY_ONBOARDING_SEEN = "VISTO_ONBOARDING"

    /** Clave para guardar la dirección de traducción seleccionada */
    const val KEY_TRANSLATION_DIR = "SENTIDO_TRADUCCION"

    /** Activa el modo hostil con chip flotante en apps seleccionadas. */
    const val KEY_HOSTILE_MODE_ENABLED = "HOSTILE_MODE_ENABLED"

    /** Conjunto de paquetes donde el overlay hostil está permitido. */
    const val KEY_HOSTILE_PACKAGES = "HOSTILE_PACKAGES"

    // ─── Valores de dirección de traducción ──────────────────────────────
    /** Español → Inglés (Modo Escribir) */
    const val SENTIDO_ES_EN = "ES_EN"

    /** Inglés → Español (Modo Leer) */
    const val SENTIDO_EN_ES = "EN_ES"

    // ─── Broadcast ───────────────────────────────────────────────────────
    /** Acción del broadcast que le ordena al AutoPasteService que pegue */
    const val ACTION_PASTE_NOW = "com.d4vram.taptranslate.PASTE_NOW"
}
