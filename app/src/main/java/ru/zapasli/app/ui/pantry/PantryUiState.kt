package ru.zapasli.app.ui.pantry

import ru.zapasli.app.core.model.ExpiryState
import ru.zapasli.app.core.model.expiryState
import ru.zapasli.app.domain.catalog.ProductSuggestion
import ru.zapasli.app.domain.pantry.NutritionPer100g
import ru.zapasli.app.domain.pantry.PantryItem
import ru.zapasli.app.domain.pantry.QuantityUnit
import ru.zapasli.app.domain.pantry.StorageLocation
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class PantryUiState(
    val isLoading: Boolean = true,
    val items: List<PantryItem> = emptyList(),
    val allItems: List<PantryItem> = emptyList(),
    val selectedFilter: PantryFilter = PantryFilter.ALL,
    val searchQuery: String = "",
    val totalItemCount: Int = 0,
    val attentionItemCount: Int = 0,
    val loadFailed: Boolean = false,
    val message: PantryMessage? = null,
    val productLookup: ProductLookupUiState = ProductLookupUiState.Idle,
)

sealed interface ProductLookupUiState {
    data object Idle : ProductLookupUiState
    data class Loading(val barcode: String) : ProductLookupUiState
    data class Found(val product: ProductSuggestion) : ProductLookupUiState
    data class NotFound(val barcode: String) : ProductLookupUiState
    data class Failed(val barcode: String) : ProductLookupUiState
}

enum class PantryFilter {
    ALL,
    EXPIRING_SOON,
    EXPIRED,
    NO_DATE,
}

sealed interface PantryMessage {
    data object ItemAdded : PantryMessage
    data object ItemUpdated : PantryMessage
    data object ItemDeleted : PantryMessage
    data object SaveFailed : PantryMessage
    data object DeleteFailed : PantryMessage
    data class ValidationFailed(val error: PantryValidationError) : PantryMessage
}

enum class PantryValidationError {
    EMPTY_NAME,
    INVALID_QUANTITY,
    INVALID_BARCODE,
    INVALID_NUTRITION,
}

data class PantryItemInput(
    val id: String?,
    val name: String,
    val barcode: String?,
    val quantity: Double,
    val unit: QuantityUnit,
    val storageLocation: StorageLocation,
    val expiresOn: LocalDate?,
    val nutritionPer100g: NutritionPer100g?,
)

internal fun validatePantryItemInput(input: PantryItemInput): PantryValidationError? = when {
    input.name.isBlank() -> PantryValidationError.EMPTY_NAME
    !input.quantity.isFinite() || input.quantity <= 0 -> PantryValidationError.INVALID_QUANTITY
    !input.barcode.isNullOrBlank() && (
        input.barcode.any { !it.isDigit() } || input.barcode.length !in 8..14
    ) -> PantryValidationError.INVALID_BARCODE
    input.nutritionPer100g?.values()?.any { !it.isFinite() || it < 0 } == true ->
        PantryValidationError.INVALID_NUTRITION
    else -> null
}

internal fun filterPantryItems(
    items: List<PantryItem>,
    filter: PantryFilter,
    today: LocalDate,
): List<PantryItem> = items.filter { item ->
    val state = item.expiryState(today)
    when (filter) {
        PantryFilter.ALL -> true
        PantryFilter.EXPIRING_SOON -> state == ExpiryState.Today || state == ExpiryState.Soon
        PantryFilter.EXPIRED -> state == ExpiryState.Expired
        PantryFilter.NO_DATE -> state == ExpiryState.NoDate
    }
}

internal fun searchPantryItems(
    items: List<PantryItem>,
    query: String,
): List<PantryItem> {
    val normalizedQuery = query.trim()
    if (normalizedQuery.isEmpty()) return items
    return items.filter { item ->
        item.name.contains(normalizedQuery, ignoreCase = true) ||
            item.barcode?.contains(normalizedQuery) == true
    }
}

internal fun List<PantryItem>.attentionCount(today: LocalDate): Int = count { item ->
    when (item.expiryState(today)) {
        ExpiryState.Expired,
        ExpiryState.Today,
        ExpiryState.Soon,
        -> true
        ExpiryState.Fresh,
        ExpiryState.NoDate,
        -> false
    }
}

internal fun PantryItem.expiryState(today: LocalDate): ExpiryState {
    val daysUntilExpiry = expiresOn?.let { expiryDate ->
        ChronoUnit.DAYS.between(today, expiryDate).toInt()
    }
    return expiryState(daysUntilExpiry)
}

private fun NutritionPer100g.values(): List<Double> = listOfNotNull(
    caloriesKcal,
    proteinGrams,
    fatGrams,
    carbohydratesGrams,
)
