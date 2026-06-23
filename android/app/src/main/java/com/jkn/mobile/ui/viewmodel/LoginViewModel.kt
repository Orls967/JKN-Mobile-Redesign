package com.jkn.mobile.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.jkn.mobile.data.Role
import com.jkn.mobile.data.SeedAccounts

class LoginViewModel : ViewModel() {
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var errorMessage by mutableStateOf<String?>(null)

    fun login(onSuccess: (Role) -> Unit) {
        val account = SeedAccounts.findAccount(email, password)
        if (account != null) {
            errorMessage = null
            onSuccess(account.role)
        } else {
            errorMessage = "Email atau password tidak ditemukan"
        }
    }
}
