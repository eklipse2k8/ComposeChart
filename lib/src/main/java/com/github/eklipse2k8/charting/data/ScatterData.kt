package com.github.eklipse2k8.charting.data

import com.github.eklipse2k8.charting.interfaces.datasets.IScatterDataSet

class ScatterData : BarLineScatterCandleBubbleData<IScatterDataSet, Entry> {

  constructor() : super()

  constructor(dataSets: MutableList<IScatterDataSet>) : super(dataSets)

  constructor(vararg dataSets: IScatterDataSet) : super(*dataSets)

  /**
   * Returns the maximum shape-size across all DataSets.
   *
   * @return
   */
  val greatestShapeSize: Float
    get() {
      var max = 0f
      for (set in dataSets) {
        val size = set.scatterShapeSize
        if (size > max) max = size
      }
      return max
    }
}
