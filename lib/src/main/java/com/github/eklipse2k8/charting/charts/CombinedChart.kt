package com.github.eklipse2k8.charting.charts

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import com.github.eklipse2k8.charting.data.*
import com.github.eklipse2k8.charting.highlight.CombinedHighlighter
import com.github.eklipse2k8.charting.highlight.Highlight
import com.github.eklipse2k8.charting.interfaces.dataprovider.CombinedDataProvider
import com.github.eklipse2k8.charting.interfaces.datasets.IBarLineScatterCandleBubbleDataSet
import com.github.eklipse2k8.charting.renderer.CombinedChartRenderer
import com.github.eklipse2k8.charting.renderer.DataRenderer

private val TAG = CombinedChart::class.java.simpleName

/**
 * This chart class allows the combination of lines, bars, scatter and candle data all displayed in
 * one chart area.
 *
 * @author Philipp Jahoda
 */
class CombinedChart
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    BarLineChartBase<CombinedData, IBarLineScatterCandleBubbleDataSet<Entry>, Entry>(
        context, attrs, defStyleAttr),
    CombinedDataProvider {

  /** if set to true, all values are drawn above their bars, instead of below their top */
  override var isDrawValueAboveBarEnabled = true
    private set

  /**
   * Set this to true to make the highlight operation full-bar oriented, false to make it highlight
   * single values (relevant only for stacked).
   */
  override var isHighlightFullBarEnabled = false

  /** if set to true, a grey area is drawn behind each bar that indicates the maximum value */
  override var isDrawBarShadowEnabled = false
    private set

  private var mDrawOrder: Array<DrawOrder> =
      arrayOf(DrawOrder.BAR, DrawOrder.BUBBLE, DrawOrder.LINE, DrawOrder.CANDLE, DrawOrder.SCATTER)

  /**
   * enum that allows to specify the order in which the different data objects for the
   * combined-chart are drawn
   */
  enum class DrawOrder {
    BAR,
    BUBBLE,
    LINE,
    CANDLE,
    SCATTER
  }

  override val dataRenderer: DataRenderer = CombinedChartRenderer(this, animator, viewPortHandler)

  override val highlighter = CombinedHighlighter(this, this)

  init {
    // Old default behaviour
    isHighlightFullBarEnabled = true
  }

  override var combinedData: CombinedData?
    get() = data
    set(value) {
      super.data = value
      (dataRenderer as CombinedChartRenderer).createRenderers()
      dataRenderer.initBuffers()
    }

  /**
   * Returns the Highlight object (contains x-index and DataSet index) of the selected value at the
   * given touch point inside the CombinedChart.
   *
   * @param x
   * @param y
   * @return
   */
  override fun getHighlightByTouchPoint(x: Float, y: Float): Highlight? =
      if (data == null) {
        Log.e(TAG, "Can't select by touch. No data set.")
        null
      } else {
        val h = highlighter.getHighlight(x, y)
        if (h == null || !isHighlightFullBarEnabled) h!!
        else Highlight(h.x, h.y, h.xPx, h.yPx, h.dataSetIndex, h.axis, -1)
      }

  override val lineData: LineData?
    get() = data?.lineData

  override val barData: BarData?
    get() = data?.barData

  override val scatterData: ScatterData?
    get() = data?.scatterData

  override val candleData: CandleData?
    get() = data?.candleData

  override val bubbleData: BubbleData?
    get() = data?.bubbleData

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

  /**
   * Sets the order in which the provided data objects should be drawn. The earlier you place them
   * in the provided array, the further they will be in the background. e.g. if you provide new
   * DrawOrer[] { DrawOrder.BAR, DrawOrder.LINE }, the bars will be drawn behind the lines.
   *
   * @param order
   */
  var drawOrder: Array<DrawOrder>?
    get() = mDrawOrder
    set(order) {
      if (order == null || order.isEmpty()) return
      mDrawOrder = order
    }

  /** draws all MarkerViews on the highlighted positions */
  override fun drawMarkers(canvas: Canvas) {
    // if there is no marker view or drawing marker is disabled
    if (marker == null || !isDrawMarkersEnabled() || !valuesToHighlight()) return
    val indicesToHighlight = mIndicesToHighlight ?: return
    indicesToHighlight.forEach { highlight ->
      if (highlight == null) return@forEach

      val set = data?.getDataSetByHighlight(highlight) as IBarLineScatterCandleBubbleDataSet<Entry>
      val e: Entry = data?.getEntryForHighlight(highlight) ?: return@forEach
      val entryIndex = set.getEntryIndex(e)

      // make sure entry not null
      if (entryIndex > set.entryCount * animator.phaseX) return@forEach
      val pos = getMarkerPosition(highlight)

      // check bounds
      if (!viewPortHandler.isInBounds(pos[0], pos[1])) return@forEach

      // callbacks to update the content
      marker?.refreshContent(e, highlight)

      // draw the marker
      marker?.draw(canvas, pos[0], pos[1])
    }
  }
}
