package com.jkn.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jkn.mobile.ui.viewmodel.OperatorViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperatorScreen(
    // PERUBAHAN: Gunakan OperatorViewModel, bukan QueueViewModel
    viewModel: OperatorViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Menampilkan pesan error/sukses (Snackbar) agar UI tidak rusak
    LaunchedEffect(uiState.infoMessage, uiState.errorMessage) {
        uiState.errorMessage?.let {
            coroutineScope.launch { snackbarHostState.showSnackbar(it) }
            viewModel.clearMessages()
        }
        uiState.infoMessage?.let {
            coroutineScope.launch { snackbarHostState.showSnackbar(it) }
            viewModel.clearMessages()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFF0F0F0) // Background dari UI lama Anda
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
                    .padding(top = 40.dp), // Beri jarak dari tombol back
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "LOKET OPERATOR",
                    fontSize = 18.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                // PERUBAHAN: Fitur Dropdown Memilih Poli (Sesuai Task)
                var expanded by remember { mutableStateOf(false) }
                val selectedQueue = uiState.availableQueues.find { it.id == uiState.selectedQueueId }
                val selectedText = selectedQueue?.counterName ?: "Pilih Poli / Loket"

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = if (uiState.isLoading && uiState.availableQueues.isEmpty()) "Memuat..." else selectedText,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pilih Poli") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        uiState.availableQueues.forEach { queue ->
                            DropdownMenuItem(
                                text = { Text(queue.counterName) },
                                onClick = {
                                    viewModel.selectQueue(queue)
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // UI CARD BUATAN ANDA YANG KEREN KITA PERTAHANKAN
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
                        Text("Nomor Saat Ini", fontSize = 18.sp, color = Color.Gray)
                        Text(
                            text = uiState.currentNumber,
                            fontSize = 80.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.DarkGray
                        )

                        // Jika backend tidak mengirim nextNumber, kita bisa sembunyikan atau kalkulasi manual
                        // Namun karena di model QueueResponse ada nextNumber, kita pakai dari objek yang dipilih
                        selectedQueue?.let { q ->
                            Spacer(modifier = Modifier.height(32.dp))
                            Text("Nomor Berikutnya", fontSize = 16.sp, color = Color.Gray)
                            Text(
                                text = uiState.nextNumber,
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.DarkGray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                // UI TOMBOL NEXT BUATAN ANDA
                Button(
                    // PERUBAHAN: Memanggil onNextClicked, bukan nextQueue(1L)
                    onClick = { viewModel.onNextClicked() },
                    enabled = !uiState.isLoading && uiState.selectedQueueId != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00C6FF),
                        disabledContainerColor = Color.LightGray
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(36.dp))
                    } else {
                        Text("NEXT", fontSize = 32.sp, fontWeight = FontWeight.Black, color = Color.White)
                    }
                }
            }

            // Tombol Back Anda
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