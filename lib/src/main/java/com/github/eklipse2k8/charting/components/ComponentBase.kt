package com.github.eklipse2k8.charting.components

import android.graphics.Color
import android.graphics.Typeface
import androidx.annotation.ColorInt
import com.github.eklipse2k8.charting.utils.Utils

/**
 * This class encapsulates everything both Axis, Legend and LimitLines have in common.
 *
 * @param isEnabled Set this to true if this component should be enabled (should be drawn), false if
 * not. If disabled, nothing of this component will be drawn. Default: true flag that indicates if
 * this axis / legend is enabled or not
 * @param typeface the typeface used for the labels
 * @param textColor the text color to use for the labels
 * @param xOffset Returns the used offset on the x-axis for drawing the axis or legend labels. This
 * offset is applied before and after the label.
 * @param yOffset Returns the used offset on the x-axis for drawing the axis labels. This offset is
 * applied before and after the label.
 * @param textSize the text size that is currently set for the labels, in pixels
 *
 * @author Philipp Jahoda
 */
abstract class ComponentBase(
    var isEnabled: Boolean = true,
    var typeface: Typeface? = null,
    @ColorInt var textColor: Int = Color.BLACK,
    var xOffset: Float = 5f,
    var yOffset: Float = 5f,
    var textSize: Float = Utils.convertDpToPixel(10f),
)
