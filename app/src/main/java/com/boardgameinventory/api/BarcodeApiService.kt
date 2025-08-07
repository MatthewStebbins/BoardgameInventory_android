package com.boardgameinventory.api

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Interface for barcode API service
 */
interface BarcodeApiService {
    @GET("barcode/{barcode}")
    suspend fun lookupBarcode(
        @Path("barcode") barcode: String
    ): BarcodeResponse

    @GET("search")
    suspend fun searchProducts(
        @Query("query") query: String,
        @Query("limit") limit: Int = 10
    ): SearchResponse
}
