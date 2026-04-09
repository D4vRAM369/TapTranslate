package com.d4vram.taptranslate

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityButtonController
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * AccessibilityService that supports two flows:
 * 1. Existing autopaste after PROCESS_TEXT / SEND / TRANSLATE.
 * 2. "Hostile mode" overlay in selected apps when text selection or copy is detected.
 */
class AutoPasteService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val mainHandler = Handler(Looper.getMainLooper())
    private val motor = TranslationEngine()

    private lateinit var prefs: PreferencesManager
    private lateinit var clipboardManager: ClipboardManager
    private lateinit var windowManager: WindowManager

    private var chipView: View? = null
    private var latestCandidateText: String? = null
    private var latestPackageName: String? = null
    private var lastClipboardText: String? = null

    private val hideChipRunnable = Runnable { hideTranslateChip() }
    private var accessibilityButtonCallback: Any? = null

    private val pasteReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AppConstants.ACTION_PASTE_NOW) {
                mainHandler.postDelayed({
                    pegarTextoAutomaticamente()
                }, 200)
            }
        }
    }

    private val clipboardListener = ClipboardManager.OnPrimaryClipChangedListener {
        if (!prefs.isHostileModeEnabled()) return@OnPrimaryClipChangedListener

        val activePackage = rootInActiveWindow?.packageName?.toString() ?: latestPackageName
        if (!shouldUseHostileMode(activePackage)) return@OnPrimaryClipChangedListener

        val text = readClipboardText() ?: return@OnPrimaryClipChangedListener
        if (text == lastClipboardText) return@OnPrimaryClipChangedListener

        lastClipboardText = text
        showCandidateForHostileMode(text, activePackage)
    }

    override fun onServiceConnected() {
        super.onServiceConnected()

        prefs = PreferencesManager(this)
        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val filter = IntentFilter(AppConstants.ACTION_PASTE_NOW)
        ContextCompat.registerReceiver(this, pasteReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        clipboardManager.addPrimaryClipChangedListener(clipboardListener)

        serviceInfo = serviceInfo.apply {
            flags = flags or
                AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or
                AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerAccessibilityButtonCallbackIfSupported()
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return

        latestPackageName = event.packageName?.toString() ?: latestPackageName

        if (!shouldUseHostileMode(latestPackageName)) {
            hideTranslateChip()
            return
        }

        when (event.eventType) {
            AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED -> {
                val selectedText = extractSelectedText(event.source)
                if (selectedText.isNullOrBlank()) {
                    hideTranslateChip()
                } else {
                    showCandidateForHostileMode(selectedText, latestPackageName)
                }
            }

            AccessibilityEvent.TYPE_VIEW_FOCUSED,
            AccessibilityEvent.TYPE_VIEW_CLICKED,
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                val selectedText = extractSelectedText(event.source)
                if (!selectedText.isNullOrBlank()) {
                    showCandidateForHostileMode(selectedText, latestPackageName)
                }
            }
        }
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        super.onDestroy()
        hideTranslateChip()
        clipboardManager.removePrimaryClipChangedListener(clipboardListener)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            unregisterAccessibilityButtonCallbackIfSupported()
        }
        unregisterReceiver(pasteReceiver)
        motor.cerrarTodo()
        serviceScope.cancel()
    }

    private fun shouldUseHostileMode(packageName: String?): Boolean {
        if (!prefs.isHostileModeEnabled()) return false
        val targetPackage = packageName ?: return false
        return prefs.getHostilePackages().contains(targetPackage)
    }

    private fun extractSelectedText(node: AccessibilityNodeInfo?): String? {
        node ?: return null

        val text = node.text?.toString().orEmpty()
        val start = node.textSelectionStart
        val end = node.textSelectionEnd

        if (text.isNotBlank() && start >= 0 && end >= 0 && start != end) {
            val from = minOf(start, end)
            val to = maxOf(start, end)
            if (from < to && to <= text.length) {
                return text.substring(from, to).trim().takeIf { it.isNotEmpty() }
            }
        }

        val fallbackText = node.text?.toString()?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            fallbackText.takeIf { node.isTextSelectable }
        } else {
            fallbackText
        }
    }

    private fun showCandidateForHostileMode(text: String, packageName: String?) {
        latestCandidateText = text
        latestPackageName = packageName
        ensureTranslateChip()
        resetChipAutoHide()
    }

    private fun ensureTranslateChip() {
        val existingView = chipView
        if (existingView != null) {
            bindChipDirection(existingView)
            return
        }

        val chip = LayoutInflater.from(this)
            .inflate(R.layout.overlay_translate_chip, null, false)
            .apply {
                alpha = 0f
                setOnClickListener { translateLatestCandidate() }
            }
        bindChipDirection(chip)

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.END or Gravity.BOTTOM
            x = dp(20)
            y = dp(210)
        }

        chipView = chip
        windowManager.addView(chip, layoutParams)
        chip.animate()
            .alpha(1f)
            .translationYBy(-dp(6).toFloat())
            .setDuration(180L)
            .start()
    }

    private fun hideTranslateChip() {
        mainHandler.removeCallbacks(hideChipRunnable)
        chipView?.let { view ->
            windowManager.removeView(view)
        }
        chipView = null
        latestCandidateText = null
    }

    private fun resetChipAutoHide() {
        mainHandler.removeCallbacks(hideChipRunnable)
        mainHandler.postDelayed(hideChipRunnable, 4500)
    }

    private fun translateLatestCandidate() {
        val sourceText = latestCandidateText?.trim().takeIf { !it.isNullOrEmpty() } ?: return
        hideTranslateChip()
        translateText(sourceText)
    }

    private fun translateManuallyFromCurrentContext() {
        val sourceText = resolveCurrentSourceText()
        if (sourceText.isNullOrBlank()) {
            showToast(getString(R.string.no_text_for_manual_translate))
            return
        }

        translateText(sourceText)
    }

    private fun resolveCurrentSourceText(): String? {
        latestCandidateText?.trim()?.takeIf { it.isNotEmpty() }?.let { return it }

        findSelectedText(rootInActiveWindow)?.let { return it }

        val focusedNode = rootInActiveWindow?.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
            ?: rootInActiveWindow?.findFocus(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY)
        extractSelectedText(focusedNode)?.let { return it }

        return readClipboardText()
    }

    private fun findSelectedText(node: AccessibilityNodeInfo?): String? {
        node ?: return null

        extractSelectedText(node)?.let { return it }

        for (index in 0 until node.childCount) {
            findSelectedText(node.getChild(index))?.let { return it }
        }

        return null
    }

    private fun translateText(sourceText: String) {
        serviceScope.launch {
            val sentido = prefs.getSentidoTraduccion()
            val resultado = withContext(Dispatchers.IO) {
                motor.translateTexto(sourceText, sentido)
            }

            resultado.fold(
                onSuccess = { textoTraducido ->
                    lastClipboardText = textoTraducido
                    clipboardManager.setPrimaryClip(
                        ClipData.newPlainText(getString(R.string.translate_chip_label), textoTraducido)
                    )

                    mainHandler.postDelayed({
                        val pasted = pegarTextoAutomaticamente()
                        if (pasted) {
                            showToast(getString(R.string.autopaste_toast))
                        } else {
                            showToast(getString(R.string.copied_to_clipboard_toast))
                        }
                    }, 120)
                },
                onFailure = {
                    showToast(getString(R.string.translation_error))
                }
            )
        }
    }

    private fun pegarTextoAutomaticamente(): Boolean {
        val targetNode = rootInActiveWindow?.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
        return targetNode?.performAction(AccessibilityNodeInfo.ACTION_PASTE) == true
    }

    private fun readClipboardText(): String? {
        val clip = clipboardManager.primaryClip ?: return null
        if (clip.itemCount == 0) return null
        return clip.getItemAt(0).coerceToText(this)?.toString()?.trim()?.takeIf { it.isNotEmpty() }
    }

    private fun showToast(message: String) {
        mainHandler.post {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    @androidx.annotation.RequiresApi(Build.VERSION_CODES.O)
    private fun registerAccessibilityButtonCallbackIfSupported() {
        val callback = object : AccessibilityButtonController.AccessibilityButtonCallback() {
            override fun onClicked(controller: AccessibilityButtonController) {
                translateManuallyFromCurrentContext()
            }
        }
        accessibilityButtonCallback = callback
        accessibilityButtonController.registerAccessibilityButtonCallback(callback, mainHandler)
    }

    @androidx.annotation.RequiresApi(Build.VERSION_CODES.O)
    private fun unregisterAccessibilityButtonCallbackIfSupported() {
        val callback = accessibilityButtonCallback as? AccessibilityButtonController.AccessibilityButtonCallback
            ?: return
        accessibilityButtonController.unregisterAccessibilityButtonCallback(callback)
        accessibilityButtonCallback = null
    }

    private fun bindChipDirection(chip: View) {
        val subtitle = chip.findViewById<TextView>(R.id.overlayChipSubtitle)
        val directionLabel = if (prefs.getSentidoTraduccion() == AppConstants.SENTIDO_ES_EN) {
            getString(R.string.translate_chip_direction_es_en)
        } else {
            getString(R.string.translate_chip_direction_en_es)
        }
        subtitle.text = directionLabel
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
