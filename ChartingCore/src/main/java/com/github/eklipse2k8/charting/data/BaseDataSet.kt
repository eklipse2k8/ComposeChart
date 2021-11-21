package com.github.eklipse2k8.charting.data

import android.content.Context
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Typeface
import androidx.annotation.RequiresApi
import com.github.eklipse2k8.charting.components.Legend.LegendForm
import com.github.eklipse2k8.charting.components.YAxis.AxisDependency
import com.github.eklipse2k8.charting.formatter.IValueFormatter
import com.github.eklipse2k8.charting.interfaces.datasets.IDataSet
import com.github.eklipse2k8.charting.utils.ColorTemplate.createColors
import com.github.eklipse2k8.charting.utils.MPPointF
import com.github.eklipse2k8.charting.utils.Utils.convertDpToPixel
import com.github.eklipse2k8.charting.utils.Utils.defaultValueFormatter
import kotlin.math.roundToInt

/**
 * Created by Philipp Jahoda on 21/10/15. This is the base dataset of all DataSets. It's purpose is
 * to implement critical methods provided by the IDataSet interface.
 */
abstract class BaseDataSet<E : Entry>(
    /** label that describes the DataSet or the data the DataSet represents */
    override var label: String? = "DataSet",
) : IDataSet<E> {

  /**
   * Sets the colors that should be used fore this DataSet. Colors are reused as soon as the number
   * of Entries the DataSet represents is higher than the size of the colors array. If you are using
   * colors from the resources, make sure that the colors are already prepared (by calling
   * getResources().getColor(...)) before adding them to the DataSet.
   *
   * @param colors
   */
  override var colors: MutableList<Int> = mutableListOf()

  /** List representing all colors that are used for drawing the actual values for this DataSet */
  var valueColors: MutableList<Int> = mutableListOf()
    protected set

  /** this specifies which axis this DataSet should be plotted against */
  override var axisDependency: AxisDependency = AxisDependency.LEFT

  /** if true, value highlightning is enabled */
  override var isHighlightEnabled = true

  /** custom formatter that is used instead of the auto-formatter if set */
  @Transient protected var mValueFormatter: IValueFormatter? = null

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

  /** the offset for drawing icons (in dp) */
  protected var mIconsOffset = MPPointF()

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

  /**
   * Sets the colors that should be used fore this DataSet. Colors are reused as soon as the number
   * of Entries the DataSet represents is higher than the size of the colors array. If you are using
   * colors from the resources, make sure that the colors are already prepared (by calling
   * getResources().getColor(...)) before adding them to the DataSet.
   *
   * @param colors
   */
  fun setColors(vararg colorArgs: Int) {
    resetColors()
    colors.addAll(createColors(colorArgs))
  }

  /**
   * Sets the colors that should be used fore this DataSet. Colors are reused as soon as the number
   * of Entries the DataSet represents is higher than the size of the colors array. You can use "new
   * int[] { R.color.red, R.color.green, ... }" to provide colors for this method. Internally, the
   * colors are resolved using getResources().getColor(...)
   *
   * @param colors
   */
  @RequiresApi(23)
  fun setColors(colorList: IntArray, context: Context) {
    colors.clear()
    colors.addAll(colorList.map { context.resources.getColor(it, context.theme) })
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
  fun setColor(colorInt: Int, alpha: Int) {
    color = Color.argb(alpha, Color.red(colorInt), Color.green(colorInt), Color.blue(colorInt))
  }

  /**
   * Sets colors with a specific alpha value.
   *
   * @param colors
   * @param alpha
   */
  fun setColors(colorList: IntArray, alpha: Int) {
    resetColors()
    colors.addAll(
        colorList.map { Color.argb(alpha, Color.red(it), Color.green(it), Color.blue(it)) })
  }

  /** Resets all colors of this DataSet and recreates the colors array. */
  fun resetColors() {
    colors.clear()
  }

  override var valueFormatter: IValueFormatter?
    get() = if (needsFormatter()) defaultValueFormatter else mValueFormatter
    set(f) {
      mValueFormatter = (f ?: return)
    }

  override fun needsFormatter(): Boolean {
    return mValueFormatter == null
  }

  override fun setValueTextColors(colors: List<Int>) {
    valueColors.clear()
    valueColors.addAll(colors)
  }

  override var valueTextColor: Int
    get() = valueColors[0]
    set(color) {
      valueColors.clear()
      valueColors.add(color)
    }

  override fun getValueTextColor(index: Int): Int {
    return valueColors[index % valueColors.size]
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

  override var iconsOffset: MPPointF?
    get() = mIconsOffset
    set(offsetDp) {
      mIconsOffset.x = offsetDp?.x ?: 0f
      mIconsOffset.y = offsetDp?.y ?: 0f
    }

  override fun getIndexInEntries(xIndex: Int): Int {
    for (i in 0 until entryCount) {
      if (xIndex == getEntryForIndex(i).x.roundToInt()) return i
    }
    return -1
  }

  override fun removeFirst(): Boolean {
    return if (entryCount > 0) {
      val entry = getEntryForIndex(0)
      removeEntry(entry)
    } else false
  }

  override fun removeLast(): Boolean {
    return if (entryCount > 0) {
      val e = getEntryForIndex(entryCount - 1)
      removeEntry(e)
    } else false
  }

  override fun removeEntryByXValue(xValue: Float): Boolean {
    val e = getEntryForXValue(xValue, Float.NaN)
    return e?.let { removeEntry(it) } ?: false
  }

  override fun removeEntry(index: Int): Boolean {
    val e = getEntryForIndex(index)
    return removeEntry(e)
  }

  override fun contains(entry: E): Boolean {
    for (i in 0 until entryCount) {
      if (getEntryForIndex(i) == entry) return true
    }
    return false
  }

  protected fun copyTo(baseDataSet: BaseDataSet<*>) {
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
    // default color
    colors.add(Color.rgb(140, 234, 255))
    valueColors.add(Color.BLACK)
  }
}
