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
 * Accessibility Service that listens for a broadcast and auto-pastes
 * the clipboard content into the focused input field.
 */
class AutoPasteService : AccessibilityService() {

    // BroadcastReceiver that triggers the paste action when signaled by ProcessTextActivity.
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
        val filter = IntentFilter(AppConstants.ACTION_PASTE_NOW)
        ContextCompat.registerReceiver(this, pasteReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) { /* Not needed */ }

    override fun onInterrupt() {}

    private fun pegarTextoAutomaticamente() {
        val targetNode = rootInActiveWindow?.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
        targetNode?.performAction(AccessibilityNodeInfo.ACTION_PASTE)
        // node.recycle() removed — deprecated since API 33. OS manages node lifecycle.
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(pasteReceiver)
    }
}
