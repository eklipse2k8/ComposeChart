package com.github.mikephil.charting.data

import com.github.mikephil.charting.data.Entry.y
import com.github.mikephil.charting.data.Entry.data
import com.github.mikephil.charting.data.Entry.x
import android.annotation.SuppressLint
import com.github.mikephil.charting.data.RadarEntry

/**
 * Created by philipp on 13/06/16.
 */
@SuppressLint("ParcelCreator")
class RadarEntry : Entry {
  constructor(value: Float) : super(0f, value) {}
  constructor(value: Float, data: Any?) : super(0f, value, data) {}

  /**
   * This is the same as getY(). Returns the value of the RadarEntry.
   *
   * @return
   */
  val value: Float
    get() = y

  override fun copy(): RadarEntry {
    return RadarEntry(y, data)
  }

  @get:Deprecated("")
  @set:Deprecated("")
  override var x: Float
    get() = super.x
    set(x) {
      super.x = x
    }
}