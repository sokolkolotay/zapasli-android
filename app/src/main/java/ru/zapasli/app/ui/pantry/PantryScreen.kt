package ru.zapasli.app.ui.pantry

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.zapasli.app.R
import ru.zapasli.app.core.designsystem.theme.Green50
import ru.zapasli.app.core.designsystem.theme.Green700
import ru.zapasli.app.core.designsystem.theme.Orange50
import ru.zapasli.app.core.designsystem.theme.Orange500
import ru.zapasli.app.core.designsystem.theme.Red50
import ru.zapasli.app.core.designsystem.theme.Red500
import ru.zapasli.app.core.designsystem.theme.Yellow50
import ru.zapasli.app.core.designsystem.theme.Yellow500
import ru.zapasli.app.core.designsystem.theme.ZapasliSpacing
import ru.zapasli.app.core.designsystem.theme.ZapasliTheme
import ru.zapasli.app.core.model.ExpiryState
import ru.zapasli.app.domain.pantry.NutritionPer100g
import ru.zapasli.app.domain.pantry.PantryItem
import ru.zapasli.app.domain.pantry.QuantityUnit
import ru.zapasli.app.domain.pantry.StorageLocation
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

@Composable
fun PantryRoute(
    viewModel: PantryViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var editorItem by remember { mutableStateOf<PantryItem?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var deleteCandidate by remember { mutableStateOf<PantryItem?>(null) }

    val messageText = uiState.message?.let { message -> pantryMessageText(message) }
    LaunchedEffect(uiState.message, messageText) {
        if (messageText != null) {
            snackbarHostState.showSnackbar(messageText)
            viewModel.consumeMessage()
        }
    }

    PantryScreen(
        state = uiState,
        snackbarHostState = snackbarHostState,
        onAddProduct = {
            editorItem = null
            showEditor = true
        },
        onEditProduct = { item ->
            editorItem = item
            showEditor = true
        },
        onDeleteProduct = { deleteCandidate = it },
        onFilterSelected = viewModel::selectFilter,
        onRetry = viewModel::retry,
        modifier = modifier,
    )

    if (showEditor) {
        PantryEditorSheet(
            item = editorItem,
            onDismiss = { showEditor = false },
            onSave = { input ->
                viewModel.saveItem(input)
                showEditor = false
            },
        )
    }

    deleteCandidate?.let { item ->
        DeleteProductDialog(
            itemName = item.name,
            onDismiss = { deleteCandidate = null },
            onConfirm = {
                viewModel.deleteItem(item.id)
                deleteCandidate = null
            },
        )
    }
}

@Composable
internal fun PantryScreen(
    state: PantryUiState,
    snackbarHostState: SnackbarHostState,
    onAddProduct: () -> Unit,
    onEditProduct: (PantryItem) -> Unit,
    onDeleteProduct: (PantryItem) -> Unit,
    onFilterSelected: (PantryFilter) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val addProductDescription = stringResource(R.string.add_product)

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("pantry_screen"),
        topBar = { PantryTopBar(state.totalItemCount) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (!state.isLoading && !state.loadFailed && state.totalItemCount > 0) {
                FloatingActionButton(
                    modifier = Modifier
                        .testTag("add_product_fab")
                        .semantics { contentDescription = addProductDescription },
                    onClick = onAddProduct,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Text(
                        text = "+",
                        style = MaterialTheme.typography.headlineSmall,
                    )
                }
            }
        },
    ) { contentPadding ->
        when {
            state.isLoading -> LoadingContent(contentPadding)
            state.loadFailed -> ErrorContent(contentPadding, onRetry)
            else -> PantryContent(
                state = state,
                contentPadding = contentPadding,
                onAddProduct = onAddProduct,
                onEditProduct = onEditProduct,
                onDeleteProduct = onDeleteProduct,
                onFilterSelected = onFilterSelected,
            )
        }
    }
}

