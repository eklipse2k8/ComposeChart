package com.github.mikephil.charting.data

import android.util.Log
import com.github.mikephil.charting.data.ChartData.calcMinMax
import com.github.mikephil.charting.data.ChartData.dataSets
import com.github.mikephil.charting.data.ChartData.yMax
import com.github.mikephil.charting.data.ChartData.yMin
import com.github.mikephil.charting.data.ChartData.xMax
import com.github.mikephil.charting.data.ChartData.xMin
import com.github.mikephil.charting.interfaces.datasets.IDataSet.axisDependency
import com.github.mikephil.charting.interfaces.datasets.IDataSet.yMax
import com.github.mikephil.charting.interfaces.datasets.IDataSet.yMin
import com.github.mikephil.charting.data.ChartData.notifyDataChanged
import com.github.mikephil.charting.highlight.Highlight.dataIndex
import com.github.mikephil.charting.highlight.Highlight.dataSetIndex
import com.github.mikephil.charting.data.ChartData.dataSetCount
import com.github.mikephil.charting.data.ChartData.getDataSetByIndex
import com.github.mikephil.charting.interfaces.datasets.IDataSet.getEntriesForXValue
import com.github.mikephil.charting.highlight.Highlight.x
import com.github.mikephil.charting.data.Entry.y
import com.github.mikephil.charting.highlight.Highlight.y
import com.github.mikephil.charting.data.ChartData.removeDataSet
import com.github.mikephil.charting.data.BarLineScatterCandleBubbleData
import com.github.mikephil.charting.interfaces.datasets.IBarLineScatterCandleBubbleDataSet
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.ScatterData
import com.github.mikephil.charting.data.CandleData
import com.github.mikephil.charting.data.BubbleData
import com.github.mikephil.charting.data.ChartData
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.highlight.Highlight
import java.util.ArrayList

/**
 * Data object that allows the combination of Line-, Bar-, Scatter-, Bubble- and
 * CandleData. Used in the CombinedChart class.
 *
 * @author Philipp Jahoda
 */
