package com.github.eklipse2k8.charting.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.Align
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import com.github.eklipse2k8.charting.formatter.DefaultValueFormatter
import com.github.eklipse2k8.charting.formatter.IValueFormatter
import kotlin.math.*

/**
 * Utilities class that has some helper methods. Needs to be initialized by calling Utils.init(...)
 * before usage. Inside the Chart.init() method, this is done, if the Utils are used before that,
 * Utils.init(...) needs to be called manually.
 *
 * @author Philipp Jahoda
 */
object Utils {
  private var mMetrics: DisplayMetrics? = null

  var minimumFlingVelocity = 50
    private set

  var maximumFlingVelocity = 8000
  const val DEG2RAD = Math.PI / 180.0

  const val FDEG2RAD = Math.PI.toFloat() / 180f

  val DOUBLE_EPSILON = Double.fromBits(1)

  val FLOAT_EPSILON = Float.fromBits(1)

  /**
   * initialize method, called inside the Chart.init() method.
   *
   * @param context
   */
  fun init(context: Context) {
    val viewConfiguration = ViewConfiguration.get(context)
    minimumFlingVelocity = viewConfiguration.scaledMinimumFlingVelocity
    maximumFlingVelocity = viewConfiguration.scaledMaximumFlingVelocity
    mMetrics = context.resources.displayMetrics
  }

  /**
   * initialize method, called inside the Chart.init() method. backwards compatibility - to not
   * break existing code
   *
   * @param res
   */
  @Deprecated("")
  fun init(res: Resources) {
    mMetrics = res.displayMetrics

    // noinspection deprecation
    minimumFlingVelocity = ViewConfiguration.getMinimumFlingVelocity()
    // noinspection deprecation
    maximumFlingVelocity = ViewConfiguration.getMaximumFlingVelocity()
  }

  /**
   * This method converts dp unit to equivalent pixels, depending on device density. NEEDS UTILS TO
   * BE INITIALIZED BEFORE USAGE.
   *
   * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
   * @return A float value to represent px equivalent to dp depending on device density
   */
  fun convertDpToPixel(dp: Float): Float {
    if (mMetrics == null) {
      Log.e(
          "MPChartLib-Utils",
          "Utils NOT INITIALIZED. You need to call Utils.init(...) at least once before" +
              " calling Utils.INSTANCE.convertDpToPixel(...). Otherwise conversion does not " +
              "take place.")
      return dp
    }
    return dp * mMetrics!!.density
  }

  /**
   * This method converts device specific pixels to density independent pixels. NEEDS UTILS TO BE
   * INITIALIZED BEFORE USAGE.
   *
   * @param px A value in px (pixels) unit. Which we need to convert into db
   * @return A float value to represent dp equivalent to px value
   */
  fun convertPixelsToDp(px: Float): Float {
    if (mMetrics == null) {
      Log.e(
          "MPChartLib-Utils",
          "Utils NOT INITIALIZED. You need to call Utils.init(...) at least once before" +
              " calling Utils.convertPixelsToDp(...). Otherwise conversion does not" +
              " take place.")
      return px
    }
    return px / mMetrics!!.density
  }

  /**
   * calculates the approximate width of a text, depending on a demo text avoid repeated calls (e.g.
   * inside drawing methods)
   *
   * @param paint
   * @param demoText
   * @return
   */
  fun calcTextWidth(paint: Paint?, demoText: String?): Int =
      paint?.measureText(demoText)?.toInt() ?: 0

  private val mCalcTextHeightRect = Rect()

  /**
   * calculates the approximate height of a text, depending on a demo text avoid repeated calls
   * (e.g. inside drawing methods)
   *
   * @param paint
   * @param demoText
   * @return
   */
  fun calcTextHeight(paint: Paint?, demoText: String): Int {
    val r = mCalcTextHeightRect
    r[0, 0, 0] = 0
    paint?.getTextBounds(demoText, 0, demoText.length, r)
    return r.height()
  }

  private val mFontMetrics = Paint.FontMetrics()
  fun getLineHeight(paint: Paint?): Float {
    return getLineHeight(paint, mFontMetrics)
  }

