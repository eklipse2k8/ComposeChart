package com.github.mikephil.charting.data

import com.github.mikephil.charting.interfaces.datasets.ICandleDataSet

class CandleData
@JvmOverloads
constructor(dataSets: MutableList<ICandleDataSet> = mutableListOf()) :
    BarLineScatterCandleBubbleData<ICandleDataSet, CandleEntry>(dataSets) {

  constructor(vararg dataSets: ICandleDataSet) : this(dataSets.toMutableList())
}
