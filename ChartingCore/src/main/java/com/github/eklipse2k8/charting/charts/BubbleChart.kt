package com.github.eklipse2k8.charting.charts

import android.content.Context
import android.util.AttributeSet
import com.github.eklipse2k8.charting.data.BubbleData
import com.github.eklipse2k8.charting.data.BubbleEntry
import com.github.eklipse2k8.charting.interfaces.dataprovider.BubbleDataProvider
import com.github.eklipse2k8.charting.interfaces.datasets.IBubbleDataSet
import com.github.eklipse2k8.charting.renderer.BubbleChartRenderer
import com.github.eklipse2k8.charting.renderer.DataRenderer

/**
 * The BubbleChart. Draws bubbles. Bubble chart implementation: Copyright 2015 Pierre-Marc Airoldi
 * Licensed under Apache License 2.0. In the BubbleChart, it is the area of the bubble, not the
 * radius or diameter of the bubble that conveys the data.
 *
 * @author Philipp Jahoda
 */
class BubbleChart
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    BarLineChartBase<BubbleData, IBubbleDataSet, BubbleEntry>(context, attrs, defStyleAttr),
    BubbleDataProvider {

  override val dataRenderer: DataRenderer = BubbleChartRenderer(this, mAnimator, mViewPortHandler)

  override val bubbleData: BubbleData?
    get() = data
}
