package com.github.mikephil.charting.data

import com.github.mikephil.charting.utils.Utils.convertDpToPixel
import com.github.mikephil.charting.interfaces.datasets.IBubbleDataSet
import java.util.ArrayList

class BubbleDataSet(yVals: List<BubbleEntry?>?, label: String?) :
  BarLineScatterCandleBubbleDataSet<BubbleEntry?>(yVals, label!!), IBubbleDataSet {
  override var maxSize = 0f
    protected set
  override var isNormalizeSizeEnabled = true
  private var mHighlightCircleWidth = 2.5f
  override var highlightCircleWidth: Float
    get() = mHighlightCircleWidth
    set(width) {
      mHighlightCircleWidth = convertDpToPixel(width)
    }

  override fun calcMinMax(e: BubbleEntry?) {
    super.calcMinMax(e)
    val size = e!!.size
    if (size > maxSize) {
      maxSize = size
    }
  }

  override fun copy(): DataSet<BubbleEntry>? {
    val entries: MutableList<BubbleEntry?> = ArrayList()
    for (i in mEntries.indices) {
      entries.add(mEntries[i]!!.copy())
    }
    val copied = BubbleDataSet(entries, label)
    copy(copied)
    return copied
  }

  protected fun copy(bubbleDataSet: BubbleDataSet) {
    bubbleDataSet.mHighlightCircleWidth = mHighlightCircleWidth
    bubbleDataSet.isNormalizeSizeEnabled = isNormalizeSizeEnabled
  }
}