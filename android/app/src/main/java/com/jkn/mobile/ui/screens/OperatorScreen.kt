package com.jkn.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jkn.mobile.ui.viewmodel.QueueViewModel

@Composable
fun OperatorScreen(
    viewModel: QueueViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchQueue(1L)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
            .background(Color(0xFFF0F0F0))
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (uiState.queue != null) {
            val queue = uiState.queue!!

            Text(
                text = "LOKET OPERATOR",
                fontSize = 18.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = queue.counterName,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1A73E8)
            )

            Spacer(modifier = Modifier.height(48.dp))

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Nomor Saat Ini", fontSize = 18.sp, color = Color.Gray)
                    Text(
                        text = queue.currentNumber.toString(),
                        fontSize = 80.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.DarkGray
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Text("Nomor Berikutnya", fontSize = 16.sp, color = Color.Gray)
                    Text(
                        text = queue.nextNumber.toString(),
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = { viewModel.nextQueue(1L) },
                enabled = !uiState.isLoading,
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

            uiState.errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Error: $error", color = Color.Red)
            }
        } else if (uiState.isLoading) {
            CircularProgressIndicator()
        } else {
            uiState.errorMessage?.let { error ->
                Text(text = "Error: $error", color = Color.Red)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { viewModel.fetchQueue(1L) }) {
                    Text("Retry")
                }
            }
        }
    }

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
