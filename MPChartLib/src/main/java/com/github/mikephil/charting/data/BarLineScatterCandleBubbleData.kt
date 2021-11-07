package com.github.mikephil.charting.data

import com.github.mikephil.charting.interfaces.datasets.IBarLineScatterCandleBubbleDataSet

/**
 * Baseclass for all Line, Bar, Scatter, Candle and Bubble data.
 *
 * @author Philipp Jahoda
 */
abstract class BarLineScatterCandleBubbleData<T, E> : ChartData<T, E> where
T : IBarLineScatterCandleBubbleDataSet<E>,
E : Entry {
  constructor() : super()

  constructor(vararg sets: T) : super(*sets)

  constructor(sets: MutableList<T>) : super(sets)
}
