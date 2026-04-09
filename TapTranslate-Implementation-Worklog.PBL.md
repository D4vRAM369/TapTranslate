# PBL: Implementación ejecutada para ampliar el alcance de TapTranslate

## 1. Contexto

TapTranslate ya funcionaba con `ACTION_PROCESS_TEXT` y `ACTION_SEND`, y disponía de un `AccessibilityService` enfocado a autopaste.

El objetivo de esta iteración fue ampliar el alcance real de la app en Android sin perder su identidad como acción rápida e invisible llamada `TapTranslate`.

## 2. Problema abordado

La cobertura de TapTranslate seguía dependiendo de que la app anfitriona expusiera menús de texto cooperativos.

Consecuencias:

- En apps permisivas, TapTranslate aparecía.
- En apps cerradas o con toolbars propias, TapTranslate no aparecía.
- El servicio de accesibilidad existente no resolvía la visibilidad, solo el pegado posterior.

## 3. Objetivos de la implementación

1. Añadir una puerta oficial extra mediante `ACTION_TRANSLATE`.
2. Mantener la etiqueta visible `TapTranslate`.
3. Crear un modo “hostil” opcional para apps concretas, no global.
4. Reutilizar el flujo actual de traducción y autopaste sin abrir una UI intermedia.
5. Mantener la configuración controlada por el usuario desde la app.

## 4. Decisiones tomadas

### 4.1. Añadir `ACTION_TRANSLATE`

Se añadió un `intent-filter` para `android.intent.action.TRANSLATE` en la misma `ProcessTextActivity`.

Razón:

- aprovechar otra vía oficial del sistema
- converger en el mismo pipeline de traducción
- no duplicar activities ni lógica

### 4.2. Mantener `TapTranslate` como identidad visible

No se cambió el label de la acción.

Razón:

- el nombre de marca es parte del valor del producto
- el usuario quiere identificar la app claramente en los menús y overlays

### 4.3. Crear modo hostil limitado por paquetes

Se añadió un modo hostil opcional con selección de apps instaladas.

Razón:

- evitar overlays globales ruidosos
- limitar el riesgo de interferencia con apps no objetivo
- permitir activar Telegram, Brave u otras apps concretas

### 4.4. Reutilizar el servicio de accesibilidad existente

En lugar de crear otro servicio, se amplió `AutoPasteService`.

Razón:

- menos superficie técnica
- menos duplicación
- conserva el comportamiento de autopaste ya existente

## 5. Cambios implementados

### 5.1. Manifest

Archivo:

- [AndroidManifest.xml](/home/defcon/2026/Proyectos/Abril/TranslateIn/app/src/main/AndroidManifest.xml)

Cambios:

- se añadió `ACTION_TRANSLATE` a `ProcessTextActivity`
- se mantuvo `PROCESS_TEXT`
- se mantuvo `SEND`

Impacto:

- TapTranslate puede ser invocada ahora por tres puertas oficiales distintas

### 5.2. Activity de entrada

Archivo:

- [ProcessTextActivity.kt](/home/defcon/2026/Proyectos/Abril/TranslateIn/app/src/main/java/com/d4vram/taptranslate/ProcessTextActivity.kt)

Cambios:

- ahora distingue entre `PROCESS_TEXT`, `TRANSLATE` y `SEND`
- trata `TRANSLATE` y `SEND` como flujos de solo lectura
- sigue traduciendo sin mostrar una pantalla visible

Impacto:

- todas las entradas convergen en la misma lógica de traducción

### 5.3. Preferencias y estado

Archivos:

- [AppConstants.kt](/home/defcon/2026/Proyectos/Abril/TranslateIn/app/src/main/java/com/d4vram/taptranslate/AppConstants.kt)
- [PreferencesManager.kt](/home/defcon/2026/Proyectos/Abril/TranslateIn/app/src/main/java/com/d4vram/taptranslate/PreferencesManager.kt)
- [MainViewModel.kt](/home/defcon/2026/Proyectos/Abril/TranslateIn/app/src/main/java/com/d4vram/taptranslate/MainViewModel.kt)

Cambios:

- se añadieron claves para activar o desactivar el modo hostil
- se añadió persistencia de paquetes seleccionados
- se cargó la lista de apps instaladas lanzables

Impacto:

- el usuario puede decidir en qué apps quiere overlay

### 5.4. UI de configuración

Archivo:

- [MainActivity.kt](/home/defcon/2026/Proyectos/Abril/TranslateIn/app/src/main/java/com/d4vram/taptranslate/MainActivity.kt)

Cambios:

- nueva tarjeta “Modo hostil”
- switch para activar o desactivar
- selector de apps objetivo con checkboxes

Impacto:

- el modo hostil queda controlado desde ajustes

### 5.5. Servicio de accesibilidad

Archivos:

- [AutoPasteService.kt](/home/defcon/2026/Proyectos/Abril/TranslateIn/app/src/main/java/com/d4vram/taptranslate/AutoPasteService.kt)
- [accessibility_service_config.xml](/home/defcon/2026/Proyectos/Abril/TranslateIn/app/src/main/res/xml/accessibility_service_config.xml)

Cambios:

