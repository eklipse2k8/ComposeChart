package com.github.eklipse2k8.charting.components

import android.graphics.DashPathEffect
import com.github.eklipse2k8.charting.components.Legend.LegendForm
import com.github.eklipse2k8.charting.utils.ColorTemplate

/**
 *
 * @param label The legend entry text. A `null` label will start a group.
 * @param form The form to draw for this entry. `NONE` will avoid drawing a form, and any related
 * space. `EMPTY` will avoid drawing a form, but keep its space. `DEFAULT` will use the Legend's
 * default.
 * @param formSize Form size will be considered except for when .None is used. Set to NaN to use the
 * legend's default.
 * @param formLineWidth Line width used for shapes that consist of lines. Set to NaN to use the
 * legend's default.
 * @param formLineDashEffect Line dash path effect used for shapes that consist of lines. Set to nil
 * to use the legend's default.
 * @param formColor The color for drawing the form.
 */
data class LegendEntry(
    val label: String? = null,
    val form: LegendForm = LegendForm.DEFAULT,
    val formSize: Float = Float.NaN,
    val formLineWidth: Float = Float.NaN,
    val formLineDashEffect: DashPathEffect? = null,
    val formColor: Int = ColorTemplate.COLOR_NONE,
)

//class LegendEntry {
//  constructor()
//
//  constructor(
//      label: String?,
//      form: LegendForm,
//      formSize: Float,
//      formLineWidth: Float,
//      formLineDashEffect: DashPathEffect?,
//      formColor: Int
//  ) {
//    this.label = label
//    this.form = form
//    this.formSize = formSize
//    this.formLineWidth = formLineWidth
//    this.formLineDashEffect = formLineDashEffect
//    this.formColor = formColor
//  }
//  @JvmField var label: String? = null
//  @JvmField var form = LegendForm.DEFAULT
//  @JvmField var formSize = Float.NaN
//  @JvmField var formLineWidth = Float.NaN
//  @JvmField var formLineDashEffect: DashPathEffect? = null
//  @JvmField var formColor = ColorTemplate.COLOR_NONE
//}
