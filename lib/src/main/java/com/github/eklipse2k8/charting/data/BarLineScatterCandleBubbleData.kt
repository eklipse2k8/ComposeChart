package com.github.eklipse2k8.charting.data

import com.github.eklipse2k8.charting.interfaces.datasets.IBarLineScatterCandleBubbleDataSet

/**
 * Baseclass for all Line, Bar, Scatter, Candle and Bubble data.
 *
 * @author Philipp Jahoda
 */
abstract class BarLineScatterCandleBubbleData<T, E>
@JvmOverloads
constructor(mutableDataSets: MutableList<T> = mutableListOf()) :
    ChartData<T, E>(mutableDataSets) where T : IBarLineScatterCandleBubbleDataSet<E>, E : Entry {

  constructor(vararg sets: T) : this(sets.toMutableList())
}
