package com.github.eklipse2k8.charting.utils

import android.graphics.Color
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.annotation.ColorInt

object ColorPalette {

  @get:ColorInt val GoldenYellow = getColor(0xFFDE03)

  @ColorInt
  private fun getColor(@ColorInt color: Int): Int =
      if (VERSION.SDK_INT >= VERSION_CODES.O) {
        Color.valueOf(color).toArgb()
      } else {
        color
      }
}
