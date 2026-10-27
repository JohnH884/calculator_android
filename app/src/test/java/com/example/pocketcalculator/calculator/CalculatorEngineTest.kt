package com.example.pocketcalculator.calculator

import kotlin.random.Random
import org.junit.Assert.*
import org.junit.Test

class CalculatorEngineTest {
    private fun enter(keys: String, initial: CalculatorState = CalculatorState()): CalculatorState =
        keys.fold(initial) { state, key ->
            val action = when (key) {
                in '0'..'9' -> CalculatorAction.Digit(key.digitToInt())
                '.' -> CalculatorAction.Decimal
                '+' -> CalculatorAction.ChooseOperation(Operation.ADD)
                '-' -> CalculatorAction.ChooseOperation(Operation.SUBTRACT)
                '*' -> CalculatorAction.ChooseOperation(Operation.MULTIPLY)
                '/' -> CalculatorAction.ChooseOperation(Operation.DIVIDE)
                '=' -> CalculatorAction.Equals
                '%' -> CalculatorAction.Percent
                '~' -> CalculatorAction.ToggleSign
                '<' -> CalculatorAction.Backspace
                'C' -> CalculatorAction.Clear
                else -> error("Unknown test key: $key")
            }
            CalculatorEngine.reduce(state, action)
        }

    @Test fun addsMultipleDigitNumbers() { assertEquals("35", enter("25+10=").entry) }
    @Test fun subtractsToNegativeResult() { assertEquals("-8", enter("2-10=").entry) }
    @Test fun multiplies() { assertEquals("56", enter("8*7=").entry) }
    @Test fun divides() { assertEquals("25", enter("100/4=").entry) }
    @Test fun addsDecimalsExactly() { assertEquals("0.3", enter("0.1+0.2=").entry) }
    @Test fun acceptsLeadingDecimalPoint() { assertEquals("1", enter(".5+.5=").entry) }
    @Test fun ignoresRepeatedDecimalPoints() { assertEquals("1.25", enter("1..2.5").entry) }
    @Test fun ignoresLeadingZeros() { assertEquals("123", enter("000123").entry) }
    @Test fun roundsRepeatingDivision() { assertEquals("0.3333333333333333", enter("1/3=").entry) }
    @Test fun acceptsTrailingDecimalPoint() { assertEquals("6", enter("5.+1=").entry) }
    @Test fun percentageKeepsReadableHistory() {
        val state = enter("200*10%=")
        assertEquals("20", state.entry)
        assertEquals(HistoryEntry("200 × 10%", "20"), state.history.single())
    }
    @Test fun standalonePercent() { assertEquals("0.25", enter("25%=").entry) }
    @Test fun additionUsesDocumentedUnaryPercent() { assertEquals("200.1", enter("200+10%=").entry) }
    @Test fun percentWithoutRightOperandIsNoOp() { assertEquals(enter("200*"), enter("200*%")) }
    @Test fun digitReplacesConvertedPercentage() { assertEquals("12", enter("6*10%2=").entry) }
    @Test fun changesSignTwice() { assertEquals("25", enter("25~~").entry) }
    @Test fun signAfterOperatorBeginsNegativeOperand() { assertEquals("-10", enter("5*~2=").entry) }
    @Test fun entersNegativeDecimal() { assertEquals("-0.5", enter("~.5").entry) }
    @Test fun equalsNormalizesNegativeZero() { assertEquals("0", enter("~=").entry) }
    @Test fun deletesOneDigit() { assertEquals("1234", enter("12345<").entry) }
    @Test fun deletionAtZeroIsSafe() { assertEquals("0", enter("<<<<").entry) }
    @Test fun deletionNeverLeavesBareMinus() { assertEquals("0", enter("5~<").entry) }
    @Test fun backspaceCancelsPendingOperator() {
        val state = enter("12+<")
        assertEquals("12", state.entry)
        assertNull(state.operation)
    }
    @Test fun editsResultWithBackspace() { assertEquals("2", enter("12+13=<").entry) }
    @Test fun replacesConsecutiveOperators() { assertEquals("6", enter("8+-2=").entry) }
    @Test fun evaluatesLeftToRight() { assertEquals("20", enter("2+3*4=").entry) }
    @Test fun incompleteEqualsIsNoOp() { assertEquals(enter("12+"), enter("12+=")) }
    @Test fun repeatedEqualsDoesNotDuplicateHistory() { assertEquals(1, enter("2+3====").history.size) }
    @Test fun digitAfterEqualsStartsNewCalculation() { assertEquals("7", enter("2+3=7").entry) }
    @Test fun operationAfterEqualsContinuesResult() { assertEquals("20", enter("2+3=*4=").entry) }
    @Test fun divisionByZeroIsRecoverable() {
        val state = enter("8/0=")
        assertEquals(CalculationError.DIVISION_BY_ZERO, state.error)
        assertTrue(state.history.isEmpty())
        assertEquals("7", enter("7", state).entry)
        assertNull(enter("7", state).error)
        assertEquals(CalculatorState(), enter("C", state))
        assertEquals(CalculatorState(), enter("<", state))
    }
    @Test fun zeroOverZeroReportsError() { assertEquals(CalculationError.DIVISION_BY_ZERO, enter("0/0=").error) }
    @Test fun limitsInputWithoutLosingTheOperand() {
        val state = enter("12345678901234567")
        assertEquals("1234567890123456", state.entry)
        assertTrue(state.limitReached)
        assertEquals("1234567890123457", enter("+1=", state).entry)
    }
    @Test fun clearPreservesHistory() {
        val state = enter("2+3=C")
        assertEquals("0", state.entry)
        assertEquals(1, state.history.size)
    }
    @Test fun clearingHistoryPreservesInput() {
        val state = enter("2+3=12*")
        assertEquals(state.copy(history = emptyList()), CalculatorEngine.reduce(state, CalculatorAction.ClearHistory))
    }
    @Test fun historyIsBoundedAndNewestFirst() {
        var state = CalculatorState()
        repeat(25) { state = enter("C$it+1=", state) }
        assertEquals(20, state.history.size)
        assertEquals("25", state.history.first().result)
        assertEquals("6", state.history.last().result)
    }
    @Test fun reportsOverflowWithoutCrashing() {
        var state = enter("9999999999999999")
        repeat(6) { state = enter("*9999999999999999=", state) }
        assertEquals(CalculationError.OUT_OF_RANGE, state.error)
    }
    @Test fun ignoresInvalidDigitActions() {
        assertEquals(CalculatorState(), CalculatorEngine.reduce(CalculatorState(), CalculatorAction.Digit(12)))
    }
    @Test fun randomNormalInputPreservesValidState() {
        val random = Random(42)
        val keys = "0123456789.+-*/=%~<C"
        var state = CalculatorState()
        repeat(10_000) {
            state = enter(keys[random.nextInt(keys.length)].toString(), state)
            assertTrue(state.entry.isNotEmpty())
            assertNotNull(state.entry.toBigDecimalOrNull())
            assertTrue(state.history.size <= CalculatorEngine.MAX_HISTORY)
            assertEquals(state.operation == null, state.accumulator == null)
        }
    }
}
