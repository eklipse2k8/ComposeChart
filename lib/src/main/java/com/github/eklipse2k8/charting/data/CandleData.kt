package com.github.eklipse2k8.charting.data

import com.github.eklipse2k8.charting.interfaces.datasets.ICandleDataSet

class CandleData
@JvmOverloads
constructor(dataSets: MutableList<ICandleDataSet> = mutableListOf()) :
    BarLineScatterCandleBubbleData<ICandleDataSet, CandleEntry>(dataSets) {

  constructor(vararg dataSets: ICandleDataSet) : this(dataSets.toMutableList())
}
