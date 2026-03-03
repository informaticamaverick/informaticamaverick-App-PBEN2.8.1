package com.example.myapplication.presentation.client

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.data.model.Provider
import com.example.myapplication.ui.theme.AppColors
import com.example.myapplication.data.model.MessageType
import com.example.myapplication.presentation.components.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatConversationView(
    provider: Provider,
    viewModel: ChatViewModel,
    onBack: () -> Unit,
    appColors: AppColors
) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val isRecording by viewModel.isRecording.collectAsStateWithLifecycle()
    val selectedBudget by viewModel.selectedBudget.collectAsStateWithLifecycle()

    var inputText by remember { mutableStateOf("") }
    var showAttachMenu by remember { mutableStateOf(false) }
    var showTenderSelectionDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // 🔥 CORRECCIÓN: Como getMatchingTenders probablemente esperaba un String en el ViewModel,
    // le pasamos la primera categoría principal del prestador, o un texto vacío si no tiene.
    val mainCategory = provider.categories.firstOrNull() ?: ""
    val matchingTenders by viewModel.getMatchingTenders(mainCategory)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    // --- VISOR DE PRESUPUESTO A4 ---
    if (selectedBudget != null) {
        Dialog(
            onDismissRequest = { viewModel.clearSelectedBudget() },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            BudgetMultiPageScreen(
                budget = selectedBudget!!,
                onBack = { viewModel.clearSelectedBudget() },
                // Aquí puedes pasar las lambdas onAccept/onReject si es necesario
            )
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.ime),
        topBar = {
            ChatHeader(
                providerName = provider.displayName,
                providerPhoto = provider.photoUrl,
                isOnline = provider.isOnline,
                onBack = onBack,
                appColors = appColors
            )
        },
        bottomBar = {
            Column(modifier = Modifier.background(appColors.backgroundColor)) {
                AnimatedVisibility(visible = showAttachMenu) {
                    AttachmentOptionsMenu(
                        onDismiss = { showAttachMenu = false },
                        onImageClick = { /*TODO*/ },
                        onLocationClick = { viewModel.sendLocation(-26.8083, -65.2176, "Tucumán") },
                        onScheduleClick = { /*TODO*/ },
                        onInviteClick = { showTenderSelectionDialog = true }
                    )
                }
                MessageInputBar(
                    inputText = inputText,
                    onInputChange = { inputText = it },
                    onSendMessage = { viewModel.sendText(it); inputText = "" },
                    appColors = appColors,
                    onAttachMenuToggle = { showAttachMenu = !showAttachMenu },
                    onCameraClick = { /*TODO*/ },
                    onAudioClick = {
                        if (isRecording) viewModel.stopRecordingAndSend()
                        else viewModel.startRecording(context)
                    },
                    onCancelAudio = { viewModel.cancelRecording() },
                    isRecordingAudio = isRecording
                )
            }
        },
        containerColor = appColors.backgroundColor
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(messages) { index, message ->
                if (index == 0 || !isSameDay(message.timestamp, messages[index - 1].timestamp)) {
                    DateSeparator(timestamp = message.timestamp, appColors = appColors)
                }

                when (message.type) {
                    MessageType.BUDGET -> BudgetBubble(
                        message = message,
                        isMe = message.senderId == viewModel.currentUserId,
                        appColors = appColors,
                        onClick = { message.relatedId?.let { viewModel.onBudgetClicked(it) } }
                    )
                    else -> EnhancedMessageBubble(
                        message = message,
                        isMe = message.senderId == viewModel.currentUserId,
                        appColors = appColors
                    )
                }
            }
        }

        if (showTenderSelectionDialog) {
            TenderSelectionDialog(
                matchingTenders = matchingTenders,
                providerCategories = provider.categories, // 🔥 CORRECCIÓN: Pasamos la lista completa
                appColors = appColors,
                onDismiss = { showTenderSelectionDialog = false },
                onSelect = { viewModel.sendTenderInvitation(it); showTenderSelectionDialog = false }
            )
        }
    }
}

// --- COMPONENTE BURBUJA PARA PRESUPUESTOS ---

