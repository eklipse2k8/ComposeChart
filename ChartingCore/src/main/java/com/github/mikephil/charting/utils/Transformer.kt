package com.github.mikephil.charting.utils

import android.graphics.Matrix
import android.graphics.Path
import android.graphics.RectF
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.interfaces.datasets.IBubbleDataSet
import com.github.mikephil.charting.interfaces.datasets.ICandleDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet

/**
 * Transformer class that contains all matrices and is responsible for transforming values into
 * pixels on the screen and backwards.
 *
 * @author Philipp Jahoda
 */
open class Transformer(@JvmField protected var viewPortHandler: ViewPortHandler) {
  /** matrix to map the values to the screen pixels */
  var valueMatrix = Matrix()
    protected set

  /** matrix for handling the different offsets of the chart */
  var offsetMatrix = Matrix()
    protected set

  /**
   * Prepares the matrix that transforms values to pixels. Calculates the scale factors from the
   * charts size and offsets.
   *
   * @param xChartMin
   * @param deltaX
   * @param deltaY
   * @param yChartMin
   */
  fun prepareMatrixValuePx(xChartMin: Float, deltaX: Float, deltaY: Float, yChartMin: Float) {
    val scaleX = (viewPortHandler.contentWidth() / deltaX).safeguard()
    val scaleY = (viewPortHandler.contentHeight() / deltaY).safeguard()

    // setup all matrices
    valueMatrix.reset()
    valueMatrix.postTranslate(-xChartMin, -yChartMin)
    valueMatrix.postScale(scaleX, -scaleY)
  }

  /**
   * Prepares the matrix that contains all offsets.
   *
   * @param inverted
   */
  open fun prepareMatrixOffset(inverted: Boolean) {
    offsetMatrix.reset()

    // offset.postTranslate(mOffsetLeft, getHeight() - mOffsetBottom);
    if (!inverted)
        offsetMatrix.postTranslate(
            viewPortHandler.offsetLeft(),
            viewPortHandler.chartHeight - viewPortHandler.offsetBottom())
    else {
      offsetMatrix.setTranslate(viewPortHandler.offsetLeft(), -viewPortHandler.offsetTop())
      offsetMatrix.postScale(1.0f, -1.0f)
    }
  }

  private var valuePointsForGenerateTransformedValuesScatter = FloatArray(1)

  /**
   * Transforms an List of Entry into a float array containing the x and y values transformed with
   * all matrices for the SCATTERCHART.
   *
   * @param data
   * @return
   */
  fun generateTransformedValuesScatter(
      data: IScatterDataSet,
      phaseX: Float,
      phaseY: Float,
      from: Int,
      to: Int
  ): FloatArray {
    val count = ((to - from) * phaseX + 1).toInt() * 2
    if (valuePointsForGenerateTransformedValuesScatter.size != count) {
      valuePointsForGenerateTransformedValuesScatter = FloatArray(count)
    }
    val valuePoints = valuePointsForGenerateTransformedValuesScatter
    var j = 0
    while (j < count) {
      val e = data.getEntryForIndex(j / 2 + from)
      if (e != null) {
        valuePoints[j] = e.x
        valuePoints[j + 1] = e.y * phaseY
      } else {
        valuePoints[j] = 0f
        valuePoints[j + 1] = 0f
      }
      j += 2
    }
    valueToPixelMatrix.mapPoints(valuePoints)
    return valuePoints
  }

  private var valuePointsForGenerateTransformedValuesBubble = FloatArray(1)

  /**
   * Transforms an List of Entry into a float array containing the x and y values transformed with
   * all matrices for the BUBBLECHART.
   *
   * @param data
   * @return
   */
  fun generateTransformedValuesBubble(
      data: IBubbleDataSet,
      phaseY: Float,
      from: Int,
      to: Int
  ): FloatArray {
    val count = (to - from + 1) * 2 // (int) Math.ceil((to - from) * phaseX) * 2;
    if (valuePointsForGenerateTransformedValuesBubble.size != count) {
      valuePointsForGenerateTransformedValuesBubble = FloatArray(count)
    }
    val valuePoints = valuePointsForGenerateTransformedValuesBubble
    var j = 0
    while (j < count) {
      val e: Entry = data.getEntryForIndex(j / 2 + from)
      if (e != null) {
        valuePoints[j] = e.x
        valuePoints[j + 1] = e.y * phaseY
      } else {
        valuePoints[j] = 0f
        valuePoints[j + 1] = 0f
      }
      j += 2
    }
    valueToPixelMatrix.mapPoints(valuePoints)
    return valuePoints
  }

