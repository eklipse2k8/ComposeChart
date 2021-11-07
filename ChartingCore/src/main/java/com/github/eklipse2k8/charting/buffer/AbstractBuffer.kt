package com.github.eklipse2k8.charting.buffer

import kotlin.math.max

/**
 * Buffer class to boost performance while drawing. Concept: Replace instead of recreate.
 *
 * @author Philipp Jahoda
 * @param <T> The data the buffer accepts to be fed with. </T>
 */
abstract class AbstractBuffer<T>(size: Int) {
  /** index in the buffer */
  @JvmField protected var index = 0

  /** float-buffer that holds the data points to draw, order: x,y,x,y,... */
  @JvmField val buffer = FloatArray(size)

  /** animation phase x-axis */
  @JvmField protected var phaseX = 1f

  /** animation phase y-axis */
  @JvmField protected var phaseY = 1f

  /** indicates from which x-index the visible data begins */
  protected var fromRange = 0

  /** indicates to which x-index the visible data ranges */
  protected var toRange = 0

  /** limits the drawing on the x-axis */
  fun limitFrom(from: Int) {
    this.fromRange = max(0, from)
  }

  /** limits the drawing on the x-axis */
  fun limitTo(to: Int) {
    toRange = max(0, to)
  }

  /** Resets the buffer index to 0 and makes the buffer reusable. */
  fun reset() {
    index = 0
  }

  /**
   * Returns the size (length) of the buffer array.
   *
   * @return
   */
  fun size(): Int {
    return buffer.size
  }

  /**
   * Set the phases used for animations.
   *
   * @param phaseX
   * @param phaseY
   */
  fun setPhases(phaseX: Float, phaseY: Float) {
    this.phaseX = phaseX
    this.phaseY = phaseY
  }

  /**
   * Builds up the buffer with the provided data and resets the buffer-index after feed-completion.
   * This needs to run FAST.
   *
   * @param data
   */
  abstract fun feed(data: T)
}
