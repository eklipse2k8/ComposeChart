package com.github.eklipse2k8.charting.data

import com.github.eklipse2k8.charting.interfaces.datasets.IBubbleDataSet
import com.github.eklipse2k8.charting.utils.Utils.convertDpToPixel

class BubbleDataSet(yVals: MutableList<BubbleEntry>, label: String?) :
    BarLineScatterCandleBubbleDataSet<BubbleEntry>(yVals, label), IBubbleDataSet {

  override var maxSize = 0f
    protected set

  override var isNormalizeSizeEnabled = true

  private var mHighlightCircleWidth = 2.5f

  override var highlightCircleWidth: Float
    get() = mHighlightCircleWidth
    set(width) {
      mHighlightCircleWidth = convertDpToPixel(width)
    }

  override fun calcMinMax(entry: BubbleEntry) {
    super.calcMinMax(entry)
    val size = entry.size
    if (size > maxSize) {
      maxSize = size
    }
  }

  override fun copy(): DataSet<BubbleEntry> {
    val entries = mutableListOf<BubbleEntry>()
    mutableEntries.forEach { entries.add(it.copy()) }
    val copied = BubbleDataSet(entries, label)
    copy(copied)
    return copied
  }

  private fun copy(bubbleDataSet: BubbleDataSet) {
    bubbleDataSet.mHighlightCircleWidth = mHighlightCircleWidth
    bubbleDataSet.isNormalizeSizeEnabled = isNormalizeSizeEnabled
  }
}
