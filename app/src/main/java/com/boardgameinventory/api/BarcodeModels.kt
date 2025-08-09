package com.boardgameinventory.api

import com.google.gson.annotations.SerializedName

data class BarcodeResponse(
    @SerializedName("product")
    val product: ProductInfo?,
    @SerializedName("products")
    val products: List<ProductInfo>?,
    @SerializedName("items")
    val items: List<ProductInfo>?,
    @SerializedName("status")
    val status: String = "",
    @SerializedName("message")
    val message: String? = null
)

data class SearchResponse(
    @SerializedName("products")
    val products: List<ProductInfo>?,
    @SerializedName("status")
    val status: String = "",
    @SerializedName("message")
    val message: String? = null
)

data class ProductInfo(
    @SerializedName("barcode")
    val barcode: String?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("product_name")
    val productName: String?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("desc")
    val desc: String?,
    @SerializedName("features")
    val features: List<String>?,
    @SerializedName("category")
    val category: List<String>?,
    @SerializedName("manufacturer")
    val manufacturer: String?,
    @SerializedName("brand")
    val brand: String?,
    @SerializedName("images")
    val images: List<String>?,
    @SerializedName("image_urls")
    val imageUrls: List<String>?,
    @SerializedName("image")
    val image: String?
) {
    /**
     * Get the most appropriate title from available fields
     */
    fun getDisplayTitle(): String? {
        return title ?: name ?: productName
    }

    /**
     * Get the most appropriate description from available fields
     */
    fun getDisplayDescription(): String? {
        return description ?: desc ?: features?.joinToString("\n")
    }

    /**
     * Get the most appropriate image URL from available fields
     */
    fun getDisplayImage(): String? {
        return images?.firstOrNull() ?: imageUrls?.firstOrNull() ?: image
    }
}
