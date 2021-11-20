package com.github.mikephil.charting.interfaces.dataprovider;

import androidx.annotation.Nullable;

import com.github.mikephil.charting.data.ScatterData;

public interface ScatterDataProvider extends BarLineScatterCandleBubbleDataProvider {

    @Nullable
    ScatterData getScatterData();
}
