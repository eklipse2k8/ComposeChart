package com.github.eklipse2k8.charting.utils

import android.os.Parcelable
import com.github.eklipse2k8.charting.utils.ObjectPool.Poolable
import kotlinx.parcelize.Parcelize

/**
 * Class for describing width and height dimensions in some arbitrary unit. Replacement for the
 * android.Util.SizeF which is available only on API >= 21.
 */
@Parcelize
class FSize
@JvmOverloads
constructor(
    var width: Float = 0f,
    var height: Float = 0f,
) : Poolable(), Parcelable {
  // TODO : Encapsulate width & height

  companion object {
    private val pool: ObjectPool<FSize> =
        (ObjectPool.create(256, FSize(0f, 0f)) as ObjectPool<FSize>).apply {
          replenishPercentage = 0.5f
        }

    @JvmStatic
    fun getInstance(width: Float, height: Float): FSize {
      val result = pool.get()
      result.width = width
      result.height = height
      return result
    }

    @JvmStatic
    fun recycleInstance(instance: FSize) {
      pool.recycle(instance)
    }

    @JvmStatic
    fun recycleInstances(instances: List<FSize>) {
      pool.recycle(instances)
    }
  }

  override fun instantiate(): Poolable {
    return FSize(0f, 0f)
  }

  override fun equals(obj: Any?): Boolean {
    if (obj == null) {
      return false
    }
    if (this === obj) {
      return true
    }
    if (obj is FSize) {
      return width == obj.width && height == obj.height
    }
    return false
  }

  override fun toString(): String {
    return width.toString() + "x" + height
  }

  /** {@inheritDoc} */
  override fun hashCode(): Int = width.toBits() xor height.toBits()
}
