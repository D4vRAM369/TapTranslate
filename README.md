# TapTranslate 🌎 -> 🌍 

TapTranslate es una aplicación minimalista para Android diseñada para integrarse directamente en el menú contextual del sistema operativo. Esto permite traducir texto seleccionado (como comentarios en Reddit u otras apps) de forma instántanea sin abrir la aplicación. 

## Características Principales 🚀
*   **100% Lado Cliente:** Usa **Google ML Kit On-Device Translation**. La traducción sucede en el teléfono móvil, garantizando total privacidad y 0 latencia de red.
*   **Invisible y Rápida:** No necesita pantallas engorrosas; intercepta el texto nativamente y lo reemplaza con un Intent `RESULT_OK`.
*   **Modo Escritura y Lectura:** 
    *   Si seleccionas un texto que tú estás escribiendo, al pulsar "TapTranslate" reemplazará el texto escrito por su traducción.
    *   Si sólo estás leyendo y das TapTranslate, te mostrará elegantemente el resultado en una cápsula sin sacarte de la app.

## Estructura
*   `TranslationEngine`: El motor que descarga y ejecuta el modelo IA de más de 30MB la primera vez y lo procesa instantáneamente.
*   `ProcessTextActivity`: El interceptor que sobrecarga el botón del sistema.
*   `MainActivity`: La base fundacional de Compose para una futura UI de configuración.
