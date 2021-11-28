package com.github.eklipse2k8.charting.interfaces.dataprovider

import com.github.eklipse2k8.charting.data.CandleData

interface CandleDataProvider : BarLineScatterCandleBubbleDataProvider {
  val candleData: CandleData?
}
