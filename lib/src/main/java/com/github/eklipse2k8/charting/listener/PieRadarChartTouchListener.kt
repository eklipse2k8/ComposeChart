package com.github.eklipse2k8.charting.listener

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import com.github.eklipse2k8.charting.charts.PieRadarChartBase
import com.github.eklipse2k8.charting.utils.MPPointF
import com.github.eklipse2k8.charting.utils.Utils
import java.util.ArrayList
import kotlin.math.abs

/**
 * Touchlistener for the PieChart.
 *
 * @author Philipp Jahoda
 */
class PieRadarChartTouchListener(chart: PieRadarChartBase<*, *, *>) :
    ChartTouchListener<PieRadarChartBase<*, *, *>>(chart) {
  private val mTouchStartPoint = MPPointF.getInstance(0f, 0f)

  /** the angle where the dragging started */
  private var mStartAngle = 0f
  private val _velocitySamples = ArrayList<AngularVelocitySample>()
  private var mDecelerationLastTime: Long = 0
  private var mDecelerationAngularVelocity = 0f

  @SuppressLint("ClickableViewAccessibility")
  override fun onTouch(v: View, event: MotionEvent): Boolean {
    if (gestureDetector.onTouchEvent(event)) return true

    // if rotation by touch is enabled
    // TODO: Also check if the pie itself is being touched, rather than the entire chart area
    if (chart.isRotationEnabled) {
      val x = event.x
      val y = event.y
      when (event.action) {
        MotionEvent.ACTION_DOWN -> {
          startAction(event)
          stopDeceleration()
          resetVelocity()
          if (chart.isDragDecelerationEnabled()) sampleVelocity(x, y)
          setGestureStartAngle(x, y)
          mTouchStartPoint.x = x
          mTouchStartPoint.y = y
        }
        MotionEvent.ACTION_MOVE -> {
          if (chart.isDragDecelerationEnabled()) sampleVelocity(x, y)
          if (touchMode == NONE &&
              distance(x, mTouchStartPoint.x, y, mTouchStartPoint.y) > Utils.convertDpToPixel(8f)) {
            lastGesture = ChartGesture.ROTATE
            touchMode = ROTATE
            chart.disableScroll()
          } else if (touchMode == ROTATE) {
            updateGestureRotation(x, y)
            chart.invalidate()
          }
          endAction(event)
        }
        MotionEvent.ACTION_UP -> {
          if (chart.isDragDecelerationEnabled()) {
            stopDeceleration()
            sampleVelocity(x, y)
            mDecelerationAngularVelocity = calculateVelocity()
            if (mDecelerationAngularVelocity != 0f) {
              mDecelerationLastTime = AnimationUtils.currentAnimationTimeMillis()
              Utils.postInvalidateOnAnimation(
                  chart) // This causes computeScroll to fire, recommended for this by Google
            }
          }
          chart.enableScroll()
          touchMode = NONE
          endAction(event)
        }
      }
    }
    return true
  }

  override fun onLongPress(me: MotionEvent) {
    lastGesture = ChartGesture.LONG_PRESS
    chart.onChartGestureListener.onChartLongPressed(me)
  }

  override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
    return true
  }

  override fun onSingleTapUp(e: MotionEvent): Boolean {
    lastGesture = ChartGesture.SINGLE_TAP
    chart.onChartGestureListener.onChartSingleTapped(e)
    if (!chart.isHighlightPerTapEnabled()) {
      return false
    }
    val high = chart.getHighlightByTouchPoint(e.x, e.y)
    performHighlight(high, e)
    return true
  }

  private fun resetVelocity() {
    _velocitySamples.clear()
  }

  private fun sampleVelocity(touchLocationX: Float, touchLocationY: Float) {
    val currentTime = AnimationUtils.currentAnimationTimeMillis()
    _velocitySamples.add(
        AngularVelocitySample(currentTime, chart.getAngleForPoint(touchLocationX, touchLocationY)))

    // Remove samples older than our sample time - 1 seconds
    var i = 0
    var count = _velocitySamples.size
    while (i < count - 2) {
      if (currentTime - _velocitySamples[i].time > 1000) {
        _velocitySamples.removeAt(0)
        i--
        count--
      } else {
        break
      }
      i++
    }
  }

  private fun calculateVelocity(): Float {
    if (_velocitySamples.isEmpty()) return 0f
    val firstSample = _velocitySamples[0]
    val lastSample = _velocitySamples[_velocitySamples.size - 1]

    // Look for a sample that's closest to the latest sample, but not the same, so we can deduce the
    // direction
    var beforeLastSample = firstSample
    for (i in _velocitySamples.indices.reversed()) {
      beforeLastSample = _velocitySamples[i]
      if (beforeLastSample.angle != lastSample.angle) {
        break
      }
    }

    // Calculate the sampling time
    var timeDelta = (lastSample.time - firstSample.time) / 1000f
    if (timeDelta == 0f) {
      timeDelta = 0.1f
    }

    // Calculate clockwise/ccw by choosing two values that should be closest to each other,
    // so if the angles are two far from each other we know they are inverted "for sure"
    var clockwise = lastSample.angle >= beforeLastSample.angle
    if (abs(lastSample.angle - beforeLastSample.angle) > 270.0) {
      clockwise = !clockwise
    }

    // Now if the "gesture" is over a too big of an angle - then we know the angles are inverted,
    // and we need to move them closer to each other from both sides of the 360.0 wrapping point
    if (lastSample.angle - firstSample.angle > 180.0) {
      firstSample.angle += 360.0f
    } else if (firstSample.angle - lastSample.angle > 180.0) {
      lastSample.angle += 360.0f
    }

    // The velocity
    var velocity = abs((lastSample.angle - firstSample.angle) / timeDelta)

    // Direction?
    if (!clockwise) {
      velocity = -velocity
    }
    return velocity
  }

  /**
   * sets the starting angle of the rotation, this is only used by the touch listener, x and y is
   * the touch position
   *
   * @param x
   * @param y
   */
  private fun setGestureStartAngle(x: Float, y: Float) {
    mStartAngle = chart.getAngleForPoint(x, y) - chart.rawRotationAngle
  }

  /**
   * updates the view rotation depending on the given touch position, also takes the starting angle
   * into consideration
   *
   * @param x
   * @param y
   */
  private fun updateGestureRotation(x: Float, y: Float) {
    chart.rotationAngle = chart.getAngleForPoint(x, y) - mStartAngle
  }

  /** Sets the deceleration-angular-velocity to 0f */
  private fun stopDeceleration() {
    mDecelerationAngularVelocity = 0f
  }

  fun computeScroll() {
    if (mDecelerationAngularVelocity == 0f) return // There's no deceleration in progress
    val currentTime = AnimationUtils.currentAnimationTimeMillis()
    mDecelerationAngularVelocity *= chart.dragDecelerationFrictionCoef
    val timeInterval = (currentTime - mDecelerationLastTime).toFloat() / 1000f
    chart.rotationAngle = chart.rotationAngle + mDecelerationAngularVelocity * timeInterval
    mDecelerationLastTime = currentTime
    if (abs(mDecelerationAngularVelocity) >= 0.001)
        Utils.postInvalidateOnAnimation(
            chart) // This causes computeScroll to fire, recommended for this by Google
    else stopDeceleration()
  }

  private inner class AngularVelocitySample(var time: Long, var angle: Float)
}
