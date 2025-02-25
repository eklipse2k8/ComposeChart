package com.github.eklipse2k8.charting.interfaces.datasets

import com.github.eklipse2k8.charting.data.Entry
import com.github.eklipse2k8.charting.renderer.scatter.IShapeRenderer

/** Created by philipp on 21/10/15. */
interface IScatterDataSet : ILineScatterCandleRadarDataSet<Entry> {
  /**
   * Returns the currently set scatter shape size
   *
   * @return
   */
  val scatterShapeSize: Float

  /**
   * Returns radius of the hole in the shape
   */
  val scatterShapeHoleRadius: Float

  /**
   * Returns the color for the hole in the shape
   */
  val scatterShapeHoleColor: Int

  /**
   * Returns the IShapeRenderer responsible for rendering this DataSet.
   */
  val shapeRenderer: IShapeRenderer?
}
