package com.example.myapplication.Login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.ui.res.painterResource
import com.example.myapplication.R
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.Components.CustomTextField
import com.example.myapplication.Components.PrimaryButton
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
// Creo q aqui esta el problema de inicio de secion 654654654654sdfgsdfgsdfgfdsgfds

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: (hasProfile: Boolean, userName: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val hasProfile by viewModel.hasProfile.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val context = LocalContext.current
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    
    // Colores adaptativos para modo oscuro
    val isDarkTheme = androidx.compose.foundation.isSystemInDarkTheme()
    val surfaceColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White
    val textPrimaryColor = if (isDarkTheme) Color.White else Color(0xFF1E293B)
    val textSecondaryColor = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)
    
    // Configurar Google Sign-In
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }
    
    val googleSignInClient = remember {
        GoogleSignIn.getClient(context, gso)
    }
    
    // Launcher para Google Sign-In
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account.idToken?.let { idToken ->
                    viewModel.handleGoogleSignInResult(idToken)
                }
            } catch (e: ApiException) {
                // Error al obtener la cuenta
            }
        }
    }

    LaunchedEffect(uiState.isLoginSuccess) {
        if (uiState.isLoginSuccess) {
            onLoginSuccess(hasProfile, userName)
        }
    }

    // Verificar si ya hay un usuario logueado al iniciar
    LaunchedEffect(Unit) {
        viewModel.checkCurrentUser()
    }

    //Mensaje de exito
    LaunchedEffect(uiState.passwordResetEmailSent) {
        if (uiState.passwordResetEmailSent) {
            showSuccessDialog = true
        }
    }

    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF3B82F6),
                        Color(0xFF6366F1)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            // Logo de la app
            androidx.compose.foundation.Image(
                painter = painterResource(id = R.drawable.logo_app),
                contentDescription = "Logo",
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Tu solución en un toque",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(60.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Iniciar Sesión",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimaryColor
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Campo email
                    CustomTextField(
                        value = uiState.email,
                        onValueChange = { viewModel.onEmailChange(it) },
                        placeholder = "Correo electrónico",
                        icon = Icons.Default.Email
                    )

                    // Campo contraseña
                    CustomTextField(
                        value = uiState.password,
                        onValueChange = { viewModel.onPasswordChange(it) },
                        placeholder = "Contraseña",
                        icon = Icons.Default.Lock,
                        isPassword = true
                    )

                    // Mensaje de error
                    if (uiState.error != null) {
                        Text(
                            text = uiState.error ?: "",
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botón de iniciar sesión
                    PrimaryButton(
                        text = if (uiState.isLoading) "Cargando..." else "Iniciar Sesión",
                        onClick = { viewModel.login() },
                        enabled = !uiState.isLoading,
                        backgroundColor = Color(0xFF3B82F6)
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // ¿Olvidaste tu contraseña?
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TextButton(
                            onClick = { showForgotPasswordDialog = true }
                        ) {
                            Text(
                                text = "¿Olvidaste tu contraseña?",
                                color = Color(0xFF3B82F6),
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Divider con texto "o"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = Color.Gray.copy(alpha = 0.3f)
                        )
                        Text(
                            text = " o ",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = Color.Gray.copy(alpha = 0.3f)
                        )
                    }
                    
                    // Botón de Google Sign In
                    OutlinedButton(
                        onClick = {
                            viewModel.signInWithGoogle()
                            launcher.launch(googleSignInClient.signInIntent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            width = 1.dp
                        ),
                        enabled = !uiState.isLoading
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_google_logo),
                                contentDescription = "Google",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Continuar con Google",
                                color = textPrimaryColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Logo inferior fuera de la tarjeta
            androidx.compose.foundation.Image(
                painter = painterResource(id = R.drawable.logo_app_bottom),
                contentDescription = "Logo inferior",
                modifier = Modifier.size(100.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Indicador de carga
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.White
            )
        }
    }

    //Dialogo de recuperacion de contraseña
    if (showForgotPasswordDialog) {
        ForgotPasswordDialog(
            onDismiss = { showForgotPasswordDialog = false },
            onSendEmail = { email->
                viewModel.resetPassword(email)
                showForgotPasswordDialog = false
            }
        )
    }
    
    //Dialogo de Éxito
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = {
                Text(
                    text = "✅ ¡Correo Enviado!",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF10B981),
                    fontSize = 20.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "📧 Hemos enviado un correo de recuperación a tu email.",
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Revisa tu bandeja de entrada y sigue las instrucciones para restablecer tu contraseña.",
                        fontSize = 14.sp
                    )
                    Text(
                        text = "💡 Revisa también la carpeta de SPAM si no lo encuentras.",
                        fontSize = 12.sp,
                        color = textSecondaryColor
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showSuccessDialog = false }
                ) {
                    Text(
                        "Entendido",
                        color = Color(0xFF3B82F6),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
}



@Composable
fun ForgotPasswordDialog(
    onDismiss: () -> Unit,
    onSendEmail: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Recuperar Contraseña",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Ingresa tu correo electronico y te enviamos un enlace para restablecer tu contraseña.",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo electrónico")},
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email"
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF3B82F6),
                        focusedLabelColor = Color (0xFF3B82F6)
                    )
                )

            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (email.isNotEmpty()) {
                        onSendEmail(email)
                    }
                }

            ) {
                Text(
                    "Enviar",
                    color = Color(0xFF3B82F6),
                    fontWeight = FontWeight.Bold
                )
            }

        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color.Gray)
            }
        }

    )

}