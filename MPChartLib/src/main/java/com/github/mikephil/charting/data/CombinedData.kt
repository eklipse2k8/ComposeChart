package com.github.mikephil.charting.data

import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.IBarLineScatterCandleBubbleDataSet

//import com.github.mikephil.charting.components.YAxis
//import com.github.mikephil.charting.highlight.Highlight
//import com.github.mikephil.charting.interfaces.datasets.IBarLineScatterCandleBubbleDataSet

class CombinedData : BarLineScatterCandleBubbleData<IBarLineScatterCandleBubbleDataSet<Entry>, Entry>() {

    var lineData: LineData? = null
    private set(value) {
      field = value
      notifyDataChanged()
    }

  var barData: BarData? = null
    private set(value) {
      field = value
      notifyDataChanged()
    }

  var scatterData: ScatterData? = null
    private set(value) {
      field = value
      notifyDataChanged()
    }

  var candleData: CandleData? = null
    private set(value) {
      field = value
      notifyDataChanged()
    }

  var bubbleData: BubbleData? = null
    private set(value) {
      field = value
      notifyDataChanged()
    }


  /**
   * Returns all data objects in row: line-bar-scatter-candle-bubble if not null.
   *
   * @return
   */
  val allData: List<BarLineScatterCandleBubbleData<*, *>>
    get() {
      val singleList = mutableListOf<BarLineScatterCandleBubbleData<*, *>>()
      lineData?.let { singleList.add(it) }
      barData?.let { singleList.add(it) }
      scatterData?.let { singleList.add(it) }
      candleData?.let { singleList.add(it) }
      bubbleData?.let { singleList.add(it) }
      return singleList.toList()
    }


  /**
   * Get dataset for highlight
   *
   * @param highlight current highlight
   * @return dataset related to highlight
   */
  fun getDataSetByHighlight(highlight: Highlight): IBarLineScatterCandleBubbleDataSet<*>? {
    return null
//    if (highlight.dataIndex >= allData.size) return null
//    val data = getDataByIndex(highlight.dataIndex)
//    return if (highlight.dataSetIndex >= data.dataSetCount) null
//    else data.dataSets[highlight.dataSetIndex]
  }


}

