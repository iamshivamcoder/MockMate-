package com.example.mockmate.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mockmate.model.SubjectPerformance
import com.example.mockmate.model.TestAttempt
import com.example.mockmate.model.UserStats
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.min

@Composable
fun ChartPlaceholder(title: String, description: String) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Chart: $description",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "(Note: Implement a charting library for a visual representation)",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
fun OverallAccuracyChart(userStats: UserStats) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Overall Accuracy", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Correct vs Incorrect across all attempts.",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (userStats.questionsAnswered > 0) {
                val correctPercentage = (userStats.correctAnswers.toFloat() / userStats.questionsAnswered.toFloat()) * 100
                val incorrectPercentage = 100 - correctPercentage

                NativeOverallAccuracyPieChart(
                    correctPercentage = correctPercentage,
                    incorrectPercentage = incorrectPercentage,
                    modifier = Modifier
                        .size(200.dp) // Adjust size as needed
                        .padding(8.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(colorScheme.primary, CircleShape)
                    )
                    Text(" Correct (%.1f%%)".format(correctPercentage), style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(start = 4.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(colorScheme.error, CircleShape)
                    )
                    Text(" Incorrect (%.1f%%)".format(incorrectPercentage), style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(start = 4.dp))
                }

            } else {
                Text(
                    text = "📊 No overall accuracy data available.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "💡 Complete tests to see your accuracy.",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Total Answered: ${userStats.questionsAnswered}",
                style = MaterialTheme.typography.bodyMedium
            )
            val accuracy = if (userStats.questionsAnswered > 0) {
                (userStats.correctAnswers.toFloat() / userStats.questionsAnswered.toFloat()) * 100
            } else {
                0f
            }
            Text(
                text = "Accuracy: ${if (userStats.questionsAnswered > 0) "%.2f%%".format(accuracy) else "No data"}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun NativeOverallAccuracyPieChart(
    correctPercentage: Float,
    incorrectPercentage: Float,
    modifier: Modifier = Modifier
) {
    val correctColor = colorScheme.primary
    val incorrectColor = colorScheme.error
    val backgroundIndicatorColor = colorScheme.surfaceVariant

    Canvas(modifier = modifier) {
        val diameter = min(size.width, size.height)
        val strokeWidth = diameter * 0.2f // Adjust for donut thickness
        val radius = (diameter - strokeWidth) / 2
        val topLeft = Offset((size.width - diameter) / 2 + strokeWidth/2, (size.height - diameter) / 2 + strokeWidth/2)
        val arcSize = Size(diameter - strokeWidth, diameter- strokeWidth)

        // Background indicator (full circle)
        drawArc(
            color = backgroundIndicatorColor,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth)
        )

        // Correct slice
        drawArc(
            color = correctColor,
            startAngle = -90f, // Start from the top
            sweepAngle = (correctPercentage / 100f) * 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth)
        )
    }
}

@Composable
fun TestScoresOverTimeChart(testAttempts: List<TestAttempt>) {
    val sortedAttempts = remember(testAttempts) {
        testAttempts.filter { it.isCompleted }.sortedBy { it.startTime.time }
    }

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
            .height(300.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "📈 Test Scores Over Time",
                style = MaterialTheme.typography.titleMedium,
                color = colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (sortedAttempts.isEmpty()) { // Changed from < 2 to isEmpty for consistency with placeholder
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "📈 No test scores available to display",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurface
                    )
                }
            } else if (sortedAttempts.size < 2) {
                 Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "📈 Need at least 2 test attempts to show trend.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurface
                    )
                 }
            }
            else {
                 NativeTestScoresLineChart(
                    testAttempts = sortedAttempts,
                    modifier = Modifier.fillMaxSize().padding(top = 16.dp)
                )
            }
        }
    }
}

