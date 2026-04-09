package com.d4vram.taptranslate

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.content.ContextCompat

/**
 * [Profe]: El héroe de la película. Este servicio se queda dormido 
 * esperando la señal del Broadcast para inyectar un "Ctrl+V" nativo.
 */
class AutoPasteService : AccessibilityService() {

    // 1. Un receptor para escuchar los gritos de ProcessTextActivity diciendo "¡Ya copié, Pega!"
    private val pasteReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.d4vram.taptranslate.PASTE_NOW") {
                // Le damos 200 milisegundos de respiro al sistema para que el menú de 'Compartir' 
                // se desvanezca y la caja de texto (EditText) de Reddit vuelva a recuperar el foco.
                Handler(Looper.getMainLooper()).postDelayed({
                    pegarTextoAutomaticamente()
                }, 200)
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        // 2. Registramos el receptor a prueba de fallos desde API 24 hasta 35+.
        val filter = IntentFilter("com.d4vram.taptranslate.PASTE_NOW")
        ContextCompat.registerReceiver(this, pasteReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // No necesitamos inspeccionar pasivamente la pantalla gastando batería. Se queda vacío.
    }

    override fun onInterrupt() {}

    private fun pegarTextoAutomaticamente() {
        // 3. Busca el "nodo" (dibujo) en pantalla activa que tiene actualmente el estado "Enfocado con Teclado".
        val targetNode = rootInActiveWindow?.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
        
        targetNode?.let { node ->
            // 4. ¡LA MAGIA! Le exigimos a la caja que ejecute su acción innata de Pegar (Paste).
            node.performAction(AccessibilityNodeInfo.ACTION_PASTE)
            node.recycle() // Limpieza de memoria
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(pasteReceiver)
    }
}
