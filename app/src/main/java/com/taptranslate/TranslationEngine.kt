package com.d4vram.taptranslate

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await

class TranslationEngine {

    /**
     * [Profe]: En lugar de tener la traducción fijada de ES -> EN, ahora le pedimos a la función
     * que reciba la preferencia guardada en SharedPreferences y acomode las reglas.
     */
    suspend fun translateTexto(textoOriginal: String, sentidoTraduccion: String): String {
        
        // 1. Asignamos los roles "Origen" (Source) y "Destino" (Target) de forma Dinámica.
        val sourceLang = if (sentidoTraduccion == "ES_EN") TranslateLanguage.SPANISH else TranslateLanguage.ENGLISH
        val targetLang = if (sentidoTraduccion == "ES_EN") TranslateLanguage.ENGLISH else TranslateLanguage.SPANISH

        // 2. Configuramos las opciones
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLang)
            .setTargetLanguage(targetLang)
            .build()
            
        val translator = Translation.getClient(options)
        val conditions = DownloadConditions.Builder().build()

        return try {
            // Se descargará el modelo de Ingles a Español sólo cuando el usuario lo use por primera vez.
            translator.downloadModelIfNeeded(conditions).await()
            val textoTraducido = translator.translate(textoOriginal).await()
            textoTraducido
        } catch (e: Exception) {
            e.printStackTrace()
            "Error de traducción: ${e.message}"
        } finally {
            translator.close()
        }
    }
}
