package com.github.eklipse2k8.charting.data

import com.github.eklipse2k8.charting.highlight.Highlight
import com.github.eklipse2k8.charting.interfaces.datasets.IRadarDataSet
import java.util.*

/**
 * Data container for the RadarChart.
 *
 * @author Philipp Jahoda
 */
class RadarData @JvmOverloads constructor(dataSets: MutableList<IRadarDataSet> = mutableListOf()) :
    ChartData<IRadarDataSet, RadarEntry>(dataSets) {
  /**
   * Sets the labels that should be drawn around the RadarChart at the end of each web line.
   *
   * @param labels
   */
  var labels: List<String>? = null

  constructor(vararg dataSets: IRadarDataSet) : this(dataSets.toMutableList())

  /**
   * Sets the labels that should be drawn around the RadarChart at the end of each web line.
   *
   * @param labels
   */
  fun setLabels(vararg labels: String) {
    this.labels = labels.toList()
  }

  override fun getEntryForHighlight(highlight: Highlight): RadarEntry? =
      getDataSetByIndex(highlight.dataSetIndex)?.getEntryForIndex(highlight.x.toInt())
}
