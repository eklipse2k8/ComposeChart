package com.github.eklipse2k8.charting.listener

import android.annotation.SuppressLint
import android.graphics.Matrix
import android.util.Log
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.animation.AnimationUtils
import com.github.eklipse2k8.charting.charts.AnyBarChart
import com.github.eklipse2k8.charting.charts.AnyBarLineChart
import com.github.eklipse2k8.charting.charts.HorizontalBarChart
import com.github.eklipse2k8.charting.interfaces.datasets.IDataSet
import com.github.eklipse2k8.charting.utils.MPPointF
import com.github.eklipse2k8.charting.utils.Utils
import kotlin.math.abs
import kotlin.math.sqrt

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

private val TAG = BarLineChartTouchListener::class.java.simpleName

/**
 * TouchListener for Bar-, Line-, Scatter- and CandleStickChart with handles all touch interaction.
 * Longpress == Zoom out. Double-Tap == Zoom in.
 *
 * @param chart instance of the chart
 * @param matrix the touch-matrix of the chart
 * @param dragTriggerDistance the minimum movement distance that will be interpreted as a "drag"
 * gesture in dp (3dp equals to about 9 pixels on a 5.5" FHD screen)
 *
 * @author Philipp Jahoda
 */
class BarLineChartTouchListener(
    chart: AnyBarChart,
    private var matrix: Matrix,
    dragTriggerDistance: Float
) : ChartTouchListener<AnyBarChart>(chart) {

  /** matrix for saving the original matrix state */
  private val savedMatrix = Matrix()

  /** point where the touch action started */
  private val touchStartPoint = MPPointF.getInstance(0f, 0f)

  /** center between two pointers (fingers on the display) */
  private val touchPointCenter = MPPointF.getInstance(0f, 0f)
  private var savedXDist = 1f
  private var savedYDist = 1f
  private var savedDist = 1f
  private var closestDataSetToTouch: IDataSet<*>? = null

  /** used for tracking velocity of dragging */
  private var velocityTracker: VelocityTracker? = null
  private var decelerationLastTime: Long = 0
  private val decelerationCurrentPoint = MPPointF.getInstance(0f, 0f)
  private val decelerationVelocity = MPPointF.getInstance(0f, 0f)

  /** the distance of movement that will be counted as a drag */
  private var dragTriggerDist: Float = Utils.convertDpToPixel(dragTriggerDistance)

  /** the minimum distance between the pointers that will trigger a zoom gesture */
  private val minScalePointerDistance: Float = Utils.convertDpToPixel(3.5f)

  @SuppressLint("ClickableViewAccessibility", "Recycle")
  override fun onTouch(v: View, event: MotionEvent): Boolean {
    val tracker: VelocityTracker? =
        if (event.actionMasked == MotionEvent.ACTION_CANCEL) {
          velocityTracker?.recycle()
          velocityTracker = null
          null
        } else {
          (velocityTracker ?: VelocityTracker.obtain()).apply { addMovement(event) }
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
            savedXDist = getXDist(event)

            // get the distance between the pointers on the y-axis
            savedYDist = getYDist(event)

            // get the total distance between the pointers
            savedDist = spacing(event)
            if (savedDist > 10f) {
              touchMode =
                  if (chart.isPinchZoomEnabled) {
                    PINCH_ZOOM
                  } else {
                    if (chart.isScaleXEnabled != chart.isScaleYEnabled) {
                      if (chart.isScaleXEnabled) X_ZOOM else Y_ZOOM
                    } else {
                      if (savedXDist > savedYDist) X_ZOOM else Y_ZOOM
                    }
                  }
            }

            // determine the touch-pointer center
            midPoint(touchPointCenter, event)
          }
      MotionEvent.ACTION_MOVE ->
          if (touchMode == DRAG) {
            chart.disableScroll()
            val x = if (chart.isDragXEnabled) event.x - touchStartPoint.x else 0f
            val y = if (chart.isDragYEnabled) event.y - touchStartPoint.y else 0f
            performDrag(event, x, y)
          } else if (touchMode == X_ZOOM || touchMode == Y_ZOOM || touchMode == PINCH_ZOOM) {
            chart.disableScroll()
            if (chart.isScaleXEnabled || chart.isScaleYEnabled) performZoom(event)
          } else if (touchMode == NONE &&
              abs(distance(event.x, touchStartPoint.x, event.y, touchStartPoint.y)) >
                  dragTriggerDist) {
            if (chart.isDragEnabled) {
              val shouldPan = !chart.isFullyZoomedOut || !chart.hasNoDragOffset()
              if (shouldPan) {
                val distanceX = abs(event.x - touchStartPoint.x)
                val distanceY = abs(event.y - touchStartPoint.y)

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
        if (tracker == null) return false
        val pointerId = event.getPointerId(0)
        tracker.computeCurrentVelocity(1000, Utils.maximumFlingVelocity.toFloat())
        val velocityY = tracker.getYVelocity(pointerId)
        val velocityX = tracker.getXVelocity(pointerId)
        if (abs(velocityX) > Utils.minimumFlingVelocity ||
            abs(velocityY) > Utils.minimumFlingVelocity) {
          if (touchMode == DRAG && chart.isDragDecelerationEnabled()) {
            stopDeceleration()
            decelerationLastTime = AnimationUtils.currentAnimationTimeMillis()
            decelerationCurrentPoint.x = event.x
            decelerationCurrentPoint.y = event.y
            decelerationVelocity.x = velocityX
            decelerationVelocity.y = velocityY
            // This causes computeScroll to fire, recommended for this by Google
            Utils.postInvalidateOnAnimation(chart)
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
        tracker.recycle()
        velocityTracker = null
        endAction(event)
      }
      MotionEvent.ACTION_POINTER_UP -> {
        if (tracker == null) return false
        Utils.velocityTrackerPointerUpCleanUpIfNecessary(event, tracker)
        touchMode = POST_ZOOM
      }
      MotionEvent.ACTION_CANCEL -> {
        touchMode = NONE
        endAction(event)
      }
    }

    // perform the transformation, update the chart
    val viewport = chart.viewPortHandler
    matrix = viewport.refresh(matrix, chart, true)
    return true // indicate event was handled
  }

  /**
   * Saves the current Matrix state and the touch-start point.
   *
   * @param event
   */
  private fun saveTouchStart(event: MotionEvent) {
    savedMatrix.set(matrix)
    touchStartPoint.x = event.x
    touchStartPoint.y = event.y
    closestDataSetToTouch = chart.getDataSetByTouchPoint(event.x, event.y)
  }

  /**
   * Performs all necessary operations needed for dragging.
   *
   * @param event
   */
  private fun performDrag(event: MotionEvent, distanceX: Float, distanceY: Float) {
    var distX = distanceX
    var distY = distanceY
    lastGesture = ChartGesture.DRAG
    matrix.set(savedMatrix)


    // check if axis is inverted
    if (inverted()) {
      // if there is an inverted horizontalbarchart
      if (chart is HorizontalBarChart) {
        distX = -distanceX
      } else {
        distY = -distY
      }
    }
    matrix.postTranslate(distX, distY)
    chart.onChartGestureListener.onChartTranslate(event, distX, distY)
  }

  /**
   * Performs the all operations necessary for pinch and axis zoom.
   *
   * @param event
   */
  private fun performZoom(event: MotionEvent) {
    if (event.pointerCount >= 2) { // two finger zoom

      // get the distance between the pointers of the touch event
      val totalDist = spacing(event)
      if (totalDist > minScalePointerDistance) {

        // get the translation
        val t = getTrans(touchPointCenter.x, touchPointCenter.y)
        val h = chart.viewPortHandler

        // take actions depending on the activated touch mode
        if (touchMode == PINCH_ZOOM) {
          lastGesture = ChartGesture.PINCH_ZOOM
          val scale = totalDist / savedDist // total scale
          val isZoomingOut = scale < 1
          val canZoomMoreX = if (isZoomingOut) h.canZoomOutMoreX() else h.canZoomInMoreX()
          val canZoomMoreY = if (isZoomingOut) h.canZoomOutMoreY() else h.canZoomInMoreY()
          val scaleX = if (chart.isScaleXEnabled) scale else 1f
          val scaleY = if (chart.isScaleYEnabled) scale else 1f
          if (canZoomMoreY || canZoomMoreX) {
            matrix.set(savedMatrix)
            matrix.postScale(scaleX, scaleY, t.x, t.y)
            chart.onChartGestureListener.onChartScale(event, scaleX, scaleY)
          }
        } else if (touchMode == X_ZOOM && chart.isScaleXEnabled) {
          lastGesture = ChartGesture.X_ZOOM
          val xDist = getXDist(event)
          val scaleX = xDist / savedXDist // x-axis scale
          val isZoomingOut = scaleX < 1
          val canZoomMoreX = if (isZoomingOut) h.canZoomOutMoreX() else h.canZoomInMoreX()
          if (canZoomMoreX) {
            matrix.set(savedMatrix)
            matrix.postScale(scaleX, 1f, t.x, t.y)
            chart.onChartGestureListener.onChartScale(event, scaleX, 1f)
          }
        } else if (touchMode == Y_ZOOM && chart.isScaleYEnabled) {
          lastGesture = ChartGesture.Y_ZOOM
          val yDist = getYDist(event)
          val scaleY = yDist / savedYDist // y-axis scale
          val isZoomingOut = scaleY < 1
          val canZoomMoreY = if (isZoomingOut) h.canZoomOutMoreY() else h.canZoomInMoreY()
          if (canZoomMoreY) {
            matrix.set(savedMatrix)
            matrix.postScale(1f, scaleY, t.x, t.y)
            chart.onChartGestureListener.onChartScale(event, 1f, scaleY)
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
    return closestDataSetToTouch == null && chart.isAnyAxisInverted ||
        (closestDataSetToTouch != null && chart.isInverted(closestDataSetToTouch!!.axisDependency))
  }

  /**
   * Sets the minimum distance that will be interpreted as a "drag" by the chart in dp. Default: 3dp
   *
   * @param dragTriggerDistance
   */
  fun setDragTriggerDist(dragTriggerDistance: Float) {
    dragTriggerDist = Utils.convertDpToPixel(dragTriggerDistance)
  }

  override fun onDoubleTap(e: MotionEvent): Boolean {
    lastGesture = ChartGesture.DOUBLE_TAP
    chart.onChartGestureListener.onChartDoubleTapped(e)

    val chartData = chart.data ?: return super.onDoubleTap(e)

    // check if double-tap zooming is enabled
    if (chart.isDoubleTapToZoomEnabled && chartData.entryCount > 0) {
      val trans = getTrans(e.x, e.y)
      val scaleX = if (chart.isScaleXEnabled) 1.4f else 1f
      val scaleY = if (chart.isScaleYEnabled) 1.4f else 1f
      chart.zoom(scaleX, scaleY, trans.x, trans.y)
      if (chart.isLogEnabled)
          Log.i(TAG, "Double-Tap, Zooming In, x: " + trans.x + ", y: " + trans.y)
      chart.onChartGestureListener.onChartScale(e, scaleX, scaleY)
      MPPointF.recycleInstance(trans)
    }
    return super.onDoubleTap(e)
  }

  override fun onLongPress(e: MotionEvent) {
    lastGesture = ChartGesture.LONG_PRESS
    chart.onChartGestureListener.onChartLongPressed(e)
  }

  override fun onSingleTapUp(e: MotionEvent): Boolean {
    lastGesture = ChartGesture.SINGLE_TAP
    chart.onChartGestureListener.onChartSingleTapped(e)
    if (!chart.isHighlightPerTapEnabled()) {
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
    chart.onChartGestureListener.onChartFling(e1, e2, velocityX, velocityY)
    return super.onFling(e1, e2, velocityX, velocityY)
  }

  private fun stopDeceleration() {
    decelerationVelocity.x = 0f
    decelerationVelocity.y = 0f
  }

  fun computeScroll() {
    if (decelerationVelocity.x == 0f && decelerationVelocity.y == 0f)
        return // There's no deceleration in progress
    val currentTime = AnimationUtils.currentAnimationTimeMillis()
    decelerationVelocity.x *= chart.dragDecelerationFrictionCoef
    decelerationVelocity.y *= chart.dragDecelerationFrictionCoef
    val timeInterval = (currentTime - decelerationLastTime).toFloat() / 1000f
    val distanceX = decelerationVelocity.x * timeInterval
    val distanceY = decelerationVelocity.y * timeInterval
    decelerationCurrentPoint.x += distanceX
    decelerationCurrentPoint.y += distanceY
    val event =
        MotionEvent.obtain(
            currentTime,
            currentTime,
            MotionEvent.ACTION_MOVE,
            decelerationCurrentPoint.x,
            decelerationCurrentPoint.y,
            0)
    val dragDistanceX =
        if (chart.isDragXEnabled) decelerationCurrentPoint.x - touchStartPoint.x else 0f
    val dragDistanceY =
        if (chart.isDragYEnabled) decelerationCurrentPoint.y - touchStartPoint.y else 0f
    performDrag(event, dragDistanceX, dragDistanceY)
    event.recycle()
    val viewport = chart.viewPortHandler
    matrix = viewport.refresh(matrix, chart, false)
    decelerationLastTime = currentTime
    if (abs(decelerationVelocity.x) >= 0.01 || abs(decelerationVelocity.y) >= 0.01)
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
}
