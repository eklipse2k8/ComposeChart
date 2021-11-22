package com.xxmassdeveloper.mpchartexample

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.github.eklipse2k8.charting.charts.BarChart
import com.github.eklipse2k8.charting.components.Legend
import com.github.eklipse2k8.charting.components.Legend.LegendForm
import com.github.eklipse2k8.charting.data.BarData
import com.github.eklipse2k8.charting.data.BarDataSet
import com.github.eklipse2k8.charting.data.BarEntry
import com.github.eklipse2k8.charting.utils.FileUtils.loadBarEntriesFromAssets
import com.xxmassdeveloper.mpchartexample.notimportant.DemoBase

class BarChartActivitySinus : DemoBase(), OnSeekBarChangeListener {
  private lateinit var chart: BarChart
  private lateinit var seekBarX: SeekBar
  private lateinit var tvX: TextView
  private lateinit var data: List<BarEntry>

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_barchart_sinus)
    title = "BarChartActivitySinus"
    data = loadBarEntriesFromAssets(assets, "othersine.txt")
    tvX = findViewById(R.id.tvValueCount)
    seekBarX = findViewById(R.id.seekbarValues)
    chart = findViewById(R.id.chart1)
    chart.setDrawBarShadow(false)
    chart.setDrawValueAboveBar(true)
    chart.description.isEnabled = false

    // if more than 60 entries are displayed in the chart, no values will be
    // drawn
    chart.setMaxVisibleValueCount(60)

    // scaling can now only be done on x- and y-axis separately
    chart.setPinchZoom(false)

    // draw shadows for each bar that show the maximum value
    chart.setDrawGridBackground(false)
    chart.xAxis.isEnabled = false
    with(chart.axisLeft) {
      typeface = tfLight
      setLabelCount(6, false)
      axisMinimum = -2.5f
      axisMaximum = 2.5f
      isGranularityEnabled = true
      granularity = 0.1f
    }
    with(chart.axisRight) {
      setDrawGridLines(false)
      typeface = tfLight
      setLabelCount(6, false)
      axisMinimum = -2.5f
      axisMaximum = 2.5f
      granularity = 0.1f
    }
    seekBarX.setOnSeekBarChangeListener(this)
    seekBarX.progress = 150 // set data
    with(chart.legend) {
      verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
      horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
      orientation = Legend.LegendOrientation.HORIZONTAL
      setDrawInside(false)
      form = LegendForm.SQUARE
      formSize = 9f
      textSize = 11f
      xEntrySpace = 4f
    }
    chart.animateXY(1500, 1500)
  }

  private fun setData(count: Int) {
    val entries = ArrayList<BarEntry>()
    for (i in 0 until count) {
      entries.add(data[i])
    }
    val set: BarDataSet?
    if (chart.data != null && chart.data!!.dataSetCount > 0) {
      set = chart.data!!.getDataSetByIndex(0) as BarDataSet?
      if (set != null) {
        set.entries = entries
      }
      chart.data!!.notifyDataChanged()
      chart.notifyDataSetChanged()
    } else {
      set = BarDataSet(entries, "Sinus Function")
      set.color = Color.rgb(240, 120, 124)
    }
    val data = BarData(set!!)
    data.setValueTextSize(10f)
    data.setValueTypeface(tfLight)
    data.setDrawValues(false)
    data.barWidth = 0.8f
    chart.data = data
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.bar, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.viewGithub -> {
        val i = Intent(Intent.ACTION_VIEW)
        i.data =
            Uri.parse(
                "https://github.com/PhilJay/MPAndroidChart/blob/master/MPChartExample/src/com/xxmassdeveloper/mpchartexample/BarChartActivitySinus.java")
        startActivity(i)
      }
      R.id.actionToggleValues -> {
        for (set in chart.data!!.dataSets) set.setDrawValues(!set.isDrawValuesEnabled)
        chart.invalidate()
      }
      R.id.actionToggleHighlight -> {
        if (chart.data != null) {
          chart.data!!.isHighlightEnabled = !chart.data!!.isHighlightEnabled
          chart.invalidate()
        }
      }
      R.id.actionTogglePinch -> {
        chart.setPinchZoom(!chart.isPinchZoomEnabled)
        chart.invalidate()
      }
      R.id.actionToggleAutoScaleMinMax -> {
        chart.isAutoScaleMinMaxEnabled = !chart.isAutoScaleMinMaxEnabled
        chart.notifyDataSetChanged()
      }
      R.id.actionToggleBarBorders -> {
        for (set in chart.data!!.dataSets) (set as BarDataSet).barBorderWidth =
            if (set.barBorderWidth == 1f) 0f else 1f
        chart.invalidate()
      }
      R.id.animateX -> {
        chart.animateX(2000)
      }
      R.id.animateY -> {
        chart.animateY(2000)
      }
      R.id.animateXY -> {
        chart.animateXY(2000, 2000)
      }
      R.id.actionSave -> {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_GRANTED) {
          saveToGallery()
        } else {
          requestStoragePermission(chart)
        }
      }
    }
    return true
  }

  override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
    tvX.text = seekBarX.progress.toString()
    setData(seekBarX.progress)
    chart.invalidate()
  }

  override fun saveToGallery() {
    saveToGallery(chart, "BarChartActivitySinus")
  }

  override fun onStartTrackingTouch(seekBar: SeekBar) {}
  override fun onStopTrackingTouch(seekBar: SeekBar) {}
}
