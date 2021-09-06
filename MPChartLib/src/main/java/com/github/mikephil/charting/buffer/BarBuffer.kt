package com.github.mikephil.charting.buffer

import com.github.mikephil.charting.interfaces.datasets.IBarDataSet

open class BarBuffer(size: Int, dataSetCount: Int, containsStacks: Boolean) :
    AbstractBuffer<IBarDataSet>(size) {
  protected var mDataSetIndex = 0
  protected var mDataSetCount = dataSetCount
  @JvmField protected var mContainsStacks = containsStacks
  @JvmField protected var mInverted = false

  /** width of the bar on the x-axis, in values (not pixels) */
  @JvmField protected var mBarWidth = 1f
  fun setBarWidth(barWidth: Float) {
    mBarWidth = barWidth
  }

  fun setDataSet(index: Int) {
    mDataSetIndex = index
  }

  fun setInverted(inverted: Boolean) {
    mInverted = inverted
  }

  protected fun addBar(left: Float, top: Float, right: Float, bottom: Float) {
    buffer[index++] = left
    buffer[index++] = top
    buffer[index++] = right
    buffer[index++] = bottom
  }

  override fun feed(data: IBarDataSet) {
    val size = data.entryCount * phaseX
    val barWidthHalf = mBarWidth / 2f
    var i = 0
    while (i < size) {
      val e = data.getEntryForIndex(i)
      if (e == null) {
        i++
        continue
      }
      val x = e.x
      var y = e.y
      val vals = e.yVals
      if (!mContainsStacks || vals == null) {
        val left = x - barWidthHalf
        val right = x + barWidthHalf
        var bottom: Float
        var top: Float
        if (mInverted) {
          bottom = if (y >= 0) y else 0f
          top = if (y <= 0) y else 0f
        } else {
          top = if (y >= 0) y else 0f
          bottom = if (y <= 0) y else 0f
        }

        // multiply the height of the rect with the phase
        if (top > 0) top *= phaseY else bottom *= phaseY
        addBar(left, top, right, bottom)
      } else {
        var posY = 0f
        var negY = -e.negativeSum
        var yStart: Float

        // fill the stack
        for (k in vals.indices) {
          val value = vals[k]
          if (value == 0.0f && (posY == 0.0f || negY == 0.0f)) {
            // Take care of the situation of a 0.0 value, which overlaps a non-zero bar
            y = value
            yStart = y
          } else if (value >= 0.0f) {
            y = posY
            yStart = posY + value
            posY = yStart
          } else {
            y = negY
            yStart = negY + Math.abs(value)
            negY += Math.abs(value)
          }
          val left = x - barWidthHalf
          val right = x + barWidthHalf
          var bottom: Float
          var top: Float
          if (mInverted) {
            bottom = if (y >= yStart) y else yStart
            top = if (y <= yStart) y else yStart
          } else {
            top = if (y >= yStart) y else yStart
            bottom = if (y <= yStart) y else yStart
          }

          // multiply the height of the rect with the phase
          top *= phaseY
          bottom *= phaseY
          addBar(left, top, right, bottom)
        }
      }
      i++
    }
    reset()
  }
}
