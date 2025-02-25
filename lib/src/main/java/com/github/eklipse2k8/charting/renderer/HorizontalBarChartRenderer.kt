package com.github.eklipse2k8.charting.renderer

import android.graphics.Canvas
import android.graphics.Paint.Align
import android.graphics.RectF
import com.github.eklipse2k8.charting.animation.ChartAnimator
import com.github.eklipse2k8.charting.buffer.HorizontalBarBuffer
import com.github.eklipse2k8.charting.highlight.Highlight
import com.github.eklipse2k8.charting.interfaces.dataprovider.BarDataProvider
import com.github.eklipse2k8.charting.interfaces.dataprovider.ChartInterface
import com.github.eklipse2k8.charting.interfaces.datasets.IBarDataSet
import com.github.eklipse2k8.charting.utils.*
import kotlin.math.ceil
import kotlin.math.min

/**
 * Renderer for the HorizontalBarChart.
 *
 * @author Philipp Jahoda
 */
class HorizontalBarChartRenderer(
    chart: BarDataProvider,
    animator: ChartAnimator,
    viewPortHandler: ViewPortHandler
) : BarChartRenderer(chart, animator, viewPortHandler) {

  override fun initBuffers() {
    val barData = chart.barData ?: return
    barBuffers = arrayOfNulls(barData.dataSetCount)
    barBuffers.indices.forEach { i ->
      val set = barData.getDataSetByIndex(i) ?: return@forEach
      barBuffers[i] =
          HorizontalBarBuffer(
              set.entryCount * 4 * if (set.isStacked) set.stackSize else 1, set.isStacked)
    }
  }

  private val barShadowRectBuffer = RectF()

  override fun drawDataSet(c: Canvas, dataSet: IBarDataSet, index: Int) {
    val trans = chart.getTransformer(dataSet.axisDependency)
    barBorderPaint.color = dataSet.barBorderColor
    barBorderPaint.strokeWidth = Utils.convertDpToPixel(dataSet.barBorderWidth)
    val drawBorder = dataSet.barBorderWidth > 0f
    val phaseX = animator.phaseX
    val phaseY = animator.phaseY
    val barData = chart.barData
    val barWidth = barData?.barWidth ?: 0f

    // draw the bar shadow before the values
    if (chart.isDrawBarShadowEnabled) {
      mShadowPaint.color = dataSet.barShadowColor
      val barWidthHalf = barWidth / 2.0f
      var x: Float
      var i = 0
      val count = min(ceil(dataSet.entryCount * phaseX).toInt(), dataSet.entryCount)
      while (i < count) {
        val e = dataSet.getEntryForIndex(i)
        x = e.x
        barShadowRectBuffer.top = x - barWidthHalf
        barShadowRectBuffer.bottom = x + barWidthHalf
        trans.rectValueToPixel(barShadowRectBuffer)
        if (!viewPortHandler.isInBoundsTop(barShadowRectBuffer.bottom)) {
          i++
          continue
        }
        if (!viewPortHandler.isInBoundsBottom(barShadowRectBuffer.top)) break
        barShadowRectBuffer.left = viewPortHandler.contentLeft()
        barShadowRectBuffer.right = viewPortHandler.contentRight()
        c.drawRect(barShadowRectBuffer, mShadowPaint)
        i++
      }
    }

    // initialize the buffer
    val buffer = barBuffers[index] ?: return
    buffer.setPhases(phaseX, phaseY)
    buffer.setDataSet(index)
    buffer.setInverted(chart.isInverted(dataSet.axisDependency))
    buffer.setBarWidth(barWidth)
    buffer.feed(dataSet)
    trans.pointValuesToPixel(buffer.buffer)
    val isCustomFill = dataSet.fills != null && dataSet.fills!!.isNotEmpty()
    val isSingleColor = dataSet.colors.size == 1
    val isInverted = chart.isInverted(dataSet.axisDependency)
    if (isSingleColor) {
      renderPaint.color = dataSet.color
    }
    var j = 0
    var pos = 0
    while (j < buffer.size()) {
      if (!viewPortHandler.isInBoundsTop(buffer.buffer[j + 3])) break
      if (!viewPortHandler.isInBoundsBottom(buffer.buffer[j + 1])) {
        j += 4
        pos++
        continue
      }
      if (!isSingleColor) {
        // Set the color for the currently drawn value. If the index
        // is out of bounds, reuse colors.
        renderPaint.color = dataSet.getColor(j / 4)
      }
      if (isCustomFill) {
        dataSet
            .getFill(pos)
            ?.fillRect(
                c,
                renderPaint,
                buffer.buffer[j],
                buffer.buffer[j + 1],
                buffer.buffer[j + 2],
                buffer.buffer[j + 3],
                if (isInverted) Fill.Direction.LEFT else Fill.Direction.RIGHT)
      } else {
        c.drawRect(
            buffer.buffer[j],
            buffer.buffer[j + 1],
            buffer.buffer[j + 2],
            buffer.buffer[j + 3],
            renderPaint)
      }
      if (drawBorder) {
        c.drawRect(
            buffer.buffer[j],
            buffer.buffer[j + 1],
            buffer.buffer[j + 2],
            buffer.buffer[j + 3],
            barBorderPaint)
      }
      j += 4
      pos++
    }
  }

  override fun drawValues(c: Canvas) {
    // if values are drawn
    val dataSets = chart.barData?.dataSets ?: emptyList()
    val dataSetCount = chart.barData?.dataSetCount ?: 0
    if (isDrawingValuesAllowed(chart)) {
      val valueOffsetPlus = Utils.convertDpToPixel(5f)
      var posOffset: Float
      var negOffset: Float
      val drawValueAboveBar = chart.isDrawValueAboveBarEnabled
      for (i in 0 until dataSetCount) {
        val dataSet = dataSets[i]
        if (!shouldDrawValues(dataSet)) continue
        val isInverted = chart.isInverted(dataSet.axisDependency)

        // apply the text-styling defined by the DataSet
        applyValueTextStyle(dataSet)
        val halfTextHeight = Utils.calcTextHeight(valuePaint, "10") / 2f
        val formatter = dataSet.valueFormatter

        // get the buffer
        val buffer = barBuffers[i]!!
        val phaseY = animator.phaseY
        val iconsOffset =
            dataSet.iconsOffset?.let { MPPointF.getInstance(it) } ?: MPPointF.getInstance(0f, 0f)
        iconsOffset.x = Utils.convertDpToPixel(iconsOffset.x)
        iconsOffset.y = Utils.convertDpToPixel(iconsOffset.y)

        // if only single values are drawn (sum)
        if (!dataSet.isStacked) {
          var j = 0
          while (j < buffer.buffer.size * animator.phaseX) {
            val y = (buffer.buffer[j + 1] + buffer.buffer[j + 3]) / 2f
            if (!viewPortHandler.isInBoundsTop(buffer.buffer[j + 1])) break
            if (!viewPortHandler.isInBoundsX(buffer.buffer[j])) {
              j += 4
              continue
            }
            if (!viewPortHandler.isInBoundsBottom(buffer.buffer[j + 1])) {
              j += 4
              continue
            }
            val entry = dataSet.getEntryForIndex(j / 4)
            val `val` = entry.y
            val formattedValue = formatter!!.getFormattedValue(`val`, entry, i, viewPortHandler)

            // calculate the correct offset depending on the draw position of the value
            val valueTextWidth = Utils.calcTextWidth(valuePaint, formattedValue).toFloat()
            posOffset =
                if (drawValueAboveBar) valueOffsetPlus else -(valueTextWidth + valueOffsetPlus)
            negOffset =
                ((if (drawValueAboveBar) -(valueTextWidth + valueOffsetPlus) else valueOffsetPlus) -
                    (buffer.buffer[j + 2] - buffer.buffer[j]))
            if (isInverted) {
              posOffset = -posOffset - valueTextWidth
              negOffset = -negOffset - valueTextWidth
            }
            if (dataSet.isDrawValuesEnabled) {
              drawValue(
                  c,
                  formattedValue,
                  buffer.buffer[j + 2] + if (`val` >= 0) posOffset else negOffset,
                  y + halfTextHeight,
                  dataSet.getValueTextColor(j / 2))
            }
            if (entry.icon != null && dataSet.isDrawIconsEnabled) {
              val icon = entry.icon
              var px = buffer.buffer[j + 2] + if (`val` >= 0) posOffset else negOffset
              var py = y
              px += iconsOffset.x
              py += iconsOffset.y
              Utils.drawImage(
                  c, icon, px.toInt(), py.toInt(), icon.intrinsicWidth, icon.intrinsicHeight)
            }
            j += 4
          }

          // if each value of a potential stack should be drawn
        } else {
          val trans = chart.getTransformer(dataSet.axisDependency)
          var bufferIndex = 0
          var index = 0
          while (index < dataSet.entryCount * animator.phaseX) {
            val entry = dataSet.getEntryForIndex(index)
            val color = dataSet.getValueTextColor(index)
            val vals = entry.yVals

            // we still draw stacked bars, but there is one
            // non-stacked
            // in between
            if (vals == null) {
              if (!viewPortHandler.isInBoundsTop(buffer.buffer[bufferIndex + 1])) break
              if (!viewPortHandler.isInBoundsX(buffer.buffer[bufferIndex])) continue
              if (!viewPortHandler.isInBoundsBottom(buffer.buffer[bufferIndex + 1])) continue
              val `val` = entry.y
              val formattedValue = formatter!!.getFormattedValue(`val`, entry, i, viewPortHandler)

              // calculate the correct offset depending on the draw position of the value
              val valueTextWidth = Utils.calcTextWidth(valuePaint, formattedValue).toFloat()
              posOffset =
                  if (drawValueAboveBar) valueOffsetPlus else -(valueTextWidth + valueOffsetPlus)
              negOffset =
                  if (drawValueAboveBar) -(valueTextWidth + valueOffsetPlus) else valueOffsetPlus
              if (isInverted) {
                posOffset = -posOffset - valueTextWidth
                negOffset = -negOffset - valueTextWidth
              }
              if (dataSet.isDrawValuesEnabled) {
                drawValue(
                    c,
                    formattedValue,
                    buffer.buffer[bufferIndex + 2] + if (entry.y >= 0) posOffset else negOffset,
                    buffer.buffer[bufferIndex + 1] + halfTextHeight,
                    color)
              }
              if (entry.icon != null && dataSet.isDrawIconsEnabled) {
                val icon = entry.icon
                var px =
                    (buffer.buffer[bufferIndex + 2] + if (entry.y >= 0) posOffset else negOffset)
                var py = buffer.buffer[bufferIndex + 1]
                px += iconsOffset.x
                py += iconsOffset.y
                Utils.drawImage(
                    c, icon, px.toInt(), py.toInt(), icon.intrinsicWidth, icon.intrinsicHeight)
              }
            } else {
              val transformed = FloatArray(vals.size * 2)
              var posY = 0f
              var negY = -entry.negativeSum
              run {
                var k = 0
                var idx = 0
                while (k < transformed.size) {
                  val value = vals[idx]
                  var y: Float
                  if (value == 0.0f && (posY == 0.0f || negY == 0.0f)) {
                    // Take care of the situation of a 0.0 value, which overlaps a non-zero bar
                    y = value
                  } else if (value >= 0.0f) {
                    posY += value
                    y = posY
                  } else {
                    y = negY
                    negY -= value
                  }
                  transformed[k] = y * phaseY
                  k += 2
                  idx++
                }
              }
              trans.pointValuesToPixel(transformed)
              var k = 0
              while (k < transformed.size) {
                val `val` = vals[k / 2]
                val formattedValue = formatter!!.getFormattedValue(`val`, entry, i, viewPortHandler)

                // calculate the correct offset depending on the draw position of the value
                val valueTextWidth = Utils.calcTextWidth(valuePaint, formattedValue).toFloat()
                posOffset =
                    if (drawValueAboveBar) valueOffsetPlus else -(valueTextWidth + valueOffsetPlus)
                negOffset =
                    if (drawValueAboveBar) -(valueTextWidth + valueOffsetPlus) else valueOffsetPlus
                if (isInverted) {
                  posOffset = -posOffset - valueTextWidth
                  negOffset = -negOffset - valueTextWidth
                }
                val drawBelow = `val` == 0.0f && negY == 0.0f && posY > 0.0f || `val` < 0.0f
                val x = (transformed[k] + if (drawBelow) negOffset else posOffset)
                val y = (buffer.buffer[bufferIndex + 1] + buffer.buffer[bufferIndex + 3]) / 2f
                if (!viewPortHandler.isInBoundsTop(y)) break
                if (!viewPortHandler.isInBoundsX(x)) {
                  k += 2
                  continue
                }
                if (!viewPortHandler.isInBoundsBottom(y)) {
                  k += 2
                  continue
                }
                if (dataSet.isDrawValuesEnabled) {
                  drawValue(c, formattedValue, x, y + halfTextHeight, color)
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
                k += 2
              }
            }
            bufferIndex = if (vals == null) bufferIndex + 4 else bufferIndex + 4 * vals.size
            index++
          }
        }
        MPPointF.recycleInstance(iconsOffset)
      }
    }
  }

  private fun drawValue(c: Canvas, valueText: String?, x: Float, y: Float, color: Int) {
    valuePaint.color = color
    valueText?.let { c.drawText(it, x, y, valuePaint) }
  }

  override fun prepareBarHighlight(
      x: Float,
      y1: Float,
      y2: Float,
      barWidthHalf: Float,
      trans: Transformer
  ) {
    val top = x - barWidthHalf
    val bottom = x + barWidthHalf
    barRect[y1, top, y2] = bottom
    trans.rectToPixelPhaseHorizontal(barRect, animator.phaseY)
  }

  override fun setHighlightDrawPos(high: Highlight, bar: RectF) {
    high.setDraw(bar.centerY(), bar.right)
  }

  override fun isDrawingValuesAllowed(chart: ChartInterface): Boolean {
    val data = chart.data ?: return false
    return data.entryCount < chart.maxVisibleCount * viewPortHandler.scaleY
  }

  init {
    valuePaint.textAlign = Align.LEFT
  }
}
