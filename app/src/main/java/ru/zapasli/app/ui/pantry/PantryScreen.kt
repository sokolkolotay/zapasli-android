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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import ru.zapasli.app.core.designsystem.theme.ZapasliCoral
import ru.zapasli.app.core.designsystem.theme.ZapasliInk
import ru.zapasli.app.core.designsystem.theme.ZapasliLime
import ru.zapasli.app.core.designsystem.theme.ZapasliMint
import ru.zapasli.app.core.designsystem.theme.ZapasliPaper
import ru.zapasli.app.core.model.ExpiryState
import ru.zapasli.app.domain.pantry.NutritionPer100g
import ru.zapasli.app.domain.pantry.PantryItem
import ru.zapasli.app.domain.pantry.QuantityUnit
import ru.zapasli.app.domain.pantry.StorageLocation
import ru.zapasli.app.ui.scanner.BarcodeScannerDialog
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
    userDisplayName: String,
    isSessionOffline: Boolean,
    onOpenSettings: () -> Unit,
    onLogout: () -> Unit,
    onProductSelected: (PantryItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var editorItem by remember { mutableStateOf<PantryItem?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var deleteCandidate by remember { mutableStateOf<PantryItem?>(null) }
    var showScanner by rememberSaveable { mutableStateOf(false) }
    var scannedBarcode by rememberSaveable { mutableStateOf<String?>(null) }

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
            scannedBarcode = null
            viewModel.clearProductLookup()
            showEditor = true
        },
        onEditProduct = { item ->
            editorItem = item
            scannedBarcode = null
            viewModel.clearProductLookup()
            showEditor = true
        },
        onDeleteProduct = { deleteCandidate = it },
        onFilterSelected = viewModel::selectFilter,
        onSearchQueryChanged = viewModel::setSearchQuery,
        onProductClick = onProductSelected,
        onRetry = viewModel::retry,
        userDisplayName = userDisplayName,
        isSessionOffline = isSessionOffline,
        onOpenSettings = onOpenSettings,
        onLogout = onLogout,
        modifier = modifier,
    )

    if (showEditor) {
        PantryEditorSheet(
            item = editorItem,
            scannedBarcode = scannedBarcode,
            productLookup = uiState.productLookup,
            onScanBarcode = {
                scannedBarcode = null
                viewModel.clearProductLookup()
                showScanner = true
            },
            onBarcodeInputChanged = {
                scannedBarcode = null
                viewModel.clearProductLookup()
            },
            onDismiss = {
                showEditor = false
                scannedBarcode = null
                viewModel.clearProductLookup()
            },
            onSave = { input ->
                viewModel.saveItem(input)
                showEditor = false
                scannedBarcode = null
                viewModel.clearProductLookup()
            },
        )
    }

    if (showScanner) {
        BarcodeScannerDialog(
            onBarcodeDetected = { barcode ->
                showScanner = false
                scannedBarcode = barcode
                viewModel.lookupProduct(barcode)
            },
            onDismiss = { showScanner = false },
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
    userDisplayName: String,
    isSessionOffline: Boolean,
    modifier: Modifier = Modifier,
    onSearchQueryChanged: (String) -> Unit = {},
    onProductClick: (PantryItem) -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onLogout: () -> Unit = {},
) {
    val addProductDescription = stringResource(R.string.add_product)

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("pantry_screen"),
        topBar = {
            PantryTopBar(
                totalItemCount = state.totalItemCount,
                userDisplayName = userDisplayName,
                isSessionOffline = isSessionOffline,
                onOpenSettings = onOpenSettings,
                onLogout = onLogout,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            PantryBottomBar(
                addProductDescription = addProductDescription,
                onAddProduct = onAddProduct,
            )
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
                onSearchQueryChanged = onSearchQueryChanged,
                onProductClick = onProductClick,
            )
        }
    }
}

