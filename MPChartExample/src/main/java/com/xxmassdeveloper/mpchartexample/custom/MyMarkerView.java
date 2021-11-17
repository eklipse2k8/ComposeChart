
package com.xxmassdeveloper.mpchartexample.custom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.github.eklipse2k8.charting.components.MarkerView;
import com.github.eklipse2k8.charting.data.CandleEntry;
import com.github.eklipse2k8.charting.data.Entry;
import com.github.eklipse2k8.charting.highlight.Highlight;
import com.github.eklipse2k8.charting.utils.MPPointF;
import com.github.eklipse2k8.charting.utils.Utils;
import com.xxmassdeveloper.mpchartexample.R;

/**
 * Custom implementation of the MarkerView.
 *
 * @author Philipp Jahoda
 */
@SuppressLint("ViewConstructor")
public class MyMarkerView extends MarkerView {

    private final TextView tvContent;

    public MyMarkerView(@NonNull Context context, int layoutResource) {
        super(context, layoutResource);

        tvContent = findViewById(R.id.tvContent);
    }

    // runs every time the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    @Override
    public void refreshContent(Entry e, Highlight highlight) {

        if (e instanceof CandleEntry) {

            CandleEntry ce = (CandleEntry) e;

            tvContent.setText(Utils.INSTANCE.formatNumber(ce.getHigh(), 0, true));
        } else {

            tvContent.setText(Utils.INSTANCE.formatNumber(e.getY(), 0, true));
        }

        super.refreshContent(e, highlight);
    }

    @NonNull
    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2), -getHeight());
    }
}
