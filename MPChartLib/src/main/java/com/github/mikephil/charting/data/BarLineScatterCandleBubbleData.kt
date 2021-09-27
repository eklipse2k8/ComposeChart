package com.github.mikephil.charting.data

import com.github.mikephil.charting.interfaces.datasets.IBarLineScatterCandleBubbleDataSet

/**
 * Baseclass for all Line, Bar, Scatter, Candle and Bubble data.
 *
 * @author Philipp Jahoda
 */
abstract class BarLineScatterCandleBubbleData<T : IBarLineScatterCandleBubbleDataSet<Entry>> :
    ChartData<T, Entry> {
  constructor() : super()
  constructor(vararg dataSets: T) : super(*dataSets)
  constructor(sets: MutableList<T>) : super(sets = sets)
}
