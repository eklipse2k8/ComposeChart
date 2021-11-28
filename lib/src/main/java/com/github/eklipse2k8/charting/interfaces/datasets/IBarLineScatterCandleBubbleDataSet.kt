package com.github.eklipse2k8.charting.interfaces.datasets

import com.github.eklipse2k8.charting.data.Entry

/** Created by philipp on 21/10/15. */
interface IBarLineScatterCandleBubbleDataSet<T : Entry> : IDataSet<T> {
  /**
   * Returns the color that is used for drawing the highlight indicators.
   *
   * @return
   */
  val highLightColor: Int
}
