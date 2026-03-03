package com.example.myapplication.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.myapplication.ui.theme.MyApplicationTheme

/**
 * Componente de Burbuja estilo Comic para el asistente Be.
 * Extraído para mejorar la mantenibilidad y modularidad.
 * Gestiona la visualización de mensajes, navegación entre tips y acciones.
 */
@Composable
fun BoxScope.BeBubbleComic(
    isVisible: Boolean,
    isDraggedToLeft: Boolean,
    message: BeMessage?,
    allMessagesSize: Int,
    currentIndex: Int,
    onCloseClick: () -> Unit,
    onPrevClick: () -> Unit,
    onNextClick: () -> Unit,
    onActionClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .offset(x = 8.dp, y = (-44).dp)
            .wrapContentSize(
                unbounded = true,
                align = if (isDraggedToLeft) Alignment.BottomStart else Alignment.BottomEnd
            )
            .zIndex(110f)
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = scaleIn(
                transformOrigin = TransformOrigin(if (isDraggedToLeft) 0f else 1f, 1f),
                animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow)
            ) + fadeIn(),
            exit = scaleOut(
                transformOrigin = TransformOrigin(if (isDraggedToLeft) 0f else 1f, 1f)
            ) + fadeOut(),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            message?.let { msg ->
                Column(horizontalAlignment = if (isDraggedToLeft) Alignment.Start else Alignment.End) {

                    Box {
                        // Sombra de la burbuja
                        Box(modifier = Modifier
                            .matchParentSize()
                            .offset(x = 4.dp, y = 4.dp)
                            .background(Color(0xFF05070A), RoundedCornerShape(16.dp)))

                        Surface(
                            color = msg.bubbleColor,
                            border = BorderStroke(2.5.dp, Color(0xFF05070A)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.widthIn(min = 180.dp, max = 260.dp).wrapContentHeight()
                        ) {
                            Column {
                                Column(modifier = Modifier.padding(16.dp).padding(end = 24.dp)) {
                                    Text(
                                        text = "BE ASISTENTE:",
                                        color = msg.textColor.copy(alpha = 0.5f),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = msg.text,
                                        color = msg.textColor,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )

                                    if (msg.actionText != null) {
                                        Spacer(Modifier.height(10.dp))
                                        Surface(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable { onActionClick() },
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFF05070A).copy(alpha = 0.15f),
                                            border = BorderStroke(1.dp, Color(0xFF05070A).copy(alpha = 0.3f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(msg.actionText, color = msg.textColor, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                                Spacer(Modifier.width(4.dp))
                                                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = msg.textColor, modifier = Modifier.size(12.dp))
                                            }
                                        }
                                    }
                                }

                                // Navegación entre mensajes (Carousel)
                                if (allMessagesSize > 1) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.Black.copy(alpha = 0.2f))
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = { onPrevClick() },
                                            modifier = Modifier.size(24.dp).background(Color.White.copy(0.1f), RoundedCornerShape(6.dp))
                                        ) { Icon(Icons.Default.ChevronLeft, null, tint = if (currentIndex > 0) msg.textColor else msg.textColor.copy(0.3f), modifier = Modifier.size(16.dp)) }

                                        Text(
                                            text = "${currentIndex + 1} / $allMessagesSize",
                                            color = msg.textColor.copy(0.8f),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 2.sp
                                        )

                                        IconButton(
                                            onClick = { onNextClick() },
                                            modifier = Modifier.size(24.dp).background(Color.White.copy(0.1f), RoundedCornerShape(6.dp))
                                        ) { Icon(Icons.Default.ChevronRight, null, tint = if (currentIndex < allMessagesSize - 1) msg.textColor else msg.textColor.copy(0.3f), modifier = Modifier.size(16.dp)) }
                                    }
                                }
                            }
                        }

                        // Botón cerrar
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 12.dp, y = (-12).dp)
                                .size(32.dp)
                                .clip(CircleShape)
                                .clickable { onCloseClick() },
                            shape = CircleShape,
                            color = Color(0xFFEF4444),
                            border = BorderStroke(2.dp, Color(0xFF05070A)),
                            shadowElevation = 8.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    // Cola de la burbuja (Tail)
                    Canvas(
                        modifier = Modifier
                            .size(20.dp, 16.dp)
                            .offset(
                                x = if (isDraggedToLeft) 46.dp else (-66).dp,
                                y = (-2.5).dp
                            )
                    ) {
                        val tailPath = Path().apply {
                            moveTo(0f, 0f)
                            lineTo(size.width, 0f)
                            lineTo(if (isDraggedToLeft) 0f else size.width, size.height)
                            close()
                        }
                        drawPath(tailPath, msg.bubbleColor)
                        drawLine(Color(0xFF05070A), Offset(0f, 0f), Offset(if (isDraggedToLeft) 0f else size.width, size.height), 2.5.dp.toPx())
                        drawLine(Color(0xFF05070A), Offset(size.width, 0f), Offset(if (isDraggedToLeft) 0f else size.width, size.height), 2.5.dp.toPx())
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BeBubbleComicPreview() {
    MyApplicationTheme {
        Box(modifier = Modifier.fillMaxSize().padding(top = 100.dp, end = 30.dp)) {
            BeBubbleComic(
                isVisible = true,
                isDraggedToLeft = false,
                message = BeMessage(
                    icon = "sparkles",
                    text = "¡Hola! Soy Be, tu asistente virtual. ¿En qué puedo ayudarte hoy?",
                    actionText = "Ver más tips",
                    bubbleColor = Color(0xFFE0F2F1)
                ),
                allMessagesSize = 3,
                currentIndex = 0,
                onCloseClick = {},
                onPrevClick = {},
                onNextClick = {},
                onActionClick = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "BeBubbleComic - Left Dragged")
@Composable
fun BeBubbleComicLeftPreview() {
    MyApplicationTheme {
        Box(modifier = Modifier.fillMaxSize().padding(top = 100.dp, end = 30.dp)) {
            BeBubbleComic(
                isVisible = true,
                isDraggedToLeft = true,
                message = BeMessage(
                    icon = "info",
                    text = "Recuerda que puedes arrastrarme a cualquier parte de la pantalla.",
                    bubbleColor = Color(0xFFFFF9C4)
                ),
                allMessagesSize = 1,
                currentIndex = 0,
                onCloseClick = {},
                onPrevClick = {},
                onNextClick = {},
                onActionClick = {}
            )
        }
    }
}
