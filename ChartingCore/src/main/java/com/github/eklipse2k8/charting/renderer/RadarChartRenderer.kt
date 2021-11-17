package com.github.eklipse2k8.charting.renderer

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import com.github.eklipse2k8.charting.animation.ChartAnimator
import com.github.eklipse2k8.charting.charts.RadarChart
import com.github.eklipse2k8.charting.highlight.Highlight
import com.github.eklipse2k8.charting.interfaces.datasets.IRadarDataSet
import com.github.eklipse2k8.charting.utils.ColorTemplate
import com.github.eklipse2k8.charting.utils.MPPointF
import com.github.eklipse2k8.charting.utils.Utils
import com.github.eklipse2k8.charting.utils.ViewPortHandler

class RadarChartRenderer(
    private var mChart: RadarChart,
    animator: ChartAnimator,
    viewPortHandler: ViewPortHandler
) : LineRadarRenderer(animator, viewPortHandler) {
  /** paint for drawing the web */
  var webPaint: Paint
    private set

  private var mHighlightCirclePaint: Paint

  override fun initBuffers() {
    // TODO Auto-generated method stub
  }

  override fun drawData(c: Canvas) {
    val radarData = mChart.data ?: return
    val mostEntries = radarData.maxEntryCountSet?.entryCount ?: return
    for (set in radarData.dataSets) {
      if (set.isVisible) {
        drawDataSet(c, set, mostEntries)
      }
    }
  }

  private var mDrawDataSetSurfacePathBuffer = Path()

  /**
   * Draws the RadarDataSet
   *
   * @param c
   * @param dataSet
   * @param mostEntries the entry count of the dataset with the most entries
   */
  private fun drawDataSet(c: Canvas, dataSet: IRadarDataSet, mostEntries: Int) {
    val phaseX = mAnimator.phaseX
    val phaseY = mAnimator.phaseY
    val sliceangle = mChart.sliceAngle

    // calculate the factor that is needed for transforming the value to
    // pixels
    val factor = mChart.factor
    val center = mChart.centerOffsets
    val pOut = MPPointF.getInstance(0f, 0f)
    val surface = mDrawDataSetSurfacePathBuffer
    surface.reset()
    var hasMovedToPoint = false
    for (j in 0 until dataSet.entryCount) {
      mRenderPaint.color = dataSet.getColor(j)
      val e = dataSet.getEntryForIndex(j)
      Utils.getPosition(
          center,
          (e.y - mChart.yChartMin) * factor * phaseY,
          sliceangle * j * phaseX + mChart.rotationAngle,
          pOut)
      if (java.lang.Float.isNaN(pOut.x)) continue
      if (!hasMovedToPoint) {
        surface.moveTo(pOut.x, pOut.y)
        hasMovedToPoint = true
      } else surface.lineTo(pOut.x, pOut.y)
    }
    if (dataSet.entryCount > mostEntries) {
      // if this is not the largest set, draw a line to the center before closing
      surface.lineTo(center?.x ?: 0f, center?.y ?: 0f)
    }
    surface.close()
    if (dataSet.isDrawFilledEnabled) {
      val drawable = dataSet.fillDrawable
      if (drawable != null) {
        drawFilledPath(c, surface, drawable)
      } else {
        drawFilledPath(c, surface, dataSet.fillColor, dataSet.fillAlpha)
      }
    }
    mRenderPaint.strokeWidth = dataSet.lineWidth
    mRenderPaint.style = Paint.Style.STROKE

    // draw the line (only if filled is disabled or alpha is below 255)
    if (!dataSet.isDrawFilledEnabled || dataSet.fillAlpha < 255) c.drawPath(surface, mRenderPaint)
    MPPointF.recycleInstance(center)
    MPPointF.recycleInstance(pOut)
  }

  override fun drawValues(c: Canvas) {
    val phaseX = mAnimator.phaseX
    val phaseY = mAnimator.phaseY
    val sliceangle = mChart.sliceAngle

    // calculate the factor that is needed for transforming the value to
    // pixels
    val factor = mChart.factor
    val center = mChart.centerOffsets
    val pOut = MPPointF.getInstance(0f, 0f)
    val pIcon = MPPointF.getInstance(0f, 0f)
    val yoffset = Utils.convertDpToPixel(5f)
    for (i in 0 until (mChart.data?.dataSetCount ?: 0)) {
      val dataSet = mChart.data?.getDataSetByIndex(i) ?: continue
      if (!shouldDrawValues(dataSet)) continue

      // apply the text-styling defined by the DataSet
      applyValueTextStyle(dataSet)
      val iconsOffset =
        dataSet.iconsOffset?.let { MPPointF.getInstance(it) } ?: MPPointF.getInstance(0f, 0f)
      iconsOffset.x = Utils.convertDpToPixel(iconsOffset.x)
      iconsOffset.y = Utils.convertDpToPixel(iconsOffset.y)
      for (j in 0 until dataSet.entryCount) {
        val entry = dataSet.getEntryForIndex(j)
        Utils.getPosition(
            center,
            (entry.y - mChart.yChartMin) * factor * phaseY,
            sliceangle * j * phaseX + mChart.rotationAngle,
            pOut)
        if (dataSet.isDrawValuesEnabled) {
          drawValue(
              c,
              dataSet.valueFormatter!!,
              entry.y,
              entry,
              i,
              pOut.x,
              pOut.y - yoffset,
              dataSet.getValueTextColor(j))
        }
        if (entry.icon != null && dataSet.isDrawIconsEnabled) {
          val icon = entry.icon
          Utils.getPosition(
              center,
              entry.y * factor * phaseY + iconsOffset.y,
              sliceangle * j * phaseX + mChart.rotationAngle,
              pIcon)
          pIcon.y += iconsOffset.x
          Utils.drawImage(
              c,
              icon,
              pIcon.x.toInt(),
              pIcon.y.toInt(),
              icon.intrinsicWidth,
              icon.intrinsicHeight)
        }
      }
      MPPointF.recycleInstance(iconsOffset)
    }
    MPPointF.recycleInstance(center)
    MPPointF.recycleInstance(pOut)
    MPPointF.recycleInstance(pIcon)
  }

  override fun drawExtras(c: Canvas) {
    drawWeb(c)
  }

  protected fun drawWeb(c: Canvas) {
    val sliceangle = mChart.sliceAngle

    // calculate the factor that is needed for transforming the value to
    // pixels
    val factor = mChart.factor
    val rotationangle = mChart.rotationAngle
    val center = mChart.centerOffsets ?: MPPointF.getInstance(0f, 0f)

    // draw the web lines that come from the center
    webPaint.strokeWidth = mChart.webLineWidth
    webPaint.color = mChart.webColor
    webPaint.alpha = mChart.webAlpha
    val xIncrements = 1 + mChart.skipWebLineCount
    val maxEntryCount = mChart.data?.maxEntryCountSet?.entryCount ?: 0
    val p = MPPointF.getInstance(0f, 0f)
    var i = 0
    while (i < maxEntryCount) {
      Utils.getPosition(center, mChart.yRange * factor, sliceangle * i + rotationangle, p)
      c.drawLine(center.x, center.y, p.x, p.y, webPaint)
      i += xIncrements
    }
    MPPointF.recycleInstance(p)

    // draw the inner-web
    webPaint.strokeWidth = mChart.webLineWidthInner
    webPaint.color = mChart.webColorInner
    webPaint.alpha = mChart.webAlpha
    val labelCount = mChart.yAxis?.mEntryCount ?: 0
    val p1out = MPPointF.getInstance(0f, 0f)
    val p2out = MPPointF.getInstance(0f, 0f)
    for (j in 0 until labelCount) {
      for (i in 0 until (mChart.data?.entryCount ?: 0)) {
        val r = ((mChart.yAxis?.mEntries?.get(j) ?: 0f) - mChart.yChartMin) * factor
        Utils.getPosition(center, r, sliceangle * i + rotationangle, p1out)
        Utils.getPosition(center, r, sliceangle * (i + 1) + rotationangle, p2out)
        c.drawLine(p1out.x, p1out.y, p2out.x, p2out.y, webPaint)
      }
    }
    MPPointF.recycleInstance(p1out)
    MPPointF.recycleInstance(p2out)
  }

  override fun drawHighlighted(c: Canvas, indices: Array<Highlight?>?) {
    val sliceangle = mChart.sliceAngle

    // calculate the factor that is needed for transforming the value to
    // pixels
    val factor = mChart.factor
    val center = mChart.centerOffsets ?: MPPointF.getInstance(0f, 0f)
    val pOut = MPPointF.getInstance(0f, 0f)
    val radarData = mChart.data
    indices?.forEach { high ->
      val set = high?.let { radarData?.getDataSetByIndex(it.dataSetIndex) } ?: return@forEach
      if (!set.isHighlightEnabled) return@forEach
      val e = set.getEntryForIndex(high.x.toInt())
      if (!isInBoundsX(e, set)) return@forEach
      val y: Float = e.y - mChart.yChartMin
      Utils.getPosition(
          center,
          y * factor * mAnimator.phaseY,
          sliceangle * high.x * mAnimator.phaseX + mChart.rotationAngle,
          pOut)
      high.setDraw(pOut.x, pOut.y)

      // draw the lines
      drawHighlightLines(c, pOut.x, pOut.y, set)
      if (set.isDrawHighlightCircleEnabled) {
        if (!java.lang.Float.isNaN(pOut.x) && !java.lang.Float.isNaN(pOut.y)) {
          var strokeColor = set.highlightCircleStrokeColor
          if (strokeColor == ColorTemplate.COLOR_NONE) {
            strokeColor = set.getColor(0)
          }
          if (set.highlightCircleStrokeAlpha < 255) {
            strokeColor = ColorTemplate.colorWithAlpha(strokeColor, set.highlightCircleStrokeAlpha)
          }
          drawHighlightCircle(
              c,
              pOut,
              set.highlightCircleInnerRadius,
              set.highlightCircleOuterRadius,
              set.highlightCircleFillColor,
              strokeColor,
              set.highlightCircleStrokeWidth)
        }
      }
    }
    MPPointF.recycleInstance(center)
    MPPointF.recycleInstance(pOut)
  }

  private var mDrawHighlightCirclePathBuffer = Path()

  private fun drawHighlightCircle(
      c: Canvas,
      point: MPPointF,
      innerRadius: Float,
      outerRadius: Float,
      fillColor: Int,
      strokeColor: Int,
      strokeWidth: Float
  ) {
    var innerRadius = innerRadius
    var outerRadius = outerRadius
    c.save()
    outerRadius = Utils.convertDpToPixel(outerRadius)
    innerRadius = Utils.convertDpToPixel(innerRadius)
    if (fillColor != ColorTemplate.COLOR_NONE) {
      val p = mDrawHighlightCirclePathBuffer
      p.reset()
      p.addCircle(point.x, point.y, outerRadius, Path.Direction.CW)
      if (innerRadius > 0f) {
        p.addCircle(point.x, point.y, innerRadius, Path.Direction.CCW)
      }
      mHighlightCirclePaint.color = fillColor
      mHighlightCirclePaint.style = Paint.Style.FILL
      c.drawPath(p, mHighlightCirclePaint)
    }
    if (strokeColor != ColorTemplate.COLOR_NONE) {
      mHighlightCirclePaint.color = strokeColor
      mHighlightCirclePaint.style = Paint.Style.STROKE
      mHighlightCirclePaint.strokeWidth = Utils.convertDpToPixel(strokeWidth)
      c.drawCircle(point.x, point.y, outerRadius, mHighlightCirclePaint)
    }
    c.restore()
  }

  init {
    mHighlightPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    mHighlightPaint.style = Paint.Style.STROKE
    mHighlightPaint.strokeWidth = 2f
    mHighlightPaint.color = Color.rgb(255, 187, 115)
    webPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    webPaint.style = Paint.Style.STROKE
    mHighlightCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG)
  }
}