  fun getLineHeight(paint: Paint?, fontMetrics: Paint.FontMetrics): Float {
    paint?.getFontMetrics(fontMetrics)
    return fontMetrics.descent - fontMetrics.ascent
  }

  fun getLineSpacing(paint: Paint?): Float {
    return getLineSpacing(paint, mFontMetrics)
  }

  fun getLineSpacing(paint: Paint?, fontMetrics: Paint.FontMetrics): Float {
    paint?.getFontMetrics(fontMetrics)
    return fontMetrics.ascent - fontMetrics.top + fontMetrics.bottom
  }

  /**
   * Returns a recyclable FSize instance. calculates the approximate size of a text, depending on a
   * demo text avoid repeated calls (e.g. inside drawing methods)
   *
   * @param paint
   * @param demoText
   * @return A Recyclable FSize instance
   */
  fun calcTextSize(paint: Paint?, demoText: String): FSize {
    val result = FSize.getInstance(0f, 0f)
    calcTextSize(paint, demoText, result)
    return result
  }

  private val mCalcTextSizeRect = Rect()

  /**
   * calculates the approximate size of a text, depending on a demo text avoid repeated calls (e.g.
   * inside drawing methods)
   *
   * @param paint
   * @param demoText
   * @param outputFSize An output variable, modified by the function.
   */
  private fun calcTextSize(paint: Paint?, demoText: String, outputFSize: FSize) {
    val r = mCalcTextSizeRect
    r[0, 0, 0] = 0
    paint?.getTextBounds(demoText, 0, demoText.length, r)
    outputFSize.width = r.width().toFloat()
    outputFSize.height = r.height().toFloat()
  }

  /** Math.pow(...) is very expensive, so avoid calling it and create it yourself. */
  private val POW_10 =
      intArrayOf(1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000)

  /// - returns: The default value formatter used for all chart components that needs a default
  val defaultValueFormatter = generateDefaultValueFormatter()
  private fun generateDefaultValueFormatter(): IValueFormatter {
    return DefaultValueFormatter(1)
  }
  /**
   * Formats the given number to the given number of decimals, and returns the number as a string,
   * maximum 35 characters.
   *
   * @param number
   * @param digitCount
   * @param separateThousands set this to true to separate thousands values
   * @param separateChar a caracter to be paced between the "thousands"
   * @return
   */
  /**
   * Formats the given number to the given number of decimals, and returns the number as a string,
   * maximum 35 characters. If thousands are separated, the separating character is a dot (".").
   *
   * @param number
   * @param digitCount
   * @param separateThousands set this to true to separate thousands values
   * @return
   */
  @JvmOverloads
  fun formatNumber(
      number: Float,
      digitCount: Int,
      separateThousands: Boolean,
      separateChar: Char = '.'
  ): String {
    var number = number
    var digitCount = digitCount
    val out = CharArray(35)
    var neg = false
    if (number == 0f) {
      return "0"
    }
    var zero = false
    if (number < 1 && number > -1) {
      zero = true
    }
    if (number < 0) {
      neg = true
      number = -number
    }
    if (digitCount > POW_10.size) {
      digitCount = POW_10.size - 1
    }
    number *= POW_10[digitCount]
    var lval = number.roundToLong()
    var ind = out.size - 1
    var charCount = 0
    var decimalPointAdded = false
    while (lval != 0L || charCount < digitCount + 1) {
      val digit = (lval % 10).toInt()
      lval /= 10
      out[ind--] = (digit + '0'.code).toChar()
      charCount++

      // add decimal point
      if (charCount == digitCount) {
        out[ind--] = ','
        charCount++
        decimalPointAdded = true

        // add thousand separators
      } else if (separateThousands && lval != 0L && charCount > digitCount) {
        if (decimalPointAdded) {
          if ((charCount - digitCount) % 4 == 0) {
            out[ind--] = separateChar
            charCount++
          }
        } else {
          if ((charCount - digitCount) % 4 == 3) {
            out[ind--] = separateChar
            charCount++
          }
        }
      }
    }

    // if number around zero (between 1 and -1)
    if (zero) {
      out[ind--] = '0'
      charCount += 1
    }

    // if the number is negative
    if (neg) {
      out[ind--] = '-'
      charCount += 1
    }
    val start = out.size - charCount

    // use this instead of "new String(...)" because of issue < Android 4.0
    return String(out, start, out.size - start)
  }

