package com.github.eklipse2k8.charting.highlight

import com.github.eklipse2k8.charting.data.Rounding
import com.github.eklipse2k8.charting.interfaces.dataprovider.BarDataProvider
import com.github.eklipse2k8.charting.interfaces.datasets.IDataSet
import com.github.eklipse2k8.charting.utils.MPPointD
import java.util.ArrayList
import kotlin.math.abs

/** Created by Philipp Jahoda on 22/07/15. */
class HorizontalBarHighlighter(chart: BarDataProvider) : BarHighlighter(chart) {
  override fun getHighlight(x: Float, y: Float): Highlight? {
    val barData = chartView.barData
    val pos = getValsForTouch(y, x)
    val high = getHighlightForX(pos.y.toFloat(), y, x) ?: return null
    val set = barData?.getDataSetByIndex(high.dataSetIndex)
    if (set?.isStacked == true) {
      return getStackedHighlight(high, set, pos.y.toFloat(), pos.x.toFloat())
    }
    MPPointD.recycleInstance(pos)
    return high
  }

  override fun buildHighlights(
      set: IDataSet<*>,
      dataSetIndex: Int,
      xVal: Float,
      rounding: Rounding?
  ): List<Highlight>? {
    val highlights = ArrayList<Highlight>()
    var entries = set.getEntriesForXValue(xVal)
    if (entries?.size == 0) {
      // Try to find closest x-value and take all entries for that x-value
      val closest = set.getEntryForXValue(xVal, Float.NaN, rounding)
      entries = closest?.let { set.getEntriesForXValue(it.x) }
    }
    if (entries.isNullOrEmpty()) return highlights
    for (e in entries) {
      val pixels = chartView.getTransformer(set.axisDependency).getPixelForValues(e.y, e.x)
      highlights.add(
          Highlight(
              e.x, e.y, pixels.x.toFloat(), pixels.y.toFloat(), dataSetIndex, set.axisDependency))
    }
    return highlights
  }

  override fun getDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
    return abs(y1 - y2)
  }
}
