package com.example.gymtracker

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min
import android.graphics.Paint
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb

@Composable
fun LineChart(points: List<XYPoint>, xLabels: List<String>, modifier: Modifier = Modifier) {
    if (points.size < 2) return

    val lineColor: Color = MaterialTheme.colorScheme.primary
    val axisColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
    val textColorInt = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f).toArgb()

    val minX = points.minOf { it.x }
    val maxX = points.maxOf { it.x }
    val minY = points.minOf { it.y }
    val maxY = points.maxOf { it.y }

    // Platz für Achsen / Text
    val leftPad = 56f
    val bottomPad = 40f
    val topPad = 10f
    val rightPad = 10f

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(210.dp)
    ) {
        val w = size.width
        val h = size.height

        val chartW = w - leftPad - rightPad
        val chartH = h - topPad - bottomPad

        fun scaleX(x: Float): Float {
            val denom = max(1f, (maxX - minX))
            return leftPad + ((x - minX) / denom) * chartW
        }

        fun scaleY(y: Float): Float {
            val denom = max(1f, (maxY - minY))
            val norm = (y - minY) / denom
            return topPad + (chartH - (norm * chartH))
        }

        // --- Achsen ---
        // y Achse
        drawLine(
            color = axisColor,
            start = Offset(leftPad, topPad),
            end = Offset(leftPad, topPad + chartH),
            strokeWidth = 3f
        )
        // x Achse
        drawLine(
            color = axisColor,
            start = Offset(leftPad, topPad + chartH),
            end = Offset(leftPad + chartW, topPad + chartH),
            strokeWidth = 3f
        )

        // --- Linie ---
        for (i in 0 until points.size - 1) {
            val p1 = Offset(scaleX(points[i].x), scaleY(points[i].y))
            val p2 = Offset(scaleX(points[i + 1].x), scaleY(points[i + 1].y))
            drawLine(
                color = lineColor,
                start = p1,
                end = p2,
                strokeWidth = 4f
            )
        }

        // --- Punkte ---
        points.forEach {
            drawCircle(
                color = lineColor,
                radius = 6f,
                center = Offset(scaleX(it.x), scaleY(it.y))
            )
        }

        // --- Text (nativeCanvas) ---
        val paint = Paint().apply {
            color = textColorInt
            textSize = 28f
            isAntiAlias = true
        }

        // y Labels: max oben, min unten
        drawContext.canvas.nativeCanvas.drawText(
            formatY(maxY),
            6f,
            topPad + 26f,
            paint
        )
        drawContext.canvas.nativeCanvas.drawText(
            formatY(minY),
            6f,
            topPad + chartH,
            paint
        )

        // x Labels: Start / Mitte / Ende
        if (xLabels.isNotEmpty()) {
            val idx0 = 0
            val idxMid = xLabels.size / 2
            val idxLast = xLabels.size - 1

            fun drawXLabel(idx: Int) {
                val safe = idx.coerceIn(0, xLabels.size - 1)
                val x = scaleX(points[safe].x)
                val y = topPad + chartH + 32f
                val text = xLabels[safe]
                val textWidth = paint.measureText(text)

                drawContext.canvas.nativeCanvas.drawText(
                    text,
                    (x - textWidth / 2f).coerceIn(0f, w - textWidth),
                    y,
                    paint
                )
            }

            drawXLabel(idx0)
            if (idxMid != idx0 && idxMid != idxLast) drawXLabel(idxMid)
            if (idxLast != idx0) drawXLabel(idxLast)
        }
    }
}

private fun formatY(v: Float): String {
    // Wenn es wie 10.0 aussieht -> "10", sonst "10.5"
    val rounded = (v * 10f).toInt() / 10f
    return if (rounded % 1f == 0f) rounded.toInt().toString() else rounded.toString()
}


@Composable
fun ScatterChart(points: List<XYPoint>, modifier: Modifier = Modifier) {
    if (points.isEmpty()) return

    val dotColor: Color = MaterialTheme.colorScheme.secondary

    val minX = points.minOf { it.x }
    val maxX = points.maxOf { it.x }
    val minY = points.minOf { it.y }
    val maxY = points.maxOf { it.y }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        val w = size.width
        val h = size.height

        fun scaleX(x: Float): Float {
            val denom = max(1f, (maxX - minX))
            return ((x - minX) / denom) * w
        }

        fun scaleY(y: Float): Float {
            val denom = max(1f, (maxY - minY))
            val norm = (y - minY) / denom
            return h - (norm * h)
        }

        points.forEach { p ->
            drawCircle(
                color = dotColor,
                radius = 6f,
                center = Offset(scaleX(p.x), scaleY(p.y))
            )
        }
    }
}
