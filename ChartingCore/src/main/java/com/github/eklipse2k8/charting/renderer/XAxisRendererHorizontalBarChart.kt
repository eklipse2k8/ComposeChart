package com.github.eklipse2k8.charting.renderer

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.Align
import android.graphics.Path
import android.graphics.RectF
import com.github.eklipse2k8.charting.components.LimitLine.LimitLabelPosition
import com.github.eklipse2k8.charting.components.XAxis
import com.github.eklipse2k8.charting.components.XAxis.XAxisPosition
import com.github.eklipse2k8.charting.utils.*
import kotlin.math.roundToInt

class XAxisRendererHorizontalBarChart(
    viewPortHandler: ViewPortHandler,
    xAxis: XAxis,
    trans: Transformer,
) : XAxisRenderer(viewPortHandler, xAxis, trans) {

  override fun computeAxis(min: Float, max: Float, inverted: Boolean) {
    if (transformer == null) return
    // calculate the starting and entry point of the y-labels (depending on
    // zoom / contentrect bounds)
    var computeMin = min
    var computeMax = max
    if (viewPortHandler.contentWidth() > 10 && !viewPortHandler.isFullyZoomedOutY) {
      val p1 =
          transformer!!.getValuesByTouchPoint(
              viewPortHandler.contentLeft(), viewPortHandler.contentBottom())
      val p2 =
          transformer!!.getValuesByTouchPoint(
              viewPortHandler.contentLeft(), viewPortHandler.contentTop())
      if (inverted) {
        computeMin = p2.y.toFloat()
        computeMax = p1.y.toFloat()
      } else {
        computeMin = p1.y.toFloat()
        computeMax = p2.y.toFloat()
      }
      MPPointD.recycleInstance(p1)
      MPPointD.recycleInstance(p2)
    }
    computeAxisValues(computeMin, computeMax)
  }

  override fun computeSize() {
    axisLabelPaint.typeface = xAxis.typeface
    axisLabelPaint.textSize = xAxis.textSize
    val longest = xAxis.longestLabel
    val labelSize = Utils.calcTextSize(axisLabelPaint, longest)
    val labelWidth = labelSize.width + xAxis.xOffset * 3.5f
    val labelHeight = labelSize.height
    val labelRotatedSize =
        Utils.getSizeOfRotatedRectangleByDegrees(
            labelSize.width, labelHeight, xAxis.labelRotationAngle)
    xAxis.labelWidth = labelWidth.roundToInt()
    xAxis.labelHeight = labelHeight.roundToInt()
    xAxis.labelRotatedWidth = (labelRotatedSize.width + xAxis.xOffset * 3.5f).toInt()
    xAxis.labelRotatedHeight = labelRotatedSize.height.roundToInt()
    FSize.recycleInstance(labelRotatedSize)
  }

  override fun renderAxisLabels(canvas: Canvas) {
    if (!xAxis.isEnabled || !xAxis.isDrawLabelsEnabled) return
    val xoffset = xAxis.xOffset
    axisLabelPaint.typeface = xAxis.typeface
    axisLabelPaint.textSize = xAxis.textSize
    axisLabelPaint.color = xAxis.textColor
    val pointF = MPPointF.getInstance(0f, 0f)
    if (xAxis.position === XAxisPosition.TOP) {
      pointF.x = 0.0f
      pointF.y = 0.5f
      drawLabels(canvas, viewPortHandler.contentRight() + xoffset, pointF)
    } else if (xAxis.position === XAxisPosition.TOP_INSIDE) {
      pointF.x = 1.0f
      pointF.y = 0.5f
      drawLabels(canvas, viewPortHandler.contentRight() - xoffset, pointF)
    } else if (xAxis.position === XAxisPosition.BOTTOM) {
      pointF.x = 1.0f
      pointF.y = 0.5f
      drawLabels(canvas, viewPortHandler.contentLeft() - xoffset, pointF)
    } else if (xAxis.position === XAxisPosition.BOTTOM_INSIDE) {
      pointF.x = 1.0f
      pointF.y = 0.5f
      drawLabels(canvas, viewPortHandler.contentLeft() + xoffset, pointF)
    } else { // BOTH SIDED
      pointF.x = 0.0f
      pointF.y = 0.5f
      drawLabels(canvas, viewPortHandler.contentRight() + xoffset, pointF)
      pointF.x = 1.0f
      pointF.y = 0.5f
      drawLabels(canvas, viewPortHandler.contentLeft() - xoffset, pointF)
    }
    MPPointF.recycleInstance(pointF)
  }

  override fun drawLabels(c: Canvas, pos: Float, anchor: MPPointF?) {
    val labelRotationAngleDegrees = xAxis.labelRotationAngle
    val centeringEnabled = xAxis.isCenterAxisLabelsEnabled
    val positions = FloatArray(xAxis.entryCount * 2)
    for (i in positions.indices step 2) {
      // only fill x values
      if (centeringEnabled) {
        positions[i + 1] = xAxis.centeredEntries[i / 2]
      } else {
        positions[i + 1] = xAxis.entries[i / 2]
      }
    }
    transformer?.pointValuesToPixel(positions)
    for (i in positions.indices step 2) {
      val y = positions[i + 1]
      if (viewPortHandler.isInBoundsY(y)) {
        val label = xAxis.valueFormatter!!.getFormattedValue(xAxis.entries[i / 2], xAxis)
        drawLabel(c, label, pos, y, anchor, labelRotationAngleDegrees)
      }
    }
  }

  override val gridClippingRect: RectF
    get() {
      _gridClippingRect.set(viewPortHandler.contentRect)
      _gridClippingRect.inset(0f, -axis.gridLineWidth)
      return _gridClippingRect
    }

  override fun drawGridLine(canvas: Canvas, x: Float, y: Float, gridLinePath: Path) {
    gridLinePath.moveTo(viewPortHandler.contentRight(), y)
    gridLinePath.lineTo(viewPortHandler.contentLeft(), y)

    // draw a path because lines don't support dashing on lower android versions
    canvas.drawPath(gridLinePath, gridPaint)
    gridLinePath.reset()
  }

  override fun renderAxisLine(canvas: Canvas) {
    if (!xAxis.isDrawAxisLineEnabled || !xAxis.isEnabled) return
    axisLinePaint.color = xAxis.axisLineColor
    axisLinePaint.strokeWidth = xAxis.axisLineWidth
    when (xAxis.position) {
      XAxisPosition.TOP, XAxisPosition.TOP_INSIDE, XAxisPosition.BOTH_SIDED ->
          canvas.drawLine(
              viewPortHandler.contentRight(),
              viewPortHandler.contentTop(),
              viewPortHandler.contentRight(),
              viewPortHandler.contentBottom(),
              axisLinePaint)
      XAxisPosition.BOTTOM, XAxisPosition.BOTTOM_INSIDE ->
          canvas.drawLine(
              viewPortHandler.contentLeft(),
              viewPortHandler.contentTop(),
              viewPortHandler.contentLeft(),
              viewPortHandler.contentBottom(),
              axisLinePaint)
    }
  }

  private var renderLimitLinesPathBuffer = Path()

  /**
   * Draws the LimitLines associated with this axis to the screen. This is the standard YAxis
   * renderer using the XAxis limit lines.
   *
   * @param canvas
   */
  override fun renderLimitLines(canvas: Canvas) {
    val limitLines = xAxis.limitLines
    if (limitLines.isEmpty()) return
    val pts = renderLimitLinesBuffer
    pts[0] = 0f
    pts[1] = 0f
    val limitLinePath = renderLimitLinesPathBuffer
    limitLinePath.reset()
    limitLines.forEach { limitLine ->
      if (!limitLine.isEnabled) return@forEach
      val clipRestoreCount = canvas.save()
      limitLineClippingRect.set(viewPortHandler.contentRect)
      limitLineClippingRect.inset(0f, -limitLine.lineWidth)
      canvas.clipRect(limitLineClippingRect)
      limitLinePaint.style = Paint.Style.STROKE
      limitLinePaint.color = limitLine.lineColor
      limitLinePaint.strokeWidth = limitLine.lineWidth
      limitLinePaint.pathEffect = limitLine.dashPathEffect
      pts[1] = limitLine.limit
      transformer?.pointValuesToPixel(pts)
      limitLinePath.moveTo(viewPortHandler.contentLeft(), pts[1])
      limitLinePath.lineTo(viewPortHandler.contentRight(), pts[1])
      canvas.drawPath(limitLinePath, limitLinePaint)
      limitLinePath.reset()
      // c.drawLines(pts, mLimitLinePaint);
      val label = limitLine.label

      // if drawing the limit-value label is enabled
      if (label.isNotEmpty()) {
        limitLinePaint.style = limitLine.textStyle
        limitLinePaint.pathEffect = null
        limitLinePaint.color = limitLine.textColor
        limitLinePaint.strokeWidth = 0.5f
        limitLinePaint.textSize = limitLine.textSize
        val labelLineHeight = Utils.calcTextHeight(limitLinePaint, label).toFloat()
        val xOffset = Utils.convertDpToPixel(4f) + limitLine.xOffset
        val yOffset = limitLine.lineWidth + labelLineHeight + limitLine.yOffset
        val position = limitLine.labelPosition
        if (position === LimitLabelPosition.RIGHT_TOP) {
          limitLinePaint.textAlign = Align.RIGHT
          canvas.drawText(
              label,
              viewPortHandler.contentRight() - xOffset,
              pts[1] - yOffset + labelLineHeight,
              limitLinePaint)
        } else if (position === LimitLabelPosition.RIGHT_BOTTOM) {
          limitLinePaint.textAlign = Align.RIGHT
          canvas.drawText(
              label, viewPortHandler.contentRight() - xOffset, pts[1] + yOffset, limitLinePaint)
        } else if (position === LimitLabelPosition.LEFT_TOP) {
          limitLinePaint.textAlign = Align.LEFT
          canvas.drawText(
              label,
              viewPortHandler.contentLeft() + xOffset,
              pts[1] - yOffset + labelLineHeight,
              limitLinePaint)
        } else {
          limitLinePaint.textAlign = Align.LEFT
          canvas.drawText(
              label, viewPortHandler.offsetLeft() + xOffset, pts[1] + yOffset, limitLinePaint)
        }
      }
      canvas.restoreToCount(clipRestoreCount)
    }
  }
}
