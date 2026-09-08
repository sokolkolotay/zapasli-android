package ru.zapasli.app.ui.pantry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import ru.zapasli.app.R
import ru.zapasli.app.core.designsystem.theme.ZapasliSpacing
import ru.zapasli.app.domain.pantry.NutritionPer100g
import ru.zapasli.app.domain.pantry.PantryItem
import ru.zapasli.app.domain.pantry.QuantityUnit
import ru.zapasli.app.domain.pantry.StorageLocation
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PantryEditorSheet(
    item: PantryItem?,
    onDismiss: () -> Unit,
    onSave: (PantryItemInput) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by rememberSaveable(item?.id) { mutableStateOf(item?.name.orEmpty()) }
    var barcode by rememberSaveable(item?.id) { mutableStateOf(item?.barcode.orEmpty()) }
    var quantity by rememberSaveable(item?.id) {
        mutableStateOf(item?.quantity?.toEditorNumber() ?: "1")
    }
    var unitName by rememberSaveable(item?.id) {
        mutableStateOf(item?.unit?.name ?: QuantityUnit.PIECE.name)
    }
    var locationName by rememberSaveable(item?.id) {
        mutableStateOf(item?.storageLocation?.name ?: StorageLocation.FRIDGE.name)
    }
    var expiryEpochDay by rememberSaveable(item?.id) {
        mutableStateOf(item?.expiresOn?.toEpochDay())
    }
    var calories by rememberSaveable(item?.id) {
        mutableStateOf(item?.nutritionPer100g?.caloriesKcal?.toEditorNumber().orEmpty())
    }
    var protein by rememberSaveable(item?.id) {
        mutableStateOf(item?.nutritionPer100g?.proteinGrams?.toEditorNumber().orEmpty())
    }
    var fat by rememberSaveable(item?.id) {
        mutableStateOf(item?.nutritionPer100g?.fatGrams?.toEditorNumber().orEmpty())
    }
    var carbohydrates by rememberSaveable(item?.id) {
        mutableStateOf(item?.nutritionPer100g?.carbohydratesGrams?.toEditorNumber().orEmpty())
    }
    var validationError by remember { mutableStateOf<PantryValidationError?>(null) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    val unit = QuantityUnit.valueOf(unitName)
    val location = StorageLocation.valueOf(locationName)
    val expiresOn = expiryEpochDay?.let(LocalDate::ofEpochDay)

    ModalBottomSheet(
        modifier = Modifier.testTag("product_editor"),
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
                .padding(
                    start = ZapasliSpacing.md,
                    end = ZapasliSpacing.md,
                    bottom = ZapasliSpacing.xl,
                ),
            verticalArrangement = Arrangement.spacedBy(ZapasliSpacing.md),
        ) {
            Text(
                text = stringResource(
                    if (item == null) R.string.add_product_title else R.string.edit_product_title,
                ),
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = stringResource(R.string.product_editor_description),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )

            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("product_name_input"),
                value = name,
                onValueChange = {
                    name = it
                    validationError = null
                },
                label = { Text(stringResource(R.string.product_name_label)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = barcode,
                onValueChange = {
                    barcode = it.filter(Char::isDigit).take(14)
                    validationError = null
                },
                label = { Text(stringResource(R.string.barcode_label)) },
                supportingText = { Text(stringResource(R.string.barcode_hint)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next,
                ),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ZapasliSpacing.sm),
            ) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = quantity,
                    onValueChange = {
                        quantity = it.decimalInput()
                        validationError = null
                    },
                    label = { Text(stringResource(R.string.quantity_label)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next,
                    ),
                )
                SelectionDropdown(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.unit_label),
                    selectedText = unit.label(),
                    options = QuantityUnit.entries,
                    optionLabel = { it.label() },
                    onSelected = { unitName = it.name },
                )
            }

            SelectionDropdown(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(R.string.storage_location_label),
                selectedText = location.label(),
                options = StorageLocation.entries,
                optionLabel = { it.label() },
                onSelected = { locationName = it.name },
            )

            Column(verticalArrangement = Arrangement.spacedBy(ZapasliSpacing.xs)) {
                Text(
                    text = stringResource(R.string.expiry_date_label),
                    style = MaterialTheme.typography.labelLarge,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(ZapasliSpacing.xs),
                ) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = { showDatePicker = true },
                    ) {
                        Text(
                            text = expiresOn?.localizedDate()
                                ?: stringResource(R.string.choose_date),
                        )
                    }
                    if (expiresOn != null) {
                        TextButton(onClick = { expiryEpochDay = null }) {
                            Text(stringResource(R.string.clear_date))
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Column(verticalArrangement = Arrangement.spacedBy(ZapasliSpacing.xs)) {
                Text(
                    text = stringResource(R.string.nutrition_editor_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = stringResource(R.string.nutrition_editor_description),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            NutritionRow(
                firstValue = calories,
                onFirstValueChange = { calories = it.decimalInput() },
                firstLabel = stringResource(R.string.calories_label),
                secondValue = protein,
                onSecondValueChange = { protein = it.decimalInput() },
                secondLabel = stringResource(R.string.protein_label),
            )
            NutritionRow(
                firstValue = fat,
                onFirstValueChange = { fat = it.decimalInput() },
                firstLabel = stringResource(R.string.fat_label),
                secondValue = carbohydrates,
                onSecondValueChange = { carbohydrates = it.decimalInput() },
                secondLabel = stringResource(R.string.carbohydrates_label),
            )

            validationError?.let { error ->
                Text(
                    text = error.label(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ZapasliSpacing.sm),
            ) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onDismiss,
                ) {
                    Text(stringResource(R.string.cancel))
                }
                Button(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("save_product"),
                    onClick = {
                        val input = PantryItemInput(
                            id = item?.id,
                            name = name,
                            barcode = barcode.ifBlank { null },
                            quantity = quantity.toDecimalOrNull() ?: Double.NaN,
                            unit = unit,
                            storageLocation = location,
                            expiresOn = expiresOn,
                            nutritionPer100g = nutritionOrNull(
                                calories = calories,
                                protein = protein,
                                fat = fat,
                                carbohydrates = carbohydrates,
                            ),
                        )
                        val error = validatePantryItemInput(input)
                        if (error == null) {
                            onSave(input)
                        } else {
                            validationError = error
                        }
                    },
                ) {
                    Text(
                        stringResource(
                            if (item == null) R.string.add else R.string.save,
                        ),
                    )
                }
            }
        }
    }

    if (showDatePicker) {
        val initialDateMillis = expiresOn
            ?.atStartOfDay(ZoneOffset.UTC)
            ?.toInstant()
            ?.toEpochMilli()
            ?: Instant.now().toEpochMilli()
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialDateMillis,
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        expiryEpochDay = datePickerState.selectedDateMillis
                            ?.let(Instant::ofEpochMilli)
                            ?.atZone(ZoneOffset.UTC)
                            ?.toLocalDate()
                            ?.toEpochDay()
                        showDatePicker = false
                    },
                ) {
                    Text(stringResource(R.string.choose))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        ) {
            DatePicker(
                state = datePickerState,
                title = {
                    Text(
                        modifier = Modifier.padding(
                            start = ZapasliSpacing.lg,
                            top = ZapasliSpacing.md,
                        ),
                        text = stringResource(R.string.expiry_date_label),
                    )
                },
            )
        }
    }
}

@Composable
private fun <T> SelectionDropdown(
    label: String,
    selectedText: String,
    options: List<T>,
    optionLabel: @Composable (T) -> String,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { expanded = true },
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = selectedText,
                )
                Text("⌄")
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(optionLabel(option)) },
                        onClick = {
                            onSelected(option)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun NutritionRow(
    firstValue: String,
    onFirstValueChange: (String) -> Unit,
    firstLabel: String,
    secondValue: String,
    onSecondValueChange: (String) -> Unit,
    secondLabel: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ZapasliSpacing.sm),
    ) {
        NutritionField(
            modifier = Modifier.weight(1f),
            value = firstValue,
            onValueChange = onFirstValueChange,
            label = firstLabel,
        )
        NutritionField(
            modifier = Modifier.weight(1f),
            value = secondValue,
            onValueChange = onSecondValueChange,
            label = secondLabel,
        )
    }
}

