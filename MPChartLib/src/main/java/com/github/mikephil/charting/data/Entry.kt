package com.github.mikephil.charting.data

import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.ParcelFormatException
import android.os.Parcelable
import com.github.mikephil.charting.utils.Utils
import kotlin.math.abs

/**
 * Class representing one entry in the chart. Might contain multiple values. Might only contain a
 * single value depending on the used constructor.
 *
 * @author Philipp Jahoda
 */
open class Entry : BaseEntry, Parcelable {

  /** the x value */
  open var x = 0f

  constructor()

  /**
   * A Entry represents one single entry in the chart.
   *
   * @param x the x value
   * @param y the y value (the actual value of the entry)
   */
  constructor(x: Float, y: Float) : super(y) {
    this.x = x
  }

  /**
   * A Entry represents one single entry in the chart.
   *
   * @param x the x value
   * @param y the y value (the actual value of the entry)
   * @param data Spot for additional data this Entry represents.
   */
  constructor(x: Float, y: Float, data: Any?) : super(y, data) {
    this.x = x
  }

  /**
   * A Entry represents one single entry in the chart.
   *
   * @param x the x value
   * @param y the y value (the actual value of the entry)
   * @param icon icon image
   */
  constructor(x: Float, y: Float, icon: Drawable?) : super(y, icon) {
    this.x = x
  }

  /**
   * A Entry represents one single entry in the chart.
   *
   * @param x the x value
   * @param y the y value (the actual value of the entry)
   * @param icon icon image
   * @param data Spot for additional data this Entry represents.
   */
  constructor(x: Float, y: Float, icon: Drawable?, data: Any?) : super(y, icon, data) {
    this.x = x
  }

  /**
   * returns an exact copy of the entry
   *
   * @return
   */
  open fun copy(): Entry? {
    return Entry(x, y, data)
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

  override fun describeContents(): Int {
    return 0
  }

  override fun writeToParcel(dest: Parcel, flags: Int) {
    dest.writeFloat(x)
    dest.writeFloat(y)
    if (data != null) {
      if (data is Parcelable) {
        dest.writeInt(1)
        dest.writeParcelable(data as Parcelable?, flags)
      } else {
        throw ParcelFormatException("Cannot parcel an Entry with non-parcelable data")
      }
    } else {
      dest.writeInt(0)
    }
  }

  protected constructor(`in`: Parcel) {
    x = `in`.readFloat()
    y = `in`.readFloat()
    if (`in`.readInt() == 1) {
      data = `in`.readParcelable(Any::class.java.classLoader)
    }
  }

  companion object CREATOR : Parcelable.Creator<Entry> {
    override fun createFromParcel(parcel: Parcel): Entry {
      return Entry(parcel)
    }

    override fun newArray(size: Int): Array<Entry?> {
      return arrayOfNulls(size)
    }
  }
}
