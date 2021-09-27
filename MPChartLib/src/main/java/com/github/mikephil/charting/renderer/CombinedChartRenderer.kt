package com.github.mikephil.charting.renderer

import android.graphics.Canvas
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.charts.CombinedChart.DrawOrder
import com.github.mikephil.charting.data.ChartData
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.ViewPortHandler
import java.lang.ref.WeakReference
import java.util.ArrayList

/** Renderer class that is responsible for rendering multiple different data-types. */
class CombinedChartRenderer(
    chart: CombinedChart,
    animator: ChartAnimator,
    viewPortHandler: ViewPortHandler
) : DataRenderer(animator, viewPortHandler) {
  /** all rederers for the different kinds of data this combined-renderer can draw */
  private var mRenderers: MutableList<DataRenderer> = ArrayList(5)
  private var mChart: WeakReference<Chart<*>> = WeakReference(chart)

  /**
   * Creates the renderers needed for this combined-renderer in the required order. Also takes the
   * DrawOrder into consideration.
   */
  fun createRenderers() {
    mRenderers.clear()
    val chart = mChart.get() as CombinedChart? ?: return
    val orders = chart.drawOrder
    for (order in orders) {
      when (order) {
        DrawOrder.BAR -> mRenderers.add(BarChartRenderer(chart, mAnimator, mViewPortHandler))
        DrawOrder.BUBBLE -> mRenderers.add(BubbleChartRenderer(chart, mAnimator, mViewPortHandler))
        DrawOrder.LINE -> mRenderers.add(LineChartRenderer(chart, mAnimator, mViewPortHandler))
        DrawOrder.CANDLE ->
            mRenderers.add(CandleStickChartRenderer(chart, mAnimator, mViewPortHandler))
        DrawOrder.SCATTER ->
            mRenderers.add(ScatterChartRenderer(chart, mAnimator, mViewPortHandler))
      }
    }
  }

  override fun initBuffers() {
    for (renderer in mRenderers) renderer.initBuffers()
  }

  override fun drawData(c: Canvas) {
    for (renderer in mRenderers) renderer.drawData(c)
  }

  override fun drawValues(c: Canvas) {
    for (renderer in mRenderers) renderer.drawValues(c)
  }

  override fun drawExtras(c: Canvas) {
    for (renderer in mRenderers) renderer.drawExtras(c)
  }

  private var mHighlightBuffer: MutableList<Highlight?> = ArrayList()

  override fun drawHighlighted(c: Canvas, indices: Array<Highlight?>) {
    val chart = mChart.get() ?: return
    for (renderer in mRenderers) {
      var data: ChartData<*>? = null
      when (renderer) {
        is BarChartRenderer -> data = renderer.mChart.barData
        is LineChartRenderer -> data = renderer.mChart.lineData
        is CandleStickChartRenderer -> data = renderer.mChart.candleData
        is ScatterChartRenderer -> data = renderer.mChart.scatterData
        is BubbleChartRenderer -> data = renderer.mChart.bubbleData
      }
      val dataIndex = if (data == null) -1 else (chart.data as CombinedData).allData.indexOf(data)
      mHighlightBuffer.clear()
      for (h in indices) {
        if (h?.dataIndex == dataIndex || h?.dataIndex == -1) mHighlightBuffer.add(h)
      }
      renderer.drawHighlighted(c, mHighlightBuffer.toTypedArray())
    }
  }

  /**
   * Returns the sub-renderer object at the specified index.
   *
   * @param index
   * @return
   */
  fun getSubRenderer(index: Int): DataRenderer? {
    return if (index >= mRenderers.size || index < 0) null else mRenderers[index]
  }

  /**
   * Returns all sub-renderers.
   *
   * @return
   */
  val subRenderers: List<DataRenderer>
    get() = mRenderers

  fun setSubRenderers(renderers: MutableList<DataRenderer>) {
    mRenderers = renderers
  }

  init {
    createRenderers()
  }
}
