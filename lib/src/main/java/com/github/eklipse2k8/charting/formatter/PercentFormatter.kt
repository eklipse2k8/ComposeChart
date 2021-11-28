package com.github.eklipse2k8.charting.formatter

import com.github.eklipse2k8.charting.components.AxisBase
import com.github.eklipse2k8.charting.data.Entry
import com.github.eklipse2k8.charting.utils.ViewPortHandler
import java.text.DecimalFormat

/**
 * This IValueFormatter is just for convenience and simply puts a "%" sign after each value.
 * (Recommeded for PieChart)
 *
 * @author Philipp Jahoda
 */
class PercentFormatter : IValueFormatter, IAxisValueFormatter {
  protected var mFormat: DecimalFormat

  constructor() {
    mFormat = DecimalFormat("###,###,##0.0")
  }

  /**
   * Allow a custom decimalformat
   *
   * @param format
   */
  constructor(format: DecimalFormat) {
    mFormat = format
  }

  // IValueFormatter
  override fun getFormattedValue(
      value: Float,
      entry: Entry,
      dataSetIndex: Int,
      viewPortHandler: ViewPortHandler
  ): String {
    return mFormat.format(value.toDouble()) + " %"
  }

  // IAxisValueFormatter
  override fun getFormattedValue(value: Float, axis: AxisBase): String? {
    return mFormat.format(value.toDouble()) + " %"
  }

  val decimalDigits: Int = 1
}
