package com.example.pocketcalculator.ui

import androidx.compose.runtime.saveable.listSaver
import com.example.pocketcalculator.calculator.CalculationError
import com.example.pocketcalculator.calculator.CalculatorState
import com.example.pocketcalculator.calculator.HistoryEntry
import com.example.pocketcalculator.calculator.Operation

/** Save only primitive strings; the engine bounds history to avoid an oversized Bundle. */
val CalculatorStateSaver = listSaver<CalculatorState, String>(
    save = { state ->
        listOf(state.entry, state.accumulator.orEmpty(), state.operation?.name.orEmpty(),
            state.waitingForOperand.toString(), state.resultShown.toString(),
            state.replaceEntry.toString(), state.entryLabel.orEmpty(), state.lastExpression,
            state.error?.name.orEmpty(), state.limitReached.toString()) +
            state.history.flatMap { listOf(it.expression, it.result) }
    },
    restore = { saved ->
        runCatching {
            CalculatorState(
                entry = saved[0], accumulator = saved[1].ifEmpty { null },
                operation = saved[2].takeIf { it.isNotEmpty() }?.let(Operation::valueOf),
                waitingForOperand = saved[3].toBoolean(), resultShown = saved[4].toBoolean(),
                replaceEntry = saved[5].toBoolean(), entryLabel = saved[6].ifEmpty { null },
                lastExpression = saved[7],
                error = saved[8].takeIf { it.isNotEmpty() }?.let(CalculationError::valueOf),
                limitReached = saved[9].toBoolean(),
                history = saved.drop(10).chunked(2).map { HistoryEntry(it[0], it[1]) },
            )
        }.getOrElse { CalculatorState() }
    },
)
