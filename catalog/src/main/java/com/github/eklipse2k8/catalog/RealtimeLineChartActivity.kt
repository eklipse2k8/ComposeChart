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
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.github.eklipse2k8.charting.charts.LineChart
import com.github.eklipse2k8.charting.components.Legend.LegendForm
import com.github.eklipse2k8.charting.components.YAxis.AxisDependency
import com.github.eklipse2k8.charting.data.Entry
import com.github.eklipse2k8.charting.data.LineData
import com.github.eklipse2k8.charting.data.LineDataSet
import com.github.eklipse2k8.charting.highlight.Highlight
import com.github.eklipse2k8.charting.listener.OnChartValueSelectedListener
import com.github.eklipse2k8.charting.utils.ColorTemplate.holoBlue
import com.github.eklipse2k8.catalog.notimportant.DemoBase

class RealtimeLineChartActivity : DemoBase(), OnChartValueSelectedListener {
  private lateinit var chart: LineChart
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_realtime_linechart)
    title = "RealtimeLineChartActivity"
    chart = findViewById(R.id.chart1)
    chart.setOnChartValueSelectedListener(this)

    // enable description text
    chart.description.isEnabled = true

    // enable touch gestures
    chart.isTouchEnabled = true

    // enable scaling and dragging
    chart.isDragEnabled = true
    chart.setScaleEnabled(true)
    chart.setDrawGridBackground(false)

    // if disabled, scaling can be done on x- and y-axis separately
    chart.setPinchZoom(true)

    // set an alternative background color
    chart.setBackgroundColor(Color.LTGRAY)
    val data = LineData()
    data.setValueTextColor(Color.WHITE)

    // add empty data
    chart.data = data

    // get the legend (only possible after setting data)
    val l = chart.legend

    // modify the legend ...
    l.form = LegendForm.LINE
    l.textColor = Color.WHITE
    val xl = chart.xAxis
    xl.textColor = Color.WHITE
    xl.setDrawGridLines(false)
    xl.setAvoidFirstLastClipping(true)
    xl.isEnabled = true
    val leftAxis = chart.axisLeft
    leftAxis.textColor = Color.WHITE
    leftAxis.axisMaximum = 100f
    leftAxis.axisMinimum = 0f
    leftAxis.setDrawGridLines(true)
    val rightAxis = chart.axisRight
    rightAxis.isEnabled = false
  }

  private fun addEntry() {
    val data = chart.data
    if (data != null) {
      var set = data.getDataSetByIndex(0)
      // set.addEntry(...); // can be called as well
      if (set == null) {
        set = createSet()
        data.addDataSet(set)
      }
      data.addEntry(Entry(set.entryCount.toFloat(), (Math.random() * 40).toFloat() + 30f), 0)
      data.notifyDataChanged()

      // let the chart know it's data has changed
      chart.notifyDataSetChanged()

      // limit the number of visible entries
      chart.setVisibleXRangeMaximum(120f)
      // chart.setVisibleYRange(30, AxisDependency.LEFT);

      // move to the latest entry
      chart.moveViewToX(data.entryCount.toFloat())
    }
  }

  private fun createSet(): LineDataSet {
    val set = LineDataSet(mutableListOf(), "Dynamic Data")
    set.axisDependency = AxisDependency.LEFT
    set.color = holoBlue
    set.setCircleColor(Color.WHITE)
    set.lineWidth = 2f
    set.circleRadius = 4f
    set.fillAlpha = 65
    set.fillColor = holoBlue
    set.highLightColor = Color.rgb(244, 117, 117)
    set.valueTextColor = Color.WHITE
    set.valueTextSize = 9f
    set.setDrawValues(false)
    return set
  }

  private var thread: Thread? = null
  private fun feedMultiple() {
    if (thread != null) thread!!.interrupt()
    val runnable = Runnable { addEntry() }
    thread =
        Thread {
          for (i in 0..999) {

            // Don't generate garbage runnables inside the loop.
            runOnUiThread(runnable)
            try {
              Thread.sleep(25)
            } catch (e: InterruptedException) {
              e.printStackTrace()
            }
          }
        }
    thread!!.start()
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.realtime, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.viewGithub -> {
        val i = Intent(Intent.ACTION_VIEW)
        i.data =
            Uri.parse(
                "https://github.com/PhilJay/MPAndroidChart/blob/master/MPChartExample/src/com/xxmassdeveloper/mpchartexample/RealtimeLineChartActivity.java")
        startActivity(i)
      }
      R.id.actionAdd -> {
        addEntry()
      }
      R.id.actionClear -> {
        chart.clearValues()
        Toast.makeText(this, "Chart cleared!", Toast.LENGTH_SHORT).show()
      }
      R.id.actionFeedMultiple -> {
        feedMultiple()
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

  override fun saveToGallery() {
    saveToGallery(chart!!, "RealtimeLineChartActivity")
  }

  override fun onValueSelected(e: Entry?, h: Highlight?) {
    Log.i("Entry selected", e.toString())
  }

  override fun onNothingSelected() {
    Log.i("Nothing selected", "Nothing selected.")
  }

  override fun onPause() {
    super.onPause()
    if (thread != null) {
      thread!!.interrupt()
    }
  }
}
