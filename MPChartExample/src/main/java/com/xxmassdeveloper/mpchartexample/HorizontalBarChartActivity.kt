package com.xxmassdeveloper.mpchartexample

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.github.eklipse2k8.charting.charts.HorizontalBarChart
import com.github.eklipse2k8.charting.components.Legend
import com.github.eklipse2k8.charting.components.XAxis.XAxisPosition
import com.github.eklipse2k8.charting.data.BarData
import com.github.eklipse2k8.charting.data.BarDataSet
import com.github.eklipse2k8.charting.data.BarEntry
import com.github.eklipse2k8.charting.data.Entry
import com.github.eklipse2k8.charting.highlight.Highlight
import com.github.eklipse2k8.charting.interfaces.datasets.IBarDataSet
import com.github.eklipse2k8.charting.listener.OnChartValueSelectedListener
import com.github.eklipse2k8.charting.utils.MPPointF.Companion.recycleInstance
import com.xxmassdeveloper.mpchartexample.notimportant.DemoBase

class HorizontalBarChartActivity :
    DemoBase(), OnSeekBarChangeListener, OnChartValueSelectedListener {
  private lateinit var chart: HorizontalBarChart
  private lateinit var seekBarX: SeekBar
  private lateinit var seekBarY: SeekBar
  private lateinit var tvX: TextView
  private lateinit var tvY: TextView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    window.setFlags(
        WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
    setContentView(R.layout.activity_horizontalbarchart)
    title = "HorizontalBarChartActivity"
    tvX = findViewById(R.id.tvXMax)
    tvY = findViewById(R.id.tvYMax)
    seekBarX = findViewById(R.id.seekBar1)
    seekBarY = findViewById(R.id.seekBar2)
    seekBarY.setOnSeekBarChangeListener(this)
    seekBarX.setOnSeekBarChangeListener(this)
    chart = findViewById(R.id.chart1)
    chart.setOnChartValueSelectedListener(this)
    chart.setDrawBarShadow(false)
    chart.setDrawValueAboveBar(true)
    chart.description.isEnabled = false

    // if more than 60 entries are displayed in the chart, no values will be
    // drawn
    chart.setMaxVisibleValueCount(60)

    // scaling can now only be done on x- and y-axis separately
    chart.setPinchZoom(false)

    // draw shadows for each bar that show the maximum value
    // chart.setDrawBarShadow(true);
    chart.setDrawGridBackground(false)
    val xl = chart.xAxis
    xl.position = XAxisPosition.BOTTOM
    xl.setDrawAxisLine(true)
    xl.setDrawGridLines(false)
    xl.granularity = 10f
    val yl = chart.axisLeft
    yl.setDrawAxisLine(true)
    yl.setDrawGridLines(true)
    yl.axisMinimum = 0f // this replaces setStartAtZero(true)
    val yr = chart.axisRight
    yr.setDrawAxisLine(true)
    yr.setDrawGridLines(false)
    yr.axisMinimum = 0f // this replaces setStartAtZero(true)
    chart.setFitBars(true)
    chart.animateY(2500)

    // setting data
    seekBarY.setProgress(50)
    seekBarX.setProgress(12)
    val l = chart.legend
    l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
    l.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
    l.orientation = Legend.LegendOrientation.HORIZONTAL
    l.setDrawInside(false)
    l.formSize = 8f
    l.xEntrySpace = 4f
  }

  private fun setData(count: Int, range: Float) {
    val barWidth = 9f
    val spaceForBar = 10f
    val values = ArrayList<BarEntry>()
    for (i in 0 until count) {
      val `val` = (Math.random() * range).toFloat()
      val entry =
          BarEntry(
              i * spaceForBar,
              `val`,
              null,
              ResourcesCompat.getDrawable(resources, R.drawable.star, null))
      values.add(entry)
    }
    val set1: BarDataSet?
    if (chart.data != null && chart.data!!.dataSetCount > 0) {
      set1 = chart.data!!.getDataSetByIndex(0) as BarDataSet?
      if (set1 != null) {
        set1.entries = values
        chart.data!!.notifyDataChanged()
        chart.notifyDataSetChanged()
      }
    } else {
      set1 = BarDataSet(values, "DataSet 1")
      set1.setDrawIcons(false)
      val dataSets = ArrayList<IBarDataSet>()
      dataSets.add(set1)
      val data = BarData(dataSets)
      data.setValueTextSize(10f)
      data.barWidth = barWidth
      chart.data = data
    }
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
                "https://github.com/PhilJay/MPAndroidChart/blob/master/MPChartExample/src/com/xxmassdeveloper/mpchartexample/HorizontalBarChartActivity.java")
        startActivity(i)
      }
      R.id.actionToggleValues -> {
        chart.data?.dataSets?.forEach { set ->
          set.setDrawValues(!set.isDrawValuesEnabled)
        }
        chart.invalidate()
      }
      R.id.actionToggleIcons -> {
        chart.data?.dataSets?.forEach { set ->
          set.setDrawIcons(!set.isDrawIconsEnabled)
        }
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
          requestStoragePermission(chart!!)
        }
      }
    }
    return true
  }

  override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
    tvX!!.text = seekBarX!!.progress.toString()
    tvY!!.text = seekBarY!!.progress.toString()
    setData(seekBarX!!.progress, seekBarY!!.progress.toFloat())
    chart.setFitBars(true)
    chart.invalidate()
  }

  override fun saveToGallery() {
    saveToGallery(chart!!, "HorizontalBarChartActivity")
  }

  override fun onStartTrackingTouch(seekBar: SeekBar) {}

  override fun onStopTrackingTouch(seekBar: SeekBar) {}

  private val onValueSelectedRectF = RectF()

  override fun onValueSelected(e: Entry?, h: Highlight?) {
    if (e == null) return

    val barEntry = e as BarEntry
    chart.getBarBounds(barEntry, onValueSelectedRectF)
    val axixDependency = h?.let { chart.data?.getDataSetByIndex(it.dataSetIndex) }?.axisDependency
    val position = axixDependency?.let { chart.getPosition(e, it) } ?: return
    Log.i("bounds", onValueSelectedRectF.toString())
    Log.i("position", position.toString())
    recycleInstance(position)
  }

  override fun onNothingSelected() {}
}
