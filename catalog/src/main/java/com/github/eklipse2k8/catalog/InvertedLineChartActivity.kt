package com.github.eklipse2k8.catalog

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
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
import com.github.eklipse2k8.charting.charts.LineChart
import com.github.eklipse2k8.charting.components.Legend.LegendForm
import com.github.eklipse2k8.charting.data.Entry
import com.github.eklipse2k8.charting.data.LineData
import com.github.eklipse2k8.charting.data.LineDataSet
import com.github.eklipse2k8.charting.highlight.Highlight
import com.github.eklipse2k8.charting.listener.OnChartValueSelectedListener
import com.github.eklipse2k8.charting.utils.EntryXComparator
import com.github.eklipse2k8.catalog.custom.MyMarkerView
import com.github.eklipse2k8.catalog.notimportant.DemoBase
import java.util.*

class InvertedLineChartActivity :
    DemoBase(), OnSeekBarChangeListener, OnChartValueSelectedListener {
  private lateinit var chart: LineChart
  private lateinit var seekBarX: SeekBar
  private lateinit var seekBarY: SeekBar
  private lateinit var tvX: TextView
  private lateinit var tvY: TextView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    window.setFlags(
        WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
    setContentView(R.layout.activity_linechart)
    title = "InvertedLineChartActivity"
    tvX = findViewById(R.id.tvXMax)
    tvY = findViewById(R.id.tvYMax)
    seekBarX = findViewById(R.id.seekBar1)
    seekBarY = findViewById(R.id.seekBar2)
    seekBarY.setOnSeekBarChangeListener(this)
    seekBarX.setOnSeekBarChangeListener(this)
    chart = findViewById(R.id.chart1)
    chart.setOnChartValueSelectedListener(this)
    chart.setDrawGridBackground(false)

    // no description text
    chart.description.isEnabled = false

    // enable touch gestures
    chart.isTouchEnabled = true

    // enable scaling and dragging
    chart.isDragEnabled = true
    chart.setScaleEnabled(true)

    // if disabled, scaling can be done on x- and y-axis separately
    chart.setPinchZoom(true)

    // set an alternative background color
    // chart.setBackgroundColor(Color.GRAY);

    // create a custom MarkerView (extend MarkerView) and specify the layout
    // to use for it
    val mv = MyMarkerView(this, R.layout.custom_marker_view)
    mv.setChartView(chart) // For bounds control
    chart.marker = mv // Set the marker to the chart
    val xl = chart.xAxis
    xl.setAvoidFirstLastClipping(true)
    xl.axisMinimum = 0f
    val leftAxis = chart.axisLeft
    leftAxis.isInverted = true
    leftAxis.axisMinimum = 0f // this replaces setStartAtZero(true)
    val rightAxis = chart.axisRight
    rightAxis.isEnabled = false

    // add data
    seekBarX.setProgress(25)
    seekBarY.setProgress(50)

    // // restrain the maximum scale-out factor
    // chart.setScaleMinima(3f, 3f);
    //
    // // center the view to a specific position inside the chart
    // chart.centerViewPort(10, 50);

    // get the legend (only possible after setting data)
    val l = chart.legend

    // modify the legend ...
    l.form = LegendForm.LINE

    // don't forget to refresh the drawing
    chart.invalidate()
  }

  private fun setData(count: Int, range: Float) {
    val entries = ArrayList<Entry>()
    for (i in 0 until count) {
      val xVal = (Math.random() * range).toFloat()
      val yVal = (Math.random() * range).toFloat()
      entries.add(Entry(xVal, yVal))
    }

    // sort by x-value
    Collections.sort(entries, EntryXComparator())

    // create a dataset and give it a type
    val set1 = LineDataSet(entries, "DataSet 1")
    set1.lineWidth = 1.5f
    set1.circleRadius = 4f

    // create a data object with the data sets
    val data = LineData(set1)

    // set data
    chart.data = data
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.line, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.viewGithub -> {
        val i = Intent(Intent.ACTION_VIEW)
        i.data =
            Uri.parse(
                "https://github.com/PhilJay/MPAndroidChart/blob/master/MPChartExample/src/com/xxmassdeveloper/mpchartexample/InvertedLineChartActivity.java")
        startActivity(i)
      }
      R.id.actionToggleValues -> {
        chart.data?.dataSets?.forEach { set ->
          set.setDrawValues(!set.isDrawValuesEnabled)
        }
        chart.invalidate()
      }
      R.id.actionToggleHighlight -> {
        if (chart.data != null) {
          chart.data!!.isHighlightEnabled = !chart.data!!.isHighlightEnabled
          chart.invalidate()
        }
      }
      R.id.actionToggleFilled -> {
        chart.data?.dataSets?.forEach { set ->
          (set as LineDataSet).setDrawFilled(!set.isDrawFilledEnabled)
        }
        chart.invalidate()
      }
      R.id.actionToggleCircles -> {
        chart.data?.dataSets?.forEach { set ->
          (set as LineDataSet).setDrawCircles(!set.isDrawCirclesEnabled)
        }
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
      R.id.actionTogglePinch -> {
        chart.setPinchZoom(!chart.isPinchZoomEnabled)
        chart.invalidate()
      }
      R.id.actionToggleAutoScaleMinMax -> {
        chart.isAutoScaleMinMaxEnabled = !chart.isAutoScaleMinMaxEnabled
        chart.notifyDataSetChanged()
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

    // redraw
    chart.invalidate()
  }

  override fun saveToGallery() {
    saveToGallery(chart!!, "InvertedLineChartActivity")
  }

  override fun onValueSelected(e: Entry?, h: Highlight?) {
    Log.i(
        "VAL SELECTED", "Value: " + e?.y + ", xIndex: " + e?.x + ", DataSet index: " + h?.dataSetIndex)
  }

  override fun onNothingSelected() {}
  override fun onStartTrackingTouch(seekBar: SeekBar) {}
  override fun onStopTrackingTouch(seekBar: SeekBar) {}
}
