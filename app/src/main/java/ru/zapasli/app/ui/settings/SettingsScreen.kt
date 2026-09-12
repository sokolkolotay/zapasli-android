package ru.zapasli.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.zapasli.app.R
import ru.zapasli.app.core.designsystem.theme.ZapasliInk
import ru.zapasli.app.core.designsystem.theme.ZapasliLime
import ru.zapasli.app.core.designsystem.theme.ZapasliPaper
import ru.zapasli.app.core.designsystem.theme.ZapasliSpacing
import ru.zapasli.app.core.preferences.LanguageMode
import ru.zapasli.app.core.preferences.PantryFilterPreference
import ru.zapasli.app.core.preferences.ThemeMode

@Composable
fun SettingsRoute(
    viewModel: SettingsViewModel,
    userDisplayName: String,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsScreen(
        state = state,
        userDisplayName = userDisplayName,
        onBack = onBack,
        onThemeSelected = viewModel::setThemeMode,
        onLanguageSelected = viewModel::setLanguageMode,
        onFilterSelected = viewModel::setDefaultPantryFilter,
        onLogout = onLogout,
        modifier = modifier,
    )
}

@Composable
internal fun SettingsScreen(
    state: SettingsUiState,
    userDisplayName: String,
    onBack: () -> Unit,
    onThemeSelected: (ThemeMode) -> Unit,
    onLanguageSelected: (LanguageMode) -> Unit,
    onFilterSelected: (PantryFilterPreference) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize().testTag("settings_screen"),
        topBar = { SettingsTopBar(onBack = onBack) },
    ) { padding ->
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
            item {
                ProfileCard(userDisplayName)
            }
            item {
                ChoiceSection(
                    title = stringResource(R.string.settings_theme_title),
                    description = stringResource(R.string.settings_theme_description),
                    choices = ThemeMode.entries,
                    selected = state.themeMode,
                    label = { it.label() },
                    onSelected = onThemeSelected,
                )
            }
            item {
                ChoiceSection(
                    title = stringResource(R.string.settings_language_title),
                    description = stringResource(R.string.settings_language_description),
                    choices = LanguageMode.entries,
                    selected = state.languageMode,
                    label = { it.label() },
                    onSelected = onLanguageSelected,
                )
            }
            item {
                ChoiceSection(
                    title = stringResource(R.string.settings_filter_title),
                    description = stringResource(R.string.settings_filter_description),
                    choices = PantryFilterPreference.entries,
                    selected = state.defaultPantryFilter,
                    label = { it.label() },
                    onSelected = onFilterSelected,
                )
            }
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(ZapasliSpacing.lg),
                        verticalArrangement = Arrangement.spacedBy(ZapasliSpacing.xs),
                    ) {
                        Text(
                            text = stringResource(R.string.settings_about_title),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = stringResource(R.string.settings_about_description),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = "api.zapasli.sokolkolotaj.ru",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
            item {
                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onLogout,
                ) {
                    Text(
                        text = stringResource(R.string.sign_out),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsTopBar(onBack: () -> Unit) {
    Surface(color = ZapasliInk, contentColor = ZapasliLime) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = ZapasliSpacing.xs, vertical = ZapasliSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onBack) {
                Text(stringResource(R.string.back), color = ZapasliLime)
            }
            Text(
                text = stringResource(R.string.settings_title),
                color = ZapasliPaper,
                style = MaterialTheme.typography.titleLarge,
            )
        }
    }
}

@Composable
private fun ProfileCard(userDisplayName: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ZapasliLime),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(ZapasliSpacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ZapasliSpacing.md),
        ) {
            Surface(shape = CircleShape, color = ZapasliInk, contentColor = ZapasliLime) {
                Text(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                    text = userDisplayName.trim().take(2).uppercase().ifBlank { "Z" },
                    fontWeight = FontWeight.Bold,
                )
            }
            Column {
                Text(
                    text = stringResource(R.string.family_home_title),
                    color = ZapasliInk,
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(
                    text = userDisplayName,
                    color = ZapasliInk,
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
        }
    }
}

@Composable
private fun <T> ChoiceSection(
    title: String,
    description: String,
    choices: List<T>,
    selected: T,
    label: @Composable (T) -> String,
    onSelected: (T) -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(modifier = Modifier.padding(vertical = ZapasliSpacing.sm)) {
            Column(modifier = Modifier.padding(horizontal = ZapasliSpacing.md)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(
                    description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Spacer(Modifier.height(ZapasliSpacing.xs))
            choices.forEachIndexed { index, choice ->
                if (index > 0) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 56.dp),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelected(choice) }
                        .padding(horizontal = ZapasliSpacing.sm, vertical = ZapasliSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = selected == choice, onClick = null)
                    Text(label(choice), style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
private fun ThemeMode.label(): String = when (this) {
    ThemeMode.SYSTEM -> stringResource(R.string.theme_system)
    ThemeMode.LIGHT -> stringResource(R.string.theme_light)
    ThemeMode.DARK -> stringResource(R.string.theme_dark)
}

@Composable
private fun LanguageMode.label(): String = when (this) {
    LanguageMode.SYSTEM -> stringResource(R.string.language_system)
    LanguageMode.ENGLISH -> stringResource(R.string.language_english)
    LanguageMode.RUSSIAN -> stringResource(R.string.language_russian)
}

@Composable
private fun PantryFilterPreference.label(): String = when (this) {
    PantryFilterPreference.ALL -> stringResource(R.string.filter_all)
    PantryFilterPreference.EXPIRING_SOON -> stringResource(R.string.filter_expiring_soon)
    PantryFilterPreference.EXPIRED -> stringResource(R.string.filter_expired)
    PantryFilterPreference.NO_DATE -> stringResource(R.string.filter_no_date)
}
