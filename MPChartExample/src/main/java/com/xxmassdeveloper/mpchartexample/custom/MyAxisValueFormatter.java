package com.xxmassdeveloper.mpchartexample.custom;

import androidx.annotation.NonNull;

import com.github.eklipse2k8.charting.components.AxisBase;
import com.github.eklipse2k8.charting.formatter.IAxisValueFormatter;

import java.text.DecimalFormat;

public class MyAxisValueFormatter implements IAxisValueFormatter
{

    @NonNull
    private final DecimalFormat mFormat;

    public MyAxisValueFormatter() {
        mFormat = new DecimalFormat("###,###,###,##0.0");
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        return mFormat.format(value) + " $";
    }
}
