package com.github.eklipse2k8.charting.buffer

import com.github.eklipse2k8.charting.interfaces.datasets.IBarDataSet
import kotlin.math.abs

class HorizontalBarBuffer(size: Int, containsStacks: Boolean) : BarBuffer(size, containsStacks) {

  override fun feed(data: IBarDataSet) {
    val size = data.entryCount * phaseX
    val barWidthHalf = mBarWidth / 2f
    var i = 0
    while (i < size) {
      val e = data.getEntryForIndex(i)
      val x = e.x
      var y = e.y
      val vals = e.yVals
      if (!mContainsStacks || vals == null) {
        val bottom = x - barWidthHalf
        val top = x + barWidthHalf
        var left: Float
        var right: Float
        if (mInverted) {
          left = if (y >= 0) y else 0f
          right = if (y <= 0) y else 0f
        } else {
          right = if (y >= 0) y else 0f
          left = if (y <= 0) y else 0f
        }

        // multiply the height of the rect with the phase
        if (right > 0) right *= phaseY else left *= phaseY
        addBar(left, top, right, bottom)
      } else {
        var posY = 0f
        var negY = -e.negativeSum
        var yStart: Float

        // fill the stack
        for (k in vals.indices) {
          val value = vals[k]
          if (value >= 0f) {
            y = posY
            yStart = posY + value
            posY = yStart
          } else {
            y = negY
            yStart = negY + abs(value)
            negY += abs(value)
          }
          val bottom = x - barWidthHalf
          val top = x + barWidthHalf
          var left: Float
          var right: Float
          if (mInverted) {
            left = if (y >= yStart) y else yStart
            right = if (y <= yStart) y else yStart
          } else {
            right = if (y >= yStart) y else yStart
            left = if (y <= yStart) y else yStart
          }

          // multiply the height of the rect with the phase
          right *= phaseY
          left *= phaseY
          addBar(left, top, right, bottom)
        }
      }
      i++
    }
    reset()
  }
}
