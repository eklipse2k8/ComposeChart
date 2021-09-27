package com.github.mikephil.charting.data

import com.github.mikephil.charting.interfaces.datasets.IDataSet
import java.util.ArrayList

/**
 * The DataSet class represents one group or type of entries (Entry) in the Chart that belong
 * together. It is designed to logically separate different groups of values inside the Chart (e.g.
 * the values for a specific line in the LineChart, or the values of a specific group of bars in the
 * BarChart).
 *
 * @author Philipp Jahoda
 */
abstract class DataSet<E : Entry>(
    /** the entries that this DataSet represents / holds together */
    protected var mEntries: MutableList<E> = mutableListOf(),
    label: String?
) : BaseDataSet<E>(label) {
  /** maximum y-value in the value array */
  override var yMax = -Float.MAX_VALUE
    protected set

  /** minimum y-value in the value array */
  override var yMin = Float.MAX_VALUE
    protected set

  /** maximum x-value in the value array */
  override var xMax = -Float.MAX_VALUE
    protected set

  /** minimum x-value in the value array */
  override var xMin = Float.MAX_VALUE
    protected set

  override fun calcMinMax() {
    yMax = -Float.MAX_VALUE
    yMin = Float.MAX_VALUE
    xMax = -Float.MAX_VALUE
    xMin = Float.MAX_VALUE
    if (mEntries == null || mEntries!!.isEmpty()) return
    for (e in mEntries!!) {
      calcMinMax(e)
    }
  }

  override fun calcMinMaxY(fromX: Float, toX: Float) {
    yMax = -Float.MAX_VALUE
    yMin = Float.MAX_VALUE
    if (mEntries == null || mEntries!!.isEmpty()) return
    val indexFrom = getEntryIndex(fromX, Float.NaN, Rounding.DOWN)
    val indexTo = getEntryIndex(toX, Float.NaN, Rounding.UP)
    if (indexTo < indexFrom) return
    for (i in indexFrom..indexTo) {

      // only recalculate y
      calcMinMaxY(mEntries!![i])
    }
  }

  /**
   * Updates the min and max x and y value of this DataSet based on the given Entry.
   *
   * @param e
   */
  protected open fun calcMinMax(e: E?) {
    if (e == null) return
    calcMinMaxX(e)
    calcMinMaxY(e)
  }

  protected fun calcMinMaxX(e: E) {
    if (e!!.x < xMin) xMin = e.x
    if (e.x > xMax) xMax = e.x
  }

  protected open fun calcMinMaxY(e: E) {
    if (e!!.y < yMin) yMin = e.y
    if (e.y > yMax) yMax = e.y
  }

  override val entryCount: Int
    get() = mEntries!!.size

  /**
   * This method is deprecated. Use getEntries() instead.
   *
   * @return
   */
  @get:Deprecated("")
  val values: List<E>?
    get() = mEntries
  /**
   * Returns the array of entries that this DataSet represents.
   *
   * @return
   */
  /**
   * Sets the array of entries that this DataSet represents, and calls notifyDataSetChanged()
   *
   * @return
   */
  var entries: MutableList<E>
    get() = mEntries
    set(entries) {
      mEntries = entries
      notifyDataSetChanged()
    }

  /**
   * This method is deprecated. Use setEntries(...) instead.
   *
   * @param values
   */
  @Deprecated("")
  fun setValues(values: MutableList<E>) {
    mEntries = values
  }

  /**
   * Provides an exact copy of the DataSet this method is used on.
   *
   * @return
   */
  abstract fun copy(): DataSet<E>?

  /**
   *
   * @param dataSet
   */
  protected open fun copy(dataSet: IDataSet<*>) {
    super.copy(dataSet)
  }

  override fun toString(): String {
    val buffer = StringBuffer()
    buffer.append(toSimpleString())
    for (i in mEntries!!.indices) {
      buffer.append(mEntries!![i].toString() + " ")
    }
    return buffer.toString()
  }

  /**
   * Returns a simple string representation of the DataSet with the type and the number of Entries.
   *
   * @return
   */
  fun toSimpleString(): String {
    val buffer = StringBuffer()
    buffer.append(
        """
  DataSet, label: ${if (label == null) "" else label}, entries: ${mEntries!!.size}
  
  """.trimIndent())
    return buffer.toString()
  }

  override fun addEntryOrdered(e: E) {
    if (e == null) return
    if (mEntries == null) {
      mEntries = ArrayList()
    }
    calcMinMax(e)
    if (mEntries!!.size > 0 && mEntries!![mEntries!!.size - 1]!!.x > e.x) {
      val closestIndex = getEntryIndex(e.x, e.y, Rounding.UP)
      mEntries!!.add(closestIndex, e)
    } else {
      mEntries!!.add(e)
    }
  }

  override fun clear() {
    mEntries!!.clear()
    notifyDataSetChanged()
  }

  override fun addEntry(e: E): Boolean {
    if (e == null) return false
    var values = mEntries
    if (values == null) {
      values = ArrayList()
    }
    calcMinMax(e)

    // add the entry
    return values.add(e)
  }

  override fun removeEntry(e: E): Boolean {
    if (e == null) return false
    if (mEntries == null) return false

    // remove the entry
    val removed = mEntries!!.remove(e)
    if (removed) {
      calcMinMax()
    }
    return removed
  }

  override fun getEntryIndex(e: E): Int {
    return mEntries!!.indexOf(e)
  }

  override fun getEntryForXValue(xValue: Float, closestToY: Float, rounding: Rounding?): E? {
    val index = getEntryIndex(xValue, closestToY, rounding)
    return if (index > -1) mEntries!![index] else null
  }

  override fun getEntryForXValue(xValue: Float, closestToY: Float): E? {
    return getEntryForXValue(xValue, closestToY, Rounding.CLOSEST)
  }

  override fun getEntryForIndex(index: Int): E {
    return mEntries!![index]
  }

  override fun getEntryIndex(xValue: Float, closestToY: Float, rounding: Rounding?): Int {
    if (mEntries == null || mEntries!!.isEmpty()) return -1
    var low = 0
    var high = mEntries!!.size - 1
    var closest = high
    while (low < high) {
      val m = (low + high) / 2
      val d1 = mEntries!![m]!!.x - xValue
      val d2 = mEntries!![m + 1]!!.x - xValue
      val ad1 = Math.abs(d1)
      val ad2 = Math.abs(d2)
      if (ad2 < ad1) {
        // [m + 1] is closer to xValue
        // Search in an higher place
        low = m + 1
      } else if (ad1 < ad2) {
        // [m] is closer to xValue
        // Search in a lower place
        high = m
      } else {
        // We have multiple sequential x-value with same distance
        if (d1 >= 0.0) {
          // Search in a lower place
          high = m
        } else if (d1 < 0.0) {
          // Search in an higher place
          low = m + 1
        }
      }
      closest = high
    }
    if (closest != -1) {
      val closestXValue = mEntries!![closest]!!.x
      if (rounding == Rounding.UP) {
        // If rounding up, and found x-value is lower than specified x, and we can go upper...
        if (closestXValue < xValue && closest < mEntries!!.size - 1) {
          ++closest
        }
      } else if (rounding == Rounding.DOWN) {
        // If rounding down, and found x-value is upper than specified x, and we can go lower...
        if (closestXValue > xValue && closest > 0) {
          --closest
        }
      }

      // Search by closest to y-value
      if (!java.lang.Float.isNaN(closestToY)) {
        while (closest > 0 && mEntries!![closest - 1]!!.x == closestXValue) closest -= 1
        var closestYValue = mEntries!![closest]!!.y
        var closestYIndex = closest
        while (true) {
          closest += 1
          if (closest >= mEntries!!.size) break
          val value: Entry = mEntries!![closest]
          if (value.x != closestXValue) break
          if (Math.abs(value.y - closestToY) <= Math.abs(closestYValue - closestToY)) {
            closestYValue = closestToY
            closestYIndex = closest
          }
        }
        closest = closestYIndex
      }
    }
    return closest
  }

  override fun getEntriesForXValue(xValue: Float): List<E> {
    val entries: MutableList<E> = ArrayList()
    var low = 0
    var high = mEntries!!.size - 1
    while (low <= high) {
      var m = (high + low) / 2
      var entry = mEntries!![m]

      // if we have a match
      if (xValue == entry!!.x) {
        while (m > 0 && mEntries!![m - 1]!!.x == xValue) m--
        high = mEntries!!.size

        // loop over all "equal" entries
        while (m < high) {
          entry = mEntries!![m]
          if (entry!!.x == xValue) {
            entries.add(entry)
          } else {
            break
          }
          m++
        }
        break
      } else {
        if (xValue > entry.x) low = m + 1 else high = m - 1
      }
    }
    return entries
  }

  /**
   * Determines how to round DataSet index values for [DataSet.getEntryIndex]
   * DataSet.getEntryIndex()} when an exact x-index is not found.
   */
  enum class Rounding {
    UP,
    DOWN,
    CLOSEST
  }

  /**
   * Creates a new DataSet object with the given values (entries) it represents. Also, a label that
   * describes the DataSet can be specified. The label can also be used to retrieve the DataSet from
   * a ChartData object.
   *
   * @param entries
   * @param label
   */
  init {
    calcMinMax()
  }
}
