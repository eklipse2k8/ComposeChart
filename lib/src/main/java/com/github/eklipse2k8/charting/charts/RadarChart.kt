package com.github.eklipse2k8.charting.charts

import android.R.attr
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import com.github.eklipse2k8.charting.components.YAxis
import com.github.eklipse2k8.charting.components.YAxis.AxisDependency
import com.github.eklipse2k8.charting.data.RadarData
import com.github.eklipse2k8.charting.data.RadarEntry
import com.github.eklipse2k8.charting.highlight.IHighlighter
import com.github.eklipse2k8.charting.highlight.RadarHighlighter
import com.github.eklipse2k8.charting.interfaces.datasets.IRadarDataSet
import com.github.eklipse2k8.charting.renderer.DataRenderer
import com.github.eklipse2k8.charting.renderer.RadarChartRenderer
import com.github.eklipse2k8.charting.renderer.XAxisRendererRadarChart
import com.github.eklipse2k8.charting.renderer.YAxisRendererRadarChart
import com.github.eklipse2k8.charting.utils.Utils
import kotlin.math.max
import kotlin.math.min

/**
 * Implementation of the RadarChart, a "spidernet"-like chart. It works best when displaying 5-10
 * entries per DataSet.
 *
 * @author Philipp Jahoda
 */
