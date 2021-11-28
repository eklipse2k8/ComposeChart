package com.github.eklipse2k8.charting.renderer

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.github.eklipse2k8.charting.animation.ChartAnimator
import com.github.eklipse2k8.charting.buffer.BarBuffer
import com.github.eklipse2k8.charting.highlight.Highlight
import com.github.eklipse2k8.charting.interfaces.dataprovider.BarDataProvider
import com.github.eklipse2k8.charting.interfaces.datasets.IBarDataSet
import com.github.eklipse2k8.charting.utils.*
import kotlin.math.ceil
import kotlin.math.min

open class BarChartRenderer(
  @JvmField var chart: BarDataProvider,
  animator: ChartAnimator,
  viewPortHandler: ViewPortHandler
) : BarLineScatterCandleBubbleRenderer(animator, viewPortHandler) {

  /** the rect object that is used for drawing the bars */
  @JvmField protected var barRect = RectF()

  protected lateinit var barBuffers: Array<BarBuffer?>

  @JvmField protected var mShadowPaint: Paint

  @JvmField protected var barBorderPaint: Paint

  override fun initBuffers() {
    val barData = chart.barData
    barBuffers = barData?.dataSetCount?.let { arrayOfNulls(it) } ?: return
    for (i in barBuffers.indices) {
      val set = barData.getDataSetByIndex(i) ?: continue
      barBuffers[i] =
          BarBuffer(set.entryCount * 4 * if (set.isStacked) set.stackSize else 1, set.isStacked)
    }
  }

  override fun drawData(c: Canvas) {
    val barData = chart.barData ?: return
    for (i in 0 until barData.dataSetCount) {
      val set = barData.getDataSetByIndex(i) ?: continue
      if (set.isVisible) {
        drawDataSet(c, set, i)
      }
    }
  }

  private val mBarShadowRectBuffer = RectF()

  protected open fun drawDataSet(c: Canvas, dataSet: IBarDataSet, index: Int) {
    val trans = chart.getTransformer(dataSet.axisDependency)
    barBorderPaint.color = dataSet.barBorderColor
    barBorderPaint.strokeWidth = Utils.convertDpToPixel(dataSet.barBorderWidth)
    val drawBorder = dataSet.barBorderWidth > 0f
    val phaseX = animator.phaseX
    val phaseY = animator.phaseY
    val barData = chart.barData ?: return

    // draw the bar shadow before the values
    if (chart.isDrawBarShadowEnabled) {
      mShadowPaint.color = dataSet.barShadowColor
      val barWidth = barData.barWidth
      val barWidthHalf = barWidth / 2.0f
      var x: Float
      var i = 0
      val count = min(ceil(dataSet.entryCount.toFloat() * phaseX).toInt(), dataSet.entryCount)
      while (i < count) {
        val e = dataSet.getEntryForIndex(i)
        x = e.x
        mBarShadowRectBuffer.left = x - barWidthHalf
        mBarShadowRectBuffer.right = x + barWidthHalf
        trans.rectValueToPixel(mBarShadowRectBuffer)
        if (!viewPortHandler.isInBoundsLeft(mBarShadowRectBuffer.right)) {
          i++
          continue
        }
        if (!viewPortHandler.isInBoundsRight(mBarShadowRectBuffer.left)) break
        mBarShadowRectBuffer.top = viewPortHandler.contentTop()
        mBarShadowRectBuffer.bottom = viewPortHandler.contentBottom()
        c.drawRect(mBarShadowRectBuffer, mShadowPaint)
        i++
      }
    }

    // initialize the buffer
    val buffer = barBuffers[index] ?: return
    buffer.setPhases(phaseX, phaseY)
    buffer.setDataSet(index)
    buffer.setInverted(chart.isInverted(dataSet.axisDependency))
    buffer.setBarWidth(barData.barWidth)
    buffer.feed(dataSet)
    trans.pointValuesToPixel(buffer.buffer)
    val isCustomFill = dataSet.fills?.isNotEmpty() == true
    val isSingleColor = dataSet.colors.size == 1
    val isInverted = chart.isInverted(dataSet.axisDependency)
    if (isSingleColor) {
      renderPaint.color = dataSet.color
    }
    var j = 0
    var pos = 0
    while (j < buffer.size()) {
      if (!viewPortHandler.isInBoundsLeft(buffer.buffer[j + 2])) {
        j += 4
        pos++
        continue
      }
      if (!viewPortHandler.isInBoundsRight(buffer.buffer[j])) break
      if (!isSingleColor) {
        // Set the color for the currently drawn value. If the index
        // is out of bounds, reuse colors.
        renderPaint.color = dataSet.getColor(pos)
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
                if (isInverted) Fill.Direction.DOWN else Fill.Direction.UP)
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

  protected open fun prepareBarHighlight(
      x: Float,
      y1: Float,
      y2: Float,
      barWidthHalf: Float,
      trans: Transformer
  ) {
    val left = x - barWidthHalf
    val right = x + barWidthHalf
    barRect[left, y1, right] = y2
    trans.rectToPixelPhase(barRect, animator.phaseY)
  }

  override fun drawValues(c: Canvas) {
    val dataSets = chart.barData?.dataSets ?: return

    // if values are drawn
    if (isDrawingValuesAllowed(chart)) {
      val valueOffsetPlus = Utils.convertDpToPixel(4.5f)
      var posOffset: Float
      var negOffset: Float
      val drawValueAboveBar = chart.isDrawValueAboveBarEnabled
      for (i in 0 until chart.barData!!.dataSetCount) {
        val dataSet = dataSets[i]
        if (!shouldDrawValues(dataSet)) continue

        // apply the text-styling defined by the DataSet
        applyValueTextStyle(dataSet)
        val isInverted = chart.isInverted(dataSet.axisDependency)

        // calculate the correct offset depending on the draw position of
        // the value
        val valueTextHeight = Utils.calcTextHeight(valuePaint, "8").toFloat()
        posOffset = if (drawValueAboveBar) -valueOffsetPlus else valueTextHeight + valueOffsetPlus
        negOffset = if (drawValueAboveBar) valueTextHeight + valueOffsetPlus else -valueOffsetPlus
        if (isInverted) {
          posOffset = -posOffset - valueTextHeight
          negOffset = -negOffset - valueTextHeight
        }

        // get the buffer
        val buffer = barBuffers[i]
        val phaseY = animator.phaseY
        val iconsOffset =
            dataSet.iconsOffset?.let { MPPointF.getInstance(it) } ?: MPPointF.getInstance(0f, 0f)
        iconsOffset.x = Utils.convertDpToPixel(iconsOffset.x)
        iconsOffset.y = Utils.convertDpToPixel(iconsOffset.y)

        // if only single values are drawn (sum)
        if (!dataSet.isStacked) {
          var j = 0
          while (j < buffer!!.buffer.size * animator.phaseX) {
            val x = (buffer.buffer[j] + buffer.buffer[j + 2]) / 2f
            if (!viewPortHandler.isInBoundsRight(x)) break
            if (!viewPortHandler.isInBoundsY(buffer.buffer[j + 1]) ||
                !viewPortHandler.isInBoundsLeft(x)) {
              j += 4
              continue
            }
            val entry = dataSet.getEntryForIndex(j / 4)
            val `val` = entry.y
            if (dataSet.isDrawValuesEnabled) {
              drawValue(
                  c,
                  dataSet.valueFormatter!!,
                  `val`,
                  entry,
                  i,
                  x,
                  if (`val` >= 0) buffer.buffer[j + 1] + posOffset
                  else buffer.buffer[j + 3] + negOffset,
                  dataSet.getValueTextColor(j / 4))
            }
            if (entry.icon != null && dataSet.isDrawIconsEnabled) {
              val icon = entry.icon
              var px = x
              var py =
                  if (`val` >= 0) buffer.buffer[j + 1] + posOffset
                  else buffer.buffer[j + 3] + negOffset
              px += iconsOffset.x
              py += iconsOffset.y
              Utils.drawImage(
                  c, icon, px.toInt(), py.toInt(), icon.intrinsicWidth, icon.intrinsicHeight)
            }
            j += 4
          }

          // if we have stacks
        } else {
          val trans = chart.getTransformer(dataSet.axisDependency)
          var bufferIndex = 0
          var index = 0
          while (index < dataSet.entryCount * animator.phaseX) {
            val entry = dataSet.getEntryForIndex(index)
            val vals = entry.yVals
            val x = (buffer!!.buffer[bufferIndex] + buffer.buffer[bufferIndex + 2]) / 2f
            val color = dataSet.getValueTextColor(index)

            // we still draw stacked bars, but there is one
            // non-stacked
            // in between
            if (vals == null) {
              if (!viewPortHandler.isInBoundsRight(x)) break
              if (!viewPortHandler.isInBoundsY(buffer.buffer[bufferIndex + 1]) ||
                  !viewPortHandler.isInBoundsLeft(x))
                  continue
              if (dataSet.isDrawValuesEnabled) {
                drawValue(
                    c,
                    dataSet.valueFormatter!!,
                    entry.y,
                    entry,
                    i,
                    x,
                    buffer.buffer[bufferIndex + 1] + if (entry.y >= 0) posOffset else negOffset,
                    color)
              }
              if (entry.icon != null && dataSet.isDrawIconsEnabled) {
                val icon = entry.icon
                var px = x
                var py = buffer.buffer[bufferIndex + 1] + if (entry.y >= 0) posOffset else negOffset
                px += iconsOffset.x
                py += iconsOffset.y
                Utils.drawImage(
                    c, icon, px.toInt(), py.toInt(), icon.intrinsicWidth, icon.intrinsicHeight)
              }

              // draw stack values
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
                  transformed[k + 1] = y * phaseY
                  k += 2
                  idx++
                }
              }
              trans.pointValuesToPixel(transformed)
              var k = 0
              while (k < transformed.size) {
                val `val` = vals[k / 2]
                val drawBelow = `val` == 0.0f && negY == 0.0f && posY > 0.0f || `val` < 0.0f
                val y = (transformed[k + 1] + if (drawBelow) negOffset else posOffset)
                if (!viewPortHandler.isInBoundsRight(x)) break
                if (!viewPortHandler.isInBoundsY(y) || !viewPortHandler.isInBoundsLeft(x)) {
                  k += 2
                  continue
                }
                if (dataSet.isDrawValuesEnabled) {
                  drawValue(c, dataSet.valueFormatter!!, vals[k / 2], entry, i, x, y, color)
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

  override fun drawHighlighted(c: Canvas, indices: Array<Highlight?>?) {
    val barData = chart.barData
    indices?.forEach { high ->
      val set = high?.let { barData?.getDataSetByIndex(it.dataSetIndex) }
      if (set == null || !set.isHighlightEnabled) return@forEach
      val e = set.getEntryForXValue(high.x, high.y) ?: return@forEach
      if (!isInBoundsX(e, set)) return@forEach
      val trans = chart.getTransformer(set.axisDependency)
      highlightPaint.color = set.highLightColor
      highlightPaint.alpha = set.highLightAlpha
      val isStack = high.stackIndex >= 0 && e.isStacked
      val y1: Float
      val y2: Float
      if (isStack) {
        if (chart.isHighlightFullBarEnabled) {
          y1 = e.positiveSum
          y2 = -e.negativeSum
        } else {
          val range = e.ranges[high.stackIndex]!!
          y1 = range.from
          y2 = range.to
        }
      } else {
        y1 = e.y
        y2 = 0f
      }
      prepareBarHighlight(e.x, y1, y2, (barData?.barWidth ?: 0f) / 2f, trans)
      setHighlightDrawPos(high, barRect)
      c.drawRect(barRect, highlightPaint)
    }
  }

  /**
   * Sets the drawing position of the highlight object based on the riven bar-rect.
   * @param high
   */
  protected open fun setHighlightDrawPos(high: Highlight, bar: RectF) {
    high.setDraw(bar.centerX(), bar.top)
  }

  override fun drawExtras(c: Canvas) {}

  init {
    highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    highlightPaint.style = Paint.Style.FILL
    highlightPaint.color = Color.rgb(0, 0, 0)
    // set alpha after color
    highlightPaint.alpha = 120
    mShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    mShadowPaint.style = Paint.Style.FILL
    barBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    barBorderPaint.style = Paint.Style.STROKE
  }
}
