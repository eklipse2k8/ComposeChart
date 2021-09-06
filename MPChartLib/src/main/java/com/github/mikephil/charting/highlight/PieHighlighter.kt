package com.github.mikephil.charting.highlight

import androidx.annotation.Nullable
import com.github.mikephil.charting.charts.PieChart

/** Created by philipp on 12/06/16. */
class PieHighlighter(chart: PieChart) : PieRadarHighlighter<PieChart>(chart) {
  @Nullable
  override fun getClosestHighlight(index: Int, x: Float, y: Float): Highlight? {
    val set = mChart.data?.dataSet
    val entry = set?.getEntryForIndex(index)
    return entry?.let { Highlight(index.toFloat(), it.y, x, y, 0, set.axisDependency) }
  }
}
