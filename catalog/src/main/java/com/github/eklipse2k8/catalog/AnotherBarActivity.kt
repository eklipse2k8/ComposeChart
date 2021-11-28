package com.github.eklipse2k8.catalog

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.github.eklipse2k8.charting.charts.BarChart
import com.github.eklipse2k8.charting.components.XAxis.XAxisPosition
import com.github.eklipse2k8.charting.data.BarData
import com.github.eklipse2k8.charting.data.BarDataSet
import com.github.eklipse2k8.charting.data.BarEntry
import com.github.eklipse2k8.charting.interfaces.datasets.IBarDataSet
import com.github.eklipse2k8.charting.utils.ColorTemplate
import com.github.eklipse2k8.catalog.notimportant.DemoBase

class AnotherBarActivity : DemoBase(), OnSeekBarChangeListener {
  private lateinit var chart: BarChart
  private lateinit var seekBarX: SeekBar
  private lateinit var seekBarY: SeekBar
  private lateinit var tvX: TextView
  private lateinit var tvY: TextView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_barchart)
    title = "AnotherBarActivity"
    tvX = findViewById(R.id.tvXMax)
    tvY = findViewById(R.id.tvYMax)
    seekBarX = findViewById(R.id.seekBar1)
    seekBarX.setOnSeekBarChangeListener(this)
    seekBarY = findViewById(R.id.seekBar2)
    seekBarY.setOnSeekBarChangeListener(this)
    chart = findViewById(R.id.chart1)
    chart.description.isEnabled = false

    // if more than 60 entries are displayed in the chart, no values will be
    // drawn
    chart.setMaxVisibleValueCount(60)

    // scaling can now only be done on x- and y-axis separately
    chart.setPinchZoom(false)
    chart.setDrawBarShadow(false)
    chart.setDrawGridBackground(false)
    val xAxis = chart.xAxis
    xAxis.position = XAxisPosition.BOTTOM
    xAxis.setDrawGridLines(false)
    chart.axisLeft.setDrawGridLines(false)

    // setting data
    seekBarX.progress = 10
    seekBarY.progress = 100

    // add a nice and smooth animation
    chart.animateY(1500)
    chart.legend.isEnabled = false
  }

  override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
    tvX.text = seekBarX.progress.toString()
    tvY.text = seekBarY.progress.toString()
    val values = ArrayList<BarEntry>()
    for (i in 0 until seekBarX.progress) {
      val multi = (seekBarY.progress + 1).toFloat()
      val `val` = (Math.random() * multi).toFloat() + multi / 3
      values.add(BarEntry(i.toFloat(), `val`))
    }
    val set1: BarDataSet?
    if (chart.data != null && chart.data!!.dataSetCount > 0) {
      set1 = chart.data!!.getDataSetByIndex(0) as BarDataSet?
      set1!!.entries = values
      chart.data!!.notifyDataChanged()
      chart.notifyDataSetChanged()
    } else {
      set1 = BarDataSet(values, "Data Set")
      set1.setColors(*ColorTemplate.VORDIPLOM_COLORS)
      set1.setDrawValues(false)
      val dataSets = ArrayList<IBarDataSet>()
      dataSets.add(set1)
      val data = BarData(dataSets)
      chart.data = data
      chart.setFitBars(true)
    }
    chart.invalidate()
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.bar, menu)
    menu.removeItem(R.id.actionToggleIcons)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.viewGithub -> {
        val i = Intent(Intent.ACTION_VIEW)
        i.data =
            Uri.parse(
                "https://github.com/PhilJay/MPAndroidChart/blob/master/MPChartExample/src/com/xxmassdeveloper/mpchartexample/AnotherBarActivity.java")
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

  override fun saveToGallery() {
    saveToGallery(chart, "AnotherBarActivity")
  }

  override fun onStartTrackingTouch(seekBar: SeekBar) {}
  override fun onStopTrackingTouch(seekBar: SeekBar) {}
}
