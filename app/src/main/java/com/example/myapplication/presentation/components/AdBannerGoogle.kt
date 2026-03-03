package com.example.myapplication.presentation.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay

// ==========================================================================================
// --- SECCIÓN 1: BANNERS HORIZONTALES (PARA CARRUSELES) ---
// ==========================================================================================

/**
 * Componente que representa un banner publicitario horizontal.
 * Proporciona una estructura premium y placeholders para integrar Google AdMob.
 *
 * NOTA PARA IMPLEMENTACIÓN REAL (Google Ads):
 * 1. Agregar dependencia: implementation("com.google.android.gms:play-services-ads:23.x.x")
 * 2. Inicializar MobileAds en la Activity o Application.
 * 3. Reemplazar el contenido visual por un AndroidView que cargue un AdView de Google.
 */
@Composable
fun AdBannerItem(item: AccordionBanner) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .border(1.dp, Color.Black.copy(alpha = 0.05f), RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F3F4)), // Tono gris estándar de anuncios
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            
            // ========================================================================
            // --- BLOQUE PARA INTEGRACIÓN DE GOOGLE ADS (ADMOB) ---
            // ========================================================================
            /*
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    com.google.android.gms.ads.AdView(context).apply {
                        setAdSize(com.google.android.gms.ads.AdSize.BANNER)
                        adUnitId = "ca-app-pub-3940256099942544/6300978111" // ID de prueba
                        loadAd(com.google.android.gms.ads.AdRequest.Builder().build())
                    }
                }
            )
            */
            // ========================================================================

            // --- REPRESENTACIÓN VISUAL PLACEHOLDER ---
            
            if (item.imageUrl != null) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.2f
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AdsClick,
                    contentDescription = "Ads",
                    tint = Color.Gray.copy(alpha = 0.6f),
                    modifier = Modifier.size(32.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = item.title,
                    fontSize = 14.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = item.subtitle,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 14.sp
                )
            }

            // Etiqueta distintiva de AD (Requerido por Google)
            Surface(
                color = Color(0xFFFFC107),
                shape = RoundedCornerShape(bottomEnd = 12.dp),
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Text(
                    text = "ANUNCIO",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }
    }
}

// ==========================================================================================
// --- SECCIÓN 2: ANUNCIOS PANTALLA COMPLETA (INTERSTITIALS) ---
// ==========================================================================================

/**
 * Componente de Anuncio Vertical Premium con integración para Google AdMob.
 * Incluye un temporizador de 10 segundos antes de permitir el cierre manual.
 *
 * @param show Controla la visibilidad del anuncio.
 * @param onDismiss Callback ejecutado cuando el usuario cierra el anuncio.
 * @param modifier Modificador para personalizar el layout.
 * @param adUnitId ID del bloque de anuncios de Google.
 */
@Composable
fun GoogleVerticalInterstitialAd(
    show: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    adUnitId: String = "ca-app-pub-3940256099942544/6300978111" // ID de prueba de Google
) {
    if (!show) return

    // --- ESTADO DEL TEMPORIZADOR ---
    var timeLeft by remember { mutableIntStateOf(10) }
    var isClosable by remember { mutableStateOf(false) }

    // --- LÓGICA DE CUENTA REGRESIVA ---
    LaunchedEffect(show) {
        if (show) {
            timeLeft = 10
            isClosable = false
            while (timeLeft > 0) {
                delay(1000L)
                timeLeft--
            }
            isClosable = true
        }
    }

    Dialog(
        onDismissRequest = { if (isClosable) onDismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = isClosable,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
        ) {
            // --- CONTENEDOR PRINCIPAL DEL ANUNCIO (ESTILO M3) ---
            Card(
                modifier = Modifier
                    .fillMaxHeight(0.85f)
                    .fillMaxWidth(0.9f)
                    .align(Alignment.Center),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(16.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    
                    // ========================================================================
                    // 🔥 INTEGRACIÓN REAL DE GOOGLE ADMOB 🔥
                    // ========================================================================
                    /*
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { context ->
                            com.google.android.gms.ads.AdView(context).apply {
                                setAdSize(com.google.android.gms.ads.AdSize.MEDIUM_RECTANGLE)
                                this.adUnitId = adUnitId
                                loadAd(com.google.android.gms.ads.AdRequest.Builder().build())
                            }
                        }
                    )
                    */
                    // ========================================================================

                    // --- UI DE PLACEHOLDER PROFESIONAL ---
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFFFFFFFF), Color(0xFFF1F3F4))
                                )
                            )
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = CircleShape,
                            modifier = Modifier.size(100.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdsClick,
                                contentDescription = null,
                                modifier = Modifier.padding(24.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Text(
                            text = "Contenido Patrocinado",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1A73E8), // Azul Google
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Disfruta de ofertas exclusivas de nuestros socios comerciales.\nEl anuncio real se cargará en este espacio.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = Color.Gray,
                            lineHeight = 24.sp
                        )
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        Button(
                            onClick = { /* Simular clic en anuncio */ },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("MÁS INFORMACIÓN", fontWeight = FontWeight.Bold)
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (isClosable) "Puedes cerrar el anuncio ahora" else "Espera $timeLeft segundos...",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isClosable) Color(0xFF34A853) else Color.Gray
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            LinearProgressIndicator(
                                progress = { (10f - timeLeft).toFloat() / 10f },
                                modifier = Modifier
                                    .fillMaxWidth(0.8f)
                                    .height(8.dp)
                                    .clip(CircleShape),
                                color = if (isClosable) Color(0xFF34A853) else MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            )
                        }
                    }
                    
                    Surface(
                        color = Color(0xFFFFC107),
                        shape = RoundedCornerShape(bottomEnd = 16.dp),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text = "ANUNCIO",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Black,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 32.dp, end = 24.dp)
            ) {
                Crossfade(targetState = isClosable, label = "CloseBtnTransition") { canClose ->
                    if (canClose) {
                        FilledIconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(48.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = Color.White,
                                contentColor = Color.Black
                            )
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar publicidad")
                        }
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { timeLeft.toFloat() / 10f },
                                modifier = Modifier.size(48.dp),
                                color = Color.White,
                                strokeWidth = 4.dp,
                                trackColor = Color.White.copy(alpha = 0.2f)
                            )
                            Text(
                                text = timeLeft.toString(),
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================================================================
// --- SECCIÓN 3: PREVIEWS ---
// ==========================================================================================

@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun AdBannerItemPreview() {
    MyApplicationTheme {
        Box(modifier = Modifier.padding(16.dp).size(width = 300.dp, height = 120.dp)) {
            AdBannerItem(
                item = AccordionBanner(
                    id = "ad",
                    title = "Publicidad Premium",
                    subtitle = "Anuncio patrocinado por Google AdMob",
                    icon = "📢",
                    color = Color.White,
                    type = BannerType.GOOGLE_AD
                )
            )
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun GoogleVerticalInterstitialAdPreview() {
    MyApplicationTheme {
        var showAd by remember { mutableStateOf(true) }
        
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Button(onClick = { showAd = true }) {
                Text("Simular Interstitial")
            }
            
            GoogleVerticalInterstitialAd(
                show = showAd,
                onDismiss = { showAd = false }
            )
        }
    }
}
