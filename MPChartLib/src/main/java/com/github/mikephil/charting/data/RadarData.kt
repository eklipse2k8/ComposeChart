package com.github.mikephil.charting.data

import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet
import java.util.*

/**
 * Data container for the RadarChart.
 *
 * @author Philipp Jahoda
 */
class RadarData : ChartData<IRadarDataSet, RadarEntry> {
  /**
   * Sets the labels that should be drawn around the RadarChart at the end of each web line.
   *
   * @param labels
   */
  var labels: List<String>? = null

  constructor() : super()

  constructor(dataSets: MutableList<IRadarDataSet>) : super(dataSets)

  constructor(vararg dataSets: IRadarDataSet) : super(*dataSets)

  /**
   * Sets the labels that should be drawn around the RadarChart at the end of each web line.
   *
   * @param labels
   */
  fun setLabels(vararg labels: String) {
    this.labels = labels.toList()
  }

  override fun getEntryForHighlight(highlight: Highlight): RadarEntry {
    return getDataSetByIndex(highlight.dataSetIndex)!!.getEntryForIndex(highlight.x.toInt())
  }
}
