package ru.zapasli.app.data.catalog

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import ru.zapasli.app.domain.catalog.ProductCatalogRepository
import ru.zapasli.app.domain.catalog.ProductCatalogResult
import ru.zapasli.app.domain.catalog.ProductSuggestion
import ru.zapasli.app.domain.pantry.NutritionPer100g
import java.io.IOException
import javax.inject.Inject

private const val USER_AGENT =
    "Zapasli/0.2.0 (https://github.com/sokolkolotay/zapasli-android)"
private const val REQUESTED_FIELDS =
    "code,product_name,product_name_ru,product_name_en,nutrition"

class OpenFoodFactsProductCatalogRepository @Inject constructor(
    private val client: OkHttpClient,
    private val json: Json,
) : ProductCatalogRepository {
    override suspend fun findByBarcode(barcode: String): ProductCatalogResult =
        withContext(Dispatchers.IO) {
            try {
                val url = "https://world.openfoodfacts.org/".toHttpUrl()
                    .newBuilder()
                    .addPathSegments("api/v3.6/product")
                    .addPathSegment("$barcode.json")
                    .addQueryParameter("fields", REQUESTED_FIELDS)
                    .build()
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", USER_AGENT)
                    .header("Accept", "application/json")
                    .get()
                    .build()

                client.newCall(request).execute().use { response ->
                    when {
                        response.code == 404 -> ProductCatalogResult.NotFound
                        !response.isSuccessful -> ProductCatalogResult.Unavailable
                        else -> response.body.string()
                            .let { body -> parseOpenFoodFactsProduct(body, barcode, json) }
                    }
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: IOException) {
                ProductCatalogResult.Unavailable
            } catch (_: IllegalArgumentException) {
                ProductCatalogResult.Unavailable
            }
        }
}

internal fun parseOpenFoodFactsProduct(
    body: String,
    fallbackBarcode: String,
    json: Json = Json,
): ProductCatalogResult {
    val root = runCatching { json.parseToJsonElement(body).jsonObject }.getOrNull()
        ?: return ProductCatalogResult.Unavailable
    val product = root["product"] as? JsonObject ?: return ProductCatalogResult.NotFound
    val barcode = root.string("code") ?: product.string("code") ?: fallbackBarcode
    val name = sequenceOf(
        product.string("product_name_ru"),
        product.string("product_name"),
        product.string("product_name_en"),
    ).firstOrNull { !it.isNullOrBlank() }?.trim()
    val currentNutrients = product.objectValue("nutrition")
        ?.objectValue("aggregated_set")
        ?.objectValue("nutrients")
    val legacyNutrients = product["nutriments"] as? JsonObject
    val nutrition = (currentNutrients ?: legacyNutrients)?.let {
        NutritionPer100g(
            caloriesKcal = currentNutrients?.nutrientValue("energy-kcal")
                ?: legacyNutrients?.number("energy-kcal_100g"),
            proteinGrams = currentNutrients?.nutrientValue("proteins")
                ?: legacyNutrients?.number("proteins_100g"),
            fatGrams = currentNutrients?.nutrientValue("fat")
                ?: legacyNutrients?.number("fat_100g"),
            carbohydratesGrams = currentNutrients?.nutrientValue("carbohydrates")
                ?: legacyNutrients?.number("carbohydrates_100g"),
        ).takeUnless { item -> item.values().all { it == null } }
    }

    return ProductCatalogResult.Found(
        ProductSuggestion(
            barcode = barcode,
            name = name,
            nutritionPer100g = nutrition,
        ),
    )
}

private fun JsonObject.string(key: String): String? =
    (get(key) as? JsonPrimitive)?.contentOrNull

private fun JsonObject.number(key: String): Double? {
    val primitive = get(key) as? JsonPrimitive ?: return null
    return primitive.doubleOrNull ?: primitive.contentOrNull?.toDoubleOrNull()
}

private fun JsonObject.objectValue(key: String): JsonObject? = get(key) as? JsonObject

private fun JsonObject.nutrientValue(key: String): Double? =
    objectValue(key)?.number("value")

private fun NutritionPer100g.values(): List<Double?> = listOf(
    caloriesKcal,
    proteinGrams,
    fatGrams,
    carbohydratesGrams,
)
