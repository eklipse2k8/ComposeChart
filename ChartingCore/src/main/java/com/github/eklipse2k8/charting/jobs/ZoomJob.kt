package com.github.eklipse2k8.charting.jobs

import android.graphics.Matrix
import android.view.View
import com.github.eklipse2k8.charting.charts.BarLineChartBase
import com.github.eklipse2k8.charting.components.YAxis.AxisDependency
import com.github.eklipse2k8.charting.utils.ObjectPool
import com.github.eklipse2k8.charting.utils.ObjectPool.Poolable
import com.github.eklipse2k8.charting.utils.Transformer
import com.github.eklipse2k8.charting.utils.ViewPortHandler

/** Created by Philipp Jahoda on 19/02/16. */
class ZoomJob(
    viewPortHandler: ViewPortHandler?,
    private var scaleX: Float,
    private var scaleY: Float,
    xValue: Float,
    yValue: Float,
    trans: Transformer?,
    private var axisDependency: AxisDependency?,
    v: View?
) : ViewPortJob(viewPortHandler, xValue, yValue, trans, v) {
  companion object {
    private var pool: ObjectPool<ZoomJob>? = null
    fun getInstance(
        viewPortHandler: ViewPortHandler?,
        scaleX: Float,
        scaleY: Float,
        xValue: Float,
        yValue: Float,
        trans: Transformer?,
        axis: AxisDependency?,
        v: View?
    ): ZoomJob? {
      val result = pool?.get() ?: return null
      result.xValue = xValue
      result.yValue = yValue
      result.scaleX = scaleX
      result.scaleY = scaleY
      result.viewPortHandler = viewPortHandler
      result.transformer = trans
      result.axisDependency = axis
      result.view = v
      return result
    }

    fun recycleInstance(instance: ZoomJob) {
      pool?.recycle(instance)
    }

    init {
      pool =
          ObjectPool.create(1, ZoomJob(null, 0f, 0f, 0f, 0f, null, null, null)) as
              ObjectPool<ZoomJob>?
      pool?.replenishPercentage = 0.5f
    }
  }

  private var mRunMatrixBuffer = Matrix()
  override fun run() {
    val save = mRunMatrixBuffer
    if (viewPortHandler == null) {
      return
    }
    viewPortHandler!!.zoom(scaleX, scaleY, save)
    viewPortHandler!!.refresh(save, view, false)
    val yValsInView =
        (view as BarLineChartBase<*, *, *>).getAxis(axisDependency!!).mAxisRange / viewPortHandler!!.scaleY
    val xValsInView = (view as BarLineChartBase<*, *, *>).xAxis.mAxisRange / viewPortHandler!!.scaleX
    pts[0] = xValue - xValsInView / 2f
    pts[1] = yValue + yValsInView / 2f
    transformer?.pointValuesToPixel(pts)
    viewPortHandler!!.translate(pts, save)
    viewPortHandler!!.refresh(save, view, false)
    (view as BarLineChartBase<*, *, *>).calculateOffsets()
    view?.postInvalidate()
    recycleInstance(this)
  }

  override fun instantiate(): Poolable {
    return ZoomJob(null, 0f, 0f, 0f, 0f, null, null, null)
  }
}