@Composable
fun NativeTestScoresLineChart(
    testAttempts: List<TestAttempt>,
    modifier: Modifier = Modifier
) {
    val lineColor = colorScheme.primary
    val pointColor = colorScheme.secondary
    val axisColor = colorScheme.onSurface.copy(alpha = 0.6f)
    val textColor = colorScheme.onSurface

    val density = LocalDensity.current
    val labelTextSizePx = with(density) { 10.sp.toPx() }
    val titleTextSizePx = with(density) { 12.sp.toPx() }


    val textPaint = remember {
        android.graphics.Paint().apply {
            this.color = textColor.hashCode()
            textAlign = android.graphics.Paint.Align.CENTER
            textSize = labelTextSizePx
        }
    }
     val titlePaint = remember {
        android.graphics.Paint().apply {
            this.color = textColor.hashCode()
            textAlign = android.graphics.Paint.Align.CENTER
            textSize = titleTextSizePx
            isFakeBoldText = true
        }
    }

    // No need for a separate isEmpty check here as TestScoresOverTimeChart handles it.
    // The size < 2 check is for drawing a line, which is appropriate here.
    if (testAttempts.size < 2) { 
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            // This message is now less likely to be seen due to checks in TestScoresOverTimeChart
            Text("Need at least 2 test attempts for a line chart", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    val dateFormat = remember { SimpleDateFormat("MMM dd", Locale.getDefault()) }

    Canvas(modifier = modifier) {
        val chartPadding = 40.dp.toPx()
        val yAxisLabelAreaWidth = 40.dp.toPx()
        val xAxisLabelAreaHeight = 40.dp.toPx()
        
        // Adjust chart area for titles
        val yTitleAreaWidth = titleTextSizePx + 8.dp.toPx() 
        val xTitleAreaHeight = titleTextSizePx + 8.dp.toPx()

        val chartWidth = size.width - yAxisLabelAreaWidth - chartPadding / 2 - yTitleAreaWidth
        val chartHeight = size.height - xAxisLabelAreaHeight - chartPadding / 2 - xTitleAreaHeight
        
        val effectiveCanvasLeftPadding = yTitleAreaWidth + yAxisLabelAreaWidth


        val maxScore = 100f 

        // Draw Y-axis line
        drawLine(
            color = axisColor,
            start = Offset(effectiveCanvasLeftPadding, chartPadding / 2),
            end = Offset(effectiveCanvasLeftPadding, chartHeight + chartPadding / 2),
            strokeWidth = 1.dp.toPx()
        )

        // Draw X-axis line
        drawLine(
            color = axisColor,
            start = Offset(effectiveCanvasLeftPadding, chartHeight + chartPadding / 2),
            end = Offset(effectiveCanvasLeftPadding + chartWidth, chartHeight + chartPadding / 2),
            strokeWidth = 1.dp.toPx()
        )

        // Y-axis labels and grid lines
        val numYLabels = 5
        for (i in 0..numYLabels) {
            val value = maxScore * i / numYLabels
            val yPos = chartHeight + chartPadding / 2 - (value / maxScore) * chartHeight
            drawContext.canvas.nativeCanvas.drawText(
                "${value.toInt()}%",
                effectiveCanvasLeftPadding - 8.dp.toPx(),
                yPos + textPaint.textSize / 3,
                textPaint.apply { textAlign = android.graphics.Paint.Align.RIGHT }
            )
             drawLine(
                color = axisColor.copy(alpha = 0.3f),
                start = Offset(effectiveCanvasLeftPadding, yPos),
                end = Offset(effectiveCanvasLeftPadding + chartWidth, yPos),
                strokeWidth = 0.5.dp.toPx()
            )
        }

        // Prepare points for line chart
        val points = testAttempts.mapIndexed { index, attempt ->
            // Ensure testAttempts.size - 1 is not zero if testAttempts.size is 1 (though guarded by size < 2 check)
            val xDenominator = if (testAttempts.size > 1) (testAttempts.size - 1).toFloat() else 1f
            val x = effectiveCanvasLeftPadding + (index.toFloat() / xDenominator) * chartWidth
            val y = chartHeight + chartPadding / 2 - (attempt.score.toFloat() / maxScore) * chartHeight
            Offset(x, y)
        }

        // Draw line
        val path = Path()
        points.forEachIndexed { index, point ->
            if (index == 0) {
                path.moveTo(point.x, point.y)
            } else {
                path.lineTo(point.x, point.y)
            }
        }
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 2.dp.toPx())
        )

        // Draw points and X-axis labels
        points.forEachIndexed { index, point ->
            drawCircle(
                color = pointColor,
                radius = 4.dp.toPx(),
                center = point
            )

            val xLabelPointsCountThreshold = 5 
            val xLabelModuloFactor = (testAttempts.size / xLabelPointsCountThreshold).coerceAtLeast(1)

            if (testAttempts.size <= xLabelPointsCountThreshold || index % xLabelModuloFactor == 0) {
                 val dateLabel = dateFormat.format(Date(testAttempts[index].startTime.time))
                 val labelX = point.x
                 val labelY = chartHeight + chartPadding / 2 + 10.dp.toPx() + textPaint.textSize
                 
                 drawContext.canvas.nativeCanvas.save()
                 drawContext.canvas.nativeCanvas.rotate(-45f, labelX, labelY - textPaint.textSize/2) 
                 drawContext.canvas.nativeCanvas.drawText(
                    dateLabel,
                    labelX,
                    labelY,
                    textPaint.apply { textAlign = android.graphics.Paint.Align.RIGHT } // Align right for rotated labels
                )
                drawContext.canvas.nativeCanvas.restore()
            }
        }
         // Axis Titles
        // Y-Axis Title ("Score (%)")
        val yTitleX = yTitleAreaWidth / 2f
        val yTitleY = chartPadding / 2 + chartHeight / 2
        drawContext.canvas.nativeCanvas.save()
        drawContext.canvas.nativeCanvas.rotate(-90f, yTitleX, yTitleY)
        drawContext.canvas.nativeCanvas.drawText(
            "Score (%)",
            yTitleX,
            yTitleY + titlePaint.textSize / 3f,
            titlePaint
        )
        drawContext.canvas.nativeCanvas.restore()

        // X-Axis Title ("Date")
        val xTitleX = effectiveCanvasLeftPadding + chartWidth / 2
        val xTitleY = chartHeight + chartPadding/2 + xAxisLabelAreaHeight - xTitleAreaHeight/2f + titlePaint.textSize/2
        drawContext.canvas.nativeCanvas.drawText(
            "Date",
            xTitleX,
            xTitleY,
            titlePaint
        )
    }
}
