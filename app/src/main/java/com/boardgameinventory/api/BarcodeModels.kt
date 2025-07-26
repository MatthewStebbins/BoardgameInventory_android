package com.boardgameinventory.api

import com.google.gson.annotations.SerializedName

data class BarcodeResponse(
    @SerializedName("product")
    val product: ProductInfo?,
    @SerializedName("products")
    val products: List<ProductInfo>?,
    @SerializedName("items")
    val items: List<ProductInfo>?
)

data class ProductInfo(
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
    @SerializedName("images")
    val images: List<String>?,
    @SerializedName("image_urls")
    val imageUrls: List<String>?,
    @SerializedName("image")
    val image: String?
) {
    fun getDisplayTitle(): String? {
        return title ?: name ?: productName
    }
    
    fun getDisplayDescription(): String? {
        return description ?: desc ?: features?.joinToString("\n")
    }
    
    fun getDisplayImage(): String? {
        return images?.firstOrNull() ?: imageUrls?.firstOrNull() ?: image
    }
}
