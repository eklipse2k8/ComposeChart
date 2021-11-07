package com.github.eklipse2k8.charting.interfaces.dataprovider

import com.github.eklipse2k8.charting.components.YAxis.AxisDependency
import com.github.eklipse2k8.charting.data.BarLineScatterCandleBubbleData
import com.github.eklipse2k8.charting.utils.Transformer

interface BarLineScatterCandleBubbleDataProvider : ChartInterface {
  fun getTransformer(axis: AxisDependency?): Transformer
  fun isInverted(axis: AxisDependency?): Boolean
  val lowestVisibleX: Float
  val highestVisibleX: Float
  override val data: BarLineScatterCandleBubbleData<*, *>?
}
