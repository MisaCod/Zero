package com.example.zero.ui.screens.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zero.ui.viewmodel.AuthViewModel

// ============================================================================
// LoginScreen — Diseño premium 0 Grados con gradiente azul glacial
// ============================================================================

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel = viewModel(),
    onLoginSuccess: () -> Unit = {},
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val primaryBlue = Color(0xFF0B4F7A)
    val lightCyan = Color(0xFF00D4FF)
    val darkBg = Color(0xFF04111C)
    val cardBg = Color(0xFF0A2236)
    val surfaceLight = Color(0xFF0F2D47)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(darkBg, Color(0xFF071828), Color(0xFF04111C))
                )
            ),
    ) {
        // ── Decoración de fondo — círculos difusos ────────────────────────
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-80).dp, y = (-60).dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(primaryBlue.copy(alpha = 0.25f), Color.Transparent),
                        radius = 400f,
                    ),
                    shape = CircleShape,
                )
        )
        Box(
            modifier = Modifier
                .size(250.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 60.dp, y = 60.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(lightCyan.copy(alpha = 0.12f), Color.Transparent),
                        radius = 350f,
                    ),
                    shape = CircleShape,
                )
        )

        // ── Contenido principal ───────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Spacer(Modifier.height(60.dp))

            // ── Logo + marca ──────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(primaryBlue, lightCyan.copy(alpha = 0.8f))
                        ),
                        shape = RoundedCornerShape(22.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.AcUnit,
                    contentDescription = "0 Grados",
                    tint = Color.White,
                    modifier = Modifier.size(44.dp),
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "0 Grados",
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = (-0.5).sp,
            )
            Text(
                text = "Gestión de Mantenimiento Industrial",
                fontSize = 13.sp,
                color = lightCyan.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp),
            )

            Spacer(Modifier.height(40.dp))

            // ── Card de login ─────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = "Iniciar Sesión",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    Text(
                        text = "Acceso exclusivo para personal autorizado",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.5f),
                    )

                    Spacer(Modifier.height(4.dp))

                    // ── Campo Email ───────────────────────────────────────
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "CORREO ELECTRÓNICO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = lightCyan.copy(alpha = 0.7f),
                            letterSpacing = 1.sp,
                        )
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text("usuario@0grados.com", color = Color.White.copy(alpha = 0.25f))
                            },
                            leadingIcon = {
                                Icon(Icons.Outlined.Email, null, tint = lightCyan.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                                focusedBorderColor = lightCyan.copy(alpha = 0.7f),
                                unfocusedContainerColor = surfaceLight,
                                focusedContainerColor = surfaceLight,
                                unfocusedTextColor = Color.White,
                                focusedTextColor = Color.White,
                                cursorColor = lightCyan,
                            ),
                        )
                    }

                    // ── Campo Password ────────────────────────────────────
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "CONTRASEÑA",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = lightCyan.copy(alpha = 0.7f),
                            letterSpacing = 1.sp,
                        )
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text("••••••••", color = Color.White.copy(alpha = 0.25f))
                            },
                            leadingIcon = {
                                Icon(Icons.Outlined.Lock, null, tint = lightCyan.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.4f),
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            },
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                focusManager.clearFocus()
                                authViewModel.login(email, password, onSuccess = onLoginSuccess)
                            }),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                                focusedBorderColor = lightCyan.copy(alpha = 0.7f),
                                unfocusedContainerColor = surfaceLight,
                                focusedContainerColor = surfaceLight,
                                unfocusedTextColor = Color.White,
                                focusedTextColor = Color.White,
                                cursorColor = lightCyan,
                            ),
                        )
                    }

                    // ── Error message ─────────────────────────────────────
                    AnimatedVisibility(
                        visible = authViewModel.errorMsg != null,
                        enter = expandVertically() + fadeIn(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Outlined.Error, null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                            Text(
                                text = authViewModel.errorMsg ?: "",
                                color = Color(0xFFEF4444),
                                fontSize = 13.sp,
                            )
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    // ── Botón de login ────────────────────────────────────
                    if (authViewModel.isLoading) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = lightCyan, modifier = Modifier.size(36.dp))
                        }
                    } else {
                        Button(
                            onClick = {
                                authViewModel.login(email, password, onSuccess = onLoginSuccess)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues(0.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(primaryBlue, lightCyan.copy(alpha = 0.85f))
                                        ),
                                        shape = RoundedCornerShape(14.dp),
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(Icons.Outlined.Login, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    Text(
                                        text = "Iniciar Sesión",
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── Footer ────────────────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Box(modifier = Modifier.size(6.dp).background(lightCyan.copy(alpha = 0.5f), CircleShape))
                    Text("Supabase Realtime", fontSize = 11.sp, color = Color.White.copy(alpha = 0.35f))
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Box(modifier = Modifier.size(6.dp).background(Color(0xFF10B981).copy(alpha = 0.7f), CircleShape))
                    Text("v1.0.0 Beta", fontSize = 11.sp, color = Color.White.copy(alpha = 0.35f))
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}
