package com.github.mikephil.charting.highlight

import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.charts.PieRadarChartBase

/** Created by philipp on 12/06/16. */
abstract class PieRadarHighlighter<T : PieRadarChartBase<*, *, *>>(
    @JvmField protected var mChart: T
) : IHighlighter {
  /** buffer for storing previously highlighted values */
  @JvmField protected val mHighlightBuffer = mutableListOf<Highlight>()

  override fun getHighlight(x: Float, y: Float): Highlight? {
    val touchDistanceToCenter = mChart.distanceToCenter(x, y)

    // check if a slice was touched
    return if (touchDistanceToCenter > mChart.radius) {
      // if no slice was touched, highlight nothing
      null
    } else {
      var angle = mChart.getAngleForPoint(x, y)
      if (mChart is PieChart) {
        angle /= mChart.getAnimator().phaseY
      }
      val index = mChart.getIndexForAngle(angle)

      // check if the index could be found
      val range = mChart.data?.maxEntryCountSet?.entryCount?.let { IntRange(0, it) }
      if (range?.contains(index) == true) {
        getClosestHighlight(index, x, y)
      } else {
        null
      }
    }
  }

  /**
   * Returns the closest Highlight object of the given objects based on the touch position inside
   * the chart.
   *
   * @param index
   * @param x
   * @param y
   * @return
   */
  protected abstract fun getClosestHighlight(index: Int, x: Float, y: Float): Highlight?
}
