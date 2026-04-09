package com.d4vram.taptranslate

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await

/**
 * [Profe - PBL Fix #5 y #6]:
 *
 * FIX #5 — Error devuelto como texto:
 *   ANTES: return "Error de traducción: ${e.message}"  ← Se pegaba en Reddit ❌
 *   DESPUÉS: return Result.failure(e)  ← El llamador decide qué hacer ✅
 *
 * FIX #6 — Cliente nuevo en cada llamada:
 *   ANTES: Translation.getClient(options)  en cada llamada ❌
 *   DESPUÉS: translatorCache — si el par de idiomas ya existe, lo reutilizamos ✅
 *
 * ¿Qué es Result<T>?
 *   Es una clase sellada de Kotlin estándar que puede ser:
 *   - Result.success(valor)  → Todo fue bien, aquí está el texto traducido
 *   - Result.failure(error)  → Algo falló, aquí está la excepción
 */
class TranslationEngine {

    /**
     * Caché de traductores indexada por sentido ("ES_EN" o "EN_ES").
     * Evita crear un objeto nuevo de ML Kit en cada llamada.
     */
    private val translatorCache = mutableMapOf<String, Translator>()

    suspend fun translateTexto(
        textoOriginal: String,
        sentidoTraduccion: String
    ): Result<String> {

        val sourceLang = if (sentidoTraduccion == AppConstants.SENTIDO_ES_EN)
            TranslateLanguage.SPANISH else TranslateLanguage.ENGLISH
        val targetLang = if (sentidoTraduccion == AppConstants.SENTIDO_ES_EN)
            TranslateLanguage.ENGLISH else TranslateLanguage.SPANISH

        // getOrPut: si la clave ya tiene un Translator, lo devuelve.
        // Si no, lo crea, lo guarda en el mapa y lo devuelve.
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
            // Propagamos la excepción hacia arriba — el llamador decide cómo informar al usuario
            Result.failure(e)
        }
        // NOTA: ya NO cerramos el translator aquí porque lo reutilizaremos.
    }

    /** Llama a esto al destruir la Activity para liberar memoria. */
    fun cerrarTodo() {
        translatorCache.values.forEach { it.close() }
        translatorCache.clear()
    }
}
