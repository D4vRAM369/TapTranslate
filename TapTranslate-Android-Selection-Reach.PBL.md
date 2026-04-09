# PBL: Ampliar el alcance de TapTranslate en selección de texto Android

## 1. Problema

TapTranslate ya se integra con `ACTION_PROCESS_TEXT`, por lo que aparece en el menú estándar de selección de texto de Android cuando la aplicación anfitriona usa ese flujo oficial.

El problema es que muchas aplicaciones no usan ese menú estándar o implementan componentes de texto personalizados, como:

- `WebView` con toolbar propia
- `EditText` custom
- Renderizado de texto dentro de canvas o capas privadas
- Menús contextuales internos de Chrome, Edge u otras apps

Como resultado, TapTranslate pierde visibilidad en una parte importante de los escenarios reales de uso.

## 2. Objetivo

Maximizar la capacidad de TapTranslate para recibir texto seleccionado desde aplicaciones Android, sin depender únicamente de `ACTION_PROCESS_TEXT`.

## 3. Estado actual real del proyecto

Situación actual observada en el código:

- TapTranslate ya recibe `ACTION_PROCESS_TEXT`
- TapTranslate ya recibe `ACTION_SEND`
- TapTranslate ya tiene un `AccessibilityService`, pero hoy está orientado a autopaste y no a detectar selección para mostrar un botón flotante
- TapTranslate todavía no declara `ACTION_TRANSLATE`

Impacto actual:

- La app ya tiene dos puertas de entrada: menú estándar de selección y menú de compartir
- Si una app no expone ninguna de esas dos vías, TapTranslate sigue sin aparecer
- El servicio de accesibilidad actual ayuda después de recibir el texto, pero no resuelve por sí solo la visibilidad en apps con selección cerrada

## 4. Opciones evaluadas

| Opción | ¿Necesita accesibilidad activada? | ¿Quién decide si TapTranslate aparece? | Cobertura esperada | Ventaja | Límite principal |
|---|---|---|---|---|---|
| `ACTION_PROCESS_TEXT` | No | La app tercera | Media | Es la integración oficial más limpia | No funciona si la app usa menú propio |
| `ACTION_TRANSLATE` | No | La app tercera o el sistema | Baja a media | Añade una vía oficial adicional | Muchas apps no lo usan |
| `ACTION_SEND` | No | La app tercera | Media | Permite entrar por flujos de compartir texto | No equivale al menú contextual de selección |
| `AccessibilityService` + overlay | Sí | TapTranslate, si detecta selección | Media a alta | Sirve como plan B cuando nadie invoca intents | Requiere permiso sensible y no cubre el 100% |
| Integración directa con toolbar de Chrome/Edge | No aplicable | Chrome/Edge | Baja e impredecible | Sería transparente para el usuario si existiera | No se puede forzar desde una app externa |

## 5. Hallazgos clave

### 5.1. Lo que sí se puede hacer

- Mantener `ACTION_PROCESS_TEXT`
- Añadir `ACTION_TRANSLATE`
- Añadir `ACTION_SEND`
- Implementar un `AccessibilityService` para detectar selección de texto cuando sea visible para accesibilidad
- Mostrar un botón flotante propio como fallback

### 5.2. Lo que no se puede forzar

- No existe un metadata de Manifest que obligue a una app tercera a incluir TapTranslate en su menú contextual privado
- No existe una API pública para insertar un botón propio dentro de la toolbar de selección de Chrome o Edge
- Si una app no expone la selección al sistema ni a accesibilidad, TapTranslate no podrá intervenir de forma fiable

## 6. Decisión recomendada

Adoptar una estrategia de tres capas:

### Capa 1. Integraciones oficiales existentes y ampliadas

Mantener e incorporar:

- `ACTION_PROCESS_TEXT`
- `ACTION_SEND`
- `ACTION_TRANSLATE`

Objetivo:
capturar todos los casos donde la app tercera sí coopera con mecanismos estándar de Android.

### Capa 2. Fallback de máxima cobertura

