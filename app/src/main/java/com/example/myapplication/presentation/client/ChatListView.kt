package com.example.myapplication.presentation.client

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.myapplication.data.local.CategoryEntity
import com.example.myapplication.data.model.Provider
import com.example.myapplication.presentation.components.PrestadorCard
import com.example.myapplication.ui.theme.AppColors
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.getThemeColors

/**
 * VISTA DE LISTA DE CHATS
 * Muestra los prestadores disponibles para conversar.
 * Esta pantalla consume el modelo de dominio 'Provider' para ser compatible con los componentes de UI.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListView(
    providersList: List<Provider>,
    allCategories: List<CategoryEntity>,
    unreadCounts: Map<String, Int> = emptyMap(), // 🔥 Mapa de no leídos
    currentUserId: String = "",
    onChatClick: (String) -> Unit,
    onBack: () -> Unit,
    appColors: AppColors,
    navController: NavHostController? = null
) {
    Scaffold(
        containerColor = appColors.backgroundColor,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Conversaciones", 
                        style = MaterialTheme.typography.titleLarge, 
                        fontWeight = FontWeight.Bold,
                        color = appColors.textPrimaryColor
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Volver", 
                            tint = appColors.textPrimaryColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = appColors.surfaceColor)
            )
        }
    ) { paddingValues ->
        if (providersList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No tienes conversaciones activas", 
                    color = appColors.textSecondaryColor,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
            ) {
                items(providersList, key = { it.id }) { provider ->
                    val chatId = "chat_${currentUserId}_${provider.id}"
                    val count = unreadCounts[chatId] ?: 0

                    Box(modifier = Modifier.fillMaxWidth()) {
                        PrestadorCard(
                            provider = provider,
                            onClick = {
                                navController?.navigate("perfil_prestador/${provider.id}")
                            },
                            onChat = { onChatClick(provider.id) },
                            showBadges = true
                        )

                        // 🔥 [NUEVO] INDICADOR DE MENSAJES NO LEÍDOS (Círculo Rojo)
                        if (count > 0) {
                            Surface(
                                color = Color.Red,
                                shape = CircleShape,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 4.dp, y = (-4).dp)
                                    .size(22.dp),
                                border = androidx.compose.foundation.BorderStroke(2.dp, appColors.surfaceColor)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = if (count > 9) "+9" else count.toString(),
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatListViewPreview() {
    MyApplicationTheme {
        val sampleProviders = listOf(
            Provider(
                uid = "1",
                email = "provider1@example.com",
                displayName = "Provider One",
                name = "Provider",
                lastName = "One",
                phoneNumber = "123456789",
                category = "Plomería",
                matricula = "12345",
                titulo = "Lic. en Plomería",
                photoUrl = null,
                bannerImageUrl = null,
                hasCompanyProfile = false,
                isSubscribed = true,
                isVerified = true,
                isOnline = true,
                isFavorite = false,
                rating = 4.5f,
                createdAt = System.currentTimeMillis()
            ),
            Provider(
                uid = "2",
                email = "provider2@example.com",
                displayName = "Provider Two",
                name = "Provider",
                lastName = "Two",
                phoneNumber = "987654321",
                category = "Electricidad",
                matricula = "54321",
                titulo = "Ing. Eléctrico",
                photoUrl = null,
                bannerImageUrl = null,
                hasCompanyProfile = true,
                isSubscribed = false,
                isVerified = true,
                isOnline = false,
                isFavorite = true,
                rating = 4.8f,
                createdAt = System.currentTimeMillis()
            )
        )

        val sampleCategories = listOf(
            CategoryEntity(
                name = "Plomería",
                icon = "plumbing",
                color = 0xFF0000FF,
                superCategory = "Hogar",
                providerIds = listOf("1"),
                imageUrl = null,
                isNew = false,
                isNewPrestador = false,
                isAd = false
            ),
            CategoryEntity(
                name = "Electricidad",
                icon = "electrical_services",
                color = 0xFFFFFF00,
                superCategory = "Hogar",
                providerIds = listOf("2"),
                imageUrl = null,
                isNew = false,
                isNewPrestador = false,
                isAd = false
            )
        )

        ChatListView(
            providersList = sampleProviders,
            allCategories = sampleCategories,
            onChatClick = {},
            onBack = {},
            appColors = getThemeColors()
        )
    }
}