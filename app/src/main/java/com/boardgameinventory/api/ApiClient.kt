package com.boardgameinventory.api

import android.content.Context
import android.util.Log
import com.boardgameinventory.utils.SecureApiKeyManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * API Client for making network requests with secure API key management
 */
object ApiClient {

    private const val BASE_URL = "https://barcodes1.p.rapidapi.com/"
    private const val TAG = "ApiClient"

    private lateinit var secureApiKeyManager: SecureApiKeyManager
    private var barcodeApiService: BarcodeApiService? = null

    /**
     * Initialize with application context
     */
    fun initialize(context: Context) {
        secureApiKeyManager = SecureApiKeyManager.getInstance(context)
        // We'll create the service lazily when needed
    }

    /**
     * Check if API key is configured
     */
    private fun isApiKeyConfigured(): Boolean {
        if (!::secureApiKeyManager.isInitialized) {
            return false
        }
        val apiKey = secureApiKeyManager.getRapidApiKey()
        return apiKey != "your_api_key_here" && apiKey.isNotBlank()
    }

    /**
     * Create OkHttpClient with secure API interceptor
     */
    private fun createHttpClient(context: Context): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(SecureApiInterceptor(context))
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    /**
     * Get or create BarcodeApiService
     */
    private fun getService(context: Context): BarcodeApiService {
        return barcodeApiService ?: synchronized(this) {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(createHttpClient(context))
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service = retrofit.create(BarcodeApiService::class.java)
            barcodeApiService = service
            service
        }
    }

    /**
     * Look up product information by barcode
     */
    suspend fun lookupBarcode(context: Context, barcode: String): ProductInfo? {
        // Check if API key is properly configured
        if (!isApiKeyConfigured()) {
            Log.w(TAG, "Warning: API key not configured. Barcode lookup will not work.")
            return null
        }

        return try {
            val response = getService(context).lookupBarcode(barcode)

            if (response.status == "success") {
                response.product
            } else {
                Log.w(TAG, "API request failed with message: ${response.message}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during barcode lookup: ${e.message}", e)
            null
        }
    }
}
