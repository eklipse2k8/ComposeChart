package com.github.mikephil.charting.utils

import android.graphics.*
import android.graphics.drawable.Drawable
import kotlin.math.floor

open class Fill {
  enum class Type {
    EMPTY,
    COLOR,
    LINEAR_GRADIENT,
    DRAWABLE
  }

  enum class Direction {
    DOWN,
    UP,
    RIGHT,
    LEFT
  }

  /** the type of fill */
  var type = Type.EMPTY

  /** the color that is used for filling */
  var color: Int? = null
    private set

  private var mFinalColor: Int? = null

  /** the drawable to be used for filling */
  private var mDrawable: Drawable? = null

  var gradientColors: IntArray? = null

  var gradientPositions: FloatArray? = null

  /** transparency used for filling */
  private var mAlpha = 255

  constructor()

  constructor(color: Int) {
    type = Type.COLOR
    this.color = color
    calculateFinalColor()
  }

  constructor(startColor: Int, endColor: Int) {
    type = Type.LINEAR_GRADIENT
    gradientColors = intArrayOf(startColor, endColor)
  }

  constructor(gradientColors: IntArray) {
    type = Type.LINEAR_GRADIENT
    this.gradientColors = gradientColors
  }

  constructor(gradientColors: IntArray, gradientPositions: FloatArray) {
    type = Type.LINEAR_GRADIENT
    this.gradientColors = gradientColors
    this.gradientPositions = gradientPositions
  }

  constructor(drawable: Drawable) {
    type = Type.DRAWABLE
    mDrawable = drawable
  }

  fun setColor(color: Int) {
    this.color = color
    calculateFinalColor()
  }

  fun setGradientColors(startColor: Int, endColor: Int) {
    gradientColors = intArrayOf(startColor, endColor)
  }

  var alpha: Int
    get() = mAlpha
    set(alpha) {
      mAlpha = alpha
      calculateFinalColor()
    }

  private fun calculateFinalColor() {
    mFinalColor =
        color?.let {
          val alpha = floor((it shr 24) / 255.0 * (mAlpha / 255.0) * 255.0).toInt()
          alpha shl 24 or (it and 0xffffff)
        }
  }

  fun fillRect(
      c: Canvas,
      paint: Paint,
      left: Float,
      top: Float,
      right: Float,
      bottom: Float,
      gradientDirection: Direction
  ) {
    when (type) {
      Type.EMPTY -> return
      Type.COLOR -> {
        val localColor = mFinalColor ?: return
        if (isClipPathSupported) {
          val save = c.save()
          c.clipRect(left, top, right, bottom)
          c.drawColor(localColor)
          c.restoreToCount(save)
        } else {
          // save
          val previous = paint.style
          val previousColor = paint.color

          // set
          paint.style = Paint.Style.FILL
          paint.color = localColor
          c.drawRect(left, top, right, bottom, paint)

          // restore
          paint.color = previousColor
          paint.style = previous
        }
      }
      Type.LINEAR_GRADIENT -> {
        if (gradientColors == null) return
        val gradient =
            LinearGradient(
                when (gradientDirection) {
                  Direction.RIGHT -> right
                  Direction.LEFT -> left
                  else -> left
                }.toFloat(),
                when (gradientDirection) {
                  Direction.UP -> bottom
                  Direction.DOWN -> top
                  else -> top
                }.toFloat(),
                when (gradientDirection) {
                  Direction.RIGHT -> left
                  Direction.LEFT -> right
                  else -> left
                }.toFloat(),
                when (gradientDirection) {
                  Direction.UP -> top
                  Direction.DOWN -> bottom
                  else -> top
                }.toFloat(),
                gradientColors ?: intArrayOf(),
                gradientPositions,
                Shader.TileMode.MIRROR)
        paint.shader = gradient
        c.drawRect(left, top, right, bottom, paint)
      }
      Type.DRAWABLE -> {
        val localDrawable = mDrawable ?: return
        localDrawable.setBounds(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
        localDrawable.draw(c)
      }
    }
  }

  fun fillPath(c: Canvas, path: Path?, paint: Paint, clipRect: RectF?) {
    if (path == null) return
    when (type) {
      Type.EMPTY -> return
      Type.COLOR -> {
        val localFinalColor = mFinalColor ?: return
        if (clipRect != null && isClipPathSupported) {
          val save = c.save()
          c.clipPath(path)
          c.drawColor(localFinalColor)
          c.restoreToCount(save)
        } else {
          // save
          val previous = paint.style
          val previousColor = paint.color

          // set
          paint.style = Paint.Style.FILL
          paint.color = localFinalColor
          c.drawPath(path, paint)

          // restore
          paint.color = previousColor
          paint.style = previous
        }
      }
      Type.LINEAR_GRADIENT -> {
        gradientColors ?: return
        val gradient =
            LinearGradient(
                0f,
                0f,
                c.width.toFloat(),
                c.height.toFloat(),
                gradientColors ?: intArrayOf(),
                gradientPositions,
                Shader.TileMode.MIRROR)
        paint.shader = gradient
        c.drawPath(path, paint)
      }
      Type.DRAWABLE -> {
        val localDrawable = mDrawable ?: return
        ensureClipPathSupported()
        val save = c.save()
        c.clipPath(path)
        localDrawable.setBounds(
            clipRect?.left?.toInt() ?: 0,
            clipRect?.top?.toInt() ?: 0,
            clipRect?.right?.toInt() ?: c.width,
            clipRect?.bottom?.toInt() ?: c.height)
        localDrawable.draw(c)
        c.restoreToCount(save)
      }
    }
  }

  private val isClipPathSupported: Boolean
    get() = Utils.getSDKInt() >= 18

  private fun ensureClipPathSupported() {
    if (Utils.getSDKInt() < 18) {
      throw RuntimeException(
          "Fill-drawables not (yet) supported below API level 18, " +
              "this code was run on API level " +
              Utils.getSDKInt() +
              ".")
    }
  }
}
