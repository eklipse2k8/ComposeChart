package com.github.mikephil.charting.data

import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.ParcelFormatException
import android.os.Parcelable
import com.github.mikephil.charting.utils.Utils.FLOAT_EPSILON
import kotlin.math.abs

/**
 * Class representing one entry in the chart. Might contain multiple values. Might only contain a
 * single value depending on the used constructor.
 *
 * @author Philipp Jahoda
 */
open class Entry(
    /** the x value */
    open var x: Float,
    /** the y value */
    override var y: Float,
    /** optional icon image */
    override var icon: Drawable? = null,
    /** optional spot for additional data this Entry represents */
    override var data: Any? = null,
) : BaseEntry(y = y, icon = icon, data = data), Parcelable {

  /**
   * returns an exact copy of the entry
   *
   * @return
   */
  open fun copy(): Entry {
    return Entry(x, y, data = data, icon = icon)
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
    if (e.data !== this.data) return false
    if (abs(e.x - x) > FLOAT_EPSILON) return false
    return abs(e.y - y) <= FLOAT_EPSILON
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
        dest.writeParcelable(this.data as Parcelable, flags)
      } else {
        throw ParcelFormatException("Cannot parcel an Entry with non-parcelable data")
      }
    } else {
      dest.writeInt(0)
    }
  }

  protected constructor(
      input: Parcel
  ) : this(
      x = input.readFloat(),
      y = input.readFloat(),
      data =
          if (input.readInt() == 1) {
            input.readParcelable(Any::class.java.classLoader)
          } else {
            null
          },
  )

  companion object CREATOR : Parcelable.Creator<Entry> {
    override fun createFromParcel(parcel: Parcel): Entry {
      return Entry(parcel)
    }

    override fun newArray(size: Int): Array<Entry?> {
      return arrayOfNulls(size)
    }
  }
}
