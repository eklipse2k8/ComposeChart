package com.github.eklipse2k8.charting.charts

import com.github.eklipse2k8.charting.data.BarData
import com.github.eklipse2k8.charting.data.BarEntry
import com.github.eklipse2k8.charting.data.BarLineScatterCandleBubbleData
import com.github.eklipse2k8.charting.data.Entry
import com.github.eklipse2k8.charting.interfaces.datasets.IBarDataSet
import com.github.eklipse2k8.charting.interfaces.datasets.IBarLineScatterCandleBubbleDataSet

typealias AnyBarLineChart =
    BarLineChartBase<
        BarLineScatterCandleBubbleData<IBarLineScatterCandleBubbleDataSet<Entry>, Entry>,
        IBarLineScatterCandleBubbleDataSet<Entry>,
        Entry>

typealias AnyBarChart = BarLineChartBase<BarData, IBarDataSet, BarEntry>
