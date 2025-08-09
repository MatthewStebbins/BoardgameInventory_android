package com.boardgameinventory.api

import android.content.Context
import com.boardgameinventory.BuildConfig
import com.boardgameinventory.utils.SecureApiKeyManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp interceptor that adds secure API keys to outgoing API requests
 */
class SecureApiInterceptor(private val context: Context) : Interceptor {

    private val apiKeyManager by lazy {
        SecureApiKeyManager.getInstance(context)
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Only add API keys for RapidAPI requests
        if (originalRequest.url.host.contains("rapidapi.com")) {
            val requestBuilder = originalRequest.newBuilder()

            // Add RapidAPI key headers from our secure storage
            val apiKey = apiKeyManager.getRapidApiKey()
            val apiHost = apiKeyManager.getRapidApiHost()

            if (apiKey.isNotBlank() && apiHost.isNotBlank()) {
                requestBuilder.addHeader("X-RapidAPI-Key", apiKey)
                requestBuilder.addHeader("X-RapidAPI-Host", apiHost)
            }

            // Debug logging for API requests (disabled in release)
            if (BuildConfig.DEBUG) {
                android.util.Log.d(
                    "SecureApiInterceptor",
                    "Request URL: ${originalRequest.url}"
                )
                android.util.Log.d(
                    "SecureApiInterceptor",
                    "Headers: X-RapidAPI-Key=${apiKey}, X-RapidAPI-Host=${apiHost}"
                )
            }

            return chain.proceed(requestBuilder.build())
        }

        // For other requests, proceed without modification
        return chain.proceed(originalRequest)
    }
}
