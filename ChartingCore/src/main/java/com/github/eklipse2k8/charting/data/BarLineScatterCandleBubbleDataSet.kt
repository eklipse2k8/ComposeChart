package com.github.eklipse2k8.charting.data

import android.graphics.Color
import com.github.eklipse2k8.charting.interfaces.datasets.IBarLineScatterCandleBubbleDataSet

/**
 * Baseclass of all DataSets for Bar-, Line-, Scatter- and CandleStickChart.
 *
 * @author Philipp Jahoda
 */
abstract class BarLineScatterCandleBubbleDataSet<T : Entry>(yVals: MutableList<T>, label: String?) :
    DataSet<T>(yVals, label), IBarLineScatterCandleBubbleDataSet<T> {

  /** default highlight color */
  override var highLightColor = Color.rgb(255, 187, 115)

  protected fun copy(barLineScatterCandleBubbleDataSet: BarLineScatterCandleBubbleDataSet<*>) {
    super.copyTo(barLineScatterCandleBubbleDataSet)
    barLineScatterCandleBubbleDataSet.highLightColor = highLightColor
  }
}
