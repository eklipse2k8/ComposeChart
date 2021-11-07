package com.github.eklipse2k8.charting.interfaces.dataprovider

import com.github.eklipse2k8.charting.data.BarData

interface BarDataProvider : BarLineScatterCandleBubbleDataProvider {
  val barData: BarData?
  val isDrawBarShadowEnabled: Boolean
  val isDrawValueAboveBarEnabled: Boolean
  val isHighlightFullBarEnabled: Boolean
}
