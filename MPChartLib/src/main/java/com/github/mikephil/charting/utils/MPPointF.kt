package com.github.mikephil.charting.utils

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MPPointF(
    @JvmField var x: Float = 0f,
    @JvmField var y: Float = 0f,
) : Parcelable
