package com.jkn.mobile.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.jkn.mobile.ui.viewmodel.ConnectionStatus
import com.jkn.mobile.ui.viewmodel.QueueUiState
import com.jkn.mobile.ui.viewmodel.QueueViewModel
import com.jkn.mobile.utils.NotificationHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueScreen(
    viewModel: QueueViewModel = viewModel(),
    onNavigateToOperator: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Initialize Notification Channel
    LaunchedEffect(Unit) {
        NotificationHelper.createNotificationChannel(context)
    }

    // Request POST_NOTIFICATIONS Permission on Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Do nothing, handled implicitly */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Listen for Backend-driven Proximity Notifications
    LaunchedEffect(Unit) {
        viewModel.showProximityNotifEvent.collect { remainingQueue ->
            NotificationHelper.showProximityNotification(context, remainingQueue)
        }
    }

    // Fetch queue with ID 1 when screen first composes
    LaunchedEffect(Unit) {
        viewModel.fetchQueue(1)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "JKN Antrean",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ConnectionStatusBanner(uiState.connectionStatus)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator()
                    }

                    uiState.errorMessage != null -> {
                        ErrorContent(
                            message = uiState.errorMessage!!,
                            onRetry = { viewModel.fetchQueue(1) }
                        )
                    }

                    uiState.queue != null -> {
                        QueueContent(
                            uiState = uiState,
                            onNavigateToOperator = onNavigateToOperator
                        )
                    }

                    else -> {
                        Text(
                            text = "Tekan tombol untuk memuat data antrean",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ConnectionStatusBanner(status: ConnectionStatus) {
    val (color, text) = when (status) {
        ConnectionStatus.CONNECTING -> Color(0xFFFFA000) to "Menghubungkan ke Server (Realtime)..."
        ConnectionStatus.CONNECTED -> Color(0xFF4CAF50) to "Terhubung secara Realtime"
        ConnectionStatus.RECONNECTING -> Color(0xFFE64A19) to "Koneksi terputus. Mencoba kembali..."
        ConnectionStatus.DISCONNECTED -> Color.Gray to "Memuat Koneksi..."
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color)
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun QueueContent(
    uiState: QueueUiState,
    onNavigateToOperator: () -> Unit
) {
    val queue = uiState.queue ?: return
    val currentNumber = queue.currentNumber
    val myTicketNumber = uiState.myTicketNumber
    val context = LocalContext.current

    // Anti-Spam Notification Logic (For Exact Call)
    var hasNotifiedCall by rememberSaveable(myTicketNumber) { mutableStateOf(false) }

    LaunchedEffect(currentNumber) {
        if (currentNumber == myTicketNumber && !hasNotifiedCall) {
            NotificationHelper.showQueueNotification(context, myTicketNumber)
            hasNotifiedCall = true
        }
    }

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Counter Name
        Text(
            text = queue.counterName,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        // Live Queue Indicator
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Nomor Antrean Anda: $myTicketNumber", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                
                when {
                    currentNumber < myTicketNumber -> {
                        val remaining = myTicketNumber - currentNumber
                        if (remaining <= 3) {
                            Text(text = "Status: Bersiap", fontWeight = FontWeight.SemiBold, color = Color(0xFFFF9800))
                            Text(text = "Nomor Anda tinggal $remaining antrean lagi!", color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        } else {
                            Text(text = "Status: Aman", fontWeight = FontWeight.SemiBold)
                            Text(text = "Sisa antrean di depan Anda: $remaining orang", color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }
                    }
                    currentNumber == myTicketNumber -> {
                        Text(text = "Status: Sedang Dipanggil!", color = Color(0xFF4CAF50), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        Text(text = "Silakan segera menuju loket.")
                    }
                    else -> {
                        Text(text = "Status: Sudah Terlewati", color = Color.Red, fontWeight = FontWeight.Bold)
                        Text(text = "Nomor antrean Anda telah lewat.")
                    }
                }
            }
        }

        // Current Number Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Nomor Saat Ini",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${queue.currentNumber}",
                    fontSize = 64.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // Next Number
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Nomor Berikutnya",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "${queue.nextNumber}",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        // Timestamps
        Text(
            text = "Dibuat: ${queue.createdAt}",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))
        OutlinedButton(onClick = onNavigateToOperator) {
            Text("Masuk ke Layar Operator")
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Terjadi Kesalahan",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )
        Text(
            text = message,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(onClick = onRetry) {
            Text("Coba Lagi")
        }
    }
}