  private var valuePointsForGenerateTransformedValuesLine = FloatArray(1)

  /**
   * Transforms an List of Entry into a float array containing the x and y values transformed with
   * all matrices for the LINECHART.
   *
   * @param data
   * @return
   */
  fun generateTransformedValuesLine(
      data: ILineDataSet,
      phaseX: Float,
      phaseY: Float,
      min: Int,
      max: Int
  ): FloatArray {
    val count = (((max - min) * phaseX).toInt() + 1) * 2
    if (valuePointsForGenerateTransformedValuesLine.size != count) {
      valuePointsForGenerateTransformedValuesLine = FloatArray(count)
    }
    val valuePoints = valuePointsForGenerateTransformedValuesLine
    var j = 0
    while (j < count) {
      val e = data.getEntryForIndex(j / 2 + min)
      if (e != null) {
        valuePoints[j] = e.x
        valuePoints[j + 1] = e.y * phaseY
      } else {
        valuePoints[j] = 0f
        valuePoints[j + 1] = 0f
      }
      j += 2
    }
    valueToPixelMatrix.mapPoints(valuePoints)
    return valuePoints
  }

  private var valuePointsForGenerateTransformedValuesCandle = FloatArray(1)

  /**
   * Transforms an List of Entry into a float array containing the x and y values transformed with
   * all matrices for the CANDLESTICKCHART.
   *
   * @param data
   * @return
   */
  fun generateTransformedValuesCandle(
      data: ICandleDataSet,
      phaseX: Float,
      phaseY: Float,
      from: Int,
      to: Int
  ): FloatArray {
    val count = ((to - from) * phaseX + 1).toInt() * 2
    if (valuePointsForGenerateTransformedValuesCandle.size != count) {
      valuePointsForGenerateTransformedValuesCandle = FloatArray(count)
    }
    val valuePoints = valuePointsForGenerateTransformedValuesCandle
    var j = 0
    while (j < count) {
      val e = data.getEntryForIndex(j / 2 + from)
      if (e != null) {
        valuePoints[j] = e.x
        valuePoints[j + 1] = e.high * phaseY
      } else {
        valuePoints[j] = 0f
        valuePoints[j + 1] = 0f
      }
      j += 2
    }
    valueToPixelMatrix.mapPoints(valuePoints)
    return valuePoints
  }

  /**
   * transform a path with all the given matrices VERY IMPORTANT: keep order to value-touch-offset
   *
   * @param path
   */
  fun pathValueToPixel(path: Path) {
    path.transform(valueMatrix)
    path.transform(viewPortHandler.matrixTouch)
    path.transform(offsetMatrix)
  }

  /**
   * Transforms multiple paths will all matrices.
   *
   * @param paths
   */
  fun pathValuesToPixel(paths: List<Path>) {
    for (i in paths.indices) {
      pathValueToPixel(paths[i])
    }
  }

  /**
   * Transform an array of points with all matrices. VERY IMPORTANT: Keep matrix order
   * "value-touch-offset" when transforming.
   *
   * @param pts
   */
  fun pointValuesToPixel(pts: FloatArray?) {
    valueMatrix.mapPoints(pts)
    viewPortHandler.matrixTouch.mapPoints(pts)
    offsetMatrix.mapPoints(pts)
  }

  /**
   * Transform a rectangle with all matrices.
   *
   * @param r
   */
  fun rectValueToPixel(r: RectF?) {
    valueMatrix.mapRect(r)
    viewPortHandler.matrixTouch.mapRect(r)
    offsetMatrix.mapRect(r)
  }

  /**
   * Transform a rectangle with all matrices with potential animation phases.
   *
   * @param r
   * @param phaseY
   */
  fun rectToPixelPhase(r: RectF, phaseY: Float) {

    // multiply the height of the rect with the phase
    r.top *= phaseY
    r.bottom *= phaseY
    valueMatrix.mapRect(r)
    viewPortHandler.matrixTouch.mapRect(r)
    offsetMatrix.mapRect(r)
  }

