package com.shivams.mockmate.ui.components.stats

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
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
// import androidx.compose.runtime.remember // Removed remember as Paint objects are now in onDraw
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shivams.mockmate.model.TestAttempt
import com.shivams.mockmate.model.UserStats
import kotlin.math.ceil
import kotlin.math.roundToInt

@Composable
fun AvgTimeSpentChart(userStats: UserStats) {
    // Debug logging
    Log.d("AvgTimeSpentChart", "UserStats received: questionsAnswered=${userStats.questionsAnswered}, subjectPerformance=${userStats.subjectPerformance.size}")

    Card(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📊 Time Management Insights", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            if (userStats.questionsAnswered == 0) {
                Text(
                    text = "Start small today. Even 15 mins counts. ⏱️",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "💡 Complete practice tests to see your time management insights",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val avgQuestions = if (userStats.subjectPerformance.isNotEmpty()) {
                    userStats.questionsAnswered.toFloat() / userStats.subjectPerformance.size
                } else 0f

                val accuracy = if (userStats.questionsAnswered > 0) {
                    (userStats.correctAnswers.toFloat() / userStats.questionsAnswered.toFloat()) * 100
                } else 0f

                Text("Questions answered: ${userStats.questionsAnswered}", style = MaterialTheme.typography.bodyMedium)
                Text("Avg. per subject: %.1f".format(avgQuestions), style = MaterialTheme.typography.bodyMedium)
                Text("Current accuracy: %.1f%%".format(accuracy), style = MaterialTheme.typography.bodyMedium)

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "📈 Focus on quality over speed - accuracy matters more than time!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
fun StreakTrackerChart(userStats: UserStats) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🔥 Streak Tracker", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Current Streak: ${userStats.currentStreak} days",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Longest Streak: ${userStats.longestStreak} days",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "(Visual for streak progress needed)",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}


@Composable
fun NativeTestAttemptsMilestoneChart(
    testAttemptsCount: Int,
    milestones: List<Int>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.secondary, // Used for Canvas.drawRect
    axisLabelColor: Color = MaterialTheme.colorScheme.onSurface, // Used for Paint text color
    axisLineColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), // Used for Canvas.drawLine
    chartTitle: String = "Test Attempts Milestones"
) {
    val density = LocalDensity.current
    val labelSizePx = with(density) { 10.sp.toPx() }
    val titleSizePx = with(density) { 12.sp.toPx() }
    val axisTitleSizePx = with(density) { 10.sp.toPx() }

    val yAxisLabelAreaWidth = with(density) { 40.dp.toPx() }
    val xAxisLabelAreaHeight = with(density) { 50.dp.toPx() } // Increased for rotated labels
    val chartPadding = with(density) { 16.dp.toPx() }

    // Resolve colors for Paint objects to ARGB Ints outside the Canvas scope
    val resolvedAxisLabelColorArgb = axisLabelColor.toArgb()
    val resolvedPrimaryColorArgb = MaterialTheme.colorScheme.primary.toArgb() // For titlePaint

    Box(modifier = modifier.padding(8.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Initialize Paint objects directly inside Canvas onDraw scope
            val textPaint = Paint().asFrameworkPaint().apply {
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER
                color = resolvedAxisLabelColorArgb // Use pre-resolved ARGB color
                // textSize will be set before each use
            }

            val titlePaint = Paint().asFrameworkPaint().apply {
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER
                color = resolvedPrimaryColorArgb // Use pre-resolved ARGB color
                textSize = titleSizePx
                isFakeBoldText = true
            }

            val axisTitlePaint = Paint().asFrameworkPaint().apply {
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER
                color = resolvedAxisLabelColorArgb // Use pre-resolved ARGB color
                textSize = axisTitleSizePx
            }

            val chartWidth = size.width - yAxisLabelAreaWidth - chartPadding * 2
            val chartHeight = size.height - xAxisLabelAreaHeight - chartPadding * 2 - titlePaint.textSize * 2

            drawContext.canvas.nativeCanvas.drawText(
                chartTitle,
                size.width / 2,
                chartPadding + titlePaint.textSize / 2,
                titlePaint
            )

            val maxMilestone = milestones.maxOrNull() ?: 1
            val barWidth = chartWidth / (milestones.size * 2 -1)

            val numYLabels = 5
            val yStep = ceil((maxMilestone.toFloat()) / (numYLabels -1 )).coerceAtLeast(1f)

            for (i in 0 until numYLabels) {
                val value = (i * yStep).roundToInt().coerceAtMost(maxMilestone)
                val y = chartPadding + titlePaint.textSize *2 + chartHeight - (value.toFloat() / maxMilestone) * chartHeight
                
                textPaint.textSize = labelSizePx
                textPaint.textAlign = android.graphics.Paint.Align.RIGHT
                drawContext.canvas.nativeCanvas.drawText(
                    value.toString(),
                    yAxisLabelAreaWidth - chartPadding /2,
                    y + textPaint.textSize / 3, 
                    textPaint
                )
                drawLine(
                    color = axisLineColor.copy(alpha=0.3f), // Use original Composable Color
                    start = Offset(yAxisLabelAreaWidth, y),
                    end = Offset(yAxisLabelAreaWidth + chartWidth, y),
                    strokeWidth = 1f
                )
            }

            milestones.forEachIndexed { index, milestone ->
                val barHeightValue = if (testAttemptsCount >= milestone) milestone.toFloat() else 0f
                val barActualHeight = (barHeightValue / maxMilestone) * chartHeight
                val barX = yAxisLabelAreaWidth + index * (barWidth * 1.5f) + barWidth /2
                
                if (barActualHeight > 0) {
                     drawRect(
                        color = barColor, // Use original Composable Color
                        topLeft = Offset(barX, chartPadding + titlePaint.textSize*2 + chartHeight - barActualHeight),
                        size = Size(barWidth, barActualHeight)
                    )
                }

                val labelX = barX + barWidth / 2
                val labelY = chartPadding + titlePaint.textSize*2 + chartHeight + xAxisLabelAreaHeight / 2

                drawContext.canvas.nativeCanvas.save()
                textPaint.textSize = labelSizePx
                textPaint.textAlign = android.graphics.Paint.Align.CENTER
                drawContext.canvas.nativeCanvas.rotate(-45f, labelX, labelY - textPaint.textSize)
                 drawContext.canvas.nativeCanvas.drawText(
                    milestone.toString(),
                    labelX,
                    labelY,
                    textPaint
                )
                drawContext.canvas.nativeCanvas.restore()
            }

            drawLine(
                color = axisLineColor, // Use original Composable Color
                start = Offset(yAxisLabelAreaWidth, chartPadding + titlePaint.textSize*2),
                end = Offset(yAxisLabelAreaWidth, chartPadding + titlePaint.textSize*2 + chartHeight),
                strokeWidth = 2f
            )

            drawLine(
                color = axisLineColor, // Use original Composable Color
                start = Offset(yAxisLabelAreaWidth, chartPadding + titlePaint.textSize*2 + chartHeight),
                end = Offset(yAxisLabelAreaWidth + chartWidth, chartPadding + titlePaint.textSize*2 + chartHeight),
                strokeWidth = 2f
            )

            drawContext.canvas.nativeCanvas.save()
            val yTitleX = chartPadding /2
            val yTitleY = chartPadding + titlePaint.textSize*2 + chartHeight / 2
            axisTitlePaint.textAlign = android.graphics.Paint.Align.CENTER
            drawContext.canvas.nativeCanvas.rotate(-90f, yTitleX, yTitleY)
            drawContext.canvas.nativeCanvas.drawText(
                "Tests Completed",
                yTitleX,
                yTitleY + axisTitlePaint.textSize / 3,
                axisTitlePaint
            )
            drawContext.canvas.nativeCanvas.restore()

            axisTitlePaint.textAlign = android.graphics.Paint.Align.CENTER
            drawContext.canvas.nativeCanvas.drawText(
                "Milestone",
                yAxisLabelAreaWidth + chartWidth / 2,
                size.height - chartPadding / 2 ,
                axisTitlePaint
            )
        }
    }
}


@Composable
fun TestAttemptsCounterChart(testAttempts: List<TestAttempt>) {
    val milestones = listOf(1, 5, 10, 20, 50, 100)

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
            .height(280.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            NativeTestAttemptsMilestoneChart(
                testAttemptsCount = testAttempts.size,
                milestones = milestones,
                modifier = Modifier.fillMaxSize(),
                chartTitle = "📈 Test Journey Milestones"
                // Default colors will be used from MaterialTheme now correctly resolved
            )

            Text(
                text = "You've completed ${testAttempts.size} tests! Next milestone: ${milestones.firstOrNull { it > testAttempts.size } ?: "Max"}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

// ChartPlaceholder Composable (already defined in AnalyticsChartsCore.kt, ensure it's accessible or duplicate if not)
/*
@Composable
fun ChartPlaceholder(title: String, description: String = "Chart data will be shown here.") {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
            .height(150.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
*/
