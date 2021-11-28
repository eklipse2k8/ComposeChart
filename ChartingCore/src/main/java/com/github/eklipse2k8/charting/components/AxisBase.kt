package com.github.eklipse2k8.charting.components

import android.content.Context
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Typeface
import android.util.Log
import androidx.core.content.res.getColorOrThrow
import androidx.core.content.res.getDimensionPixelSizeOrThrow
import androidx.core.content.res.getIntOrThrow
import androidx.core.content.res.getStringOrThrow
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.TypefaceCompatUtil
import com.github.eklipse2k8.charting.R
import com.github.eklipse2k8.charting.formatter.DefaultAxisValueFormatter
import com.github.eklipse2k8.charting.formatter.IAxisValueFormatter
import com.github.eklipse2k8.charting.utils.Utils
import kotlin.math.abs

private val TAG = AxisBase::class.java.simpleName

/**
 * Base-class of all axes (previously called labels).
 *
 * @author Philipp Jahoda
 */
abstract class AxisBase : ComponentBase() {
  /** custom formatter that is used instead of the auto-formatter if set */
  protected var mAxisValueFormatter: IAxisValueFormatter? = null

  /** Sets the width of the grid lines that are drawn away from each axis label. */
  var gridLineWidth: Float = 1f
    set(width) {
      field = Utils.convertDpToPixel(width)
    }

  /**
   * Sets the color of the grid lines for this axis (the horizontal lines coming from each label).
   */
  var gridColor = Color.GRAY
    set(value) {
      Log.i(TAG, "setGridColor value=$value")
      field = value
    }

  /** Sets the color of the border surrounding the chart. */
  var axisLineColor = Color.GRAY
    set(value) {
      Log.i(TAG, "setAxisLineColor value=$value")
      field = value
    }

  /** Sets the width of the border surrounding the chart in dp. */
  var axisLineWidth: Float = 1f
    set(width) {
      field = Utils.convertDpToPixel(width)
    }

  /** the actual array of entries */
  @JvmField var entries = floatArrayOf()

  /** axis label entries only used for centered labels */
  @JvmField var centeredEntries = floatArrayOf()

  /** the number of entries the legend contains */
  @JvmField var entryCount = 0

  /** the number of decimal digits to use */
  @JvmField var decimals = 0

  /**
   * Sets the number of label entries for the y-axis max = 25, min = 2, default: 6, be aware that
   * this number is not fixed.
   */
  var labelCount: Int = 6
    set(value) {
      var count = value
      if (count > axisMaxLabels) count = axisMaxLabels
      if (count < axisMinLabels) count = axisMinLabels
      field = count
      isForceLabelsEnabled = false
    }

  /** the minimum interval between axis values */
  protected var mGranularity = 1.0f

  /**
   * When true, axis labels are controlled by the `granularity` property. When false, axis values
   * could possibly be repeated. This could happen if two adjacent axis values are rounded to same
   * value. If using granularity this could be avoided by having fewer axis values visible.
   */
  var isGranularityEnabled = false

  /** if true, the set number of y-labels will be forced */
  var isForceLabelsEnabled = false
    protected set

  /** flag indicating if the grid lines for this axis should be drawn */
  var isDrawGridLinesEnabled = true
    protected set

  /** flag that indicates if the line alongside the axis is drawn or not */
  var isDrawAxisLineEnabled = true
    protected set

  /** flag that indicates of the labels of this axis should be drawn or not */
  var isDrawLabelsEnabled = true
    protected set

  protected var mCenterAxisLabels = false

  /** the path effect of the axis line that makes dashed lines possible */
  var axisLineDashPathEffect: DashPathEffect? = null
    private set

  /** the path effect of the grid lines that makes dashed lines possible */
  var gridDashPathEffect: DashPathEffect? = null
    private set

  /** array of limit lines that can be set for the axis */
  protected var mLimitLines: MutableList<LimitLine> = ArrayList()

  /** flag indicating the limit lines layer depth */
  var isDrawLimitLinesBehindDataEnabled = false
    protected set

