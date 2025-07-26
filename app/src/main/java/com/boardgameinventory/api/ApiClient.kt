package com.boardgameinventory.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object ApiClient {
    
    private const val BASE_URL = "https://barcodes1.p.rapidapi.com/"
    private const val RAPIDAPI_KEY = "564a5396d3msh94154227da876d1p173b65jsn193403cb56f0"
    private const val RAPIDAPI_HOST = "barcodes1.p.rapidapi.com"
    
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
        return try {
            val response = barcodeService.lookupBarcode(barcode, RAPIDAPI_KEY, RAPIDAPI_HOST)
            if (response.isSuccessful) {
                val body = response.body()
                body?.product ?: body?.products?.firstOrNull() ?: body?.items?.firstOrNull()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
