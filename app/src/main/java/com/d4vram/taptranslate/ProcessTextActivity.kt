package com.d4vram.taptranslate

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Invisible Activity that intercepts text from ACTION_PROCESS_TEXT (3-dot menu)
 * and ACTION_SEND (Share menu), translates it, then either replaces or copies the result.
 */
class ProcessTextActivity : ComponentActivity() {

    private val motor = TranslationEngine()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val textoSeleccionado = when (intent.action) {
            Intent.ACTION_PROCESS_TEXT ->
                intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString()
            Intent.ACTION_TRANSLATE ->
                intent.getCharSequenceExtra(Intent.EXTRA_TEXT)?.toString()
            Intent.ACTION_SEND ->
                intent.getStringExtra(Intent.EXTRA_TEXT)
            else ->
                intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString()
                    ?: intent.getStringExtra(Intent.EXTRA_TEXT)
        }

        val esSoloLectura = intent.action != Intent.ACTION_PROCESS_TEXT
                || intent.getBooleanExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, false)

        if (textoSeleccionado.isNullOrBlank()) {
            Toast.makeText(this, getString(R.string.no_selected_text), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val prefs = PreferencesManager(this)
        val sentidoActual = prefs.getSentidoTraduccion()

        lifecycleScope.launch(Dispatchers.IO) {
            val resultado = motor.translateTexto(textoSeleccionado, sentidoActual)

            withContext(Dispatchers.Main) {
                // Result.fold: rama onSuccess y rama onFailure limpias y separadas
                resultado.fold(
                    onSuccess = { textoTraducido ->
                        if (esSoloLectura) {
                            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("TapTranslate", textoTraducido))

                            val pasteIntent = Intent(AppConstants.ACTION_PASTE_NOW)
                            pasteIntent.setPackage(packageName)
                            sendBroadcast(pasteIntent)

                            Toast.makeText(this@ProcessTextActivity,
                                getString(R.string.autopaste_toast), Toast.LENGTH_SHORT).show()
                        } else {
                            val intentRespuesta = Intent()
                            intentRespuesta.putExtra(Intent.EXTRA_PROCESS_TEXT, textoTraducido)
                            setResult(RESULT_OK, intentRespuesta)
                        }
                    },
                    onFailure = {
                        // FIX #5: Ya no pegamos el mensaje de error como traducción
                        Toast.makeText(this@ProcessTextActivity,
                            getString(R.string.translation_error), Toast.LENGTH_LONG).show()
                    }
                )
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        motor.cerrarTodo()
    }
}
