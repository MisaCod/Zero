package com.example.zero.ui.screens.faq

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zero.data.model.UserRole

data class FaqItem(val question: String, val answer: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaqScreen(
    userRole: UserRole,
    onBack: () -> Unit = {}
) {
    val darkBg = Color(0xFF050E17)
    val cardBg = Color(0xFF0A2236)
    val cyan = Color(0xFF00C8F0)
    val textPrimary = Color(0xFFF0F8FF)
    val textSecondary = Color(0xFF7BA9C4)

    val faqs = when (userRole) {
        UserRole.CLIENTE -> listOf(
            FaqItem("¿Cómo solicito un servicio de mantenimiento?", "Ve a la sección 'Solicitudes', toca el botón '+' y llena el formulario con los detalles de tu equipo y el problema."),
            FaqItem("¿Cuánto tiempo tarda un técnico en responder?", "Normalmente, el supervisor asiganará o un técnico aceptará tu solicitud en menos de 24 horas. Recibirás notificaciones con el progreso."),
            FaqItem("¿Puedo modificar la fecha de una solicitud?", "Si la solicitud aún está en estado PENDIENTE, puedes cancelarla o contactar al supervisor. Si ya fue asignada, debes coordinar con el técnico."),
            FaqItem("¿Cómo califico el servicio de un técnico?", "Una vez que el técnico marque el reporte como COMPLETADO, te aparecerá la opción de calificar el servicio desde la vista de detalles de la solicitud.")
        )
        UserRole.TECNICO -> listOf(
            FaqItem("¿Cómo actualizo el estado de una solicitud?", "Abre la solicitud asignada y usa los botones de estado (En Progreso, Completado). Recuerda llenar el reporte técnico antes de completarla."),
            FaqItem("¿Qué hago si necesito un repuesto que no tengo?", "Puedes verificar el inventario desde la pantalla 'Repuestos'. Si no está disponible, indícalo en tu reporte técnico y avisa al supervisor para que se gestione la compra."),
            FaqItem("¿Cómo genero el reporte técnico final?", "Al finalizar el trabajo, presiona el botón para llenar el 'Reporte Técnico', detalla los hallazgos, tareas realizadas, repuestos utilizados y las recomendaciones.")
        )
        UserRole.SUPERVISOR -> listOf(
            FaqItem("¿Cómo asigno una solicitud a un técnico?", "Ve a 'Solicitudes', selecciona una que esté PENDIENTE y presiona el botón 'Asignar'. Luego selecciona al técnico disponible de la lista."),
            FaqItem("¿Dónde puedo exportar los datos del sistema?", "Abre el menú lateral (3 rayas), ve a 'Backup & Exportar' y podrás descargar los reportes, equipos y usuarios en formato CSV."),
            FaqItem("¿Cómo registro un nuevo técnico o cliente en el sistema?", "En la barra inferior, selecciona 'Usuarios', presiona el botón '+' y completa sus datos asignando el rol correspondiente.")
        )
        else -> emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Preguntas Frecuentes", color = textPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = cardBg)
            )
        },
        containerColor = darkBg
    ) { pad ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(pad),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(cyan.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.HelpOutline, contentDescription = null, tint = cyan, modifier = Modifier.size(24.dp))
                    }
                    Column {
                        Text("Centro de Ayuda", color = textPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("Respuestas para perfil: ${userRole.value}", color = textSecondary, fontSize = 14.sp)
                    }
                }
            }

            items(faqs) { faq ->
                FaqCard(faq, cardBg, textPrimary, textSecondary, cyan)
            }
        }
    }
}

@Composable
fun FaqCard(faq: FaqItem, cardBg: Color, textPrimary: Color, textSecondary: Color, cyan: Color) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = faq.question,
                    color = if (expanded) cyan else textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = if (expanded) cyan else textSecondary
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = faq.answer,
                        color = textSecondary,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}
