package com.github.eklipse2k8.charting.interfaces.dataprovider

import com.github.eklipse2k8.charting.data.CombinedData

/** Created by philipp on 11/06/16. */
interface CombinedDataProvider :
    LineDataProvider, BarDataProvider, BubbleDataProvider, CandleDataProvider, ScatterDataProvider {
  val combinedData: CombinedData?
}
