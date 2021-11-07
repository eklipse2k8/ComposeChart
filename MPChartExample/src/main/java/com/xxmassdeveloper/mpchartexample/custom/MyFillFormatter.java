package com.xxmassdeveloper.mpchartexample.custom;

import com.github.eklipse2k8.charting.formatter.IFillFormatter;
import com.github.eklipse2k8.charting.interfaces.dataprovider.LineDataProvider;
import com.github.eklipse2k8.charting.interfaces.datasets.ILineDataSet;

/**
 * Created by Philipp Jahoda on 12/09/15.
 */
@SuppressWarnings("unused")
public class MyFillFormatter implements IFillFormatter
{

    private final float fillPos;

    public MyFillFormatter(float fillPos) {
        this.fillPos = fillPos;
    }

    @Override
    public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
        // your logic could be here
        return fillPos;
    }
}
