package com.github.mikephil.charting.data

import android.graphics.Color
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet
import com.github.mikephil.charting.utils.ColorTemplate

class RadarDataSet(yVals: MutableList<RadarEntry>, label: String) :
    LineRadarDataSet<RadarEntry>(yVals, label), IRadarDataSet {
  /// Sets whether highlight circle should be drawn or not
  /// Returns true if highlight circle should be drawn, false if not
  /// flag indicating whether highlight circle should be drawn or not
  override var isDrawHighlightCircleEnabled = false
  override var highlightCircleFillColor = Color.WHITE

  /// Sets the stroke color for highlight circle.
  /// Set to Utils.COLOR_NONE in order to use the color of the dataset;
  /// Returns the stroke color for highlight circle.
  /// If Utils.COLOR_NONE, the color of the dataset is taken.
  /// The stroke color for highlight circle.
  /// If Utils.COLOR_NONE, the color of the dataset is taken.
  override var highlightCircleStrokeColor = ColorTemplate.COLOR_NONE
  override var highlightCircleStrokeAlpha = (0.3 * 255).toInt()
  override var highlightCircleInnerRadius = 3.0f
  override var highlightCircleOuterRadius = 4.0f
  override var highlightCircleStrokeWidth = 2.0f

  override fun getEntryIndex(e: RadarEntry): Int = super.getEntryIndex(e)

  override fun copy(): DataSet<RadarEntry> {
    val entries = mutableListOf<RadarEntry>()
    mEntries?.forEach { entry -> entries.add(entry.copy()) }
    val copied = RadarDataSet(entries, label!!)
    copy(copied)
    return copied
  }

  private fun copy(radarDataSet: RadarDataSet) {
    super.copy(radarDataSet)
    radarDataSet.isDrawHighlightCircleEnabled = isDrawHighlightCircleEnabled
    radarDataSet.highlightCircleFillColor = highlightCircleFillColor
    radarDataSet.highlightCircleInnerRadius = highlightCircleInnerRadius
    radarDataSet.highlightCircleStrokeAlpha = highlightCircleStrokeAlpha
    radarDataSet.highlightCircleStrokeColor = highlightCircleStrokeColor
    radarDataSet.highlightCircleStrokeWidth = highlightCircleStrokeWidth
  }
}
