package com.jkn.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jkn.mobile.ui.components.LiveQueueIndicator
import com.jkn.mobile.ui.viewmodel.ConnectionStatus
import com.jkn.mobile.ui.viewmodel.PatientViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientQueueScreen(
    viewModel: PatientViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Local state for the text field to make typing smooth
    var tempNumberInput by remember { 
        mutableStateOf(if (uiState.myNumber > 0) uiState.myNumber.toString() else "") 
    }

    Scaffold(
        containerColor = Color(0xFFF0F0F0) // Background serasi dengan layar operator
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
                    .padding(top = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Connection Status Badge (Ac 2)
                ConnectionStatusBadge(status = uiState.connectionStatus)

                Spacer(modifier = Modifier.height(24.dp))

                // Current Number Banner (AC 1)
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Nomor Antrean Saat Ini", fontSize = 18.sp, color = Color.Gray)
                        Text(
                            text = uiState.currentNumber,
                            fontSize = 80.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.DarkGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Live Queue Indicator (INTI DEMO #5)
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LiveQueueIndicator(
                            currentNumberStr = uiState.currentNumber,
                            myNumber = uiState.myNumber
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // My Number Input (AC 3)
                OutlinedTextField(
                    value = tempNumberInput,
                    onValueChange = { 
                        tempNumberInput = it
                        viewModel.setMyNumber(it)
                    },
                    label = { Text("Nomor Anda (Untuk Simulasi Demo)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            }

            // Tombol Back
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopStart)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Kembali",
                    tint = Color.DarkGray,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun ConnectionStatusBadge(status: ConnectionStatus) {
    val (color, text) = when (status) {
        ConnectionStatus.CONNECTED -> Color(0xFF4CAF50) to "Live Connected" // Hijau
        ConnectionStatus.CONNECTING -> Color(0xFFFF9800) to "Connecting..." // Orange
        ConnectionStatus.DISCONNECTED -> Color(0xFFF44336) to "Disconnected" // Merah
        ConnectionStatus.RECONNECTING -> Color(0xFFFF9800) to "Reconnecting..." // Orange
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(color = color.copy(alpha = 0.15f), shape = RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}
