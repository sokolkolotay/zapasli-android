package ru.zapasli.app.data.pantry

import org.junit.Assert.assertEquals
import org.junit.Test
import ru.zapasli.app.core.database.PantryItemEntity
import ru.zapasli.app.domain.pantry.NutritionPer100g
import ru.zapasli.app.domain.pantry.PantryItem
import ru.zapasli.app.domain.pantry.QuantityUnit
import ru.zapasli.app.domain.pantry.StorageLocation
import java.time.Instant
import java.time.LocalDate

class PantryItemMapperTest {
    @Test
    fun `domain item survives database round trip`() {
        val item = PantryItem(
            id = "item-1",
            name = "Молоко",
            barcode = "4600000000001",
            quantity = 900.0,
            unit = QuantityUnit.MILLILITER,
            storageLocation = StorageLocation.FRIDGE,
            expiresOn = LocalDate.of(2026, 9, 12),
            nutritionPer100g = NutritionPer100g(
                caloriesKcal = 60.0,
                proteinGrams = 3.0,
                fatGrams = 3.2,
                carbohydratesGrams = 4.7,
            ),
            createdAt = Instant.parse("2026-09-08T10:00:00Z"),
            updatedAt = Instant.parse("2026-09-08T11:00:00Z"),
        )

        assertEquals(item, item.toEntity().toDomain())
    }

    @Test
    fun `unknown stored enum values use safe fallbacks`() {
        val entity = PantryItemEntity(
            id = "legacy-item",
            name = "Продукт",
            barcode = null,
            quantity = 1.0,
            unit = "UNKNOWN_UNIT",
            storageLocation = "UNKNOWN_LOCATION",
            expiresAtEpochDay = null,
            caloriesPer100g = null,
            proteinPer100g = null,
            fatPer100g = null,
            carbohydratesPer100g = null,
            createdAtEpochMillis = 0,
            updatedAtEpochMillis = 0,
        )

        val item = entity.toDomain()

        assertEquals(QuantityUnit.PIECE, item.unit)
        assertEquals(StorageLocation.OTHER, item.storageLocation)
        assertEquals(null, item.nutritionPer100g)
    }
}
