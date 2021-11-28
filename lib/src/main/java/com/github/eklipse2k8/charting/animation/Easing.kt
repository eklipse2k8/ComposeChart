package com.github.eklipse2k8.charting.animation

import android.animation.TimeInterpolator
import androidx.annotation.RequiresApi
import com.github.eklipse2k8.charting.animation.Easing.EasingFunction
import kotlin.math.*

/**
 * Easing options.
 *
 * @author Daniel Cohen Gindi
 * @author Mick Ashton
 */
@RequiresApi(11)
object Easing {
  private const val DOUBLE_PI = 2f * Math.PI.toFloat()

  val Linear: EasingFunction = EasingFunction { input -> input }

  val EaseInQuad: EasingFunction = EasingFunction { input -> input * input }

  val EaseOutQuad: EasingFunction = EasingFunction { input -> -input * (input - 2f) }

  val EaseInOutQuad: EasingFunction = EasingFunction { input ->
    var calcInput = input
    calcInput *= 2f
    if (calcInput < 1f) {
      0.5f * calcInput * calcInput
    } else {
      -0.5f * (--calcInput * (calcInput - 2f) - 1f)
    }
  }

  val EaseInCubic: EasingFunction = EasingFunction { input -> input.toDouble().pow(3.0).toFloat() }

  val EaseOutCubic: EasingFunction = EasingFunction { input ->
    var calcInput = input
    calcInput--
    calcInput.toDouble().pow(3.0).toFloat() + 1f
  }

  val EaseInOutCubic: EasingFunction = EasingFunction { input ->
    var calcInput = input
    calcInput *= 2f
    if (calcInput < 1f) {
      0.5f * calcInput.toDouble().pow(3.0).toFloat()
    } else {
      calcInput -= 2f
      0.5f * (calcInput.toDouble().pow(3.0).toFloat() + 2f)
    }
  }

  val EaseInQuart: EasingFunction = EasingFunction { input -> input.toDouble().pow(4.0).toFloat() }

  val EaseOutQuart: EasingFunction = EasingFunction { input ->
    var calcInput = input
    calcInput--
    -(calcInput.toDouble().pow(4.0).toFloat() - 1f)
  }

  val EaseInOutQuart: EasingFunction = EasingFunction { input ->
    var calcInput = input
    calcInput *= 2f
    if (calcInput < 1f) {
      0.5f * calcInput.toDouble().pow(4.0).toFloat()
    } else {
      calcInput -= 2f
      -0.5f * (calcInput.toDouble().pow(4.0).toFloat() - 2f)
    }
  }

  val EaseInSine: EasingFunction = EasingFunction { input ->
    (-cos(input * (Math.PI / 2f))).toFloat() + 1f
  }

  val EaseOutSine: EasingFunction = EasingFunction { input ->
    sin(input * (Math.PI / 2f)).toFloat()
  }

  val EaseInOutSine: EasingFunction = EasingFunction { input ->
    -0.5f * (cos(Math.PI * input).toFloat() - 1f)
  }

  val EaseInExpo: EasingFunction = EasingFunction { input ->
    if (input == 0f) 0f else 2.0.pow((10f * (input - 1f)).toDouble()).toFloat()
  }

  val EaseOutExpo: EasingFunction = EasingFunction { input ->
    if (input == 1f) 1f else ((-2.0).pow((-10f * (input + 1f)).toDouble())).toFloat()
  }

  val EaseInOutExpo: EasingFunction = EasingFunction { input ->
    var calcInput = input
    when (calcInput) {
      0f -> 0f
      1f -> 1f
      else -> {
        calcInput *= 2f
        if (calcInput < 1f) {
          0.5f * 2.0.pow((10f * (calcInput - 1f)).toDouble()).toFloat()
        } else {
          0.5f * (((-2.0).pow((-10f * --calcInput).toDouble())).toFloat() + 2f)
        }
      }
    }
  }

  val EaseInCirc: EasingFunction = EasingFunction { input -> -(sqrt(1f - input * input) - 1f) }

  val EaseOutCirc: EasingFunction = EasingFunction { input ->
    var calcInput = input
    calcInput--
    sqrt(1f - calcInput * calcInput)
  }

