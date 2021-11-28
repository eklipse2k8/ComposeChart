package com.github.eklipse2k8.charting.data

import android.graphics.drawable.Drawable
import android.os.Parcelable
import com.github.eklipse2k8.charting.data.parceler.DrawableParceler
import com.github.eklipse2k8.charting.data.parceler.SafeAnyParceler
import com.github.eklipse2k8.charting.utils.Utils
import kotlin.math.abs
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith

/**
 * Class representing one entry in the chart. Might contain multiple values. Might only contain a
 * single value depending on the used constructor.
 *
 * @param x the x value
 * @param y the y value (the actual value of the entry)
 * @param icon icon image
 * @param data Spot for additional data this Entry represents.
 */
@Parcelize
open class Entry
@JvmOverloads
constructor(
    open var x: Float = Float.NaN,
    override val y: Float = Float.NaN,
    override val icon: @WriteWith<DrawableParceler>() Drawable? = null,
    override val data: @WriteWith<SafeAnyParceler>() Any? = null,
) : BaseEntry(y, icon, data), Parcelable {

  /**
   * returns an exact copy of the entry
   *
   * @return
   */
  open fun copy(): Entry? {
    return Entry(x, y, icon, data)
  }

  /**
   * Compares value, xIndex and data of the entries. Returns true if entries are equal in those
   * points, false if not. Does not check by hash-code like it's done by the "equals" method.
   *
   * @param e
   * @return
   */
  fun equalTo(e: Entry?): Boolean {
    if (e == null) return false
    if (e.data !== data) return false
    if (abs(e.x - x) > Utils.FLOAT_EPSILON) return false
    return abs(e.y - y) <= Utils.FLOAT_EPSILON
  }

  /** returns a string representation of the entry containing x-index and value */
  override fun toString(): String {
    return "Entry, x: $x y: $y"
  }
}
