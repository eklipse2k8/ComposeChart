package com.github.eklipse2k8.charting.utils

import android.os.Parcelable
import com.github.eklipse2k8.charting.utils.ObjectPool.Poolable
import kotlinx.parcelize.Parcelize

/**
 * Point encapsulating two double values.
 *
 * @author Philipp Jahoda
 */
@Parcelize
class MPPointD private constructor(var x: Double, var y: Double) : Poolable(), Parcelable {
  companion object {
    private val pool: ObjectPool<MPPointD> =
        (ObjectPool.create(64, MPPointD(0.0, 0.0)) as ObjectPool<MPPointD>).apply {
          replenishPercentage = 0.5f
        }

    @JvmStatic
    fun getInstance(x: Double, y: Double): MPPointD {
      val result = pool.get()
      result.x = x
      result.y = y
      return result
    }

    @JvmStatic
    fun recycleInstance(instance: MPPointD) {
      pool.recycle(instance)
    }

    @JvmStatic
    fun recycleInstances(instances: List<MPPointD>) {
      pool.recycle(instances)
    }
  }

  override fun instantiate(): Poolable {
    return MPPointD(0.0, 0.0)
  }

  /** returns a string representation of the object */
  override fun toString(): String {
    return "MPPointD, x: $x, y: $y"
  }
}
