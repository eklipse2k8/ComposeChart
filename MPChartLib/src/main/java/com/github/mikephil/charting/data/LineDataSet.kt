package com.github.mikephil.charting.data

import android.content.Context
import android.graphics.Color
import android.graphics.DashPathEffect
import android.util.Log
import com.github.mikephil.charting.formatter.DefaultFillFormatter
import com.github.mikephil.charting.formatter.IFillFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.Utils

class LineDataSet(yVals: List<Entry>, label: String?) :
    LineRadarDataSet<Entry>(yVals, label), ILineDataSet {

  /** Drawing mode for this line dataset */
  override var mode = Mode.LINEAR

  /** List representing all colors that are used for the circles */
  private var mCircleColors: MutableList<Int>? = null

  /** the color of the inner circles */
  override var circleHoleColor = Color.WHITE

  /** the radius of the circle-shaped value indicators */
  private var mCircleRadius = 8f

  /** the hole radius of the circle-shaped value indicators */
  private var mCircleHoleRadius = 4f

  /** sets the intensity of the cubic lines */
  private var mCubicIntensity = 0.2f

  /** the path effect of this DataSet that makes dashed lines possible */
  override var dashPathEffect: DashPathEffect? = null
    private set

  /** formatter for customizing the position of the fill-line */
  private var mFillFormatter: IFillFormatter = DefaultFillFormatter()

  /** if true, drawing circles is enabled */
  override var isDrawCirclesEnabled = true
    private set

  override var isDrawCircleHoleEnabled = true
    private set

  override fun copy(): DataSet<Entry> {
    val entries = mutableListOf<Entry>()
    mEntries.forEach { entry -> entries.add(entry.copy()!!) }
    val copied = LineDataSet(entries, label)
    copy(copied)
    return copied
  }

  private fun copy(lineDataSet: LineDataSet) {
    super.copy(lineDataSet)
    lineDataSet.mCircleColors = mCircleColors
    lineDataSet.circleHoleColor = circleHoleColor
    lineDataSet.mCircleHoleRadius = mCircleHoleRadius
    lineDataSet.mCircleRadius = mCircleRadius
    lineDataSet.mCubicIntensity = mCubicIntensity
    lineDataSet.dashPathEffect = dashPathEffect
    lineDataSet.isDrawCircleHoleEnabled = isDrawCircleHoleEnabled
    lineDataSet.isDrawCirclesEnabled = isDrawCircleHoleEnabled
    lineDataSet.mFillFormatter = mFillFormatter
    lineDataSet.mode = mode
  }

  /**
   * Sets the intensity for cubic lines (if enabled). Max = 1f = very cubic, Min = 0.05f = low cubic
   * effect, Default: 0.2f
   *
   * @param intensity
   */
  override var cubicIntensity: Float
    get() = mCubicIntensity
    set(intensity) {
      var intensity = intensity
      if (intensity > 1f) intensity = 1f
      if (intensity < 0.05f) intensity = 0.05f
      mCubicIntensity = intensity
    }

  /**
   * Sets the radius of the drawn circles. Default radius = 4f, Min = 1f
   *
   * @param radius
   */
  override var circleRadius: Float
    get() = mCircleRadius
    set(radius) {
      if (radius >= 1f) {
        mCircleRadius = Utils.convertDpToPixel(radius)
      } else {
        Log.e("LineDataSet", "Circle radius cannot be < 1")
      }
    }

  /**
   * Sets the hole radius of the drawn circles. Default radius = 2f, Min = 0.5f
   *
   * @param holeRadius
   */
  override var circleHoleRadius: Float
    get() = mCircleHoleRadius
    set(holeRadius) {
      if (holeRadius >= 0.5f) {
        mCircleHoleRadius = Utils.convertDpToPixel(holeRadius)
      } else {
        Log.e("LineDataSet", "Circle radius cannot be < 0.5")
      }
    }
  /** This function is deprecated because of unclarity. Use getCircleRadius instead. */
  /**
   * sets the size (radius) of the circle shpaed value indicators, default size = 4f
   *
   * This method is deprecated because of unclarity. Use setCircleRadius instead.
   *
   * @param size
   */
  @get:Deprecated("")
  @set:Deprecated("")
  var circleSize: Float
    get() = circleRadius
    set(size) {
      circleRadius = size
    }

  /**
   * Enables the line to be drawn in dashed mode, e.g. like this "- - - - - -". THIS ONLY WORKS IF
   * HARDWARE-ACCELERATION IS TURNED OFF. Keep in mind that hardware acceleration boosts
   * performance.
   *
   * @param lineLength the length of the line pieces
   * @param spaceLength the length of space in between the pieces
   * @param phase offset, in degrees (normally, use 0)
   */
  fun enableDashedLine(lineLength: Float, spaceLength: Float, phase: Float) {
    dashPathEffect = DashPathEffect(floatArrayOf(lineLength, spaceLength), phase)
  }

  /** Disables the line to be drawn in dashed mode. */
  fun disableDashedLine() {
    dashPathEffect = null
  }

  override val isDashedLineEnabled: Boolean
    get() = if (dashPathEffect == null) false else true

  /**
   * set this to true to enable the drawing of circle indicators for this DataSet, default true
   *
   * @param enabled
   */
  fun setDrawCircles(enabled: Boolean) {
    isDrawCirclesEnabled = enabled
  }

  @get:Deprecated("")
  override val isDrawCubicEnabled: Boolean
    get() = mode == Mode.CUBIC_BEZIER

  @get:Deprecated("")
  override val isDrawSteppedEnabled: Boolean
    get() = mode == Mode.STEPPED
  /** ALL CODE BELOW RELATED TO CIRCLE-COLORS */
  /**
   * returns all colors specified for the circles
   *
   * @return
   */
  val circleColors: List<Int>?
    get() = mCircleColors

  override fun getCircleColor(index: Int): Int {
    return mCircleColors!![index]
  }

  override val circleColorCount: Int
    get() = mCircleColors!!.size

  /**
   * Sets the colors that should be used for the circles of this DataSet. Colors are reused as soon
   * as the number of Entries the DataSet represents is higher than the size of the colors array.
   * Make sure that the colors are already prepared (by calling getResources().getColor(...)) before
   * adding them to the DataSet.
   *
   * @param colors
   */
  fun setCircleColors(colors: MutableList<Int>?) {
    mCircleColors = colors
  }

  /**
   * Sets the colors that should be used for the circles of this DataSet. Colors are reused as soon
   * as the number of Entries the DataSet represents is higher than the size of the colors array.
   * Make sure that the colors are already prepared (by calling getResources().getColor(...)) before
   * adding them to the DataSet.
   *
   * @param colors
   */
  fun setCircleColors(vararg colors: Int) {
    mCircleColors = ColorTemplate.createColors(colors).toMutableList()
  }

  /**
   * ets the colors that should be used for the circles of this DataSet. Colors are reused as soon
   * as the number of Entries the DataSet represents is higher than the size of the colors array.
   * You can use "new String[] { R.color.red, R.color.green, ... }" to provide colors for this
   * method. Internally, the colors are resolved using getResources().getColor(...)
   *
   * @param colors
   */
  fun setCircleColors(colors: IntArray, c: Context) {
    var clrs = mCircleColors
    if (clrs == null) {
      clrs = ArrayList()
    }
    clrs.clear()
    for (color in colors) {
      clrs.add(c.resources.getColor(color))
    }
    mCircleColors = clrs
  }

  /**
   * Sets the one and ONLY color that should be used for this DataSet. Internally, this recreates
   * the colors array and adds the specified color.
   *
   * @param color
   */
  fun setCircleColor(color: Int) {
    resetCircleColors()
    mCircleColors!!.add(color)
  }

  /** resets the circle-colors array and creates a new one */
  fun resetCircleColors() {
    if (mCircleColors == null) {
      mCircleColors = ArrayList()
    }
    mCircleColors!!.clear()
  }

  /**
   * Set this to true to allow drawing a hole in each data circle.
   *
   * @param enabled
   */
  fun setDrawCircleHole(enabled: Boolean) {
    isDrawCircleHoleEnabled = enabled
  }

  /**
   * Sets a custom IFillFormatter to the chart that handles the position of the filled-line for each
   * DataSet. Set this to null to use the default logic.
   *
   * @param formatter
   */
  override var fillFormatter: IFillFormatter?
    get() = mFillFormatter
    set(formatter) {
      mFillFormatter = formatter ?: DefaultFillFormatter()
    }

  enum class Mode {
    LINEAR,
    STEPPED,
    CUBIC_BEZIER,
    HORIZONTAL_BEZIER
  }

  init {
    if (mCircleColors == null) {
      mCircleColors = ArrayList()
    }
    mCircleColors!!.clear()
    mCircleColors!!.add(Color.rgb(140, 234, 255))
  }
}
