package com.github.eklipse2k8.charting.formatter

import com.github.eklipse2k8.charting.components.AxisBase
import kotlin.math.roundToInt

/** This formatter is used for passing an array of x-axis labels, on whole x steps. */
class IndexAxisValueFormatter : IAxisValueFormatter {
  private var mValues = arrayOf<String>()
  private var mValueCount = 0

  /** An empty constructor. Use `setValues` to set the axis labels. */
  constructor()

  /**
   * Constructor that specifies axis labels.
   *
   * @param values The values string array
   */
  constructor(values: Array<String>) {
    mValues = values
  }

  /**
   * Constructor that specifies axis labels.
   *
   * @param values The values string array
   */
  constructor(values: Collection<String>) {
    mValues = values.toTypedArray()
  }

  override fun getFormattedValue(value: Float, axis: AxisBase): String {
    val index = value.roundToInt()
    return if (index < 0 || index >= mValueCount || index != value.toInt()) "" else mValues[index]
  }

  var values: Array<String>?
    get() = mValues
    set(value) {
      mValues = value ?: arrayOf()
      mValueCount = mValues.size
    }
}
