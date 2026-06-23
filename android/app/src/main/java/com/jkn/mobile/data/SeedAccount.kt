package com.jkn.mobile.data

enum class Role {
    OPERATOR,
    PASIEN
}

data class SeedAccount(
    val email: String,
    val password: String,
    val role: Role,
    val displayName: String
)

object SeedAccounts {
    private val accounts = listOf(
        SeedAccount(
            email = "operator@seed.com",
            password = "operator123",
            role = Role.OPERATOR,
            displayName = "Petugas Faskes"
        ),
        SeedAccount(
            email = "user1@seed.com",
            password = "user123",
            role = Role.PASIEN,
            displayName = "Pasien Satu"
        )
    )

    fun findAccount(email: String, password: String): SeedAccount? {
        return accounts.find {
            it.email.equals(email.trim(), ignoreCase = true) && it.password == password
        }
    }
}
