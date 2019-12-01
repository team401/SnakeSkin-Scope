package org.snakeskin.scope.client.data

enum class InterpolationMode(val displayName: String) {
    NONE("None"),
    LINEAR("Linear"),
    SMOOTH("Smooth"),
    ROLLING_AVG_1X("1x Average"),
    ROLLING_AVG_2X("2x Average"),
    ROLLING_AVG_4X("4x Average")
}