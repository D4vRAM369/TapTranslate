package com.d4vram.taptranslate

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * [Profe - PBL Fix #3]: MainActivity ahora es solo un orquestador.
 *
 * Su única responsabilidad: crear el ViewModel y pasarle el Composable.
 * TODO el estado vive en MainViewModel, fuera de la Activity.
 *
 * collectAsStateWithLifecycle(): convierte un StateFlow en un State<T> de Compose
 * que solo observa el valor cuando la pantalla está activa (lifecycle-aware).
 */
class MainActivity : ComponentActivity() {

    // viewModels() returns existing instance or creates one, surviving configuration changes.
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = PreferencesManager(this)
        viewModel.init(prefs)
        viewModel.refreshAccessibilityState(this)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var showOnboarding by remember { mutableStateOf(!prefs.haVistoOnboarding()) }

                    if (showOnboarding) {
                        OnboardingScreen(onFinish = {
                            prefs.marcarOnboardingComoVisto()
                            showOnboarding = false
                        })
                    } else {
                        SettingsScreen(
                            viewModel = viewModel,
                            prefs = prefs,
                            onGoToAccessibility = {
                                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Stateless settings screen — receives all state from MainViewModel.
 */
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    prefs: PreferencesManager,
    onGoToAccessibility: () -> Unit
) {
    // Observamos el StateFlow. Compose se recompone automáticamente cuando cambia.
    val sentido by viewModel.sentido.collectAsStateWithLifecycle()
    val isAccessibilityEnabled by viewModel.isAccessibilityEnabled.collectAsStateWithLifecycle()

    // Refrescamos el estado de Accesibilidad cada vez que el usuario vuelve a la App
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshAccessibilityState(lifecycleOwner as android.content.Context)
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

        if (!isAccessibilityEnabled) {
            ElevatedButton(
                onClick = onGoToAccessibility,
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

        Button(
            onClick = { viewModel.setSentido(AppConstants.SENTIDO_ES_EN, prefs) },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (sentido == AppConstants.SENTIDO_ES_EN)
                    MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                contentColor = if (sentido == AppConstants.SENTIDO_ES_EN)
                    MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
            )
        ) {
            Text(stringResource(R.string.mode_write), fontSize = 16.sp)
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { viewModel.setSentido(AppConstants.SENTIDO_EN_ES, prefs) },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (sentido == AppConstants.SENTIDO_EN_ES)
                    MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                contentColor = if (sentido == AppConstants.SENTIDO_EN_ES)
                    MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
            )
        ) {
            Text(stringResource(R.string.mode_read), fontSize = 16.sp)
        }

        Spacer(Modifier.weight(2f))
    }
}
