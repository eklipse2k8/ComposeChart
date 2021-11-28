package com.github.eklipse2k8.charting.components

import android.graphics.Paint
import com.github.eklipse2k8.charting.utils.MPPointF
import com.github.eklipse2k8.charting.utils.Utils

/** Created by Philipp Jahoda on 17/09/16. */
class Description : ComponentBase() {
  /** the text used in the description */
  var text = "Description Label"

  /** the custom position of the description text */
  var position: MPPointF? = null
    private set

  /** the alignment of the description text */
  var textAlign = Paint.Align.RIGHT

  init {
    textSize = Utils.convertDpToPixel(8f)
  }

  /**
   * Sets a custom position for the description text in pixels on the screen.
   *
   * @param x: xcoordinate
   * @param y: ycoordinate
   */
  fun setPosition(x: Float, y: Float) {
    if (position == null) {
      position = MPPointF.getInstance(x, y)
    } else {
      position!!.x = x
      position!!.y = y
    }
  }
}
