package com.github.eklipse2k8.charting.renderer

import android.graphics.Canvas
import android.graphics.Path
import com.github.eklipse2k8.charting.animation.ChartAnimator
import com.github.eklipse2k8.charting.interfaces.datasets.ILineScatterCandleRadarDataSet
import com.github.eklipse2k8.charting.utils.ViewPortHandler

/** Created by Philipp Jahoda on 11/07/15. */
abstract class LineScatterCandleRadarRenderer(
    animator: ChartAnimator,
    viewPortHandler: ViewPortHandler
) : BarLineScatterCandleBubbleRenderer(animator, viewPortHandler) {
  /**
   * path that is used for drawing highlight-lines (drawLines(...) cannot be used because of dashes)
   */
  private val mHighlightLinePath = Path()

  /**
   * Draws vertical & horizontal highlight-lines if enabled.
   *
   * @param c
   * @param x x-position of the highlight line intersection
   * @param y y-position of the highlight line intersection
   * @param set the currently drawn dataset
   */
  protected fun drawHighlightLines(
      c: Canvas,
      x: Float,
      y: Float,
      set: ILineScatterCandleRadarDataSet<*>
  ) {

    // set color and stroke-width
    highlightPaint.color = set.highLightColor
    highlightPaint.strokeWidth = set.highlightLineWidth

    // draw highlighted lines (if enabled)
    highlightPaint.pathEffect = set.dashPathEffectHighlight

    // draw vertical highlight lines
    if (set.isVerticalHighlightIndicatorEnabled) {

      // create vertical path
      mHighlightLinePath.reset()
      mHighlightLinePath.moveTo(x, viewPortHandler.contentTop())
      mHighlightLinePath.lineTo(x, viewPortHandler.contentBottom())
      c.drawPath(mHighlightLinePath, highlightPaint)
    }

    // draw horizontal highlight lines
    if (set.isHorizontalHighlightIndicatorEnabled) {

      // create horizontal path
      mHighlightLinePath.reset()
      mHighlightLinePath.moveTo(viewPortHandler.contentLeft(), y)
      mHighlightLinePath.lineTo(viewPortHandler.contentRight(), y)
      c.drawPath(mHighlightLinePath, highlightPaint)
    }
  }
}
