package com.github.eklipse2k8.charting.data

import com.github.eklipse2k8.charting.highlight.Highlight
import com.github.eklipse2k8.charting.interfaces.datasets.IPieDataSet

/**
 * A PieData object can only represent one DataSet. Unlike all other charts, the legend labels of
 * the PieChart are created from the x-values array, and not from the DataSet labels. Each PieData
 * object can only represent one PieDataSet (multiple PieDataSets inside a single PieChart are not
 * possible).
 *
 * @author Philipp Jahoda
 */
class PieData(dataSet: IPieDataSet) : ChartData<IPieDataSet, PieEntry>(dataSet) {

  /** Sets the PieDataSet this data object should represent. */
  var dataSet: IPieDataSet
    get() = dataSets[0]
    set(dataSet) {
      mutableDataSets.clear()
      mutableDataSets.add(dataSet)
      notifyDataChanged()
    }

  /**
   * The PieData object can only have one DataSet. Use getDataSet() method instead.
   *
   * @param index
   * @return
   */
  override fun getDataSetByIndex(index: Int): IPieDataSet? {
    return if (index == 0) dataSet else null
  }

  override fun getDataSetByLabel(label: String, ignorecase: Boolean): IPieDataSet {
    return if (ignorecase)
        (if (label.equals(dataSets[0].label, ignoreCase = true)) dataSets[0] else null)!!
    else (if (label == dataSets[0].label) dataSets[0] else null)!!
  }

  override fun getEntryForHighlight(highlight: Highlight): PieEntry {
    return dataSet.getEntryForIndex(highlight.x.toInt())
  }

  /** Returns the sum of all values in this PieData object. */
  val yValueSum: Float
    get() {
      var sum = 0f
      for (i in 0 until dataSet.entryCount) sum += dataSet.getEntryForIndex(i).y
      return sum
    }
}
