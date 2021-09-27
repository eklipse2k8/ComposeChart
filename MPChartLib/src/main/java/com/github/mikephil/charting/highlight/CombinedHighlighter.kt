package com.github.mikephil.charting.highlight

import androidx.annotation.NonNull
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.ChartData
import com.github.mikephil.charting.data.DataSet
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider
import com.github.mikephil.charting.interfaces.dataprovider.CombinedDataProvider
import com.github.mikephil.charting.interfaces.datasets.IDataSet

/** Created by Philipp Jahoda on 12/09/15. */
class CombinedHighlighter(chart: CombinedDataProvider, @NonNull barChart: BarDataProvider) :
    ChartHighlighter<CombinedDataProvider>(chart), IHighlighter {
  /** bar highlighter for supporting stacked highlighting */
  protected val barHighlighter: BarHighlighter = BarHighlighter(barChart)

  override fun getHighlightsAtXValue(xVal: Float, x: Float, y: Float): List<Highlight> {
    mHighlightBuffer.clear()
    val dataObjects = mChart.combinedData.allData
    for (i in dataObjects.indices) {
      val dataObject = dataObjects[i]

      // in case of BarData, let the BarHighlighter take over
      if (dataObject is BarData) {
        val high = barHighlighter.getHighlight(x, y)
        if (high != null) {
          high.dataIndex = i
          mHighlightBuffer.add(high)
        }
      } else {
        var j = 0
        val dataSetCount = dataObject.dataSetCount
        while (j < dataSetCount) {
          val dataSet = dataObjects[i].getDataSetByIndex(j) ?: continue

          // don't include datasets that cannot be highlighted
          if (!dataSet.isHighlightEnabled) {
            j++
            continue
          }
          val highs = buildHighlights(dataSet, j, xVal, DataSet.Rounding.CLOSEST)
          for (high in highs) {
            high.dataIndex = i
            mHighlightBuffer.add(high)
          }
          j++
        }
      }
    }
    return mHighlightBuffer
  }
}