  /** flag indicating the grid lines layer depth */
  var isDrawGridLinesBehindDataEnabled = true
    protected set

  /** Extra spacing for `axisMinimum` to be added to automatically calculated `axisMinimum` */
  var spaceMin = 0f

  /** Extra spacing for `axisMaximum` to be added to automatically calculated `axisMaximum` */
  var spaceMax = 0f

  /** flag indicating that the axis-min value has been customized */
  var isAxisMinCustom = false
    protected set

  /** flag indicating that the axis-max value has been customized */
  var isAxisMaxCustom = false
    protected set

  /** don't touch this direclty, use setter */
  @JvmField var mAxisMaximum = 0f

  /** don't touch this directly, use setter */
  @JvmField var mAxisMinimum = 0f

  /** the total range of values this axis covers */
  @JvmField var mAxisRange = 0f

  private var mAxisMinLabels = 2

  private var mAxisMaxLabels = 25

  /** The minumum number of labels on the axis */
  var axisMinLabels: Int
    get() = mAxisMinLabels
    set(labels) {
      if (labels > 0) mAxisMinLabels = labels
    }

  /** The maximum number of labels on the axis */
  /** The maximum number of labels on the axis */
  var axisMaxLabels: Int
    get() = mAxisMaxLabels
    set(labels) {
      if (labels > 0) mAxisMaxLabels = labels
    }

  /**
   * Set this to true to enable drawing the grid lines for this axis.
   *
   * @param enabled
   */
  fun setDrawGridLines(enabled: Boolean) {
    isDrawGridLinesEnabled = enabled
  }

  /**
   * Set this to true if the line alongside the axis should be drawn or not.
   *
   * @param enabled
   */
  fun setDrawAxisLine(enabled: Boolean) {
    isDrawAxisLineEnabled = enabled
  }

  /**
   * Centers the axis labels instead of drawing them at their original position. This is useful
   * especially for grouped BarChart.
   *
   * @param enabled
   */
  fun setCenterAxisLabels(enabled: Boolean) {
    mCenterAxisLabels = enabled
  }

  val isCenterAxisLabelsEnabled: Boolean
    get() = mCenterAxisLabels && entryCount > 0

  /**
   * Set this to true to enable drawing the labels of this axis (this will not affect drawing the
   * grid lines or axis lines).
   *
   * @param enabled
   */
  fun setDrawLabels(enabled: Boolean) {
    isDrawLabelsEnabled = enabled
  }

  /**
   * sets the number of label entries for the y-axis max = 25, min = 2, default: 6, be aware that
   * this number is not fixed (if force == false) and can only be approximated.
   *
   * @param count the number of y-axis labels that should be displayed
   * @param force if enabled, the set label count will be forced, meaning that the exact specified
   * count of labels will be drawn and evenly distributed alongside the axis - this might cause
   * labels to have uneven values
   */
  fun setLabelCount(count: Int, force: Boolean) {
    labelCount = count
    isForceLabelsEnabled = force
  }

  /**
   * Set a minimum interval for the axis when zooming in. The axis is not allowed to go below that
   * limit. This can be used to avoid label duplicating when zooming in.
   */
  var granularity: Float
    get() = mGranularity
    set(granularity) {
      mGranularity = granularity
      // set this to true if it was disabled, as it makes no sense to call this method with
      // granularity disabled
      isGranularityEnabled = true
    }

  /**
   * Adds a new LimitLine to this axis.
   *
   * @param l
   */
  fun addLimitLine(l: LimitLine) {
    mLimitLines.add(l)
    if (mLimitLines.size > 6) {
      Log.e(
          "MPAndroiChart",
          "Warning! You have more than 6 LimitLines on your axis, do you really want " + "that?")
    }
  }

  /**
   * Removes the specified LimitLine from the axis.
   *
   * @param l
   */
  fun removeLimitLine(l: LimitLine) {
    mLimitLines.remove(l)
  }

  /** Removes all LimitLines from the axis. */
  fun removeAllLimitLines() {
    mLimitLines.clear()
  }