class CombinedData :
  BarLineScatterCandleBubbleData<IBarLineScatterCandleBubbleDataSet<out Entry?>?>() {
  var lineData: LineData? = null
    private set
  var barData: BarData? = null
    private set
  var scatterData: ScatterData? = null
    private set
  var candleData: CandleData? = null
    private set
  var bubbleData: BubbleData? = null
    private set

  fun setData(data: LineData?) {
    lineData = data
    notifyDataChanged()
  }

  fun setData(data: BarData?) {
    barData = data
    notifyDataChanged()
  }

  fun setData(data: ScatterData?) {
    scatterData = data
    notifyDataChanged()
  }

  fun setData(data: CandleData?) {
    candleData = data
    notifyDataChanged()
  }

  fun setData(data: BubbleData?) {
    bubbleData = data
    notifyDataChanged()
  }

  override fun calcMinMax() {
    if (mDataSets == null) {
      mDataSets = ArrayList()
    }
    mDataSets.clear()
    yMax = -Float.MAX_VALUE
    yMin = Float.MAX_VALUE
    xMax = -Float.MAX_VALUE
    xMin = Float.MAX_VALUE
    mLeftAxisMax = -Float.MAX_VALUE
    mLeftAxisMin = Float.MAX_VALUE
    mRightAxisMax = -Float.MAX_VALUE
    mRightAxisMin = Float.MAX_VALUE
    val allData = allData
    for (data in allData) {
      data.calcMinMax()
      val sets: List<IBarLineScatterCandleBubbleDataSet<out Entry?>> = data.dataSets
      mDataSets.addAll(sets)
      if (data.yMax > yMax) yMax = data.yMax
      if (data.yMin < yMin) yMin = data.yMin
      if (data.xMax > xMax) xMax = data.xMax
      if (data.xMin < xMin) xMin = data.xMin
      for (dataset in sets) {
        if (dataset.axisDependency === YAxis.AxisDependency.LEFT) {
          if (dataset.yMax > mLeftAxisMax) {
            mLeftAxisMax = dataset.yMax
          }
          if (dataset.yMin < mLeftAxisMin) {
            mLeftAxisMin = dataset.yMin
          }
        } else {
          if (dataset.yMax > mRightAxisMax) {
            mRightAxisMax = dataset.yMax
          }
          if (dataset.yMin < mRightAxisMin) {
            mRightAxisMin = dataset.yMin
          }
        }
      }
    }
  }

  /**
   * Returns all data objects in row: line-bar-scatter-candle-bubble if not null.
   *
   * @return
   */
  val allData: List<BarLineScatterCandleBubbleData<*>>
    get() {
      val data: MutableList<BarLineScatterCandleBubbleData<*>> = ArrayList()
      if (lineData != null) data.add(lineData!!)
      if (barData != null) data.add(barData)
      if (scatterData != null) data.add(scatterData!!)
      if (candleData != null) data.add(candleData!!)
      if (bubbleData != null) data.add(bubbleData!!)
      return data
    }

  fun getDataByIndex(index: Int): BarLineScatterCandleBubbleData<*> {
    return allData[index]
  }

  override fun notifyDataChanged() {
    if (lineData != null) lineData!!.notifyDataChanged()
    if (barData != null) barData.notifyDataChanged()
    if (candleData != null) candleData!!.notifyDataChanged()
    if (scatterData != null) scatterData!!.notifyDataChanged()
    if (bubbleData != null) bubbleData!!.notifyDataChanged()
    calcMinMax() // recalculate everything
  }

  /**
   * Get the Entry for a corresponding highlight object
   *
   * @param highlight
   * @return the entry that is highlighted
   */
  override fun getEntryForHighlight(highlight: Highlight): Entry? {
    if (highlight.dataIndex >= allData.size) return null
    val data: ChartData<*, *> = getDataByIndex(highlight.dataIndex)
    if (highlight.dataSetIndex >= data.dataSetCount) return null

    // The value of the highlighted entry could be NaN -
    //   if we are not interested in highlighting a specific value.
    val entries = data.getDataSetByIndex(highlight.dataSetIndex)
      .getEntriesForXValue(highlight.x)
    for (entry in entries) if (entry.y == highlight.y ||
        java.lang.Float.isNaN(highlight.y)
    ) return entry
    return null
  }

  /**
   * Get dataset for highlight
   *
   * @param highlight current highlight
   * @return dataset related to highlight
   */
  fun getDataSetByHighlight(highlight: Highlight): IBarLineScatterCandleBubbleDataSet<out Entry>? {
    if (highlight.dataIndex >= allData.size) return null
    val data = getDataByIndex(highlight.dataIndex)
    return if (highlight.dataSetIndex >= data.dataSetCount) null else data.dataSets[highlight.dataSetIndex]
  }

  fun getDataIndex(data: ChartData<*, *>?): Int {
    return allData.indexOf(data)
  }

  override fun removeDataSet(d: IBarLineScatterCandleBubbleDataSet<out Entry?>?): Boolean {
    val datas = allData
    var success = false
    for (data in datas) {
      success = data.removeDataSet(d)
      if (success) {
        break
      }
    }
    return success
  }

  @Deprecated("")
  override fun removeDataSet(index: Int): Boolean {
    Log.e("MPAndroidChart", "removeDataSet(int index) not supported for CombinedData")
    return false
  }

  @Deprecated("")
  override fun removeEntry(e: Entry?, dataSetIndex: Int): Boolean {
    Log.e("MPAndroidChart", "removeEntry(...) not supported for CombinedData")
    return false
  }

  @Deprecated("")
  override fun removeEntry(xValue: Float, dataSetIndex: Int): Boolean {
    Log.e("MPAndroidChart", "removeEntry(...) not supported for CombinedData")
    return false
  }
}