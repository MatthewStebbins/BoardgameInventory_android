package com.boardgameinventory.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface BarcodeApiService {
    
    @GET("/")
    suspend fun lookupBarcode(
        @Query("query") barcode: String,
        @Header("X-RapidAPI-Key") apiKey: String,
        @Header("X-RapidAPI-Host") host: String
    ): Response<BarcodeResponse>
}