  /**
   * Returns the LimitLines of this axis.
   *
   * @return
   */
  val limitLines: List<LimitLine>
    get() = mLimitLines

  /**
   * If this is set to true, the LimitLines are drawn behind the actual data, otherwise on top.
   * Default: false
   *
   * @param enabled
   */
  fun setDrawLimitLinesBehindData(enabled: Boolean) {
    isDrawLimitLinesBehindDataEnabled = enabled
  }

  /**
   * If this is set to false, the grid lines are draw on top of the actual data, otherwise behind.
   * Default: true
   *
   * @param enabled
   */
  fun setDrawGridLinesBehindData(enabled: Boolean) {
    isDrawGridLinesBehindDataEnabled = enabled
  }

  /**
   * Returns the longest formatted label (in terms of characters), this axis contains.
   *
   * @return
   */
  val longestLabel: String
    get() {
      var longest = ""
      for (i in entries.indices) {
        val text = getFormattedLabel(i)
        if (text != null && longest.length < text.length) longest = text
      }
      return longest
    }

  fun getFormattedLabel(index: Int): String? {
    return if (index < 0 || index >= entries.size) ""
    else valueFormatter!!.getFormattedValue(entries[index], this)
  }

  /**
   * Sets the formatter to be used for formatting the axis labels. If no formatter is set, the chart
   * will automatically determine a reasonable formatting (concerning decimals) for all the values
   * that are drawn inside the chart. Use chart.getDefaultValueFormatter() to use the formatter
   * calculated by the chart.
   */
  var valueFormatter: IAxisValueFormatter?
    get() {
      if (mAxisValueFormatter == null ||
          mAxisValueFormatter is DefaultAxisValueFormatter &&
              (mAxisValueFormatter as DefaultAxisValueFormatter).decimalDigits != decimals)
          mAxisValueFormatter = DefaultAxisValueFormatter(decimals)
      return mAxisValueFormatter
    }
    set(f) {
      mAxisValueFormatter = f ?: DefaultAxisValueFormatter(decimals)
    }

  /**
   * Enables the grid line to be drawn in dashed mode, e.g. like this "- - - - - -". THIS ONLY WORKS
   * IF HARDWARE-ACCELERATION IS TURNED OFF. Keep in mind that hardware acceleration boosts
   * performance.
   *
   * @param lineLength the length of the line pieces
   * @param spaceLength the length of space in between the pieces
   * @param phase offset, in degrees (normally, use 0)
   */
  fun enableGridDashedLine(lineLength: Float, spaceLength: Float, phase: Float) {
    gridDashPathEffect = DashPathEffect(floatArrayOf(lineLength, spaceLength), phase)
  }

  /**
   * Enables the grid line to be drawn in dashed mode, e.g. like this "- - - - - -". THIS ONLY WORKS
   * IF HARDWARE-ACCELERATION IS TURNED OFF. Keep in mind that hardware acceleration boosts
   * performance.
   *
   * @param effect the DashPathEffect
   */
  fun setGridDashedLine(effect: DashPathEffect?) {
    gridDashPathEffect = effect
  }

  /** Disables the grid line to be drawn in dashed mode. */
  fun disableGridDashedLine() {
    gridDashPathEffect = null
  }

  /**
   * Returns true if the grid dashed-line effect is enabled, false if not.
   *
   * @return
   */
  val isGridDashedLineEnabled: Boolean
    get() = gridDashPathEffect != null

  /**
   * Enables the axis line to be drawn in dashed mode, e.g. like this "- - - - - -". THIS ONLY WORKS
   * IF HARDWARE-ACCELERATION IS TURNED OFF. Keep in mind that hardware acceleration boosts
   * performance.
   *
   * @param lineLength the length of the line pieces
   * @param spaceLength the length of space in between the pieces
   * @param phase offset, in degrees (normally, use 0)
   */
  fun enableAxisLineDashedLine(lineLength: Float, spaceLength: Float, phase: Float) {
    axisLineDashPathEffect = DashPathEffect(floatArrayOf(lineLength, spaceLength), phase)
  }

