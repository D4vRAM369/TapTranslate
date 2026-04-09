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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * First-run onboarding carousel using HorizontalPager.
 * Shown once and dismissed permanently via PreferencesManager.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    // Definimos las 3 páginas de contenido que vamos a mostrar.
    val paginas = listOf(
        Pair(stringResource(R.string.onboarding_title_1), stringResource(R.string.onboarding_desc_1)),
        Pair(stringResource(R.string.onboarding_title_2), stringResource(R.string.onboarding_desc_2)),
        Pair(stringResource(R.string.onboarding_title_3), stringResource(R.string.onboarding_desc_3))
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

        // Dot indicators — one per page, highlighted for the current page.
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
                Text(text = stringResource(R.string.onboarding_btn_finish), fontSize = 18.sp)
            }
        } else {
            // Un espaciador invisible para que el botón no haga que los textos salten arriba y abajo
            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}
