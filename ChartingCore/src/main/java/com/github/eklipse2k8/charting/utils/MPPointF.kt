package com.github.eklipse2k8.charting.utils

import android.os.Parcelable
import com.github.eklipse2k8.charting.utils.ObjectPool.Poolable
import kotlinx.parcelize.Parcelize

/** Created by Tony Patino on 6/24/16. */
@Parcelize
class MPPointF(
    var x: Float = 0f,
    var y: Float = 0f,
) : Poolable(), Parcelable {

  companion object {
    private val pool: ObjectPool<MPPointF> =
        (ObjectPool.create(32, MPPointF(0f, 0f)) as ObjectPool<MPPointF>).apply {
          replenishPercentage = 0.5f
        }

    @JvmStatic
    fun getInstance(x: Float, y: Float): MPPointF =
        pool.get().apply {
          this.x = x
          this.y = y
        }

    @JvmStatic
    val instance: MPPointF
      get() = pool.get()

    @JvmStatic
    fun getInstance(other: MPPointF): MPPointF =
        pool.get().apply {
          this.x = other.x
          this.y = other.y
        }

    @JvmStatic
    fun recycleInstance(instance: MPPointF) {
      pool.recycle(instance)
    }

    @JvmStatic
    fun recycleInstances(instances: List<MPPointF>) {
      pool.recycle(instances)
    }
  }

  override fun instantiate(): Poolable {
    return MPPointF(0f, 0f)
  }
}
