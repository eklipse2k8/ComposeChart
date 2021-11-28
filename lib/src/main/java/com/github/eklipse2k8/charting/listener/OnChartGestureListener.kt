package com.github.eklipse2k8.charting.listener

import android.view.MotionEvent
import com.github.eklipse2k8.charting.listener.ChartTouchListener.ChartGesture

/**
 * Listener for callbacks when doing gestures on the chart.
 *
 * @author Philipp Jahoda
 */
interface OnChartGestureListener {
  /**
   * Callbacks when a touch-gesture has started on the chart (ACTION_DOWN)
   *
   * @param me
   * @param lastPerformedGesture
   */
  fun onChartGestureStart(me: MotionEvent?, lastPerformedGesture: ChartGesture?) = Unit

  /**
   * Callbacks when a touch-gesture has ended on the chart (ACTION_UP, ACTION_CANCEL)
   *
   * @param me
   * @param lastPerformedGesture
   */
  fun onChartGestureEnd(me: MotionEvent?, lastPerformedGesture: ChartGesture?) = Unit

  /**
   * Callbacks when the chart is longpressed.
   *
   * @param me
   */
  fun onChartLongPressed(me: MotionEvent?) = Unit

  /**
   * Callbacks when the chart is double-tapped.
   *
   * @param me
   */
  fun onChartDoubleTapped(me: MotionEvent?) = Unit

  /**
   * Callbacks when the chart is single-tapped.
   *
   * @param me
   */
  fun onChartSingleTapped(me: MotionEvent?) = Unit

  /**
   * Callbacks then a fling gesture is made on the chart.
   *
   * @param me1
   * @param me2
   * @param velocityX
   * @param velocityY
   */
  fun onChartFling(me1: MotionEvent?, me2: MotionEvent?, velocityX: Float, velocityY: Float) = Unit

  /**
   * Callbacks when the chart is scaled / zoomed via pinch zoom / double-tap gesture.
   *
   * @param me
   * @param scaleX scalefactor on the x-axis
   * @param scaleY scalefactor on the y-axis
   */
  fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) = Unit

  /**
   * Callbacks when the chart is moved / translated via drag gesture.
   *
   * @param me
   * @param dX translation distance on the x-axis
   * @param dY translation distance on the y-axis
   */
  fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) = Unit
}
