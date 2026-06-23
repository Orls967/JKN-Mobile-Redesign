package com.jkn.mobile.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jkn.mobile.data.MockDataProvider
import com.jkn.mobile.R
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image

enum class ItemType { NAVIGATION, TOGGLE }

data class SettingsItem(
    val icon: ImageVector,
    val label: String,
    val type: ItemType = ItemType.NAVIGATION,
    val iconTint: Color = Color(0xFF1976D2)
)

val settingsItems = listOf(
    SettingsItem(Icons.Default.Calculate, "Kalkulator Kesehatan"),
    SettingsItem(Icons.Default.Pin, "Ubah PIN"),
    SettingsItem(Icons.Default.Password, "Ubah Kata Sandi"),
    SettingsItem(Icons.Default.Security, "Keamanan dan Privasi"),
    SettingsItem(Icons.Default.FitnessCenter, "Bugar"),
    SettingsItem(Icons.Default.Accessibility, "Fitur aksesibilitas", ItemType.TOGGLE),
    SettingsItem(Icons.Default.Fingerprint, "Login dengan Biometrik", ItemType.TOGGLE),
    SettingsItem(Icons.Default.Notifications, "Notifikasi", ItemType.TOGGLE),
    SettingsItem(Icons.Default.MenuBook, "Panduan"),
    SettingsItem(Icons.Default.Logout, "Keluar", iconTint = Color.Red),
)

@Composable
fun ProfileScreen(
    onNavigateToFaq: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val profile = MockDataProvider.profile

    var aksesibilitasEnabled by remember { mutableStateOf(false) }
    var biometrikEnabled by remember { mutableStateOf(false) }
    var notifikasiEnabled by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .systemBarsPadding()
    ) {
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header dengan gradient aurora
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF6B4EFF),
                                    Color(0xFF4A90D9),
                                    Color(0xFFE91E8C),
                                    Color(0xFFFF6B35)
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                            )
                        )
                )

                // Card Profil (Avatar + Nama + Kode Referral)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .offset(y = (-40).dp), // overlap dengan header
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar ilustrasi
                        Image(
                            painter = painterResource(R.drawable.ic_avatar_male),
                            contentDescription = "Avatar",
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = profile.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { /* show kode referral */ },
                            border = BorderStroke(1.dp, Color(0xFF1976D2)),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.height(36.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 0.dp)
                        ) {
                            Text("Kode Referral", color = Color(0xFF1976D2), fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column {
                    settingsItems.forEachIndexed { index, item ->
                        val checked = when (item.label) {
                            "Fitur aksesibilitas" -> aksesibilitasEnabled
                            "Login dengan Biometrik" -> biometrikEnabled
                            "Notifikasi" -> notifikasiEnabled
                            else -> false
                        }
                        val onCheckedChange = { isChecked: Boolean ->
                            when (item.label) {
                                "Fitur aksesibilitas" -> aksesibilitasEnabled = isChecked
                                "Login dengan Biometrik" -> biometrikEnabled = isChecked
                                "Notifikasi" -> notifikasiEnabled = isChecked
                            }
                        }

                        SettingsListItem(
                            item = item,
                            checked = checked,
                            onCheckedChange = onCheckedChange,
                            onClick = {
                                if (item.label == "Panduan") {
                                    onNavigateToFaq()
                                } else if (item.label == "Keluar") {
                                    onLogout()
                                }
                            }
                        )
                        if (index < settingsItems.size - 1) {
                            HorizontalDivider(color = Color(0xFFF5F5F5))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun SettingsListItem(
    item: SettingsItem,
    checked: Boolean = false,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = item.iconTint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = item.label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
            color = if (item.label == "Keluar") Color.Red else Color.Black
        )
        when (item.type) {
            ItemType.TOGGLE -> Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF1976D2),
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFBDBDBD)
                )
            )
            ItemType.NAVIGATION -> Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}
