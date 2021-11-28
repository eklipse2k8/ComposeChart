package com.github.eklipse2k8.charting.renderer

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.Align
import android.graphics.Path
import android.graphics.RectF
import com.github.eklipse2k8.charting.components.LimitLine.LimitLabelPosition
import com.github.eklipse2k8.charting.components.YAxis
import com.github.eklipse2k8.charting.components.YAxis.AxisDependency
import com.github.eklipse2k8.charting.components.YAxis.YAxisLabelPosition
import com.github.eklipse2k8.charting.utils.MPPointD
import com.github.eklipse2k8.charting.utils.Transformer
import com.github.eklipse2k8.charting.utils.Utils
import com.github.eklipse2k8.charting.utils.ViewPortHandler

class YAxisRendererHorizontalBarChart(
    viewPortHandler: ViewPortHandler,
    yAxis: YAxis,
    trans: Transformer?
) : YAxisRenderer(viewPortHandler, yAxis, trans) {

  init {
    limitLinePaint.textAlign = Align.LEFT
  }

  /**
   * Computes the axis values.
   *
   * @param min
   * - the minimum y-value in the data object for this axis
   * @param max
   * - the maximum y-value in the data object for this axis
   */
  override fun computeAxis(min: Float, max: Float, inverted: Boolean) {
    // calculate the starting and entry point of the y-labels (depending on
    // zoom / contentrect bounds)
    var computeYMin = min
    var computeYMax = max
    if (viewPortHandler.contentHeight() > 10 && !viewPortHandler.isFullyZoomedOutX) {
      val p1 =
          transformer!!.getValuesByTouchPoint(
              viewPortHandler.contentLeft(), viewPortHandler.contentTop())
      val p2 =
          transformer!!.getValuesByTouchPoint(
              viewPortHandler.contentRight(), viewPortHandler.contentTop())
      if (!inverted) {
        computeYMin = p1.x.toFloat()
        computeYMax = p2.x.toFloat()
      } else {
        computeYMin = p2.x.toFloat()
        computeYMax = p1.x.toFloat()
      }
      MPPointD.recycleInstance(p1)
      MPPointD.recycleInstance(p2)
    }
    computeAxisValues(computeYMin, computeYMax)
  }

  /** draws the y-axis labels to the screen */
  override fun renderAxisLabels(canvas: Canvas) {
    if (!yAxis.isEnabled || !yAxis.isDrawLabelsEnabled) return
    val positions = transformedPositions
    axisLabelPaint.typeface = yAxis.typeface
    axisLabelPaint.textSize = yAxis.textSize
    axisLabelPaint.color = yAxis.textColor
    axisLabelPaint.textAlign = Align.CENTER
    val baseYOffset = Utils.convertDpToPixel(2.5f)
    val textHeight = Utils.calcTextHeight(axisLabelPaint, "Q").toFloat()
    val dependency = yAxis.axisDependency
    val labelPosition = yAxis.labelPosition
    var yPos = 0f
    yPos =
        if (dependency === AxisDependency.LEFT) {
          if (labelPosition === YAxisLabelPosition.OUTSIDE_CHART) {
            viewPortHandler.contentTop() - baseYOffset
          } else {
            viewPortHandler.contentTop() - baseYOffset
          }
        } else {
          if (labelPosition === YAxisLabelPosition.OUTSIDE_CHART) {
            viewPortHandler.contentBottom() + textHeight + baseYOffset
          } else {
            viewPortHandler.contentBottom() + textHeight + baseYOffset
          }
        }
    drawYLabels(canvas, yPos, positions, yAxis.yOffset)
  }

  override fun renderAxisLine(canvas: Canvas) {
    if (!yAxis.isEnabled || !yAxis.isDrawAxisLineEnabled) return
    axisLinePaint.color = yAxis.axisLineColor
    axisLinePaint.strokeWidth = yAxis.axisLineWidth
    if (yAxis.axisDependency === AxisDependency.LEFT) {
      canvas!!.drawLine(
          viewPortHandler.contentLeft(),
          viewPortHandler.contentTop(),
          viewPortHandler.contentRight(),
          viewPortHandler.contentTop(),
          axisLinePaint)
    } else {
      canvas!!.drawLine(
          viewPortHandler.contentLeft(),
          viewPortHandler.contentBottom(),
          viewPortHandler.contentRight(),
          viewPortHandler.contentBottom(),
          axisLinePaint)
    }
  }

  /**
   * draws the y-labels on the specified x-position
   *
   * @param fixedPosition
   * @param positions
   */
  override fun drawYLabels(
      canvas: Canvas,
      fixedPosition: Float,
      positions: FloatArray,
      offset: Float
  ) {
    axisLabelPaint.typeface = yAxis.typeface
    axisLabelPaint.textSize = yAxis.textSize
    axisLabelPaint.color = yAxis.textColor
    val from = if (yAxis.isDrawBottomYLabelEntryEnabled) 0 else 1
    val to = if (yAxis.isDrawTopYLabelEntryEnabled) yAxis.entryCount else yAxis.entryCount - 1
    val xOffset = yAxis.labelXOffset
    for (i in from until to) {
      val text = yAxis.getFormattedLabel(i)
      canvas!!.drawText(text!!, positions[i * 2], fixedPosition - offset + xOffset, axisLabelPaint)
    }
  }

  // only fill x values, y values are not needed for x-labels
  override val transformedPositions: FloatArray
    get() {
      if (mGetTransformedPositionsBuffer.size != yAxis.entryCount * 2) {
        mGetTransformedPositionsBuffer = FloatArray(yAxis.entryCount * 2)
      }
      val positions = mGetTransformedPositionsBuffer
      var i = 0
      while (i < positions.size) {

        // only fill x values, y values are not needed for x-labels
        positions[i] = yAxis.entries[i / 2]
        i += 2
      }
      transformer!!.pointValuesToPixel(positions)
      return positions
    }

  override val gridClippingRect: RectF
    get() {
      val clippingRect = RectF()
      clippingRect.set(viewPortHandler.contentRect)
      clippingRect.inset(-axis.gridLineWidth, 0f)
      return clippingRect
    }

  override fun linePath(path: Path, i: Int, positions: FloatArray): Path {
    path.moveTo(positions[i], viewPortHandler.contentTop())
    path.lineTo(positions[i], viewPortHandler.contentBottom())
    return path
  }

  private val drawZeroLinePathBuffer = Path()

  override fun drawZeroLine(canvas: Canvas) {
    val clipRestoreCount = canvas!!.save()
    zeroLineClippingRect.set(viewPortHandler.contentRect)
    zeroLineClippingRect.inset(-yAxis.zeroLineWidth, 0f)
    canvas.clipRect(limitLineClippingRect)

    // draw zero line
    val pos = transformer!!.getPixelForValues(0f, 0f)
    zeroLinePaint.color = yAxis.zeroLineColor
    zeroLinePaint.strokeWidth = yAxis.zeroLineWidth
    val zeroLinePath = drawZeroLinePathBuffer
    zeroLinePath.reset()
    zeroLinePath.moveTo(pos.x.toFloat() - 1, viewPortHandler.contentTop())
    zeroLinePath.lineTo(pos.x.toFloat() - 1, viewPortHandler.contentBottom())

    // draw a path because lines don't support dashing on lower android versions
    canvas.drawPath(zeroLinePath, zeroLinePaint)
    canvas.restoreToCount(clipRestoreCount)
  }

  private var renderLimitLinesPathBuffer = Path()

  override var renderLimitLinesBuffer = FloatArray(4)

  /**
   * Draws the LimitLines associated with this axis to the screen. This is the standard XAxis
   * renderer using the YAxis limit lines.
   *
   * @param canvas
   */
  override fun renderLimitLines(canvas: Canvas) {
    val limitLines = yAxis.limitLines
    if (limitLines.isEmpty()) return
    val pts = renderLimitLinesBuffer
    pts[0] = 0f
    pts[1] = 0f
    pts[2] = 0f
    pts[3] = 0f
    val limitLinePath = renderLimitLinesPathBuffer
    limitLinePath.reset()
    limitLines.forEach { limitLine ->
      if (!limitLine.isEnabled) return@forEach
      val clipRestoreCount = canvas!!.save()
      limitLineClippingRect.set(viewPortHandler.contentRect)
      limitLineClippingRect.inset(-limitLine.lineWidth, 0f)
      canvas.clipRect(limitLineClippingRect)
      pts[0] = limitLine.limit
      pts[2] = limitLine.limit
      transformer!!.pointValuesToPixel(pts)
      pts[1] = viewPortHandler.contentTop()
      pts[3] = viewPortHandler.contentBottom()
      limitLinePath.moveTo(pts[0], pts[1])
      limitLinePath.lineTo(pts[2], pts[3])
      limitLinePaint.style = Paint.Style.STROKE
      limitLinePaint.color = limitLine.lineColor
      limitLinePaint.pathEffect = limitLine.dashPathEffect
      limitLinePaint.strokeWidth = limitLine.lineWidth
      canvas.drawPath(limitLinePath, limitLinePaint)
      limitLinePath.reset()
      val label = limitLine.label

      // if drawing the limit-value label is enabled
      if (label.isNotEmpty()) {
        limitLinePaint.style = limitLine.textStyle
        limitLinePaint.pathEffect = null
        limitLinePaint.color = limitLine.textColor
        limitLinePaint.typeface = limitLine.typeface
        limitLinePaint.strokeWidth = 0.5f
        limitLinePaint.textSize = limitLine.textSize
        val xOffset = limitLine.lineWidth + limitLine.xOffset
        val yOffset = Utils.convertDpToPixel(2f) + limitLine.yOffset
        val position = limitLine.labelPosition
        if (position === LimitLabelPosition.RIGHT_TOP) {
          val labelLineHeight = Utils.calcTextHeight(limitLinePaint, label).toFloat()
          limitLinePaint.textAlign = Align.LEFT
          canvas.drawText(
              label,
              pts[0] + xOffset,
              viewPortHandler.contentTop() + yOffset + labelLineHeight,
              limitLinePaint)
        } else if (position === LimitLabelPosition.RIGHT_BOTTOM) {
          limitLinePaint.textAlign = Align.LEFT
          canvas.drawText(
              label, pts[0] + xOffset, viewPortHandler.contentBottom() - yOffset, limitLinePaint)
        } else if (position === LimitLabelPosition.LEFT_TOP) {
          limitLinePaint.textAlign = Align.RIGHT
          val labelLineHeight = Utils.calcTextHeight(limitLinePaint, label).toFloat()
          canvas.drawText(
              label,
              pts[0] - xOffset,
              viewPortHandler.contentTop() + yOffset + labelLineHeight,
              limitLinePaint)
        } else {
          limitLinePaint.textAlign = Align.RIGHT
          canvas.drawText(
              label, pts[0] - xOffset, viewPortHandler.contentBottom() - yOffset, limitLinePaint)
        }
      }
      canvas.restoreToCount(clipRestoreCount)
    }
  }
}
