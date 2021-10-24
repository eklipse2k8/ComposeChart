package com.github.mikephil.charting.data

import com.github.mikephil.charting.utils.Utils.convertDpToPixel
import com.github.mikephil.charting.data.BarLineScatterCandleBubbleDataSet.copy
import com.github.mikephil.charting.data.BarLineScatterCandleBubbleDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineScatterCandleRadarDataSet
import android.graphics.DashPathEffect
import com.github.mikephil.charting.data.LineScatterCandleRadarDataSet

/**
 * Created by Philipp Jahoda on 11/07/15.
 */
abstract class LineScatterCandleRadarDataSet<T : Entry?>(yVals: List<T>?, label: String?) :
  BarLineScatterCandleBubbleDataSet<T>(yVals, label!!), ILineScatterCandleRadarDataSet<T> {
  protected var mDrawVerticalHighlightIndicator = true
  protected var mDrawHorizontalHighlightIndicator = true

  /** the width of the highlight indicator lines  */
  protected var mHighlightLineWidth = 0.5f

  /** the path effect for dashed highlight-lines  */
  protected var mHighlightDashPathEffect: DashPathEffect? = null

  /**
   * Enables / disables the horizontal highlight-indicator. If disabled, the indicator is not drawn.
   * @param enabled
   */
  fun setDrawHorizontalHighlightIndicator(enabled: Boolean) {
    mDrawHorizontalHighlightIndicator = enabled
  }

  /**
   * Enables / disables the vertical highlight-indicator. If disabled, the indicator is not drawn.
   * @param enabled
   */
  fun setDrawVerticalHighlightIndicator(enabled: Boolean) {
    mDrawVerticalHighlightIndicator = enabled
  }

  /**
   * Enables / disables both vertical and horizontal highlight-indicators.
   * @param enabled
   */
  fun setDrawHighlightIndicators(enabled: Boolean) {
    setDrawVerticalHighlightIndicator(enabled)
    setDrawHorizontalHighlightIndicator(enabled)
  }

  override fun isVerticalHighlightIndicatorEnabled(): Boolean {
    return mDrawVerticalHighlightIndicator
  }

  override fun isHorizontalHighlightIndicatorEnabled(): Boolean {
    return mDrawHorizontalHighlightIndicator
  }

  /**
   * Sets the width of the highlight line in dp.
   * @param width
   */
  fun setHighlightLineWidth(width: Float) {
    mHighlightLineWidth = convertDpToPixel(width)
  }

  override fun getHighlightLineWidth(): Float {
    return mHighlightLineWidth
  }

  /**
   * Enables the highlight-line to be drawn in dashed mode, e.g. like this "- - - - - -"
   *
   * @param lineLength the length of the line pieces
   * @param spaceLength the length of space inbetween the line-pieces
   * @param phase offset, in degrees (normally, use 0)
   */
  fun enableDashedHighlightLine(lineLength: Float, spaceLength: Float, phase: Float) {
    mHighlightDashPathEffect = DashPathEffect(
      floatArrayOf(
        lineLength, spaceLength
      ), phase
    )
  }

  /**
   * Disables the highlight-line to be drawn in dashed mode.
   */
  fun disableDashedHighlightLine() {
    mHighlightDashPathEffect = null
  }

  /**
   * Returns true if the dashed-line effect is enabled for highlight lines, false if not.
   * Default: disabled
   *
   * @return
   */
  val isDashedHighlightLineEnabled: Boolean
    get() = if (mHighlightDashPathEffect == null) false else true

  override fun getDashPathEffectHighlight(): DashPathEffect {
    return mHighlightDashPathEffect!!
  }

  protected fun copy(lineScatterCandleRadarDataSet: LineScatterCandleRadarDataSet<*>) {
    super.copy(lineScatterCandleRadarDataSet)
    lineScatterCandleRadarDataSet.mDrawHorizontalHighlightIndicator =
      mDrawHorizontalHighlightIndicator
    lineScatterCandleRadarDataSet.mDrawVerticalHighlightIndicator = mDrawVerticalHighlightIndicator
    lineScatterCandleRadarDataSet.mHighlightLineWidth = mHighlightLineWidth
    lineScatterCandleRadarDataSet.mHighlightDashPathEffect = mHighlightDashPathEffect
  }

  init {
    mHighlightLineWidth = convertDpToPixel(0.5f)
  }
}