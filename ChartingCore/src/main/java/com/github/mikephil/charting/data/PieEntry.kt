package com.github.mikephil.charting.data

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable

/** @author Philipp Jahoda */
@SuppressLint("ParcelCreator")
class PieEntry
@JvmOverloads
constructor(
    value: Float = Float.NaN,
    val label: String? = null,
    icon: Drawable? = null,
    data: Any? = null
) : Entry(y = value, icon = icon, data = data) {

  override var x: Float
    get() = throw IllegalAccessError("unsupported parameter")
    set(value) = throw IllegalAccessError("unsupported parameter")

  override fun copy(): PieEntry {
    return PieEntry(y, label, icon, data)
  }
}
