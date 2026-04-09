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
 * [Profe]: Mantenemos nuestra actividad invisible, pero ahora leemos 
 * lo que el usuario haya seleccionado en los Ajustes.
 */
class ProcessTextActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val textoMenuContextual = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString()
        val textoCompartido = intent.getStringExtra(Intent.EXTRA_TEXT)
        
        // Asignamos el texto venga del menú de 3 puntos o del menú de Compartir
        val textoSeleccionado = textoMenuContextual ?: textoCompartido
        
        // Si el usuario llega por "Compartir" (ACTION_SEND), siempre será tratado como Sólo Lectura
        // porque Android no permite sustituir de facto el texto cuando es una ventana de compartir genérica.
        val esSoloLectura = intent.getBooleanExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, false) || intent.action == Intent.ACTION_SEND

        if (textoSeleccionado.isNullOrBlank()) {
            Toast.makeText(this, getString(R.string.no_selected_text), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 1. Instanciamos la base de datos preferencial y sacamos qué modo quiere usar ahorita.
        val prefs = PreferencesManager(this)
        val sentidoActual = prefs.getSentidoTraduccion() // ej. "ES_EN" o "EN_ES"

        val motor = TranslationEngine()

        lifecycleScope.launch(Dispatchers.IO) {
            
            // 2. Le pasamos al motor tanto el texto como el sentido de traducción seleccionado.
            val resultado = motor.translateTexto(textoSeleccionado, sentidoActual)

            withContext(Dispatchers.Main) {
                if (esSoloLectura) {
                    // Si el usuario usa "Compartir" o es modo lectura, copiamos el texto automáticamente al portapapeles de Android.
                    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("TapTranslate", resultado)
                    clipboard.setPrimaryClip(clip)
                    
                    // Disparamos la Orden de Auto-Pegado a nuestro servicio de Accesibilidad silente.
                    val pasteIntent = Intent("com.d4vram.taptranslate.PASTE_NOW")
                    pasteIntent.setPackage(packageName)
                    sendBroadcast(pasteIntent)
                    
                    Toast.makeText(this@ProcessTextActivity, getString(R.string.autopaste_toast), Toast.LENGTH_SHORT).show()
                } else {
                    val intentRespuesta = Intent()
                    intentRespuesta.putExtra(Intent.EXTRA_PROCESS_TEXT, resultado)
                    setResult(RESULT_OK, intentRespuesta)
                }
                finish()
            }
        }
    }
}
