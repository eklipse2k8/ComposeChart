package com.github.mikephil.charting.charts

import android.content.Context
import android.util.AttributeSet
import com.github.mikephil.charting.data.CandleData
import com.github.mikephil.charting.data.CandleEntry
import com.github.mikephil.charting.interfaces.dataprovider.CandleDataProvider
import com.github.mikephil.charting.interfaces.datasets.ICandleDataSet
import com.github.mikephil.charting.renderer.CandleStickChartRenderer

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

  init {
    mRenderer = CandleStickChartRenderer(this, mAnimator, mViewPortHandler)
    xAxis.spaceMin = 0.5f
    xAxis.spaceMax = 0.5f
  }

  override val candleData: CandleData?
    get() = data
}
