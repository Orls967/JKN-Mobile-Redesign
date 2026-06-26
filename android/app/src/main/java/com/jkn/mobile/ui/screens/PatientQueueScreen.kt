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

    val context = androidx.compose.ui.platform.LocalContext.current
    var showPermissionRationale by remember { mutableStateOf(false) }

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            // Handle permission denied logic or show a rationale next time
        }
    }

    LaunchedEffect(Unit) {
        viewModel.startObserving(context)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val activity = context as? android.app.Activity
            if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                if (activity != null && androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(activity, android.Manifest.permission.POST_NOTIFICATIONS)) {
                    showPermissionRationale = true
                } else {
                    permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    if (showPermissionRationale) {
        AlertDialog(
            onDismissRequest = { showPermissionRationale = false },
            title = { Text("Izin Notifikasi Dibutuhkan") },
            text = { Text("Aplikasi membutuhkan izin notifikasi untuk memberi tahu Anda saat antrean sudah dekat.") },
            confirmButton = {
                TextButton(onClick = {
                    showPermissionRationale = false
                    permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }) {
                    Text("Izinkan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionRationale = false }) {
                    Text("Tolak")
                }
            }
        )
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

                Spacer(modifier = Modifier.height(24.dp))

                // Estimasi Waktu Tunggu (ETA)
                if (uiState.myNumber > 0) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("Estimasi Waktu Tunggu", fontSize = 14.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(4.dp))
                            val etaText = if (uiState.etaMinutes == -1) {
                                "Menghitung..."
                            } else if (uiState.etaMinutes == 0) {
                                "Antrean Anda sudah dekat / terlewat"
                            } else {
                                "~ ${uiState.etaMinutes} Menit"
                            }
                            Text(
                                text = etaText,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.etaMinutes == 0) Color(0xFF4CAF50) else Color.DarkGray
                            )
                        }
                    }
                }
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
