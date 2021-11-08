package com.github.eklipse2k8.charting.charts

import android.content.Context
import android.util.AttributeSet
import com.github.eklipse2k8.charting.data.Entry
import com.github.eklipse2k8.charting.data.ScatterData
import com.github.eklipse2k8.charting.interfaces.dataprovider.ScatterDataProvider
import com.github.eklipse2k8.charting.interfaces.datasets.IScatterDataSet
import com.github.eklipse2k8.charting.renderer.DataRenderer
import com.github.eklipse2k8.charting.renderer.ScatterChartRenderer

/**
 * The ScatterChart. Draws dots, triangles, squares and custom shapes into the Chart-View. CIRCLE
 * and SCQUARE offer the best performance, TRIANGLE has the worst performance.
 *
 * @author Philipp Jahoda
 */
class ScatterChart
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    BarLineChartBase<ScatterData, IScatterDataSet, Entry>(context, attrs, defStyleAttr),
    ScatterDataProvider {

  override val dataRenderer: DataRenderer = ScatterChartRenderer(this, mAnimator, viewPortHandler)

  init {
    xAxis.spaceMin = 0.5f
    xAxis.spaceMax = 0.5f
  }

  override val scatterData: ScatterData?
    get() = data

}
