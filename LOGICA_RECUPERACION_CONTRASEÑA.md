# 🔐 Lógica de Recuperación de Contraseña

## ✅ Implementación Actual

### 1. AuthRepository.kt
```kotlin
suspend fun sendPasswordResetEmail(email: String): Result<Unit>
```
- ✅ Configurado idioma español (`auth.setLanguageCode("es")`)
- ✅ Envía correo de recuperación con Firebase Auth
- ✅ Manejo de errores con Result

### 2. LoginViewModel.kt
```kotlin
fun resetPassword(email: String)
```
- ✅ Validación de email vacío
- ✅ Validación de formato de email (@)
- ✅ Mensajes de error personalizados:
  - "No existe una cuenta con este email"
  - "Email inválido"
  - "Demasiados intentos. Intenta más tarde"
- ✅ Estado `passwordResetEmailSent` para mostrar diálogo de éxito

### 3. LoginScreen.kt
- ✅ Botón "¿Olvidaste tu contraseña?"
- ✅ Diálogo `ForgotPasswordDialog` para ingresar email
- ✅ Diálogo de éxito con:
  - ✅ Icono de confirmación
  - ✅ Instrucciones claras
  - ✅ Recordatorio de revisar SPAM

---

## 🔄 Flujo del Usuario

```
1. Usuario en pantalla de login
   ↓
2. Click en "¿Olvidaste tu contraseña?"
   ↓
3. Aparece diálogo para ingresar email
   ↓
4. Usuario ingresa su email
   ↓
5. Click en "Enviar"
   ↓
6. Validaciones:
   - ¿Email vacío? → Error: "Por favor ingresa tu email"
   - ¿Email sin @? → Error: "Email inválido"
   - ¿Usuario no existe? → Error: "No existe una cuenta con este email"
   - ¿Demasiados intentos? → Error: "Demasiados intentos. Intenta más tarde"
   ↓
7. Firebase envía correo de recuperación EN ESPAÑOL
   ↓
8. Diálogo de éxito:
   "📧 Hemos enviado un correo de recuperación a tu email"
   "✅ Revisa tu bandeja de entrada..."
   "💡 Revisa también la carpeta de SPAM..."
   ↓
9. Usuario revisa su email
   ↓
10. Click en el link de Firebase (válido por 1 hora)
   ↓
11. Redirige a página de Firebase para crear nueva contraseña
   ↓
12. Usuario crea nueva contraseña
   ↓
13. ✅ Contraseña actualizada
   ↓
14. Usuario puede iniciar sesión con la nueva contraseña
```

---

## 📧 Personalización del Email (Firebase Console)

### Para personalizar el correo de recuperación:

1. **Ve a Firebase Console:**
   - https://console.firebase.google.com/
   - Selecciona tu proyecto "MyApplication"

2. **Authentication > Templates:**
   - Click en "Templates" (Plantillas)
   - Selecciona "Password reset" (Restablecer contraseña)

3. **Personaliza el mensaje:**
```
Asunto: Recupera tu contraseña - MyApplication

Hola,

Hemos recibido una solicitud para restablecer tu contraseña en MyApplication.

Para crear una nueva contraseña, haz click en el siguiente enlace:
%LINK%

Este enlace expira en 1 hora.

Si no solicitaste este cambio, ignora este correo y tu contraseña permanecerá sin cambios.

Saludos,
El equipo de MyApplication
```

4. **Configurar idioma:**
   - Authentication > Settings
   - Language: Spanish (es)

---

## 🔐 Seguridad Implementada

✅ **Validación de email** - Formato correcto requerido
✅ **Verificación de cuenta** - Solo envía si el email existe
✅ **Link temporal** - Expira en 1 hora
✅ **Límite de intentos** - Protección contra spam
✅ **Email en español** - `auth.setLanguageCode("es")`
✅ **Mensajes claros** - Usuario sabe qué hacer

---

## ⚠️ Casos de Error Manejados

| Error | Mensaje al Usuario |
|-------|-------------------|
| Email vacío | "Por favor ingresa tu email" |
| Email sin @ | "Email inválido" |
| Usuario no existe | "No existe una cuenta con este email" |
| Demasiados intentos | "Demasiados intentos. Intenta más tarde" |
| Email inválido (Firebase) | "Email inválido" |
| Otro error | "Error al enviar el correo: [mensaje]" |

---

## 🧪 Para Probar

1. **Abre la app**
2. **Pantalla de login** → Click en "¿Olvidaste tu contraseña?"
3. **Ingresa un email registrado**
4. **Click en "Enviar"**
5. **Revisa tu bandeja** (puede tardar 1-2 minutos)
6. **Click en el link** del correo
7. **Crea nueva contraseña** en la página de Firebase
8. **Inicia sesión** con la nueva contraseña

---

## 📝 Notas Importantes

- El correo puede tardar unos minutos en llegar
- Revisa la carpeta de SPAM si no aparece
- El link expira después de 1 hora
- Puedes solicitar múltiples resets (con límite de intentos)
- Si el email no existe, Firebase NO lo dice por seguridad (pero nosotros sí)
- La contraseña solo cambia cuando el usuario completa el proceso

---

## 🆚 Comparación con Cambio de Email

| Aspecto | Recuperación Contraseña | Cambio de Email |
|---------|------------------------|-----------------|
| **Autenticación previa** | ❌ No requiere | ✅ Requiere contraseña |
| **Verificación** | ✅ Link en email actual | ✅ Link en email nuevo |
| **Actualización** | Inmediata al verificar | Al verificar link |
| **Seguridad** | Link expira en 1h | Link expira en 1h |
| **Idioma** | ✅ Español | ✅ Español |

---

## ✅ TODO Listo

La lógica de recuperación de contraseña está completamente implementada y lista para usar:

✅ Validaciones completas
✅ Mensajes de error claros
✅ Email en español
✅ Diálogo de éxito mejorado
✅ Protección contra spam
✅ Seguridad Firebase
✅ Experiencia de usuario fluida

🚀 **¡Lista para producción!**
