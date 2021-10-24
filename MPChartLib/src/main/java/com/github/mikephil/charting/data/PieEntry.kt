package com.github.mikephil.charting.data

import android.graphics.drawable.Drawable
import android.util.Log

/** @author Philipp Jahoda */
class PieEntry(var label: String? = null, value: Float, icon: Drawable?, data: Any?) :
    Entry(x = 0f, y = value, icon = icon, data = data) {

  /**
   * This is the same as getY(). Returns the value of the PieEntry.
   */
  val value: Float
    get() = y

  @get:Deprecated("")
  @set:Deprecated("")
  override var x: Float
    get() {
      Log.i("DEPRECATED", "Pie entries do not have x values")
      return super.x
    }
    set(x) {
      super.x = x
      Log.i("DEPRECATED", "Pie entries do not have x values")
    }

  override fun copy(): PieEntry {
    return PieEntry(y, label, data)
  }
}
