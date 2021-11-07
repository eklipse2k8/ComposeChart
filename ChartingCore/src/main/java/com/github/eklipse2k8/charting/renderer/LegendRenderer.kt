package com.github.eklipse2k8.charting.renderer

import android.graphics.*
import android.graphics.Paint.Align
import com.github.eklipse2k8.charting.components.Legend
import com.github.eklipse2k8.charting.components.Legend.LegendDirection
import com.github.eklipse2k8.charting.components.Legend.LegendForm
import com.github.eklipse2k8.charting.components.Legend.LegendHorizontalAlignment
import com.github.eklipse2k8.charting.components.Legend.LegendOrientation
import com.github.eklipse2k8.charting.components.Legend.LegendVerticalAlignment
import com.github.eklipse2k8.charting.components.LegendEntry
import com.github.eklipse2k8.charting.data.ChartData
import com.github.eklipse2k8.charting.interfaces.datasets.IBarDataSet
import com.github.eklipse2k8.charting.interfaces.datasets.ICandleDataSet
import com.github.eklipse2k8.charting.interfaces.datasets.IPieDataSet
import com.github.eklipse2k8.charting.utils.ColorTemplate
import com.github.eklipse2k8.charting.utils.Utils
import com.github.eklipse2k8.charting.utils.ViewPortHandler
import java.util.*
import kotlin.math.min

