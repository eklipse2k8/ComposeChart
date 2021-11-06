package com.github.mikephil.charting.listener

import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View.OnTouchListener
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.highlight.Highlight
import kotlin.math.sqrt

/** Created by philipp on 12/06/15. */
abstract class ChartTouchListener<T : Chart<*,*,*>>(
    /** the chart the listener represents */
    protected val chart: T
) : SimpleOnGestureListener(), OnTouchListener {
  enum class ChartGesture {
    NONE,
    DRAG,
    X_ZOOM,
    Y_ZOOM,
    PINCH_ZOOM,
    ROTATE,
    SINGLE_TAP,
    DOUBLE_TAP,
    LONG_PRESS,
    FLING
  }
  /**
   * Returns the last gesture that has been performed on the chart.
   *
   * @return
   */
  /** the last touch gesture that has been performed */
  var lastGesture = ChartGesture.NONE
    protected set
  /**
   * returns the touch mode the listener is currently in
   *
   * @return
   */
  /** integer field that holds the current touch-state */
  var touchMode = NONE
    protected set

  /** the last highlighted object (via touch) */
  var lastHighlighted: Highlight? = null

  /** the gesturedetector used for detecting taps and longpresses, ... */
  protected var gestureDetector: GestureDetector = GestureDetector(chart.context, this)

  /**
   * Calls the OnChartGestureListener to do the start callback
   *
   * @param me
   */
  fun startAction(me: MotionEvent?) {
    val l = chart.getOnChartGestureListener()
    l?.onChartGestureStart(me, lastGesture)
  }

  /**
   * Calls the OnChartGestureListener to do the end callback
   *
   * @param me
   */
  fun endAction(me: MotionEvent?) {
    val l = chart.getOnChartGestureListener()
    l?.onChartGestureEnd(me, lastGesture)
  }

  /**
   * Perform a highlight operation.
   *
   * @param e
   */
  protected fun performHighlight(h: Highlight?, e: MotionEvent?) {
    lastHighlighted =
        if (h == null || h.equalTo(lastHighlighted)) {
          chart.highlightValue(null, true)
          null
        } else {
          chart.highlightValue(h, true)
          h
        }
  }

  companion object {
    // states
    @JvmStatic protected val NONE = 0
    @JvmStatic protected val DRAG = 1
    @JvmStatic protected val X_ZOOM = 2
    @JvmStatic protected val Y_ZOOM = 3
    @JvmStatic protected val PINCH_ZOOM = 4
    @JvmStatic protected val POST_ZOOM = 5
    @JvmStatic protected val ROTATE = 6

    /**
     * returns the distance between two points
     *
     * @param eventX
     * @param startX
     * @param eventY
     * @param startY
     * @return
     */
    @JvmStatic
    protected fun distance(eventX: Float, startX: Float, eventY: Float, startY: Float): Float {
      val dx = eventX - startX
      val dy = eventY - startY
      return sqrt((dx * dx + dy * dy).toDouble()).toFloat()
    }
  }
}
