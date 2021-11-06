package com.github.mikephil.charting.data

import android.graphics.Paint
import com.github.mikephil.charting.interfaces.datasets.ICandleDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.Utils.convertDpToPixel

/**
 * DataSet for the CandleStickChart.
 *
 * @author Philipp Jahoda
 */
class CandleDataSet(yVals: MutableList<CandleEntry>, label: String?) :
    LineScatterCandleRadarDataSet<CandleEntry>(yVals, label), ICandleDataSet {

  /**
   * Sets the width of the candle-shadow-line in pixels. Default 3f.
   *
   * @param width
   */
  override var shadowWidth: Float = 3f
    set(value) {
      field = convertDpToPixel(value)
    }

  /**
   * Sets whether the candle bars should show? should the candle bars show? when false, only "ticks"
   * will show
   *
   * - default: true
   */
  override var showCandleBar = true

  /**
   * Sets the space that is left out on the left and right side of each candle, default 0.1f (10%),
   * max 0.45f, min 0f
   */
  override var barSpace: Float = 0.1f
    set(value) {
      field = value.coerceIn(0f, 0.45f)
    }

  /** use candle color for the shadow */
  override var shadowColorSameAsCandle = false

  /** paint style when open < close increasing candlesticks are traditionally hollow */
  override var increasingPaintStyle = Paint.Style.STROKE

  /** paint style when open > close descreasing candlesticks are traditionally filled */
  override var decreasingPaintStyle = Paint.Style.FILL

  /** color for open == close */
  override var neutralColor = ColorTemplate.COLOR_SKIP

  /** color for open < close */
  override var increasingColor = ColorTemplate.COLOR_SKIP

  /** color for open > close */
  override var decreasingColor = ColorTemplate.COLOR_SKIP

  /** shadow line color, set -1 for backward compatibility and uses default color */
  override var shadowColor = ColorTemplate.COLOR_SKIP

  override fun copy(): DataSet<CandleEntry> {
    val entries: MutableList<CandleEntry> = ArrayList()
    for (i in mEntries!!.indices) {
      entries.add(mEntries!![i]!!.copy())
    }
    val copied = CandleDataSet(entries, label)
    copy(copied)
    return copied
  }

  private fun copy(candleDataSet: CandleDataSet) {
    super.copy(candleDataSet)
    candleDataSet.shadowWidth = shadowWidth
    candleDataSet.showCandleBar = showCandleBar
    candleDataSet.barSpace = barSpace
    candleDataSet.shadowColorSameAsCandle = shadowColorSameAsCandle
    candleDataSet.highLightColor = highLightColor
    candleDataSet.increasingPaintStyle = increasingPaintStyle
    candleDataSet.decreasingPaintStyle = decreasingPaintStyle
    candleDataSet.neutralColor = neutralColor
    candleDataSet.increasingColor = increasingColor
    candleDataSet.decreasingColor = decreasingColor
    candleDataSet.shadowColor = shadowColor
  }

  override fun calcMinMax(e: CandleEntry?) {
    if (e!!.low < yMin) yMin = e.low
    if (e.high > yMax) yMax = e.high
    calcMinMaxX(e)
  }

  override fun calcMinMaxY(e: CandleEntry) {
    if (e.high < yMin) yMin = e.high
    if (e.high > yMax) yMax = e.high
    if (e.low < yMin) yMin = e.low
    if (e.low > yMax) yMax = e.low
  }
}
