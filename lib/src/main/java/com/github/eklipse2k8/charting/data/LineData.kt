package com.github.eklipse2k8.charting.data

import com.github.eklipse2k8.charting.interfaces.datasets.ILineDataSet

/**
 * Data object that encapsulates all data associated with a LineChart.
 *
 * @author Philipp Jahoda
 */
class LineData
@JvmOverloads
constructor(mutableDataSets: MutableList<ILineDataSet> = mutableListOf()) :
    BarLineScatterCandleBubbleData<ILineDataSet, Entry>(mutableDataSets) {

  constructor(vararg dataSets: ILineDataSet) : this(dataSets.toMutableList())
}
