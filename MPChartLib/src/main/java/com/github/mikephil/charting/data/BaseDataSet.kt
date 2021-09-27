package com.github.mikephil.charting.data

import android.content.Context
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Typeface
import com.github.mikephil.charting.components.Legend.LegendForm
import com.github.mikephil.charting.components.YAxis.AxisDependency
import com.github.mikephil.charting.formatter.IValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IDataSet
import com.github.mikephil.charting.utils.ColorTemplate.createColors
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.Utils.convertDpToPixel
import com.github.mikephil.charting.utils.Utils.defaultValueFormatter
import java.util.ArrayList

/**
 * Created by Philipp Jahoda on 21/10/15. This is the base dataset of all DataSets. It's purpose is
 * to implement critical methods provided by the IDataSet interface.
 */
abstract class BaseDataSet<E : Entry>(
    /** label that describes the DataSet or the data the DataSet represents */
    override var label: String? = "DataSet"
) : IDataSet<E> {
  /** ###### ###### COLOR GETTING RELATED METHODS ##### ###### */
  /**
   * Sets the colors that should be used fore this DataSet. Colors are reused as soon as the number
   * of Entries the DataSet represents is higher than the size of the colors array. If you are using
   * colors from the resources, make sure that the colors are already prepared (by calling
   * getResources().getColor(...)) before adding them to the DataSet.
   *
   * @param colors
   */
  /** List representing all colors that are used for this DataSet */
  override val colors: MutableList<Int> = mutableListOf()

  /** List representing all colors that are used for drawing the actual values for this DataSet */
  var valueColors: List<Int>? = null
    protected set

  /** ###### ###### OTHER STYLING RELATED METHODS ##### ###### */

  /** this specifies which axis this DataSet should be plotted against */
  override var axisDependency = AxisDependency.LEFT

  /** if true, value highlightning is enabled */
  override var isHighlightEnabled = true

  /** the typeface used for the value text */
  override var valueTypeface: Typeface? = null
  override var form = LegendForm.DEFAULT
  override var formSize = Float.NaN
  override var formLineWidth = Float.NaN
  override var formLineDashEffect: DashPathEffect? = null

  /** if true, y-values are drawn on the chart */
  override var isDrawValuesEnabled = true
    protected set

  /** if true, y-icons are drawn on the chart */
  override var isDrawIconsEnabled = true
    protected set

  /** the size of the value-text labels */
  protected var mValueTextSize = 17f

  /** flag that indicates if the DataSet is visible or not */
  override var isVisible = true

  /** Use this method to tell the data set that the underlying data has changed. */
  fun notifyDataSetChanged() {
    calcMinMax()
  }

  /**
   * Sets the one and ONLY color that should be used for this DataSet. Internally, this recreates
   * the colors array and adds the specified color.
   *
   * @param color
   */
  override var color: Int
    get() = colors[0]
    set(color) {
      resetColors()
      colors.add(color)
    }

  override fun getColor(index: Int): Int {
    return colors[index % colors.size]
  }

  /** ###### ###### COLOR SETTING RELATED METHODS ##### ###### */

  /**
   * Sets the colors that should be used fore this DataSet. Colors are reused as soon as the number
   * of Entries the DataSet represents is higher than the size of the colors array. If you are using
   * colors from the resources, make sure that the colors are already prepared (by calling
   * getResources().getColor(...)) before adding them to the DataSet.
   *
   * @param colorArgs
   */
  fun setColors(vararg colorArgs: Int) {
    colors.clear()
    colors.addAll(createColors(colorArgs))
  }

  /**
   * Sets the colors that should be used fore this DataSet. Colors are reused as soon as the number
   * of Entries the DataSet represents is higher than the size of the colors array. You can use "new
   * int[] { R.color.red, R.color.green, ... }" to provide colors for this method. Internally, the
   * colors are resolved using getResources().getColor(...)
   *
   * @param colorArray
   */
  fun setColors(colorArray: IntArray, c: Context) {
    colors.clear()
    colors.addAll(createColors(c, colorArray))
  }

  /**
   * Adds a new color to the colors array of the DataSet.
   *
   * @param color
   */
  fun addColor(color: Int) {
    colors.add(color)
  }

  /**
   * Sets a color with a specific alpha value.
   *
   * @param color
   * @param alpha from 0-255
   */
  fun setColor(value: Int, alpha: Int) {
    color = Color.argb(alpha, Color.red(value), Color.green(value), Color.blue(value))
  }

  /**
   * Sets colors with a specific alpha value.
   *
   * @param colors
   * @param alpha
   */
  fun setColors(colors: IntArray, alpha: Int) {
    resetColors()
    for (color in colors) {
      addColor(Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color)))
    }
  }

  /** Resets all colors of this DataSet and recreates the colors array. */
  fun resetColors() {
    colors.clear()
  }

  /** custom formatter that is used instead of the auto-formatter if set */
  @Transient
  override var valueFormatter: IValueFormatter? = null
    get() = if (field == null || needsFormatter()) defaultValueFormatter else field

  override fun needsFormatter(): Boolean = false

  override fun setValueTextColors(colors: List<Int>?) {
    valueColors = colors
  }

  override var valueTextColor: Int?
    get() = valueColors?.let { it[0] }
    set(color) {
      valueColors = color?.let { listOf(it) }
    }

  override fun getValueTextColor(index: Int): Int? {
    return valueColors?.let { it[index % it.size] }
  }

  override var valueTextSize: Float
    get() = mValueTextSize
    set(size) {
      mValueTextSize = convertDpToPixel(size)
    }

  override fun setDrawValues(enabled: Boolean) {
    isDrawValuesEnabled = enabled
  }

  override fun setDrawIcons(enabled: Boolean) {
    isDrawIconsEnabled = enabled
  }

  /** the offset for drawing icons (in dp) */
  override var iconsOffset: MPPointF? = MPPointF()
    set(value) {
      if (field == null) {
        field = MPPointF()
      }
      field?.x = value?.x
      field?.y = value?.y
    }

  /** ###### ###### DATA RELATED METHODS ###### ###### */
  override fun getIndexInEntries(xIndex: Int): Int {
    for (i in 0 until entryCount) {
      if (xIndex == getEntryForIndex(i)!!.x) return i
    }
    return -1
  }

  override fun removeFirst(): Boolean {
    return if (entryCount > 0) {
      val entry: E? = getEntryForIndex(0)
      removeEntry(entry)
    } else false
  }

  override fun removeLast(): Boolean {
    return if (entryCount > 0) {
      val e: E? = getEntryForIndex(entryCount - 1)
      removeEntry(e)
    } else false
  }

  override fun removeEntryByXValue(xValue: Float): Boolean {
    val e: E? = getEntryForXValue(xValue, Float.NaN)
    return removeEntry(e)
  }

  override fun removeEntry(index: Int): Boolean {
    val e: E? = getEntryForIndex(index)
    return removeEntry(e)
  }

  override fun contains(e: E): Boolean {
    for (i in 0 until entryCount) {
      if (getEntryForIndex(i) == e) return true
    }
    return false
  }

  protected fun copy(baseDataSet: IDataSet<*>) {
    baseDataSet.axisDependency = axisDependency
    baseDataSet.colors = colors
    baseDataSet.isDrawIconsEnabled = isDrawIconsEnabled
    baseDataSet.isDrawValuesEnabled = isDrawValuesEnabled
    baseDataSet.form = form
    baseDataSet.formLineDashEffect = formLineDashEffect
    baseDataSet.formLineWidth = formLineWidth
    baseDataSet.formSize = formSize
    baseDataSet.isHighlightEnabled = isHighlightEnabled
    baseDataSet.mIconsOffset = mIconsOffset
    baseDataSet.valueColors = valueColors
    baseDataSet.mValueFormatter = mValueFormatter
    baseDataSet.valueColors = valueColors
    baseDataSet.mValueTextSize = mValueTextSize
    baseDataSet.isVisible = isVisible
  }

  /** Default constructor. */
  init {
    colors = ArrayList()
    valueColors = ArrayList()

    // default color
    colors.add(Color.rgb(140, 234, 255))
    valueColors.add(Color.BLACK)
  }
}
