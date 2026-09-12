package ru.zapasli.app.ui.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.zapasli.app.R
import ru.zapasli.app.core.designsystem.theme.ZapasliCoral
import ru.zapasli.app.core.designsystem.theme.ZapasliInk
import ru.zapasli.app.core.designsystem.theme.ZapasliLime
import ru.zapasli.app.core.designsystem.theme.ZapasliMint
import ru.zapasli.app.core.designsystem.theme.ZapasliPaper
import ru.zapasli.app.core.designsystem.theme.ZapasliSpacing
import ru.zapasli.app.core.model.ExpiryState
import ru.zapasli.app.domain.pantry.NutritionPer100g
import ru.zapasli.app.domain.pantry.PantryItem
import ru.zapasli.app.domain.pantry.QuantityUnit
import ru.zapasli.app.domain.pantry.StorageLocation
import ru.zapasli.app.ui.pantry.PantryViewModel
import ru.zapasli.app.ui.pantry.expiryState
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

@Composable
fun ProductDetailsRoute(
    viewModel: PantryViewModel,
    itemId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ProductDetailsScreen(
        item = state.allItems.firstOrNull { it.id == itemId },
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
internal fun ProductDetailsScreen(
    item: PantryItem?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val locale = LocalConfiguration.current.locales[0]
    Scaffold(
        modifier = modifier.fillMaxSize().testTag("product_details_screen"),
        topBar = { DetailsTopBar(onBack) },
    ) { padding ->
        if (item == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text(stringResource(R.string.product_not_found))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = ZapasliSpacing.md,
                    top = padding.calculateTopPadding() + ZapasliSpacing.md,
                    end = ZapasliSpacing.md,
                    bottom = padding.calculateBottomPadding() + ZapasliSpacing.xl,
                ),
                verticalArrangement = Arrangement.spacedBy(ZapasliSpacing.md),
            ) {
                item { ProductHero(item, locale) }
                item {
                    DetailsCard(
                        title = stringResource(R.string.product_details_storage),
                        rows = listOf(
                            stringResource(R.string.storage_location_label) to item.storageLocation.label(),
                            stringResource(R.string.quantity_label) to item.quantityLabel(),
                            stringResource(R.string.expiry_date_label) to item.expiryLabel(locale),
                        ),
                    )
                }
                item {
                    val nutrition = item.nutritionPer100g
                    DetailsCard(
                        title = stringResource(R.string.nutrition_editor_title),
                        rows = if (nutrition == null) {
                            listOf(stringResource(R.string.product_details_no_nutrition) to "")
                        } else {
                            nutrition.rows()
                        },
                    )
                }
                item {
                    DetailsCard(
                        title = stringResource(R.string.product_details_identification),
                        rows = listOf(
                            stringResource(R.string.barcode_label) to
                                (item.barcode ?: stringResource(R.string.product_details_not_specified)),
                            stringResource(R.string.product_details_updated) to
                                DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
                                    .withLocale(locale)
                                    .withZone(java.time.ZoneId.systemDefault())
                                    .format(item.updatedAt),
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailsTopBar(onBack: () -> Unit) {
    Surface(color = ZapasliInk, contentColor = ZapasliPaper) {
        Row(
            modifier = Modifier.fillMaxWidth().statusBarsPadding()
                .padding(horizontal = ZapasliSpacing.xs, vertical = ZapasliSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onBack) {
                Text(stringResource(R.string.back), color = ZapasliLime)
            }
            Text(
                text = stringResource(R.string.product_details_title),
                style = MaterialTheme.typography.titleLarge,
            )
        }
    }
}

@Composable
private fun ProductHero(item: PantryItem, locale: Locale) {
    Card(
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = ZapasliInk, contentColor = ZapasliPaper),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(ZapasliSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(ZapasliSpacing.md),
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                color = ZapasliLime,
                contentColor = ZapasliInk,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = item.name.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Text(item.name, style = MaterialTheme.typography.displaySmall)
            Surface(
                shape = CircleShape,
                color = item.expiryColor(),
                contentColor = ZapasliInk,
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = ZapasliSpacing.sm, vertical = 6.dp),
                    text = item.expiryLabel(locale),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
private fun DetailsCard(title: String, rows: List<Pair<String, String>>) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(ZapasliSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(ZapasliSpacing.sm),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            rows.forEach { (label, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = label,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    if (value.isNotBlank()) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = value,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PantryItem.expiryLabel(locale: Locale): String {
    val today = LocalDate.now()
    val days = expiresOn?.let { ChronoUnit.DAYS.between(today, it).toInt() }
    return when (expiryState(today)) {
        ExpiryState.Expired -> pluralStringResource(
            R.plurals.product_details_expired,
            -requireNotNull(days),
            -days,
        )
        ExpiryState.Today -> stringResource(R.string.expires_today)
        ExpiryState.Soon -> pluralStringResource(
            R.plurals.product_details_days_left,
            requireNotNull(days),
            days,
        )
        ExpiryState.Fresh -> stringResource(
            R.string.expires_on,
            requireNotNull(expiresOn).format(
                DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                    .withLocale(locale),
            ),
        )
        ExpiryState.NoDate -> stringResource(R.string.no_expiry_date)
    }
}

@Composable
private fun PantryItem.expiryColor() = when (expiryState(LocalDate.now())) {
    ExpiryState.Expired, ExpiryState.Today -> ZapasliCoral
    ExpiryState.Soon -> ZapasliLime
    ExpiryState.Fresh -> ZapasliMint
    ExpiryState.NoDate -> ZapasliPaper
}

@Composable
private fun PantryItem.quantityLabel(): String = stringResource(
    R.string.quantity_with_unit,
    quantity.number(),
    unit.label(),
)

@Composable
private fun QuantityUnit.label(): String = when (this) {
    QuantityUnit.PIECE -> stringResource(R.string.unit_piece)
    QuantityUnit.GRAM -> stringResource(R.string.unit_gram)
    QuantityUnit.KILOGRAM -> stringResource(R.string.unit_kilogram)
    QuantityUnit.MILLILITER -> stringResource(R.string.unit_milliliter)
    QuantityUnit.LITER -> stringResource(R.string.unit_liter)
    QuantityUnit.PACKAGE -> stringResource(R.string.unit_package)
}

@Composable
private fun StorageLocation.label(): String = when (this) {
    StorageLocation.FRIDGE -> stringResource(R.string.location_fridge)
    StorageLocation.FREEZER -> stringResource(R.string.location_freezer)
    StorageLocation.PANTRY -> stringResource(R.string.location_pantry)
    StorageLocation.OTHER -> stringResource(R.string.location_other)
}

@Composable
private fun NutritionPer100g.rows(): List<Pair<String, String>> = listOf(
    stringResource(R.string.calories_label) to (caloriesKcal?.number() ?: "—"),
    stringResource(R.string.protein_label) to (proteinGrams?.number() ?: "—"),
    stringResource(R.string.fat_label) to (fatGrams?.number() ?: "—"),
    stringResource(R.string.carbohydrates_label) to (carbohydratesGrams?.number() ?: "—"),
)

private fun Double.number(): String = NumberFormat.getNumberInstance().apply {
    maximumFractionDigits = 2
}.format(this)
