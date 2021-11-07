package com.github.eklipse2k8.charting.charts

import android.content.Context
import android.util.AttributeSet
import com.github.eklipse2k8.charting.data.Entry
import com.github.eklipse2k8.charting.data.LineData
import com.github.eklipse2k8.charting.interfaces.dataprovider.LineDataProvider
import com.github.eklipse2k8.charting.interfaces.datasets.ILineDataSet
import com.github.eklipse2k8.charting.renderer.DataRenderer
import com.github.eklipse2k8.charting.renderer.LineChartRenderer

/**
 * Chart that draws lines, surfaces, circles, ...
 *
 * @author Philipp Jahoda
 */
class LineChart
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    BarLineChartBase<LineData, ILineDataSet, Entry>(context, attrs, defStyleAttr),
    LineDataProvider {
  override val dataRenderer: DataRenderer = LineChartRenderer(this, mAnimator, viewPortHandler)

  override val lineData: LineData?
    get() = data

  override fun onDetachedFromWindow() {
    // releases the bitmap in the renderer to avoid oom error
    if (dataRenderer is LineChartRenderer) {
      dataRenderer.releaseBitmap()
    }
    super.onDetachedFromWindow()
  }
}
