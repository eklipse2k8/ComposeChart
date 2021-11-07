package com.github.mikephil.charting.data

import android.annotation.SuppressLint

/** Created by philipp on 13/06/16. */
@SuppressLint("ParcelCreator")
class RadarEntry @JvmOverloads constructor(value: Float = Float.NaN, data: Any? = null) :
    Entry(y = value, data = data) {

  override fun copy(): RadarEntry = RadarEntry(y, data)
}
