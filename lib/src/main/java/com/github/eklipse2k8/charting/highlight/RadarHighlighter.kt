package com.github.eklipse2k8.charting.highlight

import com.github.eklipse2k8.charting.charts.RadarChart
import com.github.eklipse2k8.charting.utils.MPPointF
import com.github.eklipse2k8.charting.utils.Utils
import kotlin.math.abs

/** Created by philipp on 12/06/16. */
class RadarHighlighter(chart: RadarChart) : PieRadarHighlighter<RadarChart>(chart) {
  override fun getClosestHighlight(index: Int, x: Float, y: Float): Highlight? {
    val highlights = getHighlightsAtIndex(index)
    val distanceToCenter = mChart.distanceToCenter(x, y) / mChart.factor
    var closest: Highlight? = null
    var distance = Float.MAX_VALUE
    for (i in highlights.indices) {
      val high = highlights[i]
      val cdistance = abs(high.y - distanceToCenter)
      if (cdistance < distance) {
        closest = high
        distance = cdistance
      }
    }
    return closest
  }

  /**
   * Returns an array of Highlight objects for the given index. The Highlight objects give
   * information about the value at the selected index and the DataSet it belongs to. INFORMATION:
   * This method does calculations at runtime. Do not over-use in performance critical situations.
   *
   * @param index
   * @return
   */
  private fun getHighlightsAtIndex(index: Int): List<Highlight> {
    mHighlightBuffer.clear()
    val phaseX = mChart.animator.phaseX
    val phaseY = mChart.animator.phaseY
    val sliceangle = mChart.sliceAngle
    val factor = mChart.factor
    val pOut = MPPointF.getInstance(0f, 0f)
    val data = mChart.data
    if (data != null) {
      for (i in 0 until data.dataSetCount) {
        val dataSet = data.getDataSetByIndex(i)
        val entry = dataSet?.getEntryForIndex(index)
        if (entry != null) {
          val y = entry.y - mChart.yChartMin
          Utils.getPosition(
              mChart.centerOffsets,
              y * factor * phaseY,
              sliceangle * index * phaseX + mChart.rotationAngle,
              pOut)
          mHighlightBuffer.add(
              Highlight(index.toFloat(), entry.y, pOut.x, pOut.y, i, dataSet.axisDependency))
        }
      }
    }
    return mHighlightBuffer
  }
}
