package com.github.eklipse2k8.charting.data

import com.github.eklipse2k8.charting.interfaces.datasets.IBubbleDataSet

class BubbleData : BarLineScatterCandleBubbleData<IBubbleDataSet, BubbleEntry> {

  constructor() : super()

  constructor(vararg dataSets: IBubbleDataSet) : super(*dataSets)

  constructor(dataSets: MutableList<IBubbleDataSet>) : super(dataSets)

  /**
   * Sets the width of the circle that surrounds the bubble when highlighted for all DataSet objects
   * this data object contains, in dp.
   *
   * @param width
   */
  fun setHighlightCircleWidth(width: Float) {
    for (set in dataSets) {
      set.highlightCircleWidth = width
    }
  }
}
