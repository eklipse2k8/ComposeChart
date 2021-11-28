package com.github.eklipse2k8.charting.renderer

import android.graphics.*
import android.graphics.Paint.Align
import com.github.eklipse2k8.charting.components.LimitLine
import com.github.eklipse2k8.charting.components.LimitLine.LimitLabelPosition
import com.github.eklipse2k8.charting.components.XAxis
import com.github.eklipse2k8.charting.components.XAxis.XAxisPosition
import com.github.eklipse2k8.charting.utils.*
import kotlin.math.roundToInt

open class XAxisRenderer(
    viewPortHandler: ViewPortHandler,
    @JvmField protected var xAxis: XAxis,
    trans: Transformer?
) : AxisRenderer(viewPortHandler, trans, xAxis) {

  private fun setupGridPaint() {
    gridPaint.color = xAxis.gridColor
    gridPaint.strokeWidth = xAxis.gridLineWidth
    gridPaint.pathEffect = xAxis.gridDashPathEffect
  }

  override fun computeAxis(min: Float, max: Float, inverted: Boolean) {
    if (transformer == null) return
    // calculate the starting and entry point of the y-labels (depending on
    // zoom / contentrect bounds)
    var computeMin = min
    var computeMax = max
    if (viewPortHandler.contentWidth() > 10 && !viewPortHandler.isFullyZoomedOutX) {
      val p1 =
          transformer!!.getValuesByTouchPoint(
              viewPortHandler.contentLeft(), viewPortHandler.contentTop())
      val p2 =
          transformer!!.getValuesByTouchPoint(
              viewPortHandler.contentRight(), viewPortHandler.contentTop())
      if (inverted) {
        computeMin = p2.x.toFloat()
        computeMax = p1.x.toFloat()
      } else {
        computeMin = p1.x.toFloat()
        computeMax = p2.x.toFloat()
      }
      MPPointD.recycleInstance(p1)
      MPPointD.recycleInstance(p2)
    }
    computeAxisValues(computeMin, computeMax)
  }

  override fun computeAxisValues(min: Float, max: Float) {
    super.computeAxisValues(min, max)
    computeSize()
  }

  protected open fun computeSize() {
    val longest = xAxis.longestLabel
    axisLabelPaint.typeface = xAxis.typeface
    axisLabelPaint.textSize = xAxis.textSize
    val labelSize = Utils.calcTextSize(axisLabelPaint, longest)
    val labelWidth = labelSize.width
    val labelHeight = Utils.calcTextHeight(axisLabelPaint, "Q").toFloat()
    val labelRotatedSize =
        Utils.getSizeOfRotatedRectangleByDegrees(labelWidth, labelHeight, xAxis.labelRotationAngle)
    xAxis.mLabelWidth = labelWidth.roundToInt()
    xAxis.mLabelHeight = labelHeight.roundToInt()
    xAxis.mLabelRotatedWidth = labelRotatedSize.width.roundToInt()
    xAxis.mLabelRotatedHeight = labelRotatedSize.height.roundToInt()
    FSize.recycleInstance(labelRotatedSize)
    FSize.recycleInstance(labelSize)
  }

  override fun renderAxisLabels(canvas: Canvas?) {
    if (!xAxis.isEnabled || !xAxis.isDrawLabelsEnabled) return
    val yoffset = xAxis.yOffset
    axisLabelPaint.typeface = xAxis.typeface
    axisLabelPaint.textSize = xAxis.textSize
    axisLabelPaint.color = xAxis.textColor
    val pointF = MPPointF.getInstance(0f, 0f)
    if (xAxis.position === XAxisPosition.TOP) {
      pointF.x = 0.5f
      pointF.y = 1.0f
      drawLabels(canvas, viewPortHandler.contentTop() - yoffset, pointF)
    } else if (xAxis.position === XAxisPosition.TOP_INSIDE) {
      pointF.x = 0.5f
      pointF.y = 1.0f
      drawLabels(canvas, viewPortHandler.contentTop() + yoffset + xAxis.mLabelRotatedHeight, pointF)
    } else if (xAxis.position === XAxisPosition.BOTTOM) {
      pointF.x = 0.5f
      pointF.y = 0.0f
      drawLabels(canvas, viewPortHandler.contentBottom() + yoffset, pointF)
    } else if (xAxis.position === XAxisPosition.BOTTOM_INSIDE) {
      pointF.x = 0.5f
      pointF.y = 0.0f
      drawLabels(
          canvas, viewPortHandler.contentBottom() - yoffset - xAxis.mLabelRotatedHeight, pointF)
    } else { // BOTH SIDED
      pointF.x = 0.5f
      pointF.y = 1.0f
      drawLabels(canvas, viewPortHandler.contentTop() - yoffset, pointF)
      pointF.x = 0.5f
      pointF.y = 0.0f
      drawLabels(canvas, viewPortHandler.contentBottom() + yoffset, pointF)
    }
    MPPointF.recycleInstance(pointF)
  }

  override fun renderAxisLine(canvas: Canvas?) {
    if (!xAxis.isDrawAxisLineEnabled || !xAxis.isEnabled) return
    axisLinePaint.color = xAxis.axisLineColor
    axisLinePaint.strokeWidth = xAxis.axisLineWidth
    axisLinePaint.pathEffect = xAxis.axisLineDashPathEffect
    if (xAxis.position === XAxisPosition.TOP ||
        xAxis.position === XAxisPosition.TOP_INSIDE ||
        xAxis.position === XAxisPosition.BOTH_SIDED) {
      canvas!!.drawLine(
          viewPortHandler.contentLeft(),
          viewPortHandler.contentTop(),
          viewPortHandler.contentRight(),
          viewPortHandler.contentTop(),
          axisLinePaint)
    }
    if (xAxis.position === XAxisPosition.BOTTOM ||
        xAxis.position === XAxisPosition.BOTTOM_INSIDE ||
        xAxis.position === XAxisPosition.BOTH_SIDED) {
      canvas!!.drawLine(
          viewPortHandler.contentLeft(),
          viewPortHandler.contentBottom(),
          viewPortHandler.contentRight(),
          viewPortHandler.contentBottom(),
          axisLinePaint)
    }
  }

  /**
   * draws the x-labels on the specified y-position
   *
   * @param pos
   */
  protected open fun drawLabels(c: Canvas?, pos: Float, anchor: MPPointF?) {
    val labelRotationAngleDegrees = xAxis.labelRotationAngle
    val centeringEnabled = xAxis.isCenterAxisLabelsEnabled
    val positions = FloatArray(xAxis.entryCount * 2)
    run {
      var i = 0
      while (i < positions.size) {

        // only fill x values
        if (centeringEnabled) {
          positions[i] = xAxis.centeredEntries[i / 2]
        } else {
          positions[i] = xAxis.entries[i / 2]
        }
        i += 2
      }
    }
    transformer?.pointValuesToPixel(positions)
    var i = 0
    while (i < positions.size) {
      var x = positions[i]
      if (viewPortHandler.isInBoundsX(x)) {
        val label = xAxis.valueFormatter!!.getFormattedValue(xAxis.entries[i / 2], xAxis)
        if (xAxis.isAvoidFirstLastClippingEnabled) {

          // avoid clipping of the last
          if (i / 2 == xAxis.entryCount - 1 && xAxis.entryCount > 1) {
            val width = Utils.calcTextWidth(axisLabelPaint, label).toFloat()
            if (width > viewPortHandler.offsetRight() * 2 && x + width > viewPortHandler.chartWidth)
                x -= width / 2

            // avoid clipping of the first
          } else if (i == 0) {
            val width = Utils.calcTextWidth(axisLabelPaint, label).toFloat()
            x += width / 2
          }
        }
        drawLabel(c, label, x, pos, anchor, labelRotationAngleDegrees)
      }
      i += 2
    }
  }

  protected fun drawLabel(
      c: Canvas?,
      formattedLabel: String?,
      x: Float,
      y: Float,
      anchor: MPPointF?,
      angleDegrees: Float
  ) {
    Utils.drawXAxisValue(c, formattedLabel, x, y, axisLabelPaint, anchor, angleDegrees)
  }

  private var mRenderGridLinesPath = Path()

  private var mRenderGridLinesBuffer = FloatArray(2)

  override fun renderGridLines(canvas: Canvas?) {
    if (!xAxis.isDrawGridLinesEnabled || !xAxis.isEnabled) return
    val clipRestoreCount = canvas!!.save()
    canvas.clipRect(gridClippingRect!!)
    if (mRenderGridLinesBuffer.size != axis.entryCount * 2) {
      mRenderGridLinesBuffer = FloatArray(xAxis.entryCount * 2)
    }
    val positions = mRenderGridLinesBuffer
    run {
      var i = 0
      while (i < positions.size) {
        positions[i] = xAxis.entries[i / 2]
        positions[i + 1] = xAxis.entries[i / 2]
        i += 2
      }
    }
    transformer?.pointValuesToPixel(positions)
    setupGridPaint()
    val gridLinePath = mRenderGridLinesPath
    gridLinePath.reset()
    var i = 0
    while (i < positions.size) {
      drawGridLine(canvas, positions[i], positions[i + 1], gridLinePath)
      i += 2
    }
    canvas.restoreToCount(clipRestoreCount)
  }

  @JvmField protected var mGridClippingRect = RectF()

  open val gridClippingRect: RectF?
    get() {
      mGridClippingRect.set(viewPortHandler.contentRect)
      mGridClippingRect.inset(-axis.gridLineWidth, 0f)
      return mGridClippingRect
    }

  /**
   * Draws the grid line at the specified position using the provided path.
   *
   * @param canvas
   * @param x
   * @param y
   * @param gridLinePath
   */
  protected open fun drawGridLine(canvas: Canvas?, x: Float, y: Float, gridLinePath: Path) {
    gridLinePath.moveTo(x, viewPortHandler.contentBottom())
    gridLinePath.lineTo(x, viewPortHandler.contentTop())

    // draw a path because lines don't support dashing on lower android versions
    canvas!!.drawPath(gridLinePath, gridPaint)
    gridLinePath.reset()
  }

  @JvmField protected var renderLimitLinesBuffer = FloatArray(2)

  @JvmField protected var limitLineClippingRect = RectF()

  /**
   * Draws the LimitLines associated with this axis to the screen.
   *
   * @param canvas
   */
  override fun renderLimitLines(canvas: Canvas?) {
    val limitLines = xAxis.limitLines
    if (limitLines.isEmpty()) return
    val position = renderLimitLinesBuffer
    position[0] = 0f
    position[1] = 0f
    for (i in limitLines.indices) {
      val l = limitLines[i]
      if (!l.isEnabled) continue
      val clipRestoreCount = canvas!!.save()
      limitLineClippingRect.set(viewPortHandler.contentRect)
      limitLineClippingRect.inset(-l.lineWidth, 0f)
      canvas.clipRect(limitLineClippingRect)
      position[0] = l.limit
      position[1] = 0f
      transformer?.pointValuesToPixel(position)
      renderLimitLineLine(canvas, l, position)
      renderLimitLineLabel(canvas, l, position, 2f + l.yOffset)
      canvas.restoreToCount(clipRestoreCount)
    }
  }

  var limitLineSegmentsBuffer = FloatArray(4)

  private val limitLinePath = Path()

  private fun renderLimitLineLine(canvas: Canvas, limitLine: LimitLine, position: FloatArray) {
    limitLineSegmentsBuffer[0] = position[0]
    limitLineSegmentsBuffer[1] = viewPortHandler.contentTop()
    limitLineSegmentsBuffer[2] = position[0]
    limitLineSegmentsBuffer[3] = viewPortHandler.contentBottom()
    limitLinePath.reset()
    limitLinePath.moveTo(limitLineSegmentsBuffer[0], limitLineSegmentsBuffer[1])
    limitLinePath.lineTo(limitLineSegmentsBuffer[2], limitLineSegmentsBuffer[3])
    limitLinePaint.style = Paint.Style.STROKE
    limitLinePaint.color = limitLine.lineColor
    limitLinePaint.strokeWidth = limitLine.lineWidth
    limitLinePaint.pathEffect = limitLine.dashPathEffect
    canvas.drawPath(limitLinePath, limitLinePaint)
  }

  private fun renderLimitLineLabel(
      canvas: Canvas,
      limitLine: LimitLine,
      position: FloatArray,
      yOffset: Float
  ) {
    val label = limitLine.label

    // if drawing the limit-value label is enabled
    if (label.isNotEmpty()) {
      limitLinePaint.style = limitLine.textStyle
      limitLinePaint.pathEffect = null
      limitLinePaint.color = limitLine.textColor
      limitLinePaint.strokeWidth = 0.5f
      limitLinePaint.textSize = limitLine.textSize
      val xOffset = limitLine.lineWidth + limitLine.xOffset
      val labelPosition = limitLine.labelPosition
      if (labelPosition === LimitLabelPosition.RIGHT_TOP) {
        val labelLineHeight = Utils.calcTextHeight(limitLinePaint, label).toFloat()
        limitLinePaint.textAlign = Align.LEFT
        canvas.drawText(
            label,
            position[0] + xOffset,
            viewPortHandler.contentTop() + yOffset + labelLineHeight,
            limitLinePaint)
      } else if (labelPosition === LimitLabelPosition.RIGHT_BOTTOM) {
        limitLinePaint.textAlign = Align.LEFT
        canvas.drawText(
            label, position[0] + xOffset, viewPortHandler.contentBottom() - yOffset, limitLinePaint)
      } else if (labelPosition === LimitLabelPosition.LEFT_TOP) {
        limitLinePaint.textAlign = Align.RIGHT
        val labelLineHeight = Utils.calcTextHeight(limitLinePaint, label).toFloat()
        canvas.drawText(
            label,
            position[0] - xOffset,
            viewPortHandler.contentTop() + yOffset + labelLineHeight,
            limitLinePaint)
      } else {
        limitLinePaint.textAlign = Align.RIGHT
        canvas.drawText(
            label, position[0] - xOffset, viewPortHandler.contentBottom() - yOffset, limitLinePaint)
      }
    }
  }
}
