package com.jkn.mobile.data.remote

import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.atomic.AtomicReference

/**
 * OkHttp Interceptor untuk mengelola X-Request-ID header (TASK-07-A Android Impact).
 *
 * Fungsi:
 * 1. Membaca X-Request-ID dari setiap response dan menyimpannya.
 * 2. Pada retry request, menyertakan X-Request-ID terakhir di header outgoing request
 *    untuk memudahkan debugging end-to-end (correlation antara log client & server).
 */
class RequestIdInterceptor : Interceptor {

    companion object {
        private val lastRequestId = AtomicReference<String?>(null)

        /**
         * Mendapatkan X-Request-ID terakhir yang diterima dari server.
         * Dapat digunakan oleh layer lain untuk logging atau debugging.
         */
        fun getLastRequestId(): String? = lastRequestId.get()
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        // Jika ada X-Request-ID dari response sebelumnya, sertakan di header
        val previousRequestId = lastRequestId.get()
        if (previousRequestId != null) {
            request = request.newBuilder()
                .header("X-Request-ID", previousRequestId)
                .build()
        }

        val response = chain.proceed(request)

        // Simpan X-Request-ID dari response untuk correlation berikutnya
        val responseRequestId = response.header("X-Request-ID")
        if (responseRequestId != null) {
            lastRequestId.set(responseRequestId)
        }

        return response
    }
}
