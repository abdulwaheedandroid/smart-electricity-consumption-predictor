package com.abdulwaheed.smartelectricitypredictor.domain.calculator

import com.abdulwaheed.smartelectricitypredictor.domain.model.ConsumptionTrend
import com.abdulwaheed.smartelectricitypredictor.domain.model.HistoricalConsumption
import com.abdulwaheed.smartelectricitypredictor.domain.model.TrendDirection

/** Compares actual historical usage only; current appliance estimates are not inputs. */
object HistoricalTrendCalculator {
    /** Returns newest-first trends, regardless of input order. Missing months remain missing. */
    fun calculateTrends(records: List<HistoricalConsumption>): List<ConsumptionTrend> {
        records.forEach { validatePeriod(it.month, it.year) }
        val byPeriod = records.associateBy { it.year to it.month }
        require(byPeriod.size == records.size) { "Only one historical record per month is allowed." }

        return records.sortedWith(
            compareByDescending<HistoricalConsumption> { it.year }.thenByDescending { it.month }
        ).map { current ->
            val previousYear = if (current.month == 1) current.year - 1 else current.year
            val previousMonth = if (current.month == 1) 12 else current.month - 1
            val previous = byPeriod[previousYear to previousMonth]
            calculateTrend(current.month, current.year, current.unitsConsumed, previous?.unitsConsumed)
        }
    }

    /**
     * The caller must supply units from the immediately preceding calendar month,
     * or null if that month's data is missing. calculateTrends performs this lookup.
     * Null, negative, or non-finite units are treated as unavailable.
     */
    fun calculateTrend(
        month: Int,
        year: Int,
        unitsConsumed: Double?,
        previousUnitsConsumed: Double?
    ): ConsumptionTrend {
        validatePeriod(month, year)
        val current = unitsConsumed?.takeIf { it.isFinite() && it >= 0.0 }
        val previous = previousUnitsConsumed?.takeIf { it.isFinite() && it >= 0.0 }
        if (current == null || previous == null) {
            return ConsumptionTrend(month, year, current, previous, null, null, TrendDirection.NOT_AVAILABLE)
        }

        val change = current - previous
        val direction = when {
            current > previous -> TrendDirection.INCREASED
            current < previous -> TrendDirection.DECREASED
            else -> TrendDirection.UNCHANGED
        }
        val percentage = when {
            change == 0.0 -> 0.0
            previous == 0.0 -> null
            else -> ((change / previous) * 100.0).takeIf { it.isFinite() }
        }
        return ConsumptionTrend(month, year, current, previous, change, percentage, direction)
    }

    private fun validatePeriod(month: Int, year: Int) {
        require(month in 1..12) { "Month must be between 1 and 12." }
        require(year in 1..9999) { "Year must be between 1 and 9999." }
    }
}