Implementar:

- `AccessibilityService`
- detección de selección visible
- botón flotante propio para traducir

Objetivo:
seguir funcionando incluso cuando la app tercera no muestra el menú estándar.

### Capa 3. Compatibilidad oportunista con navegadores

Tratar Chrome, Edge y apps similares como integración oportunista:

- si resuelven `PROCESS_TEXT` o `TRANSLATE`, TapTranslate entra
- si no lo hacen, depender del fallback por accesibilidad

Objetivo:
no diseñar la estrategia alrededor de una integración que TapTranslate no controla.

## 7. Cambios respecto al comportamiento actual

Antes:

- TapTranslate ya entra por `PROCESS_TEXT`
- TapTranslate ya entra por `SEND`
- TapTranslate usa accesibilidad para pegar automáticamente después de traducir
- Si la app tercera no muestra ni menú contextual estándar ni compartir, TapTranslate no entra

Después:

- TapTranslate mantiene `PROCESS_TEXT`
- TapTranslate mantiene `SEND`
- TapTranslate gana una puerta oficial adicional: `TRANSLATE`
- TapTranslate amplía accesibilidad para detectar selección y mostrar un botón flotante cuando sea posible

Resultado esperado:

- mayor cobertura general
- menos dependencia de la app tercera
- experiencia más consistente entre apps

## 8. Riesgos y costes

### Riesgos técnicos

- El evento de selección por accesibilidad no aparecerá en todas las apps
- Algunas apps exponen texto, pero no la selección concreta
- El posicionamiento del overlay puede requerir ajustes por fabricante o versión de Android

### Riesgos de producto

- El usuario debe activar accesibilidad manualmente
- Algunos usuarios pueden desconfiar de ese permiso
- El uso de accesibilidad debe justificarse claramente en onboarding y políticas

### Riesgos de compliance

- El uso de `AccessibilityService` debe cumplir la política de Google Play
- No debe presentarse como accesibilidad si su objetivo real es productividad sin encajar en los usos permitidos
- La documentación, disclosure y propósito del servicio deben estar muy claros

## 9. Plan de implementación por fases

### Fase 1. Ampliación de intents

Implementar:

- mantener `ACTION_PROCESS_TEXT`
- añadir `ACTION_TRANSLATE`
- añadir `ACTION_SEND`

Resultado esperado:

- mejora inmediata con bajo riesgo
- sin permisos sensibles

### Fase 2. Servicio de accesibilidad

Implementar:

- `AccessibilityService`
- escucha de eventos de selección
- extracción del texto seleccionado cuando sea posible
- overlay flotante para abrir TapTranslate

Resultado esperado:

- aumento real de cobertura en apps no compatibles con el menú estándar

### Fase 3. Endurecimiento

Implementar:

- debounce de eventos
- lista blanca o negra por paquetes
- exclusión de campos sensibles
- métricas locales de efectividad por app
- UX de onboarding para permiso de accesibilidad

Resultado esperado:

- menos ruido
- mejor estabilidad
- menor riesgo de mala experiencia de usuario

## 10. Recomendación final

La mejor decisión no es sustituir `ACTION_PROCESS_TEXT`, sino convertirlo en una parte de una estrategia más amplia.

Recomendación final:

1. Mantener `PROCESS_TEXT`
2. Mantener `SEND`
3. Añadir `TRANSLATE`
4. Ampliar el `AccessibilityService` actual para usarlo también como fallback visual
5. No depender de una integración específica con Chrome o Edge

## 11. Conclusión ejecutiva

TapTranslate no puede obligar a apps terceras a mostrar su acción en menús privados.

Sí puede:

- aprovechar mejor los mecanismos oficiales del sistema
- añadir más puertas de entrada estándar
- usar accesibilidad como plan B para cubrir los casos donde las apps no cooperan

La arquitectura recomendada mejora claramente el alcance real de la app en Android, pero la cobertura total nunca será del 100% porque parte del control sigue estando del lado de cada app tercera.