///**
// * Data object that allows the combination of Line-, Bar-, Scatter-, Bubble- and CandleData. Used in
// * the CombinedChart class.
// *
// * @author Philipp Jahoda
// */
//class CombinedData :
//    BarLineScatterCandleBubbleData<IBarLineScatterCandleBubbleDataSet<Entry>, Entry>() {
//
//  var lineData: LineData? = null
//    private set(value) {
//      field = value
//      notifyDataChanged()
//    }
//
//  var barData: BarData? = null
//    private set(value) {
//      field = value
//      notifyDataChanged()
//    }
//
//  var scatterData: ScatterData? = null
//    private set(value) {
//      field = value
//      notifyDataChanged()
//    }
//
//  var candleData: CandleData? = null
//    private set(value) {
//      field = value
//      notifyDataChanged()
//    }
//
//  var bubbleData: BubbleData? = null
//    private set(value) {
//      field = value
//      notifyDataChanged()
//    }
//
//  override fun calcMinMax() {
//    mDataSets.clear()
//    yMax = -Float.MAX_VALUE
//    yMin = Float.MAX_VALUE
//    xMax = -Float.MAX_VALUE
//    xMin = Float.MAX_VALUE
//    mLeftAxisMax = -Float.MAX_VALUE
//    mLeftAxisMin = Float.MAX_VALUE
//    mRightAxisMax = -Float.MAX_VALUE
//    mRightAxisMin = Float.MAX_VALUE
//    val allData = allData
//    for (data in allData) {
//      data.calcMinMax()
//      val sets = data.dataSets
//      mDataSets.addAll(sets as Collection<IBarLineScatterCandleBubbleDataSet<Entry>>)
//      if (data.yMax > yMax) yMax = data.yMax
//      if (data.yMin < yMin) yMin = data.yMin
//      if (data.xMax > xMax) xMax = data.xMax
//      if (data.xMin < xMin) xMin = data.xMin
//      for (dataset in sets) {
//        if (dataset.axisDependency === YAxis.AxisDependency.LEFT) {
//          if (dataset.yMax > mLeftAxisMax) {
//            mLeftAxisMax = dataset.yMax
//          }
//          if (dataset.yMin < mLeftAxisMin) {
//            mLeftAxisMin = dataset.yMin
//          }
//        } else {
//          if (dataset.yMax > mRightAxisMax) {
//            mRightAxisMax = dataset.yMax
//          }
//          if (dataset.yMin < mRightAxisMin) {
//            mRightAxisMin = dataset.yMin
//          }
//        }
//      }
//    }
//  }
//
//  /**
//   * Returns all data objects in row: line-bar-scatter-candle-bubble if not null.
//   *
//   * @return
//   */
//  val allData: List<Any>
//    get() {
//      val singleList = mutableListOf<Any>()
//      lineData?.let { singleList.add(it) }
//      barData?.let { singleList.add(it) }
//      scatterData?.let { singleList.add(it) }
//      candleData?.let { singleList.add(it) }
//      bubbleData?.let { singleList.add(it) }
//      return singleList.toList()
//    }
//
//  fun getDataByIndex(index: Int): BarLineScatterCandleBubbleData<*, *> {
//    return allData[index] as BarLineScatterCandleBubbleData<*, *>
//  }
//
//  override fun notifyDataChanged() {
//    lineData?.notifyDataChanged()
//    barData?.notifyDataChanged()
//    candleData?.notifyDataChanged()
//    scatterData?.notifyDataChanged()
//    bubbleData?.notifyDataChanged()
//    calcMinMax() // recalculate everything
//  }
//
//  /**
//   * Get the Entry for a corresponding highlight object
//   *
//   * @param highlight
//   * @return the entry that is highlighted
//   */
//  override fun getEntryForHighlight(highlight: Highlight): Entry? {
//    if (highlight.dataIndex >= allData.size) return null
//
//    val data = getDataByIndex(highlight.dataIndex)
//
//    if (highlight.dataSetIndex >= data.dataSetCount) return null
//
//    // The value of the highlighted entry could be NaN -
//    //   if we are not interested in highlighting a specific value.
//    val entries = data.getDataSetByIndex(highlight.dataSetIndex)?.getEntriesForXValue(highlight.x)
//    return entries?.firstOrNull { entry -> entry.y == highlight.y || highlight.y.isNaN() }
//  }
//
//  /**
//   * Get dataset for highlight
//   *
//   * @param highlight current highlight
//   * @return dataset related to highlight
//   */
//  fun getDataSetByHighlight(highlight: Highlight): IBarLineScatterCandleBubbleDataSet<*>? {
//    if (highlight.dataIndex >= allData.size) return null
//    val data = getDataByIndex(highlight.dataIndex)
//    return if (highlight.dataSetIndex >= data.dataSetCount) null
//    else data.dataSets[highlight.dataSetIndex]
//  }
//
//  fun getDataIndex(data: BarLineScatterCandleBubbleData<*, *>): Int {
//    return allData.indexOf(data)
//  }
//
//  override fun removeDataSet(d: IBarLineScatterCandleBubbleDataSet<Entry>?): Boolean {
//    return allData.firstOrNull { d?.removeEntry(it as Entry) == true } != null
//  }
//
//  override fun removeDataSet(index: Int): Boolean {
//    throw UnsupportedOperationException("removeDataSet(int index) not supported for CombinedData")
//  }
//
//  override fun removeEntry(e: Entry?, dataSetIndex: Int): Boolean {
//    throw UnsupportedOperationException("removeEntry(...) not supported for CombinedData")
//  }
//
//  override fun removeEntry(xValue: Float, dataSetIndex: Int): Boolean {
//    throw UnsupportedOperationException("removeEntry(...) not supported for CombinedData")
//  }
//}
