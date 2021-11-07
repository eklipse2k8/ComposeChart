
package com.xxmassdeveloper.mpchartexample.custom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.TextView;

import com.github.eklipse2k8.charting.components.MarkerView;
import com.github.eklipse2k8.charting.data.BarEntry;
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
@SuppressWarnings("unused")
@SuppressLint("ViewConstructor")
public class StackedBarsMarkerView extends MarkerView {

    private TextView tvContent;

    public StackedBarsMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);

        tvContent = findViewById(R.id.tvContent);
    }

    // runs every time the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    @Override
    public void refreshContent(Entry e, Highlight highlight) {

        if (e instanceof BarEntry) {

            BarEntry be = (BarEntry) e;

            if(be.getYVals() != null) {

                // draw the stack value
                tvContent.setText(Utils.INSTANCE.formatNumber(be.getYVals()[highlight.getStackIndex()], 0, true));
            } else {
                tvContent.setText(Utils.INSTANCE.formatNumber(be.getY(), 0, true));
            }
        } else {

            tvContent.setText(Utils.INSTANCE.formatNumber(e.getY(), 0, true));
        }

        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2), -getHeight());
    }
}