  /**
   * rounds the given number to the next significant number
   *
   * @param number
   * @return
   */
  fun roundToNextSignificant(number: Double): Float {
    if (number.isInfinite() || number.isNaN() || number == 0.0) return 0f
    val d = ceil(log10(if (number < 0) -number else number))
    val pw = 1 - d.toInt()
    val magnitude = 10.0.pow(pw).toFloat()
    val shifted = (number * magnitude).roundToInt()
    return shifted / magnitude
  }

  /**
   * Returns the appropriate number of decimals to be used for the provided number.
   *
   * @param number
   * @return
   */
  fun getDecimals(number: Float): Int {
    val i = roundToNextSignificant(number.toDouble())
    return if (i.isInfinite()) 0 else ceil(-log10(i)).toInt() + 2
  }

  /**
   * Converts the provided Integer List to an int array.
   *
   * @param integers
   * @return
   */
  fun convertIntegers(integers: List<Int>): IntArray {
    val ret = IntArray(integers.size)
    copyIntegers(integers, ret)
    return ret
  }

  private fun copyIntegers(from: List<Int>, to: IntArray) {
    val count = if (to.size < from.size) to.size else from.size
    for (i in 0 until count) {
      to[i] = from[i]
    }
  }

  /**
   * Converts the provided String List to a String array.
   *
   * @param strings
   * @return
   */
  fun convertStrings(strings: List<String?>): Array<String?> {
    val ret = arrayOfNulls<String>(strings.size)
    for (i in ret.indices) {
      ret[i] = strings[i]
    }
    return ret
  }

  fun copyStrings(from: List<String?>, to: Array<String?>) {
    val count = if (to.size < from.size) to.size else from.size
    for (i in 0 until count) {
      to[i] = from[i]
    }
  }

  /**
   * Returns a recyclable MPPointF instance. Calculates the position around a center point,
   * depending on the distance from the center, and the angle of the position around the center.
   *
   * @param center
   * @param dist
   * @param angle in degrees, converted to radians internally
   * @return
   */
  fun getPosition(center: MPPointF, dist: Float, angle: Float): MPPointF {
    val p = MPPointF.getInstance(0f, 0f)
    getPosition(center, dist, angle, p)
    return p
  }

  fun getPosition(center: MPPointF?, dist: Float, angle: Float, outputPoint: MPPointF) {
    val centerX = center?.x ?: 0f
    val centerY = center?.y ?: 0f

    outputPoint.x = centerX + dist * cos(angle.toRadians())
    outputPoint.y = centerY + dist * sin(angle.toRadians())
  }

  fun velocityTrackerPointerUpCleanUpIfNecessary(ev: MotionEvent, tracker: VelocityTracker) {
    // Check the dot product of current velocities.
    // If the pointer that left was opposing another velocity vector, clear.
    tracker.computeCurrentVelocity(1000, maximumFlingVelocity.toFloat())
    val upIndex = ev.actionIndex
    val id1 = ev.getPointerId(upIndex)
    val x1 = tracker.getXVelocity(id1)
    val y1 = tracker.getYVelocity(id1)
    var i = 0
    val count = ev.pointerCount
    while (i < count) {
      if (i == upIndex) {
        i++
        continue
      }
      val id2 = ev.getPointerId(i)
      val x = x1 * tracker.getXVelocity(id2)
      val y = y1 * tracker.getYVelocity(id2)
      val dot = x + y
      if (dot < 0) {
        tracker.clear()
        break
      }
      i++
    }
  }

  /**
   * Original method view.postInvalidateOnAnimation() only supportd in API >= 16, This is a replica
   * of the code from ViewCompat.
   *
   * @param view
   */
  fun postInvalidateOnAnimation(view: View) {
    view.postInvalidateOnAnimation()
  }

  /** returns an angle between 0.f < 360.f (not less than zero, less than 360) */
  fun getNormalizedAngle(angle: Float): Float {
    var localAngle = angle
    while (localAngle < 0f) localAngle += 360f
    return localAngle % 360f
  }

