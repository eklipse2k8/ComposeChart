package com.github.eklipse2k8.charting.renderer

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.github.eklipse2k8.charting.components.AxisBase
import com.github.eklipse2k8.charting.utils.MPPointD
import com.github.eklipse2k8.charting.utils.Transformer
import com.github.eklipse2k8.charting.utils.Utils
import com.github.eklipse2k8.charting.utils.ViewPortHandler
import kotlin.math.*

/**
 * Baseclass of all axis renderers.
 *
 * @author Philipp Jahoda
 */
abstract class AxisRenderer(
    viewPortHandler: ViewPortHandler,
    /** transformer to transform values to screen pixels and return */
    protected var transformer: Transformer?,
    /** base axis this axis renderer works with */
    @JvmField protected var axis: AxisBase
) : Renderer(viewPortHandler) {

  /** paint object for the grid lines */
  @JvmField
  protected var gridPaint: Paint =
      Paint().apply {
        color = Color.GRAY
        strokeWidth = 1f
        style = Paint.Style.STROKE
        alpha = 90
      }

  /** paint for the x-label values */
  @JvmField protected var axisLabelPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

  /** paint for the line surrounding the chart */
  @JvmField
  protected var axisLinePaint: Paint =
      Paint().apply {
        color = Color.BLACK
        strokeWidth = 1f
        style = Paint.Style.STROKE
      }

  /** paint used for the limit lines */
  @JvmField
  protected var limitLinePaint: Paint =
      Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }

  /** Returns the Paint object used for drawing the axis (labels). */
  val paintAxisLabels = axisLabelPaint

  /**
   * Computes the axis values.
   *
   * @param min
   * - the minimum value in the data object for this axis
   * @param max
   * - the maximum value in the data object for this axis
   */
  open fun computeAxis(min: Float, max: Float, inverted: Boolean) {
    if (transformer == null) return
    // calculate the starting and entry point of the y-labels (depending on
    // zoom / contentrect bounds)
    var minVal = min
    var maxVal = max
    if (viewPortHandler.contentWidth() > 10 && !viewPortHandler.isFullyZoomedOutY) {
      val p1 =
          transformer!!.getValuesByTouchPoint(
              viewPortHandler.contentLeft(), viewPortHandler.contentTop())
      val p2 =
          transformer!!.getValuesByTouchPoint(
              viewPortHandler.contentLeft(), viewPortHandler.contentBottom())
      if (!inverted) {
        minVal = p2.y.toFloat()
        maxVal = p1.y.toFloat()
      } else {
        minVal = p1.y.toFloat()
        maxVal = p2.y.toFloat()
      }
      MPPointD.recycleInstance(p1)
      MPPointD.recycleInstance(p2)
    }
    computeAxisValues(minVal, maxVal)
  }

  /**
   * Sets up the axis values. Computes the desired number of labels between the two given extremes.
   *
   * @return
   */
  protected open fun computeAxisValues(min: Float, max: Float) {
    val labelCount = axis.labelCount
    val range = abs(max - min).toDouble()
    if (labelCount == 0 || range <= 0 || range.isInfinite()) {
      axis.mEntries = floatArrayOf()
      axis.mCenteredEntries = floatArrayOf()
      axis.mEntryCount = 0
      return
    }

    // Find out how much spacing (in y value space) between axis values
    val rawInterval = range / labelCount
    var interval = Utils.roundToNextSignificant(rawInterval).toDouble()

    // If granularity is enabled, then do not allow the interval to go below specified granularity.
    // This is used to avoid repeated values when rounding values for display.
    if (axis.isGranularityEnabled)
        interval = if (interval < axis.granularity) axis.granularity.toDouble() else interval

    // Normalize interval
    val intervalMagnitude = Utils.roundToNextSignificant(10.0.pow(log10(interval))).toDouble()
    val intervalSigDigit = (interval / intervalMagnitude).toInt()
    if (intervalSigDigit > 5) {
      // Use one order of magnitude higher, to avoid intervals like 0.9 or 90
      // if it's 0.0 after floor(), we use the old value
      interval =
          if (floor(10.0 * intervalMagnitude) == 0.0) interval else floor(10.0 * intervalMagnitude)
    }
    var n = if (axis.isCenterAxisLabelsEnabled) 1 else 0

    // force label count
    if (axis.isForceLabelsEnabled) {
      interval = (range.toFloat() / (labelCount - 1).toFloat()).toDouble()
      axis.mEntryCount = labelCount
      if (axis.mEntries.size < labelCount) {
        // Ensure stops contains at least numStops elements.
        axis.mEntries = FloatArray(labelCount)
      }
      var v = min
      for (i in 0 until labelCount) {
        axis.mEntries[i] = v
        v += interval.toFloat()
      }
      n = labelCount

      // no forced count
    } else {
      var first = if (interval == 0.0) 0.0 else ceil(min / interval) * interval
      if (axis.isCenterAxisLabelsEnabled) {
        first -= interval
      }
      val last = if (interval == 0.0) 0.0 else (floor(max / interval) * interval).nextUp()
      var f: Double
      if (interval != 0.0 && last != first) {
        f = first
        while (f <= last) {
          ++n
          f += interval
        }
      } else if (last == first && n == 0) {
        n = 1
      }
      axis.mEntryCount = n
      if (axis.mEntries.size < n) {
        // Ensure stops contains at least numStops elements.
        axis.mEntries = FloatArray(n)
      }
      f = first
      var i = 0
      while (i < n) {
        if (f == 0.0) // Fix for negative zero case (Where value == -0.0, and 0.0 == -0.0)
         f = 0.0
        axis.mEntries[i] = f.toFloat()
        f += interval
        ++i
      }
    }

    // set decimals
    if (interval < 1) {
      axis.mDecimals = ceil(-log10(interval)).toInt()
    } else {
      axis.mDecimals = 0
    }
    if (axis.isCenterAxisLabelsEnabled) {
      if (axis.mCenteredEntries.size < n) {
        axis.mCenteredEntries = FloatArray(n)
      }
      val offset = interval.toFloat() / 2f
      for (i in 0 until n) {
        axis.mCenteredEntries[i] = axis.mEntries[i] + offset
      }
    }
  }

  /**
   * Draws the axis labels to the screen.
   *
   * @param canvas
   */
  abstract fun renderAxisLabels(canvas: Canvas?)

  /**
   * Draws the grid lines belonging to the axis.
   *
   * @param canvas
   */
  abstract fun renderGridLines(canvas: Canvas?)

  /**
   * Draws the line that goes alongside the axis.
   *
   * @param canvas
   */
  abstract fun renderAxisLine(canvas: Canvas?)

  /**
   * Draws the LimitLines associated with this axis to the screen.
   *
   * @param canvas
   */
  abstract fun renderLimitLines(canvas: Canvas?)
}
