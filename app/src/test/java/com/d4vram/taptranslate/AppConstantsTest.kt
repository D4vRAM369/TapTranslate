package com.d4vram.taptranslate

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * [Profe - PBL Fix #2]: Tests Unitarios
 *
 * ¿Qué es un test unitario?
 *   Una función pequeña que verifica que otra función tuya hace lo correcto.
 *   Si cambias código y algún test falla, sabes exactamente QUÉ rompiste
 *   antes de que llegue al usuario.
 *
 * Estos tests NO necesitan Android ni un emulador: se ejecutan en la JVM local
 * de tu ordenador en milisegundos con: ./gradlew testDebugUnitTest
 */
class AppConstantsTest {

    @Test
    fun `SENTIDO_ES_EN debe ser la cadena ES_EN`() {
        // ¿Por qué testear una constante?
        // Para garantizar que nadie la cambia accidentalmente en el futuro.
        assertEquals("ES_EN", AppConstants.SENTIDO_ES_EN)
    }

    @Test
    fun `SENTIDO_EN_ES debe ser la cadena EN_ES`() {
        assertEquals("EN_ES", AppConstants.SENTIDO_EN_ES)
    }

    @Test
    fun `SENTIDO_ES_EN y SENTIDO_EN_ES deben ser distintos`() {
        assert(AppConstants.SENTIDO_ES_EN != AppConstants.SENTIDO_EN_ES)
    }

    @Test
    fun `ACTION_PASTE_NOW debe contener el packageID correcto`() {
        assert(AppConstants.ACTION_PASTE_NOW.contains("com.d4vram.taptranslate"))
    }

    @Test
    fun `PREFS_NAME no debe estar vacío`() {
        assert(AppConstants.PREFS_NAME.isNotBlank())
    }

    @Test
    fun `ambas claves de preferencias deben ser distintas`() {
        assert(AppConstants.KEY_ONBOARDING_SEEN != AppConstants.KEY_TRANSLATION_DIR)
    }
}
