package com.example.habitflow.repository

import kotlin.math.roundToInt

fun generateGoalDataForFirestore(
	duration: Int,
	end: Float,
	precision: String
): List<Map<String, Any>> {
	val step = end / (duration - 1)
	return (0 until duration).map { i ->
		val x = i.toFloat()
		val yRaw = i * step
		val y = when (precision) {
			"tenths" -> (yRaw * 10).roundToInt() / 10f
			"hundredths" -> (yRaw * 100).roundToInt() / 100f
			else -> yRaw.roundToInt().toFloat()
		}
		mapOf("x" to x, "y" to y)
	}
}