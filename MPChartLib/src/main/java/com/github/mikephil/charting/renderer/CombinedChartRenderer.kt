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

/** Renderer class that is responsible for rendering multiple different data-types. */
class CombinedChartRenderer(
    chart: CombinedChart,
    animator: ChartAnimator?,
    viewPortHandler: ViewPortHandler?
) : DataRenderer(animator!!, viewPortHandler!!) {
  /** all rederers for the different kinds of data this combined-renderer can draw */
  private var subRenderers: MutableList<DataRenderer> = mutableListOf()

  private var weakChart: WeakReference<Chart<*,*,*>> = WeakReference(chart)

  /**
   * Creates the renderers needed for this combined-renderer in the required order. Also takes the
   * DrawOrder into consideration.
   */
  fun createRenderers() {
    this.subRenderers.clear()
    val chart = weakChart.get() as CombinedChart? ?: return
    val orders = chart.drawOrder
    if (orders != null) {
      for (order in orders) {
        when (order) {
          DrawOrder.BAR -> this.subRenderers.add(BarChartRenderer(chart, mAnimator, mViewPortHandler))
          DrawOrder.BUBBLE ->
            this.subRenderers.add(BubbleChartRenderer(chart, mAnimator, mViewPortHandler))
          DrawOrder.LINE ->
            this.subRenderers.add(LineChartRenderer(chart, mAnimator, mViewPortHandler))
          DrawOrder.CANDLE ->
            this.subRenderers.add(CandleStickChartRenderer(chart, mAnimator, mViewPortHandler))
          DrawOrder.SCATTER ->
            this.subRenderers.add(ScatterChartRenderer(chart, mAnimator, mViewPortHandler))
          else -> Unit
        }
      }
    }
  }

  override fun initBuffers() {
    for (renderer in this.subRenderers) renderer.initBuffers()
  }

  override fun drawData(c: Canvas) {
    for (renderer in this.subRenderers) renderer.drawData(c)
  }

  override fun drawValues(c: Canvas) {
    for (renderer in this.subRenderers) renderer.drawValues(c)
  }

  override fun drawExtras(c: Canvas) {
    for (renderer in this.subRenderers) renderer.drawExtras(c)
  }

  private var mHighlightBuffer: MutableList<Highlight> = ArrayList()

  override fun drawHighlighted(c: Canvas, indices: Array<Highlight?>?) {
    val chart = weakChart.get() ?: return
    for (renderer in this.subRenderers) {
      var data: ChartData<*, *>? = null
      when (renderer) {
        is BarChartRenderer -> data = renderer.mChart.barData
        is LineChartRenderer -> data = renderer.mChart.lineData
        is CandleStickChartRenderer -> data = renderer.mChart.candleData
        is ScatterChartRenderer -> data = renderer.mChart.scatterData
        is BubbleChartRenderer -> data = renderer.mChart.bubbleData
      }
      val dataIndex = if (data == null) -1 else (chart.data as CombinedData).allData.indexOf(data)
      mHighlightBuffer.clear()
      indices?.forEach { h ->
        if (h == null) return@forEach
        if (h.dataIndex == dataIndex || h.dataIndex == -1) mHighlightBuffer.add(h)
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
  fun getSubRenderer(index: Int): DataRenderer? = subRenderers.getOrNull(index)

  init {
    createRenderers()
  }
}
