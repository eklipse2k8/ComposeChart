package com.github.eklipse2k8.charting.charts

import android.content.Context
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import com.github.eklipse2k8.charting.components.YAxis
import com.github.eklipse2k8.charting.data.BarData
import com.github.eklipse2k8.charting.data.BarEntry
import com.github.eklipse2k8.charting.highlight.BarHighlighter
import com.github.eklipse2k8.charting.highlight.Highlight
import com.github.eklipse2k8.charting.interfaces.dataprovider.BarDataProvider
import com.github.eklipse2k8.charting.renderer.BarChartRenderer
import com.github.eklipse2k8.charting.renderer.DataRenderer

private val TAG = BarChart::class.java.simpleName

/**
 * Chart that draws bars.
 *
 * @author Philipp Jahoda
 */
open class BarChart
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    AnyBarChart(context, attrs, defStyleAttr), BarDataProvider {

  /**
   * Set this to true to make the highlight operation full-bar oriented, false to make it highlight
   * single values (relevant only for stacked). If enabled, highlighting operations will highlight
   * the whole bar, even if only a single stack entry was tapped. Default: false
   */
  override var isHighlightFullBarEnabled = false

  /** if set to true, all values are drawn above their bars, instead of below their top */
  override var isDrawValueAboveBarEnabled = true

  /** if set to true, a grey area is drawn behind each bar that indicates the maximum value */
  override var isDrawBarShadowEnabled = false

  private var mFitBars = false

  override val dataRenderer: DataRenderer = BarChartRenderer(this, animator, viewPortHandler)

  override val highlighter = BarHighlighter(this)

  init {
    xAxis.spaceMin = 0.5f
    xAxis.spaceMax = 0.5f
  }

  override fun calcMinMax() {
    val localData = barData ?: return

    if (mFitBars) {
      xAxis.calculate(
          localData.xMin - localData.barWidth / 2f, localData.xMax + localData.barWidth / 2f)
    } else {
      xAxis.calculate(localData.xMin, localData.xMax)
    }

    // calculate axis range (min / max) according to provided data
    axisLeft.calculate(
        localData.getYMin(YAxis.AxisDependency.LEFT), localData.getYMax(YAxis.AxisDependency.LEFT))
    axisRight.calculate(
        localData.getYMin(YAxis.AxisDependency.RIGHT),
        localData.getYMax(YAxis.AxisDependency.RIGHT))
  }

  /**
   * Returns the Highlight object (contains x-index and DataSet index) of the selected value at the
   * given touch point inside the BarChart.
   *
   * @param x
   * @param y
   * @return
   */
  override fun getHighlightByTouchPoint(x: Float, y: Float): Highlight? {
    return if (data == null) {
      Log.e(TAG, "Can't select by touch. No data set.")
      null
    } else {
      val h = highlighter.getHighlight(x, y)
      if (h == null || !isHighlightFullBarEnabled) h!!
      else Highlight(h.x, h.y, h.xPx, h.yPx, h.dataSetIndex, h.axis, -1)
    }
  }

  /**
   * Returns the bounding box of the specified Entry in the specified DataSet. Returns null if the
   * Entry could not be found in the charts data. Performance-intensive code should use void
   * getBarBounds(BarEntry, RectF) instead.
   *
   * @param e
   * @return
   */
  fun getBarBounds(e: BarEntry): RectF {
    val bounds = RectF()
    getBarBounds(e, bounds)
    return bounds
  }

  /**
   * The passed outputRect will be assigned the values of the bounding box of the specified Entry in
   * the specified DataSet. The rect will be assigned Float.MIN_VALUE in all locations if the Entry
   * could not be found in the charts data.
   *
   * @param e
   * @return
   */
  open fun getBarBounds(e: BarEntry, outputRect: RectF) {
    val set = data!!.getDataSetForEntry(e)
    if (set == null) {
      outputRect[Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE] = Float.MIN_VALUE
      return
    }
    val y = e.y
    val x = e.x
    val barWidth = barData?.barWidth ?: 0f
    val left = x - barWidth / 2f
    val right = x + barWidth / 2f
    val top: Float = if (y >= 0) y else 0f
    val bottom: Float = if (y <= 0) y else 0f
    outputRect[left, top, right] = bottom
    getTransformer(set.axisDependency).rectValueToPixel(outputRect)
  }

  /**
   * If set to true, all values are drawn above their bars, instead of below their top.
   *
   * @param enabled
   */
  fun setDrawValueAboveBar(enabled: Boolean) {
    isDrawValueAboveBarEnabled = enabled
  }

  /**
   * If set to true, a grey area is drawn behind each bar that indicates the maximum value. Enabling
   * his will reduce performance by about 50%.
   *
   * @param enabled
   */
  fun setDrawBarShadow(enabled: Boolean) {
    isDrawBarShadowEnabled = enabled
  }

  fun highlightValue(
      x: Float,
      dataSetIndex: Int,
      stackIndex: Int,
  ) {
    highlightValue(Highlight(x, dataSetIndex, stackIndex), false)
  }

  override val barData: BarData?
    get() = data as BarData

  /**
   * Adds half of the bar width to each side of the x-axis range in order to allow the bars of the
   * barchart to be fully displayed. Default: false
   *
   * @param enabled
   */
  fun setFitBars(enabled: Boolean) {
    mFitBars = enabled
  }

  /**
   * Groups all BarDataSet objects this data object holds together by modifying the x-value of their
   * entries. Previously set x-values of entries will be overwritten. Leaves space between bars and
   * groups as specified by the parameters. Calls notifyDataSetChanged() afterwards.
   *
   * @param fromX the starting point on the x-axis where the grouping should begin
   * @param groupSpace the space between groups of bars in values (not pixels) e.g. 0.8f for bar
   * width 1f
   * @param barSpace the space between individual bars in values (not pixels) e.g. 0.1f for bar
   * width 1f
   */
  fun groupBars(fromX: Float, groupSpace: Float, barSpace: Float) {
    barData?.groupBars(fromX, groupSpace, barSpace)
    notifyDataSetChanged()
  }
}
