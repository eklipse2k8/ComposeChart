package com.github.eklipse2k8.charting.renderer

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Align.CENTER
import android.graphics.Paint.Style.FILL
import android.graphics.Paint.Style.STROKE
import android.graphics.Paint.*
import com.github.eklipse2k8.charting.animation.ChartAnimator
import com.github.eklipse2k8.charting.data.Entry
import com.github.eklipse2k8.charting.formatter.IValueFormatter
import com.github.eklipse2k8.charting.highlight.Highlight
import com.github.eklipse2k8.charting.interfaces.dataprovider.ChartInterface
import com.github.eklipse2k8.charting.interfaces.datasets.IDataSet
import com.github.eklipse2k8.charting.utils.Utils
import com.github.eklipse2k8.charting.utils.ViewPortHandler

/**
 * Superclass of all render classes for the different data types (line, bar, ...).
 *
 * @author Philipp Jahoda
 */
abstract class DataRenderer(
  /** the animator object used to perform animations on the chart data */
    @JvmField protected var animator: ChartAnimator,
  viewPortHandler: ViewPortHandler,
) : Renderer(viewPortHandler) {

  /** main paint object used for rendering */
  @JvmField
  protected var renderPaint: Paint = Paint(ANTI_ALIAS_FLAG).apply { style = FILL }

  /** paint object for drawing values (text representing values of chart entries) */
  @JvmField
  protected var valuePaint: Paint =
      Paint(ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(63, 63, 63)
        textAlign = CENTER
        textSize = Utils.convertDpToPixel(9f)
      }

  /** paint used for highlighting values */
  @JvmField
  protected var highlightPaint: Paint =
      Paint(ANTI_ALIAS_FLAG).apply {
        style = STROKE
        strokeWidth = 2f
        color = Color.rgb(255, 187, 115)
      }

  @JvmField protected var mDrawPaint: Paint = Paint(DITHER_FLAG)

  /**
   * Returns the Paint object this renderer uses for drawing the values (value-text).
   *
   * @return
   */
  protected val paintValues = valuePaint

  protected open fun isDrawingValuesAllowed(chart: ChartInterface): Boolean {
    return chart.data!!.entryCount < chart.maxVisibleCount * viewPortHandler.scaleX
  }

  /**
   * Applies the required styling (provided by the DataSet) to the value-paint object.
   *
   * @param set
   */
  protected fun applyValueTextStyle(set: IDataSet<*>) {
    paintValues.typeface = set.valueTypeface
    paintValues.textSize = set.valueTextSize
  }

  /**
   * Initializes the buffers used for rendering with a new size. Since this method performs memory
   * allocations, it should only be called if necessary.
   */
  abstract fun initBuffers()

  /**
   * Draws the actual data in form of lines, bars, ... depending on Renderer subclass.
   *
   * @param c
   */
  abstract fun drawData(c: Canvas)

  /**
   * Loops over all Entrys and draws their values.
   *
   * @param c
   */
  abstract fun drawValues(c: Canvas)

  /**
   * Draws the value of the given entry by using the provided IValueFormatter.
   *
   * @param c canvas
   * @param formatter formatter for custom value-formatting
   * @param value the value to be drawn
   * @param entry the entry the value belongs to
   * @param dataSetIndex the index of the DataSet the drawn Entry belongs to
   * @param x position
   * @param y position
   * @param color
   */
  fun drawValue(
      c: Canvas,
      formatter: IValueFormatter,
      value: Float,
      entry: Entry?,
      dataSetIndex: Int,
      x: Float,
      y: Float,
      color: Int
  ) {
    paintValues.color = color
    c.drawText(
        formatter.getFormattedValue(value, entry!!, dataSetIndex, viewPortHandler),
        x,
        y,
        paintValues)
  }

  /**
   * Draws any kind of additional information (e.g. line-circles).
   *
   * @param c
   */
  abstract fun drawExtras(c: Canvas)

  /**
   * Draws all highlight indicators for the values that are currently highlighted.
   *
   * @param c
   * @param indices the highlighted values
   */
  abstract fun drawHighlighted(c: Canvas, indices: Array<Highlight?>?)
}
