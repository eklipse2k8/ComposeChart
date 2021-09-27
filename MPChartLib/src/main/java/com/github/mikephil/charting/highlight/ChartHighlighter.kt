package com.github.mikephil.charting.highlight

import androidx.annotation.Nullable
import com.github.mikephil.charting.components.YAxis.AxisDependency
import com.github.mikephil.charting.data.BarLineScatterCandleBubbleData
import com.github.mikephil.charting.data.DataSet.Rounding
import com.github.mikephil.charting.interfaces.dataprovider.BarLineScatterCandleBubbleDataProvider
import com.github.mikephil.charting.interfaces.datasets.IDataSet
import com.github.mikephil.charting.utils.MPPointD
import kotlin.math.hypot

/** Created by Philipp Jahoda on 21/07/15. */
open class ChartHighlighter<T : BarLineScatterCandleBubbleDataProvider>(
    /** instance of the data-provider */
    @JvmField protected var mChart: T
) : IHighlighter {

  /** buffer for storing previously highlighted values */
  @JvmField protected var mHighlightBuffer = mutableListOf<Highlight>()

  override fun getHighlight(x: Float, y: Float): Highlight? {
    val pos = getValsForTouch(x, y)
    val xVal = pos.x.toFloat()
    MPPointD.recycleInstance(pos)
    return getHighlightForX(xVal, x, y)
  }

  /**
   * Returns a recyclable MPPointD instance. Returns the corresponding xPos for a given
   * touch-position in pixels.
   *
   * @param x
   * @param y
   * @return
   */
  protected fun getValsForTouch(x: Float, y: Float): MPPointD {
    // take any transformer to determine the x-axis value
    return mChart.getTransformer(AxisDependency.LEFT).getValuesByTouchPoint(x, y)
  }

  /**
   * Returns the corresponding Highlight for a given xVal and x- and y-touch position in pixels.
   *
   * @param xVal
   * @param x
   * @param y
   * @return
   */
  protected fun getHighlightForX(xVal: Float, x: Float, y: Float): Highlight? {
    val closestValues = getHighlightsAtXValue(xVal, x, y)
    if (closestValues.isEmpty()) {
      return null
    }
    val leftAxisMinDist = getMinimumDistance(closestValues, y, AxisDependency.LEFT)
    val rightAxisMinDist = getMinimumDistance(closestValues, y, AxisDependency.RIGHT)
    val axis = if (leftAxisMinDist < rightAxisMinDist) AxisDependency.LEFT else AxisDependency.RIGHT
    return getClosestHighlightByPixel(closestValues, x, y, axis, mChart.maxHighlightDistance)
  }

  /**
   * Returns the minimum distance from a touch value (in pixels) to the closest value (in pixels)
   * that is displayed in the chart.
   *
   * @param closestValues
   * @param pos
   * @param axis
   * @return
   */
  protected fun getMinimumDistance(
      closestValues: List<Highlight>,
      pos: Float,
      axis: AxisDependency
  ): Float {
    var distance = Float.MAX_VALUE
    for (i in closestValues.indices) {
      val high = closestValues[i]
      if (high.axis == axis) {
        val tempDistance = Math.abs(getHighlightPos(high) - pos)
        if (tempDistance < distance) {
          distance = tempDistance
        }
      }
    }
    return distance
  }

  private fun getHighlightPos(h: Highlight): Float {
    return h.yPx
  }

  /**
   * Returns a list of Highlight objects representing the entries closest to the given xVal. The
   * returned list contains two objects per DataSet (closest rounding up, closest rounding down).
   *
   * @param xVal the transformed x-value of the x-touch position
   * @param x touch position
   * @param y touch position
   * @return
   */
  protected open fun getHighlightsAtXValue(xVal: Float, x: Float, y: Float): List<Highlight> {
    mHighlightBuffer.clear()
    val data = data ?: return mHighlightBuffer
    var i = 0
    val dataSetCount = data.dataSetCount
    while (i < dataSetCount) {
      val dataSet = data.getDataSetByIndex(i)
      // don't include DataSets that cannot be highlighted
      if (dataSet?.isHighlightEnabled != false) {
        i++
        continue
      }
      mHighlightBuffer.addAll(buildHighlights(dataSet, i, xVal, Rounding.CLOSEST))
      i++
    }
    return mHighlightBuffer
  }

  /**
   * An array of `Highlight` objects corresponding to the selected xValue and dataSetIndex.
   *
   * @param set
   * @param dataSetIndex
   * @param xVal
   * @param rounding
   * @return
   */
  protected open fun buildHighlights(
      set: IDataSet<*>,
      dataSetIndex: Int,
      xVal: Float,
      rounding: Rounding
  ): List<Highlight> {
    val highlights = mutableListOf<Highlight>()
    var entries = set.getEntriesForXValue(xVal)
    if (entries.isEmpty()) {
      // Try to find closest x-value and take all entries for that x-value
      val closest = set.getEntryForXValue(xVal, Float.NaN, rounding)
      entries = set.getEntriesForXValue(closest.x)
    }
    if (entries.isEmpty()) return highlights
    for (e in entries) {
      val pixels = mChart.getTransformer(set.axisDependency).getPixelForValues(e.x, e.y)
      highlights.add(
          Highlight(
              e.x, e.y, pixels.x.toFloat(), pixels.y.toFloat(), dataSetIndex, set.axisDependency))
    }
    return highlights
  }

  /**
   * Returns the Highlight of the DataSet that contains the closest value on the y-axis.
   *
   * @param closestValues contains two Highlight objects per DataSet closest to the selected
   * x-position (determined by rounding up an down)
   * @param x
   * @param y
   * @param axis the closest axis
   * @param minSelectionDistance
   * @return
   */
  private fun getClosestHighlightByPixel(
      closestValues: List<Highlight>,
      x: Float,
      y: Float,
      axis: AxisDependency?,
      minSelectionDistance: Float
  ): Highlight? {
    var closest: Highlight? = null
    var distance = minSelectionDistance
    for (i in closestValues.indices) {
      val high = closestValues[i]
      if (axis == null || high.axis == axis) {
        val cDistance = getDistance(x, y, high.xPx, high.yPx)
        if (cDistance < distance) {
          closest = high
          distance = cDistance
        }
      }
    }
    return closest
  }

  /**
   * Calculates the distance between the two given points.
   *
   * @param x1
   * @param y1
   * @param x2
   * @param y2
   * @return
   */
  protected open fun getDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
    return hypot((x1 - x2), (y1 - y2))
  }

  protected open val data: BarLineScatterCandleBubbleData?
    @Nullable get() = mChart.data
}
