package com.github.mikephil.charting.utils

/**
 * general number utilities
 *
 * @author Matt Jarjoura
 */

/** Constant value: PI / 180.0f */
private const val DEGREES_TO_RADIANS_F = 0.017453292519943295f

/** guards result from NAN or Infinity */
fun Float.safeguard(): Float =
    if (isInfinite() || isNaN()) {
      0f
    } else {
      this
    }

fun Float.toRadians(): Float = this * DEGREES_TO_RADIANS_F

/** guards result from NAN or Infinity */
fun Double.safeguard(): Double =
    if (isInfinite() || isNaN()) {
      0.0
    } else {
      this
    }
