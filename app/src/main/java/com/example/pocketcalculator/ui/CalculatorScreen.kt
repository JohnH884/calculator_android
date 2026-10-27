package com.example.pocketcalculator.ui

import android.app.Activity
import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.pocketcalculator.R
import com.example.pocketcalculator.calculator.CalculationError
import com.example.pocketcalculator.calculator.CalculatorAction
import com.example.pocketcalculator.calculator.CalculatorEngine
import com.example.pocketcalculator.calculator.CalculatorState
import com.example.pocketcalculator.calculator.HistoryEntry
import com.example.pocketcalculator.calculator.Operation

@Composable
fun CalculatorApp() {
    val systemDark = isSystemInDarkTheme()
    var darkTheme by rememberSaveable { mutableStateOf(systemDark) }
    var state by rememberSaveable(stateSaver = CalculatorStateSaver) { mutableStateOf(CalculatorState()) }
    val view = LocalView.current
    SideEffect {
        (view.context as? Activity)?.window?.let { window ->
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }
    CalculatorTheme(darkTheme) {
        CalculatorScreen(state, darkTheme, { darkTheme = it }) {
            state = CalculatorEngine.reduce(state, it)
        }
    }
}

@Composable
fun CalculatorScreen(
    state: CalculatorState, darkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit, onAction: (CalculatorAction) -> Unit,
) {
    var historyVisible by rememberSaveable { mutableStateOf(false) }
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = 20.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            val wide = maxWidth >= 600.dp
            val displayHeight = (maxHeight.value - 650f).coerceIn(158f, 260f).dp
            val keyHeight = (64f * LocalDensity.current.fontScale).coerceAtLeast(64f).dp
            Column(
                modifier = Modifier.widthIn(max = 1000.dp).fillMaxWidth()
                    .verticalScroll(rememberScrollState()).padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                CalculatorHeader(darkTheme, onThemeChange)
                if (wide) {
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            CalculatorDisplay(state, Modifier.fillMaxWidth().heightIn(min = 220.dp))
                            HistoryButton(state.history.size) { historyVisible = true }
                        }
                        CalculatorKeypad(state.operation, keyHeight, onAction, Modifier.weight(1f))
                    }
                } else {
                    CalculatorDisplay(state, Modifier.fillMaxWidth().heightIn(min = displayHeight))
                    HistoryButton(state.history.size) { historyVisible = true }
                    CalculatorKeypad(state.operation, keyHeight, onAction, Modifier.fillMaxWidth())
                }
                Text(stringResource(R.string.percent_hint), style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            }
        }
    }
    if (historyVisible) HistoryDialog(state.history, { historyVisible = false }) {
        onAction(CalculatorAction.ClearHistory)
    }
}

@Composable
private fun CalculatorHeader(darkTheme: Boolean, onThemeChange: (Boolean) -> Unit) {
    val title: @Composable () -> Unit = {
        Text(stringResource(R.string.calculator_title), style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold, modifier = Modifier.semantics { heading() })
    }
    val toggle: @Composable () -> Unit = {
        Row(
            modifier = Modifier.toggleable(value = darkTheme, role = Role.Switch,
                onValueChange = onThemeChange).heightIn(min = 48.dp).padding(start = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(stringResource(R.string.dark_mode), style = MaterialTheme.typography.labelMedium)
            Switch(checked = darkTheme, onCheckedChange = null)
        }
    }
    if (LocalDensity.current.fontScale > 1.2f) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) { title(); toggle() }
    } else {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) { title(); toggle() }
    }
}

