package com.github.eklipse2k8.charting.formatter

import com.github.eklipse2k8.charting.data.Entry
import com.github.eklipse2k8.charting.utils.ViewPortHandler

/**
 * Interface that allows custom formatting of all values inside the chart before they are being
 * drawn to the screen. Simply create your own formatting class and let it implement
 * IValueFormatter. Then override the getFormattedValue(...) method and return whatever you want.
 *
 * @author Philipp Jahoda
 */
interface IValueFormatter {
  /**
   * Called when a value (from labels inside the chart) is formatted before being drawn. For
   * performance reasons, avoid excessive calculations and memory allocations inside this method.
   *
   * @param value the value to be formatted
   * @param entry the entry the value belongs to - in e.g. BarChart, this is of class BarEntry
   * @param dataSetIndex the index of the DataSet the entry in focus belongs to
   * @param viewPortHandler provides information about the current chart state (scale, translation,
   * ...)
   * @return the formatted label ready for being drawn
   */
  fun getFormattedValue(
      value: Float,
      entry: Entry,
      dataSetIndex: Int,
      viewPortHandler: ViewPortHandler
  ): String
}
