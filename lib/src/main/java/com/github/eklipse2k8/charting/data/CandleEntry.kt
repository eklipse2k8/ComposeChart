package com.github.eklipse2k8.charting.data

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import kotlin.math.abs

/**
 * Subclass of Entry that holds all values for one entry in a CandleStickChart.
 *
 * @param x The value on the x-axis
 * @param high The (shadow) high value
 * @param low The (shadow) low value
 * @param open
 * @param close
 * @param icon Icon image
 * @param data Spot for additional data this Entry represents
 *
 * @author Philipp Jahoda
 */
@SuppressLint("ParcelCreator")
class CandleEntry
@JvmOverloads
constructor(
    x: Float = Float.NaN,
    val high: Float = 0f,
    val low: Float = 0f,
    val open: Float = 0f,
    val close: Float = 0f,
    icon: Drawable? = null,
    data: Any? = null
) : Entry(x, (high + low) / 2f, icon, data) {

  /**
   * Returns the overall range (difference) between shadow-high and shadow-low.
   *
   * @return
   */
  val shadowRange: Float = abs(high - low)

  /**
   * Returns the body size (difference between open and close).
   *
   * @return
   */
  val bodyRange: Float = abs(open - close)

  override fun copy(): CandleEntry {
    return CandleEntry(x, high, low, open, close, icon, data)
  }
}
