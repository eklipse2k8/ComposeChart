package com.github.mikephil.charting.charts

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.components.YAxis.AxisDependency
import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.highlight.RadarHighlighter
import com.github.mikephil.charting.renderer.RadarChartRenderer
import com.github.mikephil.charting.renderer.XAxisRendererRadarChart
import com.github.mikephil.charting.renderer.YAxisRendererRadarChart
import com.github.mikephil.charting.utils.Utils
import kotlin.math.max
import kotlin.math.min

/**
 * Implementation of the RadarChart, a "spidernet"-like chart. It works best when displaying 5-10
 * entries per DataSet.
 *
 * @author Philipp Jahoda
 */
class RadarChart : PieRadarChartBase<RadarData> {
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

  constructor(context: Context?) : super(context)

  constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

  constructor(
      context: Context?,
      attrs: AttributeSet?,
      defStyle: Int
  ) : super(context, attrs, defStyle)

  override fun init() {
    super.init()
    yAxis = YAxis(AxisDependency.LEFT)
    yAxis!!.labelXOffset = 10f
    mWebLineWidth = Utils.convertDpToPixel(1.5f)
    mInnerWebLineWidth = Utils.convertDpToPixel(0.75f)
    mRenderer = RadarChartRenderer(this, mAnimator, mViewPortHandler)
    mYAxisRenderer = YAxisRendererRadarChart(mViewPortHandler, yAxis!!, this)
    mXAxisRenderer = XAxisRendererRadarChart(mViewPortHandler, mXAxis, this)
    mHighlighter = RadarHighlighter(this)
  }

  override fun calcMinMax() {
    super.calcMinMax()
    yAxis!!.calculate(mData!!.getYMin(AxisDependency.LEFT), mData!!.getYMax(AxisDependency.LEFT))
    mXAxis.calculate(0f, mData!!.maxEntryCountSet.entryCount.toFloat())
  }

  override fun notifyDataSetChanged() {
    if (mData == null) return
    calcMinMax()
    mYAxisRenderer?.computeAxis(
        yAxis?.axisMinimum ?: 0f, yAxis?.axisMaximum ?: 0f, yAxis?.isInverted == true)
    mXAxisRenderer?.computeAxis(mXAxis.axisMinimum, mXAxis.axisMaximum, false)
    if (mLegend != null && !mLegend.isLegendCustom) mLegendRenderer.computeLegend(mData!!)
    calculateOffsets()
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    if (mData == null) return

    if (mXAxis.isEnabled)
        mXAxisRenderer!!.computeAxis(mXAxis.axisMinimum, mXAxis.axisMaximum, false)
    mXAxisRenderer!!.renderAxisLabels(canvas)
    if (mDrawWeb) mRenderer.drawExtras(canvas)
    if (yAxis!!.isEnabled && yAxis!!.isDrawLimitLinesBehindDataEnabled)
        mYAxisRenderer!!.renderLimitLines(canvas)
    mRenderer.drawData(canvas)
    if (valuesToHighlight()) mRenderer.drawHighlighted(canvas, mIndicesToHighlight)
    if (yAxis!!.isEnabled && !yAxis!!.isDrawLimitLinesBehindDataEnabled)
        mYAxisRenderer!!.renderLimitLines(canvas)
    mYAxisRenderer!!.renderAxisLabels(canvas)
    mRenderer.drawValues(canvas)
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

  /**
   * Returns the angle that each slice in the radar chart occupies.
   *
   * @return
   */
  val sliceAngle: Float
    get() = 360f / mData!!.maxEntryCountSet.entryCount.toFloat()

  override fun getIndexForAngle(angle: Float): Int {
    // take the current angle of the chart into consideration
    val a = Utils.getNormalizedAngle(angle - rotationAngle)
    val sliceangle = sliceAngle
    val max = mData!!.maxEntryCountSet.entryCount
    var index = 0
    for (i in 0 until max) {
      val referenceAngle = sliceangle * (i + 1) - sliceangle / 2f
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
        if (mXAxis.isEnabled && mXAxis.isDrawLabelsEnabled) mXAxis.mLabelRotatedWidth.toFloat()
        else Utils.convertDpToPixel(10f)

  override val radius: Float
    get() {
      val content = mViewPortHandler.contentRect
      return min(content.width() / 2f, content.height() / 2f)
    }
}
