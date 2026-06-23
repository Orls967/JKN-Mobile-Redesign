package com.jkn.mobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jkn.mobile.data.MockDataProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AntreanFaskesPertamaScreen(
    onBack: () -> Unit,
    onSimpan: () -> Unit
) {
    val faskes = MockDataProvider.profile.faskes
    var keluhan by remember { mutableStateOf("") }
    
    // Dropdown states
    var expandedPeserta by remember { mutableStateOf(false) }
    var selectedPeserta by remember { mutableStateOf(MockDataProvider.profile.name) }
    
    var expandedPoli by remember { mutableStateOf(false) }
    val poliOptions = listOf("Poli Umum", "Poli Gigi", "Poli KIA")
    var selectedPoli by remember { mutableStateOf(poliOptions[0]) }

    var expandedTanggal by remember { mutableStateOf(false) }
    val tanggalOptions = listOf("Hari ini", "Besok")
    var selectedTanggal by remember { mutableStateOf(tanggalOptions[0]) }

    var expandedJadwal by remember { mutableStateOf(false) }
    val jadwalOptions = listOf("dr. Andi (08:00 - 12:00)", "dr. Budi (13:00 - 16:00)")
    var selectedJadwal by remember { mutableStateOf(jadwalOptions[0]) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fasilitas Kesehatan Tingkat Pertama", color = Color.White, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1565C0)
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            item {
                // Section: Peserta
                Text("Peserta", color = Color(0xFF1976D2), fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp))
                ExposedDropdownMenuBox(
                    expanded = expandedPeserta,
                    onExpandedChange = { expandedPeserta = !expandedPeserta }
                ) {
                    OutlinedTextField(
                        value = selectedPeserta,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedPeserta) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1976D2),
                            unfocusedBorderColor = Color(0xFFBDBDBD)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expandedPeserta,
                        onDismissRequest = { expandedPeserta = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(MockDataProvider.profile.name) },
                            onClick = {
                                selectedPeserta = MockDataProvider.profile.name
                                expandedPeserta = false
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Section: Faskes Tingkat Pertama (Info Card)
                Text("Faskes Tingkat Pertama", color = Color(0xFF1976D2), fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 4.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(faskes, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row {
                            Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Alamat", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Jl. Sehat Selalu No. 123", style = MaterialTheme.typography.labelSmall)
                        }
                        Row {
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Telepon", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("021-12345678", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Section: Pilih Poli
                Text("Pilih Poli", color = Color(0xFF1976D2), fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 4.dp))
                ExposedDropdownMenuBox(
                    expanded = expandedPoli,
                    onExpandedChange = { expandedPoli = !expandedPoli }
                ) {
                    OutlinedTextField(
                        value = selectedPoli,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedPoli) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1976D2),
                            unfocusedBorderColor = Color(0xFFBDBDBD)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expandedPoli,
                        onDismissRequest = { expandedPoli = false }
                    ) {
                        poliOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    selectedPoli = option
                                    expandedPoli = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Section: Pilih Tanggal Daftar
                Text("Pilih Tanggal Daftar", color = Color(0xFF1976D2), fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 4.dp))
                ExposedDropdownMenuBox(
                    expanded = expandedTanggal,
                    onExpandedChange = { expandedTanggal = !expandedTanggal }
                ) {
                    OutlinedTextField(
                        value = selectedTanggal,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedTanggal) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1976D2),
                            unfocusedBorderColor = Color(0xFFBDBDBD)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expandedTanggal,
                        onDismissRequest = { expandedTanggal = false }
                    ) {
                        tanggalOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    selectedTanggal = option
                                    expandedTanggal = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Section: Pilih Jadwal
                Text("Pilih Jadwal", color = Color(0xFF1976D2), fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 4.dp))
                ExposedDropdownMenuBox(
                    expanded = expandedJadwal,
                    onExpandedChange = { expandedJadwal = !expandedJadwal }
                ) {
                    OutlinedTextField(
                        value = selectedJadwal,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedJadwal) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1976D2),
                            unfocusedBorderColor = Color(0xFFBDBDBD)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expandedJadwal,
                        onDismissRequest = { expandedJadwal = false }
                    ) {
                        jadwalOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    selectedJadwal = option
                                    expandedJadwal = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Section: Keluhan
                Text("Keluhan", color = Color(0xFF1976D2), fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 4.dp))
                OutlinedTextField(
                    value = keluhan,
                    onValueChange = { keluhan = it },
                    placeholder = { Text("Silakan isi keluhan . . .") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1976D2),
                        unfocusedBorderColor = Color(0xFFBDBDBD)
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Tombol Simpan
                Button(
                    onClick = onSimpan,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Simpan", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
