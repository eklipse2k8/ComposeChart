package com.github.eklipse2k8.charting.data

import android.graphics.Color
import com.github.eklipse2k8.charting.interfaces.datasets.IBarDataSet
import com.github.eklipse2k8.charting.utils.Fill

class BarDataSet(yVals: MutableList<BarEntry>, label: String?) :
    BarLineScatterCandleBubbleDataSet<BarEntry>(yVals, label), IBarDataSet {

  /**
   * the maximum number of bars that are stacked upon each other, this value is calculated from the
   * Entries that are added to the DataSet
   */
  override var stackSize = 1
    private set

  /**
   * Sets the color used for drawing the bar-shadows. The bar shadows is a surface behind the bar
   * that indicates the maximum value. Don't for get to use getResources().getColor(...) to set
   * this. Or Color.rgb(...).
   */
  override var barShadowColor = Color.rgb(215, 215, 215)

  /**
   * Returns the width used for drawing borders around the bars. If borderWidth == 0, no border will
   * be drawn.
   */
  override var barBorderWidth = 0.0f

  /** Returns the color drawing borders around the bars. */
  override var barBorderColor = Color.BLACK

  /** the alpha value used to draw the highlight indicator bar */
  override var highLightAlpha = 120

  /** the overall entry count, including counting each stack-value individually */
  var entryCountStacks = 0
    private set

  /** array of labels used to describe the different values of the stacked bars */
  override var stackLabels = arrayOf<String>()

  private val mutableFills: MutableList<Fill> = mutableListOf()

  override val isStacked: Boolean
    get() = stackSize > 1

  /** Sets the fills for the bars in this dataset. */
  override var fills: List<Fill>
    get() = mutableFills.toList()
    set(value) {
      mutableFills.clear()
      mutableFills.addAll(value)
    }

  init {
    highLightColor = Color.rgb(0, 0, 0)
    calcStackSize(yVals)
    calcEntryCountIncludingStacks(yVals)
  }

  override fun copy(): DataSet<BarEntry> {
    val entries: MutableList<BarEntry> = mutableListOf()
    mutableEntries.forEach { entries.add(it.copy()) }
    val copied = BarDataSet(entries, label)
    copyTo(copied)
    return copied
  }

  private fun copyTo(barDataSet: BarDataSet) {
    super.copyTo(barDataSet)
    barDataSet.stackSize = stackSize
    barDataSet.barShadowColor = barShadowColor
    barDataSet.barBorderWidth = barBorderWidth
    barDataSet.stackLabels = stackLabels
    barDataSet.highLightAlpha = highLightAlpha
  }

  override fun getFill(index: Int): Fill {
    return mutableFills[index % mutableFills.size]
  }

  /**
   * Calculates the total number of entries this DataSet represents, including stacks. All values
   * belonging to a stack are calculated separately.
   */
  private fun calcEntryCountIncludingStacks(yVals: List<BarEntry>) {
    entryCountStacks = 0
    yVals.forEach { entry ->
      val vals = entry.yVals
      if (vals == null) {
        entryCountStacks++
      } else {
        entryCountStacks += vals.size
      }
    }
  }

  /** calculates the maximum stacksize that occurs in the Entries array of this DataSet */
  private fun calcStackSize(yVals: List<BarEntry>) {
    yVals.forEach { entry ->
      val vals = entry.yVals
      if (vals != null && vals.size > stackSize) stackSize = vals.size
    }
  }

  override fun calcMinMax(entry: BarEntry) {
    if (!entry.y.isNaN()) {
      if (entry.yVals == null) {
        if (entry.y < yMin) yMin = entry.y
        if (entry.y > yMax) yMax = entry.y
      } else {
        if (-entry.negativeSum < yMin) yMin = -entry.negativeSum
        if (entry.positiveSum > yMax) yMax = entry.positiveSum
      }
      super.calcMinMax(entry)
    }
  }
}
