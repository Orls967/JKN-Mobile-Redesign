package com.jkn.mobile.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

data class FaqItem(val question: String, val answer: String, val views: String)

data class MenuItem(val title: String, val icon: ImageVector, val isNew: Boolean = false)

data class ProfileData(val name: String, val nik: String, val cardNumber: String, val faskes: String, val kelas: String, val status: String, val email: String)

object MockDataProvider {
    val profile = ProfileData(
        name = "ORLANDO SUGIAN",
        nik = "3201012345678901",
        cardNumber = "0002501932781",
        faskes = "Klinik Utama Sehat",
        kelas = "Kelas 1",
        status = "AKTIF",
        email = "orlando.sugian@example.com"
    )

    val faqs = listOf(
        FaqItem("Siapa sajakah yang masuk dalam Anggota Keluarga", "Anggota keluarga yang ditanggung meliputi istri/suami yang sah, dan anak kandung/tiri/angkat yang sah maksimal 3 orang.", "8.51M"),
        FaqItem("Berapa besaran iuran PBI?", "Iuran PBI sepenuhnya dibayarkan oleh Pemerintah.", "5.07M"),
        FaqItem("Berapa besaran iuran Peserta PBPU/ Mandiri/Perseorangan?", "Kelas 1 Rp150.000, Kelas 2 Rp100.000, Kelas 3 Rp35.000.", "3.65M"),
        FaqItem("Cara Perubahan Data Melalui Aplikasi Mobile JKN?", "Buka menu Perubahan Data Peserta, pilih data yang ingin diubah, dan ikuti instruksi.", "3.36M"),
        FaqItem("Bagaimana Jika Kartu Peserta Hilang", "Gunakan Kartu KIS Digital di aplikasi Mobile JKN, fungsinya sama dengan kartu fisik.", "2.66M"),
        FaqItem("Siapa saja anggota keluarga yang ditanggung oleh Pekerja Penerima Upah?", "Suami/Istri sah dan maksimal 3 orang anak.", "2.51M"),
        FaqItem("Aplikasi Mobile JKN", "Aplikasi Mobile JKN adalah layanan mandiri untuk peserta JKN-KIS.", "2.49M"),
        FaqItem("Apa Hak Peserta", "Mendapatkan pelayanan kesehatan sesuai indikasi medis di fasilitas kesehatan.", "1.7M")
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
        Pair("Bugar", Icons.Default.ThumbUp),
        Pair("Bantuan & FAQ", Icons.Default.Info)
    )
}
