package com.github.mikephil.charting.data

import android.graphics.drawable.Drawable

/** Created by Philipp Jahoda on 02/06/16. */
abstract class BaseEntry {

  /** the y value */
  open var y = 0f

  /** optional spot for additional data this Entry represents */
  var data: Any? = null

  /** optional icon image */
  var icon: Drawable? = null

  constructor()

  constructor(y: Float) {
    this.y = y
  }

  constructor(y: Float, data: Any?) : this(y) {
    this.data = data
  }

  constructor(y: Float, icon: Drawable?) : this(y) {
    this.icon = icon
  }

  constructor(y: Float, icon: Drawable?, data: Any?) : this(y) {
    this.icon = icon
    this.data = data
  }
}
