package com.github.mikephil.charting.renderer

import android.graphics.*
import com.github.mikephil.charting.components.LimitLine.LimitLabelPosition
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.components.YAxis.AxisDependency
import com.github.mikephil.charting.components.YAxis.YAxisLabelPosition
import com.github.mikephil.charting.utils.Transformer
import com.github.mikephil.charting.utils.Utils.calcTextHeight
import com.github.mikephil.charting.utils.Utils.convertDpToPixel
import com.github.mikephil.charting.utils.ViewPortHandler

open class YAxisRenderer(
    viewPortHandler: ViewPortHandler,
    protected var mYAxis: YAxis,
    trans: Transformer?
) : AxisRenderer(viewPortHandler, trans, mYAxis) {

  @JvmField protected var mZeroLinePaint: Paint? = null

  /** draws the y-axis labels to the screen */
  override fun renderAxisLabels(c: Canvas?) {
    if (!mYAxis.isEnabled || !mYAxis.isDrawLabelsEnabled) return
    val positions = transformedPositions
    paintAxisLabels!!.typeface = mYAxis.typeface
    paintAxisLabels!!.textSize = mYAxis.textSize
    paintAxisLabels!!.color = mYAxis.textColor
    val xoffset = mYAxis.xOffset
    val yoffset = calcTextHeight(paintAxisLabels!!, "A") / 2.5f + mYAxis.yOffset
    val dependency = mYAxis.axisDependency
    val labelPosition = mYAxis.labelPosition
    var xPos = 0f
    if (dependency === AxisDependency.LEFT) {
      if (labelPosition === YAxisLabelPosition.OUTSIDE_CHART) {
        paintAxisLabels!!.textAlign = Paint.Align.RIGHT
        xPos = mViewPortHandler.offsetLeft() - xoffset
      } else {
        paintAxisLabels!!.textAlign = Paint.Align.LEFT
        xPos = mViewPortHandler.offsetLeft() + xoffset
      }
    } else {
      if (labelPosition === YAxisLabelPosition.OUTSIDE_CHART) {
        paintAxisLabels!!.textAlign = Paint.Align.LEFT
        xPos = mViewPortHandler.contentRight() + xoffset
      } else {
        paintAxisLabels!!.textAlign = Paint.Align.RIGHT
        xPos = mViewPortHandler.contentRight() - xoffset
      }
    }
    drawYLabels(c, xPos, positions, yoffset)
  }

  override fun renderAxisLine(c: Canvas?) {
    if (!mYAxis.isEnabled || !mYAxis.isDrawAxisLineEnabled) return
    paintAxisLine!!.color = mYAxis.axisLineColor
    paintAxisLine!!.strokeWidth = mYAxis.axisLineWidth
    if (mYAxis.axisDependency === AxisDependency.LEFT) {
      c!!.drawLine(
          mViewPortHandler.contentLeft(),
          mViewPortHandler.contentTop(),
          mViewPortHandler.contentLeft(),
          mViewPortHandler.contentBottom(),
          paintAxisLine!!)
    } else {
      c!!.drawLine(
          mViewPortHandler.contentRight(),
          mViewPortHandler.contentTop(),
          mViewPortHandler.contentRight(),
          mViewPortHandler.contentBottom(),
          paintAxisLine!!)
    }
  }

  /**
   * draws the y-labels on the specified x-position
   *
   * @param fixedPosition
   * @param positions
   */
  protected open fun drawYLabels(
      c: Canvas?,
      fixedPosition: Float,
      positions: FloatArray,
      offset: Float
  ) {
    val from = if (mYAxis.isDrawBottomYLabelEntryEnabled) 0 else 1
    val to = if (mYAxis.isDrawTopYLabelEntryEnabled) mYAxis.mEntryCount else mYAxis.mEntryCount - 1
    val xOffset = mYAxis.labelXOffset

    // draw
    for (i in from until to) {
      val text = mYAxis.getFormattedLabel(i)
      c!!.drawText(
          text!!, fixedPosition + xOffset, positions[i * 2 + 1] + offset, paintAxisLabels!!)
    }
  }

  protected var mRenderGridLinesPath = Path()

  override fun renderGridLines(c: Canvas?) {
    if (!mYAxis.isEnabled) return
    if (mYAxis.isDrawGridLinesEnabled) {
      val clipRestoreCount = c!!.save()
      c.clipRect(gridClippingRect!!)
      val positions = transformedPositions
      paintGrid!!.color = mYAxis.gridColor
      paintGrid!!.strokeWidth = mYAxis.gridLineWidth
      paintGrid!!.pathEffect = mYAxis.gridDashPathEffect
      val gridLinePath = mRenderGridLinesPath
      gridLinePath.reset()

      // draw the grid
      var i = 0
      while (i < positions.size) {

        // draw a path because lines don't support dashing on lower android versions
        c.drawPath(linePath(gridLinePath, i, positions)!!, paintGrid!!)
        gridLinePath.reset()
        i += 2
      }
      c.restoreToCount(clipRestoreCount)
    }
    if (mYAxis.isDrawZeroLineEnabled) {
      drawZeroLine(c)
    }
  }

  @JvmField protected var mGridClippingRect = RectF()

  open val gridClippingRect: RectF?
    get() {
      mGridClippingRect.set(mViewPortHandler.contentRect)
      mGridClippingRect.inset(0f, -mAxis.gridLineWidth)
      return mGridClippingRect
    }

  /**
   * Calculates the path for a grid line.
   *
   * @param p
   * @param i
   * @param positions
   * @return
   */
  protected open fun linePath(p: Path, i: Int, positions: FloatArray): Path? {
    p.moveTo(mViewPortHandler.offsetLeft(), positions[i + 1])
    p.lineTo(mViewPortHandler.contentRight(), positions[i + 1])
    return p
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
    protected get() {
      if (mGetTransformedPositionsBuffer.size != mYAxis.mEntryCount * 2) {
        mGetTransformedPositionsBuffer = FloatArray(mYAxis.mEntryCount * 2)
      }
      val positions = mGetTransformedPositionsBuffer
      var i = 0
      while (i < positions.size) {

        // only fill y values, x values are not needed for y-labels
        positions[i + 1] = mYAxis.mEntries[i / 2]
        i += 2
      }
      transformer?.pointValuesToPixel(positions)
      return positions
    }
  protected var mDrawZeroLinePath = Path()
  @JvmField protected var mZeroLineClippingRect = RectF()

  /** Draws the zero line. */
  protected open fun drawZeroLine(c: Canvas?) {
    val clipRestoreCount = c!!.save()
    mZeroLineClippingRect.set(mViewPortHandler.contentRect)
    mZeroLineClippingRect.inset(0f, -mYAxis.zeroLineWidth)
    c.clipRect(mZeroLineClippingRect)

    // draw zero line
    val pos = transformer?.getPixelForValues(0f, 0f)
    mZeroLinePaint!!.color = mYAxis.zeroLineColor
    mZeroLinePaint!!.strokeWidth = mYAxis.zeroLineWidth
    val zeroLinePath = mDrawZeroLinePath
    zeroLinePath.reset()
    zeroLinePath.moveTo(mViewPortHandler.contentLeft(), pos?.y?.toFloat() ?: 0f)
    zeroLinePath.lineTo(mViewPortHandler.contentRight(), pos?.y?.toFloat() ?: 0f)

    // draw a path because lines don't support dashing on lower android versions
    c.drawPath(zeroLinePath, mZeroLinePaint!!)
    c.restoreToCount(clipRestoreCount)
  }

  protected var mRenderLimitLines = Path()
  protected open var mRenderLimitLinesBuffer = FloatArray(2)
  @JvmField protected var mLimitLineClippingRect = RectF()

  /**
   * Draws the LimitLines associated with this axis to the screen.
   *
   * @param c
   */
  override fun renderLimitLines(c: Canvas?) {
    val limitLines = mYAxis.limitLines
    if (limitLines.isEmpty()) return
    val pts = mRenderLimitLinesBuffer
    pts[0] = 0f
    pts[1] = 0f
    val limitLinePath = mRenderLimitLines
    limitLinePath.reset()
    for (i in limitLines.indices) {
      val l = limitLines[i]
      if (!l.isEnabled) continue
      val clipRestoreCount = c!!.save()
      mLimitLineClippingRect.set(mViewPortHandler.contentRect)
      mLimitLineClippingRect.inset(0f, -l.lineWidth)
      c.clipRect(mLimitLineClippingRect)
      mLimitLinePaint!!.style = Paint.Style.STROKE
      mLimitLinePaint!!.color = l.lineColor
      mLimitLinePaint!!.strokeWidth = l.lineWidth
      mLimitLinePaint!!.pathEffect = l.dashPathEffect
      pts[1] = l.limit
      transformer?.pointValuesToPixel(pts)
      limitLinePath.moveTo(mViewPortHandler.contentLeft(), pts[1])
      limitLinePath.lineTo(mViewPortHandler.contentRight(), pts[1])
      c.drawPath(limitLinePath, mLimitLinePaint!!)
      limitLinePath.reset()
      // c.drawLines(pts, mLimitLinePaint);
      val label = l.label

      // if drawing the limit-value label is enabled
      if (label != "") {
        mLimitLinePaint!!.style = l.textStyle
        mLimitLinePaint!!.pathEffect = null
        mLimitLinePaint!!.color = l.textColor
        mLimitLinePaint!!.typeface = l.typeface
        mLimitLinePaint!!.strokeWidth = 0.5f
        mLimitLinePaint!!.textSize = l.textSize
        val labelLineHeight = calcTextHeight(mLimitLinePaint!!, label).toFloat()
        val xOffset = convertDpToPixel(4f) + l.xOffset
        val yOffset = l.lineWidth + labelLineHeight + l.yOffset
        val position = l.labelPosition
        if (position === LimitLabelPosition.RIGHT_TOP) {
          mLimitLinePaint!!.textAlign = Paint.Align.RIGHT
          c.drawText(
              label,
              mViewPortHandler.contentRight() - xOffset,
              pts[1] - yOffset + labelLineHeight,
              mLimitLinePaint!!)
        } else if (position === LimitLabelPosition.RIGHT_BOTTOM) {
          mLimitLinePaint!!.textAlign = Paint.Align.RIGHT
          c.drawText(
              label, mViewPortHandler.contentRight() - xOffset, pts[1] + yOffset, mLimitLinePaint!!)
        } else if (position === LimitLabelPosition.LEFT_TOP) {
          mLimitLinePaint!!.textAlign = Paint.Align.LEFT
          c.drawText(
              label,
              mViewPortHandler.contentLeft() + xOffset,
              pts[1] - yOffset + labelLineHeight,
              mLimitLinePaint!!)
        } else {
          mLimitLinePaint!!.textAlign = Paint.Align.LEFT
          c.drawText(
              label, mViewPortHandler.offsetLeft() + xOffset, pts[1] + yOffset, mLimitLinePaint!!)
        }
      }
      c.restoreToCount(clipRestoreCount)
    }
  }

  init {
    paintAxisLabels!!.color = Color.BLACK
    paintAxisLabels!!.textSize = convertDpToPixel(10f)
    mZeroLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    mZeroLinePaint!!.color = Color.GRAY
    mZeroLinePaint!!.strokeWidth = 1f
    mZeroLinePaint!!.style = Paint.Style.STROKE
  }
}
