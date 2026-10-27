package com.example.pocketcalculator.calculator

enum class Operation(val symbol: String) {
    ADD("+"), SUBTRACT("−"), MULTIPLY("×"), DIVIDE("÷")
}
enum class CalculationError { DIVISION_BY_ZERO, OUT_OF_RANGE, INVALID_INPUT }
data class HistoryEntry(val expression: String, val result: String)

/** Immutable state keeps Compose updates predictable and the engine easy to test. */
data class CalculatorState(
    val entry: String = "0",
    val accumulator: String? = null,
    val operation: Operation? = null,
    val waitingForOperand: Boolean = false,
    val resultShown: Boolean = false,
    val replaceEntry: Boolean = false,
    val entryLabel: String? = null,
    val lastExpression: String = "",
    val error: CalculationError? = null,
    val limitReached: Boolean = false,
    val history: List<HistoryEntry> = emptyList(),
) {
    val expression: String
        get() = when {
            operation != null -> buildString {
                append(accumulator ?: "0")
                append(" ${operation.symbol}")
                if (!waitingForOperand) append(" ${entryLabel ?: entry}")
            }
            entryLabel != null -> entryLabel
            else -> lastExpression
        }
}

sealed interface CalculatorAction {
    data class Digit(val value: Int) : CalculatorAction
    data class ChooseOperation(val value: Operation) : CalculatorAction
    data object Decimal : CalculatorAction
    data object Clear : CalculatorAction
    data object Backspace : CalculatorAction
    data object ToggleSign : CalculatorAction
    data object Percent : CalculatorAction
    data object Equals : CalculatorAction
    data object ClearHistory : CalculatorAction
}