  private val mDrawableBoundsCache = Rect()
  fun drawImage(canvas: Canvas, drawable: Drawable?, x: Int, y: Int, width: Int, height: Int) {
    if (drawable == null) return
    val drawOffset = MPPointF.instance
    drawOffset.x = (x - width / 2).toFloat()
    drawOffset.y = (y - height / 2).toFloat()
    drawable.copyBounds(mDrawableBoundsCache)
    drawable.setBounds(
        mDrawableBoundsCache.left,
        mDrawableBoundsCache.top,
        mDrawableBoundsCache.left + width,
        mDrawableBoundsCache.top + width)
    val saveId = canvas.save()
    // translate to the correct position and draw
    canvas.translate(drawOffset.x, drawOffset.y)
    drawable.draw(canvas)
    canvas.restoreToCount(saveId)
  }

  private val mDrawTextRectBuffer = Rect()
  private val mFontMetricsBuffer = Paint.FontMetrics()
  fun drawXAxisValue(
      c: Canvas?,
      text: String?,
      x: Float,
      y: Float,
      paint: Paint,
      anchor: MPPointF?,
      angleDegrees: Float
  ) {
    if (c == null) return
    var drawOffsetX = 0f
    var drawOffsetY = 0f
    val lineHeight = paint.getFontMetrics(mFontMetricsBuffer)
    paint.getTextBounds(text, 0, text?.length ?: 0, mDrawTextRectBuffer)

    // Android sometimes has pre-padding
    drawOffsetX -= mDrawTextRectBuffer.left.toFloat()

    // Android does not snap the bounds to line boundaries,
    //  and draws from bottom to top.
    // And we want to normalize it.
    drawOffsetY += -mFontMetricsBuffer.ascent

    // To have a consistent point of reference, we always draw left-aligned
    val originalTextAlign = paint.textAlign
    paint.textAlign = Align.LEFT
    if (angleDegrees != 0f) {

      // Move the text drawing rect in a way that it always rotates around its center
      drawOffsetX -= mDrawTextRectBuffer.width() * 0.5f
      drawOffsetY -= lineHeight * 0.5f
      var translateX = x
      var translateY = y

      // Move the "outer" rect relative to the anchor, assuming its centered
      if (anchor?.x != 0.5f || anchor.y != 0.5f) {
        val rotatedSize =
            getSizeOfRotatedRectangleByDegrees(
                mDrawTextRectBuffer.width().toFloat(), lineHeight, angleDegrees)
        translateX -= rotatedSize.width * ((anchor?.x ?: 0f) - 0.5f)
        translateY -= rotatedSize.height * ((anchor?.y ?: 0f) - 0.5f)
        FSize.recycleInstance(rotatedSize)
      }
      c.save()
      c.translate(translateX, translateY)
      c.rotate(angleDegrees)
      text?.let { c.drawText(it, drawOffsetX, drawOffsetY, paint) }
      c.restore()
    } else {
      if (anchor?.x != 0f || anchor.y != 0f) {
        drawOffsetX -= mDrawTextRectBuffer.width() * (anchor?.x ?: 0f)
        drawOffsetY -= lineHeight * (anchor?.y ?: 0f)
      }
      drawOffsetX += x
      drawOffsetY += y
      text?.let { c.drawText(it, drawOffsetX, drawOffsetY, paint) }
    }
    paint.textAlign = originalTextAlign
  }

