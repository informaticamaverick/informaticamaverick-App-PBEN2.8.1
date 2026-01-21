package com.example.myapplication.prestador.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.prestador.ui.theme.PrestadorOrange
import com.example.myapplication.prestador.ui.theme.PrestadorOrangeDark
import androidx.hilt.navigation.compose.hiltViewModel
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.blur
import androidx.compose.animation.core.*
import com.example.myapplication.prestador.R


@Composable
fun PrestadorLoginScreen(
    onLoginSuccess: (hasProfile: Boolean) -> Unit,
    viewModel: PrestadorLoginViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
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
                } ?: run {
                    // Si el idToken es null
                    errorMessage = "No se pudo obtener el token de Google. Verifica la configuración de Firebase."
                    isLoading = false
                }
            } catch (e: ApiException) {
                errorMessage = "Error al iniciar sesión con Google: ${e.message}"
                isLoading = false
            }
        } else {
            // Si el usuario cancela el login
            isLoading = false
        }
    }

    val loginState by viewModel.loginState.collectAsState()
    val hasProfile by viewModel.hasProfile.collectAsState()
    val passwordResetEmailSent by viewModel.passwordResetEmailSent.collectAsState()
    
    LaunchedEffect(loginState) {
        when (loginState) {
            is LoginState.Success -> {
                onLoginSuccess(hasProfile)
            }
            is LoginState.Error -> {
                isLoading = false
                errorMessage = (loginState as LoginState.Error).message
            }
            is LoginState.Loading -> {
                isLoading = true
                errorMessage = null
            }
            is LoginState.Idle -> {
                isLoading = false
            }
        }
    }
    
    // Mostrar diálogo de éxito cuando se envía el email
    LaunchedEffect(passwordResetEmailSent) {
        if (passwordResetEmailSent) {
            showSuccessDialog = true
        }
    }

    // Animación de pulso para el círculo superior izquierdo
    val infiniteTransition = rememberInfiniteTransition(label = "pulseAnimation")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF5ED))
    ) {
        // --- EFECTO DE DIFUMINADO ---
        
        // 1. Círculo Superior Izquierda (Con animación de pulso)
        Box(
            modifier = Modifier
                .offset(x = (-96).dp, y = (-96).dp)
                .size(384.dp)
                .background(
                    color = Color(0xFFFB923C).copy(alpha = pulseAlpha),
                    shape = CircleShape
                )
                .blur(radius = 80.dp)
        )
        
        // 2. Círculo Inferior Derecha
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 80.dp, y = 0.dp)
                .size(320.dp)
                .background(
                    color = Color(0xFFFBBF24).copy(alpha = 0.4f),
                    shape = CircleShape
                )
                .blur(radius = 80.dp)
        )
        
        // 3. Círculo Central/Inferior (Para conectar los colores)
        Box(
            modifier = Modifier
                .offset(x = 100.dp, y = 200.dp)
                .size(256.dp)
                .background(
                    color = Color(0xFFFCA5A5).copy(alpha = 0.3f),
                    shape = CircleShape
                )
                .blur(radius = 100.dp)
        )
        
        // Contenido principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
        // Logo o título
        Text(
            text = "PBEM Prestador",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Tarjeta de login
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 0.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Ingresa a tu cuenta",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
        
        // Campo de Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text("Correo electrónico", color = Color.Gray) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Email",
                    tint = Color.Gray
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF8FAFC),
                unfocusedContainerColor = Color(0xFFF8FAFC),
                focusedBorderColor = PrestadorOrange,
                unfocusedBorderColor = Color(0xFFE2E8F0),
                focusedTextColor = Color(0xFF1E293B),
                unfocusedTextColor = Color(0xFF1E293B),
                cursorColor = PrestadorOrange
            ),
            shape = RoundedCornerShape(12.dp)
        )
        
        // Campo de Contraseña
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("Contraseña", color = Color.Gray) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Password",
                    tint = Color.Gray
                )
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        painter = painterResource(
                            id = if (passwordVisible) R.drawable.ic_eye_open else R.drawable.ic_eye_closed
                        ),
                        contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                        tint = if (passwordVisible) PrestadorOrange else Color.Gray
                    )
                }
            },
            visualTransformation = if (passwordVisible) 
                VisualTransformation.None 
            else 
                PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF8FAFC),
                unfocusedContainerColor = Color(0xFFF8FAFC),
                focusedBorderColor = PrestadorOrange,
                unfocusedBorderColor = Color(0xFFE2E8F0),
                focusedTextColor = Color(0xFF1E293B),
                unfocusedTextColor = Color(0xFF1E293B),
                cursorColor = PrestadorOrange
            ),
            shape = RoundedCornerShape(12.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Mensaje de error
        if (errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Botón de Login
        Button(
            onClick = {
                if (email.isNotBlank() && password.isNotBlank()) {
                    viewModel.login(email, password)
                } else {
                    errorMessage = "Por favor completa todos los campos"
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrestadorOrange
            ),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    text = "Iniciar Sesión",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Opción de recuperar contraseña
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            TextButton(onClick = { showForgotPasswordDialog = true }) {
                Text(
                    text = "¿Olvidaste tu contraseña?",
                    color = PrestadorOrange,
                    fontSize = 14.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Divider con texto "o"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
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
            enabled = !isLoading
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
                    color = Color(0xFF1E293B),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
    
    // Diálogo de recuperación de contraseña
    if (showForgotPasswordDialog) {
        ForgotPasswordDialog(
            onDismiss = { showForgotPasswordDialog = false },
            onSendEmail = { emailToReset ->
                viewModel.resetPassword(emailToReset)
                showForgotPasswordDialog = false
            }
        )
    }
    
    // Diálogo de éxito
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                viewModel.resetPasswordEmailSentFlag()
            },
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
                        color = Color.Gray
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        viewModel.resetPasswordEmailSentFlag()
                    }
                ) {
                    Text(
                        "Entendido",
                        color = PrestadorOrange,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
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
                    text = "Ingresa tu correo electrónico y te enviaremos un enlace para restablecer tu contraseña.",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo electrónico") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email"
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrestadorOrange,
                        focusedLabelColor = PrestadorOrange
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
                    color = PrestadorOrange,
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