  /**
   * Enables the axis line to be drawn in dashed mode, e.g. like this "- - - - - -". THIS ONLY WORKS
   * IF HARDWARE-ACCELERATION IS TURNED OFF. Keep in mind that hardware acceleration boosts
   * performance.
   *
   * @param effect the DashPathEffect
   */
  fun setAxisLineDashedLine(effect: DashPathEffect?) {
    axisLineDashPathEffect = effect
  }

  /** Disables the axis line to be drawn in dashed mode. */
  fun disableAxisLineDashedLine() {
    axisLineDashPathEffect = null
  }

  /**
   * Returns true if the axis dashed-line effect is enabled, false if not.
   *
   * @return
   */
  val isAxisLineDashedLineEnabled: Boolean
    get() = axisLineDashPathEffect != null

  /**
   * Set a custom maximum value for this axis. If set, this value will not be calculated
   * automatically depending on the provided data. Use resetAxisMaxValue() to undo this.
   *
   * @param max
   */
  var axisMaximum: Float
    get() = mAxisMaximum
    set(max) {
      isAxisMaxCustom = true
      mAxisMaximum = max
      mAxisRange = abs(max - mAxisMinimum)
    }

  /**
   * Set a custom minimum value for this axis. If set, this value will not be calculated
   * automatically depending on the provided data. Use resetAxisMinValue() to undo this. Do not
   * forget to call setStartAtZero(false) if you use this method. Otherwise, the axis-minimum value
   * will still be forced to 0.
   *
   * @param min
   */
  var axisMinimum: Float
    get() = mAxisMinimum
    set(min) {
      isAxisMinCustom = true
      mAxisMinimum = min
      mAxisRange = Math.abs(mAxisMaximum - min)
    }

  /**
   * By calling this method, any custom maximum value that has been previously set is reseted, and
   * the calculation is done automatically.
   */
  fun resetAxisMaximum() {
    isAxisMaxCustom = false
  }

  /**
   * By calling this method, any custom minimum value that has been previously set is reseted, and
   * the calculation is done automatically.
   */
  fun resetAxisMinimum() {
    isAxisMinCustom = false
  }

  fun applyTextAppearance(context: Context, resId: Int) {
    context.withStyledAttributes(resId, R.styleable.Axis_TextAppearance) {
      textColor = getColorOrThrow(R.styleable.Axis_TextAppearance_android_textColor)
      textSize =
          getDimensionPixelSizeOrThrow(R.styleable.Axis_TextAppearance_android_textSize).toFloat()

      val familyName = getStringOrThrow(R.styleable.Axis_TextAppearance_android_fontFamily)
      val fontStyle = getIntOrThrow(R.styleable.Axis_TextAppearance_android_textStyle)
      typeface = Typeface.create(familyName, fontStyle)
    }
  }

  /**
   * Use setAxisMinimum(...) instead.
   *
   * @param min
   */
  @Deprecated("", ReplaceWith("axisMinimum = min"))
  fun setAxisMinValue(min: Float) {
    axisMinimum = min
  }

  /**
   * Use setAxisMaximum(...) instead.
   *
   * @param max
   */
  @Deprecated("", ReplaceWith("axisMaximum = max"))
  fun setAxisMaxValue(max: Float) {
    axisMaximum = max
  }

  /**
   * Calculates the minimum / maximum and range values of the axis with the given minimum and
   * maximum values from the chart data.
   *
   * @param dataMin the min value according to chart data
   * @param dataMax the max value according to chart data
   */
  open fun calculate(dataMin: Float, dataMax: Float) {
    // if custom, use value as is, else use data value
    var min = if (isAxisMinCustom) mAxisMinimum else dataMin - spaceMin
    var max = if (isAxisMaxCustom) mAxisMaximum else dataMax + spaceMax

    // temporary range (before calculations)
    val range = abs(max - min)

    // in case all values are equal
    if (range == 0f) {
      max += 1f
      min -= 1f
    }
    mAxisMinimum = min
    mAxisMaximum = max

    // actual range
    mAxisRange = abs(max - min)
  }

}
