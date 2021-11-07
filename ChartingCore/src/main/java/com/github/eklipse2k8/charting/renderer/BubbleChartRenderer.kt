package com.github.eklipse2k8.charting.renderer

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.github.eklipse2k8.charting.animation.ChartAnimator
import com.github.eklipse2k8.charting.data.Entry
import com.github.eklipse2k8.charting.highlight.Highlight
import com.github.eklipse2k8.charting.interfaces.dataprovider.BubbleDataProvider
import com.github.eklipse2k8.charting.interfaces.datasets.IBarLineScatterCandleBubbleDataSet
import com.github.eklipse2k8.charting.interfaces.datasets.IBubbleDataSet
import com.github.eklipse2k8.charting.utils.MPPointF
import com.github.eklipse2k8.charting.utils.Utils
import com.github.eklipse2k8.charting.utils.ViewPortHandler
import kotlin.math.*

/**
 * Bubble chart implementation: Copyright 2015 Pierre-Marc Airoldi Licensed under Apache License 2.0
 * Ported by Daniel Cohen Gindi
 */
class BubbleChartRenderer(
    @JvmField var mChart: BubbleDataProvider,
    animator: ChartAnimator?,
    viewPortHandler: ViewPortHandler?
) : BarLineScatterCandleBubbleRenderer(animator!!, viewPortHandler!!) {

  override fun initBuffers() {}

  override fun drawData(c: Canvas) {
    mChart.bubbleData?.dataSets?.forEach { set -> if (set.isVisible) drawDataSet(c, set) }
  }

  private val sizeBuffer = FloatArray(4)

  private val pointBuffer = FloatArray(2)

  protected fun getShapeSize(
      entrySize: Float,
      maxSize: Float,
      reference: Float,
      normalizeSize: Boolean
  ): Float {
    val factor =
        if (normalizeSize) if (maxSize == 0f) 1f else sqrt(entrySize / maxSize) else entrySize
    return reference * factor
  }

  protected fun drawDataSet(c: Canvas, dataSet: IBubbleDataSet) {
    if (dataSet.entryCount < 1) return
    val trans = mChart.getTransformer(dataSet.axisDependency)
    val phaseY = mAnimator.phaseY
    mXBounds[mChart] = dataSet as IBarLineScatterCandleBubbleDataSet<Entry>
    sizeBuffer[0] = 0f
    sizeBuffer[2] = 1f
    trans.pointValuesToPixel(sizeBuffer)
    val normalizeSize = dataSet.isNormalizeSizeEnabled

    // calcualte the full width of 1 step on the x-axis
    val maxBubbleWidth = Math.abs(sizeBuffer[2] - sizeBuffer[0])
    val maxBubbleHeight = Math.abs(mViewPortHandler.contentBottom() - mViewPortHandler.contentTop())
    val referenceSize = Math.min(maxBubbleHeight, maxBubbleWidth)
    for (j in mXBounds.min..mXBounds.range + mXBounds.min) {
      val entry = dataSet.getEntryForIndex(j)
      pointBuffer[0] = entry.x
      pointBuffer[1] = entry.y * phaseY
      trans.pointValuesToPixel(pointBuffer)
      val shapeHalf = getShapeSize(entry.size, dataSet.maxSize, referenceSize, normalizeSize) / 2f
      if (!mViewPortHandler.isInBoundsTop(pointBuffer[1] + shapeHalf) ||
          !mViewPortHandler.isInBoundsBottom(pointBuffer[1] - shapeHalf))
          continue
      if (!mViewPortHandler.isInBoundsLeft(pointBuffer[0] + shapeHalf)) continue
      if (!mViewPortHandler.isInBoundsRight(pointBuffer[0] - shapeHalf)) break
      val color = dataSet.getColor(j)
      mRenderPaint.color = color
      c.drawCircle(pointBuffer[0], pointBuffer[1], shapeHalf, mRenderPaint)
    }
  }

  override fun drawValues(c: Canvas) {
    val bubbleData = mChart.bubbleData ?: return

    // if values are drawn
    if (isDrawingValuesAllowed(mChart)) {
      val dataSets = bubbleData.dataSets
      val lineHeight = Utils.calcTextHeight(mValuePaint, "1").toFloat()
      for (i in dataSets.indices) {
        val dataSet = dataSets[i]
        if (!shouldDrawValues(dataSet) || dataSet.entryCount < 1) continue

        // apply the text-styling defined by the DataSet
        applyValueTextStyle(dataSet)
        val phaseX = max(0f, min(1f, mAnimator.phaseX))
        val phaseY = mAnimator.phaseY
        mXBounds[mChart] = dataSet as IBarLineScatterCandleBubbleDataSet<Entry>
        val positions =
            mChart
                .getTransformer(dataSet.axisDependency)
                .generateTransformedValuesBubble(dataSet, phaseY, mXBounds.min, mXBounds.max)
        val alpha = if (phaseX == 1f) phaseY else phaseX
        val iconsOffset = MPPointF.getInstance(dataSet.iconsOffset)
        iconsOffset.x = Utils.convertDpToPixel(iconsOffset.x)
        iconsOffset.y = Utils.convertDpToPixel(iconsOffset.y)
        var j = 0
        while (j < positions.size) {
          var valueTextColor = dataSet.getValueTextColor(j / 2 + mXBounds.min)
          valueTextColor =
              Color.argb(
                  (255f * alpha).roundToInt(),
                  Color.red(valueTextColor),
                  Color.green(valueTextColor),
                  Color.blue(valueTextColor))
          val x = positions[j]
          val y = positions[j + 1]
          if (!mViewPortHandler.isInBoundsRight(x)) break
          if (!mViewPortHandler.isInBoundsLeft(x) || !mViewPortHandler.isInBoundsY(y)) {
            j += 2
            continue
          }
          val entry = dataSet.getEntryForIndex(j / 2 + mXBounds.min)
          if (dataSet.isDrawValuesEnabled) {
            drawValue(
                c,
                dataSet.valueFormatter!!,
                entry.size,
                entry,
                i,
                x,
                y + 0.5f * lineHeight,
                valueTextColor)
          }
          if (entry.icon != null && dataSet.isDrawIconsEnabled) {
            val icon = entry.icon
            Utils.drawImage(
                c,
                icon,
                (x + iconsOffset.x).toInt(),
                (y + iconsOffset.y).toInt(),
                icon.intrinsicWidth,
                icon.intrinsicHeight)
          }
          j += 2
        }
        MPPointF.recycleInstance(iconsOffset)
      }
    }
  }

  private val _hsvBuffer = FloatArray(3)

  override fun drawExtras(c: Canvas) = Unit

  override fun drawHighlighted(c: Canvas, indices: Array<Highlight?>?) {
    val bubbleData = mChart.bubbleData ?: return
    val phaseY = mAnimator.phaseY
    indices?.forEach { high ->
      if (high == null) return@forEach
      val set = bubbleData.getDataSetByIndex(high.dataSetIndex)
      if (set == null || !set.isHighlightEnabled) return@forEach
      val entry = set.getEntryForXValue(high.x, high.y) ?: return@forEach
      if (entry.y != high.y) return@forEach
      if (!isInBoundsX(entry, set)) return@forEach
      val trans = mChart.getTransformer(set.axisDependency)
      sizeBuffer[0] = 0f
      sizeBuffer[2] = 1f
      trans.pointValuesToPixel(sizeBuffer)
      val normalizeSize = set.isNormalizeSizeEnabled

      // calcualte the full width of 1 step on the x-axis
      val maxBubbleWidth = abs(sizeBuffer[2] - sizeBuffer[0])
      val maxBubbleHeight = abs(mViewPortHandler.contentBottom() - mViewPortHandler.contentTop())
      val referenceSize = min(maxBubbleHeight, maxBubbleWidth)
      pointBuffer[0] = entry.x
      pointBuffer[1] = entry.y * phaseY
      trans.pointValuesToPixel(pointBuffer)
      high.setDraw(pointBuffer[0], pointBuffer[1])
      val shapeHalf = getShapeSize(entry.size, set.maxSize, referenceSize, normalizeSize) / 2f
      if (!mViewPortHandler.isInBoundsTop(pointBuffer[1] + shapeHalf) ||
          !mViewPortHandler.isInBoundsBottom(pointBuffer[1] - shapeHalf))
          return@forEach
      if (!mViewPortHandler.isInBoundsLeft(pointBuffer[0] + shapeHalf)) return@forEach
      if (!mViewPortHandler.isInBoundsRight(pointBuffer[0] - shapeHalf)) return@forEach
      val originalColor = set.getColor(entry.x.toInt())
      Color.RGBToHSV(
          Color.red(originalColor),
          Color.green(originalColor),
          Color.blue(originalColor),
          _hsvBuffer)
      _hsvBuffer[2] *= 0.5f
      val color = Color.HSVToColor(Color.alpha(originalColor), _hsvBuffer)
      mHighlightPaint.color = color
      mHighlightPaint.strokeWidth = set.highlightCircleWidth
      c.drawCircle(pointBuffer[0], pointBuffer[1], shapeHalf, mHighlightPaint)
    }
  }

  init {
    mRenderPaint.style = Paint.Style.FILL
    mHighlightPaint.style = Paint.Style.STROKE
    mHighlightPaint.strokeWidth = Utils.convertDpToPixel(1.5f)
  }
}
