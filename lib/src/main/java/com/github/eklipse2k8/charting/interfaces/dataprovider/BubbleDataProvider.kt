package com.github.eklipse2k8.charting.interfaces.dataprovider

import com.github.eklipse2k8.charting.data.BubbleData

interface BubbleDataProvider : BarLineScatterCandleBubbleDataProvider {
  val bubbleData: BubbleData?
}
