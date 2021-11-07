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
import com.github.eklipse2k8.charting.renderer.*
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

  override val dataRenderer: DataRenderer = RadarChartRenderer(this, mAnimator, mViewPortHandler)

  /** width of the main web lines */
  private var mWebLineWidth = 2.5f

  /** width of the inner web lines */
  private var mInnerWebLineWidth = 1.5f

  /**
   * Sets the color for the web lines that come from the center. Don't forget to use
   * getResources().getColor(...) when loading a color from the resources. Default: Color.rgb(122,
   * 122, 122)
   *
   * @param color
   */
  /** color for the main web lines */
  var webColor = Color.rgb(122, 122, 122)

  /**
   * Sets the color for the web lines in between the lines that come from the center. Don't forget
   * to use getResources().getColor(...) when loading a color from the resources. Default:
   * Color.rgb(122, 122, 122)
   *
   * @param color
   */
  /** color for the inner web */
  var webColorInner = Color.rgb(122, 122, 122)

  /**
   * Returns the alpha value for all web lines.
   *
   * @return
   */
  /**
   * Sets the transparency (alpha) value for all web lines, default: 150, 255 = 100% opaque, 0 =
   * 100% transparent
   *
   * @param alpha
   */
  /** transparency the grid is drawn with (0-255) */
  var webAlpha = 150

  /** flag indicating if the web lines should be drawn or not */
  private var mDrawWeb = true

  /** modulus that determines how many labels and web-lines are skipped before the next is drawn */
  private var mSkipWebLineCount = 0
  /**
   * Returns the object that represents all y-labels of the RadarChart.
   *
   * @return
   */
  /** the object reprsenting the y-axis labels */
  var yAxis: YAxis? = null
    private set

  private var mYAxisRenderer: YAxisRendererRadarChart? = null

  private var mXAxisRenderer: XAxisRendererRadarChart? = null

  val yRange: Float
    get() = yAxis?.mAxisRange ?: 0f

  override val highlighter: IHighlighter = RadarHighlighter(this)

  init {
    yAxis = YAxis(AxisDependency.LEFT)
    yAxis!!.labelXOffset = 10f
    mWebLineWidth = Utils.convertDpToPixel(1.5f)
    mInnerWebLineWidth = Utils.convertDpToPixel(0.75f)
    mYAxisRenderer = YAxisRendererRadarChart(mViewPortHandler, yAxis!!, this)
    mXAxisRenderer = XAxisRendererRadarChart(mViewPortHandler, xAxis, this)
  }

  override fun calcMinMax() {
    super.calcMinMax()
    yAxis!!.calculate(data!!.getYMin(AxisDependency.LEFT), data!!.getYMax(AxisDependency.LEFT))
    xAxis.calculate(0f, data!!.maxEntryCountSet?.entryCount?.toFloat() ?: 0f)
  }

  override fun notifyDataSetChanged() {
    if (data == null) return
    calcMinMax()
    mYAxisRenderer?.computeAxis(
        yAxis?.axisMinimum ?: 0f, yAxis?.axisMaximum ?: 0f, yAxis?.isInverted == true)
    mXAxisRenderer?.computeAxis(xAxis.axisMinimum, xAxis.axisMaximum, false)
    if (!mLegend.isLegendCustom) mLegendRenderer.computeLegend(data!!)
    calculateOffsets()
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    if (data == null) return

    if (xAxis.isEnabled) mXAxisRenderer!!.computeAxis(xAxis.axisMinimum, xAxis.axisMaximum, false)
    mXAxisRenderer!!.renderAxisLabels(canvas)
    if (mDrawWeb) dataRenderer.drawExtras(canvas)
    if (yAxis!!.isEnabled && yAxis!!.isDrawLimitLinesBehindDataEnabled)
        mYAxisRenderer!!.renderLimitLines(canvas)
    dataRenderer.drawData(canvas)
    if (valuesToHighlight()) dataRenderer.drawHighlighted(canvas, mIndicesToHighlight)
    if (yAxis!!.isEnabled && !yAxis!!.isDrawLimitLinesBehindDataEnabled)
        mYAxisRenderer!!.renderLimitLines(canvas)
    mYAxisRenderer!!.renderAxisLabels(canvas)
    dataRenderer.drawValues(canvas)
    mLegendRenderer.renderLegend(canvas)
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
      val content = mViewPortHandler.contentRect
      return min(content.width() / 2f, content.height() / 2f) / (yAxis?.mAxisRange ?: 0f)
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

  /**
   * Sets the width of the web lines that come from the center.
   *
   * @param width
   */
  var webLineWidth: Float
    get() = mWebLineWidth
    set(width) {
      mWebLineWidth = Utils.convertDpToPixel(width)
    }

  /**
   * Sets the width of the web lines that are in between the lines coming from the center.
   *
   * @param width
   */
  var webLineWidthInner: Float
    get() = mInnerWebLineWidth
    set(width) {
      mInnerWebLineWidth = Utils.convertDpToPixel(width)
    }

  /**
   * If set to true, drawing the web is enabled, if set to false, drawing the whole web is disabled.
   * Default: true
   *
   * @param enabled
   */
  fun setDrawWeb(enabled: Boolean) {
    mDrawWeb = enabled
  }
  /**
   * Returns the modulus that is used for skipping web-lines.
   *
   * @return
   */
  /**
   * Sets the number of web-lines that should be skipped on chart web before the next one is drawn.
   * This targets the lines that come from the center of the RadarChart.
   *
   * @param count if count = 1 -> 1 line is skipped in between
   */
  var skipWebLineCount: Int
    get() = mSkipWebLineCount
    set(count) {
      mSkipWebLineCount = max(0, count)
    }

  override val requiredLegendOffset: Float
    get() = mLegendRenderer.labelPaint.textSize * 4f

  override val requiredBaseOffset: Float
    get() =
        if (xAxis.isEnabled && xAxis.isDrawLabelsEnabled) xAxis.mLabelRotatedWidth.toFloat()
        else Utils.convertDpToPixel(10f)

  override val radius: Float
    get() {
      val content = mViewPortHandler.contentRect
      return min(content.width() / 2f, content.height() / 2f)
    }
}
