package com.github.mikephil.charting.model

import com.github.mikephil.charting.utils.Fill

/** Deprecated. Use `Fill` */
@Deprecated("Use Fill()")
class GradientColor : Fill() {

  var startColor: Int
    get() = gradientColors[0]
    set(startColor) {
      if (gradientColors.size != 2) {
        gradientColors =
            intArrayOf(startColor, if (gradientColors.size > 1) gradientColors[1] else 0)
      } else {
        gradientColors[0] = startColor
      }
    }

  var endColor: Int
    get() = gradientColors[1]
    set(endColor) {
      if (gradientColors.size != 2) {
        gradientColors =
            intArrayOf(if (gradientColors.isNotEmpty()) gradientColors[0] else 0, endColor)
      } else {
        gradientColors[1] = endColor
      }
    }
}
