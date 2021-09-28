package com.github.mikephil.charting.data

import com.github.mikephil.charting.charts.ScatterChart.ScatterShape
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet
import com.github.mikephil.charting.renderer.scatter.ChevronDownShapeRenderer
import com.github.mikephil.charting.renderer.scatter.ChevronUpShapeRenderer
import com.github.mikephil.charting.renderer.scatter.CircleShapeRenderer
import com.github.mikephil.charting.renderer.scatter.CrossShapeRenderer
import com.github.mikephil.charting.renderer.scatter.IShapeRenderer
import com.github.mikephil.charting.renderer.scatter.SquareShapeRenderer
import com.github.mikephil.charting.renderer.scatter.TriangleShapeRenderer
import com.github.mikephil.charting.renderer.scatter.XShapeRenderer
import com.github.mikephil.charting.utils.ColorTemplate

class ScatterDataSet(yVals: List<Entry>, label: String) :
    LineScatterCandleRadarDataSet<Entry>(yVals, label), IScatterDataSet {
  /**
   * Sets the size in density pixels the drawn scattershape will have. This only applies for non
   * custom shapes.
   *
   * @param size
   */
  /** the size the scattershape will have, in density pixels */
  override var scatterShapeSize = 15f
  /**
   * Sets a new IShapeRenderer responsible for drawing this DataSet. This can also be used to set a
   * custom IShapeRenderer aside from the default ones.
   *
   * @param shapeRenderer
   */
  /** Renderer responsible for rendering this DataSet, default: square */
  override var shapeRenderer: IShapeRenderer? = SquareShapeRenderer()
  /**
   * Sets the radius of the hole in the shape (applies to Square, Circle and Triangle) Set this to
   * <= 0 to remove holes.
   *
   * @param holeRadius
   */
  /**
   * The radius of the hole in the shape (applies to Square, Circle and Triangle)
   * - default: 0.0
   */
  override var scatterShapeHoleRadius = 0f
  /**
   * Sets the color for the hole in the shape.
   *
   * @param holeColor
   */
  /**
   * Color for the hole in the shape. Setting to `ColorTemplate.COLOR_NONE` will behave as
   * transparent.
   * - default: ColorTemplate.COLOR_NONE
   */
  override var scatterShapeHoleColor = ColorTemplate.COLOR_NONE
  override fun copy(): DataSet<Entry?> {
    val entries = mutableListOf<Entry>()
    mEntries.forEach { entry -> entries.add(entry.copy()!!) }
    val copied = ScatterDataSet(entries, label!!)
    copy(copied)
    return copied
  }

  private fun copy(scatterDataSet: ScatterDataSet) {
    super.copy(scatterDataSet)
    scatterDataSet.scatterShapeSize = scatterShapeSize
    scatterDataSet.shapeRenderer = shapeRenderer
    scatterDataSet.scatterShapeHoleRadius = scatterShapeHoleRadius
    scatterDataSet.scatterShapeHoleColor = scatterShapeHoleColor
  }

  /**
   * Sets the ScatterShape this DataSet should be drawn with. This will search for an available
   * IShapeRenderer and set this renderer for the DataSet.
   *
   * @param shape
   */
  fun setScatterShape(shape: ScatterShape?) {
    shapeRenderer = getRendererForShape(shape)
  }

  companion object {
    fun getRendererForShape(shape: ScatterShape?): IShapeRenderer? {
      when (shape) {
        ScatterShape.SQUARE -> return SquareShapeRenderer()
        ScatterShape.CIRCLE -> return CircleShapeRenderer()
        ScatterShape.TRIANGLE -> return TriangleShapeRenderer()
        ScatterShape.CROSS -> return CrossShapeRenderer()
        ScatterShape.X -> return XShapeRenderer()
        ScatterShape.CHEVRON_UP -> return ChevronUpShapeRenderer()
        ScatterShape.CHEVRON_DOWN -> return ChevronDownShapeRenderer()
      }
      return null
    }
  }
}
