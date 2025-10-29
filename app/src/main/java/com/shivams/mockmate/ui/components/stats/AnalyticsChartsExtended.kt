package com.shivams.mockmate.ui.components.stats

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shivams.mockmate.model.TestAttempt
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.max

@Composable
fun AccuracyTrendChart(testAttempts: List<TestAttempt>) {
    val sortedAttempts = remember(testAttempts) {
        testAttempts.sortedBy { it.startTime.time }
    }

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
            .height(280.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("📈 Accuracy Trend", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            if (sortedAttempts.size < 2) {
                Text(
                    text = "Need at least 2 test attempts to show a trend.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                NativeAccuracyTrendLineChart(attempts = sortedAttempts)
                val avgScore = sortedAttempts.map { it.score }.average()
                Text(
                    text = "Average: %.1f%% over ${sortedAttempts.size} attempts".format(avgScore),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}


@Composable
fun EngagementTimelineChart(testAttempts: List<TestAttempt>) {

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
            .height(250.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("📅 Engagement Timeline", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            if (testAttempts.isEmpty()) {
                Text(
                    text = "No engagement data available.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                val attemptsByDay = testAttempts.groupBy { attempt ->
                    val cal = Calendar.getInstance().apply {
                        time = attempt.startTime
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    cal.timeInMillis
                }

                val timelineEntries = attemptsByDay.entries.sortedBy { it.key }.takeLast(7)
                NativeEngagementTimelineBarChart(timelineEntries = timelineEntries)

                val totalAttempts = testAttempts.size
                val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
                val firstAttemptDate = testAttempts.minByOrNull { it.startTime.time }?.startTime?.let { dateFormat.format(it) } ?: "N/A"
                val lastAttemptDate = testAttempts.maxByOrNull { it.startTime.time }?.startTime?.let { dateFormat.format(it) } ?: "N/A"

                Text(
                    text = "$totalAttempts tests ($firstAttemptDate - $lastAttemptDate)",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun PerQuestionAnalysisChart(testAttempts: List<TestAttempt>) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
            .height(250.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("📊 Score Distribution", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            if (testAttempts.isEmpty()) {
                Text(
                    text = "No attempts available for analysis.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                val scores = testAttempts.map { it.score }
                val scoreRanges = listOf(0f, 25f, 50f, 75f, 100f)
                val distribution = mutableListOf<Int>()
                for (i in 0 until scoreRanges.size - 1) {
                    val count = scores.count { it >= scoreRanges[i] && it < scoreRanges[i + 1] }
                    distribution.add(count)
                }
                // Manually add count for exactly 100
                distribution[distribution.lastIndex] += scores.count { it == 100f }

                NativePerQuestionAnalysisBarChart(distribution = distribution)

                val avgScore = scores.average()
                val minScore = scores.minOrNull() ?: 0f
                val maxScore = scores.maxOrNull() ?: 0f

                Text(
                    text = "📈 Avg: %.1f%% | 📉 Min: %.1f%% | 📈 Max: %.1f%%".format(avgScore, minScore, maxScore),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}


@Composable
private fun NativeAccuracyTrendLineChart(attempts: List<TestAttempt>) {
    val lineColor = MaterialTheme.colorScheme.primary
    val axisColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    val density = LocalDensity.current
    val labelSize = with(density) { 10.sp.toPx() }
    val dateFormat = remember { SimpleDateFormat("MM/dd", Locale.getDefault()) }

    Canvas(modifier = Modifier.fillMaxSize().padding(top = 16.dp)) {
        val chartPadding = 60f
        val yAxisLabelAreaWidth = 80f
        val xAxisLabelAreaHeight = 100f

        val chartWidth = size.width - yAxisLabelAreaWidth - chartPadding
        val chartHeight = size.height - xAxisLabelAreaHeight - chartPadding

        val labelPaint = Paint().apply {
            color = Color.BLACK
            textAlign = Paint.Align.CENTER
            textSize = labelSize
        }
        val titlePaint = Paint(labelPaint).apply {
            textSize = labelSize * 1.2f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        // Draw axes
        drawLine(axisColor, Offset(yAxisLabelAreaWidth, chartPadding), Offset(yAxisLabelAreaWidth, chartPadding + chartHeight))
        drawLine(axisColor, Offset(yAxisLabelAreaWidth, chartPadding + chartHeight), Offset(yAxisLabelAreaWidth + chartWidth, chartPadding + chartHeight))

        // Y-axis labels
        val maxScore = 100f
        val numYLabels = 5
        for (i in 0..numYLabels) {
            val value = maxScore / numYLabels * i
            val y = chartPadding + chartHeight * (1 - (value / maxScore))
            drawContext.canvas.nativeCanvas.drawText("${value.toInt()}", yAxisLabelAreaWidth - 15f, y + labelSize / 3, labelPaint.apply { textAlign = Paint.Align.RIGHT })
        }

        // X-axis labels and line points
        val xStep = chartWidth / (attempts.size - 1)
        val points = attempts.mapIndexed { index, attempt ->
            val x = yAxisLabelAreaWidth + index * xStep
            val y = chartPadding + chartHeight * (1 - (attempt.score / maxScore))
            Offset(x, y)
        }

        // Draw the line
        for (i in 0 until points.size - 1) {
            drawLine(lineColor, points[i], points[i + 1], strokeWidth = 4f)
        }
        // Draw points on the line
        points.forEach { point ->
            drawCircle(color = lineColor, radius = 8f, center = point)
        }

        // X-axis labels
        attempts.forEachIndexed { index, attempt ->
            val x = yAxisLabelAreaWidth + index * xStep
            val y = chartPadding + chartHeight + 30f
            drawContext.canvas.nativeCanvas.save()
            drawContext.canvas.nativeCanvas.rotate(-45f, x, y)
            drawContext.canvas.nativeCanvas.drawText(dateFormat.format(attempt.startTime), x, y, labelPaint)
            drawContext.canvas.nativeCanvas.restore()
        }

        // Axis Titles
        drawContext.canvas.nativeCanvas.save()
        val yTitleX = chartPadding / 4 + titlePaint.textSize
        val yTitleY = size.height / 2
        drawContext.canvas.nativeCanvas.rotate(-90f, yTitleX, yTitleY)
        drawContext.canvas.nativeCanvas.drawText("Score (%)", yTitleX, yTitleY, titlePaint)
        drawContext.canvas.nativeCanvas.restore()

        drawContext.canvas.nativeCanvas.drawText("Date", yAxisLabelAreaWidth + chartWidth / 2, size.height - chartPadding / 4, titlePaint)
    }
}


@Composable
private fun NativeEngagementTimelineBarChart(timelineEntries: List<Map.Entry<Long, List<TestAttempt>>>) {
    val barColor = MaterialTheme.colorScheme.secondary
    val axisColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    val density = LocalDensity.current
    val labelSize = with(density) { 10.sp.toPx() }
    val dateFormat = remember { SimpleDateFormat("MM/dd", Locale.getDefault()) }

    Canvas(modifier = Modifier.fillMaxSize().padding(top = 16.dp)) {
        val chartPadding = 60f
        val yAxisLabelAreaWidth = 80f
        val xAxisLabelAreaHeight = 60f

        val chartWidth = size.width - yAxisLabelAreaWidth - chartPadding
        val chartHeight = size.height - xAxisLabelAreaHeight - chartPadding

        val labelPaint = Paint().apply {
            color = Color.BLACK
            textAlign = Paint.Align.CENTER
            textSize = labelSize
        }
        val titlePaint = Paint(labelPaint).apply {
            textSize = labelSize * 1.2f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        // Draw axes
        drawLine(axisColor, Offset(yAxisLabelAreaWidth, chartPadding), Offset(yAxisLabelAreaWidth, chartPadding + chartHeight))
        drawLine(axisColor, Offset(yAxisLabelAreaWidth, chartPadding + chartHeight), Offset(yAxisLabelAreaWidth + chartWidth, chartPadding + chartHeight))

        val maxAttempts = max(1f, timelineEntries.maxOfOrNull { it.value.size }?.toFloat() ?: 1f)
        val numYLabels = ceil(maxAttempts).toInt().let { if (it <= 5) it else 5 }

        // Y-axis labels
        for (i in 0..numYLabels) {
            val value = maxAttempts / numYLabels * i
            val y = chartPadding + chartHeight * (1 - (value / maxAttempts))
            drawContext.canvas.nativeCanvas.drawText(value.toInt().toString(), yAxisLabelAreaWidth - 15f, y + labelSize / 3, labelPaint.apply { textAlign = Paint.Align.RIGHT })
        }

        // X-axis labels and bars
        val barWidth = (chartWidth / timelineEntries.size) * 0.7f
        val barSpacing = (chartWidth / timelineEntries.size) * 0.3f
        timelineEntries.forEachIndexed { index, entry ->
            val barLeft = yAxisLabelAreaWidth + index * (barWidth + barSpacing) + barSpacing / 2
            val barHeight = chartHeight * (entry.value.size / maxAttempts)
            drawRect(
                color = barColor,
                topLeft = Offset(barLeft, chartPadding + chartHeight - barHeight),
                size = Size(barWidth, barHeight)
            )

            val x = barLeft + barWidth / 2
            val y = chartPadding + chartHeight + 30f
            drawContext.canvas.nativeCanvas.drawText(dateFormat.format(Date(entry.key)), x, y, labelPaint)
        }

        // Axis Titles
        drawContext.canvas.nativeCanvas.save()
        val yTitleX = chartPadding / 4 + titlePaint.textSize
        val yTitleY = size.height / 2
        drawContext.canvas.nativeCanvas.rotate(-90f, yTitleX, yTitleY)
        drawContext.canvas.nativeCanvas.drawText("Tests Taken", yTitleX, yTitleY, titlePaint)
        drawContext.canvas.nativeCanvas.restore()

        drawContext.canvas.nativeCanvas.drawText("Date (Last 7 Days)", yAxisLabelAreaWidth + chartWidth / 2, size.height - chartPadding / 4, titlePaint)
    }
}


@Composable
private fun NativePerQuestionAnalysisBarChart(distribution: List<Int>) {
    val barColor = MaterialTheme.colorScheme.tertiary
    val axisColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    val density = LocalDensity.current
    val labelSize = with(density) { 10.sp.toPx() }
    val scoreRanges = listOf("0-25%", "25-50%", "50-75%", "75-100%")

    Canvas(modifier = Modifier.fillMaxSize().padding(top = 16.dp)) {
        val chartPadding = 60f
        val yAxisLabelAreaWidth = 80f
        val xAxisLabelAreaHeight = 60f

        val chartWidth = size.width - yAxisLabelAreaWidth - chartPadding
        val chartHeight = size.height - xAxisLabelAreaHeight - chartPadding

        val labelPaint = Paint().apply {
            color = Color.BLACK
            textAlign = Paint.Align.CENTER
            textSize = labelSize
        }
        val titlePaint = Paint(labelPaint).apply {
            textSize = labelSize * 1.2f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        // Draw axes
        drawLine(axisColor, Offset(yAxisLabelAreaWidth, chartPadding), Offset(yAxisLabelAreaWidth, chartPadding + chartHeight))
        drawLine(axisColor, Offset(yAxisLabelAreaWidth, chartPadding + chartHeight), Offset(yAxisLabelAreaWidth + chartWidth, chartPadding + chartHeight))

        val maxCount = max(1f, distribution.maxOrNull()?.toFloat() ?: 1f)
        val numYLabels = ceil(maxCount).toInt().let { if (it <= 5) it else 5 }

        // Y-axis labels
        for (i in 0..numYLabels) {
            val value = maxCount / numYLabels * i
            val y = chartPadding + chartHeight * (1 - (value / maxCount))
            drawContext.canvas.nativeCanvas.drawText(value.toInt().toString(), yAxisLabelAreaWidth - 15f, y + labelSize / 3, labelPaint.apply { textAlign = Paint.Align.RIGHT })
        }

        // X-axis labels and bars
        val barWidth = (chartWidth / scoreRanges.size) * 0.7f
        val barSpacing = (chartWidth / scoreRanges.size) * 0.3f
        distribution.forEachIndexed { index, count ->
            val barLeft = yAxisLabelAreaWidth + index * (barWidth + barSpacing) + barSpacing / 2
            val barHeight = chartHeight * (count / maxCount)
            drawRect(
                color = barColor,
                topLeft = Offset(barLeft, chartPadding + chartHeight - barHeight),
                size = Size(barWidth, barHeight)
            )

            val x = barLeft + barWidth / 2
            val y = chartPadding + chartHeight + 30f
            drawContext.canvas.nativeCanvas.drawText(scoreRanges[index], x, y, labelPaint)
        }

        // Axis Titles
        drawContext.canvas.nativeCanvas.save()
        val yTitleX = chartPadding / 4 + titlePaint.textSize
        val yTitleY = size.height / 2
        drawContext.canvas.nativeCanvas.rotate(-90f, yTitleX, yTitleY)
        drawContext.canvas.nativeCanvas.drawText("Number of Tests", yTitleX, yTitleY, titlePaint)
        drawContext.canvas.nativeCanvas.restore()

        drawContext.canvas.nativeCanvas.drawText("Score Range", yAxisLabelAreaWidth + chartWidth / 2, size.height - chartPadding / 4, titlePaint)
    }
}
