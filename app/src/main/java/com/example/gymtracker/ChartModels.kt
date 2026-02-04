package com.example.gymtracker

enum class ChartMode(val label: String) {
    WEIGHT_OVER_TIME("kg über Zeit"),
    REPS_OVER_TIME("Wdh über Zeit")
}

data class XYPoint(
    val x: Float,
    val y: Float
)

data class ChartSeries(
    val mode: ChartMode,
    val points: List<XYPoint>,
    val xLabels: List<String> = emptyList()
)
