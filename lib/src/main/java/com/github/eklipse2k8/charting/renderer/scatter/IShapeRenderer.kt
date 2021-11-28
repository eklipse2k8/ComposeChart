package com.github.eklipse2k8.charting.renderer.scatter

import android.graphics.Canvas
import android.graphics.Paint
import com.github.eklipse2k8.charting.interfaces.datasets.IScatterDataSet
import com.github.eklipse2k8.charting.utils.ViewPortHandler

/** Created by wajdic on 15/06/2016. Created at Time 09:07 */
interface IShapeRenderer {
  /**
   * Renders the provided ScatterDataSet with a shape.
   *
   * @param c Canvas object for drawing the shape
   * @param dataSet The DataSet to be drawn
   * @param viewPortHandler Contains information about the current state of the view
   * @param posX Position to draw the shape at
   * @param posY Position to draw the shape at
   * @param renderPaint Paint object used for styling and drawing
   */
  fun renderShape(
      c: Canvas,
      dataSet: IScatterDataSet,
      viewPortHandler: ViewPortHandler,
      posX: Float,
      posY: Float,
      renderPaint: Paint?
  )
}
