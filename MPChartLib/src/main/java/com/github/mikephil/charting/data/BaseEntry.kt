package com.github.mikephil.charting.data

import android.graphics.drawable.Drawable

/** Created by Philipp Jahoda on 02/06/16. */
abstract class BaseEntry(
    /** the y value */
    open var y: Float = 0f,
    /** optional icon image */
    open var icon: Drawable? = null,
    /** optional spot for additional data this Entry represents */
    open var data: Any? = null,
)
