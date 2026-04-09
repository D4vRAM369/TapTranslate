package com.d4vram.taptranslate

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * [Profe]: Aquí creamos el carrusel de bienvenida.
 * Usamos 'HorizontalPager' (que está dentro de Compose Foundation) para crear ese 
 * efecto deslizable a los lados tan moderno.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    // Definimos las 3 páginas de contenido que vamos a mostrar.
    val paginas = listOf(
        Pair("1. Actúa en las sombras", "TapTranslate no tiene una pantalla que debas abrir todos los días. Opera como un botón escondido en tu móvil para máxima rapidez."),
        Pair("2. ¿Cómo se usa?", "Cuando escribas un comentario (ej. en Reddit), subraya el texto, presiona los 3 puntitos de opciones, y elige 'TapTranslate'."),
        Pair("3. 100% Local & Privado", "La primera vez descargará 30MB del diccionario. Luego, todas las traducciones sucederán sin internet dentro de tu teléfono.")
    )

    // El PagerState guarda en qué página nos encontramos (0, 1 o 2).
    val pagerState = rememberPagerState(pageCount = { paginas.size })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        
        Spacer(modifier = Modifier.weight(1f))

        // El componente del carrusel en sí.
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { pageIndex ->
            // Qué se dibuja adentro de cada "Diapositiva"
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                // Título en negrita
                Text(
                    text = paginas[pageIndex].first,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Descripción
                Text(
                    text = paginas[pageIndex].second,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // [Profe]: Esta fila dibujará los "puntitos" mágicos que indican el progreso.
        Row(
            Modifier
                .height(50.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(paginas.size) { iteration ->
                val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else Color.LightGray
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // El botón para finalizar (solo se muestra o se habilita si es la última diapositiva).
        if (pagerState.currentPage == paginas.lastIndex) {
            Button(
                onClick = onFinish,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .padding(bottom = 32.dp)
            ) {
                Text(text = "¡Entendido, a traducir!", fontSize = 18.sp)
            }
        } else {
            // Un espaciador invisible para que el botón no haga que los textos salten arriba y abajo
            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}
