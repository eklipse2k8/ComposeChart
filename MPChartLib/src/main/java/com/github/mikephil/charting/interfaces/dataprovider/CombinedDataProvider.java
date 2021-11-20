package com.github.mikephil.charting.interfaces.dataprovider;

import androidx.annotation.Nullable;

import com.github.mikephil.charting.data.CombinedData;

/**
 * Created by philipp on 11/06/16.
 */
public interface CombinedDataProvider extends LineDataProvider, BarDataProvider, BubbleDataProvider, CandleDataProvider, ScatterDataProvider {

    @Nullable
    CombinedData getCombinedData();
}
