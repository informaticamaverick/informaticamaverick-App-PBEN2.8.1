package com.example.myapplication.prestador.ui.presupuesto

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.prestador.data.PPrestadorProfileFalso
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    textStyle: TextStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
) {
    val colors = getPrestadorColors()
    val interactionSource = remember { MutableInteractionSource() }
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        textStyle = textStyle.copy(color = colors.textPrimary),
        keyboardOptions = keyboardOptions,
        singleLine = true,
        interactionSource = interactionSource,
        decorationBox = { innerTextField ->
            OutlinedTextFieldDefaults.DecorationBox(
                value = value,
                innerTextField = innerTextField,
                enabled = true,
                singleLine = true,
                visualTransformation = VisualTransformation.None,
                interactionSource = interactionSource,
                label = label,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primaryOrange,
                    unfocusedBorderColor = colors.border,
                    focusedLabelColor = colors.primaryOrange,
                    unfocusedLabelColor = colors.textSecondary,
                    cursorColor = colors.primaryOrange,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledBorderColor = colors.border,
                    disabledLabelColor = Color(0xFF9CA3AF), // Gris
                    disabledTextColor = Color(0xFF9CA3AF), // Gris
                    errorBorderColor = Color(0xFFEF4444), // Rojo para errores
                    errorLabelColor = Color(0xFFEF4444),
                    errorCursorColor = Color(0xFFEF4444)
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp), // Padding compacto
                container = {
                    OutlinedTextFieldDefaults.ContainerBox(
                        enabled = true,
                        isError = false,
                        interactionSource = interactionSource,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF6B35),
                            unfocusedBorderColor = Color(0xFFD1D5DB),
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            )
        }
    )
}

@Composable
fun BudgetItemRow(item: BudgetItem, suggestionItems: List<BudgetItem> = emptyList(), onUpdate: (BudgetItem) -> Unit) {
    val colors = getPrestadorColors()
    val base = item.unitPrice * item.quantity
    val taxAmount = base * (item.taxPercentage / 100)
    val withTax = base + taxAmount
    val discountAmount = withTax * (item.discountPercentage / 100)
    val total = withTax - discountAmount

    var showSuggestions by remember { mutableStateOf(false) }
    val filtered = if (showSuggestions && item.description.length >= 2 && suggestionItems.isNotEmpty()) {
        suggestionItems.filter { it.description.contains(item.description, ignoreCase = true) }.take(5)
    } else emptyList()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            CompactTextField(
                value = item.code,
                onValueChange = { onUpdate(item.copy(code = it)) },
                label = { Text("Cód.") },
                modifier = Modifier.weight(0.25f),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            CompactTextField(
                value = item.description,
                onValueChange = {
                    onUpdate(item.copy(description = it))
                    showSuggestions = it.isNotBlank()
                },
                label = {
                    Text(if (suggestionItems.isNotEmpty()) "Descripción (${suggestionItems.size} sugerencias)" else "Descripción")
                },
                modifier = Modifier.weight(0.75f),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
        }

        // Sugerencias inline
        if (showSuggestions && filtered.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8F5)),
                elevation = CardDefaults.cardElevation(4.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column {
                    filtered.forEachIndexed { index, suggestion ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onUpdate(suggestion.copy(id = item.id))
                                    showSuggestions = false
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    suggestion.description,
                                    fontWeight = FontWeight.Medium,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.textPrimary
                                )
                                Text(
                                    "Cant: ${suggestion.quantity}  •  \$${"%.2f".format(suggestion.unitPrice)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                            }
                            Icon(
                                Icons.Default.NorthWest,
                                contentDescription = "Usar",
                                tint = colors.primaryOrange,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        if (index < filtered.size - 1) {
                            HorizontalDivider(color = colors.border)
                        }
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            CompactTextField(
                value = if (item.quantity == 0) "" else item.quantity.toString(),
                onValueChange = { onUpdate(item.copy(quantity = it.toIntOrNull() ?: 0)) },
                label = { Text("Cant.") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
            )
            CompactTextField(
                value = if (item.unitPrice == 0.0) "" else item.unitPrice.toString(),
                onValueChange = { onUpdate(item.copy(unitPrice = it.toDoubleOrNull() ?: 0.0)) },
                label = { Text("P. Unit.") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done)
            )
            Text(
                "Total: \$${"%.2f".format(total)}",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.primaryOrange
            )
        }
    }
}

@Composable
fun BudgetServiceRow(service: BudgetService, onUpdate: (BudgetService) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
         Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            CompactTextField(value = service.code, onValueChange = { onUpdate(service.copy(code = it)) }, label = { Text("Cód.") }, modifier = Modifier.weight(0.25f), keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next))
            CompactTextField(value = service.description, onValueChange = { onUpdate(service.copy(description = it)) }, label = { Text("Descripción") }, modifier = Modifier.weight(0.75f), keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            CompactTextField(value = if (service.total == 0.0) "" else service.total.toString(), onValueChange = { onUpdate(service.copy(total = it.toDoubleOrNull() ?: 0.0)) }, label = { Text("Total") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done))
        }
    }
}

@Composable
fun BudgetProfessionalFeeRow(fee: BudgetProfessionalFee, onUpdate: (BudgetProfessionalFee) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
         Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            CompactTextField(value = fee.code, onValueChange = { onUpdate(fee.copy(code = it)) }, label = { Text("Cód.") }, modifier = Modifier.weight(0.25f), keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next))
            CompactTextField(value = fee.description, onValueChange = { onUpdate(fee.copy(description = it)) }, label = { Text("Descripción") }, modifier = Modifier.weight(0.75f), keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            CompactTextField(value = if (fee.total == 0.0) "" else fee.total.toString(), onValueChange = { onUpdate(fee.copy(total = it.toDoubleOrNull() ?: 0.0)) }, label = { Text("Total") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done))
        }
    }
}

