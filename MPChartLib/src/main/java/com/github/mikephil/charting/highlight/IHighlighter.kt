package com.github.mikephil.charting.highlight

import androidx.annotation.Nullable

/** Created by philipp on 10/06/16. */
interface IHighlighter {
  /**
   * Returns a Highlight object corresponding to the given x- and y- touch positions in pixels.
   *
   * @param x
   * @param y
   * @return
   */
  @Nullable fun getHighlight(x: Float, y: Float): Highlight?
}
