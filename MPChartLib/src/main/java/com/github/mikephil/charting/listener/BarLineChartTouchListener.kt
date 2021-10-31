package com.github.mikephil.charting.listener

import android.annotation.SuppressLint
import android.graphics.Matrix
import android.util.Log
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.animation.AnimationUtils
import com.github.mikephil.charting.charts.BarLineChartBase
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.data.BarLineScatterCandleBubbleData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.interfaces.datasets.IBarLineScatterCandleBubbleDataSet
import com.github.mikephil.charting.interfaces.datasets.IDataSet
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.Utils
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * TouchListener for Bar-, Line-, Scatter- and CandleStickChart with handles all touch interaction.
 * Longpress == Zoom out. Double-Tap == Zoom in.
 *
 * @author Philipp Jahoda
 */
class BarLineChartTouchListener(
    chart:
        BarLineChartBase<
            out BarLineScatterCandleBubbleData<out IBarLineScatterCandleBubbleDataSet<out Entry>>>,
    touchMatrix: Matrix,
    dragTriggerDistance: Float
) :
    ChartTouchListener<
        BarLineChartBase<
            out BarLineScatterCandleBubbleData<out IBarLineScatterCandleBubbleDataSet<out Entry>>>>(
        chart) {
  /**
   * returns the matrix object the listener holds
   *
   * @return
   */
  /** the original touch-matrix from the chart */
  var matrix = Matrix()
    private set

  /** matrix for saving the original matrix state */
  private val mSavedMatrix = Matrix()

  /** point where the touch action started */
  private val mTouchStartPoint = MPPointF.getInstance(0f, 0f)

  /** center between two pointers (fingers on the display) */
  private val mTouchPointCenter = MPPointF.getInstance(0f, 0f)
  private var mSavedXDist = 1f
  private var mSavedYDist = 1f
  private var mSavedDist = 1f
  private var mClosestDataSetToTouch: IDataSet<*>? = null

  /** used for tracking velocity of dragging */
  private var mVelocityTracker: VelocityTracker? = null
  private var mDecelerationLastTime: Long = 0
  private val mDecelerationCurrentPoint = MPPointF.getInstance(0f, 0f)
  private val mDecelerationVelocity = MPPointF.getInstance(0f, 0f)

  /** the distance of movement that will be counted as a drag */
  private var mDragTriggerDist: Float

  /** the minimum distance between the pointers that will trigger a zoom gesture */
  private val mMinScalePointerDistance: Float
  @SuppressLint("ClickableViewAccessibility")
  override fun onTouch(v: View, event: MotionEvent): Boolean {
    if (mVelocityTracker == null) {
      mVelocityTracker = VelocityTracker.obtain()
    }
    mVelocityTracker!!.addMovement(event)
    if (event.actionMasked == MotionEvent.ACTION_CANCEL) {
      if (mVelocityTracker != null) {
        mVelocityTracker!!.recycle()
        mVelocityTracker = null
      }
    }
    if (touchMode == NONE) {
      gestureDetector.onTouchEvent(event)
    }
    if (!chart.isDragEnabled && !chart.isScaleXEnabled && !chart.isScaleYEnabled) return true
    when (event.action and MotionEvent.ACTION_MASK) {
      MotionEvent.ACTION_DOWN -> {
        startAction(event)
        stopDeceleration()
        saveTouchStart(event)
      }
      MotionEvent.ACTION_POINTER_DOWN ->
          if (event.pointerCount >= 2) {
            chart.disableScroll()
            saveTouchStart(event)

            // get the distance between the pointers on the x-axis
            mSavedXDist = getXDist(event)

            // get the distance between the pointers on the y-axis
            mSavedYDist = getYDist(event)

            // get the total distance between the pointers
            mSavedDist = spacing(event)
            if (mSavedDist > 10f) {
              touchMode =
                  if (chart.isPinchZoomEnabled) {
                    PINCH_ZOOM
                  } else {
                    if (chart.isScaleXEnabled != chart.isScaleYEnabled) {
                      if (chart.isScaleXEnabled) X_ZOOM else Y_ZOOM
                    } else {
                      if (mSavedXDist > mSavedYDist) X_ZOOM else Y_ZOOM
                    }
                  }
            }

            // determine the touch-pointer center
            midPoint(mTouchPointCenter, event)
          }
      MotionEvent.ACTION_MOVE ->
          if (touchMode == DRAG) {
            chart.disableScroll()
            val x = if (chart.isDragXEnabled) event.x - mTouchStartPoint.x else 0f
            val y = if (chart.isDragYEnabled) event.y - mTouchStartPoint.y else 0f
            performDrag(event, x, y)
          } else if (touchMode == X_ZOOM || touchMode == Y_ZOOM || touchMode == PINCH_ZOOM) {
            chart.disableScroll()
            if (chart.isScaleXEnabled || chart.isScaleYEnabled) performZoom(event)
          } else if (touchMode == NONE &&
              abs(distance(event.x, mTouchStartPoint.x, event.y, mTouchStartPoint.y)) >
                  mDragTriggerDist) {
            if (chart.isDragEnabled) {
              val shouldPan = !chart.isFullyZoomedOut || !chart.hasNoDragOffset()
              if (shouldPan) {
                val distanceX = abs(event.x - mTouchStartPoint.x)
                val distanceY = abs(event.y - mTouchStartPoint.y)

                // Disable dragging in a direction that's disallowed
                if ((chart.isDragXEnabled || distanceY >= distanceX) &&
                    (chart.isDragYEnabled || distanceY <= distanceX)) {
                  lastGesture = ChartGesture.DRAG
                  touchMode = DRAG
                }
              } else {
                if (chart.isHighlightPerDragEnabled) {
                  lastGesture = ChartGesture.DRAG
                  if (chart.isHighlightPerDragEnabled) performHighlightDrag(event)
                }
              }
            }
          }
      MotionEvent.ACTION_UP -> {
        val velocityTracker = mVelocityTracker
        val pointerId = event.getPointerId(0)
        velocityTracker!!.computeCurrentVelocity(1000, Utils.maximumFlingVelocity.toFloat())
        val velocityY = velocityTracker.getYVelocity(pointerId)
        val velocityX = velocityTracker.getXVelocity(pointerId)
        if (abs(velocityX) > Utils.minimumFlingVelocity ||
            abs(velocityY) > Utils.minimumFlingVelocity) {
          if (touchMode == DRAG && chart.isDragDecelerationEnabled) {
            stopDeceleration()
            mDecelerationLastTime = AnimationUtils.currentAnimationTimeMillis()
            mDecelerationCurrentPoint.x = event.x
            mDecelerationCurrentPoint.y = event.y
            mDecelerationVelocity.x = velocityX
            mDecelerationVelocity.y = velocityY
            Utils.postInvalidateOnAnimation(
                chart) // This causes computeScroll to fire, recommended for this by
            // Google
          }
        }
        if (touchMode == X_ZOOM ||
            touchMode == Y_ZOOM ||
            touchMode == PINCH_ZOOM ||
            touchMode == POST_ZOOM) {

          // Range might have changed, which means that Y-axis labels
          // could have changed in size, affecting Y-axis size.
          // So we need to recalculate offsets.
          chart.calculateOffsets()
          chart.postInvalidate()
        }
        touchMode = NONE
        chart.enableScroll()
        if (mVelocityTracker != null) {
          mVelocityTracker!!.recycle()
          mVelocityTracker = null
        }
        endAction(event)
      }
      MotionEvent.ACTION_POINTER_UP -> {
        mVelocityTracker?.let { Utils.velocityTrackerPointerUpCleanUpIfNecessary(event, it) }
        touchMode = POST_ZOOM
      }
      MotionEvent.ACTION_CANCEL -> {
        touchMode = NONE
        endAction(event)
      }
    }

    // perform the transformation, update the chart
    matrix = chart.viewPortHandler.refresh(matrix, chart, true)
    return true // indicate event was handled
  }
  /** ################ ################ ################ ################ */
  /** BELOW CODE PERFORMS THE ACTUAL TOUCH ACTIONS */
  /**
   * Saves the current Matrix state and the touch-start point.
   *
   * @param event
   */
  private fun saveTouchStart(event: MotionEvent) {
    mSavedMatrix.set(matrix)
    mTouchStartPoint.x = event.x
    mTouchStartPoint.y = event.y
    mClosestDataSetToTouch = chart.getDataSetByTouchPoint(event.x, event.y)
  }

  /**
   * Performs all necessary operations needed for dragging.
   *
   * @param event
   */
  private fun performDrag(event: MotionEvent, distanceX: Float, distanceY: Float) {
    var distanceX = distanceX
    var distanceY = distanceY
    lastGesture = ChartGesture.DRAG
    matrix.set(mSavedMatrix)
    val l = chart.onChartGestureListener

    // check if axis is inverted
    if (inverted()) {

      // if there is an inverted horizontalbarchart
      if (chart is HorizontalBarChart) {
        distanceX = -distanceX
      } else {
        distanceY = -distanceY
      }
    }
    matrix.postTranslate(distanceX, distanceY)
    l?.onChartTranslate(event, distanceX, distanceY)
  }

  /**
   * Performs the all operations necessary for pinch and axis zoom.
   *
   * @param event
   */
  private fun performZoom(event: MotionEvent) {
    if (event.pointerCount >= 2) { // two finger zoom
      val l = chart.onChartGestureListener

      // get the distance between the pointers of the touch event
      val totalDist = spacing(event)
      if (totalDist > mMinScalePointerDistance) {

        // get the translation
        val t = getTrans(mTouchPointCenter.x, mTouchPointCenter.y)
        val h = chart.viewPortHandler

        // take actions depending on the activated touch mode
        if (touchMode == PINCH_ZOOM) {
          lastGesture = ChartGesture.PINCH_ZOOM
          val scale = totalDist / mSavedDist // total scale
          val isZoomingOut = scale < 1
          val canZoomMoreX = if (isZoomingOut) h.canZoomOutMoreX() else h.canZoomInMoreX()
          val canZoomMoreY = if (isZoomingOut) h.canZoomOutMoreY() else h.canZoomInMoreY()
          val scaleX = if (chart.isScaleXEnabled) scale else 1f
          val scaleY = if (chart.isScaleYEnabled) scale else 1f
          if (canZoomMoreY || canZoomMoreX) {
            matrix.set(mSavedMatrix)
            matrix.postScale(scaleX, scaleY, t.x, t.y)
            l?.onChartScale(event, scaleX, scaleY)
          }
        } else if (touchMode == X_ZOOM && chart.isScaleXEnabled) {
          lastGesture = ChartGesture.X_ZOOM
          val xDist = getXDist(event)
          val scaleX = xDist / mSavedXDist // x-axis scale
          val isZoomingOut = scaleX < 1
          val canZoomMoreX = if (isZoomingOut) h.canZoomOutMoreX() else h.canZoomInMoreX()
          if (canZoomMoreX) {
            matrix.set(mSavedMatrix)
            matrix.postScale(scaleX, 1f, t.x, t.y)
            l?.onChartScale(event, scaleX, 1f)
          }
        } else if (touchMode == Y_ZOOM && chart.isScaleYEnabled) {
          lastGesture = ChartGesture.Y_ZOOM
          val yDist = getYDist(event)
          val scaleY = yDist / mSavedYDist // y-axis scale
          val isZoomingOut = scaleY < 1
          val canZoomMoreY = if (isZoomingOut) h.canZoomOutMoreY() else h.canZoomInMoreY()
          if (canZoomMoreY) {
            matrix.set(mSavedMatrix)
            matrix.postScale(1f, scaleY, t.x, t.y)
            l?.onChartScale(event, 1f, scaleY)
          }
        }
        MPPointF.recycleInstance(t)
      }
    }
  }

  /**
   * Highlights upon dragging, generates callbacks for the selection-listener.
   *
   * @param e
   */
  private fun performHighlightDrag(e: MotionEvent) {
    val h = chart.getHighlightByTouchPoint(e.x, e.y)
    if (h != null && !h.equalTo(lastHighlighted)) {
      lastHighlighted = h
      chart.highlightValue(h, true)
    }
  }

  /**
   * Returns a recyclable MPPointF instance. returns the correct translation depending on the
   * provided x and y touch points
   *
   * @param x
   * @param y
   * @return
   */
  fun getTrans(x: Float, y: Float): MPPointF {
    val vph = chart.viewPortHandler
    val xTrans = x - vph.offsetLeft()

    // check if axis is inverted
    val yTrans: Float =
        if (inverted()) {
          -(y - vph.offsetTop())
        } else {
          -(chart.measuredHeight - y - vph.offsetBottom())
        }
    return MPPointF.getInstance(xTrans, yTrans)
  }

  /**
   * Returns true if the current touch situation should be interpreted as inverted, false if not.
   *
   * @return
   */
  private fun inverted(): Boolean {
    return mClosestDataSetToTouch == null && chart.isAnyAxisInverted ||
        (mClosestDataSetToTouch != null &&
            chart.isInverted(mClosestDataSetToTouch!!.axisDependency))
  }
  /** ################ ################ ################ ################ */
  /** GETTERS AND GESTURE RECOGNITION BELOW */
  /**
   * Sets the minimum distance that will be interpreted as a "drag" by the chart in dp. Default: 3dp
   *
   * @param dragTriggerDistance
   */
  fun setDragTriggerDist(dragTriggerDistance: Float) {
    mDragTriggerDist = Utils.convertDpToPixel(dragTriggerDistance)
  }

  override fun onDoubleTap(e: MotionEvent): Boolean {
    lastGesture = ChartGesture.DOUBLE_TAP
    val l = chart.onChartGestureListener
    l?.onChartDoubleTapped(e)

    // check if double-tap zooming is enabled
    if (chart.isDoubleTapToZoomEnabled && chart.data?.entryCount!! > 0) {
      val trans = getTrans(e.x, e.y)
      val scaleX = if (chart.isScaleXEnabled) 1.4f else 1f
      val scaleY = if (chart.isScaleYEnabled) 1.4f else 1f
      chart.zoom(scaleX, scaleY, trans.x, trans.y)
      if (chart.isLogEnabled)
          Log.i("BarlineChartTouch", "Double-Tap, Zooming In, x: " + trans.x + ", y: " + trans.y)
      l?.onChartScale(e, scaleX, scaleY)
      MPPointF.recycleInstance(trans)
    }
    return super.onDoubleTap(e)
  }

  override fun onLongPress(e: MotionEvent) {
    lastGesture = ChartGesture.LONG_PRESS
    val l = chart.onChartGestureListener
    l?.onChartLongPressed(e)
  }

  override fun onSingleTapUp(e: MotionEvent): Boolean {
    lastGesture = ChartGesture.SINGLE_TAP
    val l = chart.onChartGestureListener
    l?.onChartSingleTapped(e)
    if (!chart.isHighlightPerTapEnabled) {
      return false
    }
    val h = chart.getHighlightByTouchPoint(e.x, e.y)
    performHighlight(h, e)
    return super.onSingleTapUp(e)
  }

  override fun onFling(
      e1: MotionEvent,
      e2: MotionEvent,
      velocityX: Float,
      velocityY: Float
  ): Boolean {
    lastGesture = ChartGesture.FLING
    val l = chart.onChartGestureListener
    l?.onChartFling(e1, e2, velocityX, velocityY)
    return super.onFling(e1, e2, velocityX, velocityY)
  }

  private fun stopDeceleration() {
    mDecelerationVelocity.x = 0f
    mDecelerationVelocity.y = 0f
  }

  fun computeScroll() {
    if (mDecelerationVelocity.x == 0f && mDecelerationVelocity.y == 0f)
        return // There's no deceleration in progress
    val currentTime = AnimationUtils.currentAnimationTimeMillis()
    mDecelerationVelocity.x *= chart.dragDecelerationFrictionCoef
    mDecelerationVelocity.y *= chart.dragDecelerationFrictionCoef
    val timeInterval = (currentTime - mDecelerationLastTime).toFloat() / 1000f
    val distanceX = mDecelerationVelocity.x * timeInterval
    val distanceY = mDecelerationVelocity.y * timeInterval
    mDecelerationCurrentPoint.x += distanceX
    mDecelerationCurrentPoint.y += distanceY
    val event =
        MotionEvent.obtain(
            currentTime,
            currentTime,
            MotionEvent.ACTION_MOVE,
            mDecelerationCurrentPoint.x,
            mDecelerationCurrentPoint.y,
            0)
    val dragDistanceX =
        if (chart.isDragXEnabled) mDecelerationCurrentPoint.x - mTouchStartPoint.x else 0f
    val dragDistanceY =
        if (chart.isDragYEnabled) mDecelerationCurrentPoint.y - mTouchStartPoint.y else 0f
    performDrag(event, dragDistanceX, dragDistanceY)
    event.recycle()
    matrix = chart.viewPortHandler.refresh(matrix, chart, false)
    mDecelerationLastTime = currentTime
    if (Math.abs(mDecelerationVelocity.x) >= 0.01 || Math.abs(mDecelerationVelocity.y) >= 0.01)
        Utils.postInvalidateOnAnimation(
            chart) // This causes computeScroll to fire, recommended for this by Google
    else {
      // Range might have changed, which means that Y-axis labels
      // could have changed in size, affecting Y-axis size.
      // So we need to recalculate offsets.
      chart.calculateOffsets()
      chart.postInvalidate()
      stopDeceleration()
    }
  }

  companion object {
    /** ################ ################ ################ ################ */
    /** DOING THE MATH BELOW ;-) */
    /**
     * Determines the center point between two pointer touch points.
     *
     * @param point
     * @param event
     */
    private fun midPoint(point: MPPointF, event: MotionEvent) {
      val x = event.getX(0) + event.getX(1)
      val y = event.getY(0) + event.getY(1)
      point.x = x / 2f
      point.y = y / 2f
    }

    /**
     * returns the distance between two pointer touch points
     *
     * @param event
     * @return
     */
    private fun spacing(event: MotionEvent): Float {
      val x = event.getX(0) - event.getX(1)
      val y = event.getY(0) - event.getY(1)
      return sqrt((x * x + y * y).toDouble()).toFloat()
    }

    /**
     * calculates the distance on the x-axis between two pointers (fingers on the display)
     *
     * @param e
     * @return
     */
    private fun getXDist(e: MotionEvent): Float {
      return abs(e.getX(0) - e.getX(1))
    }

    /**
     * calculates the distance on the y-axis between two pointers (fingers on the display)
     *
     * @param e
     * @return
     */
    private fun getYDist(e: MotionEvent): Float {
      return abs(e.getY(0) - e.getY(1))
    }
  }

  /**
   * Constructor with initialization parameters.
   *
   * @param chart instance of the chart
   * @param touchMatrix the touch-matrix of the chart
   * @param dragTriggerDistance the minimum movement distance that will be interpreted as a "drag"
   * gesture in dp (3dp equals to about 9 pixels on a 5.5" FHD screen)
   */
  init {
    matrix = touchMatrix
    mDragTriggerDist = Utils.convertDpToPixel(dragTriggerDistance)
    mMinScalePointerDistance = Utils.convertDpToPixel(3.5f)
  }
}
