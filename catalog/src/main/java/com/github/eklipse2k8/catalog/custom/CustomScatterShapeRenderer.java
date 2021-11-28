package com.github.eklipse2k8.catalog.custom;

import android.graphics.Canvas;
import android.graphics.Paint;

import androidx.annotation.NonNull;

import com.github.eklipse2k8.charting.interfaces.datasets.IScatterDataSet;
import com.github.eklipse2k8.charting.renderer.scatter.IShapeRenderer;
import com.github.eklipse2k8.charting.utils.ViewPortHandler;

/**
 * Custom shape renderer that draws a single line.
 * Created by philipp on 26/06/16.
 */
public class CustomScatterShapeRenderer implements IShapeRenderer
{

    @Override
    public void renderShape(@NonNull Canvas c, @NonNull IScatterDataSet dataSet, ViewPortHandler viewPortHandler,
                            float posX, float posY, Paint renderPaint) {

        final float shapeHalf = dataSet.getScatterShapeSize() / 2f;

        c.drawLine(
                posX - shapeHalf,
                posY - shapeHalf,
                posX + shapeHalf,
                posY + shapeHalf,
                renderPaint);
    }
}
