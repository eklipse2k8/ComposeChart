package com.github.eklipse2k8.charting.charts

import android.content.Context
import android.graphics.Canvas
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import com.github.eklipse2k8.charting.data.PieData
import com.github.eklipse2k8.charting.data.PieEntry
import com.github.eklipse2k8.charting.highlight.Highlight
import com.github.eklipse2k8.charting.highlight.IHighlighter
import com.github.eklipse2k8.charting.highlight.PieHighlighter
import com.github.eklipse2k8.charting.interfaces.datasets.IPieDataSet
import com.github.eklipse2k8.charting.renderer.DataRenderer
import com.github.eklipse2k8.charting.renderer.PieChartRenderer
import com.github.eklipse2k8.charting.utils.MPPointF
import com.github.eklipse2k8.charting.utils.Utils.convertDpToPixel
import com.github.eklipse2k8.charting.utils.Utils.getNormalizedAngle
import com.github.eklipse2k8.charting.utils.toRadians
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * View that represents a pie chart. Draws cake like slices.
 *
 * @author Philipp Jahoda
 */
class PieChart
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    PieRadarChartBase<PieData, IPieDataSet, PieEntry>(context, attrs, defStyleAttr) {

  override val dataRenderer: DataRenderer = PieChartRenderer(this, animator, viewPortHandler)

  /** rect object that represents the bounds of the piechart, needed for drawing the circle */
  val circleBox: RectF = RectF()

  /** flag indicating if entry labels should be drawn or not */
  var isDrawEntryLabelsEnabled = true
    private set

  /**
   * returns an integer array of all the different angles the chart slices have the angles in the
   * returned array determine how much space (of 360°) each slice takes
   */
  var drawAngles = FloatArray(1)
    private set

  /** returns the absolute angles of the different chart slices (where the slices end) */
  var absoluteAngles = FloatArray(1)
    private set

  /** if true, the white hole inside the chart will be drawn */
  var isDrawHoleEnabled = true

  /** if true, the hole will see-through to the inner tips of the slices */
  var isDrawSlicesUnderHoleEnabled = false
    private set

  /** if true, the values inside the piechart are drawn as percent values */
  var isUsePercentValuesEnabled = false
    private set

  /** if true, the slices of the piechart are rounded */
  var isDrawRoundedSlicesEnabled = false
    private set

  /** variable for the text that is drawn in the center of the pie-chart */
  private var mCenterText: CharSequence = ""

  private val mCenterTextOffset = MPPointF.getInstance(0f, 0f)

  /** indicates the size of the hole in the center of the piechart, default: radius / 2 */
  var holeRadius = 50f

  /**
   * sets the radius of the transparent circle that is drawn next to the hole in the piechart in
   * percent of the maximum radius (max = the radius of the whole chart), default 55% -> means 5%
   * larger than the center-hole by default
   */
  var transparentCircleRadius = 55f

  /** returns true if drawing the center text is enabled */
  var isDrawCenterTextEnabled = true
    private set

  /**
   * the rectangular radius of the bounding box for the center text, as a percentage of the pie hole
   * default 1.f (100%)
   */
  var centerTextRadiusPercent = 100f

  private var mMaxAngle = 360f

  /**
   * Minimum angle to draw slices, this only works if there is enough room for all slices to have
   * the minimum angle, default 0f.
   */
  private var mMinAngleForSlices = 0f

  override val highlighter: IHighlighter = PieHighlighter(this)

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    if (data == null) return
    dataRenderer.drawData(canvas)
    if (valuesToHighlight()) dataRenderer.drawHighlighted(canvas, mIndicesToHighlight)
    dataRenderer.drawExtras(canvas)
    dataRenderer.drawValues(canvas)
    legendRenderer.renderLegend(canvas)
    drawDescription(canvas)
    drawMarkers(canvas)
  }

  override fun calculateOffsets() {
    super.calculateOffsets()

    // prevent nullpointer when no data set
    if (data == null) return
    val diameter = diameter
    val radius = diameter / 2f
    val c: MPPointF = centerOffsets
    val shift = data?.dataSet?.selectionShift ?: 0f

    // create the circle box that will contain the pie-chart (the bounds of
    // the pie-chart)
    circleBox[c.x - radius + shift, c.y - radius + shift, c.x + radius - shift] =
        c.y + radius - shift
    MPPointF.recycleInstance(c)
  }

  override fun calcMinMax() {
    calcAngles()
  }

  override fun getMarkerPosition(high: Highlight): FloatArray {
    val center = centerCircleBox
    var r = radius
    var off = r / 10f * 3.6f
    if (isDrawHoleEnabled) {
      off = (r - r / 100f * holeRadius) / 2f
    }
    r -= off // offset to keep things inside the chart
    val rotationAngle = rotationAngle
    val entryIndex = high.x.toInt()

    // offset needed to center the drawn text in the slice
    val offset = drawAngles[entryIndex] / 2

    // calculate the text position
    val x =
        r *
            cos(
                ((rotationAngle + absoluteAngles[entryIndex] - offset) * animator.phaseY)
                    .toRadians()) + center.x
    val y =
        r *
            sin(
                ((rotationAngle + absoluteAngles[entryIndex] - offset) * animator.phaseY)
                    .toRadians()) + center.y
    MPPointF.recycleInstance(center)
    return floatArrayOf(x, y)
  }

  /** calculates the needed angles for the chart slices */
  private fun calcAngles() {
    val localData = data ?: return

    val entryCount = localData.entryCount
    if (drawAngles.size != entryCount) {
      drawAngles = FloatArray(entryCount)
    } else {
      for (i in 0 until entryCount) {
        drawAngles[i] = 0f
      }
    }
    if (absoluteAngles.size != entryCount) {
      absoluteAngles = FloatArray(entryCount)
    } else {
      for (i in 0 until entryCount) {
        absoluteAngles[i] = 0f
      }
    }
    val yValueSum = localData.yValueSum
    val dataSets = localData.dataSets
    val hasMinAngle = mMinAngleForSlices != 0f && entryCount * mMinAngleForSlices <= mMaxAngle
    val minAngles = FloatArray(entryCount)
    var cnt = 0
    var offset = 0f
    var diff = 0f
    for (i in 0 until localData.dataSetCount) {
      val set = dataSets[i]
      for (j in 0 until set.entryCount) {
        val drawAngle = calcAngle(abs(set.getEntryForIndex(j).y), yValueSum)
        if (hasMinAngle) {
          val temp = drawAngle - mMinAngleForSlices
          if (temp <= 0) {
            minAngles[cnt] = mMinAngleForSlices
            offset += -temp
          } else {
            minAngles[cnt] = drawAngle
            diff += temp
          }
        }
        drawAngles[cnt] = drawAngle
        if (cnt == 0) {
          absoluteAngles[cnt] = drawAngles[cnt]
        } else {
          absoluteAngles[cnt] = absoluteAngles[cnt - 1] + drawAngles[cnt]
        }
        cnt++
      }
    }
    if (hasMinAngle) {
      // Correct bigger slices by relatively reducing their angles based on the total angle needed
      // to subtract
      // This requires that `entryCount * mMinAngleForSlices <= mMaxAngle` be true to properly work!
      for (i in 0 until entryCount) {
        minAngles[i] -= (minAngles[i] - mMinAngleForSlices) / diff * offset
        if (i == 0) {
          absoluteAngles[0] = minAngles[0]
        } else {
          absoluteAngles[i] = absoluteAngles[i - 1] + minAngles[i]
        }
      }
      drawAngles = minAngles
    }
  }

  /**
   * Checks if the given index is set to be highlighted.
   *
   * @param index
   * @return
   */
  fun needsHighlight(index: Int): Boolean {
    // no highlight
    if (!valuesToHighlight()) return false
    // check if the xvalue for the given dataset needs highlight
    val first = mIndicesToHighlight?.firstOrNull { it?.x?.toInt() == index }
    return first != null
  }

  /**
   * calculates the needed angle for a given value
   *
   * @param value
   * @param yValueSum
   * @return
   */
  private fun calcAngle(value: Float, yValueSum: Float = data?.yValueSum ?: 0f): Float {
    return value / yValueSum * mMaxAngle
  }

  override fun getIndexForAngle(angle: Float): Int {
    // take the current angle of the chart into consideration
    val a = getNormalizedAngle(angle - rotationAngle)
    for (i in absoluteAngles.indices) {
      if (absoluteAngles[i] > a) return i
    }
    return -1 // return -1 if no index found
  }

  /**
   * Returns the index of the DataSet this x-index belongs to.
   *
   * @param xIndex
   * @return
   */
  fun getDataSetIndexForIndex(xIndex: Int): Int =
      data?.dataSets?.indexOfFirst { it.getEntryForXValue(xIndex.toFloat(), Float.NaN) != null }
          ?: -1

  /**
   * Sets the color for the hole that is drawn in the center of the PieChart (if enabled).
   *
   * @param color
   */
  fun setHoleColor(color: Int) {
    (dataRenderer as PieChartRenderer).paintHole.color = color
  }

  /** Enable or disable the visibility of the inner tips of the slices behind the hole */
  fun setDrawSlicesUnderHole(enable: Boolean) {
    isDrawSlicesUnderHoleEnabled = enable
  }

  /** returns the text that is drawn in the center of the pie-chart */
  var centerText: CharSequence?
    get() = mCenterText
    set(text) {
      mCenterText = text ?: ""
    }

  /** set this to true to draw the text that is displayed in the center of the pie chart */
  fun setDrawCenterText(enabled: Boolean) {
    isDrawCenterTextEnabled = enabled
  }

  override val requiredLegendOffset: Float
    get() = legendRenderer.labelPaint.textSize * 2f

  override val requiredBaseOffset: Float
    get() = 0f

  override val radius: Float
    get() = min(circleBox.width() / 2f, circleBox.height() / 2f)

  /** returns the center of the circlebox */
  val centerCircleBox: MPPointF
    get() = MPPointF.getInstance(circleBox.centerX(), circleBox.centerY())

  /** sets the typeface for the center-text paint */
  fun setCenterTextTypeface(t: Typeface?) {
    (dataRenderer as PieChartRenderer).paintCenterText.typeface = t
  }

  /**
   * Sets the size of the center text of the PieChart in dp.
   *
   * @param sizeDp
   */
  fun setCenterTextSize(sizeDp: Float) {
    (dataRenderer as PieChartRenderer).paintCenterText.textSize = convertDpToPixel(sizeDp)
  }

  /**
   * Sets the size of the center text of the PieChart in pixels.
   *
   * @param sizePixels
   */
  fun setCenterTextSizePixels(sizePixels: Float) {
    (dataRenderer as PieChartRenderer).paintCenterText.textSize = sizePixels
  }

  /**
   * Sets the offset the center text should have from it's original position in dp. Default x = 0, y
   * = 0
   *
   * @param x
   * @param y
   */
  fun setCenterTextOffset(x: Float, y: Float) {
    mCenterTextOffset.x = convertDpToPixel(x)
    mCenterTextOffset.y = convertDpToPixel(y)
  }

  /**
   * Returns the offset on the x- and y-axis the center text has in dp.
   *
   * @return
   */
  val centerTextOffset: MPPointF
    get() = MPPointF.getInstance(mCenterTextOffset.x, mCenterTextOffset.y)

  /**
   * Sets the color of the center text of the PieChart.
   *
   * @param color
   */
  fun setCenterTextColor(color: Int) {
    (dataRenderer as PieChartRenderer).paintCenterText.color = color
  }

  /**
   * Sets the color the transparent-circle should have.
   *
   * @param color
   */
  fun setTransparentCircleColor(color: Int) {
    val p = (dataRenderer as PieChartRenderer).paintTransparentCircle
    val alpha = p.alpha
    p.color = color
    p.alpha = alpha
  }

  /**
   * Sets the amount of transparency the transparent circle should have 0 = fully transparent, 255 =
   * fully opaque. Default value is 100.
   *
   * @param alpha 0-255
   */
  fun setTransparentCircleAlpha(alpha: Int) {
    (dataRenderer as PieChartRenderer).paintTransparentCircle.alpha = alpha
  }

  /**
   * Set this to true to draw the entry labels into the pie slices (Provided by the getLabel()
   * method of the PieEntry class). Deprecated -> use setDrawEntryLabels(...) instead.
   *
   * @param enabled
   */
  @Deprecated("")
  fun setDrawSliceText(enabled: Boolean) {
    isDrawEntryLabelsEnabled = enabled
  }

  /**
   * Set this to true to draw the entry labels into the pie slices (Provided by the getLabel()
   * method of the PieEntry class).
   *
   * @param enabled
   */
  fun setDrawEntryLabels(enabled: Boolean) {
    isDrawEntryLabelsEnabled = enabled
  }

  /**
   * Sets the color the entry labels are drawn with.
   *
   * @param color
   */
  fun setEntryLabelColor(color: Int) {
    (dataRenderer as PieChartRenderer).paintEntryLabels.color = color
  }

  /**
   * Sets a custom Typeface for the drawing of the entry labels.
   *
   * @param tf
   */
  fun setEntryLabelTypeface(tf: Typeface?) {
    (dataRenderer as PieChartRenderer).paintEntryLabels.typeface = tf
  }

  /**
   * Sets the size of the entry labels in dp. Default: 13dp
   *
   * @param size
   */
  fun setEntryLabelTextSize(size: Float) {
    (dataRenderer as PieChartRenderer).paintEntryLabels.textSize = convertDpToPixel(size)
  }

  /**
   * Sets whether to draw slices in a curved fashion, only works if drawing the hole is enabled and
   * if the slices are not drawn under the hole.
   *
   * @param enabled draw curved ends of slices
   */
  fun setDrawRoundedSlices(enabled: Boolean) {
    isDrawRoundedSlicesEnabled = enabled
  }

  /**
   * If this is enabled, values inside the PieChart are drawn in percent and not with their original
   * value. Values provided for the IValueFormatter to format are then provided in percent.
   *
   * @param enabled
   */
  fun setUsePercentValues(enabled: Boolean) {
    isUsePercentValuesEnabled = enabled
  }

  /**
   * Sets the max angle that is used for calculating the pie-circle. 360f means it's a full
   * PieChart, 180f results in a half-pie-chart. Default: 360f
   */
  var maxAngle: Float
    get() = mMaxAngle
    set(value) {
      mMaxAngle = value.coerceIn(90f, 360f)
    }

  /**
   * Set the angle to set minimum size for slices, you must call [.notifyDataSetChanged] and
   * [.invalidate] when changing this, only works if there is enough room for all slices to have the
   * minimum angle.
   */
  var minAngleForSlices: Float
    get() = mMinAngleForSlices
    set(value) {
      var minAngle = value
      if (minAngle > mMaxAngle / 2f) minAngle = mMaxAngle / 2f else if (minAngle < 0) minAngle = 0f
      mMinAngleForSlices = minAngle
    }

  override fun onDetachedFromWindow() {
    // releases the bitmap in the renderer to avoid oom error
    if (dataRenderer is PieChartRenderer) {
      dataRenderer.releaseBitmap()
    }
    super.onDetachedFromWindow()
  }
}
