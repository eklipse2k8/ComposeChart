package com.github.eklipse2k8.charting.interfaces.dataprovider

import android.graphics.RectF
import com.github.eklipse2k8.charting.data.ChartData
import com.github.eklipse2k8.charting.formatter.IValueFormatter
import com.github.eklipse2k8.charting.utils.MPPointF

/**
 * Interface that provides everything there is to know about the dimensions, bounds, and range of
 * the chart.
 *
 * @author Philipp Jahoda
 */
interface ChartInterface {
  /**
   * Returns the minimum x value of the chart, regardless of zoom or translation.
   *
   * @return
   */
  val xChartMin: Float

  /**
   * Returns the maximum x value of the chart, regardless of zoom or translation.
   *
   * @return
   */
  val xChartMax: Float
  val xRange: Float

  /**
   * Returns the minimum y value of the chart, regardless of zoom or translation.
   *
   * @return
   */
  val yChartMin: Float

  /**
   * Returns the maximum y value of the chart, regardless of zoom or translation.
   *
   * @return
   */
  val yChartMax: Float

  /**
   * Returns the maximum distance in scren dp a touch can be away from an entry to cause it to get
   * highlighted.
   *
   * @return
   */
  val maxHighlightDistance: Float

  val centerOfView: MPPointF?
  val centerOffsets: MPPointF?
  val contentRect: RectF?
  val defaultValueFormatter: IValueFormatter?
  val data: ChartData<*, *>?
  val maxVisibleCount: Int
}
