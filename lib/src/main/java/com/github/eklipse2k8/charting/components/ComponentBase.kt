package com.github.eklipse2k8.charting.components

import android.graphics.Color.BLACK
import android.graphics.Typeface as SysTypeface
import androidx.annotation.ColorInt
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.eklipse2k8.charting.annotation.ExperimentalComponentApi
import com.github.eklipse2k8.charting.utils.Utils

val DEFAULT_OFFSET = 5f.dp

/** This class encapsulates everything both Axis, Legend and LimitLines have in common. */
@ExperimentalComponentApi
@Immutable
data class ComponentBaseConfig(
    val isEnabled: Boolean = true,
    val textStyle: TextStyle =
        TextStyle(
            color = Color.Black,
            fontSize = 10f.sp,
        ),
    val xOffset: Dp = DEFAULT_OFFSET,
    val yOffset: Dp = DEFAULT_OFFSET,
)

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
open class ComponentBase(
    var isEnabled: Boolean = true,
    var typeface: SysTypeface? = null,
    @ColorInt var textColor: Int = BLACK,
    var xOffset: Float = 5f,
    var yOffset: Float = 5f,
    var textSize: Float = Utils.convertDpToPixel(10f),
)
