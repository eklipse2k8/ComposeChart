package com.github.mikephil.charting.data

import android.graphics.Typeface
import android.util.Log
import com.github.mikephil.charting.components.YAxis.AxisDependency
import com.github.mikephil.charting.formatter.IValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.IDataSet

/**
 * Class that holds all relevant data that represents the chart. That involves at least one (or
 * more) DataSets, and an array of x-values.
 *
 * @param sets the dataset array
 *
 * @author Philipp Jahoda
 */
abstract class ChartData<T, E>
@JvmOverloads
constructor(protected val mutableDataSets: MutableList<T> = mutableListOf()) where
T : IDataSet<E>,
E : Entry {

  /** maximum y-value in the value array across all axes */
  var yMax = -Float.MAX_VALUE
    protected set

  /** the minimum y-value in the value array across all axes */
  var yMin = Float.MAX_VALUE
    protected set

  /** maximum x-value in the value array */
  var xMax = -Float.MAX_VALUE
    protected set

  /** minimum x-value in the value array */
  var xMin = Float.MAX_VALUE
    protected set

  protected var mLeftAxisMax = -Float.MAX_VALUE

  protected var mLeftAxisMin = Float.MAX_VALUE

  protected var mRightAxisMax = -Float.MAX_VALUE

  protected var mRightAxisMin = Float.MAX_VALUE

  /** Returns the total entry count across all DataSet objects this data object contains. */
  val entryCount: Int
    get() = this.dataSets.sumOf { it.entryCount }

  /**
   * Returns the DataSet object with the maximum number of entries or null if there are no DataSets.
   */
  val maxEntryCountSet: T?
    get() {
      var max: T = mutableDataSets.firstOrNull() ?: return null
      mutableDataSets.forEach { set -> if (set.entryCount > max.entryCount) max = set }
      return max
    }

  /** Returns all DataSet objects this ChartData object holds. */
  open val dataSets: List<T>
    get() = mutableDataSets.toList()

  /** returns the number of LineDataSets this object contains */
  val dataSetCount: Int
    get() = mutableDataSets.size

  /** Returns the labels of all DataSets as a string array. */
  val dataSetLabels: Array<String?>
    get() {
      val types = arrayOfNulls<String>(dataSetCount)
      mutableDataSets.forEachIndexed { i, set -> types[i] = set.label }
      return types
    }

  /**
   * Constructor taking single or multiple DataSet objects.
   *
   * @param dataSets
   */
  constructor(vararg dataSets: T) : this(dataSets.toMutableList())

  init {
    notifyDataChanged()
  }

  /**
   * Call this method to let the ChartData know that the underlying data has changed. Calling this
   * performs all necessary recalculations needed when the contained data has changed.
   */
  open fun notifyDataChanged() {
    calcMinMax()
  }

  /**
   * Calc minimum and maximum y-values over all DataSets. Tell DataSets to recalculate their min and
   * max y-values, this is only needed for autoScaleMinMax.
   *
   * @param fromX the x-value to start the calculation from
   * @param toX the x-value to which the calculation should be performed
   */
  fun calcMinMaxY(fromX: Float, toX: Float) {
    mutableDataSets.forEach { set -> set.calcMinMaxY(fromX, toX) }
    // apply the new data
    calcMinMax()
  }

  /** Calc minimum and maximum values (both x and y) over all DataSets. */
  open fun calcMinMax() {
    yMax = -Float.MAX_VALUE
    yMin = Float.MAX_VALUE
    xMax = -Float.MAX_VALUE
    xMin = Float.MAX_VALUE

    mutableDataSets.forEach { set -> calcMinMax(set) }

    mLeftAxisMax = -Float.MAX_VALUE
    mLeftAxisMin = Float.MAX_VALUE
    mRightAxisMax = -Float.MAX_VALUE
    mRightAxisMin = Float.MAX_VALUE

    // left axis
    val firstLeft = getFirstLeft(mutableDataSets)

    if (firstLeft != null) {
      mLeftAxisMax = firstLeft.yMax
      mLeftAxisMin = firstLeft.yMin
      mutableDataSets.forEach { dataSet ->
        if (dataSet.axisDependency === AxisDependency.LEFT) {
          if (dataSet.yMin < mLeftAxisMin) mLeftAxisMin = dataSet.yMin
          if (dataSet.yMax > mLeftAxisMax) mLeftAxisMax = dataSet.yMax
        }
      }
    }

    // right axis
    val firstRight = getFirstRight(mutableDataSets)

    if (firstRight != null) {
      mRightAxisMax = firstRight.yMax
      mRightAxisMin = firstRight.yMin
      mutableDataSets.forEach { dataSet ->
        if (dataSet.axisDependency === AxisDependency.RIGHT) {
          if (dataSet.yMin < mRightAxisMin) mRightAxisMin = dataSet.yMin
          if (dataSet.yMax > mRightAxisMax) mRightAxisMax = dataSet.yMax
        }
      }
    }
  }

  /**
   * Returns the minimum y-value for the specified axis.
   *
   * @param axis
   * @return
   */
  fun getYMin(axis: AxisDependency): Float {
    return if (axis === AxisDependency.LEFT) {
      if (mLeftAxisMin == Float.MAX_VALUE) {
        mRightAxisMin
      } else mLeftAxisMin
    } else {
      if (mRightAxisMin == Float.MAX_VALUE) {
        mLeftAxisMin
      } else mRightAxisMin
    }
  }

  /**
   * Returns the maximum y-value for the specified axis.
   *
   * @param axis
   * @return
   */
  fun getYMax(axis: AxisDependency): Float {
    return if (axis === AxisDependency.LEFT) {
      if (mLeftAxisMax == -Float.MAX_VALUE) {
        mRightAxisMax
      } else mLeftAxisMax
    } else {
      if (mRightAxisMax == -Float.MAX_VALUE) {
        mLeftAxisMax
      } else mRightAxisMax
    }
  }

  /**
   * Retrieve the index of a DataSet with a specific label from the ChartData. Search can be case
   * sensitive or not. IMPORTANT: This method does calculations at runtime, do not over-use in
   * performance critical situations.
   *
   * @param dataSets the DataSet array to search
   * @param label
   * @param ignorecase if true, the search is not case-sensitive
   * @return
   */
  private fun getDataSetIndexByLabel(dataSets: List<T>, label: String, ignorecase: Boolean): Int {
    if (ignorecase) {
      for (i in dataSets.indices) if (label.equals(dataSets[i].label, ignoreCase = true)) return i
    } else {
      for (i in dataSets.indices) if (label == dataSets[i].label) return i
    }
    return -1
  }

  /**
   * Get the Entry for a corresponding highlight object
   *
   * @param highlight
   * @return the entry that is highlighted
   */
  open fun getEntryForHighlight(highlight: Highlight): E? =
      this.dataSets.let {
        if (highlight.dataSetIndex < it.size) {
          it[highlight.dataSetIndex].getEntryForXValue(highlight.x, highlight.y)
        } else {
          null
        }
      }

  /**
   * Returns the DataSet object with the given label. Search can be case sensitive or not.
   * IMPORTANT: This method does calculations at runtime. Use with care in performance critical
   * situations.
   *
   * @param label
   * @param ignorecase
   * @return
   */
  open fun getDataSetByLabel(label: String, ignorecase: Boolean): T? {
    val index = getDataSetIndexByLabel(mutableDataSets, label, ignorecase)
    return if (index < 0 || index >= dataSetCount) null else mutableDataSets[index]
  }

  open fun getDataSetByIndex(index: Int): T? {
    return if (index < 0 || index >= dataSetCount) null else mutableDataSets[index]
  }

  /**
   * Adds a DataSet dynamically.
   *
   * @param dataSet
   */
  fun addDataSet(dataSet: T) {
    calcMinMax(dataSet)
    mutableDataSets.add(dataSet)
  }

  /**
   * Removes the given DataSet from this data object. Also recalculates all minimum and maximum
   * values. Returns true if a DataSet was removed, false if no DataSet could be removed.
   *
   * @param dataSet
   */
  open fun removeDataSet(dataSet: T): Boolean {
    val removed = mutableDataSets.remove(dataSet)
    // if a DataSet was removed
    if (removed) {
      notifyDataChanged()
    }
    return removed
  }

  /**
   * Removes the DataSet at the given index in the DataSet array from the data object. Also
   * recalculates all minimum and maximum values. Returns true if a DataSet was removed, false if no
   * DataSet could be removed.
   *
   * @param index
   */
  open fun removeDataSet(index: Int): Boolean {
    if (index >= this.dataSets.size || index < 0) return false
    val set = this.dataSets[index]
    return removeDataSet(set)
  }

  /**
   * Adds an Entry to the DataSet at the specified index. Entries are added to the end of the list.
   *
   * @param entry
   * @param dataSetIndex
   */
  fun addEntry(entry: E, dataSetIndex: Int) {
    if (dataSetIndex in 0 until dataSetCount) {
      val set = mutableDataSets[dataSetIndex]
      // add the entry to the dataset
      if (!set.addEntry(entry)) return
      calcMinMax(entry, set.axisDependency)
    } else {
      Log.e("addEntry", "Cannot add Entry because dataSetIndex too high or too low.")
    }
  }

  /**
   * Adjusts the current minimum and maximum values based on the provided Entry object.
   *
   * @param entry
   * @param axis
   */
  protected fun calcMinMax(entry: E, axis: AxisDependency?) {
    if (yMax < entry.y) yMax = entry.y
    if (yMin > entry.y) yMin = entry.y
    if (xMax < entry.x) xMax = entry.x
    if (xMin > entry.x) xMin = entry.x
    if (axis === AxisDependency.LEFT) {
      if (mLeftAxisMax < entry.y) mLeftAxisMax = entry.y
      if (mLeftAxisMin > entry.y) mLeftAxisMin = entry.y
    } else {
      if (mRightAxisMax < entry.y) mRightAxisMax = entry.y
      if (mRightAxisMin > entry.y) mRightAxisMin = entry.y
    }
  }

  /**
   * Adjusts the minimum and maximum values based on the given DataSet.
   *
   * @param dataSet
   */
  protected fun calcMinMax(dataSet: T) {
    if (yMax < dataSet.yMax) yMax = dataSet.yMax
    if (yMin > dataSet.yMin) yMin = dataSet.yMin
    if (xMax < dataSet.xMax) xMax = dataSet.xMax
    if (xMin > dataSet.xMin) xMin = dataSet.xMin
    if (dataSet.axisDependency === AxisDependency.LEFT) {
      if (mLeftAxisMax < dataSet.yMax) mLeftAxisMax = dataSet.yMax
      if (mLeftAxisMin > dataSet.yMin) mLeftAxisMin = dataSet.yMin
    } else {
      if (mRightAxisMax < dataSet.yMax) mRightAxisMax = dataSet.yMax
      if (mRightAxisMin > dataSet.yMin) mRightAxisMin = dataSet.yMin
    }
  }

  /**
   * Removes the given Entry object from the DataSet at the specified index.
   *
   * @param entry
   * @param dataSetIndex
   */
  open fun removeEntry(entry: E, dataSetIndex: Int): Boolean {
    if (dataSetIndex >= dataSetCount) return false
    val set = mutableDataSets[dataSetIndex]
    // remove the entry from the dataset
    return set.removeEntry(entry).apply {
      if (this) {
        notifyDataChanged()
      }
    }
  }

  /**
   * Removes the Entry object closest to the given DataSet at the specified index. Returns true if
   * an Entry was removed, false if no Entry was found that meets the specified requirements.
   *
   * @param xValue
   * @param dataSetIndex
   * @return
   */
  open fun removeEntry(xValue: Float, dataSetIndex: Int): Boolean {
    if (dataSetIndex >= dataSetCount) return false
    val dataSet = mutableDataSets[dataSetIndex]
    val e = dataSet.getEntryForXValue(xValue, Float.NaN) ?: return false
    return removeEntry(e, dataSetIndex)
  }

  /**
   * Returns the DataSet that contains the provided Entry, or null, if no DataSet contains this
   * Entry.
   *
   * @param entry
   * @return
   */
  fun getDataSetForEntry(entry: E): T? {
    mutableDataSets.forEach { set ->
      for (j in 0 until set.entryCount) {
        if (entry == set.getEntryForXValue(entry.x, entry.y)) return set
      }
    }
    return null
  }

  /**
   * Returns all colors used across all DataSet objects this object represents.
   *
   * @return
   */
  val colors: IntArray
    get() {
      var clrcnt = 0
      for (i in this.dataSets.indices) {
        clrcnt += this.dataSets[i].colors.size
      }
      val colors = IntArray(clrcnt)
      var cnt = 0
      for (i in this.dataSets.indices) {
        val clrs = this.dataSets[i].colors
        for (clr in clrs) {
          colors[cnt] = clr
          cnt++
        }
      }
      return colors
    }

  /**
   * Returns the index of the provided DataSet in the DataSet array of this data object, or -1 if it
   * does not exist.
   *
   * @param dataSet
   * @return
   */
  fun getIndexOfDataSet(dataSet: T): Int = mutableDataSets.indexOf(dataSet)

  private fun getFirstAxis(sets: List<T>, dependency: AxisDependency): T? =
      sets.firstOrNull { it.axisDependency === dependency }

  /**
   * Returns the first DataSet from the datasets-array that has it's dependency on the left axis.
   * Returns null if no DataSet with left dependency could be found.
   *
   * @return
   */
  fun getFirstLeft(sets: List<T>): T? = getFirstAxis(sets, AxisDependency.LEFT)

  /**
   * Returns the first DataSet from the datasets-array that has it's dependency on the right axis.
   * Returns null if no DataSet with right dependency could be found.
   *
   * @return
   */
  fun getFirstRight(sets: List<T>): T? = getFirstAxis(sets, AxisDependency.RIGHT)

  /**
   * Sets a custom IValueFormatter for all DataSets this data object contains.
   *
   * @param f
   */
  fun setValueFormatter(f: IValueFormatter?) {
    if (f == null) return
    this.dataSets.forEach { it.valueFormatter = f }
  }

  /**
   * Sets the color of the value-text (color in which the value-labels are drawn) for all DataSets
   * this data object contains.
   *
   * @param color
   */
  fun setValueTextColor(color: Int) {
    this.dataSets.forEach { it.valueTextColor = color }
  }

  /**
   * Sets the same list of value-colors for all DataSets this data object contains.
   *
   * @param colors
   */
  fun setValueTextColors(colors: List<Int>) {
    this.dataSets.forEach { it.setValueTextColors(colors) }
  }

  /**
   * Sets the Typeface for all value-labels for all DataSets this data object contains.
   *
   * @param tf
   */
  fun setValueTypeface(tf: Typeface?) {
    this.dataSets.forEach { it.valueTypeface = tf }
  }

  /**
   * Sets the size (in dp) of the value-text for all DataSets this data object contains.
   *
   * @param size
   */
  fun setValueTextSize(size: Float) {
    this.dataSets.forEach { it.valueTextSize = size }
  }

  /**
   * Enables / disables drawing values (value-text) for all DataSets this data object contains.
   *
   * @param enabled
   */
  fun setDrawValues(enabled: Boolean) {
    this.dataSets.forEach { it.setDrawValues(enabled) }
  }

  /**
   * Enables / disables highlighting values for all DataSets this data object contains. If set to
   * true, this means that values can be highlighted programmatically or by touch gesture.
   */
  var isHighlightEnabled: Boolean
    get() {
      val first = this.dataSets.firstOrNull { !it.isHighlightEnabled }
      if (first != null) {
        return false
      }
      return true
    }
    set(enabled) {
      this.dataSets.forEach { it.isHighlightEnabled = enabled }
    }

  /**
   * Clears this data object from all DataSets and removes all Entries. Don't forget to invalidate
   * the chart after this.
   */
  fun clearValues() {
    mutableDataSets.clear()
    notifyDataChanged()
  }

  /**
   * Checks if this data object contains the specified DataSet. Returns true if so, false if not.
   */
  operator fun contains(dataSet: T): Boolean = mutableDataSets.contains(dataSet)
}