@Composable
fun ArticleSummaryRow(modifier: Modifier = Modifier, item: BudgetItem) {
    val colors = getPrestadorColors()
    val base = item.unitPrice * item.quantity
    val taxAmount = base * (item.taxPercentage / 100)
    val withTax = base + taxAmount
    val discountAmount = withTax * (item.discountPercentage / 100)
    val total = withTax - discountAmount

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(colors.primaryOrange)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.description.ifBlank { "(Sin descripción)" }, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
            Text("${item.quantity} × \$${"%.2f".format(item.unitPrice)}", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
        }
        Text("\$${"%.2f".format(total)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = colors.primaryOrange)
    }
}

@Composable
fun ServiceSummaryRow(modifier: Modifier = Modifier, item: BudgetService) {
    val colors = getPrestadorColors()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF3B82F6)))
        Spacer(modifier = Modifier.width(10.dp))
        Text(item.description.ifBlank { "(Sin descripción)" }, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
        Text("\$${"%.2f".format(item.total)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = colors.primaryOrange)
    }
}

@Composable
fun ProfessionalFeeSummaryRow(modifier: Modifier = Modifier, item: BudgetProfessionalFee) {
    val colors = getPrestadorColors()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF8B5CF6)))
        Spacer(modifier = Modifier.width(10.dp))
        Text(item.description.ifBlank { "(Sin descripción)" }, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
        Text("\$${"%.2f".format(item.total)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = colors.primaryOrange)
    }
}

@Composable
fun TaxSummaryRow(modifier: Modifier = Modifier, item: BudgetTax) {
    val colors = getPrestadorColors()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFEF4444)))
        Spacer(modifier = Modifier.width(10.dp))
        Text(item.description.ifBlank { "(Sin descripción)" }, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
        Text("\$${"%.2f".format(item.amount)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = colors.primaryOrange)
    }
}

@Composable
fun AttachmentSummaryRow(modifier: Modifier = Modifier, item: BudgetAttachment) {
    val colors = getPrestadorColors()
    Row(modifier = modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (item.type == AttachmentType.PDF) Icons.Default.PictureAsPdf else Icons.Default.Image, 
            contentDescription = null, 
            tint = colors.primaryOrange,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            item.description.ifBlank { "(Sin descripción)" }, 
            modifier = Modifier.weight(1f), 
            fontWeight = FontWeight.SemiBold, 
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textPrimary
        )
        Icon(
            Icons.Default.CheckCircle, 
            contentDescription = "Adjunto", 
            tint = Color(0xFF10B981),
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun MiscExpenseSummaryRow(modifier: Modifier = Modifier, item: BudgetMiscExpense) {
    val colors = getPrestadorColors()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF10B981)))
        Spacer(modifier = Modifier.width(10.dp))
        Text(item.description.ifBlank { "(Sin descripción)" }, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
        Text("\$${"%.2f".format(item.amount)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = colors.primaryOrange)
    }
}