@Composable
private fun PantryTopBar(totalItemCount: Int) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = ZapasliSpacing.md, vertical = ZapasliSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primary,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "Z",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
            }
            Spacer(modifier = Modifier.width(ZapasliSpacing.sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.pantry_title),
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = pluralStringResource(
                        R.plurals.pantry_item_count,
                        totalItemCount,
                        totalItemCount,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun PantryContent(
    state: PantryUiState,
    contentPadding: PaddingValues,
    onAddProduct: () -> Unit,
    onEditProduct: (PantryItem) -> Unit,
    onDeleteProduct: (PantryItem) -> Unit,
    onFilterSelected: (PantryFilter) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("pantry_list"),
        contentPadding = PaddingValues(
            start = ZapasliSpacing.md,
            top = contentPadding.calculateTopPadding() + ZapasliSpacing.md,
            end = ZapasliSpacing.md,
            bottom = contentPadding.calculateBottomPadding() + 96.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(ZapasliSpacing.md),
    ) {
        if (state.totalItemCount == 0) {
            item {
                EmptyPantry(onAddProduct)
            }
            return@LazyColumn
        }

        item {
            AttentionSummary(state.attentionItemCount)
        }
        item {
            PantryFilters(
                selectedFilter = state.selectedFilter,
                onFilterSelected = onFilterSelected,
            )
        }

        if (state.items.isEmpty()) {
            item { EmptyFilter() }
        } else {
            items(
                items = state.items,
                key = PantryItem::id,
            ) { item ->
                PantryProductCard(
                    item = item,
                    onEdit = { onEditProduct(item) },
                    onDelete = { onDeleteProduct(item) },
                )
            }
        }
    }
}

@Composable
private fun AttentionSummary(attentionItemCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = if (attentionItemCount > 0) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.primaryContainer
            },
        ),
    ) {
        Row(
            modifier = Modifier.padding(ZapasliSpacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ZapasliSpacing.md),
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = if (attentionItemCount > 0) Orange500 else Green700,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (attentionItemCount > 0) attentionItemCount.toString() else "✓",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (attentionItemCount > 0) {
                        stringResource(R.string.attention_title)
                    } else {
                        stringResource(R.string.everything_fresh_title)
                    },
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = if (attentionItemCount > 0) {
                        pluralStringResource(
                            R.plurals.attention_item_count,
                            attentionItemCount,
                            attentionItemCount,
                        )
                    } else {
                        stringResource(R.string.everything_fresh_description)
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun PantryFilters(
    selectedFilter: PantryFilter,
    onFilterSelected: (PantryFilter) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(ZapasliSpacing.xs),
    ) {
        PantryFilter.entries.forEach { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = { Text(filter.label()) },
            )
        }
    }
}

@Composable
private fun PantryProductCard(
    item: PantryItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("product_card_${item.id}"),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(modifier = Modifier.padding(ZapasliSpacing.md)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(Green50),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = item.name.firstOrNull()?.uppercase() ?: "?",
                        color = Green700,
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
                Spacer(modifier = Modifier.width(ZapasliSpacing.sm))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        modifier = Modifier.testTag("product_name"),
                        text = item.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = "${item.storageLocation.label()} · ${item.quantityLabel()}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            Spacer(modifier = Modifier.height(ZapasliSpacing.sm))
            ExpiryBadge(item)

            item.nutritionPer100g?.let { nutrition ->
                Spacer(modifier = Modifier.height(ZapasliSpacing.md))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(ZapasliSpacing.sm))
                Text(
                    text = stringResource(R.string.nutrition_compact, nutrition.compactLabel()),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(
                    modifier = Modifier.testTag("edit_product"),
                    onClick = onEdit,
                ) {
                    Text(stringResource(R.string.edit))
                }
                TextButton(
                    modifier = Modifier.testTag("delete_product"),
                    onClick = onDelete,
                ) {
                    Text(
                        text = stringResource(R.string.delete),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpiryBadge(item: PantryItem) {
    val today = LocalDate.now()
    val state = item.expiryState(today)
    val days = item.expiresOn?.let { ChronoUnit.DAYS.between(today, it).toInt() }
    val (containerColor, contentColor) = when (state) {
        ExpiryState.Expired -> Red50 to Red500
        ExpiryState.Today -> Orange50 to Orange500
        ExpiryState.Soon -> Yellow50 to Yellow500
        ExpiryState.Fresh -> Green50 to Green700
        ExpiryState.NoDate -> MaterialTheme.colorScheme.surfaceVariant to
            MaterialTheme.colorScheme.onSurfaceVariant
    }
    val label = when (state) {
        ExpiryState.Expired -> pluralStringResource(
            R.plurals.expired_days,
            -requireNotNull(days),
            -days,
        )
        ExpiryState.Today -> stringResource(R.string.expires_today)
        ExpiryState.Soon -> pluralStringResource(
            R.plurals.expires_in_days,
            requireNotNull(days),
            days,
        )
        ExpiryState.Fresh -> stringResource(
            R.string.expires_on,
            requireNotNull(item.expiresOn).localizedDate(),
        )
        ExpiryState.NoDate -> stringResource(R.string.no_expiry_date)
    }

    Surface(
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor,
    ) {
        Text(
            modifier = Modifier.padding(
                horizontal = ZapasliSpacing.sm,
                vertical = ZapasliSpacing.xs,
            ),
            text = label,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
private fun EmptyPantry(onAddProduct: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 72.dp)
            .testTag("empty_pantry"),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            modifier = Modifier.size(88.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "Z",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.displaySmall,
                )
            }
        }
        Spacer(modifier = Modifier.height(ZapasliSpacing.lg))
        Text(
            text = stringResource(R.string.empty_pantry_title),
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(modifier = Modifier.height(ZapasliSpacing.xs))
        Text(
            text = stringResource(R.string.empty_pantry_description),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(modifier = Modifier.height(ZapasliSpacing.lg))
        Button(
            modifier = Modifier.testTag("add_first_product"),
            onClick = onAddProduct,
        ) {
            Text(stringResource(R.string.add_first_product))
        }
    }
}

@Composable
private fun EmptyFilter() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = ZapasliSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.empty_filter_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(R.string.empty_filter_description),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun LoadingContent(contentPadding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(
    contentPadding: PaddingValues,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(ZapasliSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.load_error_title),
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(modifier = Modifier.height(ZapasliSpacing.xs))
        Text(
            text = stringResource(R.string.load_error_description),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(ZapasliSpacing.lg))
        Button(onClick = onRetry) {
            Text(stringResource(R.string.retry))
        }
    }
}

@Composable
private fun DeleteProductDialog(
    itemName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_product_title)) },
        text = { Text(stringResource(R.string.delete_product_description, itemName)) },
        confirmButton = {
            TextButton(
                modifier = Modifier.testTag("confirm_delete"),
                onClick = onConfirm,
            ) {
                Text(
                    text = stringResource(R.string.delete),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@Composable
private fun PantryFilter.label(): String = when (this) {
    PantryFilter.ALL -> stringResource(R.string.filter_all)
    PantryFilter.EXPIRING_SOON -> stringResource(R.string.filter_expiring_soon)
    PantryFilter.EXPIRED -> stringResource(R.string.filter_expired)
    PantryFilter.NO_DATE -> stringResource(R.string.filter_no_date)
}

@Composable
private fun StorageLocation.label(): String = when (this) {
    StorageLocation.FRIDGE -> stringResource(R.string.location_fridge)
    StorageLocation.FREEZER -> stringResource(R.string.location_freezer)
    StorageLocation.PANTRY -> stringResource(R.string.location_pantry)
    StorageLocation.OTHER -> stringResource(R.string.location_other)
}

@Composable
private fun PantryItem.quantityLabel(): String = stringResource(
    R.string.quantity_with_unit,
    quantity.localizedNumber(),
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
private fun pantryMessageText(message: PantryMessage): String = when (message) {
    PantryMessage.ItemAdded -> stringResource(R.string.product_added)
    PantryMessage.ItemUpdated -> stringResource(R.string.product_updated)
    PantryMessage.ItemDeleted -> stringResource(R.string.product_deleted)
    PantryMessage.SaveFailed -> stringResource(R.string.product_save_failed)
    PantryMessage.DeleteFailed -> stringResource(R.string.product_delete_failed)
    is PantryMessage.ValidationFailed -> when (message.error) {
        PantryValidationError.EMPTY_NAME -> stringResource(R.string.error_empty_name)
        PantryValidationError.INVALID_QUANTITY -> stringResource(R.string.error_invalid_quantity)
        PantryValidationError.INVALID_BARCODE -> stringResource(R.string.error_invalid_barcode)
        PantryValidationError.INVALID_NUTRITION -> stringResource(R.string.error_invalid_nutrition)
    }
}

private fun NutritionPer100g.compactLabel(): String {
    fun Double?.value() = this?.localizedNumber() ?: "—"
    return listOf(
        caloriesKcal.value(),
        proteinGrams.value(),
        fatGrams.value(),
        carbohydratesGrams.value(),
    ).joinToString(" / ")
}

private fun Double.localizedNumber(): String = NumberFormat.getNumberInstance().apply {
    maximumFractionDigits = 2
}.format(this)

private fun LocalDate.localizedDate(): String = format(
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault()),
)

@Preview(name = "Empty pantry", showBackground = true, locale = "ru")
@Composable
private fun EmptyPantryPreview() {
    ZapasliTheme {
        PantryScreen(
            state = PantryUiState(isLoading = false),
            snackbarHostState = remember { SnackbarHostState() },
            onAddProduct = {},
            onEditProduct = {},
            onDeleteProduct = {},
            onFilterSelected = {},
            onRetry = {},
        )
    }
}

@Preview(name = "Pantry with product", showBackground = true, locale = "ru")
@Composable
private fun PantryWithProductPreview() {
    val sample = PantryItem(
        id = "preview",
        name = "Молоко 3,2%",
        barcode = "4600000000001",
        quantity = 900.0,
        unit = QuantityUnit.MILLILITER,
        storageLocation = StorageLocation.FRIDGE,
        expiresOn = LocalDate.now().plusDays(2),
        nutritionPer100g = NutritionPer100g(60.0, 3.0, 3.2, 4.7),
        createdAt = Instant.now(),
        updatedAt = Instant.now(),
    )
    ZapasliTheme {
        PantryScreen(
            state = PantryUiState(
                isLoading = false,
                items = listOf(sample),
                totalItemCount = 1,
                attentionItemCount = 1,
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onAddProduct = {},
            onEditProduct = {},
            onDeleteProduct = {},
            onFilterSelected = {},
            onRetry = {},
        )
    }
}