  val EaseInOutCirc: EasingFunction = EasingFunction { input ->
    var calcInput = input
    calcInput *= 2f
    if (calcInput < 1f) {
      -0.5f * (sqrt((1f - calcInput * calcInput)) - 1f)
    } else
        0.5f *
            (sqrt(
                1f -
                    2f.let {
                      calcInput -= it
                      calcInput
                    } * calcInput) + 1f)
  }

  val EaseInElastic: EasingFunction =
      object : EasingFunction {
        override fun getInterpolation(input: Float): Float {
          var calcInput = input
          if (calcInput == 0f) {
            return 0f
          } else if (calcInput == 1f) {
            return 1f
          }
          val p = 0.3f
          val s = p / DOUBLE_PI * asin(1.0).toFloat()
          return -(2.0.pow(
                  (10f *
                          1f.let {
                            calcInput -= it
                            calcInput
                          })
                      .toDouble())
              .toFloat() * sin((calcInput - s) * DOUBLE_PI / p))
        }
      }

  val EaseOutElastic: EasingFunction = EasingFunction { input ->
    when (input) {
      0f -> 0f
      1f -> 1f
      else -> {
        val p = 0.3f
        val s = p / DOUBLE_PI * asin(1.0).toFloat()
        (1f + 2.0.pow((-10f * input).toDouble()).toFloat() * sin((input - s) * DOUBLE_PI / p))
      }
    }
  }

  val EaseInOutElastic: EasingFunction = EasingFunction { input ->
    var calcInput = input
    calcInput *= 2f
    when (calcInput) {
      0f -> 0f
      2f -> 1f
      else -> {
        val p = 1f / 0.45f
        val s = 0.45f / DOUBLE_PI * asin(1.0).toFloat()
        if (calcInput < 1f) {
          (-0.5f *
              (2.0.pow(
                      (10f *
                              1f.let {
                                calcInput -= it
                                calcInput
                              })
                          .toDouble())
                  .toFloat() * sin((calcInput * 1f - s) * DOUBLE_PI * p)))
        } else
            1f +
                (0.5f *
                    2.0.pow(
                            (-10f *
                                    1f.let {
                                      calcInput -= it
                                      calcInput
                                    })
                                .toDouble())
                        .toFloat() *
                    sin((calcInput * 1f - s) * DOUBLE_PI * p))
      }
    }
  }

  val EaseInBack: EasingFunction = EasingFunction { input ->
    val s = 1.70158f
    input * input * ((s + 1f) * input - s)
  }

  val EaseOutBack: EasingFunction = EasingFunction { input ->
    var calcInput = input
    val s = 1.70158f
    calcInput--
    calcInput * calcInput * ((s + 1f) * calcInput + s) + 1f
  }

  val EaseInOutBack: EasingFunction = EasingFunction { input ->
    var calcInput = input
    var s = 1.70158f
    calcInput *= 2f
    if (calcInput < 1f) {
      0.5f *
          (calcInput *
              calcInput *
              ((1.525f.let {
                s *= it
                s
              } + 1f) * calcInput - s))
    } else
        0.5f *
            (2f.let {
              calcInput -= it
              calcInput
            } *
                calcInput *
                ((1.525f.let {
                  s *= it
                  s
                } + 1f) * calcInput + s) + 2f)
  }

  val EaseInBounce: EasingFunction = EasingFunction { input ->
    1f - EaseOutBounce.getInterpolation(1f - input)
  }

  val EaseOutBounce: EasingFunction =
      object : EasingFunction {
        override fun getInterpolation(input: Float): Float {
          var input = input
          val s = 7.5625f
          if (input < 1f / 2.75f) {
            return s * input * input
          } else if (input < 2f / 2.75f) {
            return s *
                (1.5f / 2.75f).let {
                  input -= it
                  input
                } *
                input + 0.75f
          } else if (input < 2.5f / 2.75f) {
            return s *
                (2.25f / 2.75f).let {
                  input -= it
                  input
                } *
                input + 0.9375f
          }
          return s *
              (2.625f / 2.75f).let {
                input -= it
                input
              } *
              input + 0.984375f
        }
      }

  val EaseInOutBounce: EasingFunction = EasingFunction { input ->
    if (input < 0.5f) {
      EaseInBounce.getInterpolation(input * 2f) * 0.5f
    } else EaseOutBounce.getInterpolation(input * 2f - 1f) * 0.5f + 0.5f
  }

  fun interface EasingFunction : TimeInterpolator {
    override fun getInterpolation(input: Float): Float
  }
}
