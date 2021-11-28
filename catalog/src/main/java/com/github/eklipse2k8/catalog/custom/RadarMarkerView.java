
package com.github.eklipse2k8.catalog.custom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.github.eklipse2k8.charting.components.MarkerView;
import com.github.eklipse2k8.charting.data.Entry;
import com.github.eklipse2k8.charting.highlight.Highlight;
import com.github.eklipse2k8.charting.utils.MPPointF;
import com.github.eklipse2k8.catalog.R;

import java.text.DecimalFormat;

/**
 * Custom implementation of the MarkerView.
 *
 * @author Philipp Jahoda
 */
@SuppressLint("ViewConstructor")
public class RadarMarkerView extends MarkerView {

    private final TextView tvContent;
    private final DecimalFormat format = new DecimalFormat("##0");

    public RadarMarkerView(@NonNull Context context, int layoutResource) {
        super(context, layoutResource);

        tvContent = findViewById(R.id.tvContent);
        tvContent.setTypeface(Typeface.createFromAsset(context.getAssets(), "OpenSans-Light.ttf"));
    }

    // runs every time the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    @Override
    public void refreshContent(@NonNull Entry e, Highlight highlight) {
        tvContent.setText(String.format("%s %%", format.format(e.getY())));

        super.refreshContent(e, highlight);
    }

    @NonNull
    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2), -getHeight() - 10);
    }
}
