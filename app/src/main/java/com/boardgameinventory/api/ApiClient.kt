package com.boardgameinventory.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import com.boardgameinventory.BuildConfig

object ApiClient {
    
    private const val BASE_URL = "https://barcodes1.p.rapidapi.com/"
    
    // Use BuildConfig for secure API key storage
    private val RAPIDAPI_KEY = BuildConfig.RAPIDAPI_KEY
    private val RAPIDAPI_HOST = BuildConfig.RAPIDAPI_HOST
    
    // Check if API key is configured
    private fun isApiKeyConfigured(): Boolean {
        return RAPIDAPI_KEY != "your_api_key_here" && RAPIDAPI_KEY.isNotBlank()
    }
    
    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val barcodeService: BarcodeApiService = retrofit.create(BarcodeApiService::class.java)
    
    suspend fun lookupBarcode(barcode: String): ProductInfo? {
        // Check if API key is properly configured
        if (!isApiKeyConfigured()) {
            println("Warning: API key not configured. Barcode lookup will not work.")
            return null
        }
        
        return try {
            val response = barcodeService.lookupBarcode(barcode, RAPIDAPI_KEY, RAPIDAPI_HOST)
            if (response.isSuccessful) {
                val body = response.body()
                body?.product ?: body?.products?.firstOrNull() ?: body?.items?.firstOrNull()
            } else {
                println("API request failed with code: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            println("Error during barcode lookup: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}
