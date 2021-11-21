package com.github.eklipse2k8.charting.charts

import android.content.Context
import android.util.AttributeSet
import com.github.eklipse2k8.charting.data.CandleData
import com.github.eklipse2k8.charting.data.CandleEntry
import com.github.eklipse2k8.charting.interfaces.dataprovider.CandleDataProvider
import com.github.eklipse2k8.charting.interfaces.datasets.ICandleDataSet
import com.github.eklipse2k8.charting.renderer.CandleStickChartRenderer
import com.github.eklipse2k8.charting.renderer.DataRenderer

/**
 * Financial chart type that draws candle-sticks (OHCL chart).
 *
 * @author Philipp Jahoda
 */
class CandleStickChart
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    BarLineChartBase<CandleData, ICandleDataSet, CandleEntry>(context, attrs, defStyleAttr),
    CandleDataProvider {

  override val dataRenderer: DataRenderer =
      CandleStickChartRenderer(this, animator, viewPortHandler)

  init {
    xAxis.spaceMin = 0.5f
    xAxis.spaceMax = 0.5f
  }

  override val candleData: CandleData?
    get() = data
}
