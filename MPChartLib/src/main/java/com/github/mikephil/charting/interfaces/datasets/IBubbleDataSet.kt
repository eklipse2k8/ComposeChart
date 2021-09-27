package com.github.mikephil.charting.interfaces.datasets

import com.github.mikephil.charting.data.Entry

/** Created by philipp on 21/10/15. */
interface IBubbleDataSet : IBarLineScatterCandleBubbleDataSet<Entry> {
  val maxSize: Float
  val isNormalizeSizeEnabled: Boolean
  /**
   * Returns the width of the highlight-circle that surrounds the bubble Sets the width of the
   * circle that surrounds the bubble when highlighted, in dp.
   *
   * @return
   */
  var highlightCircleWidth: Float
}
