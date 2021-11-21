package com.github.eklipse2k8.charting.renderer

import android.graphics.*
import android.graphics.Paint.Align
import com.github.eklipse2k8.charting.components.LimitLine.LimitLabelPosition
import com.github.eklipse2k8.charting.components.YAxis
import com.github.eklipse2k8.charting.components.YAxis.AxisDependency
import com.github.eklipse2k8.charting.components.YAxis.YAxisLabelPosition
import com.github.eklipse2k8.charting.utils.Transformer
import com.github.eklipse2k8.charting.utils.Utils
import com.github.eklipse2k8.charting.utils.ViewPortHandler

open class YAxisRenderer(
    viewPortHandler: ViewPortHandler,
    @JvmField protected var yAxis: YAxis,
    trans: Transformer?
) : AxisRenderer(viewPortHandler, trans, yAxis) {
  @JvmField
  protected var zeroLinePaint: Paint =
      Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
        strokeWidth = 1f
        style = Paint.Style.STROKE
      }

  init {
    axisLabelPaint.color = Color.BLACK
    axisLabelPaint.textSize = Utils.convertDpToPixel(10f)
  }

  /** draws the y-axis labels to the screen */
  override fun renderAxisLabels(canvas: Canvas?) {
    if (!yAxis.isEnabled || !yAxis.isDrawLabelsEnabled) return
    val positions = transformedPositions
    axisLabelPaint.typeface = yAxis.typeface
    axisLabelPaint.textSize = yAxis.textSize
    axisLabelPaint.color = yAxis.textColor
    val xoffset = yAxis.xOffset
    val yoffset = Utils.calcTextHeight(axisLabelPaint, "A") / 2.5f + yAxis.yOffset
    val dependency = yAxis.axisDependency
    val labelPosition = yAxis.labelPosition
    var xPos = 0f
    if (dependency === AxisDependency.LEFT) {
      if (labelPosition === YAxisLabelPosition.OUTSIDE_CHART) {
        axisLabelPaint.textAlign = Align.RIGHT
        xPos = viewPortHandler.offsetLeft() - xoffset
      } else {
        axisLabelPaint.textAlign = Align.LEFT
        xPos = viewPortHandler.offsetLeft() + xoffset
      }
    } else {
      if (labelPosition === YAxisLabelPosition.OUTSIDE_CHART) {
        axisLabelPaint.textAlign = Align.LEFT
        xPos = viewPortHandler.contentRight() + xoffset
      } else {
        axisLabelPaint.textAlign = Align.RIGHT
        xPos = viewPortHandler.contentRight() - xoffset
      }
    }
    drawYLabels(canvas, xPos, positions, yoffset)
  }

  override fun renderAxisLine(canvas: Canvas?) {
    if (!yAxis.isEnabled || !yAxis.isDrawAxisLineEnabled) return
    axisLinePaint.color = yAxis.axisLineColor
    axisLinePaint.strokeWidth = yAxis.axisLineWidth
    if (yAxis.axisDependency === AxisDependency.LEFT) {
      canvas!!.drawLine(
          viewPortHandler.contentLeft(),
          viewPortHandler.contentTop(),
          viewPortHandler.contentLeft(),
          viewPortHandler.contentBottom(),
          axisLinePaint)
    } else {
      canvas!!.drawLine(
          viewPortHandler.contentRight(),
          viewPortHandler.contentTop(),
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
  protected open fun drawYLabels(
      canvas: Canvas?,
      fixedPosition: Float,
      positions: FloatArray,
      offset: Float
  ) {
    val from = if (yAxis.isDrawBottomYLabelEntryEnabled) 0 else 1
    val to = if (yAxis.isDrawTopYLabelEntryEnabled) yAxis.mEntryCount else yAxis.mEntryCount - 1
    val xOffset = yAxis.labelXOffset

    // draw
    for (i in from until to) {
      val text = yAxis.getFormattedLabel(i)
      canvas!!.drawText(
          text!!, fixedPosition + xOffset, positions[i * 2 + 1] + offset, axisLabelPaint)
    }
  }

  @JvmField protected var renderGridLinesPath = Path()

  override fun renderGridLines(canvas: Canvas?) {
    if (!yAxis.isEnabled) return
    if (yAxis.isDrawGridLinesEnabled) {
      val clipRestoreCount = canvas!!.save()
      canvas.clipRect(gridClippingRect)
      val positions = transformedPositions
      gridPaint.color = yAxis.gridColor
      gridPaint.strokeWidth = yAxis.gridLineWidth
      gridPaint.pathEffect = yAxis.gridDashPathEffect
      val gridLinePath = renderGridLinesPath
      gridLinePath.reset()

      // draw the grid
      var i = 0
      while (i < positions.size) {

        // draw a path because lines don't support dashing on lower android versions
        canvas.drawPath(linePath(gridLinePath, i, positions)!!, gridPaint)
        gridLinePath.reset()
        i += 2
      }
      canvas.restoreToCount(clipRestoreCount)
    }
    if (yAxis.isDrawZeroLineEnabled) {
      drawZeroLine(canvas)
    }
  }

  open val gridClippingRect: RectF
    get() {
      val clippingRect = RectF()
      clippingRect.set(viewPortHandler.contentRect)
      clippingRect.inset(0f, -axis.gridLineWidth)
      return clippingRect
    }

  /**
   * Calculates the path for a grid line.
   *
   * @param path
   * @param i
   * @param positions
   * @return
   */
  protected open fun linePath(path: Path, i: Int, positions: FloatArray): Path? {
    path.moveTo(viewPortHandler.offsetLeft(), positions[i + 1])
    path.lineTo(viewPortHandler.contentRight(), positions[i + 1])
    return path
  }

  @JvmField
  protected var mGetTransformedPositionsBuffer =
      FloatArray(2) // only fill y values, x values are not needed for y-labels

  /**
   * Transforms the values contained in the axis entries to screen pixels and returns them in form
   * of a float array of x- and y-coordinates.
   *
   * @return
   */
  protected open val transformedPositions: FloatArray
    get() {
      if (mGetTransformedPositionsBuffer.size != yAxis.mEntryCount * 2) {
        mGetTransformedPositionsBuffer = FloatArray(yAxis.mEntryCount * 2)
      }
      val positions = mGetTransformedPositionsBuffer
      var i = 0
      while (i < positions.size) {

        // only fill y values, x values are not needed for y-labels
        positions[i + 1] = yAxis.mEntries[i / 2]
        i += 2
      }
      transformer?.pointValuesToPixel(positions)
      return positions
    }

  @JvmField protected var mDrawZeroLinePath = Path()

  @JvmField protected var mZeroLineClippingRect = RectF()

  /** Draws the zero line. */
  protected open fun drawZeroLine(c: Canvas?) {
    val clipRestoreCount = c!!.save()
    mZeroLineClippingRect.set(viewPortHandler.contentRect)
    mZeroLineClippingRect.inset(0f, -yAxis.zeroLineWidth)
    c.clipRect(mZeroLineClippingRect)

    // draw zero line
    if (transformer == null) {
      return
    }
    val pos = transformer!!.getPixelForValues(0f, 0f)
    zeroLinePaint.color = yAxis.zeroLineColor
    zeroLinePaint.strokeWidth = yAxis.zeroLineWidth
    val zeroLinePath = mDrawZeroLinePath
    zeroLinePath.reset()
    zeroLinePath.moveTo(viewPortHandler.contentLeft(), pos.y.toFloat())
    zeroLinePath.lineTo(viewPortHandler.contentRight(), pos.y.toFloat())

    // draw a path because lines don't support dashing on lower android versions
    c.drawPath(zeroLinePath, zeroLinePaint)
    c.restoreToCount(clipRestoreCount)
  }

  @JvmField protected var renderLimitLines = Path()

  protected open var renderLimitLinesBuffer = FloatArray(2)

  @JvmField protected var limitLineClippingRect = RectF()

  /**
   * Draws the LimitLines associated with this axis to the screen.
   *
   * @param canvas
   */
  override fun renderLimitLines(canvas: Canvas?) {
    val limitLines = yAxis.limitLines
    if (limitLines.isEmpty()) return
    val pts = renderLimitLinesBuffer
    pts[0] = 0f
    pts[1] = 0f
    val limitLinePath = renderLimitLines
    limitLinePath.reset()
    for (i in limitLines.indices) {
      val l = limitLines[i]
      if (!l.isEnabled) continue
      val clipRestoreCount = canvas!!.save()
      limitLineClippingRect.set(viewPortHandler.contentRect)
      limitLineClippingRect.inset(0f, -l.lineWidth)
      canvas.clipRect(limitLineClippingRect)
      limitLinePaint.style = Paint.Style.STROKE
      limitLinePaint.color = l.lineColor
      limitLinePaint.strokeWidth = l.lineWidth
      limitLinePaint.pathEffect = l.dashPathEffect
      pts[1] = l.limit
      transformer?.pointValuesToPixel(pts)
      limitLinePath.moveTo(viewPortHandler.contentLeft(), pts[1])
      limitLinePath.lineTo(viewPortHandler.contentRight(), pts[1])
      canvas.drawPath(limitLinePath, limitLinePaint)
      limitLinePath.reset()
      // c.drawLines(pts, mLimitLinePaint);
      val label = l.label

      // if drawing the limit-value label is enabled
      if (label.isNotEmpty()) {
        limitLinePaint.style = l.textStyle
        limitLinePaint.pathEffect = null
        limitLinePaint.color = l.textColor
        limitLinePaint.typeface = l.typeface
        limitLinePaint.strokeWidth = 0.5f
        limitLinePaint.textSize = l.textSize
        val labelLineHeight = Utils.calcTextHeight(limitLinePaint, label).toFloat()
        val xOffset = Utils.convertDpToPixel(4f) + l.xOffset
        val yOffset = l.lineWidth + labelLineHeight + l.yOffset
        val position = l.labelPosition
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
