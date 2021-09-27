package com.github.mikephil.charting.utils

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MPPointD(
    @JvmField var x: Double = 0.0,
    @JvmField var y: Double = 0.0,
) : Parcelable
