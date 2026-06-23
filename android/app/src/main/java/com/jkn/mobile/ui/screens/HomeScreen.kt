package com.jkn.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.jkn.mobile.BuildConfig
import com.jkn.mobile.R
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image

data class HomeMenuItem(val iconRes: Int, val label: String, val badge: String? = null)

val menuItems = listOf(
    HomeMenuItem(R.drawable.ic_menu_program_jkn, "Info Program JKN"),
    HomeMenuItem(R.drawable.ic_menu_telehealth, "TELEHEALTH"),
    HomeMenuItem(R.drawable.ic_menu_riwayat, "Info Riwayat Pelayanan"),
    HomeMenuItem(R.drawable.ic_menu_bugar, "Bugar", badge = "Baru"),
    HomeMenuItem(R.drawable.ic_menu_rehab, "NEW Rehab\n(Cicilan)", badge = "Baru"),
    HomeMenuItem(R.drawable.ic_menu_tambah_peserta, "Penambahan Peserta"),
    HomeMenuItem(R.drawable.ic_menu_info_peserta, "Info Peserta"),
    HomeMenuItem(R.drawable.ic_menu_sos, "SOS"),
    HomeMenuItem(R.drawable.ic_menu_lokasi_faskes, "Info Lokasi Faskes"),
    HomeMenuItem(R.drawable.ic_menu_perubahan_data, "Perubahan Data Peserta"),
    HomeMenuItem(R.drawable.ic_menu_pengaduan, "Pengaduan Layanan JKN"),
    HomeMenuItem(R.drawable.ic_menu_lainnya, "Menu Lainnya"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onNavigateToQueue: () -> Unit) {
    var showAntreanPicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .imePadding()
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF6B4EFF), // ungu
                            Color(0xFF4A90D9), // biru
                            Color(0xFFE91E8C), // pink-magenta
                            Color(0xFFFF6B35)  // oranye
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_jkn_logo),
                        contentDescription = "Logo JKN",
                        modifier = Modifier.height(32.dp),
                        contentScale = ContentScale.Fit
                    )
                    AsyncImage(
                        model = "https://ui-avatars.com/api/?name=Orlando+Sugian&background=random",
                        contentDescription = "Foto Profil",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color.White, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Greeting
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Hi, ORLANDO",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        painter = painterResource(R.drawable.ic_verified_check),
                        contentDescription = "Terverifikasi",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = "Semua Keluarga Anda Terlindungi (Aktif)",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Text(
                text = "v${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Antrean Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Illustration
                Image(
                    painter = painterResource(R.drawable.ic_antrean_illustration),
                    contentDescription = null,
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Antrean Online",
                        color = Color(0xFF1565C0),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Untuk kunjungan lebih efisien tanpa harus menunggu lama.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { showAntreanPicker = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                        modifier = Modifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                    ) {
                        Text("Ambil Antrean", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Menu Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            contentPadding = PaddingValues(horizontal = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            userScrollEnabled = false
        ) {
            items(menuItems) { menu ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Box(contentAlignment = Alignment.TopEnd) {
                        Image(
                            painter = painterResource(menu.iconRes),
                            contentDescription = menu.label,
                            modifier = Modifier.size(50.dp),
                            contentScale = ContentScale.Fit
                        )
                        if (menu.badge != null) {
                            Box(
                                modifier = Modifier
                                    .offset(x = 8.dp, y = (-4).dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Red)
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(menu.badge, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = menu.label,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        lineHeight = 12.sp
                    )
                }
            }
        }

        // Banner section
        Text(
            "JANJI LAYANAN JAMINAN KESEHATAN NASIONAL (JKN)",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1565C0)
        )
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(3) {
                Card(
                    modifier = Modifier.width(280.dp).height(140.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Banner Informasi JKN", color = Color(0xFF1976D2))
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(100.dp))
    }

    if (showAntreanPicker) {
        ModalBottomSheet(
            onDismissRequest = { showAntreanPicker = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            AntreanTypePicker(
                onSelectFaskesPertama = {
                    showAntreanPicker = false
                    onNavigateToQueue() // We will pass the right route from NavGraph
                },
                onSelectFaskesRujukan = {
                    showAntreanPicker = false
                },
                onSelectKantorCabang = {
                    showAntreanPicker = false
                }
            )
        }
    }
}

@Composable
fun AntreanTypePicker(
    onSelectFaskesPertama: () -> Unit,
    onSelectFaskesRujukan: () -> Unit,
    onSelectKantorCabang: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        Text(
            "Pilih Jenis Antrean",
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1976D2)
        )

        data class AntreanType(val icon: ImageVector, val label: String, val onClick: () -> Unit)
        val types = listOf(
            AntreanType(Icons.Default.HealthAndSafety, "Faskes Tingkat Pertama", onSelectFaskesPertama),
            AntreanType(Icons.Default.History, "Faskes Rujukan Tingkat Lanjut", onSelectFaskesRujukan),
            AntreanType(Icons.Default.Info, "Kantor Cabang", onSelectKantorCabang),
        )

        types.forEach { type ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clickable(onClick = type.onClick),
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = type.icon,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = Color(0xFF1565C0)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        type.label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                }
            }
        }
    }
}
