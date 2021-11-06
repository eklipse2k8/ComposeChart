package com.github.mikephil.charting.data

import android.graphics.drawable.Drawable

/**
 * Subclass of Entry that holds a value for one entry in a BubbleChart. Bubble chart implementation:
 * Copyright 2015 Pierre-Marc Airoldi Licensed under Apache License 2.0
 *
 * @param x The value on the x-axis.
 * @param y The value on the y-axis.
 * @param size The size of the bubble.
 * @param icon Icon image
 * @param data Spot for additional data this Entry represents.
 * @author Philipp Jahoda
 */
class BubbleEntry
@JvmOverloads
constructor(
    x: Float = Float.NaN,
    y: Float = Float.NaN,
    val size: Float = 0f,
    icon: Drawable? = null,
    data: Any? = null
) : Entry(x, y, icon, data) {

  override fun copy(): BubbleEntry {
    return BubbleEntry(x, y, size, icon, data)
  }
}