  private fun drawMultilineText(
      c: Canvas,
      textLayout: StaticLayout,
      x: Float,
      y: Float,
      paint: TextPaint,
      anchor: MPPointF,
      angleDegrees: Float
  ) {
    var drawOffsetX = 0f
    var drawOffsetY = 0f
    val drawHeight: Float
    val lineHeight = paint.getFontMetrics(mFontMetricsBuffer)
    val drawWidth: Float = textLayout.width.toFloat()
    drawHeight = textLayout.lineCount * lineHeight

    // Android sometimes has pre-padding
    drawOffsetX -= mDrawTextRectBuffer.left.toFloat()

    // Android does not snap the bounds to line boundaries,
    //  and draws from bottom to top.
    // And we want to normalize it.
    drawOffsetY += drawHeight

    // To have a consistent point of reference, we always draw left-aligned
    val originalTextAlign = paint.textAlign
    paint.textAlign = Align.LEFT
    if (angleDegrees != 0f) {

      // Move the text drawing rect in a way that it always rotates around its center
      drawOffsetX -= drawWidth * 0.5f
      drawOffsetY -= drawHeight * 0.5f
      var translateX = x
      var translateY = y

      // Move the "outer" rect relative to the anchor, assuming its centered
      if (anchor.x != 0.5f || anchor.y != 0.5f) {
        val rotatedSize = getSizeOfRotatedRectangleByDegrees(drawWidth, drawHeight, angleDegrees)
        translateX -= rotatedSize.width * (anchor.x - 0.5f)
        translateY -= rotatedSize.height * (anchor.y - 0.5f)
        FSize.recycleInstance(rotatedSize)
      }
      c.save()
      c.translate(translateX, translateY)
      c.rotate(angleDegrees)
      c.translate(drawOffsetX, drawOffsetY)
      textLayout.draw(c)
      c.restore()
    } else {
      if (anchor.x != 0f || anchor.y != 0f) {
        drawOffsetX -= drawWidth * anchor.x
        drawOffsetY -= drawHeight * anchor.y
      }
      drawOffsetX += x
      drawOffsetY += y
      c.save()
      c.translate(drawOffsetX, drawOffsetY)
      textLayout.draw(c)
      c.restore()
    }
    paint.textAlign = originalTextAlign
  }

  fun drawMultilineText(
      c: Canvas,
      text: String,
      x: Float,
      y: Float,
      paint: TextPaint,
      constrainedToSize: FSize,
      anchor: MPPointF,
      angleDegrees: Float
  ) {
    val textLayout =
        StaticLayout(
            text,
            0,
            text.length,
            paint,
            max(ceil(constrainedToSize.width), 1f).toInt(),
            Layout.Alignment.ALIGN_NORMAL,
            1f,
            0f,
            false)
    drawMultilineText(c, textLayout, x, y, paint, anchor, angleDegrees)
  }

  /**
   * Returns a recyclable FSize instance. Represents size of a rotated rectangle by degrees.
   *
   * @param rectangleSize
   * @param degrees
   * @return A Recyclable FSize instance
   */
  fun getSizeOfRotatedRectangleByDegrees(rectangleSize: FSize, degrees: Float): FSize {
    val radians = degrees * FDEG2RAD
    return getSizeOfRotatedRectangleByRadians(rectangleSize.width, rectangleSize.height, radians)
  }

  /**
   * Returns a recyclable FSize instance. Represents size of a rotated rectangle by radians.
   *
   * @param rectangleSize
   * @param radians
   * @return A Recyclable FSize instance
   */
  fun getSizeOfRotatedRectangleByRadians(rectangleSize: FSize, radians: Float): FSize {
    return getSizeOfRotatedRectangleByRadians(rectangleSize.width, rectangleSize.height, radians)
  }

  /**
   * Returns a recyclable FSize instance. Represents size of a rotated rectangle by degrees.
   *
   * @param rectangleWidth
   * @param rectangleHeight
   * @param degrees
   * @return A Recyclable FSize instance
   */
  fun getSizeOfRotatedRectangleByDegrees(
      rectangleWidth: Float,
      rectangleHeight: Float,
      degrees: Float
  ): FSize {
    val radians = degrees * FDEG2RAD
    return getSizeOfRotatedRectangleByRadians(rectangleWidth, rectangleHeight, radians)
  }

  /**
   * Returns a recyclable FSize instance. Represents size of a rotated rectangle by radians.
   *
   * @param rectangleWidth
   * @param rectangleHeight
   * @param radians
   * @return A Recyclable FSize instance
   */
  private fun getSizeOfRotatedRectangleByRadians(
      rectangleWidth: Float,
      rectangleHeight: Float,
      radians: Float
  ): FSize {
    return FSize.getInstance(
        abs(rectangleWidth * cos(radians)) + abs(rectangleHeight * sin(radians)),
        abs(rectangleWidth * sin(radians)) + abs(rectangleHeight * cos(radians)))
  }

  val sDKInt: Int
    get() = Build.VERSION.SDK_INT
}