@Composable
fun BudgetBubble(
    message: com.example.myapplication.data.local.MessageEntity,
    isMe: Boolean,
    appColors: AppColors,
    onClick: () -> Unit
) {
    val alignment = if (isMe) Alignment.End else Alignment.Start
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalAlignment = alignment) {
        Surface(
            color = if (isMe) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else appColors.surfaceColor,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
            modifier = Modifier.widthIn(max = 280.dp).clickable(onClick = onClick)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Description, "Presupuesto", tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Propuesta Técnica", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.height(8.dp))
                Text(message.content, fontSize = 14.sp, color = appColors.textPrimaryColor)
                Spacer(Modifier.height(12.dp))
                Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
                    Text("VER DETALLE")
                }
            }
        }
    }
}

// --- OTROS COMPONENTES ---

@Composable
fun DateSeparator(timestamp: Long, appColors: AppColors) {
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
        Surface(color = appColors.surfaceColor.copy(alpha = 0.5f), shape = RoundedCornerShape(16.dp)) {
            Text(
                text = SimpleDateFormat("dd 'de' MMMM", Locale.getDefault()).format(Date(timestamp)),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                fontSize = 11.sp,
                color = appColors.textSecondaryColor
            )
        }
    }
}

@Composable
fun EnhancedMessageBubble(message: com.example.myapplication.data.local.MessageEntity, isMe: Boolean, appColors: AppColors) {
    val alignment = if (isMe) Alignment.End else Alignment.Start
    val colors = if (isMe) {
        BubbleColors(container = MaterialTheme.colorScheme.primary, content = MaterialTheme.colorScheme.onPrimary)
    } else {
        BubbleColors(container = appColors.surfaceColor, content = appColors.textPrimaryColor)
    }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalAlignment = alignment) {
        Surface(color = colors.container, shape = RoundedCornerShape(16.dp)) {
            Text(
                text = message.content,
                modifier = Modifier.padding(16.dp),
                color = colors.content
            )
        }
    }
}

data class BubbleColors(val container: Color, val content: Color)

// 🔥 CORRECCIÓN: Actualizada la firma para recibir una lista (List<String>)
@Composable
fun TenderSelectionDialog(
    matchingTenders: List<com.example.myapplication.data.local.TenderEntity>,
    providerCategories: List<String>,
    appColors: AppColors,
    onDismiss: () -> Unit,
    onSelect: (com.example.myapplication.data.local.TenderEntity) -> Unit
) {
    // ...
}

fun isSameDay(t1: Long, t2: Long): Boolean {
    val fmt = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    return fmt.format(Date(t1)) == fmt.format(Date(t2))
}




/**
package com.example.myapplication.presentation.client

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.data.model.Provider
import com.example.myapplication.ui.theme.AppColors
import com.example.myapplication.data.model.MessageType
import com.example.myapplication.presentation.components.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatConversationView(
    provider: Provider,
    viewModel: ChatViewModel,
    onBack: () -> Unit,
    appColors: AppColors
) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val isRecording by viewModel.isRecording.collectAsStateWithLifecycle()
    val selectedBudget by viewModel.selectedBudget.collectAsStateWithLifecycle()

    var inputText by remember { mutableStateOf("") }
    var showAttachMenu by remember { mutableStateOf(false) }
    var showTenderSelectionDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val matchingTenders by viewModel.getMatchingTenders(provider.categories)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    // --- VISOR DE PRESUPUESTO A4 ---
    if (selectedBudget != null) {
        Dialog(
            onDismissRequest = { viewModel.clearSelectedBudget() },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            BudgetMultiPageScreen(
                budget = selectedBudget!!,
                onBack = { viewModel.clearSelectedBudget() },
                // Aquí puedes pasar las lambdas onAccept/onReject si es necesario
            )
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.ime),
        topBar = {
            ChatHeader(
                providerName = provider.displayName,
                providerPhoto = provider.photoUrl,
                isOnline = provider.isOnline,
                onBack = onBack,
                appColors = appColors
            )
        },
        bottomBar = {
            Column(modifier = Modifier.background(appColors.backgroundColor)) {
                AnimatedVisibility(visible = showAttachMenu) {
                    AttachmentOptionsMenu(
                        onDismiss = { showAttachMenu = false },
                        onImageClick = { /*TODO*/ },
                        onLocationClick = { viewModel.sendLocation(-26.8083, -65.2176, "Tucumán") },
                        onScheduleClick = { /*TODO*/ },
                        onInviteClick = { showTenderSelectionDialog = true }
                    )
                }
                MessageInputBar(
                    inputText = inputText,
                    onInputChange = { inputText = it },
                    onSendMessage = { viewModel.sendText(it); inputText = "" },
                    appColors = appColors,
                    onAttachMenuToggle = { showAttachMenu = !showAttachMenu },
                    onCameraClick = { /*TODO*/ },
                    onAudioClick = {
                        if (isRecording) viewModel.stopRecordingAndSend()
                        else viewModel.startRecording(context)
                    },
                    onCancelAudio = { viewModel.cancelRecording() },
                    isRecordingAudio = isRecording
                )
            }
        },
        containerColor = appColors.backgroundColor
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(messages) { index, message ->
                if (index == 0 || !isSameDay(message.timestamp, messages[index - 1].timestamp)) {
                    DateSeparator(timestamp = message.timestamp, appColors = appColors)
                }

                when (message.type) {
                    MessageType.BUDGET -> BudgetBubble(
                        message = message,
                        isMe = message.senderId == viewModel.currentUserId,
                        appColors = appColors,
                        onClick = { message.relatedId?.let { viewModel.onBudgetClicked(it) } }
                    )
                    else -> EnhancedMessageBubble(
                        message = message,
                        isMe = message.senderId == viewModel.currentUserId,
                        appColors = appColors
                    )
                }
            }
        }

        if (showTenderSelectionDialog) {
            TenderSelectionDialog(
                matchingTenders = matchingTenders,
                providerCategory = provider.categories,
                appColors = appColors,
                onDismiss = { showTenderSelectionDialog = false },
                onSelect = { viewModel.sendTenderInvitation(it); showTenderSelectionDialog = false }
            )
        }
    }
}

