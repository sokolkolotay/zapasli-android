package ru.zapasli.app.data.pantry

import ru.zapasli.app.core.database.PantryItemEntity
import ru.zapasli.app.domain.pantry.NutritionPer100g
import ru.zapasli.app.domain.pantry.PantryItem
import ru.zapasli.app.domain.pantry.QuantityUnit
import ru.zapasli.app.domain.pantry.StorageLocation
import java.time.Instant
import java.time.LocalDate

internal fun PantryItemEntity.toDomain(): PantryItem =
    PantryItem(
        id = id,
        name = name,
        barcode = barcode,
        quantity = quantity,
        unit = enumValueOrDefault(unit, QuantityUnit.PIECE),
        storageLocation = enumValueOrDefault(storageLocation, StorageLocation.OTHER),
        expiresOn = expiresAtEpochDay?.let(LocalDate::ofEpochDay),
        nutritionPer100g = nutritionOrNull(),
        createdAt = Instant.ofEpochMilli(createdAtEpochMillis),
        updatedAt = Instant.ofEpochMilli(updatedAtEpochMillis),
    )

internal fun PantryItem.toEntity(): PantryItemEntity =
    PantryItemEntity(
        id = id,
        name = name,
        barcode = barcode,
        quantity = quantity,
        unit = unit.name,
        storageLocation = storageLocation.name,
        expiresAtEpochDay = expiresOn?.toEpochDay(),
        caloriesPer100g = nutritionPer100g?.caloriesKcal,
        proteinPer100g = nutritionPer100g?.proteinGrams,
        fatPer100g = nutritionPer100g?.fatGrams,
        carbohydratesPer100g = nutritionPer100g?.carbohydratesGrams,
        createdAtEpochMillis = createdAt.toEpochMilli(),
        updatedAtEpochMillis = updatedAt.toEpochMilli(),
    )

private fun PantryItemEntity.nutritionOrNull(): NutritionPer100g? {
    if (
        caloriesPer100g == null &&
        proteinPer100g == null &&
        fatPer100g == null &&
        carbohydratesPer100g == null
    ) {
        return null
    }

    return NutritionPer100g(
        caloriesKcal = caloriesPer100g,
        proteinGrams = proteinPer100g,
        fatGrams = fatPer100g,
        carbohydratesGrams = carbohydratesPer100g,
    )
}

private inline fun <reified T : Enum<T>> enumValueOrDefault(
    value: String,
    default: T,
): T = enumValues<T>().firstOrNull { it.name == value } ?: default
