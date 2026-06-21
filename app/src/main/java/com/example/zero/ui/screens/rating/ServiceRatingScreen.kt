package com.example.zero.ui.screens.rating

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zero.data.model.ServiceRating
import com.example.zero.data.repository.ServiceRatingRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

// ViewModel inline para rating
class ServiceRatingViewModel : ViewModel() {
    private val repo = ServiceRatingRepository()
    var score by androidx.compose.runtime.mutableIntStateOf(5)
        private set
    var comments by androidx.compose.runtime.mutableStateOf("")
        private set
    var isLoading by androidx.compose.runtime.mutableStateOf(false)
        private set
    var isSubmitting by androidx.compose.runtime.mutableStateOf(false)
        private set
    var submitSuccess by androidx.compose.runtime.mutableStateOf(false)
        private set
    var alreadyRated by androidx.compose.runtime.mutableStateOf(false)
        private set
    var errorMsg by androidx.compose.runtime.mutableStateOf<String?>(null)
        private set

    fun load(assigmentId: String) {
        isLoading = true
        viewModelScope.launch {
            repo.getRating(assigmentId).onSuccess { rating ->
                if (rating != null) { alreadyRated = true; score = rating.score }
            }
            isLoading = false
        }
    }
    fun updateScore(s: Int) { score = s }
    fun updateComments(c: String) { comments = c }
    fun submit(assigmentId: String) {
        isSubmitting = true; errorMsg = null
        viewModelScope.launch {
            repo.insertRating(ServiceRating(assigmentId = assigmentId, score = score, comments = comments.ifBlank { null })).fold(
                onSuccess = { isSubmitting = false; submitSuccess = true },
                onFailure = { e -> isSubmitting = false; errorMsg = "Error al enviar: ${e.localizedMessage}" }
            )
        }
    }
    fun clearError() { errorMsg = null }
    fun resetSuccess() { submitSuccess = false }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceRatingScreen(
    assigmentId: String,
    vm: ServiceRatingViewModel = viewModel(),
    onBack: () -> Unit = {},
) {
    val darkBg = Color(0xFF050E17); val cardBg = Color(0xFF0A2236); val surfaceBg = Color(0xFF0F2D47)
    val cyan = Color(0xFF00C8F0); val blue = Color(0xFF0B4F7A)
    val gold = Color(0xFFF59E0B); val textPrimary = Color(0xFFF0F8FF); val textSecondary = Color(0xFF7BA9C4)

    LaunchedEffect(assigmentId) { vm.load(assigmentId) }
    LaunchedEffect(vm.submitSuccess) { if (vm.submitSuccess) { vm.resetSuccess(); onBack() } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calificar Servicio", color = textPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = textPrimary) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = cardBg),
            )
        },
        containerColor = darkBg,
    ) { pad ->
        when {
            vm.isLoading -> Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = cyan) }
            vm.alreadyRated -> {
                Column(Modifier.fillMaxSize().padding(pad), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.CheckCircle, null, tint = Color(0xFF10B981), modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("¡Ya calificaste este servicio!", color = textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("Tu calificación: ${"★".repeat(vm.score)}${"☆".repeat(5 - vm.score)}", color = gold, fontSize = 24.sp)
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = blue)) { Text("Volver", color = Color.White) }
                }
            }
            else -> Column(
                modifier = Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(8.dp))
                Icon(Icons.Filled.StarRate, null, tint = gold, modifier = Modifier.size(56.dp))
                Text("¿Cómo fue el servicio?", color = textPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Text("Tu opinión nos ayuda a mejorar", color = textSecondary, fontSize = 14.sp, textAlign = TextAlign.Center)

                // Estrellas
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = cardBg)) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = when (vm.score) { 1 -> "Muy Malo"; 2 -> "Malo"; 3 -> "Regular"; 4 -> "Bueno"; else -> "Excelente" },
                            color = when (vm.score) { 1 -> Color(0xFFEF4444); 2 -> Color(0xFFFF6B6B); 3 -> Color(0xFFF59E0B); 4 -> cyan; else -> Color(0xFF10B981) },
                            fontSize = 18.sp, fontWeight = FontWeight.Bold,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            (1..5).forEach { star ->
                                val starColor by animateColorAsState(if (star <= vm.score) gold else Color.White.copy(alpha = 0.2f), animationSpec = tween(150))
                                Icon(
                                    imageVector = if (star <= vm.score) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                    contentDescription = "Estrella $star",
                                    tint = starColor,
                                    modifier = Modifier.size(44.dp).clickable { vm.updateScore(star) },
                                )
                            }
                        }
                    }
                }

                // Comentarios
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("COMENTARIOS (OPCIONAL)", color = cyan.copy(alpha = 0.8f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
                    OutlinedTextField(
                        value = vm.comments,
                        onValueChange = vm::updateComments,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                        placeholder = { Text("¿Qué mejorarías? ¿Quedaste satisfecho?", color = textSecondary.copy(alpha = 0.4f)) },
                        maxLines = 5,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.White.copy(alpha = 0.12f), focusedBorderColor = cyan.copy(alpha = 0.7f),
                            unfocusedContainerColor = surfaceBg, focusedContainerColor = surfaceBg,
                            unfocusedTextColor = textPrimary, focusedTextColor = textPrimary, cursorColor = cyan,
                        ),
                    )
                }

                vm.errorMsg?.let {
                    Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFEF4444).copy(alpha = 0.12f), RoundedCornerShape(12.dp)).padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Error, null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                        Text(it, color = Color(0xFFEF4444), fontSize = 13.sp)
                    }
                }

                if (vm.isSubmitting) {
                    CircularProgressIndicator(color = cyan)
                } else {
                    Button(
                        onClick = { vm.submit(assigmentId) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(blue, gold.copy(alpha = 0.9f))), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Star, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                Text("Enviar Calificación", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}
