package com.github.mikephil.charting.interfaces.dataprovider;

import androidx.annotation.Nullable;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;

public interface LineDataProvider extends BarLineScatterCandleBubbleDataProvider {

    @Nullable
    LineData getLineData();

    YAxis getAxis(YAxis.AxisDependency dependency);
}
