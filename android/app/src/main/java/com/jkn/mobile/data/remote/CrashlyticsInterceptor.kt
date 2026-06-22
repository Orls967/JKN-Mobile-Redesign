package com.jkn.mobile.data.remote

import com.google.firebase.crashlytics.FirebaseCrashlytics
import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp Interceptor that records transport-level network exceptions to Firebase Crashlytics.
 *
 * Story 1.5 — Maintenance Tool Integration (JMRO-54)
 *
 * Responsibility boundary:
 * - This interceptor handles: SocketTimeoutException, ConnectException, UnknownHostException
 *   and other transport-level exceptions thrown by OkHttp.
 * - QueueRepository handles: HttpException (Retrofit-level, after successful HTTP response)
 *   and application-level exceptions.
 *
 * This separation prevents duplicate exception recording in Crashlytics.
 */
class CrashlyticsInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        return try {
            chain.proceed(chain.request())
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().apply {
                setCustomKey("layer", "okhttp_interceptor")
                setCustomKey("request_url", chain.request().url.toString())
                recordException(e)
            }
            throw e // Re-throw to preserve existing error handling
        }
    }
}
