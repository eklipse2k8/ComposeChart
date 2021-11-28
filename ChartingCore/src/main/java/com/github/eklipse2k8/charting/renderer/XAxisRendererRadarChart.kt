package com.github.eklipse2k8.charting.renderer

import android.graphics.Canvas
import com.github.eklipse2k8.charting.charts.RadarChart
import com.github.eklipse2k8.charting.components.XAxis
import com.github.eklipse2k8.charting.utils.MPPointF
import com.github.eklipse2k8.charting.utils.Utils
import com.github.eklipse2k8.charting.utils.ViewPortHandler

class XAxisRendererRadarChart(
    viewPortHandler: ViewPortHandler,
    xAxis: XAxis,
    private val mChart: RadarChart
) : XAxisRenderer(viewPortHandler, xAxis, null) {

  override fun renderAxisLabels(canvas: Canvas?) {
    if (!xAxis.isEnabled || !xAxis.isDrawLabelsEnabled) return
    val labelRotationAngleDegrees = xAxis.labelRotationAngle
    val drawLabelAnchor = MPPointF.getInstance(0.5f, 0.25f)
    axisLabelPaint.typeface = xAxis.typeface
    axisLabelPaint.textSize = xAxis.textSize
    axisLabelPaint.color = xAxis.textColor
    val sliceangle = mChart.sliceAngle

    // calculate the factor that is needed for transforming the value to
    // pixels
    val factor = mChart.factor
    val center = mChart.centerOffsets
    val pOut = MPPointF.getInstance(0f, 0f)
    for (i in 0 until mChart.data?.maxEntryCountSet?.entryCount!!) {
      val label = xAxis.valueFormatter!!.getFormattedValue(i.toFloat(), xAxis)
      val angle = (sliceangle * i + mChart.rotationAngle) % 360f
      Utils.getPosition(
          center, mChart.yRange * factor + xAxis.labelRotatedWidth / 2f, angle, pOut)
      drawLabel(
          canvas,
          label,
          pOut.x,
          pOut.y - xAxis.labelRotatedHeight / 2f,
          drawLabelAnchor,
          labelRotationAngleDegrees)
    }
    MPPointF.recycleInstance(center)
    MPPointF.recycleInstance(pOut)
    MPPointF.recycleInstance(drawLabelAnchor)
  }

  /**
   * XAxis LimitLines on RadarChart not yet supported.
   *
   * @param canvas
   */
  override fun renderLimitLines(canvas: Canvas?) {
    // this space intentionally left blank
  }
}