// --- COMPONENTE BURBUJA PARA PRESUPUESTOS ---

@Composable
fun BudgetBubble(
    message: com.example.myapplication.data.local.MessageEntity,
    isMe: Boolean,
    appColors: AppColors,
    onClick: () -> Unit
) {
    val alignment = if (isMe) Alignment.End else Alignment.Start
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalAlignment = alignment) {
        Surface(
            color = if (isMe) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else appColors.surfaceColor,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
            modifier = Modifier.widthIn(max = 280.dp).clickable(onClick = onClick)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Description, "Presupuesto", tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Propuesta Técnica", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.height(8.dp))
                Text(message.content, fontSize = 14.sp, color = appColors.textPrimaryColor)
                Spacer(Modifier.height(12.dp))
                Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
                    Text("VER DETALLE")
                }
            }
        }
    }
}

// --- OTROS COMPONENTES ---

@Composable
fun DateSeparator(timestamp: Long, appColors: AppColors) {
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
        Surface(color = appColors.surfaceColor.copy(alpha = 0.5f), shape = RoundedCornerShape(16.dp)) {
            Text(
                text = SimpleDateFormat("dd 'de' MMMM", Locale.getDefault()).format(Date(timestamp)),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                fontSize = 11.sp,
                color = appColors.textSecondaryColor
            )
        }
    }
}

@Composable
fun EnhancedMessageBubble(message: com.example.myapplication.data.local.MessageEntity, isMe: Boolean, appColors: AppColors) {
    val alignment = if (isMe) Alignment.End else Alignment.Start
    val colors = if (isMe) {
        BubbleColors(container = MaterialTheme.colorScheme.primary, content = MaterialTheme.colorScheme.onPrimary)
    } else {
        BubbleColors(container = appColors.surfaceColor, content = appColors.textPrimaryColor)
    }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalAlignment = alignment) {
        Surface(color = colors.container, shape = RoundedCornerShape(16.dp)) {
            Text(
                text = message.content,
                modifier = Modifier.padding(16.dp),
                color = colors.content
            )
        }
    }
}

data class BubbleColors(val container: Color, val content: Color)

@Composable
fun TenderSelectionDialog(matchingTenders: List<com.example.myapplication.data.local.TenderEntity>, providerCategory: String, appColors: AppColors, onDismiss: () -> Unit, onSelect: (com.example.myapplication.data.local.TenderEntity) -> Unit) {
    // ...
}

fun isSameDay(t1: Long, t2: Long): Boolean {
    val fmt = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    return fmt.format(Date(t1)) == fmt.format(Date(t2))
}
**/