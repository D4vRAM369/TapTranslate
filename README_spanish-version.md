# TapTranslate

> **Traducción instantánea, privada y sin internet — directamente desde el menú de selección de texto de cualquier app.**

[![Android](https://img.shields.io/badge/Plataforma-Android%206.0%2B-green?logo=android)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Lenguaje-Kotlin-7F52FF?logo=kotlin)](https://kotlinlang.org)
[![ML Kit](https://img.shields.io/badge/IA-Google%20ML%20Kit-4285F4?logo=google)](https://developers.google.com/ml-kit/language/translation)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-03DAC5?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Licencia](https://img.shields.io/badge/Licencia-MIT-yellow)](LICENSE)

---

## ¿Qué es TapTranslate?

TapTranslate es una utilidad minimalista para Android que se integra directamente en el menú nativo de selección de texto del sistema operativo. Selecciona cualquier texto en cualquier aplicación, pulsa el menú de tres puntitos y elige **TapTranslate** — tu texto se traduce al instante, sin internet, sin que ningún dato abandone tu dispositivo.

Sin anuncios. Sin nube. Sin cuenta. Solo traducción.

---

## Funciones principales

- **⚡ Instantáneo** — Traduce en el mismo lugar usando `ACTION_PROCESS_TEXT`. El texto seleccionado es sustituido automáticamente por su traducción.
- **🔒 100% Privado** — Impulsado por Google ML Kit On-Device Translation. Todo el procesamiento ocurre de manera local. Cero peticiones de red durante la traducción.
- **🌐 Bidireccional** — Alterna entre **Español → Inglés** (Modo Escribir) e **Inglés → Español** (Modo Leer) desde el panel de ajustes.
- **🤖 Auto-Pegado** — Para apps que restringen el menú contextual (por ejemplo, comentarios ajenos en Reddit), comparte el texto hacia TapTranslate y éste lo pega automáticamente usando la API de Accesibilidad de Android.
- **🎨 UI Minimalista** — Construida con Jetpack Compose, siguiendo el patrón arquitectónico **MVVM (Model-View-ViewModel)**.
- **📱 Icono Adaptativo** — Icono profesional (`mipmap-anydpi`) que se adapta a cualquier forma (círculo, cuadrado) sin bordes blancos.
- **🌍 Localizada** — La interfaz se adapta al idioma del sistema (Inglés y Español soportados).
- **📝 Código Limpio** — Constantes centralizadas y estructura de paquetes profesional.

---

## Cómo funciona

```
[El usuario selecciona texto en cualquier app]
         ↓
[El menú del OS muestra TapTranslate]
         ↓
[ProcessTextActivity recibe el texto (Capa de Intercepción)]
         ↓
[MainViewModel gestiona el estado y activa la lógica]
         ↓
[TranslationEngine llama al motor NMT (Neural Machine Translation) local]
         ↓
[El texto traducido es devuelto a la app de origen]
```

Para texto de solo lectura en apps cerradas (posts de Reddit, etc.):
```
[El usuario comparte el texto → TapTranslate]
         ↓
[La traducción se copia al portapapeles]
         ↓
[AutoPasteService (Accesibilidad) inyecta ACTION_PASTE en el campo enfocado]
```

---

## Requisitos

| Requisito | Versión |
|---|---|
| Android OS | 6.0+ (API 24) |
| Target SDK | Android 15 (API 35) |
| Build Tools | Android Gradle Plugin 8.2.0 |
| Gradle | 8.4 |
| Kotlin | 1.9.24 |
| Java | 17 |

---

## Instalación

### Compilar desde el código fuente

1. Clona el repositorio:
   ```bash
   git clone https://github.com/D4vRAM369/TapTranslate.git
   cd TapTranslate
   ```

2. Ábrelo en Android Studio (Hedgehog o posterior).

3. Sincroniza Gradle y ejecútalo en tu dispositivo o emulador (API 24+).

4. En el primer lanzamiento: sigue el carrusel de bienvenida y activa opcionalmente el Servicio de Accesibilidad para el Auto-Pegado.

---

## Configuración inicial

### Paso 1 — Traducción básica (funciona en todas partes)
No se necesita configuración. Tras instalar, selecciona cualquier texto en cualquier app → pulsa ⋯ → elige **TapTranslate**.

### Paso 2 — Modo Auto-Pegado (opcional, para apps restringidas)
Para automatización completa en apps como Reddit:

1. Abre TapTranslate.
2. Toca el banner **⚠️ Súper-Autopegado Desactivado**.
3. En los ajustes de Accesibilidad, busca **TapTranslate** y actívalo.
4. Vuelve a TapTranslate — el banner desaparece confirmando que está activo.

---

## Estructura del proyecto

```
app/
├── src/main/
│   ├── java/com/d4vram/taptranslate/
│   │   ├── AutoPasteService.kt       # Servicio de Accesibilidad — motor de auto-pegado
│   │   ├── MainActivity.kt           # UI de Ajustes (Jetpack Compose)
│   │   ├── OnboardingScreen.kt       # Carrusel de bienvenida (HorizontalPager)
│   │   ├── PreferencesManager.kt     # Envoltorio de SharedPreferences
│   │   ├── ProcessTextActivity.kt    # Interceptor invisible — activador de traducción
│   │   └── TranslationEngine.kt      # Envoltorio de ML Kit — traducción bidireccional
│   ├── res/
│   │   ├── drawable/
│   │   │   └── ic_taptranslate.xml   # Icono vectorial
│   │   ├── values/
│   │   │   └── strings.xml           # Cadenas en español (por defecto)
│   │   ├── values-en/
│   │   │   └── strings.xml           # Cadenas en inglés
│   │   └── xml/
│   │       └── accessibility_service_config.xml
│   └── AndroidManifest.xml
├── build.gradle.kts
└── gradle/wrapper/
    └── gradle-wrapper.properties     # Gradle 8.4 fijado
```

---

## Stack Tecnológico

| Capa | Tecnología |
|---|---|
| Lenguaje | Kotlin |
| UI | Jetpack Compose + Material Design 3 (Patrón MVVM) |
| IA de Traducción | Google ML Kit NMT (Neural Machine Translation) |
| Asincronía | Kotlin Coroutines + `kotlinx-coroutines-play-services` + StateFlow |
| Persistencia | Android SharedPreferences |
| Integración con OS | `ACTION_PROCESS_TEXT`, `ACTION_SEND`, `AccessibilityService` |

---

## Privacidad

TapTranslate **no**:
- Se conecta a ningún servidor externo
- Recopila ningún dato del usuario
- Requiere conexión a internet para traducir

El permiso de Servicio de Accesibilidad se usa exclusivamente para pegar el texto traducido en el campo de entrada enfocado tras usar la ruta de **Compartir**. No lee, registra ni transmite el contenido de la pantalla.

---

## Limitaciones conocidas

- Solo soporta traducción **Español ↔ Inglés** en v1.0. Se planean pares de idiomas adicionales.
- El primer uso requiere una descarga puntual de ~30MB de modelo por par de idiomas.
- El Auto-Pegado por Accesibilidad requiere activación manual del permiso por parte del usuario (política de seguridad de Android).
- Algunas apps pueden suprimir el menú `ACTION_PROCESS_TEXT` completamente (usa la alternativa de Compartir en su lugar).

---

## Hoja de ruta

- [ ] Soporte para pares de idiomas adicionales (FR, DE, PT…)
- [ ] Detección automática de idioma (eliminar el selector manual de modo)
- [x] Refactorización a arquitectura ViewModel + MVVM
- [x] Cobertura de tests unitarios de constantes y lógica base
- [ ] Cobertura de tests de UI (Espresso/Compose Test)
- [ ] Publicación en Play Store

---

## Licencia

Licencia MIT © 2026 D4vRAM369