@Composable
private fun CalculatorDisplay(state: CalculatorState, modifier: Modifier = Modifier) {
    val scroll = rememberScrollState()
    LaunchedEffect(state.entry, scroll.maxValue) { scroll.scrollTo(scroll.maxValue) }
    val message = when (state.error) {
        CalculationError.DIVISION_BY_ZERO -> stringResource(R.string.divide_by_zero)
        CalculationError.OUT_OF_RANGE -> stringResource(R.string.out_of_range)
        CalculationError.INVALID_INPUT -> stringResource(R.string.invalid_input)
        null -> if (state.limitReached) stringResource(R.string.digit_limit) else ""
    }
    Surface(modifier, shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surfaceContainer) {
        Column(Modifier.padding(18.dp), horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.Bottom)) {
            Text(stringResource(R.string.standard_mode), modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.labelSmall, letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(state.expression.ifEmpty { stringResource(R.string.ready_hint) },
                style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.End,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("expression"))
            Row(Modifier.fillMaxWidth().horizontalScroll(scroll), horizontalArrangement = Arrangement.End) {
                Text(
                    if (state.error != null) stringResource(R.string.error_title) else state.entry,
                    fontSize = 46.sp, lineHeight = 56.sp, fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace, maxLines = 1,
                    color = if (state.error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag("calculator_display").semantics {
                        if (state.resultShown || state.error != null) liveRegion = LiveRegionMode.Polite
                    },
                )
            }
            if (message.isNotEmpty()) {
                Text(message, color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.End,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
            } else {
                Text(stringResource(if (state.resultShown) R.string.result else R.string.current_input),
                    style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private enum class KeyStyle { NUMBER, UTILITY, OPERATOR, EQUALS }
private data class CalculatorKey(
    val label: String, val action: CalculatorAction,
    @StringRes val description: Int? = null, val style: KeyStyle = KeyStyle.NUMBER,
)

private val KeyRows = listOf(
    listOf(
        CalculatorKey("AC", CalculatorAction.Clear, R.string.clear_description, KeyStyle.UTILITY),
        CalculatorKey("±", CalculatorAction.ToggleSign, R.string.sign_description, KeyStyle.UTILITY),
        CalculatorKey("%", CalculatorAction.Percent, R.string.percent_description, KeyStyle.UTILITY),
        CalculatorKey("÷", CalculatorAction.ChooseOperation(Operation.DIVIDE), R.string.divide_description, KeyStyle.OPERATOR),
    ),
    listOf(7, 8, 9).map { CalculatorKey(it.toString(), CalculatorAction.Digit(it)) } +
        CalculatorKey("×", CalculatorAction.ChooseOperation(Operation.MULTIPLY), R.string.multiply_description, KeyStyle.OPERATOR),
    listOf(4, 5, 6).map { CalculatorKey(it.toString(), CalculatorAction.Digit(it)) } +
        CalculatorKey("−", CalculatorAction.ChooseOperation(Operation.SUBTRACT), R.string.subtract_description, KeyStyle.OPERATOR),
    listOf(1, 2, 3).map { CalculatorKey(it.toString(), CalculatorAction.Digit(it)) } +
        CalculatorKey("+", CalculatorAction.ChooseOperation(Operation.ADD), R.string.add_description, KeyStyle.OPERATOR),
    listOf(
        CalculatorKey("⌫", CalculatorAction.Backspace, R.string.delete_description, KeyStyle.UTILITY),
        CalculatorKey("0", CalculatorAction.Digit(0)),
        CalculatorKey(".", CalculatorAction.Decimal, R.string.decimal_description),
        CalculatorKey("=", CalculatorAction.Equals, R.string.equals_description, KeyStyle.EQUALS),
    ),
)

@Composable
private fun CalculatorKeypad(
    operation: Operation?, keyHeight: Dp, onAction: (CalculatorAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        KeyRows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { key ->
                    CalculatorButton(key,
                        selected = operation != null && (key.action as? CalculatorAction.ChooseOperation)?.value == operation,
                        onClick = { onAction(key.action) }, modifier = Modifier.weight(1f).heightIn(min = keyHeight))
                }
            }
        }
    }
}

@Composable
private fun CalculatorButton(key: CalculatorKey, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interactions = remember { MutableInteractionSource() }
    val pressed by interactions.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.94f else 1f, tween(100), label = "Key press")
    val colors = MaterialTheme.colorScheme
    val (background, foreground) = when {
        selected || key.style == KeyStyle.EQUALS -> colors.primary to colors.onPrimary
        key.style == KeyStyle.OPERATOR -> colors.primaryContainer to colors.onPrimaryContainer
        key.style == KeyStyle.UTILITY -> colors.secondaryContainer to colors.onSecondaryContainer
        else -> colors.surfaceContainerHigh to colors.onSurface
    }
    val description = key.description?.let { stringResource(it) } ?: key.label
    Button(onClick = onClick, interactionSource = interactions,
        modifier = modifier.scale(scale).semantics { contentDescription = description },
        shape = RoundedCornerShape(22.dp), contentPadding = PaddingValues(horizontal = 4.dp, vertical = 12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = background, contentColor = foreground)) {
        Text(key.label, fontSize = 26.sp, fontWeight = FontWeight.Medium, maxLines = 1,
            modifier = Modifier.clearAndSetSemantics { })
    }
}

@Composable
private fun HistoryButton(count: Int, onClick: () -> Unit) {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
        OutlinedButton(onClick, modifier = Modifier.heightIn(min = 48.dp)) {
            Text(stringResource(R.string.history_button, count))
        }
    }
}

@Composable
private fun HistoryDialog(history: List<HistoryEntry>, onDismiss: () -> Unit, onClear: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.history_title)) },
        text = {
            Column(Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (history.isEmpty()) Text(stringResource(R.string.history_empty))
                else {
                    Text(stringResource(R.string.history_note), style = MaterialTheme.typography.labelMedium)
                    history.forEach { item ->
                        val spoken = stringResource(R.string.history_accessibility, item.expression, item.result)
                        Column(Modifier.fillMaxWidth().clearAndSetSemantics { contentDescription = spoken },
                            horizontalAlignment = Alignment.End) {
                            Text(item.expression, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.End)
                            Text("= ${item.result}", style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.End)
                        }
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = { TextButton(onDismiss) { Text(stringResource(R.string.close)) } },
        dismissButton = {
            TextButton(onClear, enabled = history.isNotEmpty()) { Text(stringResource(R.string.clear_history)) }
        },
    )
}

@Preview(name = "Light", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
fun CalculatorLightPreview() {
    CalculatorTheme(false) { CalculatorScreen(CalculatorState(), false, {}, {}) }
}
@Preview(name = "Dark", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
fun CalculatorDarkPreview() {
    val sample = CalculatorState(entry = "20", resultShown = true, lastExpression = "200 × 10% =")
    CalculatorTheme(true) { CalculatorScreen(sample, true, {}, {}) }
}
@Preview(name = "Landscape", showBackground = true, widthDp = 800, heightDp = 400)
@Preview(name = "Large text", showBackground = true, widthDp = 360, heightDp = 800, fontScale = 1.5f)
@Composable
fun CalculatorResponsivePreview() {
    CalculatorTheme(false) { CalculatorScreen(CalculatorState(), false, {}, {}) }
}
