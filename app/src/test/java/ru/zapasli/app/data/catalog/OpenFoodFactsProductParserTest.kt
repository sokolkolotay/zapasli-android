package ru.zapasli.app.data.catalog

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.zapasli.app.domain.catalog.ProductCatalogResult

class OpenFoodFactsProductParserTest {
    @Test
    fun `product response maps russian name and nutrition`() {
        val result = parseOpenFoodFactsProduct(
            body = """
                {
                  "code": "4600000000001",
                  "product": {
                    "product_name": "Milk",
                    "product_name_ru": "Молоко",
                    "nutrition": {
                      "aggregated_set": {
                        "per": "100g",
                        "nutrients": {
                          "energy-kcal": { "unit": "kcal", "value": 60 },
                          "proteins": { "unit": "g", "value": 3.0 },
                          "fat": { "unit": "g", "value": 3.2 },
                          "carbohydrates": { "unit": "g", "value": 4.7 }
                        }
                      }
                    }
                  }
                }
            """.trimIndent(),
            fallbackBarcode = "4600000000001",
        )

        val found = result as ProductCatalogResult.Found
        assertEquals("4600000000001", found.product.barcode)
        assertEquals("Молоко", found.product.name)
        assertEquals(60.0, found.product.nutritionPer100g?.caloriesKcal)
        assertEquals(3.0, found.product.nutritionPer100g?.proteinGrams)
        assertEquals(3.2, found.product.nutritionPer100g?.fatGrams)
        assertEquals(4.7, found.product.nutritionPer100g?.carbohydratesGrams)
    }

    @Test
    fun `missing product returns not found`() {
        val result = parseOpenFoodFactsProduct(
            body = """{"code":"12345678","status":"failure"}""",
            fallbackBarcode = "12345678",
        )

        assertTrue(result is ProductCatalogResult.NotFound)
    }

    @Test
    fun `product without nutrition still returns a suggestion`() {
        val result = parseOpenFoodFactsProduct(
            body = """{"product":{"product_name":"Bread"}}""",
            fallbackBarcode = "12345678",
        ) as ProductCatalogResult.Found

        assertEquals("Bread", result.product.name)
        assertNull(result.product.nutritionPer100g)
    }

    @Test
    fun `invalid response is unavailable`() {
        val result = parseOpenFoodFactsProduct(
            body = "not-json",
            fallbackBarcode = "12345678",
        )

        assertTrue(result is ProductCatalogResult.Unavailable)
    }
}
