package ru.zapasli.app.ui.foundation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.zapasli.app.R
import ru.zapasli.app.core.designsystem.theme.Green50
import ru.zapasli.app.core.designsystem.theme.Green700
import ru.zapasli.app.core.designsystem.theme.Orange500
import ru.zapasli.app.core.designsystem.theme.ZapasliSpacing
import ru.zapasli.app.core.designsystem.theme.ZapasliTheme

@Composable
fun FoundationScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .safeDrawingPadding()
            .padding(horizontal = ZapasliSpacing.md, vertical = ZapasliSpacing.sm)
            .testTag("foundation_screen"),
        verticalArrangement = Arrangement.spacedBy(ZapasliSpacing.md),
    ) {
        AppHeader()
        AttentionCard()
        ProductCard()
        PreviewNotice()
        Spacer(modifier = Modifier.height(ZapasliSpacing.xs))
    }
}

@Composable
private fun AppHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
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
                text = "Zapasli",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = stringResource(R.string.foundation_subtitle),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun AttentionCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(ZapasliSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(ZapasliSpacing.md),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ZapasliSpacing.sm),
            ) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = Orange500,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "3",
                            color = androidx.compose.ui.graphics.Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.attention_title),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Text(
                        text = stringResource(R.string.attention_description),
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.76f),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {},
            ) {
                Text(stringResource(R.string.open_pantry))
            }
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {},
            ) {
                Text(stringResource(R.string.scan_product))
            }
        }
    }
}

@Composable
private fun ProductCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(ZapasliSpacing.lg)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(Green50),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "M",
                        color = Green700,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                }
                Spacer(modifier = Modifier.width(ZapasliSpacing.sm))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.product_name),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = stringResource(R.string.product_location),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            Spacer(modifier = Modifier.height(ZapasliSpacing.md))
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.errorContainer,
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = ZapasliSpacing.sm, vertical = ZapasliSpacing.xs),
                    text = stringResource(R.string.expiry_soon),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            Spacer(modifier = Modifier.height(ZapasliSpacing.lg))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(ZapasliSpacing.md))
            Text(
                text = stringResource(R.string.nutrition_title),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
            )
            Spacer(modifier = Modifier.height(ZapasliSpacing.sm))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ZapasliSpacing.xs),
            ) {
                MacroMetric(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.energy_label),
                    value = stringResource(R.string.calories_value),
                )
                MacroMetric(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.protein_label),
                    value = stringResource(R.string.protein_value),
                )
                MacroMetric(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.fat_label),
                    value = stringResource(R.string.fat_value),
                )
                MacroMetric(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.carbs_label),
                    value = stringResource(R.string.carbs_value),
                )
            }
        }
    }
}

@Composable
private fun MacroMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = value,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelLarge,
        )
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
private fun PreviewNotice() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Column(modifier = Modifier.padding(ZapasliSpacing.md)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                )
                Spacer(modifier = Modifier.width(ZapasliSpacing.xs))
                Text(
                    text = stringResource(R.string.sync_status),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            Spacer(modifier = Modifier.height(ZapasliSpacing.xs))
            Text(
                text = stringResource(R.string.preview_notice),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Preview(name = "Zapasli light", showBackground = true, locale = "ru")
@Composable
private fun FoundationScreenLightPreview() {
    ZapasliTheme(darkTheme = false) {
        FoundationScreen()
    }
}

@Preview(name = "Zapasli dark", showBackground = true, locale = "ru")
@Composable
private fun FoundationScreenDarkPreview() {
    ZapasliTheme(darkTheme = true) {
        FoundationScreen()
    }
}
