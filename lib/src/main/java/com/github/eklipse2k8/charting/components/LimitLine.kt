package com.github.eklipse2k8.charting.components

import android.graphics.DashPathEffect
import android.graphics.Paint.Style
import android.graphics.Paint.Style.FILL_AND_STROKE
import androidx.core.math.MathUtils.clamp
import com.github.eklipse2k8.charting.components.LimitLine.LimitLabelPosition.RIGHT_TOP
import com.github.eklipse2k8.charting.utils.ColorPalette
import com.github.eklipse2k8.charting.utils.DashedLine
import com.github.eklipse2k8.charting.utils.Utils

/**
 * The limit line is an additional feature for all Line-, Bar- and ScatterCharts. It allows the
 * displaying of an additional line in the chart that marks a certain maximum / limit on the
 * specified axis (x- or y-axis).
 *
 * @property limit the position (the value) on the y-axis (y-value) or x-axis (xIndex) where this
 * line should appear
 * @property label provide "" if no label is required
 * @property lineColor Gets/Sets the [color] for this LimitLine. Make sure to use
 * getResources().getColor(...)
 * @property textStyle Gets/Sets the color of the value-text that is drawn next to the LimitLine.
 * Default: Paint.Style.FILL_AND_STROKE
 * @property labelPosition Gets/Sets the position of the LimitLine value label (either on the right
 * or on the left edge of the chart). Not supported for RadarChart.
 *
 * @author Philipp Jahoda
 */
class LimitLine(
    val limit: Float = 0f,
    val label: String = "",
    val lineColor: Int = ColorPalette.GoldenYellow,
    val textStyle: Style = FILL_AND_STROKE,
    val labelPosition: LimitLabelPosition = RIGHT_TOP,
) : ComponentBase() {

  /** enum that indicates the position of the LimitLine label */
  enum class LimitLabelPosition {
    LEFT_TOP,
    LEFT_BOTTOM,
    RIGHT_TOP,
    RIGHT_BOTTOM
  }

  /**
   * get/set the line width of the chart (min = 0.2f, max = 12f); default 2f NOTE: thinner line ==
   * better performance, thicker line == worse performance
   */
  var lineWidth: Float = 2f
    set(value) {
      field = Utils.convertDpToPixel(clamp(value, 0.2f, 12.0f))
    }

  private val dashedLine = DashedLine()

  /** the path effect of this LimitLine that makes dashed lines possible */
  val dashPathEffect: DashPathEffect? by dashedLine::get

  /**
   * Enables the line to be drawn in dashed mode, e.g. like this "- - - - - -"
   *
   * @param lineLength the length of the line pieces
   * @param spaceLength the length of space inbetween the pieces
   * @param phase offset, in degrees (normally, use 0)
   */
  fun enableDashedLine(lineLength: Float, spaceLength: Float, phase: Float) {
    dashedLine.set(lineLength, spaceLength, phase)
  }
}
