package com.github.eklipse2k8.charting.utils

import android.graphics.DashPathEffect

class DashedLine {
  private var dashPathEffect: DashPathEffect? = null

  fun set(lineLength: Float, spaceLength: Float, phase: Float) {
    dashPathEffect = DashPathEffect(floatArrayOf(lineLength, spaceLength), phase)
  }

  val get: DashPathEffect?
    get() = dashPathEffect
}
