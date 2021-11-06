package com.github.mikephil.charting.charts

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import com.github.mikephil.charting.animation.Easing.EasingFunction
import com.github.mikephil.charting.components.Legend.*
import com.github.mikephil.charting.data.ChartData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.interfaces.datasets.IDataSet
import com.github.mikephil.charting.listener.PieRadarChartTouchListener
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.Utils.convertDpToPixel
import com.github.mikephil.charting.utils.Utils.getNormalizedAngle
import com.github.mikephil.charting.utils.toRadians
import kotlin.math.*

private val TAG = PieRadarChartBase::class.java.simpleName

/**
 * Baseclass of PieChart and RadarChart.
 *
 * @author Philipp Jahoda
 */
abstract class PieRadarChartBase<T, S, E>
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    Chart<T, S, E>(context, attrs, defStyleAttr) where
T : ChartData<S, E>,
S : IDataSet<E>,
E : Entry {

  /** holds the normalized version of the current rotation angle of the chart */
  private var mRotationAngle = 270f
  /**
   * gets the raw version of the current rotation angle of the pie chart the returned value could be
   * any value, negative or positive, outside of the 360 degrees. this is used when working with
   * rotation direction, mainly by gestures and animations.
   *
   * @return
   */
  /** holds the raw version of the current rotation angle of the chart */
  var rawRotationAngle = 270f
    private set
  /**
   * Returns true if rotation of the chart by touch is enabled, false if not.
   *
   * @return
   */
  /**
   * Set this to true to enable the rotation / spinning of the chart by touch. Set it to false to
   * disable it. Default: true
   *
   * @param enabled
   */
  /** flag that indicates if rotation is enabled or not */
  var isRotationEnabled = true

  /** Sets the minimum offset (padding) around the chart, defaults to 0.f */
  var minOffset = 0f

  init {
    onTouchListener = PieRadarChartTouchListener(this)
  }

  override fun calcMinMax() {}

  override val maxVisibleCount: Int
    get() = data?.entryCount ?: 0

  override fun onTouchEvent(event: MotionEvent): Boolean {
    // use the pie- and radarchart listener own listener
    val touchListener = onTouchListener ?: return super.onTouchEvent(event)
    return touchListener.onTouch(this, event)
  }

  override fun computeScroll() {
    val touchListener = onTouchListener
    if (touchListener is PieRadarChartTouchListener) {
      touchListener.computeScroll()
    }
  }

  override fun notifyDataSetChanged() {
    if (data != null) {
      calcMinMax()
      mLegendRenderer.computeLegend(data)
      calculateOffsets()
    }
  }

  public override fun calculateOffsets() {
    var legendLeft = 0f
    var legendRight = 0f
    var legendBottom = 0f
    var legendTop = 0f
    if (mLegend != null && mLegend.isEnabled && !mLegend.isDrawInsideEnabled) {
      val fullLegendWidth =
          Math.min(mLegend.mNeededWidth, mViewPortHandler.chartWidth * mLegend.maxSizePercent)
      when (mLegend.orientation) {
        LegendOrientation.VERTICAL -> {
          var xLegendOffset = 0f
          if (mLegend.horizontalAlignment === LegendHorizontalAlignment.LEFT ||
              mLegend.horizontalAlignment === LegendHorizontalAlignment.RIGHT) {
            if (mLegend.verticalAlignment === LegendVerticalAlignment.CENTER) {
              // this is the space between the legend and the chart
              val spacing = convertDpToPixel(13f)
              xLegendOffset = fullLegendWidth + spacing
            } else {
              // this is the space between the legend and the chart
              val spacing = convertDpToPixel(8f)
              val legendWidth = fullLegendWidth + spacing
              val legendHeight = mLegend.mNeededHeight + mLegend.mTextHeightMax
              val center = getCenter() ?: MPPointF.getInstance(0f, 0f)
              val bottomX =
                  if (mLegend.horizontalAlignment === LegendHorizontalAlignment.RIGHT)
                      width - legendWidth + 15f
                  else legendWidth - 15f
              val bottomY = legendHeight + 15f
              val distLegend = distanceToCenter(bottomX, bottomY)
              val reference = getPosition(center, radius, getAngleForPoint(bottomX, bottomY))
              val distReference = distanceToCenter(reference.x, reference.y)
              val minOffset = convertDpToPixel(5f)
              if (bottomY >= center.y && height - legendWidth > width) {
                xLegendOffset = legendWidth
              } else if (distLegend < distReference) {
                val diff = distReference - distLegend
                xLegendOffset = minOffset + diff
              }
              MPPointF.recycleInstance(center)
              MPPointF.recycleInstance(reference)
            }
          }
          when (mLegend.horizontalAlignment) {
            LegendHorizontalAlignment.LEFT -> legendLeft = xLegendOffset
            LegendHorizontalAlignment.RIGHT -> legendRight = xLegendOffset
            LegendHorizontalAlignment.CENTER ->
                when (mLegend.verticalAlignment) {
                  LegendVerticalAlignment.TOP ->
                      legendTop =
                          Math.min(
                              mLegend.mNeededHeight,
                              mViewPortHandler.chartHeight * mLegend.maxSizePercent)
                  LegendVerticalAlignment.BOTTOM ->
                      legendBottom =
                          Math.min(
                              mLegend.mNeededHeight,
                              mViewPortHandler.chartHeight * mLegend.maxSizePercent)
                }
          }
        }
        LegendOrientation.HORIZONTAL -> {
          var yLegendOffset = 0f
          if (mLegend.verticalAlignment === LegendVerticalAlignment.TOP ||
              mLegend.verticalAlignment === LegendVerticalAlignment.BOTTOM) {

            // It's possible that we do not need this offset anymore as it
            //   is available through the extraOffsets, but changing it can mean
            //   changing default visibility for existing apps.
            val yOffset = requiredLegendOffset
            yLegendOffset =
                Math.min(
                    mLegend.mNeededHeight + yOffset,
                    mViewPortHandler.chartHeight * mLegend.maxSizePercent)
            when (mLegend.verticalAlignment) {
              LegendVerticalAlignment.TOP -> legendTop = yLegendOffset
              LegendVerticalAlignment.BOTTOM -> legendBottom = yLegendOffset
            }
          }
        }
      }
      legendLeft += requiredBaseOffset
      legendRight += requiredBaseOffset
      legendTop += requiredBaseOffset
      legendBottom += requiredBaseOffset
    }
    var minOffset = convertDpToPixel(minOffset)
    if (this is RadarChart) {
      val x = xAxis
      if (x.isEnabled && x.isDrawLabelsEnabled) {
        minOffset = max(minOffset, x.mLabelRotatedWidth.toFloat())
      }
    }
    legendTop += extraTopOffset
    legendRight += extraRightOffset
    legendBottom += extraBottomOffset
    legendLeft += extraLeftOffset
    val offsetLeft = minOffset.coerceAtLeast(legendLeft)
    val offsetTop = minOffset.coerceAtLeast(legendTop)
    val offsetRight = minOffset.coerceAtLeast(legendRight)
    val offsetBottom = minOffset.coerceAtLeast(requiredBaseOffset.coerceAtLeast(legendBottom))
    mViewPortHandler.restrainViewPort(offsetLeft, offsetTop, offsetRight, offsetBottom)
    if (mLogEnabled)
        Log.i(
            TAG,
            "offsetLeft: $offsetLeft, offsetTop: $offsetTop, offsetRight: $offsetRight, offsetBottom: $offsetBottom")
  }

  /**
   * returns the angle relative to the chart center for the given point on the chart in degrees. The
   * angle is always between 0 and 360°, 0° is NORTH, 90° is EAST, ...
   *
   * @param x
   * @param y
   * @return
   */
  fun getAngleForPoint(x: Float, y: Float): Float {
    val c: MPPointF = centerOffsets ?: MPPointF.getInstance(0f, 0f)
    val tx = (x - c.x).toDouble()
    val ty = (y - c.y).toDouble()
    val length = sqrt(tx * tx + ty * ty)
    val r = acos(ty / length)
    var angle = Math.toDegrees(r).toFloat()
    if (x > c.x) angle = 360f - angle

    // add 90° because chart starts EAST
    angle += 90f

    // neutralize overflow
    if (angle > 360f) angle -= 360f
    MPPointF.recycleInstance(c)
    return angle
  }

  /**
   * Returns a recyclable MPPointF instance. Calculates the position around a center point,
   * depending on the distance from the center, and the angle of the position around the center.
   *
   * @param center
   * @param dist
   * @param angle in degrees, converted to radians internally
   * @return
   */
  fun getPosition(center: MPPointF, dist: Float, angle: Float): MPPointF {
    val p = MPPointF.getInstance(0f, 0f)
    getPosition(center, dist, angle, p)
    return p
  }

  fun getPosition(center: MPPointF, dist: Float, angle: Float, outputPoint: MPPointF) {
    outputPoint.x = (center.x + dist * cos(angle.toRadians()))
    outputPoint.y = (center.y + dist * sin(angle.toRadians()))
  }

  /**
   * Returns the distance of a certain point on the chart to the center of the chart.
   *
   * @param x
   * @param y
   * @return
   */
  fun distanceToCenter(x: Float, y: Float): Float {
    val c: MPPointF = centerOffsets ?: MPPointF.getInstance(0f, 0f)
    var dist = 0f
    var xDist = 0f
    var yDist = 0f
    xDist =
        if (x > c.x) {
          x - c.x
        } else {
          c.x - x
        }
    yDist =
        if (y > c.y) {
          y - c.y
        } else {
          c.y - y
        }

    // pythagoras
    dist = sqrt(xDist.pow(2.0f) + yDist.pow(2.0f))
    MPPointF.recycleInstance(c)
    return dist
  }

  /**
   * Returns the xIndex for the given angle around the center of the chart. Returns -1 if not found
   * / outofbounds.
   *
   * @param angle
   * @return
   */
  abstract fun getIndexForAngle(angle: Float): Int
  /**
   * gets a normalized version of the current rotation angle of the pie chart, which will always be
   * between 0.0 < 360.0
   *
   * @return
   */
  /**
   * Set an offset for the rotation of the RadarChart in degrees. Default 270f --> top (NORTH)
   *
   * @param angle
   */
  var rotationAngle: Float
    get() = mRotationAngle
    set(angle) {
      rawRotationAngle = angle
      mRotationAngle = getNormalizedAngle(rawRotationAngle)
    }

  /**
   * returns the diameter of the pie- or radar-chart
   *
   * @return
   */
  val diameter: Float
    get() {
      val content = mViewPortHandler.contentRect
      content.left += extraLeftOffset
      content.top += extraTopOffset
      content.right -= extraRightOffset
      content.bottom -= extraBottomOffset
      return Math.min(content.width(), content.height())
    }

  /**
   * Returns the radius of the chart in pixels.
   *
   * @return
   */
  abstract val radius: Float

  /**
   * Returns the required offset for the chart legend.
   *
   * @return
   */
  protected abstract val requiredLegendOffset: Float

  /**
   * Returns the base offset needed for the chart without calculating the legend size.
   *
   * @return
   */
  abstract val requiredBaseOffset: Float

  override val yChartMax: Float = 0f

  override val yChartMin: Float = 0f

  /**
   * Applys a spin animation to the Chart.
   *
   * @param durationmillis
   * @param fromangle
   * @param toangle
   */
  fun spin(durationmillis: Int, fromangle: Float, toangle: Float, easing: EasingFunction?) {
    rotationAngle = fromangle
    val spinAnimator = ObjectAnimator.ofFloat(this, "rotationAngle", fromangle, toangle)
    spinAnimator.duration = durationmillis.toLong()
    spinAnimator.interpolator = easing
    spinAnimator.addUpdateListener { postInvalidate() }
    spinAnimator.start()
  }
}
