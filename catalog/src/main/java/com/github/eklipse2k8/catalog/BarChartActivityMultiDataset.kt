package com.github.eklipse2k8.catalog

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.github.eklipse2k8.charting.charts.BarChart
import com.github.eklipse2k8.charting.components.Legend
import com.github.eklipse2k8.charting.data.BarData
import com.github.eklipse2k8.charting.data.BarDataSet
import com.github.eklipse2k8.charting.data.BarEntry
import com.github.eklipse2k8.charting.data.Entry
import com.github.eklipse2k8.charting.formatter.IAxisValueFormatter
import com.github.eklipse2k8.charting.formatter.LargeValueFormatter
import com.github.eklipse2k8.charting.highlight.Highlight
import com.github.eklipse2k8.charting.listener.OnChartValueSelectedListener
import com.github.eklipse2k8.catalog.custom.MyMarkerView
import com.github.eklipse2k8.catalog.notimportant.DemoBase
import java.util.*

class BarChartActivityMultiDataset :
    DemoBase(), OnSeekBarChangeListener, OnChartValueSelectedListener {

  private lateinit var chart: BarChart
  private lateinit var seekBarX: SeekBar
  private lateinit var seekBarY: SeekBar
  private lateinit var tvX: TextView
  private lateinit var tvY: TextView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_barchart)
    title = "BarChartActivityMultiDataset"
    tvX = findViewById(R.id.tvXMax)
    tvX.textSize = 10f
    tvY = findViewById(R.id.tvYMax)
    seekBarX = findViewById(R.id.seekBar1)
    seekBarX.max = 50
    seekBarX.setOnSeekBarChangeListener(this)
    seekBarY = findViewById(R.id.seekBar2)
    seekBarY.setOnSeekBarChangeListener(this)
    chart = findViewById(R.id.chart1)
    chart.setOnChartValueSelectedListener(this)
    chart.description.isEnabled = false

    //        chart.setDrawBorders(true);

    // scaling can now only be done on x- and y-axis separately
    chart.setPinchZoom(false)
    chart.setDrawBarShadow(false)
    chart.setDrawGridBackground(false)

    // create a custom MarkerView (extend MarkerView) and specify the layout
    // to use for it
    val mv = MyMarkerView(this, R.layout.custom_marker_view)
    mv.setChartView(chart) // For bounds control
    chart.marker = mv // Set the marker to the chart
    seekBarX.progress = 10
    seekBarY.progress = 100
    val l = chart.legend
    l.verticalAlignment = Legend.LegendVerticalAlignment.TOP
    l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
    l.orientation = Legend.LegendOrientation.VERTICAL
    l.setDrawInside(true)
    l.yOffset = 0f
    l.xOffset = 10f
    l.yEntrySpace = 0f
    l.textSize = 8f
    val xAxis = chart.xAxis
    xAxis.granularity = 1f
    xAxis.setCenterAxisLabels(true)
    xAxis.valueFormatter = IAxisValueFormatter { value, _ -> value.toString() }
    val leftAxis = chart.axisLeft
    leftAxis.valueFormatter = LargeValueFormatter()
    leftAxis.setDrawGridLines(false)
    leftAxis.spaceTop = 35f
    leftAxis.axisMinimum = 0f // this replaces setStartAtZero(true)
    chart.axisRight.isEnabled = false
  }

  override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
    val groupSpace = 0.08f
    val barSpace = 0.03f // x4 DataSet
    val barWidth = 0.2f // x4 DataSet
    // (0.2 + 0.03) * 4 + 0.08 = 1.00 -> interval per "group"
    val groupCount = seekBarX.progress + 1
    val startYear = 1980
    val endYear = startYear + groupCount
    tvX.text = String.format(Locale.ENGLISH, "%d-%d", startYear, endYear)
    tvY.text = seekBarY.progress.toString()
    val values1 = ArrayList<BarEntry>()
    val values2 = ArrayList<BarEntry>()
    val values3 = ArrayList<BarEntry>()
    val values4 = ArrayList<BarEntry>()
    val randomMultiplier = seekBarY.progress * 100000f
    for (i in startYear until endYear) {
      values1.add(BarEntry(i.toFloat(), (Math.random() * randomMultiplier).toFloat()))
      values2.add(BarEntry(i.toFloat(), (Math.random() * randomMultiplier).toFloat()))
      values3.add(BarEntry(i.toFloat(), (Math.random() * randomMultiplier).toFloat()))
      values4.add(BarEntry(i.toFloat(), (Math.random() * randomMultiplier).toFloat()))
    }
    val set1: BarDataSet?
    val set2: BarDataSet?
    val set3: BarDataSet?
    val set4: BarDataSet?
    if (chart.data != null && chart.data!!.dataSetCount > 0) {
      set1 = chart.data!!.getDataSetByIndex(0) as BarDataSet?
      set2 = chart.data!!.getDataSetByIndex(1) as BarDataSet?
      set3 = chart.data!!.getDataSetByIndex(2) as BarDataSet?
      set4 = chart.data!!.getDataSetByIndex(3) as BarDataSet?
      if (set1 != null) {
        set1.entries = values1
      }
      if (set2 != null) {
        set2.entries = values2
      }
      if (set3 != null) {
        set3.entries = values3
      }
      if (set4 != null) {
        set4.entries = values4
      }
      chart.data!!.notifyDataChanged()
      chart.notifyDataSetChanged()
    } else {
      // create 4 DataSets
      set1 = BarDataSet(values1, "Company A")
      set1.color = Color.rgb(104, 241, 175)
      set2 = BarDataSet(values2, "Company B")
      set2.color = Color.rgb(164, 228, 251)
      set3 = BarDataSet(values3, "Company C")
      set3.color = Color.rgb(242, 247, 158)
      set4 = BarDataSet(values4, "Company D")
      set4.color = Color.rgb(255, 102, 0)
      val data = BarData(set1, set2, set3, set4)
      data.setValueFormatter(LargeValueFormatter())
      chart.data = data
    }

    // specify the width each bar should have
    chart.barData!!.barWidth = barWidth

    // restrict the x-axis range
    chart.xAxis.axisMinimum = startYear.toFloat()

    // barData.getGroupWith(...) is a helper that calculates the width each group needs based on the
    // provided parameters
    chart.xAxis.axisMaximum =
        startYear + chart.barData!!.getGroupWidth(groupSpace, barSpace) * groupCount
    chart.groupBars(startYear.toFloat(), groupSpace, barSpace)
    chart.invalidate()
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
                "https://github.com/PhilJay/MPAndroidChart/blob/master/MPChartExample/src/com/xxmassdeveloper/mpchartexample/BarChartActivityMultiDataset.java")
        startActivity(i)
      }
      R.id.actionToggleValues -> {
        for (set in chart.data!!.dataSets) set.setDrawValues(!set.isDrawValuesEnabled)
        chart.invalidate()
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
      R.id.actionToggleHighlight -> {
        if (chart.data != null) {
          chart.data!!.isHighlightEnabled = !chart.data!!.isHighlightEnabled
          chart.invalidate()
        }
      }
      R.id.actionSave -> {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_GRANTED) {
          saveToGallery()
        } else {
          requestStoragePermission(chart!!)
        }
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
    }
    return true
  }

  override fun saveToGallery() {
    saveToGallery(chart, "BarChartActivityMultiDataset")
  }

  override fun onStartTrackingTouch(seekBar: SeekBar) {}
  override fun onStopTrackingTouch(seekBar: SeekBar) {}
  override fun onValueSelected(e: Entry?, h: Highlight?) {
    Log.i("Activity", "Selected: ${e.toString()}, dataSet: ${h?.dataSetIndex}")
  }

  override fun onNothingSelected() {
    Log.i("Activity", "Nothing selected.")
  }
}
