package com.github.eklipse2k8.charting.interfaces.dataprovider

import com.github.eklipse2k8.charting.data.ScatterData

interface ScatterDataProvider : BarLineScatterCandleBubbleDataProvider {
  val scatterData: ScatterData?
}
