package com.github.eklipse2k8.charting.jobs

import android.animation.ValueAnimator
import android.view.View
import com.github.eklipse2k8.charting.utils.ObjectPool
import com.github.eklipse2k8.charting.utils.ObjectPool.Poolable
import com.github.eklipse2k8.charting.utils.Transformer
import com.github.eklipse2k8.charting.utils.ViewPortHandler

/** Created by Philipp Jahoda on 19/02/16. */
class AnimatedMoveViewJob(
    viewPortHandler: ViewPortHandler?,
    xValue: Float,
    yValue: Float,
    trans: Transformer?,
    v: View?,
    xOrigin: Float,
    yOrigin: Float,
    duration: Long
) : AnimatedViewPortJob(viewPortHandler, xValue, yValue, trans, v, xOrigin, yOrigin, duration) {

  companion object {
    private var pool: ObjectPool<AnimatedMoveViewJob>? = null
    fun getInstance(
        viewPortHandler: ViewPortHandler?,
        xValue: Float,
        yValue: Float,
        trans: Transformer?,
        v: View?,
        xOrigin: Float,
        yOrigin: Float,
        duration: Long
    ): AnimatedMoveViewJob? {
      val result = pool?.get() ?: return null
      result.viewPortHandler = viewPortHandler
      result.xValue = xValue
      result.yValue = yValue
      result.transformer = trans
      result.view = v
      result.xOrigin = xOrigin
      result.yOrigin = yOrigin
      result.animator.duration = duration
      return result
    }

    fun recycleInstance(instance: AnimatedMoveViewJob) {
      pool?.recycle(instance)
    }

    init {
      pool =
          ObjectPool.create(4, AnimatedMoveViewJob(null, 0f, 0f, null, null, 0f, 0f, 0L)) as
              ObjectPool<AnimatedMoveViewJob>?
      pool?.replenishPercentage = 0.5f
    }
  }

  override fun onAnimationUpdate(animation: ValueAnimator) {
    pts[0] = xOrigin + (xValue - xOrigin) * phase
    pts[1] = yOrigin + (yValue - yOrigin) * phase
    transformer?.pointValuesToPixel(pts)
    view?.let { viewPortHandler?.centerViewPort(pts, it) }
  }

  override fun recycleSelf() {
    recycleInstance(this)
  }

  override fun instantiate(): Poolable {
    return AnimatedMoveViewJob(null, 0f, 0f, null, null, 0f, 0f, 0)
  }
}