  fun rectToPixelPhaseHorizontal(r: RectF, phaseY: Float) {

    // multiply the height of the rect with the phase
    r.left *= phaseY
    r.right *= phaseY
    valueMatrix.mapRect(r)
    viewPortHandler.matrixTouch.mapRect(r)
    offsetMatrix.mapRect(r)
  }

  /**
   * Transform a rectangle with all matrices with potential animation phases.
   *
   * @param r
   */
  fun rectValueToPixelHorizontal(r: RectF?) {
    valueMatrix.mapRect(r)
    viewPortHandler.matrixTouch.mapRect(r)
    offsetMatrix.mapRect(r)
  }

  /**
   * Transform a rectangle with all matrices with potential animation phases.
   *
   * @param r
   * @param phaseY
   */
  fun rectValueToPixelHorizontal(r: RectF, phaseY: Float) {

    // multiply the height of the rect with the phase
    r.left *= phaseY
    r.right *= phaseY
    valueMatrix.mapRect(r)
    viewPortHandler.matrixTouch.mapRect(r)
    offsetMatrix.mapRect(r)
  }

  /**
   * transforms multiple rects with all matrices
   *
   * @param rects
   */
  fun rectValuesToPixel(rects: List<RectF?>) {
    val m = valueToPixelMatrix
    for (i in rects.indices) m.mapRect(rects[i])
  }

  protected var mPixelToValueMatrixBuffer = Matrix()

  /**
   * Transforms the given array of touch positions (pixels) (x, y, x, y, ...) into values on the
   * chart.
   *
   * @param pixels
   */
  fun pixelsToValue(pixels: FloatArray?) {
    val tmp = mPixelToValueMatrixBuffer
    tmp.reset()

    // invert all matrixes to convert back to the original value
    offsetMatrix.invert(tmp)
    tmp.mapPoints(pixels)
    viewPortHandler.matrixTouch.invert(tmp)
    tmp.mapPoints(pixels)
    valueMatrix.invert(tmp)
    tmp.mapPoints(pixels)
  }

  /** buffer for performance */
  var ptsBuffer = FloatArray(2)

  /**
   * Returns a recyclable MPPointD instance. returns the x and y values in the chart at the given
   * touch point (encapsulated in a MPPointD). This method transforms pixel coordinates to
   * coordinates / values in the chart. This is the opposite method to getPixelForValues(...).
   *
   * @param x
   * @param y
   * @return
   */
  fun getValuesByTouchPoint(x: Float, y: Float): MPPointD {
    val result = MPPointD.getInstance(0.0, 0.0)
    getValuesByTouchPoint(x, y, result)
    return result
  }

  fun getValuesByTouchPoint(x: Float, y: Float, outputPoint: MPPointD) {
    ptsBuffer[0] = x
    ptsBuffer[1] = y
    pixelsToValue(ptsBuffer)
    outputPoint.x = ptsBuffer[0].toDouble()
    outputPoint.y = ptsBuffer[1].toDouble()
  }

  /**
   * Returns a recyclable MPPointD instance. Returns the x and y coordinates (pixels) for a given x
   * and y value in the chart.
   *
   * @param x
   * @param y
   * @return
   */
  fun getPixelForValues(x: Float, y: Float): MPPointD {
    ptsBuffer[0] = x
    ptsBuffer[1] = y
    pointValuesToPixel(ptsBuffer)
    val xPx = ptsBuffer[0].toDouble()
    val yPx = ptsBuffer[1].toDouble()
    return MPPointD.getInstance(xPx, yPx)
  }

  private val mMBuffer1 = Matrix()

  private val valueToPixelMatrix: Matrix
    get() {
      mMBuffer1.set(valueMatrix)
      mMBuffer1.postConcat(viewPortHandler.matrixTouch)
      mMBuffer1.postConcat(offsetMatrix)
      return mMBuffer1
    }

  private val mMBuffer2 = Matrix()

  val pixelToValueMatrix: Matrix
    get() {
      valueToPixelMatrix.invert(mMBuffer2)
      return mMBuffer2
    }
}
