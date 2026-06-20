package com.jkn.mobile.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

data class FaqItem(val question: String, val views: String)

data class MenuItem(val title: String, val icon: ImageVector, val isNew: Boolean = false)

data class ProfileData(val name: String, val nik: String, val cardNumber: String)

object MockDataProvider {
    val profile = ProfileData(
        name = "ORLANDO SUGIAN",
        nik = "3201012345678901",
        cardNumber = "0002501932781"
    )

    val faqs = listOf(
        FaqItem("Siapa sajakah yang masuk dalam Anggota Keluarga", "8.51M"),
        FaqItem("Berapa besaran iuran PBI?", "5.07M"),
        FaqItem("Berapa besaran iuran Peserta PBPU/ Mandiri/Perseorangan?", "3.65M"),
        FaqItem("Cara Perubahan Data Melalui Aplikasi Mobile JKN?", "3.36M"),
        FaqItem("Bagaimana Jika Kartu Peserta Hilang", "2.66M"),
        FaqItem("Siapa saja anggota keluarga yang ditanggung oleh Pekerja Penerima Upah?", "2.51M"),
        FaqItem("Aplikasi Mobile JKN", "2.49M"),
        FaqItem("Apa Hak Peserta", "1.7M")
    )

    val homeMenus = listOf(
        MenuItem("Info Program JKN", Icons.Default.Info),
        MenuItem("TELEHEALTH", Icons.Default.Call),
        MenuItem("Info Riwayat Pelayanan", Icons.Default.Favorite),
        MenuItem("Bugar", Icons.Default.ThumbUp, isNew = true),
        MenuItem("NEW Rehab (Cicilan)", Icons.Default.Build, isNew = true),
        MenuItem("Penambahan Peserta", Icons.Default.Person),
        MenuItem("Info Peserta", Icons.Default.AccountBox),
        MenuItem("SOS", Icons.Default.Warning),
        MenuItem("Info Lokasi Faskes", Icons.Default.LocationOn),
        MenuItem("Perubahan Data Peserta", Icons.Default.Edit),
        MenuItem("Pengaduan Layanan JKN", Icons.Default.Email),
        MenuItem("Menu Lainnya", Icons.Default.MoreVert)
    )

    val profileMenus = listOf(
        Pair("Kalkulator Kesehatan", Icons.Default.ShoppingCart), // Placeholder icons
        Pair("Ubah PIN", Icons.Default.Lock),
        Pair("Ubah Kata Sandi", Icons.Default.MoreVert),
        Pair("Keamanan dan Privasi", Icons.Default.Info),
        Pair("Bugar", Icons.Default.ThumbUp)
    )
}
