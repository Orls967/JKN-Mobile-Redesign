package com.jkn.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

data class Artikel(
    val id: String,
    val judul: String,
    val thumbnailUrl: String,
    val views: String,
    val tanggal: String
)

val mockArticles = listOf(
    Artikel("1", "BPJS Kesehatan Cabang Banjar Dorong Hidup Sehat Lewat Prolanis", "https://picsum.photos/seed/jkn1/200/200", "404", "15-06-2026"),
    Artikel("2", "Prolanis JKN Bantu Yayah Kelola Hipertensi Lebih Baik", "https://picsum.photos/seed/jkn2/200/200", "476", "18-06-2026"),
    Artikel("3", "Dengan JKN, Kisti Rasakan Pelayanan Kesehatan Mudah dan Cepat", "https://picsum.photos/seed/jkn3/200/200", "366", "18-06-2026"),
    Artikel("4", "Pentingnya Menjaga Pola Makan di Masa Tua", "https://picsum.photos/seed/jkn4/200/200", "1.2K", "20-06-2026")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeritaScreen() {
    var searchQuery by remember { mutableStateOf("") }
    val categories = listOf("Rekomendasi", "Berita Utama", "Testimoni", "Tips Sehat", "Gaya Hidup")
    var selectedCategory by remember { mutableStateOf("Rekomendasi") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding()
            .imePadding()
    ) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Cari berita, testimoni, tips sehat atau gaya ...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF1976D2)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(24.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF1976D2),
                unfocusedBorderColor = Color(0xFFE0E0E0)
            )
        )

        // Section "Artikel"
        Text(
            "Artikel",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        // Category Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(categories) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = { Text(category) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF1976D2),
                        selectedLabelColor = Color.White,
                        containerColor = Color.Transparent,
                        labelColor = Color(0xFF1976D2)
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedCategory == category,
                        borderColor = Color(0xFF1976D2),
                        selectedBorderColor = Color(0xFF1976D2)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Section "Berita Lainnya"
        Text(
            "Berita Lainnya",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        // Article List
        LazyColumn {
            items(mockArticles) { artikel ->
                ArticleCard(artikel = artikel)
                HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
            }
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun ArticleCard(artikel: Artikel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clickable { /* navigate to detail */ },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail kiri
        AsyncImage(
            model = artikel.thumbnailUrl,
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            // Judul artikel (max 2 baris)
            Text(
                text = artikel.judul,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            // Views + Tanggal
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.width(2.dp))
                Text(artikel.views, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Spacer(modifier = Modifier.width(12.dp))
                Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.width(2.dp))
                Text(artikel.tanggal, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
    }
}
