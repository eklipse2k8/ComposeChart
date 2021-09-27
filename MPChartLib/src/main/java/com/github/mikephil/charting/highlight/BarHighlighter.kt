package com.github.mikephil.charting.highlight

import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider
import com.github.mikephil.charting.utils.MPPointD
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.data.BarLineScatterCandleBubbleData
import kotlin.math.abs
import kotlin.math.max

/**
 * Created by Philipp Jahoda on 22/07/15.
 */
open class BarHighlighter(chart: BarDataProvider) : ChartHighlighter<BarDataProvider>(chart) {
  override fun getHighlight(x: Float, y: Float): Highlight? {
    val high = super.getHighlight(x, y) ?: return null
    val pos = getValsForTouch(x, y)
    val barData = mChart.barData
    val set = barData.getDataSetByIndex(high.dataSetIndex)
    if (set?.isStacked == true) {
      return getStackedHighlight(
        high,
        set,
        pos.x.toFloat(),
        pos.y.toFloat()
      )
    }
    MPPointD.recycleInstance(pos)
    return high
  }

  /**
   * This method creates the Highlight object that also indicates which value of a stacked BarEntry has been
   * selected.
   *
   * @param high the Highlight to work with looking for stacked values
   * @param set
   * @param xVal
   * @param yVal
   * @return
   */
  fun getStackedHighlight(high: Highlight, set: IBarDataSet, xVal: Float, yVal: Float): Highlight? {
    val entry = set.getEntryForXValue(xVal, yVal)

    // not stacked
    if (entry.yVals == null) {
      return high
    } else {
      val ranges = entry.ranges ?: return null
      if (ranges.isNotEmpty()) {
        val stackIndex = getClosestStackIndex(ranges, yVal)
        val pixels = mChart.getTransformer(set.axisDependency)
          .getPixelForValues(high.x, ranges[stackIndex]!!.to)
        val stackedHigh = Highlight(
          entry.x,
          entry.y,
          pixels.x.toFloat(),
          pixels.y.toFloat(),
          high.dataSetIndex,
          stackIndex,
          high.axis
        )
        MPPointD.recycleInstance(pixels)
        return stackedHigh
      }
    }
    return null
  }

  /**
   * Returns the index of the closest value inside the values array / ranges (stacked barchart) to the value
   * given as
   * a parameter.
   *
   * @param ranges
   * @param value
   * @return
   */
  private fun getClosestStackIndex(ranges: Array<Range?>?, value: Float): Int {
    if (ranges == null || ranges.isEmpty()) return 0
    for ((stackIndex, range) in ranges.withIndex()) {
      if (range?.contains(value) == true) return stackIndex
    }
    val length = max(ranges.size - 1, 0)
    return if (value > ranges[length]!!.to) length else 0
  }

  override fun getDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
    return abs(x1 - x2)
  }

  override val data: BarLineScatterCandleBubbleData
    get() = mChart.barData
}