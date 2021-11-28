package com.github.eklipse2k8.charting.interfaces.dataprovider

import com.github.eklipse2k8.charting.components.YAxis
import com.github.eklipse2k8.charting.components.YAxis.AxisDependency
import com.github.eklipse2k8.charting.data.LineData

interface LineDataProvider : BarLineScatterCandleBubbleDataProvider {
  val lineData: LineData?
  fun getAxis(dependency: AxisDependency): YAxis
}
