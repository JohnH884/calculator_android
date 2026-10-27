package com.example.pocketcalculator.calculator

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

/** Standard calculator: operations execute left to right, without expression precedence. */
object CalculatorEngine {
    const val MAX_INPUT_DIGITS = 16
    const val MAX_HISTORY = 20
    private val precision = MathContext(16, RoundingMode.HALF_EVEN)
    private val largestValue = BigDecimal("1E99")
    private val smallestValue = BigDecimal("1E-99")

    fun reduce(state: CalculatorState, action: CalculatorAction): CalculatorState {
        if (action == CalculatorAction.ClearHistory) return state.copy(history = emptyList())
        if (action == CalculatorAction.Clear) return CalculatorState(history = state.history)
        val current = if (state.error != null) {
            when (action) {
                is CalculatorAction.Digit, CalculatorAction.Decimal,
                CalculatorAction.Backspace -> CalculatorState(history = state.history)
                else -> return state
            }
        } else state.copy(limitReached = false)
        return try {
            when (action) {
                is CalculatorAction.Digit -> enterDigit(current, action.value)
                is CalculatorAction.ChooseOperation -> chooseOperation(current, action.value)
                CalculatorAction.Decimal -> enterDecimal(current)
                CalculatorAction.Backspace -> deleteLast(current)
                CalculatorAction.ToggleSign -> toggleSign(current)
                CalculatorAction.Percent -> percent(current)
                CalculatorAction.Equals -> calculate(current, recordHistory = true)
                CalculatorAction.Clear, CalculatorAction.ClearHistory -> current
            }
        } catch (_: NumberFormatException) {
            current.copy(error = CalculationError.INVALID_INPUT)
        } catch (_: ArithmeticException) {
            current.copy(error = CalculationError.OUT_OF_RANGE)
        }
    }

    private fun prepareEntry(state: CalculatorState): CalculatorState = when {
        state.resultShown -> CalculatorState(history = state.history)
        state.waitingForOperand || state.replaceEntry -> state.copy(
            entry = "0", waitingForOperand = false, replaceEntry = false,
            entryLabel = null, lastExpression = "",
        )
        else -> state.copy(lastExpression = "", entryLabel = null)
    }

    private fun enterDigit(state: CalculatorState, digit: Int): CalculatorState {
        if (digit !in 0..9) return state
        val ready = prepareEntry(state)
        val candidate = when (ready.entry) {
            "0" -> digit.toString()
            "-0" -> "-$digit"
            else -> ready.entry + digit
        }
        if (candidate.count { it.isDigit() } > MAX_INPUT_DIGITS) return state.copy(limitReached = true)
        return ready.copy(entry = candidate)
    }

    private fun enterDecimal(state: CalculatorState): CalculatorState {
        val ready = prepareEntry(state)
        return if ('.' in ready.entry) ready else ready.copy(entry = ready.entry + ".")
    }

    private fun deleteLast(state: CalculatorState): CalculatorState {
        // Backspace after an operator cancels that operator, leaving the left operand.
        if (state.waitingForOperand) return state.copy(
            accumulator = null, operation = null, waitingForOperand = false,
            resultShown = false, replaceEntry = false, entryLabel = null, lastExpression = "",
        )
        val shortened = state.entry.dropLast(1).takeUnless { it.isEmpty() || it == "-" } ?: "0"
        return state.copy(entry = shortened, resultShown = false, replaceEntry = false,
            entryLabel = null, lastExpression = "")
    }

    private fun toggleSign(state: CalculatorState): CalculatorState {
        val ready = if (state.waitingForOperand) state.copy(entry = "0") else state
        return ready.copy(
            entry = if (ready.entry.startsWith("-")) ready.entry.drop(1) else "-${ready.entry}",
            waitingForOperand = false, resultShown = false, replaceEntry = false,
            entryLabel = null, lastExpression = "",
        )
    }

    private fun percent(state: CalculatorState): CalculatorState {
        if (state.waitingForOperand) return state
        // Unary percent divides the active number by 100 for every operator.
        val result = checkedFormat(state.entry.toBigDecimal().movePointLeft(2).round(precision))
        return state.copy(entry = result, entryLabel = "${state.entry}%", replaceEntry = true,
            resultShown = false, lastExpression = "")
    }

    private fun chooseOperation(state: CalculatorState, operation: Operation): CalculatorState {
        val resolved = if (state.operation != null && !state.waitingForOperand) {
            calculate(state, recordHistory = false)
        } else state
        if (resolved.error != null) return resolved
        return resolved.copy(
            accumulator = checkedFormat(resolved.entry.toBigDecimal()), operation = operation,
            waitingForOperand = true, resultShown = false, replaceEntry = false,
            entryLabel = null, lastExpression = "",
        )
    }

    private fun calculate(state: CalculatorState, recordHistory: Boolean): CalculatorState {
        // Incomplete input and repeated equals are deliberate no-ops.
        if (state.waitingForOperand || state.resultShown) return state
        val right = state.entry.toBigDecimal()
        val left = state.accumulator?.toBigDecimal()
        val result = when (state.operation) {
            Operation.ADD -> requireNotNull(left).add(right, precision)
            Operation.SUBTRACT -> requireNotNull(left).subtract(right, precision)
            Operation.MULTIPLY -> requireNotNull(left).multiply(right, precision)
            Operation.DIVIDE -> {
                if (right.signum() == 0) return state.copy(error = CalculationError.DIVISION_BY_ZERO)
                requireNotNull(left).divide(right, precision)
            }
            null -> right
        }
        val formatted = checkedFormat(result)
        val expression = if (state.operation != null) state.expression else state.entryLabel ?: state.entry
        val history = if (recordHistory && (state.operation != null || state.entryLabel != null)) {
            (listOf(HistoryEntry(expression, formatted)) + state.history).take(MAX_HISTORY)
        } else state.history
        return CalculatorState(entry = formatted, resultShown = true,
            lastExpression = "$expression =", history = history)
    }

    private fun checkedFormat(value: BigDecimal): String {
        val magnitude = value.abs()
        if (magnitude > largestValue || (magnitude.signum() != 0 && magnitude < smallestValue)) {
            throw ArithmeticException("Result is outside the supported range")
        }
        return if (value.signum() == 0) "0" else value.stripTrailingZeros().toPlainString()
    }
}
