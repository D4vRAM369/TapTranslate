package com.d4vram.taptranslate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import android.provider.Settings
import android.accessibilityservice.AccessibilityServiceInfo
import android.view.accessibility.AccessibilityManager
import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource

class MainActivity : ComponentActivity() {

    private fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
        for (service in enabledServices) {
            if (service.id.contains("AutoPasteService")) {
                return true
            }
        }
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val prefs = PreferencesManager(this)
        val haVistoInicio = prefs.haVistoOnboarding()
        
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var showOnboarding by remember { mutableStateOf(!haVistoInicio) }

                    if (showOnboarding) {
                        OnboardingScreen(onFinish = {
                            prefs.marcarOnboardingComoVisto()
                            showOnboarding = false
                        })
                    } else {
                        // [Profe]: Este 'Estado' (sentido) vigilará el botón seleccionado.
                        var sentido by remember { mutableStateOf(prefs.getSentidoTraduccion()) }
                        
                        // Estado para ver si tenemos habilitado el Paste super rápido
                        var isAccessibilityEnabled by remember { mutableStateOf(isAccessibilityServiceEnabled(this@MainActivity)) }

                        // Actualizar estado al volver a la App
                        val lifecycleOwner = LocalLifecycleOwner.current
                        DisposableEffect(lifecycleOwner) {
                            val observer = LifecycleEventObserver { _, event ->
                                if (event == Lifecycle.Event.ON_RESUME) {
                                    isAccessibilityEnabled = isAccessibilityServiceEnabled(this@MainActivity)
                                }
                            }
                            lifecycleOwner.lifecycle.addObserver(observer)
                            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Spacer(Modifier.height(48.dp))
                            
                            // 🚀 NUEVA ALERTA DE PERMISOS
                            if (!isAccessibilityEnabled) {
                                ElevatedButton(
                                    onClick = {
                                        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                                    },
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                                    colors = ButtonDefaults.elevatedButtonColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer,
                                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = stringResource(R.string.warning_title), fontWeight = FontWeight.Bold)
                                        Spacer(Modifier.height(4.dp))
                                        Text(text = stringResource(R.string.warning_desc), textAlign = TextAlign.Center, fontSize = 12.sp)
                                    }
                                }
                            }
                            
                            Text(
                                text = stringResource(R.string.settings_panel), 
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(Modifier.height(16.dp))
                            
                            Text(
                                text = stringResource(R.string.settings_desc),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            
                            Spacer(Modifier.weight(1f))
                            
                            // Botón 1: Español -> Inglés
                            Button(
                                onClick = {
                                    sentido = "ES_EN"
                                    prefs.setSentidoTraduccion("ES_EN") // Salvamos a memoria permanentemente
                                },
                                modifier = Modifier.fillMaxWidth().height(60.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (sentido == "ES_EN") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = if (sentido == "ES_EN") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            ) {
                                Text(stringResource(R.string.mode_write), fontSize = 16.sp)
                            }

                            Spacer(Modifier.height(16.dp))

                            // Botón 2: Inglés -> Español
                            Button(
                                onClick = {
                                    sentido = "EN_ES"
                                    prefs.setSentidoTraduccion("EN_ES") // Salvamos a memoria permanentemente
                                },
                                modifier = Modifier.fillMaxWidth().height(60.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (sentido == "EN_ES") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = if (sentido == "EN_ES") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            ) {
                                Text(stringResource(R.string.mode_read), fontSize = 16.sp)
                            }
                            
                            Spacer(Modifier.weight(2f))
                        }
                    }
                }
            }
        }
    }
}
