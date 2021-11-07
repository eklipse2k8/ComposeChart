package com.github.mikephil.charting.data

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import com.github.mikephil.charting.highlight.Range
import kotlin.math.abs

/**
 * Entry class for the BarChart. (especially stacked bars)
 *
 * @param x
 * @param y
 * @param yVals the stack values, use at least 2
 * @param icon icon image
 * @param data Spot for additional data this Entry represents.
 *
 * @author Philipp Jahoda
 */
@SuppressLint("ParcelCreator")
class BarEntry
@JvmOverloads
constructor(
    x: Float = Float.NaN,
    y: Float = Float.NaN,
    val yVals: FloatArray? = null,
    icon: Drawable? = null,
    data: Any? = null
) : Entry(x, yVals?.sum() ?: y, icon, data) {

  /** the ranges for the individual stack values - automatically calculated */
  var ranges: Array<Range?> = emptyArray()
    private set

  /** the sum of all negative values this entry (if stacked) contains */
  var negativeSum = 0f
    private set

  /** the sum of all positive values this entry (if stacked) contains */
  var positiveSum = 0f
    private set

  /** Returns an exact copy of the BarEntry. */
  override fun copy(): BarEntry = BarEntry(x, y, yVals, icon, data)

  init {
    calcPosNegSum()
    calcRanges()
  }

  /**
   * Returns true if this BarEntry is stacked (has a values array), false if not.
   *
   * @return
   */
  val isStacked: Boolean = yVals != null

  fun getSumBelow(stackIndex: Int): Float {
    if (yVals == null) return 0f
    var remainder = 0f
    var index = yVals.size - 1
    while (index > stackIndex && index >= 0) {
      remainder += yVals[index]
      index--
    }
    return remainder
  }

  private fun calcPosNegSum() {
    if (yVals == null) {
      negativeSum = 0f
      positiveSum = 0f
      return
    }
    var sumNeg = 0f
    var sumPos = 0f
    for (f in yVals) {
      if (f <= 0f) sumNeg += abs(f) else sumPos += f
    }
    negativeSum = sumNeg
    positiveSum = sumPos
  }

  private fun calcRanges() {
    val values = yVals
    if (values == null || values.isEmpty()) return
    ranges = arrayOfNulls(values.size)
    var negRemain = -negativeSum
    var posRemain = 0f
    for (i in ranges.indices) {
      val value = values[i]
      if (value < 0) {
        ranges[i] = Range(negRemain, negRemain - value)
        negRemain -= value
      } else {
        ranges[i] = Range(posRemain, posRemain + value)
        posRemain += value
      }
    }
  }
}
