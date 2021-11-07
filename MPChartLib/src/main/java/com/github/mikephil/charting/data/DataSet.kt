package com.github.mikephil.charting.data

import kotlin.math.abs

/**
 * The DataSet class represents one group or type of entries (Entry) in the Chart that belong
 * together. It is designed to logically separate different groups of values inside the Chart (e.g.
 * the values for a specific line in the LineChart, or the values of a specific group of bars in the
 * BarChart).
 *
 * Creates a new DataSet object with the given values (entries) it represents. Also, a label that
 * describes the DataSet can be specified. The label can also be used to retrieve the DataSet from a
 * ChartData object.
 *
 * @param mutableEntries the entries that this DataSet represents / holds together
 * @param label
 *
 * @author Philipp Jahoda
 */
abstract class DataSet<E : Entry>(
    protected var mutableEntries: MutableList<E> = mutableListOf(),
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

  override val entryCount: Int
    get() = mutableEntries.size

  init {
    calcMinMax()
  }

  override fun calcMinMax() {
    yMax = -Float.MAX_VALUE
    yMin = Float.MAX_VALUE
    xMax = -Float.MAX_VALUE
    xMin = Float.MAX_VALUE
    mutableEntries.forEach { calcMinMax(it) }
  }

  override fun calcMinMaxY(fromX: Float, toX: Float) {
    yMax = -Float.MAX_VALUE
    yMin = Float.MAX_VALUE
    if (mutableEntries.isEmpty()) return
    val indexFrom = getEntryIndex(fromX, Float.NaN, Rounding.DOWN)
    val indexTo = getEntryIndex(toX, Float.NaN, Rounding.UP)
    if (indexTo < indexFrom) return
    for (i in indexFrom..indexTo) {
      // only recalculate y
      calcMinMaxY(mutableEntries[i])
    }
  }

  /**
   * Updates the min and max x and y value of this DataSet based on the given Entry.
   *
   * @param entry
   */
  protected open fun calcMinMax(entry: E) {
    calcMinMaxX(entry)
    calcMinMaxY(entry)
  }

  protected fun calcMinMaxX(e: E) {
    if (e.x < xMin) xMin = e.x
    if (e.x > xMax) xMax = e.x
  }

  protected open fun calcMinMaxY(e: E) {
    if (e.y < yMin) yMin = e.y
    if (e.y > yMax) yMax = e.y
  }

  /**
   * Returns the array of entries that this DataSet represents.
   *
   * @return
   */
  var entries: List<E>
    get() = mutableEntries.toList()
    set(value) {
      mutableEntries.clear()
      mutableEntries.addAll(value)
      notifyDataSetChanged()
    }

  /**
   * Provides an exact copy of the DataSet this method is used on.
   *
   * @return
   */
  abstract fun copy(): DataSet<E>

  protected fun copyTo(dataSet: DataSet<*>) {
    super.copyTo(dataSet)
  }

  override fun toString(): String = mutableEntries.joinToString(" ")

  /**
   * Returns a simple string representation of the DataSet with the type and the number of Entries.
   *
   * @return
   */
  fun toSimpleString(): String = "DataSet, label: $label, entries: $entryCount"

  override fun addEntryOrdered(entry: E) {
    calcMinMax(entry)
    if (mutableEntries.size > 0 && mutableEntries[entryCount - 1].x > entry.x) {
      val closestIndex = getEntryIndex(entry.x, entry.y, Rounding.UP)
      mutableEntries.add(closestIndex, entry)
    } else {
      mutableEntries.add(entry)
    }
  }

  override fun clear() {
    mutableEntries.clear()
    notifyDataSetChanged()
  }

  override fun addEntry(entry: E): Boolean {
    calcMinMax(entry)
    // add the entry
    return mutableEntries.add(entry)
  }

  override fun removeEntry(entry: E): Boolean {
    // remove the entry
    val removed = mutableEntries.remove(entry)
    if (removed) {
      calcMinMax()
    }
    return removed
  }

  override fun getEntryIndex(entry: E): Int = mutableEntries.indexOf(entry)

  override fun getEntryForXValue(xValue: Float, closestToY: Float, rounding: Rounding?): E? {
    val index = getEntryIndex(xValue, closestToY, rounding)
    return if (index > -1) mutableEntries[index] else null
  }

  override fun getEntryForXValue(xValue: Float, closestToY: Float): E? {
    return getEntryForXValue(xValue, closestToY, Rounding.CLOSEST)
  }

  override fun getEntryForIndex(index: Int): E {
    return mutableEntries[index]
  }

  override fun getEntryIndex(xValue: Float, closestToY: Float, rounding: Rounding?): Int {
    if (mutableEntries.isEmpty()) return -1
    var low = 0
    var high = mutableEntries.size - 1
    var closest = high
    while (low < high) {
      val m = (low + high) / 2
      val d1 = mutableEntries[m].x - xValue
      val d2 = mutableEntries[m + 1].x - xValue
      val ad1 = abs(d1)
      val ad2 = abs(d2)
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
      val closestXValue = mutableEntries[closest].x
      if (rounding == Rounding.UP) {
        // If rounding up, and found x-value is lower than specified x, and we can go upper...
        if (closestXValue < xValue && closest < mutableEntries.size - 1) {
          ++closest
        }
      } else if (rounding == Rounding.DOWN) {
        // If rounding down, and found x-value is upper than specified x, and we can go lower...
        if (closestXValue > xValue && closest > 0) {
          --closest
        }
      }

      // Search by closest to y-value
      if (!closestToY.isNaN()) {
        while (closest > 0 && mutableEntries[closest - 1].x == closestXValue) closest -= 1
        var closestYValue = mutableEntries[closest].y
        var closestYIndex = closest
        while (true) {
          closest += 1
          if (closest >= mutableEntries.size) break
          val value: Entry = mutableEntries[closest]
          if (value.x != closestXValue) break
          if (abs(value.y - closestToY) <= abs(closestYValue - closestToY)) {
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
    val entries = mutableListOf<E>()
    var low = 0
    var high = entryCount - 1
    while (low <= high) {
      var m = (high + low) / 2
      var entry = mutableEntries[m]

      // if we have a match
      if (xValue == entry.x) {
        while (m > 0 && mutableEntries[m - 1].x == xValue) m--
        high = entryCount

        // loop over all "equal" entries
        while (m < high) {
          entry = mutableEntries[m]
          if (entry.x == xValue) {
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
    return entries.toList()
  }
}
