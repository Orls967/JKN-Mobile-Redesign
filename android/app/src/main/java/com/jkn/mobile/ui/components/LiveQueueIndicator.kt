package com.jkn.mobile.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LiveQueueIndicator(
    currentNumberStr: String,
    myNumber: Int,
    modifier: Modifier = Modifier
) {
    val currentNumber = currentNumberStr.toIntOrNull() ?: 0
    val sisaAntrean = if (myNumber > currentNumber) myNumber - currentNumber else 0

    // Kalkulasi warna sesuai threshold (Hijau >5, Kuning 3-5, Merah <=2)
    val targetColor = when {
        myNumber <= 0 -> Color.LightGray
        sisaAntrean > 5 -> Color(0xFF4CAF50) // Hijau
        sisaAntrean in 3..5 -> Color(0xFFFFC107) // Kuning
        else -> Color(0xFFF44336) // Merah
    }

    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 500),
        label = "ColorAnimation"
    )

    // Kalkulasi progress (0.0 sampai 1.0)
    val rawProgress = if (myNumber > 0) currentNumber.toFloat() / myNumber.toFloat() else 0f
    val clampedProgress = rawProgress.coerceIn(0f, 1f)

    val animatedProgress by animateFloatAsState(
        targetValue = clampedProgress,
        animationSpec = tween(durationMillis = 1000),
        label = "ProgressAnimation"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val sisaText = if (myNumber <= 0) {
            "Masukkan nomor antrean Anda di bawah"
        } else if (sisaAntrean == 0 && currentNumber > 0 && currentNumber >= myNumber) {
            "Giliran Anda / Sudah Terlewat"
        } else {
            "Posisi Anda: $sisaAntrean nomor lagi"
        }

        Text(
            text = sisaText,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.DarkGray
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp),
            color = animatedColor,
            trackColor = Color(0xFFE0E0E0),
            // Material3 requires drawer shape to be modified if needed, but default is fine
        )
    }
}
