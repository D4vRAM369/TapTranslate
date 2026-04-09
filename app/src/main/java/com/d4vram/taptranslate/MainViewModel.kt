package com.d4vram.taptranslate

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.view.accessibility.AccessibilityManager
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * [Profe - PBL Fix #3]: ViewModel — La memoria que sobrevive a todo.
 *
 * ANTES (❌ estado en onCreate — se pierde al rotar pantalla):
 *   var sentido by remember { mutableStateOf(prefs.getSentidoTraduccion()) }
 *
 * DESPUÉS (✅ estado en ViewModel — sobrevive rotaciones y recreaciones):
 *   val sentido: StateFlow<String> = _sentido.asStateFlow()
 *
 * ¿Qué es StateFlow?
 *   Es un contenedor observable de un valor. Cuando su valor cambia,
 *   Compose recompone (redibuja) automáticamente solo las partes afectadas.
 *   Es el equivalente Compose de LiveData, pero moderno y basado en Coroutines.
 */
class MainViewModel : ViewModel() {

    // ─── Estado del sentido de traducción ────────────────────────────────────
    // MutableStateFlow = la versión privada que podemos modificar dentro del ViewModel
    private val _sentido = MutableStateFlow(AppConstants.SENTIDO_ES_EN)

    // StateFlow = la versión pública, solo de lectura, que expone la UI
    val sentido: StateFlow<String> = _sentido.asStateFlow()

    // ─── Estado del permiso de Accesibilidad ─────────────────────────────────
    private val _isAccessibilityEnabled = MutableStateFlow(false)
    val isAccessibilityEnabled: StateFlow<Boolean> = _isAccessibilityEnabled.asStateFlow()

    /** Inicializa el estado leyendo las preferencias guardadas. */
    fun init(prefs: PreferencesManager) {
        _sentido.value = prefs.getSentidoTraduccion()
    }

    /** Cambia el sentido y lo persiste en SharedPreferences. */
    fun setSentido(nuevoSentido: String, prefs: PreferencesManager) {
        _sentido.value = nuevoSentido
        prefs.setSentidoTraduccion(nuevoSentido)
    }

    /** Refresca el estado de Accesibilidad leyendo el sistema. Llamar en ON_RESUME. */
    fun refreshAccessibilityState(context: Context) {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        _isAccessibilityEnabled.value = am
            .getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
            .any { it.id.contains("AutoPasteService") }
    }
}
