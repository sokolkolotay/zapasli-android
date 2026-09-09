package ru.zapasli.app.domain.catalog

import ru.zapasli.app.domain.pantry.NutritionPer100g

interface ProductCatalogRepository {
    suspend fun findByBarcode(barcode: String): ProductCatalogResult
}

sealed interface ProductCatalogResult {
    data class Found(val product: ProductSuggestion) : ProductCatalogResult
    data object NotFound : ProductCatalogResult
    data object Unavailable : ProductCatalogResult
}

data class ProductSuggestion(
    val barcode: String,
    val name: String?,
    val nutritionPer100g: NutritionPer100g?,
)
