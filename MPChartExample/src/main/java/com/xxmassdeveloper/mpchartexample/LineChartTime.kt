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
import com.github.eklipse2k8.charting.charts.LineChart
import com.github.eklipse2k8.charting.components.AxisBase
import com.github.eklipse2k8.charting.components.XAxis.XAxisPosition
import com.github.eklipse2k8.charting.components.YAxis.AxisDependency
import com.github.eklipse2k8.charting.components.YAxis.YAxisLabelPosition
import com.github.eklipse2k8.charting.data.Entry
import com.github.eklipse2k8.charting.data.LineData
import com.github.eklipse2k8.charting.data.LineDataSet
import com.github.eklipse2k8.charting.formatter.IAxisValueFormatter
import com.github.eklipse2k8.charting.utils.ColorTemplate.holoBlue
import com.xxmassdeveloper.mpchartexample.notimportant.DemoBase
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class LineChartTime : DemoBase(), OnSeekBarChangeListener {
  private lateinit var chart: LineChart
  private lateinit var seekBarX: SeekBar
  private lateinit var tvX: TextView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_linechart_time)
    title = "LineChartTime"
    tvX = findViewById(R.id.tvXMax)
    seekBarX = findViewById(R.id.seekBar1)
    seekBarX.setOnSeekBarChangeListener(this)
    chart = findViewById(R.id.chart1)

    // no description text
    chart.description.isEnabled = false

    // enable touch gestures
    chart.isTouchEnabled = true
    chart.dragDecelerationFrictionCoef = 0.9f

    // enable scaling and dragging
    chart.isDragEnabled = true
    chart.setScaleEnabled(true)
    chart.setDrawGridBackground(false)
    chart.isHighlightPerDragEnabled = true

    // set an alternative background color
    chart.setViewPortOffsets(0f, 0f, 0f, 0f)

    // add data
    seekBarX.setProgress(100)

    // get the legend (only possible after setting data)
    val l = chart.legend
    l.isEnabled = false
    val xAxis = chart.xAxis
    xAxis.position = XAxisPosition.TOP_INSIDE
    xAxis.typeface = tfLight
    xAxis.textSize = 10f
    xAxis.textColor = Color.WHITE
    xAxis.setDrawAxisLine(false)
    xAxis.setDrawGridLines(true)
    xAxis.textColor = Color.rgb(255, 192, 56)
    xAxis.setCenterAxisLabels(true)
    xAxis.granularity = 1f // one hour
    xAxis.valueFormatter =
        object : IAxisValueFormatter {
          private val mFormat = SimpleDateFormat("dd MMM HH:mm", Locale.ENGLISH)
          override fun getFormattedValue(value: Float, axis: AxisBase): String? {
            val millis = TimeUnit.HOURS.toMillis(value.toLong())
            return mFormat.format(Date(millis))
          }
        }
    val leftAxis = chart.axisLeft
    leftAxis.setPosition(YAxisLabelPosition.INSIDE_CHART)
    leftAxis.typeface = tfLight
    leftAxis.textColor = holoBlue
    leftAxis.setDrawGridLines(true)
    leftAxis.isGranularityEnabled = true
    leftAxis.axisMinimum = 0f
    leftAxis.axisMaximum = 170f
    leftAxis.yOffset = -9f
    leftAxis.textColor = Color.rgb(255, 192, 56)
    val rightAxis = chart.axisRight
    rightAxis.isEnabled = false
  }

  private fun setData(count: Int) {

    // now in hours
    val now = TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis())
    val values = ArrayList<Entry>()

    // count = hours
    val to = (now + count).toFloat()

    // increment by 1 hour
    var x = now.toFloat()
    while (x < to) {
      val y = getRandom(50.toFloat(), 50f)
      values.add(Entry(x, y)) // add one entry per hour
      x++
    }

    // create a dataset and give it a type
    val set1 = LineDataSet(values, "DataSet 1")
    set1.axisDependency = AxisDependency.LEFT
    set1.color = holoBlue
    set1.valueTextColor = holoBlue
    set1.lineWidth = 1.5f
    set1.setDrawCircles(false)
    set1.setDrawValues(false)
    set1.fillAlpha = 65
    set1.fillColor = holoBlue
    set1.highLightColor = Color.rgb(244, 117, 117)
    set1.setDrawCircleHole(false)

    // create a data object with the data sets
    val data = LineData(set1)
    data.setValueTextColor(Color.WHITE)
    data.setValueTextSize(9f)

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
                "https://github.com/PhilJay/MPAndroidChart/blob/master/MPChartExample/src/com/xxmassdeveloper/mpchartexample/LineChartTime.java")
        startActivity(i)
      }
      R.id.actionToggleValues -> {
        chart.data?.dataSets?.forEach { set -> set.setDrawValues(!set.isDrawValuesEnabled) }
        chart.invalidate()
      }
      R.id.actionToggleHighlight -> {
        if (chart.data != null) {
          chart.data!!.isHighlightEnabled = !chart.data!!.isHighlightEnabled
          chart.invalidate()
        }
      }
      R.id.actionToggleFilled -> {
        chart.data?.dataSets?.forEach { set -> set.setDrawFilled(!set.isDrawFilledEnabled) }
        chart.invalidate()
      }
      R.id.actionToggleCircles -> {
        chart.data?.dataSets?.forEach { set ->
          (set as LineDataSet).setDrawCircles(!set.isDrawCirclesEnabled)
        }
        chart.invalidate()
      }
      R.id.actionToggleCubic -> {
        chart.data?.dataSets?.forEach { set ->
          (set as LineDataSet).mode =
              when (set.mode) {
                LineDataSet.Mode.CUBIC_BEZIER -> LineDataSet.Mode.LINEAR
                else -> LineDataSet.Mode.CUBIC_BEZIER
              }
        }
        chart.invalidate()
      }
      R.id.actionToggleStepped -> {
        chart.data?.dataSets?.forEach { set ->
          (set as LineDataSet).mode =
              when (set.mode) {
                LineDataSet.Mode.STEPPED -> LineDataSet.Mode.LINEAR
                else -> LineDataSet.Mode.STEPPED
              }
        }
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
    tvX.text = seekBarX.progress.toString()
    setData(seekBarX.progress)

    // redraw
    chart.invalidate()
  }

  override fun saveToGallery() {
    saveToGallery(chart, "LineChartTime")
  }

  override fun onStartTrackingTouch(seekBar: SeekBar) {}
  override fun onStopTrackingTouch(seekBar: SeekBar) {}
}
