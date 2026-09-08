package ru.zapasli.app.ui.pantry

import org.junit.Assert.assertEquals
import org.junit.Test
import ru.zapasli.app.domain.pantry.NutritionPer100g
import ru.zapasli.app.domain.pantry.PantryItem
import ru.zapasli.app.domain.pantry.QuantityUnit
import ru.zapasli.app.domain.pantry.StorageLocation
import java.time.Instant
import java.time.LocalDate

class PantryUiStateTest {
    private val today = LocalDate.of(2026, 9, 8)

    @Test
    fun `expiring soon filter includes today and next three days`() {
        val items = listOf(
            item("expired", today.minusDays(1)),
            item("today", today),
            item("soon", today.plusDays(3)),
            item("fresh", today.plusDays(4)),
            item("no-date", null),
        )

        val result = filterPantryItems(items, PantryFilter.EXPIRING_SOON, today)

        assertEquals(listOf("today", "soon"), result.map(PantryItem::id))
    }

    @Test
    fun `attention count includes expired today and soon`() {
        val items = listOf(
            item("expired", today.minusDays(1)),
            item("today", today),
            item("soon", today.plusDays(2)),
            item("fresh", today.plusDays(10)),
            item("no-date", null),
        )

        assertEquals(3, items.attentionCount(today))
    }

    @Test
    fun `validation rejects malformed values`() {
        assertEquals(
            PantryValidationError.EMPTY_NAME,
            validatePantryItemInput(validInput().copy(name = "  ")),
        )
        assertEquals(
            PantryValidationError.INVALID_QUANTITY,
            validatePantryItemInput(validInput().copy(quantity = 0.0)),
        )
        assertEquals(
            PantryValidationError.INVALID_BARCODE,
            validatePantryItemInput(validInput().copy(barcode = "ABC")),
        )
        assertEquals(
            PantryValidationError.INVALID_NUTRITION,
            validatePantryItemInput(
                validInput().copy(
                    nutritionPer100g = NutritionPer100g(-1.0, null, null, null),
                ),
            ),
        )
    }

    @Test
    fun `valid input has no validation error`() {
        assertEquals(null, validatePantryItemInput(validInput()))
    }

    private fun item(id: String, expiry: LocalDate?) = PantryItem(
        id = id,
        name = id,
        barcode = null,
        quantity = 1.0,
        unit = QuantityUnit.PIECE,
        storageLocation = StorageLocation.FRIDGE,
        expiresOn = expiry,
        nutritionPer100g = null,
        createdAt = Instant.EPOCH,
        updatedAt = Instant.EPOCH,
    )

    private fun validInput() = PantryItemInput(
        id = null,
        name = "Молоко",
        barcode = "4600000000001",
        quantity = 1.0,
        unit = QuantityUnit.PACKAGE,
        storageLocation = StorageLocation.FRIDGE,
        expiresOn = today.plusDays(2),
        nutritionPer100g = NutritionPer100g(60.0, 3.0, 3.2, 4.7),
    )
}