class RadarChart
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    PieRadarChartBase<RadarData, IRadarDataSet, RadarEntry>(context, attrs, defStyleAttr) {

  override val dataRenderer: DataRenderer = RadarChartRenderer(this, animator, viewPortHandler)

  /** width of the main web lines */
  private var mWebLineWidth = 2.5f

  /** width of the inner web lines */
  private var mInnerWebLineWidth = 1.5f

  /**
   * Sets the color for the web lines that come from the center. Don't forget to use
   * getResources().getColor(...) when loading a color from the resources. Default: Color.rgb(122,
   * 122, 122)
   */
  var webColor = Color.rgb(122, 122, 122)

  /**
   * Sets the color for the web lines in between the lines that come from the center. Don't forget
   * to use getResources().getColor(...) when loading a color from the resources. Default:
   * Color.rgb(122, 122, 122)
   */
  var webColorInner = Color.rgb(122, 122, 122)

  /**
   * Sets the transparency (alpha) value for all web lines, default: 150, 255 = 100% opaque, 0 =
   * 100% transparent
   */
  var webAlpha = 150

  /** flag indicating if the web lines should be drawn or not */
  private var mDrawWeb = true

  /** modulus that determines how many labels and web-lines are skipped before the next is drawn */
  private var mSkipWebLineCount = 0

  /** the object reprsenting the y-axis labels */
  val yAxis: YAxis = YAxis(AxisDependency.LEFT)

  private var mYAxisRenderer: YAxisRendererRadarChart? = null

  private var mXAxisRenderer: XAxisRendererRadarChart? = null

  val yRange: Float
    get() = yAxis.mAxisRange

  override val highlighter: IHighlighter = RadarHighlighter(this)

  init {
    yAxis.labelXOffset = 10f
    mWebLineWidth = Utils.convertDpToPixel(1.5f)
    mInnerWebLineWidth = Utils.convertDpToPixel(0.75f)
    mYAxisRenderer = YAxisRendererRadarChart(viewPortHandler, yAxis, this)
    mXAxisRenderer = XAxisRendererRadarChart(viewPortHandler, xAxis, this)
  }

  override fun calcMinMax() {
    super.calcMinMax()
    yAxis.calculate(data!!.getYMin(AxisDependency.LEFT), data!!.getYMax(AxisDependency.LEFT))
    xAxis.calculate(0f, data!!.maxEntryCountSet?.entryCount?.toFloat() ?: 0f)
  }

  override fun notifyDataSetChanged() {
    if (data == null) return
    calcMinMax()
    mYAxisRenderer?.computeAxis(yAxis.axisMinimum, yAxis.axisMaximum, yAxis.isInverted)
    mXAxisRenderer?.computeAxis(xAxis.axisMinimum, xAxis.axisMaximum, false)
    if (!legend.isLegendCustom) legendRenderer.computeLegend(data!!)
    calculateOffsets()
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    if (data == null) return

    if (xAxis.isEnabled) mXAxisRenderer!!.computeAxis(xAxis.axisMinimum, xAxis.axisMaximum, false)
    mXAxisRenderer!!.renderAxisLabels(canvas)
    if (mDrawWeb) dataRenderer.drawExtras(canvas)
    if (yAxis.isEnabled && yAxis.isDrawLimitLinesBehindDataEnabled)
        mYAxisRenderer!!.renderLimitLines(canvas)
    dataRenderer.drawData(canvas)
    if (valuesToHighlight()) dataRenderer.drawHighlighted(canvas, mIndicesToHighlight)
    if (yAxis.isEnabled && !yAxis.isDrawLimitLinesBehindDataEnabled)
        mYAxisRenderer!!.renderLimitLines(canvas)
    mYAxisRenderer!!.renderAxisLabels(canvas)
    dataRenderer.drawValues(canvas)
    legendRenderer.renderLegend(canvas)
    drawDescription(canvas)
    drawMarkers(canvas)
  }

  /**
   * Returns the factor that is needed to transform values into pixels.
   *
   * @return
   */
  val factor: Float
    get() {
      val content = viewPortHandler.contentRect
      return min(content.width() / 2f, content.height() / 2f) / yAxis.mAxisRange
    }

  /** Returns the angle that each slice in the radar chart occupies. */
  val sliceAngle: Float
    get() {
      val entryCount = data?.maxEntryCountSet?.entryCount ?: 0
      return if (entryCount > 0) {
        360f / entryCount
      } else {
        0f
      }
    }

  override fun getIndexForAngle(angle: Float): Int {
    // take the current angle of the chart into consideration
    val a = Utils.getNormalizedAngle(attr.angle - rotationAngle)
    val localSliceAngle = sliceAngle
    val max: Int = data?.maxEntryCountSet?.entryCount ?: 0
    var index = 0

    for (i in 0 until max) {
      val referenceAngle = localSliceAngle * (i + 1) - localSliceAngle / 2f
      if (referenceAngle > a) {
        index = i
        break
      }
    }

    return index
  }

  /** Sets the width of the web lines that come from the center. */
  var webLineWidth: Float
    get() = mWebLineWidth
    set(width) {
      mWebLineWidth = Utils.convertDpToPixel(width)
    }

  /** Sets the width of the web lines that are in between the lines coming from the center. */
  var webLineWidthInner: Float
    get() = mInnerWebLineWidth
    set(width) {
      mInnerWebLineWidth = Utils.convertDpToPixel(width)
    }

  /**
   * If set to true, drawing the web is enabled, if set to false, drawing the whole web is disabled.
   * Default: true
   */
  fun setDrawWeb(enabled: Boolean) {
    mDrawWeb = enabled
  }

  /**
   * Sets the number of web-lines that should be skipped on chart web before the next one is drawn.
   * This targets the lines that come from the center of the RadarChart.
   */
  var skipWebLineCount: Int
    get() = mSkipWebLineCount
    set(count) {
      mSkipWebLineCount = max(0, count)
    }

  override val requiredLegendOffset: Float
    get() = legendRenderer.labelPaint.textSize * 4f

  override val requiredBaseOffset: Float
    get() =
        if (xAxis.isEnabled && xAxis.isDrawLabelsEnabled) xAxis.labelRotatedWidth.toFloat()
        else Utils.convertDpToPixel(10f)

  override val radius: Float
    get() {
      val content = viewPortHandler.contentRect
      return min(content.width() / 2f, content.height() / 2f)
    }
}
