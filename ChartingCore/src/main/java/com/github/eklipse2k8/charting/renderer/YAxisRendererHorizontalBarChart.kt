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
  /**
   * Computes the axis values.
   *
   * @param yMin
   * - the minimum y-value in the data object for this axis
   * @param yMax
   * - the maximum y-value in the data object for this axis
   */
  override fun computeAxis(yMin: Float, yMax: Float, inverted: Boolean) {

    // calculate the starting and entry point of the y-labels (depending on
    // zoom / contentrect bounds)
    var yMin = yMin
    var yMax = yMax
    if (viewPortHandler.contentHeight() > 10 && !viewPortHandler.isFullyZoomedOutX) {
      val p1 =
          mTrans!!.getValuesByTouchPoint(
              viewPortHandler.contentLeft(), viewPortHandler.contentTop())
      val p2 =
          mTrans!!.getValuesByTouchPoint(
              viewPortHandler.contentRight(), viewPortHandler.contentTop())
      if (!inverted) {
        yMin = p1.x.toFloat()
        yMax = p2.x.toFloat()
      } else {
        yMin = p2.x.toFloat()
        yMax = p1.x.toFloat()
      }
      MPPointD.recycleInstance(p1)
      MPPointD.recycleInstance(p2)
    }
    computeAxisValues(yMin, yMax)
  }

  /** draws the y-axis labels to the screen */
  override fun renderAxisLabels(c: Canvas?) {
    if (!mYAxis.isEnabled || !mYAxis.isDrawLabelsEnabled) return
    val positions = transformedPositions
    mAxisLabelPaint.typeface = mYAxis.typeface
    mAxisLabelPaint.textSize = mYAxis.textSize
    mAxisLabelPaint.color = mYAxis.textColor
    mAxisLabelPaint.textAlign = Align.CENTER
    val baseYOffset = Utils.convertDpToPixel(2.5f)
    val textHeight = Utils.calcTextHeight(mAxisLabelPaint, "Q").toFloat()
    val dependency = mYAxis.axisDependency
    val labelPosition = mYAxis.labelPosition
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
    drawYLabels(c, yPos, positions, mYAxis.yOffset)
  }

  override fun renderAxisLine(c: Canvas?) {
    if (!mYAxis.isEnabled || !mYAxis.isDrawAxisLineEnabled) return
    mAxisLinePaint!!.color = mYAxis.axisLineColor
    mAxisLinePaint!!.strokeWidth = mYAxis.axisLineWidth
    if (mYAxis.axisDependency === AxisDependency.LEFT) {
      c!!.drawLine(
          viewPortHandler.contentLeft(),
          viewPortHandler.contentTop(),
          viewPortHandler.contentRight(),
          viewPortHandler.contentTop(),
          mAxisLinePaint!!)
    } else {
      c!!.drawLine(
          viewPortHandler.contentLeft(),
          viewPortHandler.contentBottom(),
          viewPortHandler.contentRight(),
          viewPortHandler.contentBottom(),
          mAxisLinePaint!!)
    }
  }

  /**
   * draws the y-labels on the specified x-position
   *
   * @param fixedPosition
   * @param positions
   */
  override fun drawYLabels(c: Canvas?, fixedPosition: Float, positions: FloatArray, offset: Float) {
    mAxisLabelPaint.typeface = mYAxis.typeface
    mAxisLabelPaint.textSize = mYAxis.textSize
    mAxisLabelPaint.color = mYAxis.textColor
    val from = if (mYAxis.isDrawBottomYLabelEntryEnabled) 0 else 1
    val to = if (mYAxis.isDrawTopYLabelEntryEnabled) mYAxis.mEntryCount else mYAxis.mEntryCount - 1
    val xOffset = mYAxis.labelXOffset
    for (i in from until to) {
      val text = mYAxis.getFormattedLabel(i)
      c!!.drawText(text!!, positions[i * 2], fixedPosition - offset + xOffset, mAxisLabelPaint)
    }
  }

  // only fill x values, y values are not needed for x-labels
  override val transformedPositions: FloatArray
    protected get() {
      if (mGetTransformedPositionsBuffer.size != mYAxis.mEntryCount * 2) {
        mGetTransformedPositionsBuffer = FloatArray(mYAxis.mEntryCount * 2)
      }
      val positions = mGetTransformedPositionsBuffer
      var i = 0
      while (i < positions.size) {

        // only fill x values, y values are not needed for x-labels
        positions[i] = mYAxis.mEntries[i / 2]
        i += 2
      }
      mTrans!!.pointValuesToPixel(positions)
      return positions
    }
  override val gridClippingRect: RectF
    get() {
      mGridClippingRect.set(viewPortHandler.contentRect)
      mGridClippingRect.inset(-mAxis.gridLineWidth, 0f)
      return mGridClippingRect
    }

  override fun linePath(p: Path, i: Int, positions: FloatArray): Path? {
    p.moveTo(positions[i], viewPortHandler.contentTop())
    p.lineTo(positions[i], viewPortHandler.contentBottom())
    return p
  }

  protected var mDrawZeroLinePathBuffer = Path()
  override fun drawZeroLine(c: Canvas?) {
    val clipRestoreCount = c!!.save()
    mZeroLineClippingRect.set(viewPortHandler.contentRect)
    mZeroLineClippingRect.inset(-mYAxis.zeroLineWidth, 0f)
    c.clipRect(mLimitLineClippingRect)

    // draw zero line
    val pos = mTrans!!.getPixelForValues(0f, 0f)
    mZeroLinePaint!!.color = mYAxis.zeroLineColor
    mZeroLinePaint!!.strokeWidth = mYAxis.zeroLineWidth
    val zeroLinePath = mDrawZeroLinePathBuffer
    zeroLinePath.reset()
    zeroLinePath.moveTo(pos.x.toFloat() - 1, viewPortHandler.contentTop())
    zeroLinePath.lineTo(pos.x.toFloat() - 1, viewPortHandler.contentBottom())

    // draw a path because lines don't support dashing on lower android versions
    c.drawPath(zeroLinePath, mZeroLinePaint!!)
    c.restoreToCount(clipRestoreCount)
  }

  @JvmField protected var mRenderLimitLinesPathBuffer = Path()

  override var mRenderLimitLinesBuffer = FloatArray(4)

  /**
   * Draws the LimitLines associated with this axis to the screen. This is the standard XAxis
   * renderer using the YAxis limit lines.
   *
   * @param c
   */
  override fun renderLimitLines(c: Canvas?) {
    val limitLines = mYAxis.limitLines
    if (limitLines == null || limitLines.size <= 0) return
    val pts = mRenderLimitLinesBuffer
    pts[0] = 0f
    pts[1] = 0f
    pts[2] = 0f
    pts[3] = 0f
    val limitLinePath = mRenderLimitLinesPathBuffer
    limitLinePath.reset()
    for (i in limitLines.indices) {
      val l = limitLines[i]
      if (!l.isEnabled) continue
      val clipRestoreCount = c!!.save()
      mLimitLineClippingRect.set(viewPortHandler.contentRect)
      mLimitLineClippingRect.inset(-l.lineWidth, 0f)
      c.clipRect(mLimitLineClippingRect)
      pts[0] = l.limit
      pts[2] = l.limit
      mTrans!!.pointValuesToPixel(pts)
      pts[1] = viewPortHandler.contentTop()
      pts[3] = viewPortHandler.contentBottom()
      limitLinePath.moveTo(pts[0], pts[1])
      limitLinePath.lineTo(pts[2], pts[3])
      mLimitLinePaint!!.style = Paint.Style.STROKE
      mLimitLinePaint!!.color = l.lineColor
      mLimitLinePaint!!.pathEffect = l.dashPathEffect
      mLimitLinePaint!!.strokeWidth = l.lineWidth
      c.drawPath(limitLinePath, mLimitLinePaint!!)
      limitLinePath.reset()
      val label = l.label

      // if drawing the limit-value label is enabled
      if (label != null && label != "") {
        mLimitLinePaint!!.style = l.textStyle
        mLimitLinePaint!!.pathEffect = null
        mLimitLinePaint!!.color = l.textColor
        mLimitLinePaint!!.typeface = l.typeface
        mLimitLinePaint!!.strokeWidth = 0.5f
        mLimitLinePaint!!.textSize = l.textSize
        val xOffset = l.lineWidth + l.xOffset
        val yOffset = Utils.convertDpToPixel(2f) + l.yOffset
        val position = l.labelPosition
        if (position === LimitLabelPosition.RIGHT_TOP) {
          val labelLineHeight = Utils.calcTextHeight(mLimitLinePaint, label).toFloat()
          mLimitLinePaint!!.textAlign = Align.LEFT
          c.drawText(
              label,
              pts[0] + xOffset,
              viewPortHandler.contentTop() + yOffset + labelLineHeight,
              mLimitLinePaint!!)
        } else if (position === LimitLabelPosition.RIGHT_BOTTOM) {
          mLimitLinePaint!!.textAlign = Align.LEFT
          c.drawText(
              label,
              pts[0] + xOffset,
              viewPortHandler.contentBottom() - yOffset,
              mLimitLinePaint!!)
        } else if (position === LimitLabelPosition.LEFT_TOP) {
          mLimitLinePaint!!.textAlign = Align.RIGHT
          val labelLineHeight = Utils.calcTextHeight(mLimitLinePaint, label).toFloat()
          c.drawText(
              label,
              pts[0] - xOffset,
              viewPortHandler.contentTop() + yOffset + labelLineHeight,
              mLimitLinePaint!!)
        } else {
          mLimitLinePaint!!.textAlign = Align.RIGHT
          c.drawText(
              label,
              pts[0] - xOffset,
              viewPortHandler.contentBottom() - yOffset,
              mLimitLinePaint!!)
        }
      }
      c.restoreToCount(clipRestoreCount)
    }
  }

  init {
    mLimitLinePaint!!.textAlign = Align.LEFT
  }
}
