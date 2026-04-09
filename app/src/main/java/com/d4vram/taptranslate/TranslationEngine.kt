package com.d4vram.taptranslate

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await

/**
 * Wraps Google ML Kit On-Device Translation.
 * Caches Translator instances by language pair to avoid recreating them on each call.
 * Returns Result<String> so callers handle errors explicitly.
 */
class TranslationEngine {

    /** Cached translators by direction key (e.g. "ES_EN"). Reused across calls. */
    private val translatorCache = mutableMapOf<String, Translator>()

    suspend fun translateTexto(
        textoOriginal: String,
        sentidoTraduccion: String
    ): Result<String> {

        val sourceLang = if (sentidoTraduccion == AppConstants.SENTIDO_ES_EN)
            TranslateLanguage.SPANISH else TranslateLanguage.ENGLISH
        val targetLang = if (sentidoTraduccion == AppConstants.SENTIDO_ES_EN)
            TranslateLanguage.ENGLISH else TranslateLanguage.SPANISH

        // Returns cached translator for this direction or creates and caches a new one.
        val translator = translatorCache.getOrPut(sentidoTraduccion) {
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(sourceLang)
                .setTargetLanguage(targetLang)
                .build()
            Translation.getClient(options)
        }

        return try {
            translator.downloadModelIfNeeded(DownloadConditions.Builder().build()).await()
            val textoTraducido = translator.translate(textoOriginal).await()
            Result.success(textoTraducido)
        } catch (e: Exception) {
            Result.failure(e)
        }

    }

    /** Llama a esto al destruir la Activity para liberar memoria. */
    fun cerrarTodo() {
        translatorCache.values.forEach { it.close() }
        translatorCache.clear()
    }
}
