package com.github.mikephil.charting.data

import android.graphics.Color
import com.github.mikephil.charting.interfaces.datasets.IBarLineScatterCandleBubbleDataSet

/**
 * Baseclass of all DataSets for Bar-, Line-, Scatter- and CandleStickChart.
 *
 * @author Philipp Jahoda
 */
abstract class BarLineScatterCandleBubbleDataSet<E : Entry>(
    yVals: MutableList<E>,
    label: String,
) : DataSet<E>(yVals, label), IBarLineScatterCandleBubbleDataSet<E> {
  /**
   * Sets the color that is used for drawing the highlight indicators. Dont forget to resolve the
   * color using getResources().getColor(...) or Color.rgb(...).
   *
   * @param color
   */
  override val highLightColor: Int
    get() = Color.rgb(255, 187, 115)

  override fun copy(dataSet: IBarLineScatterCandleBubbleDataSet<E>?) {
    super.copy(dataSet)

  }

//  override fun copy(): DataSet<E>? {
//    super.copy()
//  }

//  protected fun copy(barLineScatterCandleBubbleDataSet: BarLineScatterCandleBubbleDataSet<*>) {
//    super.copy(barLineScatterCandleBubbleDataSet)
//    barLineScatterCandleBubbleDataSet.highLightColor = highLightColor
//  }
}
