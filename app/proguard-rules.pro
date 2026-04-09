# ─── Reglas de ProGuard para TapTranslate ────────────────────────────────────
# ProGuard encoge y ofusca el código en builds de Release.
# Sin estas reglas, podría eliminar clases que ML Kit necesita en tiempo de ejecución.

# Mantener las clases de Google ML Kit Translate
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.internal.mlkit_translate.** { *; }

# Mantener Kotlin Coroutines
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Mantener las clases propias de la app (no ofuscar los nombres)
-keep class com.d4vram.taptranslate.** { *; }

# Mantener el AccessibilityService (Android lo busca por nombre)
-keep class com.d4vram.taptranslate.AutoPasteService { *; }
