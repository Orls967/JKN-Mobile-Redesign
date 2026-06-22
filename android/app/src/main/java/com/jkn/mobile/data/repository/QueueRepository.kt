package com.jkn.mobile.data.repository

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.jkn.mobile.data.model.QueueResponse
import com.jkn.mobile.data.remote.RetrofitClient
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Repository for queue-related network operations.
 *
 * Story 1.5 — Crashlytics responsibility boundary:
 * - Transport-level exceptions (SocketTimeout, Connect, UnknownHost) are recorded
 *   by CrashlyticsInterceptor at the OkHttp layer. NOT recorded here to avoid duplicates.
 * - HttpException (Retrofit-level) is recorded HERE because it occurs after a successful
 *   HTTP response — CrashlyticsInterceptor never sees it.
 * - Generic Exception is recorded HERE only if it is NOT a transport-level exception.
 */
class QueueRepository {

    private val apiService = RetrofitClient.apiService
    private val crashlytics = FirebaseCrashlytics.getInstance()

    suspend fun getQueueById(id: Long): Result<QueueResponse> {
        return try {
            val response = apiService.getQueueById(id)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: SocketTimeoutException) {
            // Recorded by CrashlyticsInterceptor — no duplicate recording
            Result.failure(e)
        } catch (e: ConnectException) {
            // Recorded by CrashlyticsInterceptor — no duplicate recording
            Result.failure(e)
        } catch (e: UnknownHostException) {
            // Recorded by CrashlyticsInterceptor — no duplicate recording
            Result.failure(e)
        } catch (e: HttpException) {
            // HttpException is NOT caught by CrashlyticsInterceptor — record here
            crashlytics.apply {
                setCustomKey("layer", "repository")
                setCustomKey("http_status_code", e.code())
                e.response()?.raw()?.request?.url?.toString()?.let { url ->
                    setCustomKey("request_url", url)
                }
                recordException(e)
            }
            Result.failure(e)
        } catch (e: Exception) {
            // Record only non-transport exceptions to avoid duplicates
            if (e !is SocketTimeoutException && e !is ConnectException && e !is UnknownHostException) {
                crashlytics.apply {
                    setCustomKey("layer", "repository")
                    recordException(e)
                }
            }
            Result.failure(e)
        }
    }

    suspend fun nextQueue(id: Long): Result<QueueResponse> {
        return try {
            val response = apiService.nextQueue(id)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: SocketTimeoutException) {
            // Recorded by CrashlyticsInterceptor — no duplicate recording
            Result.failure(e)
        } catch (e: ConnectException) {
            // Recorded by CrashlyticsInterceptor — no duplicate recording
            Result.failure(e)
        } catch (e: UnknownHostException) {
            // Recorded by CrashlyticsInterceptor — no duplicate recording
            Result.failure(e)
        } catch (e: HttpException) {
            // HttpException is NOT caught by CrashlyticsInterceptor — record here
            crashlytics.apply {
                setCustomKey("layer", "repository")
                setCustomKey("http_status_code", e.code())
                e.response()?.raw()?.request?.url?.toString()?.let { url ->
                    setCustomKey("request_url", url)
                }
                recordException(e)
            }
            Result.failure(e)
        } catch (e: Exception) {
            // Record only non-transport exceptions to avoid duplicates
            if (e !is SocketTimeoutException && e !is ConnectException && e !is UnknownHostException) {
                crashlytics.apply {
                    setCustomKey("layer", "repository")
                    recordException(e)
                }
            }
            Result.failure(e)
        }
    }
}