@Composable
fun TotalsSummary(modifier: Modifier = Modifier, isExpanded: Boolean, grandTotal: Double) {
    val priceTextStyle = if (isExpanded) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleLarge
    val verticalPadding = if (isExpanded) 14.dp else 8.dp
    val gradient = Brush.linearGradient(colors = listOf(Color(0xFF1E293B), Color(0xFF334155)))
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(10.dp)
            .background(gradient)
            .animateContentSize()
            .padding(horizontal = 20.dp, vertical = verticalPadding)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    "TOTAL GENERAL",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "\$${"%.2f".format(grandTotal)}",
                    style = priceTextStyle,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFF6B35)
                )
            }
            Icon(
                Icons.Default.Receipt,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.2f),
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PrestadorHeader(prestador: PPrestadorProfileFalso, onFilterClick: () -> Unit) {
    val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    val gradient = Brush.linearGradient(
        colors = listOf(Color(0xFFFF6B35), Color(0xFFE64A19))
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(gradient)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Avatar con borde blanco
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .border(2.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                        .padding(2.dp)
                        .clip(CircleShape)
                ) {
                    AsyncImage(
                        model = prestador.profileImageUrl,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        prestador.companyName ?: "${prestador.name} ${prestador.lastName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        prestador.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        prestador.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
                IconButton(onClick = onFilterClick) {
                    Icon(
                        Icons.Filled.FilterList,
                        contentDescription = "Filtrar Secciones",
                        tint = Color.White
                    )
                }
            }
            HorizontalDivider(color = Color.White.copy(alpha = 0.3f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Description, contentDescription = null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(14.dp))
                    Text("Nº: 0001", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(14.dp))
                    Text(currentDate, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun BudgetMetaBox(title: String, value: String) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, Color.Black, RoundedCornerShape(4.dp))) {
        Text(text = title, modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1976D2))
            .padding(2.dp), color = Color.White, fontSize = 9.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
        Text(text = value, modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(4.dp), fontSize = 10.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun SectionHeader(text: String) {
    Surface(color = Color(0xFF1976D2), modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))) {
        Text(text = text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
    }
}

@Composable
fun <T> CollapsibleSection(
    title: String,
    items: List<T>,
    sectionTotal: Double,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onAddClick: () -> Unit,
    quickAddSlot: (@Composable () -> Unit)? = null,
    itemContent: @Composable (item: T, index: Int) -> Unit
) {
    val colors = getPrestadorColors()
    val rotationAngle by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "arrowRotation")

    Box(modifier = Modifier.padding(top = 18.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = colors.surfaceColor
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onToggleExpand)
                ) {
                    Badge(
                        containerColor = colors.primaryOrange,
                        contentColor = Color.White
                    ) {
                        Text(
                            text = "${items.size}",
                            modifier = Modifier.padding(horizontal = 6.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        title, 
                        style = MaterialTheme.typography.titleMedium, 
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = if (isExpanded) "Colapsar" else "Expandir",
                        modifier = Modifier.rotate(rotationAngle),
                        tint = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }

                AnimatedVisibility(visible = isExpanded) {
                    Column {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = colors.border
                        )
                        // Quick-add slot (search from saved items)
                        if (quickAddSlot != null) {
                            quickAddSlot()
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        if (items.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .border(
                                        width = 1.dp,
                                        color = colors.border,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.AddCircleOutline,
                                        contentDescription = null,
                                        tint = colors.textSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        "Sin ítems. Tocá + para agregar.",
                                        color = colors.textSecondary,
                                        style = MaterialTheme.typography.bodySmall,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            Column {
                                items.forEachIndexed { index, item ->
                                    itemContent(item, index)
                                    if (index < items.size - 1) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 4.dp),
                                            color = colors.border
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        SmallFloatingActionButton(
            onClick = onAddClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 4.dp, y = (-18).dp),
            shape = CircleShape,
            containerColor = colors.primaryOrange,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "Añadir ${title.substringBefore(' ')}")
        }

        if (items.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 4.dp, y = 12.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFFFF3E0),
                border = BorderStroke(1.dp, colors.primaryOrange.copy(alpha = 0.3f))
            ) {
                Text(
                    text = "Subtotal: \$${"%.2f".format(sectionTotal)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.primaryOrange,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}



/**
 * A quick-add search field that shows saved suggestions.
 * The user types to filter; tapping a suggestion calls onSelect.
 */
@Composable
fun <T> QuickAddField(
    suggestions: List<T>,
    getLabel: (T) -> String,
    getSubLabel: (T) -> String = { "" },
    placeholder: String = "Buscar guardados...",
    onSelect: (T) -> Unit
) {
    if (suggestions.isEmpty()) return

    val colors = getPrestadorColors()
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, suggestions) {
        if (query.isBlank()) suggestions
        else suggestions.filter { getLabel(it).contains(query, ignoreCase = true) }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, style = MaterialTheme.typography.bodySmall) },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = colors.primaryOrange,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = if (query.isNotEmpty()) {
                { IconButton(onClick = { query = "" }) {
                    Icon(Icons.Default.Clear, contentDescription = "Limpiar", modifier = Modifier.size(16.dp))
                }}
            } else null,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodySmall,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.primaryOrange,
                unfocusedBorderColor = colors.border,
                focusedLabelColor = colors.primaryOrange,
                cursorColor = colors.primaryOrange,
                focusedTextColor = colors.textPrimary,
                unfocusedTextColor = colors.textPrimary
            ),
            shape = RoundedCornerShape(8.dp)
        )

        if (filtered.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surfaceColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    filtered.take(5).forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelect(item)
                                    query = ""
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    getLabel(item),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.textPrimary,
                                    maxLines = 1
                                )
                                val sub = getSubLabel(item)
                                if (sub.isNotBlank()) {
                                    Text(
                                        sub,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colors.textSecondary
                                    )
                                }
                            }
                            Icon(
                                Icons.Default.AddCircleOutline,
                                contentDescription = "Agregar",
                                tint = colors.primaryOrange,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        if (index < filtered.take(5).lastIndex) {
                            HorizontalDivider(color = colors.border)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleAutoCompleteFields(
    suggestions: List<BudgetItem>,
    onAdd: (BudgetItem) -> Unit
) {
    if (suggestions.isEmpty()) return
    val colors = getPrestadorColors()
    var codeText by remember { mutableStateOf("") }
    var nameText by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<BudgetItem?>(null) }
    var codeExpanded by remember { mutableStateOf(false) }
    var nameExpanded by remember { mutableStateOf(false) }

    val codeFiltered = remember(codeText, suggestions) {
        if (codeText.isBlank()) emptyList()
        else suggestions.filter { it.code.contains(codeText, ignoreCase = true) }
    }
    val nameFiltered = remember(nameText, suggestions) {
        if (nameText.isBlank()) emptyList()
        else suggestions.filter { it.description.contains(nameText, ignoreCase = true) }
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = colors.primaryOrange,
        unfocusedBorderColor = colors.border,
        focusedLabelColor = colors.primaryOrange,
        cursorColor = colors.primaryOrange,
        focusedTextColor = colors.textPrimary,
        unfocusedTextColor = colors.textPrimary
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        // --- CODIGO ---
        ExposedDropdownMenuBox(
            expanded = codeExpanded && codeFiltered.isNotEmpty(),
            onExpandedChange = { codeExpanded = it },
            modifier = Modifier.weight(0.35f)
        ) {
            OutlinedTextField(
                value = codeText,
                onValueChange = { codeText = it; selected = null; codeExpanded = true },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable).fillMaxWidth(),
                label = { Text("Código", style = MaterialTheme.typography.labelSmall) },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall,
                colors = fieldColors,
                shape = RoundedCornerShape(8.dp)
            )
            ExposedDropdownMenu(
                expanded = codeExpanded && codeFiltered.isNotEmpty(),
                onDismissRequest = { codeExpanded = false }
            ) {
                codeFiltered.take(5).forEach { item ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(item.code, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                                Text(item.description, style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                            }
                        },
                        onClick = {
                            codeText = item.code
                            nameText = item.description
                            selected = item
                            codeExpanded = false
                        }
                    )
                }
            }
        }

        // --- NOMBRE ---
        ExposedDropdownMenuBox(
            expanded = nameExpanded && nameFiltered.isNotEmpty(),
            onExpandedChange = { nameExpanded = it },
            modifier = Modifier.weight(0.65f)
        ) {
            OutlinedTextField(
                value = nameText,
                onValueChange = { nameText = it; selected = null; nameExpanded = true },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable).fillMaxWidth(),
                label = { Text("Nombre", style = MaterialTheme.typography.labelSmall) },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall,
                trailingIcon = if (selected != null) {
                    {
                        IconButton(onClick = {
                            onAdd(selected!!.copy(id = System.currentTimeMillis()))
                            codeText = ""; nameText = ""; selected = null
                        }) {
                            Icon(Icons.Default.AddCircle, contentDescription = "Agregar", tint = colors.primaryOrange)
                        }
                    }
                } else null,
                colors = fieldColors,
                shape = RoundedCornerShape(8.dp)
            )
            ExposedDropdownMenu(
                expanded = nameExpanded && nameFiltered.isNotEmpty(),
                onDismissRequest = { nameExpanded = false }
            ) {
                nameFiltered.take(5).forEach { item ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(item.description, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                                if (item.unitPrice > 0) Text("$ ${"%.2f".format(item.unitPrice)}", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                            }
                        },
                        onClick = {
                            codeText = item.code
                            nameText = item.description
                            selected = item
                            nameExpanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceAutoCompleteFields(
    suggestions: List<BudgetService>,
    onAdd: (BudgetService) -> Unit
) {
    if (suggestions.isEmpty()) return
    val colors = getPrestadorColors()
    var codeText by remember { mutableStateOf("") }
    var nameText by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<BudgetService?>(null) }
    var codeExpanded by remember { mutableStateOf(false) }
    var nameExpanded by remember { mutableStateOf(false) }

    val codeFiltered = remember(codeText, suggestions) {
        if (codeText.isBlank()) emptyList()
        else suggestions.filter { it.code.contains(codeText, ignoreCase = true) }
    }
    val nameFiltered = remember(nameText, suggestions) {
        if (nameText.isBlank()) emptyList()
        else suggestions.filter { it.description.contains(nameText, ignoreCase = true) }
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = colors.primaryOrange,
        unfocusedBorderColor = colors.border,
        focusedLabelColor = colors.primaryOrange,
        cursorColor = colors.primaryOrange,
        focusedTextColor = colors.textPrimary,
        unfocusedTextColor = colors.textPrimary
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        ExposedDropdownMenuBox(
            expanded = codeExpanded && codeFiltered.isNotEmpty(),
            onExpandedChange = { codeExpanded = it },
            modifier = Modifier.weight(0.35f)
        ) {
            OutlinedTextField(
                value = codeText,
                onValueChange = { codeText = it; selected = null; codeExpanded = true },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable).fillMaxWidth(),
                label = { Text("Código", style = MaterialTheme.typography.labelSmall) },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall,
                colors = fieldColors,
                shape = RoundedCornerShape(8.dp)
            )
            ExposedDropdownMenu(
                expanded = codeExpanded && codeFiltered.isNotEmpty(),
                onDismissRequest = { codeExpanded = false }
            ) {
                codeFiltered.take(5).forEach { item ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(item.code, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                                Text(item.description, style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                            }
                        },
                        onClick = {
                            codeText = item.code; nameText = item.description
                            selected = item; codeExpanded = false
                        }
                    )
                }
            }
        }

        ExposedDropdownMenuBox(
            expanded = nameExpanded && nameFiltered.isNotEmpty(),
            onExpandedChange = { nameExpanded = it },
            modifier = Modifier.weight(0.65f)
        ) {
            OutlinedTextField(
                value = nameText,
                onValueChange = { nameText = it; selected = null; nameExpanded = true },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable).fillMaxWidth(),
                label = { Text("Descripción", style = MaterialTheme.typography.labelSmall) },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall,
                trailingIcon = if (selected != null) {
                    {
                        IconButton(onClick = {
                            onAdd(selected!!.copy(id = System.currentTimeMillis()))
                            codeText = ""; nameText = ""; selected = null
                        }) {
                            Icon(Icons.Default.AddCircle, contentDescription = "Agregar", tint = colors.primaryOrange)
                        }
                    }
                } else null,
                colors = fieldColors,
                shape = RoundedCornerShape(8.dp)
            )
            ExposedDropdownMenu(
                expanded = nameExpanded && nameFiltered.isNotEmpty(),
                onDismissRequest = { nameExpanded = false }
            ) {
                nameFiltered.take(5).forEach { item ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(item.description, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                                if (item.total > 0) Text("$ ${"%.2f".format(item.total)}", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                            }
                        },
                        onClick = {
                            codeText = item.code; nameText = item.description
                            selected = item; nameExpanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeeAutoCompleteFields(
    suggestions: List<BudgetProfessionalFee>,
    onAdd: (BudgetProfessionalFee) -> Unit
) {
    if (suggestions.isEmpty()) return
    val colors = getPrestadorColors()
    var codeText by remember { mutableStateOf("") }
    var nameText by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<BudgetProfessionalFee?>(null) }
    var codeExpanded by remember { mutableStateOf(false) }
    var nameExpanded by remember { mutableStateOf(false) }
    val codeFiltered = remember(codeText, suggestions) {
        if (codeText.isBlank()) emptyList() else suggestions.filter { it.code.contains(codeText, ignoreCase = true) }
    }
    val nameFiltered = remember(nameText, suggestions) {
        if (nameText.isBlank()) emptyList() else suggestions.filter { it.description.contains(nameText, ignoreCase = true) }
    }
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = colors.primaryOrange, unfocusedBorderColor = colors.border,
        focusedLabelColor = colors.primaryOrange, cursorColor = colors.primaryOrange,
        focusedTextColor = colors.textPrimary, unfocusedTextColor = colors.textPrimary
    )
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
        ExposedDropdownMenuBox(expanded = codeExpanded && codeFiltered.isNotEmpty(), onExpandedChange = { codeExpanded = it }, modifier = Modifier.weight(0.35f)) {
            OutlinedTextField(value = codeText, onValueChange = { codeText = it; selected = null; codeExpanded = true },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable).fillMaxWidth(),
                label = { Text("Código", style = MaterialTheme.typography.labelSmall) }, singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall, colors = fieldColors, shape = RoundedCornerShape(8.dp))
            ExposedDropdownMenu(expanded = codeExpanded && codeFiltered.isNotEmpty(), onDismissRequest = { codeExpanded = false }) {
                codeFiltered.take(5).forEach { item ->
                    DropdownMenuItem(text = { Column { Text(item.code, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = colors.textPrimary); Text(item.description, style = MaterialTheme.typography.labelSmall, color = colors.textSecondary) } },
                        onClick = { codeText = item.code; nameText = item.description; selected = item; codeExpanded = false })
                }
            }
        }
        ExposedDropdownMenuBox(expanded = nameExpanded && nameFiltered.isNotEmpty(), onExpandedChange = { nameExpanded = it }, modifier = Modifier.weight(0.65f)) {
            OutlinedTextField(value = nameText, onValueChange = { nameText = it; selected = null; nameExpanded = true },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable).fillMaxWidth(),
                label = { Text("Descripción", style = MaterialTheme.typography.labelSmall) }, singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall,
                trailingIcon = if (selected != null) { { IconButton(onClick = { onAdd(selected!!.copy(id = System.currentTimeMillis())); codeText = ""; nameText = ""; selected = null }) { Icon(Icons.Default.AddCircle, null, tint = colors.primaryOrange) } } } else null,
                colors = fieldColors, shape = RoundedCornerShape(8.dp))
            ExposedDropdownMenu(expanded = nameExpanded && nameFiltered.isNotEmpty(), onDismissRequest = { nameExpanded = false }) {
                nameFiltered.take(5).forEach { item ->
                    DropdownMenuItem(text = { Column { Text(item.description, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = colors.textPrimary); if (item.total > 0) Text("$ ${"%.2f".format(item.total)}", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary) } },
                        onClick = { codeText = item.code; nameText = item.description; selected = item; nameExpanded = false })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DescriptionAutoCompleteField(
    label: String,
    suggestions: List<String>,
    onSelect: (String) -> Unit
) {
    if (suggestions.isEmpty()) return
    val colors = getPrestadorColors()
    var text by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val filtered = remember(text, suggestions) {
        if (text.isBlank()) emptyList() else suggestions.filter { it.contains(text, ignoreCase = true) }
    }
    ExposedDropdownMenuBox(expanded = expanded && filtered.isNotEmpty(), onExpandedChange = { expanded = it }, modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(value = text, onValueChange = { text = it; expanded = true },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable).fillMaxWidth(),
            label = { Text(label, style = MaterialTheme.typography.labelSmall) }, singleLine = true,
            textStyle = MaterialTheme.typography.bodySmall,
            trailingIcon = if (text.isNotEmpty()) { { IconButton(onClick = { onSelect(text); text = "" }) { Icon(Icons.Default.AddCircle, null, tint = colors.primaryOrange) } } } else null,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.primaryOrange, unfocusedBorderColor = colors.border, focusedLabelColor = colors.primaryOrange, cursorColor = colors.primaryOrange, focusedTextColor = colors.textPrimary, unfocusedTextColor = colors.textPrimary),
            shape = RoundedCornerShape(8.dp))
        ExposedDropdownMenu(expanded = expanded && filtered.isNotEmpty(), onDismissRequest = { expanded = false }) {
            filtered.take(5).forEach { desc ->
                DropdownMenuItem(text = { Text(desc, style = MaterialTheme.typography.bodySmall, color = colors.textPrimary) },
                    onClick = { onSelect(desc); text = ""; expanded = false })
            }
        }
    }
}
