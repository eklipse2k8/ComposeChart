package com.github.eklipse2k8.charting.components

import android.graphics.Paint.Align
import android.graphics.Paint.Align.RIGHT
import com.github.eklipse2k8.charting.utils.MPPointF
import com.github.eklipse2k8.charting.utils.Utils

/**
 * @property text the text used in the description
 * @property position the custom position of the description text
 * @property textAlign the alignment of the description text
 *
 * Created by Philipp Jahoda on 17/09/16.
 */
class Description(
    var text: String = "Description Label",
    var position: MPPointF? = null,
    var textAlign: Align = RIGHT,
) : ComponentBase() {

  init {
    textSize = Utils.convertDpToPixel(8f)
  }

  /**
   * Sets a custom position for the description text in pixels on the screen.
   *
   * @param x x coordinate
   * @param y y coordinate
   */
  fun setPosition(x: Float, y: Float) {
    if (position == null) {
      position = MPPointF.getInstance(x, y)
    } else {
      position?.x = x
      position?.y = y
    }
  }
}