@Composable
private fun NutritionField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Next,
        ),
    )
}

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
private fun PantryValidationError.label(): String = when (this) {
    PantryValidationError.EMPTY_NAME -> stringResource(R.string.error_empty_name)
    PantryValidationError.INVALID_QUANTITY -> stringResource(R.string.error_invalid_quantity)
    PantryValidationError.INVALID_BARCODE -> stringResource(R.string.error_invalid_barcode)
    PantryValidationError.INVALID_NUTRITION -> stringResource(R.string.error_invalid_nutrition)
}

private fun nutritionOrNull(
    calories: String,
    protein: String,
    fat: String,
    carbohydrates: String,
): NutritionPer100g? {
    if (listOf(calories, protein, fat, carbohydrates).all(String::isBlank)) {
        return null
    }
    return NutritionPer100g(
        caloriesKcal = calories.toOptionalDecimal(),
        proteinGrams = protein.toOptionalDecimal(),
        fatGrams = fat.toOptionalDecimal(),
        carbohydratesGrams = carbohydrates.toOptionalDecimal(),
    )
}

private fun String.decimalInput(): String {
    val normalized = replace(',', '.')
        .filter { it.isDigit() || it == '.' }
    val firstSeparator = normalized.indexOf('.')
    if (firstSeparator < 0) return normalized
    return normalized.substring(0, firstSeparator + 1) +
        normalized.substring(firstSeparator + 1).replace(".", "")
}

private fun String.toDecimalOrNull(): Double? =
    replace(',', '.').toDoubleOrNull()

private fun String.toOptionalDecimal(): Double? = when {
    isBlank() -> null
    else -> toDecimalOrNull() ?: Double.NaN
}

private fun Double.toEditorNumber(): String = toString().removeSuffix(".0")

private fun LocalDate.localizedDate(): String = format(
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault()),
)
