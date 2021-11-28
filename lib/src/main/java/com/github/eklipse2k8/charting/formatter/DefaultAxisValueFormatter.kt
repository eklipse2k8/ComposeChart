package com.github.eklipse2k8.charting.formatter

import com.github.eklipse2k8.charting.components.AxisBase
import java.text.DecimalFormat

/** Created by philipp on 02/06/16. */
class DefaultAxisValueFormatter(digits: Int) : IAxisValueFormatter {
  /** decimalformat for formatting */
  protected var mFormat: DecimalFormat
  /**
   * Returns the number of decimal digits this formatter uses or -1, if unspecified.
   *
   * @return
   */
  /** the number of decimal digits this formatter uses */
  var decimalDigits = digits
    protected set

  override fun getFormattedValue(value: Float, axis: AxisBase): String {
    // avoid memory allocations here (for performance)
    return mFormat.format(value)
  }

  /**
   * Constructor that specifies to how many digits the value should be formatted.
   *
   * @param digits
   */
  init {
    val b = StringBuffer()
    for (i in 0 until digits) {
      if (i == 0) b.append(".")
      b.append("0")
    }
    mFormat = DecimalFormat("###,###,###,##0$b")
  }
}