class LegendRenderer(
    viewPortHandler: ViewPortHandler,
    /** the legend object this renderer renders */
    var mLegend: Legend
) : Renderer(viewPortHandler) {
  /** paint for the legend labels */
  var labelPaint: Paint
    private set

  /** paint used for the legend forms */
  var formPaint: Paint
    private set

  private var computedEntries: MutableList<LegendEntry> = mutableListOf()

  /**
   * Prepares the legend and calculates all needed forms, labels and colors.
   *
   * @param data
   */
  fun computeLegend(data: ChartData<*, *>) {
    if (!mLegend.isLegendCustom) {
      computedEntries.clear()

      // loop for building up the colors and labels used in the legend
      for (i in 0 until data.dataSetCount) {
        val dataSet = data.getDataSetByIndex(i) ?: continue
        val clrs = dataSet.colors
        val entryCount = dataSet.entryCount

        // if we have a barchart with stacked bars
        if (dataSet is IBarDataSet && dataSet.isStacked) {
          val sLabels = dataSet.stackLabels
          val minEntries = min(clrs.size, dataSet.stackSize)
          for (j in 0 until minEntries) {
            val label =
                if (!sLabels.isNullOrEmpty()) {
                  val labelIndex = j % minEntries
                  if (labelIndex < sLabels.size) sLabels[labelIndex] else null
                } else {
                  null
                }
            computedEntries.add(
                LegendEntry(
                    label,
                    dataSet.form!!,
                    dataSet.formSize,
                    dataSet.formLineWidth,
                    dataSet.formLineDashEffect,
                  clrs[j]
                ))
          }
          if (dataSet.label != null) {
            // add the legend description label
            computedEntries.add(
                LegendEntry(
                    dataSet.label,
                    LegendForm.NONE,
                    Float.NaN,
                    Float.NaN,
                    null,
                    ColorTemplate.COLOR_NONE))
          }
        } else if (dataSet is IPieDataSet) {
          var j = 0
          while (j < clrs.size && j < entryCount) {
            computedEntries.add(
                LegendEntry(
                    dataSet.getEntryForIndex(j).label,
                    dataSet.form!!,
                    dataSet.formSize,
                    dataSet.formLineWidth,
                    dataSet.formLineDashEffect,
                  clrs[j]
                ))
            j++
          }
          if (dataSet.label != null) {
            // add the legend description label
            computedEntries.add(
                LegendEntry(
                    dataSet.label,
                    LegendForm.NONE,
                    Float.NaN,
                    Float.NaN,
                    null,
                    ColorTemplate.COLOR_NONE))
          }
        } else if (dataSet is ICandleDataSet &&
            dataSet.decreasingColor != ColorTemplate.COLOR_NONE) {
          val decreasingColor = dataSet.decreasingColor
          val increasingColor = dataSet.increasingColor
          computedEntries.add(
              LegendEntry(
                  null,
                  dataSet.form!!,
                  dataSet.formSize,
                  dataSet.formLineWidth,
                  dataSet.formLineDashEffect,
                  decreasingColor))
          computedEntries.add(
              LegendEntry(
                  dataSet.label,
                  dataSet.form!!,
                  dataSet.formSize,
                  dataSet.formLineWidth,
                  dataSet.formLineDashEffect,
                  increasingColor))
        } else { // all others
          var j = 0
          while (j < clrs.size && j < entryCount) {
            // if multiple colors are set for a DataSet, group them
            val label =
                if (j < clrs.size - 1 && j < entryCount - 1) {
                  null
                } else { // add label to the last entry
                  data.getDataSetByIndex(i)!!.label
                }
            computedEntries.add(
                LegendEntry(
                    label,
                    dataSet.form!!,
                    dataSet.formSize,
                    dataSet.formLineWidth,
                    dataSet.formLineDashEffect,
                  clrs[j]
                ))
            j++
          }
        }
      }
      Collections.addAll(computedEntries, *mLegend.extraEntries)
      mLegend.setEntries(computedEntries)
    }
    val tf = mLegend.typeface
    if (tf != null) labelPaint.typeface = tf
    labelPaint.textSize = mLegend.textSize
    labelPaint.color = mLegend.textColor

    // calculate all dimensions of the mLegend
    mLegend.calculateDimensions(labelPaint, mViewPortHandler)
  }

  private var legendFontMetrics = Paint.FontMetrics()

  fun renderLegend(c: Canvas) {
    if (!mLegend.isEnabled) return
    val tf = mLegend.typeface
    if (tf != null) labelPaint.typeface = tf
    labelPaint.textSize = mLegend.textSize
    labelPaint.color = mLegend.textColor
    val labelLineHeight = Utils.getLineHeight(labelPaint, legendFontMetrics)
    val labelLineSpacing =
        (Utils.getLineSpacing(labelPaint, legendFontMetrics) +
            Utils.convertDpToPixel(mLegend.yEntrySpace))
    val formYOffset = labelLineHeight - Utils.calcTextHeight(labelPaint, "ABC") / 2f
    val entries = mLegend.entries
    val formToTextSpace = Utils.convertDpToPixel(mLegend.formToTextSpace)
    val xEntrySpace = Utils.convertDpToPixel(mLegend.xEntrySpace)
    val orientation = mLegend.orientation
    val horizontalAlignment = mLegend.horizontalAlignment
    val verticalAlignment = mLegend.verticalAlignment
    val direction = mLegend.direction
    val defaultFormSize = Utils.convertDpToPixel(mLegend.formSize)

    // space between the entries
    val stackSpace = Utils.convertDpToPixel(mLegend.stackSpace)
    val yoffset = mLegend.yOffset
    val xoffset = mLegend.xOffset
    var originPosX = 0f
    when (horizontalAlignment) {
      LegendHorizontalAlignment.LEFT -> {
        originPosX =
            if (orientation === LegendOrientation.VERTICAL) xoffset
            else mViewPortHandler.contentLeft() + xoffset
        if (direction === LegendDirection.RIGHT_TO_LEFT) originPosX += mLegend.mNeededWidth
      }
      LegendHorizontalAlignment.RIGHT -> {
        originPosX =
            if (orientation === LegendOrientation.VERTICAL) mViewPortHandler.chartWidth - xoffset
            else mViewPortHandler.contentRight() - xoffset
        if (direction === LegendDirection.LEFT_TO_RIGHT) originPosX -= mLegend.mNeededWidth
      }
      LegendHorizontalAlignment.CENTER -> {
        originPosX =
            if (orientation === LegendOrientation.VERTICAL) mViewPortHandler.chartWidth / 2f
            else mViewPortHandler.contentLeft()
        +mViewPortHandler.contentWidth() / 2f
        originPosX += if (direction === LegendDirection.LEFT_TO_RIGHT) +xoffset else -xoffset

        // Horizontally layed out legends do the center offset on a line basis,
        // So here we offset the vertical ones only.
        if (orientation === LegendOrientation.VERTICAL) {
          originPosX +=
              (if (direction === LegendDirection.LEFT_TO_RIGHT)
                      -mLegend.mNeededWidth / 2.0 + xoffset
                  else mLegend.mNeededWidth / 2.0 - xoffset)
                  .toFloat()
        }
      }
    }
    when (orientation) {
      LegendOrientation.HORIZONTAL -> {
        val calculatedLineSizes = mLegend.calculatedLineSizes
        val calculatedLabelSizes = mLegend.calculatedLabelSizes
        val calculatedLabelBreakPoints = mLegend.calculatedLabelBreakPoints
        var posX = originPosX
        var posY =
            when (verticalAlignment) {
              LegendVerticalAlignment.TOP -> yoffset
              LegendVerticalAlignment.BOTTOM ->
                  mViewPortHandler.chartHeight - yoffset - mLegend.mNeededHeight
              LegendVerticalAlignment.CENTER ->
                  (mViewPortHandler.chartHeight - mLegend.mNeededHeight) / 2f + yoffset
            }
        var lineIndex = 0
        var i = 0
        val count = entries.size
        while (i < count) {
          val e = entries[i]
          val drawingForm = e.form !== LegendForm.NONE
          val formSize =
              if (java.lang.Float.isNaN(e.formSize)) defaultFormSize
              else Utils.convertDpToPixel(e.formSize)
          if (i < calculatedLabelBreakPoints.size && calculatedLabelBreakPoints[i]) {
            posX = originPosX
            posY += labelLineHeight + labelLineSpacing
          }
          if (posX == originPosX &&
              horizontalAlignment === LegendHorizontalAlignment.CENTER &&
              lineIndex < calculatedLineSizes.size) {
            posX +=
                (if (direction === LegendDirection.RIGHT_TO_LEFT)
                    calculatedLineSizes[lineIndex].width
                else -calculatedLineSizes[lineIndex].width) / 2f
            lineIndex++
          }
          val isStacked = e.label == null // grouped forms have null labels
          if (drawingForm) {
            if (direction === LegendDirection.RIGHT_TO_LEFT) posX -= formSize
            drawForm(c, posX, posY + formYOffset, e, mLegend)
            if (direction === LegendDirection.LEFT_TO_RIGHT) posX += formSize
          }
          if (!isStacked) {
            if (drawingForm)
                posX +=
                    if (direction === LegendDirection.RIGHT_TO_LEFT) -formToTextSpace
                    else formToTextSpace
            if (direction === LegendDirection.RIGHT_TO_LEFT) posX -= calculatedLabelSizes[i].width
            drawLabel(c, posX, posY + labelLineHeight, e.label)
            if (direction === LegendDirection.LEFT_TO_RIGHT) posX += calculatedLabelSizes[i].width
            posX += if (direction === LegendDirection.RIGHT_TO_LEFT) -xEntrySpace else xEntrySpace
          } else
              posX += if (direction === LegendDirection.RIGHT_TO_LEFT) -stackSpace else stackSpace
          i++
        }
      }
      LegendOrientation.VERTICAL -> {

        // contains the stacked legend size in pixels
        var stack = 0f
        var wasStacked = false
        var posY = 0f
        when (verticalAlignment) {
          LegendVerticalAlignment.TOP -> {
            posY =
                if (horizontalAlignment === LegendHorizontalAlignment.CENTER) 0f
                else mViewPortHandler.contentTop()
            posY += yoffset
          }
          LegendVerticalAlignment.BOTTOM -> {
            posY =
                if (horizontalAlignment === LegendHorizontalAlignment.CENTER)
                    mViewPortHandler.chartHeight
                else mViewPortHandler.contentBottom()
            posY -= mLegend.mNeededHeight + yoffset
          }
          LegendVerticalAlignment.CENTER ->
              posY =
                  (mViewPortHandler.chartHeight / 2f - mLegend.mNeededHeight / 2f + mLegend.yOffset)
        }
        var i = 0
        while (i < entries.size) {
          val e = entries[i]
          val drawingForm = e.form !== LegendForm.NONE
          val formSize =
              if (java.lang.Float.isNaN(e.formSize)) defaultFormSize
              else Utils.convertDpToPixel(e.formSize)
          var posX = originPosX
          if (drawingForm) {
            if (direction === LegendDirection.LEFT_TO_RIGHT) posX += stack
            else posX -= formSize - stack
            drawForm(c, posX, posY + formYOffset, e, mLegend)
            if (direction === LegendDirection.LEFT_TO_RIGHT) posX += formSize
          }
          if (e.label != null) {
            if (drawingForm && !wasStacked)
                posX +=
                    if (direction === LegendDirection.LEFT_TO_RIGHT) formToTextSpace
                    else -formToTextSpace
            else if (wasStacked) posX = originPosX
            if (direction === LegendDirection.RIGHT_TO_LEFT)
                posX -= Utils.calcTextWidth(labelPaint, e.label).toFloat()
            if (!wasStacked) {
              drawLabel(c, posX, posY + labelLineHeight, e.label)
            } else {
              posY += labelLineHeight + labelLineSpacing
              drawLabel(c, posX, posY + labelLineHeight, e.label)
            }

            // make a step down
            posY += labelLineHeight + labelLineSpacing
            stack = 0f
          } else {
            stack += formSize + stackSpace
            wasStacked = true
          }
          i++
        }
      }
    }
  }

  private val mLineFormPath = Path()

  /**
   * Draws the Legend-form at the given position with the color at the given index.
   *
   * @param c canvas to draw with
   * @param x position
   * @param y position
   * @param entry the entry to render
   * @param legend the legend context
   */
  private fun drawForm(c: Canvas, x: Float, y: Float, entry: LegendEntry, legend: Legend) {
    if (entry.formColor == ColorTemplate.COLOR_SKIP ||
        entry.formColor == ColorTemplate.COLOR_NONE ||
        entry.formColor == 0)
        return
    val restoreCount = c.save()
    var form = entry.form
    if (form === LegendForm.DEFAULT) form = legend.form
    formPaint.color = entry.formColor
    val formSize =
        Utils.convertDpToPixel(
            if (java.lang.Float.isNaN(entry.formSize)) legend.formSize else entry.formSize)
    val half = formSize / 2f
    when (form) {
      LegendForm.NONE -> {}
      LegendForm.EMPTY -> {}
      LegendForm.DEFAULT, LegendForm.CIRCLE -> {
        formPaint.style = Paint.Style.FILL
        c.drawCircle(x + half, y, half, formPaint)
      }
      LegendForm.SQUARE -> {
        formPaint.style = Paint.Style.FILL
        c.drawRect(x, y - half, x + formSize, y + half, formPaint)
      }
      LegendForm.LINE -> {
        val formLineWidth =
            Utils.convertDpToPixel(
                if (java.lang.Float.isNaN(entry.formLineWidth)) legend.formLineWidth
                else entry.formLineWidth)
        val formLineDashEffect = entry.formLineDashEffect ?: legend.formLineDashEffect
        formPaint.style = Paint.Style.STROKE
        formPaint.strokeWidth = formLineWidth
        formPaint.pathEffect = formLineDashEffect
        mLineFormPath.reset()
        mLineFormPath.moveTo(x, y)
        mLineFormPath.lineTo(x + formSize, y)
        c.drawPath(mLineFormPath, formPaint)
      }
    }
    c.restoreToCount(restoreCount)
  }

  /**
   * Draws the provided label at the given position.
   *
   * @param c canvas to draw with
   * @param x
   * @param y
   * @param label the label to draw
   */
  private fun drawLabel(c: Canvas, x: Float, y: Float, label: String?) {
    c.drawText(label!!, x, y, labelPaint)
  }

  init {
    labelPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    labelPaint.textSize = Utils.convertDpToPixel(9f)
    labelPaint.textAlign = Align.LEFT
    formPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    formPaint.style = Paint.Style.FILL
  }
}
