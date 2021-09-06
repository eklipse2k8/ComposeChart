package com.github.mikephil.charting.renderer

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.utils.MPPointD
import com.github.mikephil.charting.utils.Transformer
import com.github.mikephil.charting.utils.Utils.nextUp
import com.github.mikephil.charting.utils.Utils.roundToNextSignificant
import com.github.mikephil.charting.utils.ViewPortHandler
import kotlin.math.*

/**
 * Baseclass of all axis renderers.
 *
 * @author Philipp Jahoda
 */
abstract class AxisRenderer(
    viewPortHandler: ViewPortHandler,
    /** transformer to transform values to screen pixels and return */
    var transformer: Transformer? = null,
    /** base axis this axis renderer works with */
    protected var mAxis: AxisBase
) : Renderer(viewPortHandler) {
  /**
   * Returns the Transformer object used for transforming the axis values.
   *
   * @return
   */
  /**
   * Returns the Paint object that is used for drawing the grid-lines of the axis.
   *
   * @return
   */
  /** paint object for the grid lines */
  var paintGrid: Paint? = null
    protected set
  /**
   * Returns the Paint object used for drawing the axis (labels).
   *
   * @return
   */
  /** paint for the x-label values */
  var paintAxisLabels: Paint? = null
    protected set
  /**
   * Returns the Paint object that is used for drawing the axis-line that goes alongside the axis.
   *
   * @return
   */
  /** paint for the line surrounding the chart */
  var paintAxisLine: Paint? = null
    protected set

  /** paint used for the limit lines */
  @JvmField protected var mLimitLinePaint: Paint? = null

  /**
   * Computes the axis values.
   *
   * @param min
   * - the minimum value in the data object for this axis
   * @param max
   * - the maximum value in the data object for this axis
   */
  open fun computeAxis(min: Float, max: Float, inverted: Boolean) {
    // calculate the starting and entry point of the y-labels (depending on
    // zoom / contentrect bounds)
    var min = min
    var max = max
    if (mViewPortHandler.contentWidth() > 10 && !mViewPortHandler.isFullyZoomedOutY) {
      val p1 =
          transformer?.getValuesByTouchPoint(
              mViewPortHandler.contentLeft(), mViewPortHandler.contentTop())
      val p2 =
          transformer?.getValuesByTouchPoint(
              mViewPortHandler.contentLeft(), mViewPortHandler.contentBottom())
      if (!inverted) {
        min = p2?.y?.toFloat() ?: 0f
        max = p1?.y?.toFloat() ?: 0f
      } else {
        min = p1?.y?.toFloat() ?: 0f
        max = p2?.y?.toFloat() ?: 0f
      }
      MPPointD.recycleInstance(p1)
      MPPointD.recycleInstance(p2)
    }
    computeAxisValues(min, max)
  }

  /**
   * Sets up the axis values. Computes the desired number of labels between the two given extremes.
   *
   * @return
   */
  protected open fun computeAxisValues(min: Float, max: Float) {
    val labelCount = mAxis.labelCount
    val range = abs(max - min).toDouble()
    if (labelCount == 0 || range <= 0 || range.isInfinite()) {
      mAxis.mEntries = floatArrayOf()
      mAxis.mCenteredEntries = floatArrayOf()
      mAxis.mEntryCount = 0
      return
    }

    // Find out how much spacing (in y value space) between axis values
    val rawInterval = range / labelCount
    var interval = roundToNextSignificant(rawInterval).toDouble()

    // If granularity is enabled, then do not allow the interval to go below specified granularity.
    // This is used to avoid repeated values when rounding values for display.
    if (mAxis.isGranularityEnabled)
        interval = if (interval < mAxis.granularity) mAxis.granularity.toDouble() else interval

    // Normalize interval
    val intervalMagnitude = roundToNextSignificant(10.0.pow(log10(interval)))
    val intervalSigDigit = (interval / intervalMagnitude).toInt()
    if (intervalSigDigit > 5) {
      // Use one order of magnitude higher, to avoid intervals like 0.9 or 90
      // if it's 0.0 after floor(), we use the old value
      interval =
          if (floor(10.0 * intervalMagnitude) == 0.0) interval else floor(10.0 * intervalMagnitude)
    }
    var n = if (mAxis.isCenterAxisLabelsEnabled) 1 else 0

    // force label count
    if (mAxis.isForceLabelsEnabled) {
      interval = (range.toFloat() / (labelCount - 1).toFloat()).toDouble()
      mAxis.mEntryCount = labelCount
      if (mAxis.mEntries.size < labelCount) {
        // Ensure stops contains at least numStops elements.
        mAxis.mEntries = FloatArray(labelCount)
      }
      var v = min
      for (i in 0 until labelCount) {
        mAxis.mEntries[i] = v
        v += interval.toFloat()
      }
      n = labelCount

      // no forced count
    } else {
      var first = if (interval == 0.0) 0.0 else ceil(min / interval) * interval
      if (mAxis.isCenterAxisLabelsEnabled) {
        first -= interval
      }
      val last = if (interval == 0.0) 0.0 else nextUp(floor(max / interval) * interval)
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
      mAxis.mEntryCount = n
      if (mAxis.mEntries.size < n) {
        // Ensure stops contains at least numStops elements.
        mAxis.mEntries = FloatArray(n)
      }
      f = first
      var i = 0
      while (i < n) {
        if (f == 0.0) // Fix for negative zero case (Where value == -0.0, and 0.0 == -0.0)
         f = 0.0
        mAxis.mEntries[i] = f.toFloat()
        f += interval
        ++i
      }
    }

    // set decimals
    if (interval < 1) {
      mAxis.mDecimals = ceil(-log10(interval)).toInt()
    } else {
      mAxis.mDecimals = 0
    }
    if (mAxis.isCenterAxisLabelsEnabled) {
      if (mAxis.mCenteredEntries.size < n) {
        mAxis.mCenteredEntries = FloatArray(n)
      }
      val offset = interval.toFloat() / 2f
      for (i in 0 until n) {
        mAxis.mCenteredEntries[i] = mAxis.mEntries[i] + offset
      }
    }
  }

  /**
   * Draws the axis labels to the screen.
   *
   * @param c
   */
  abstract fun renderAxisLabels(c: Canvas?)

  /**
   * Draws the grid lines belonging to the axis.
   *
   * @param c
   */
  abstract fun renderGridLines(c: Canvas?)

  /**
   * Draws the line that goes alongside the axis.
   *
   * @param c
   */
  abstract fun renderAxisLine(c: Canvas?)

  /**
   * Draws the LimitLines associated with this axis to the screen.
   *
   * @param c
   */
  abstract fun renderLimitLines(c: Canvas?)

  init {
    paintAxisLabels = Paint(Paint.ANTI_ALIAS_FLAG)
    paintGrid =
        Paint().apply {
          color = Color.GRAY
          strokeWidth = 1f
          style = Paint.Style.STROKE
          alpha = 90
        }
    paintAxisLine =
        Paint().apply {
          color = Color.BLACK
          strokeWidth = 1f
          style = Paint.Style.STROKE
        }
    mLimitLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
  }
}
