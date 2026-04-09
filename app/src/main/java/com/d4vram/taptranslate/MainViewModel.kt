package com.d4vram.taptranslate

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.content.Context
import android.content.pm.PackageManager
import android.view.accessibility.AccessibilityManager
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class InstalledAppInfo(
    val label: String,
    val packageName: String
)

/**
 * Holds and manages UI state for the settings screen.
 * Survives Activity recreation (screen rotation, system kill).
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

    private val _isHostileModeEnabled = MutableStateFlow(false)
    val isHostileModeEnabled: StateFlow<Boolean> = _isHostileModeEnabled.asStateFlow()

    private val _selectedHostilePackages = MutableStateFlow<Set<String>>(emptySet())
    val selectedHostilePackages: StateFlow<Set<String>> = _selectedHostilePackages.asStateFlow()

    private val _availableApps = MutableStateFlow<List<InstalledAppInfo>>(emptyList())
    val availableApps: StateFlow<List<InstalledAppInfo>> = _availableApps.asStateFlow()

    /** Inicializa el estado leyendo las preferencias guardadas. */
    fun init(context: Context, prefs: PreferencesManager) {
        _sentido.value = prefs.getSentidoTraduccion()
        _isHostileModeEnabled.value = prefs.isHostileModeEnabled()
        _selectedHostilePackages.value = prefs.getHostilePackages()

        if (_availableApps.value.isEmpty()) {
            _availableApps.value = loadLaunchableApps(context)
        }
    }

    /** Cambia el sentido y lo persiste en SharedPreferences. */
    fun setSentido(nuevoSentido: String, prefs: PreferencesManager) {
        _sentido.value = nuevoSentido
        prefs.setSentidoTraduccion(nuevoSentido)
    }

    fun setHostileModeEnabled(enabled: Boolean, prefs: PreferencesManager) {
        _isHostileModeEnabled.value = enabled
        prefs.setHostileModeEnabled(enabled)
    }

    fun toggleHostilePackage(packageName: String, prefs: PreferencesManager) {
        val next = _selectedHostilePackages.value.toMutableSet().apply {
            if (!add(packageName)) remove(packageName)
        }
        _selectedHostilePackages.value = next
        prefs.setHostilePackages(next)
    }

    /** Refresca el estado de Accesibilidad leyendo el sistema. Llamar en ON_RESUME. */
    fun refreshAccessibilityState(context: Context) {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        _isAccessibilityEnabled.value = am
            .getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
            .any { it.id.contains("AutoPasteService") }
    }

    @Suppress("DEPRECATION")
    private fun loadLaunchableApps(context: Context): List<InstalledAppInfo> {
        val packageManager = context.packageManager
        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)

        return packageManager.queryIntentActivities(launcherIntent, PackageManager.MATCH_ALL)
            .asSequence()
            .map { resolveInfo ->
                InstalledAppInfo(
                    label = resolveInfo.loadLabel(packageManager).toString(),
                    packageName = resolveInfo.activityInfo.packageName
                )
            }
            .filter { it.packageName != context.packageName }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
            .toList()
    }
}
