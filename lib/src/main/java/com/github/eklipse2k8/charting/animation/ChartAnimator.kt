package com.github.eklipse2k8.charting.animation

import android.animation.ObjectAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import com.github.eklipse2k8.charting.animation.Easing.EasingFunction

private const val PHASE_MIN = 0f
private const val PHASE_MAX = 1f

/**
 * Object responsible for all animations in the Chart. Animations require API level 11.
 *
 * @author Philipp Jahoda
 * @author Mick Ashton
 */
class ChartAnimator(private val listener: AnimatorUpdateListener? = null) {

  private fun xAnimator(duration: Int, easing: EasingFunction): ObjectAnimator {
    val animatorX = ObjectAnimator.ofFloat(this, "phaseX", PHASE_MIN, PHASE_MAX)
    animatorX.interpolator = easing
    animatorX.duration = duration.toLong()
    return animatorX
  }

  private fun yAnimator(duration: Int, easing: EasingFunction): ObjectAnimator {
    val animatorY = ObjectAnimator.ofFloat(this, "phaseY", PHASE_MIN, PHASE_MAX)
    animatorY.interpolator = easing
    animatorY.duration = duration.toLong()
    return animatorY
  }

  /**
   * Animates values along the X axis, in a linear fashion.
   *
   * @param durationMillis animation duration
   */
  fun animateX(durationMillis: Int) {
    animateX(durationMillis, Easing.Linear)
  }

  /**
   * Animates values along the X axis.
   *
   * @param durationMillis animation duration
   * @param easing EasingFunction
   */
  fun animateX(durationMillis: Int, easing: EasingFunction) {
    val animatorX = xAnimator(durationMillis, easing)
    animatorX.addUpdateListener(listener)
    animatorX.start()
  }

  /**
   * Animates values along both the X and Y axes, in a linear fashion.
   *
   * @param durationMillisX animation duration along the X axis
   * @param durationMillisY animation duration along the Y axis
   */
  fun animateXY(durationMillisX: Int, durationMillisY: Int) {
    animateXY(durationMillisX, durationMillisY, Easing.Linear, Easing.Linear)
  }

  /**
   * Animates values along both the X and Y axes.
   *
   * @param durationMillisX animation duration along the X axis
   * @param durationMillisY animation duration along the Y axis
   * @param easing EasingFunction for both axes
   */
  fun animateXY(durationMillisX: Int, durationMillisY: Int, easing: EasingFunction) {
    val xAnimator = xAnimator(durationMillisX, easing)
    val yAnimator = yAnimator(durationMillisY, easing)
    if (durationMillisX > durationMillisY) {
      xAnimator.addUpdateListener(listener)
    } else {
      yAnimator.addUpdateListener(listener)
    }
    xAnimator.start()
    yAnimator.start()
  }

  /**
   * Animates values along both the X and Y axes.
   *
   * @param durationMillisX animation duration along the X axis
   * @param durationMillisY animation duration along the Y axis
   * @param easingX EasingFunction for the X axis
   * @param easingY EasingFunction for the Y axis
   */
  fun animateXY(
      durationMillisX: Int,
      durationMillisY: Int,
      easingX: EasingFunction,
      easingY: EasingFunction
  ) {
    val xAnimator = xAnimator(durationMillisX, easingX)
    val yAnimator = yAnimator(durationMillisY, easingY)
    if (durationMillisX > durationMillisY) {
      xAnimator.addUpdateListener(listener)
    } else {
      yAnimator.addUpdateListener(listener)
    }
    xAnimator.start()
    yAnimator.start()
  }

  /**
   * Animates values along the Y axis, in a linear fashion.
   *
   * @param durationMillis animation duration
   */
  fun animateY(durationMillis: Int) {
    animateY(durationMillis, Easing.Linear)
  }

  /**
   * Animates values along the Y axis.
   *
   * @param durationMillis animation duration
   * @param easing EasingFunction
   */
  fun animateY(durationMillis: Int, easing: EasingFunction) {
    val animatorY = yAnimator(durationMillis, easing)
    animatorY.addUpdateListener(listener)
    animatorY.start()
  }

  /**
   * Sets the Y axis phase of the animation.
   *
   * float value between 0 - 1
   */
  var phaseY: Float = PHASE_MAX
    set(value) {
      field = value.coerceIn(PHASE_MIN, PHASE_MAX)
    }

  /**
   * Sets the X axis phase of the animation.
   *
   * float value between 0 - 1
   */
  var phaseX: Float = PHASE_MAX
    set(value) {
      field = value.coerceIn(PHASE_MIN, PHASE_MAX)
    }
}
