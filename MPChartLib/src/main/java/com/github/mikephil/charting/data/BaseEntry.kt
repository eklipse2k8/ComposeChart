package com.github.mikephil.charting.data

import android.graphics.drawable.Drawable

/** Created by Philipp Jahoda on 02/06/16. */
abstract class BaseEntry
@JvmOverloads
constructor(
    /** the y value */
    open val y: Float = 0f,
    /** optional icon image */
    open val icon: Drawable? = null,
    /** optional spot for additional data this Entry represents */
    open val data: Any? = null,
)