@Composable
private fun PantryTopBar(
    totalItemCount: Int,
    userDisplayName: String,
    isSessionOffline: Boolean,
    onOpenSettings: () -> Unit,
    onLogout: () -> Unit,
) {
    var profileMenuExpanded by rememberSaveable { mutableStateOf(false) }
    val appName = stringResource(R.string.app_name)
    val normalizedDisplayName = userDisplayName.trim().ifBlank { appName }
    val profileMenuDescription = stringResource(
        R.string.profile_menu_description,
        normalizedDisplayName,
    )
    val itemCount = pluralStringResource(
        R.plurals.pantry_item_count,
        totalItemCount,
        totalItemCount,
    )
    Surface(
        color = ZapasliInk,
        contentColor = ZapasliPaper,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = ZapasliSpacing.md, vertical = ZapasliSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.family_home_title),
                    color = ZapasliMint,
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(
                    text = stringResource(R.string.pantry_title),
                    color = ZapasliPaper,
                    style = MaterialTheme.typography.headlineSmall,
                )
                if (isSessionOffline) {
                    Text(
                        text = stringResource(R.string.offline_session),
                        color = ZapasliCoral,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
            Box {
                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        onClick = { profileMenuExpanded = true },
                        modifier = Modifier
                            .testTag("profile_menu_button")
                            .semantics { contentDescription = profileMenuDescription },
                        shape = CircleShape,
                        color = ZapasliLime,
                        contentColor = ZapasliInk,
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            text = normalizedDisplayName.take(2).uppercase(),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Text(
                        text = itemCount,
                        color = ZapasliPaper.copy(alpha = 0.72f),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
                DropdownMenu(
                    expanded = profileMenuExpanded,
                    onDismissRequest = { profileMenuExpanded = false },
                    modifier = Modifier.testTag("profile_menu"),
                ) {
                    Column(
                        modifier = Modifier.padding(
                            horizontal = ZapasliSpacing.md,
                            vertical = ZapasliSpacing.sm,
                        ),
                    ) {
                        Text(
                            text = normalizedDisplayName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = stringResource(R.string.profile_menu_title),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                    HorizontalDivider()
                    DropdownMenuItem(
                        modifier = Modifier.testTag("profile_settings"),
                        text = { Text(stringResource(R.string.settings_title)) },
                        onClick = {
                            profileMenuExpanded = false
                            onOpenSettings()
                        },
                    )
                    DropdownMenuItem(
                        modifier = Modifier.testTag("profile_logout"),
                        text = {
                            Text(
                                text = stringResource(R.string.sign_out),
                                color = ZapasliCoral,
                            )
                        },
                        onClick = {
                            profileMenuExpanded = false
                            onLogout()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun PantryBottomBar(
    addProductDescription: String,
    onAddProduct: () -> Unit,
) {
    Surface(
        color = ZapasliInk,
        contentColor = ZapasliPaper,
        shadowElevation = 12.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = ZapasliSpacing.lg,
                    vertical = ZapasliSpacing.sm,
                ),
        ) {
            Text(
                modifier = Modifier.align(Alignment.CenterStart),
                text = stringResource(R.string.pantry_title),
                color = ZapasliLime,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.labelLarge,
            )
            FloatingActionButton(
                modifier = Modifier
                    .align(Alignment.Center)
                    .testTag("add_product_fab")
                    .semantics { contentDescription = addProductDescription },
                onClick = onAddProduct,
                shape = CircleShape,
                containerColor = ZapasliLime,
                contentColor = ZapasliInk,
            ) {
                Text("+", style = MaterialTheme.typography.headlineSmall)
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
    onSearchQueryChanged: (String) -> Unit,
    onProductClick: (PantryItem) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("pantry_list"),
        contentPadding = PaddingValues(
            start = ZapasliSpacing.md,
            top = contentPadding.calculateTopPadding() + ZapasliSpacing.md,
            end = ZapasliSpacing.md,
            bottom = contentPadding.calculateBottomPadding() + ZapasliSpacing.md,
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
            AttentionSummary(
                totalItemCount = state.totalItemCount,
                attentionItemCount = state.attentionItemCount,
            )
        }
        item {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth().testTag("pantry_search"),
                value = state.searchQuery,
                onValueChange = onSearchQueryChanged,
                label = { Text(stringResource(R.string.search_products)) },
                singleLine = true,
                shape = MaterialTheme.shapes.large,
            )
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
                    onOpenDetails = { onProductClick(item) },
                )
            }
        }
    }
}

@Composable
private fun AttentionSummary(totalItemCount: Int, attentionItemCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = ZapasliLime,
        ),
    ) {
        Row(
            modifier = Modifier.padding(ZapasliSpacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ZapasliSpacing.md),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = totalItemCount.toString(),
                    color = ZapasliInk,
                    style = MaterialTheme.typography.displaySmall,
                )
                Text(
                    text = stringResource(R.string.products_tracked),
                    color = ZapasliInk,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = if (attentionItemCount > 0) ZapasliCoral else ZapasliInk,
                contentColor = if (attentionItemCount > 0) ZapasliInk else ZapasliLime,
            ) {
                Text(
                    modifier = Modifier.padding(ZapasliSpacing.sm),
                    text = if (attentionItemCount > 0) {
                        pluralStringResource(
                            R.plurals.attention_item_count,
                            attentionItemCount,
                            attentionItemCount,
                        )
                    } else {
                        stringResource(R.string.everything_fresh_title)
                    },
                    style = MaterialTheme.typography.labelMedium,
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
    onOpenDetails: () -> Unit,
) {
    ElevatedCard(
        onClick = onOpenDetails,
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
                        .background(ZapasliMint),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = item.name.firstOrNull()?.uppercase() ?: "?",
                        color = ZapasliInk,
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
            userDisplayName = "Alex",
            isSessionOffline = false,
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
            userDisplayName = "Alex",
            isSessionOffline = false,
        )
    }
}
