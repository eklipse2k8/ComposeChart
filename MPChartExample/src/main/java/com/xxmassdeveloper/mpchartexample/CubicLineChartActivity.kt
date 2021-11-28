package com.xxmassdeveloper.mpchartexample

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.github.eklipse2k8.charting.charts.LineChart
import com.github.eklipse2k8.charting.components.YAxis.YAxisLabelPosition
import com.github.eklipse2k8.charting.data.Entry
import com.github.eklipse2k8.charting.data.LineData
import com.github.eklipse2k8.charting.data.LineDataSet
import com.github.eklipse2k8.charting.formatter.IFillFormatter
import com.github.eklipse2k8.charting.interfaces.dataprovider.LineDataProvider
import com.github.eklipse2k8.charting.interfaces.datasets.ILineDataSet
import com.xxmassdeveloper.mpchartexample.notimportant.DemoBase

class CubicLineChartActivity : DemoBase(), OnSeekBarChangeListener {
  private lateinit var chart: LineChart
  private lateinit var seekBarX: SeekBar
  private lateinit var seekBarY: SeekBar
  private lateinit var tvX: TextView
  private lateinit var tvY: TextView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_linechart)
    title = "CubicLineChartActivity"
    tvX = findViewById(R.id.tvXMax)
    tvY = findViewById(R.id.tvYMax)
    seekBarX = findViewById(R.id.seekBar1)
    seekBarY = findViewById(R.id.seekBar2)
    chart = findViewById(R.id.chart1)
    chart.setViewPortOffsets(0f, 0f, 0f, 0f)
    chart.setBackgroundColor(Color.rgb(104, 241, 175))

    // no description text
    chart.description.isEnabled = false

    // enable touch gestures
    chart.isTouchEnabled = true

    // enable scaling and dragging
    chart.isDragEnabled = true
    chart.setScaleEnabled(true)

    // if disabled, scaling can be done on x- and y-axis separately
    chart.setPinchZoom(false)
    chart.setDrawGridBackground(false)
    chart.maxHighlightDistance = 300f
    val x = chart.xAxis
    x.isEnabled = false
    val y = chart.axisLeft
    y.setLabelCount(6, false)
    y.textColor = Color.WHITE
    y.setPosition(YAxisLabelPosition.INSIDE_CHART)
    y.setDrawGridLines(false)
    y.axisLineColor = Color.WHITE
    chart.axisRight.isEnabled = false

    // add data
    seekBarY.setOnSeekBarChangeListener(this)
    seekBarX.setOnSeekBarChangeListener(this)

    // lower max, as cubic runs significantly slower than linear
    seekBarX.setMax(700)
    seekBarX.setProgress(45)
    seekBarY.setProgress(100)
    chart.legend.isEnabled = false
    chart.animateXY(2000, 2000)

    // don't forget to refresh the drawing
    chart.invalidate()
  }

  private fun setData(count: Int, range: Float) {
    val values = ArrayList<Entry>()
    for (i in 0 until count) {
      val `val` = (Math.random() * (range + 1)).toFloat() + 20
      values.add(Entry(i.toFloat(), `val`))
    }
    val set1: LineDataSet?
    if (chart.data != null && chart.data!!.dataSetCount > 0) {
      set1 = chart.data!!.getDataSetByIndex(0) as LineDataSet?
      set1!!.entries = values
      chart.data!!.notifyDataChanged()
      chart.notifyDataSetChanged()
    } else {
      // create a dataset and give it a type
      set1 = LineDataSet(values, "DataSet 1")
      set1.mode = LineDataSet.Mode.CUBIC_BEZIER
      set1.cubicIntensity = 0.2f
      set1.setDrawFilled(true)
      set1.setDrawCircles(false)
      set1.lineWidth = 1.8f
      set1.circleRadius = 4f
      set1.setCircleColor(Color.WHITE)
      set1.highLightColor = Color.rgb(244, 117, 117)
      set1.color = Color.WHITE
      set1.fillColor = Color.WHITE
      set1.fillAlpha = 100
      set1.setDrawHorizontalHighlightIndicator(false)
      set1.fillFormatter =
          object : IFillFormatter {
            override fun getFillLinePosition(
                dataSet: ILineDataSet,
                dataProvider: LineDataProvider
            ): Float {
              return chart.axisLeft.axisMinimum
            }
          }

      // create a data object with the data sets
      val data = LineData(set1)
      data.setValueTextSize(9f)
      data.setDrawValues(false)

      // set data
      chart.data = data
    }
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
                "https://github.com/PhilJay/MPAndroidChart/blob/master/MPChartExample/src/com/xxmassdeveloper/mpchartexample/CubicLineChartActivity.java")
        startActivity(i)
      }
      R.id.actionToggleValues -> {
        for (set in chart.data!!.dataSets) set.setDrawValues(!set.isDrawValuesEnabled)
        chart.invalidate()
      }
      R.id.actionToggleHighlight -> {
        chart.data?.let { it.isHighlightEnabled = !it.isHighlightEnabled }
        chart.invalidate()
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
              if (set.mode === LineDataSet.Mode.CUBIC_BEZIER) LineDataSet.Mode.LINEAR
              else LineDataSet.Mode.CUBIC_BEZIER
        }
        chart.invalidate()
      }
      R.id.actionToggleStepped -> {
        chart.data?.dataSets?.forEach { set ->
          (set as LineDataSet).mode =
              if (set.mode === LineDataSet.Mode.STEPPED) LineDataSet.Mode.LINEAR
              else LineDataSet.Mode.STEPPED
        }
        chart.invalidate()
      }
      R.id.actionToggleHorizontalCubic -> {
        chart.data?.dataSets?.forEach { set ->
          (set as LineDataSet).mode =
              if (set.mode === LineDataSet.Mode.HORIZONTAL_BEZIER) LineDataSet.Mode.LINEAR
              else LineDataSet.Mode.HORIZONTAL_BEZIER
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
          requestStoragePermission(chart)
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
    saveToGallery(chart!!, "CubicLineChartActivity")
  }

  override fun onStartTrackingTouch(seekBar: SeekBar) {}
  override fun onStopTrackingTouch(seekBar: SeekBar) {}
}
