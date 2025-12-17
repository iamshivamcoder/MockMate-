package com.shivams.mockmate.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Centralized date formatting utilities to avoid scattered SimpleDateFormat instances.
 */
object DateFormatUtils {
    
    private val testHistoryFormatter = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())
    private val welcomeDateFormatter = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
    private val chartDateLongFormatter = SimpleDateFormat("MMM dd", Locale.getDefault())
    private val chartDateShortFormatter = SimpleDateFormat("MM/dd", Locale.getDefault())

    /**
     * Formats date for test history display.
     * Example: "Jan 15, 2024 at 3:30 PM"
     */
    fun formatTestHistoryDate(date: Date): String {
        return testHistoryFormatter.format(date)
    }

    /**
     * Formats date for welcome card display.
     * Example: "Mon, Jan 15, 2024"
     */
    fun formatWelcomeDate(date: Date): String {
        return welcomeDateFormatter.format(date)
    }

    /**
     * Formats date for analytics charts (long format).
     * Example: "Jan 15"
     */
    fun formatChartDateLong(date: Date): String {
        return chartDateLongFormatter.format(date)
    }

    /**
     * Formats date for analytics charts (short format).
     * Example: "01/15"
     */
    fun formatChartDateShort(date: Date): String {
        return chartDateShortFormatter.format(date)
    }
}
