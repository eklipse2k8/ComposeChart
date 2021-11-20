package com.github.mikephil.charting.interfaces.dataprovider;

import androidx.annotation.Nullable;

import com.github.mikephil.charting.data.BubbleData;

public interface BubbleDataProvider extends BarLineScatterCandleBubbleDataProvider {

    @Nullable
    BubbleData getBubbleData();
}
