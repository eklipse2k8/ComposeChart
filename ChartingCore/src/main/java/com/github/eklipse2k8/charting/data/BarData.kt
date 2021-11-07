package com.github.eklipse2k8.charting.data

import com.github.eklipse2k8.charting.interfaces.datasets.IBarDataSet

/**
 * Data object that represents all data for the BarChart.
 *
 * @author Philipp Jahoda
 */
class BarData : BarLineScatterCandleBubbleData<IBarDataSet, BarEntry> {

  /** the width of the bars on the x-axis, in values (not pixels) */
  var barWidth = 0.85f

  constructor() : super()

  constructor(vararg dataSets: IBarDataSet) : super(*dataSets)

  constructor(dataSets: MutableList<IBarDataSet>) : super(dataSets)

  /**
   * Groups all BarDataSet objects this data object holds together by modifying the x-value of their
   * entries. Previously set x-values of entries will be overwritten. Leaves space between bars and
   * groups as specified by the parameters. Do not forget to call notifyDataSetChanged() on your
   * BarChart object after calling this method.
   *
   * @param fromX the starting point on the x-axis where the grouping should begin
   * @param groupSpace the space between groups of bars in values (not pixels) e.g. 0.8f for bar
   * width 1f
   * @param barSpace the space between individual bars in values (not pixels) e.g. 0.1f for bar
   * width 1f
   */
  fun groupBars(fromX: Float, groupSpace: Float, barSpace: Float) {
    if (dataSetCount <= 1) {
      throw IllegalArgumentException(
          "BarData needs to hold at least 2 BarDataSets to allow grouping.")
    }
    val max = maxEntryCountSet
    val maxEntryCount = max?.entryCount ?: return
    val groupSpaceWidthHalf = groupSpace / 2f
    val barSpaceHalf = barSpace / 2f
    val barWidthHalf = barWidth / 2f
    val interval = getGroupWidth(groupSpace, barSpace)
    var startFromX = fromX
    for (i in 0 until maxEntryCount) {
      val start = startFromX
      startFromX += groupSpaceWidthHalf
      for (set in dataSets) {
        startFromX += barSpaceHalf
        startFromX += barWidthHalf
        if (i < set.entryCount) {
          val entry = set.getEntryForIndex(i)
          entry.x = startFromX
        }
        startFromX += barWidthHalf
        startFromX += barSpaceHalf
      }
      startFromX += groupSpaceWidthHalf
      val end = startFromX
      val innerInterval = end - start
      val diff = interval - innerInterval

      // correct rounding errors
      if (diff > 0 || diff < 0) {
        startFromX += diff
      }
    }
    notifyDataChanged()
  }

  /**
   * In case of grouped bars, this method returns the space an individual group of bar needs on the
   * x-axis.
   *
   * @param groupSpace
   * @param barSpace
   * @return
   */
  fun getGroupWidth(groupSpace: Float, barSpace: Float): Float {
    return dataSets.size * (barWidth + barSpace) + groupSpace
  }
}
