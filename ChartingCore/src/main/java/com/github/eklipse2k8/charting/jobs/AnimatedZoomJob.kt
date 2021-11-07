package com.github.eklipse2k8.charting.jobs

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.Matrix
import android.view.View
import com.github.eklipse2k8.charting.charts.BarLineChartBase
import com.github.eklipse2k8.charting.components.YAxis
import com.github.eklipse2k8.charting.utils.ObjectPool
import com.github.eklipse2k8.charting.utils.ObjectPool.Poolable
import com.github.eklipse2k8.charting.utils.Transformer
import com.github.eklipse2k8.charting.utils.ViewPortHandler

/** Created by Philipp Jahoda on 19/02/16. */
@SuppressLint("NewApi")
class AnimatedZoomJob(
    viewPortHandler: ViewPortHandler?,
    v: View?,
    trans: Transformer?,
    axis: YAxis?,
    xAxisRange: Float,
    scaleX: Float,
    scaleY: Float,
    xOrigin: Float,
    yOrigin: Float,
    private var zoomCenterX: Float,
    private var zoomCenterY: Float,
    private var zoomOriginX: Float,
    private var zoomOriginY: Float,
    duration: Long
) :
    AnimatedViewPortJob(viewPortHandler, scaleX, scaleY, trans, v, xOrigin, yOrigin, duration),
    Animator.AnimatorListener {

  companion object {
    private var pool: ObjectPool<AnimatedZoomJob>? = null
    fun getInstance(
        viewPortHandler: ViewPortHandler?,
        v: View?,
        trans: Transformer?,
        axis: YAxis?,
        xAxisRange: Float,
        scaleX: Float,
        scaleY: Float,
        xOrigin: Float,
        yOrigin: Float,
        zoomCenterX: Float,
        zoomCenterY: Float,
        zoomOriginX: Float,
        zoomOriginY: Float,
        duration: Long
    ): AnimatedZoomJob? {
      val result = pool?.get() ?: return null
      result.mViewPortHandler = viewPortHandler
      result.xValue = scaleX
      result.yValue = scaleY
      result.mTrans = trans
      result.view = v
      result.xOrigin = xOrigin
      result.yOrigin = yOrigin
      result.yAxis = axis
      result.xAxisRange = xAxisRange
      result.resetAnimator()
      result.animator.duration = duration
      return result
    }

    init {
      pool =
          ObjectPool.create(
              8, AnimatedZoomJob(null, null, null, null, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0L)) as
              ObjectPool<AnimatedZoomJob>?
    }
  }

  private var yAxis: YAxis?

  private var xAxisRange: Float

  private var mOnAnimationUpdateMatrixBuffer = Matrix()

  override fun onAnimationUpdate(animation: ValueAnimator) {
    if (mViewPortHandler == null) {
      return
    }
    val scaleX = xOrigin + (xValue - xOrigin) * phase
    val scaleY = yOrigin + (yValue - yOrigin) * phase
    val save = mOnAnimationUpdateMatrixBuffer
    mViewPortHandler!!.setZoom(scaleX, scaleY, save)
    mViewPortHandler!!.refresh(save, view, false)
    val valsInView = (yAxis?.mAxisRange ?: 0f) / mViewPortHandler!!.scaleY
    val xsInView = xAxisRange / mViewPortHandler!!.scaleX
    pts[0] = zoomOriginX + (zoomCenterX - xsInView / 2f - zoomOriginX) * phase
    pts[1] = zoomOriginY + (zoomCenterY + valsInView / 2f - zoomOriginY) * phase
    mTrans?.pointValuesToPixel(pts)
    mViewPortHandler?.translate(pts, save)
    mViewPortHandler?.refresh(save, view, true)
  }

  override fun onAnimationEnd(animation: Animator) {
    if (view is BarLineChartBase<*, *, *>) {
      (view as BarLineChartBase<*, *, *>).calculateOffsets()
    }
    view?.postInvalidate()
  }

  override fun onAnimationCancel(animation: Animator) = Unit
  override fun onAnimationRepeat(animation: Animator) = Unit
  override fun recycleSelf() = Unit
  override fun onAnimationStart(animation: Animator) = Unit

  override fun instantiate(): Poolable {
    return AnimatedZoomJob(null, null, null, null, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0L)
  }

  init {
    animator.addListener(this)
    yAxis = axis
    this.xAxisRange = xAxisRange
  }
}
