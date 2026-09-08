package ru.zapasli.app.domain.pantry

import java.time.Instant
import java.time.LocalDate

data class PantryItem(
    val id: String,
    val name: String,
    val barcode: String?,
    val quantity: Double,
    val unit: QuantityUnit,
    val storageLocation: StorageLocation,
    val expiresOn: LocalDate?,
    val nutritionPer100g: NutritionPer100g?,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class NutritionPer100g(
    val caloriesKcal: Double?,
    val proteinGrams: Double?,
    val fatGrams: Double?,
    val carbohydratesGrams: Double?,
)

enum class QuantityUnit {
    PIECE,
    GRAM,
    KILOGRAM,
    MILLILITER,
    LITER,
    PACKAGE,
}

enum class StorageLocation {
    FRIDGE,
    FREEZER,
    PANTRY,
    OTHER,
}
