package com.github.eklipse2k8.charting.charts

import com.github.eklipse2k8.charting.renderer.scatter.*

/**
 * Predefined ScatterShapes that allow the specification of a shape a ScatterDataSet should be drawn
 * with. If a ScatterShape is specified for a ScatterDataSet, the required renderer is set.
 */
enum class ScatterShape {
  SQUARE,
  CIRCLE,
  TRIANGLE,
  CROSS,
  X,
  CHEVRON_UP,
  CHEVRON_DOWN,
}

val allDefaultShapes: Array<ScatterShape>
  get() =
      arrayOf(
          ScatterShape.SQUARE,
          ScatterShape.CIRCLE,
          ScatterShape.TRIANGLE,
          ScatterShape.CROSS,
          ScatterShape.X,
          ScatterShape.CHEVRON_UP,
          ScatterShape.CHEVRON_DOWN)

fun getRendererForShape(shape: ScatterShape): IShapeRenderer =
    when (shape) {
      ScatterShape.SQUARE -> SquareShapeRenderer()
      ScatterShape.CIRCLE -> CircleShapeRenderer()
      ScatterShape.TRIANGLE -> TriangleShapeRenderer()
      ScatterShape.CROSS -> CrossShapeRenderer()
      ScatterShape.X -> XShapeRenderer()
      ScatterShape.CHEVRON_UP -> ChevronUpShapeRenderer()
      ScatterShape.CHEVRON_DOWN -> ChevronDownShapeRenderer()
    }
