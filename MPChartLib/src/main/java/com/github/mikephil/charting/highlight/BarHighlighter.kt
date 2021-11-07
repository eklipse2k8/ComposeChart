package com.github.mikephil.charting.highlight

import com.github.mikephil.charting.data.BarLineScatterCandleBubbleData
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.utils.MPPointD
import kotlin.math.abs
import kotlin.math.max

/** Created by Philipp Jahoda on 22/07/15. */
open class BarHighlighter(chart: BarDataProvider) : ChartHighlighter<BarDataProvider>(chart) {

  override fun getHighlight(x: Float, y: Float): Highlight? {
    val highlight = super.getHighlight(x, y) ?: return null
    return chartView.barData?.getDataSetByIndex(highlight.dataSetIndex)?.let {
      if (it.isStacked) {
        val pos = getValsForTouch(x, y)
        val stacked = getStackedHighlight(highlight, it, pos.x.toFloat(), pos.y.toFloat())
        MPPointD.recycleInstance(pos)
        stacked
      } else {
        null
      }
    }
        ?: highlight
  }

  /**
   * This method creates the Highlight object that also indicates which value of a stacked BarEntry
   * has been selected.
   *
   * @param high the Highlight to work with looking for stacked values
   * @param set
   * @param xVal
   * @param yVal
   * @return
   */
  fun getStackedHighlight(high: Highlight, set: IBarDataSet, xVal: Float, yVal: Float): Highlight? {
    val entry = set.getEntryForXValue(xVal, yVal) ?: return null

    // not stacked
    if (entry.yVals == null) {
      return high
    } else {
      val ranges = entry.ranges
      if (ranges.isNotEmpty()) {
        val stackIndex = getClosestStackIndex(ranges, yVal)
        val pixels =
            chartView
                .getTransformer(set.axisDependency)
                .getPixelForValues(high.x, ranges[stackIndex]?.to!!)
        val stackedHigh =
            Highlight(
                entry.x,
                entry.y,
                pixels.x.toFloat(),
                pixels.y.toFloat(),
                high.dataSetIndex,
                high.axis,
                stackIndex)
        MPPointD.recycleInstance(pixels)
        return stackedHigh
      }
    }
    return null
  }

  /**
   * Returns the index of the closest value inside the values array / ranges (stacked barchart) to
   * the value given as a parameter.
   *
   * @param ranges
   * @param value
   * @return
   */
  private fun getClosestStackIndex(ranges: Array<Range?>, value: Float): Int {
    if (ranges.isEmpty()) return 0
    for ((stackIndex, range) in ranges.withIndex()) {
      if (range?.contains(value) == true) return stackIndex
    }
    val length = max(ranges.size - 1, 0)
    return if (value > ranges[length]?.to!!) length else 0
  }

  override fun getDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
    return abs(x1 - x2)
  }

  override val data: BarLineScatterCandleBubbleData<*, *>?
    get() = chartView.barData
}