- el servicio ahora escucha selección de texto y cambios de ventana relevantes
- se añadió detección de texto seleccionado
- se añadió detección de texto recién copiado
- se añadió un chip overlay minimalista `TapTranslate`
- al tocar el chip, traduce según el sentido actual ES→EN o EN→ES
- el resultado se copia al portapapeles y, si existe un campo enfocado, intenta pegar automáticamente
- se evitó un bucle por portapapeles repitiendo el chip tras traducir

Impacto:

- se habilita un fallback práctico para apps seleccionadas que no cooperan con los intents estándar

### 5.6. Strings

Archivos:

- [strings.xml](/home/defcon/2026/Proyectos/Abril/TranslateIn/app/src/main/res/values/strings.xml)
- [strings.xml](/home/defcon/2026/Proyectos/Abril/TranslateIn/app/src/main/res/values-en/strings.xml)

Cambios:

- nuevos textos para la UI del modo hostil
- nuevo texto para feedback del chip flotante

## 6. Funcionamiento final esperado

### Caso A. App cooperativa con `PROCESS_TEXT`

- el usuario selecciona texto
- la app anfitriona muestra TapTranslate en el menú
- TapTranslate traduce
- si es editable, devuelve el resultado al host

### Caso B. App cooperativa con `SEND`

- el usuario comparte el texto
- TapTranslate aparece como destino
- traduce
- copia el resultado y lanza autopaste si hay un input enfocado

### Caso C. App cooperativa con `TRANSLATE`

- la app anfitriona lanza `android.intent.action.TRANSLATE`
- TapTranslate recibe `EXTRA_TEXT`
- traduce
- copia y/o pega según el flujo de solo lectura

### Caso D. App hostil seleccionada

- el usuario activa el modo hostil
- marca apps concretas
- en esas apps, si accesibilidad detecta selección o copiado, aparece un chip `TapTranslate`
- al tocarlo, se traduce el texto y se intenta pegar automáticamente

## 7. Resultado técnico

La app queda mejor preparada para:

- apps que usan el menú estándar
- apps que usan compartir
- apps que usan una acción explícita de traducir
- apps seleccionadas donde solo es viable un fallback por overlay

## 8. Riesgos restantes

- Telegram y apps similares pueden no exponer suficiente información de selección a accesibilidad
- algunos OEM pueden alterar toolbars y foco de inputs
- el chip puede funcionar mejor con texto copiado que con selección viva en ciertas apps
- la política de accesibilidad de Google Play sigue siendo un factor a revisar si se amplía el uso del servicio

## 9. Verificación realizada

Se ejecutó:

```bash
./gradlew testDebugUnitTest assembleDebug
```

Resultado:

- compilación correcta
- tests unitarios correctos
- APK debug ensamblado correctamente

## 10. Simplificaciones aplicadas

- no se creó una nueva activity para `TRANSLATE`
- no se cambió el branding visible de `TapTranslate`
- no se añadió un overlay global siempre activo
- no se introdujeron dependencias nuevas
- se reutilizó el servicio de accesibilidad existente

## 11. Archivos modificados

- [AndroidManifest.xml](/home/defcon/2026/Proyectos/Abril/TranslateIn/app/src/main/AndroidManifest.xml)
- [AppConstants.kt](/home/defcon/2026/Proyectos/Abril/TranslateIn/app/src/main/java/com/d4vram/taptranslate/AppConstants.kt)
- [AutoPasteService.kt](/home/defcon/2026/Proyectos/Abril/TranslateIn/app/src/main/java/com/d4vram/taptranslate/AutoPasteService.kt)
- [MainActivity.kt](/home/defcon/2026/Proyectos/Abril/TranslateIn/app/src/main/java/com/d4vram/taptranslate/MainActivity.kt)
- [MainViewModel.kt](/home/defcon/2026/Proyectos/Abril/TranslateIn/app/src/main/java/com/d4vram/taptranslate/MainViewModel.kt)
- [PreferencesManager.kt](/home/defcon/2026/Proyectos/Abril/TranslateIn/app/src/main/java/com/d4vram/taptranslate/PreferencesManager.kt)
- [ProcessTextActivity.kt](/home/defcon/2026/Proyectos/Abril/TranslateIn/app/src/main/java/com/d4vram/taptranslate/ProcessTextActivity.kt)
- [strings.xml](/home/defcon/2026/Proyectos/Abril/TranslateIn/app/src/main/res/values/strings.xml)
- [strings.xml](/home/defcon/2026/Proyectos/Abril/TranslateIn/app/src/main/res/values-en/strings.xml)
- [accessibility_service_config.xml](/home/defcon/2026/Proyectos/Abril/TranslateIn/app/src/main/res/xml/accessibility_service_config.xml)
- [TapTranslate-Android-Selection-Reach.PBL.md](/home/defcon/2026/Proyectos/Abril/TranslateIn/TapTranslate-Android-Selection-Reach.PBL.md)

## 12. Próximos pasos sugeridos

1. Probar Telegram, Brave, Taskito y al menos una app propia.
2. Medir si Telegram expone selección, copiado o ninguno de los dos.
3. Ajustar si el chip debe reaccionar solo a selección, solo a copiado o a ambos según paquete.
4. Añadir exclusiones para campos sensibles si se amplía el modo hostil.
