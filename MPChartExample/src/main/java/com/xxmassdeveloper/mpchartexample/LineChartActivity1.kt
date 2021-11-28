package com.xxmassdeveloper.mpchartexample

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.DashPathEffect
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.github.eklipse2k8.charting.animation.Easing.EaseInOutCubic
import com.github.eklipse2k8.charting.charts.LineChart
import com.github.eklipse2k8.charting.components.Legend.LegendForm
import com.github.eklipse2k8.charting.components.LimitLine
import com.github.eklipse2k8.charting.components.LimitLine.LimitLabelPosition
import com.github.eklipse2k8.charting.components.XAxis
import com.github.eklipse2k8.charting.components.YAxis
import com.github.eklipse2k8.charting.data.Entry
import com.github.eklipse2k8.charting.data.LineData
import com.github.eklipse2k8.charting.data.LineDataSet
import com.github.eklipse2k8.charting.formatter.IFillFormatter
import com.github.eklipse2k8.charting.highlight.Highlight
import com.github.eklipse2k8.charting.interfaces.dataprovider.LineDataProvider
import com.github.eklipse2k8.charting.interfaces.datasets.ILineDataSet
import com.github.eklipse2k8.charting.listener.OnChartValueSelectedListener
import com.xxmassdeveloper.mpchartexample.custom.MyMarkerView
import com.xxmassdeveloper.mpchartexample.notimportant.DemoBase

/**
 * Example of a heavily customized [LineChart] with limit lines, custom line shapes, etc.
 *
 * @version 3.1.0
 * @since 1.7.4
 */
class LineChartActivity1 : DemoBase(), OnSeekBarChangeListener, OnChartValueSelectedListener {
  private lateinit var chart: LineChart
  private lateinit var seekBarX: SeekBar
  private lateinit var seekBarY: SeekBar
  private lateinit var tvX: TextView
  private lateinit var tvY: TextView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_linechart)
    title = "LineChartActivity1"
    tvX = findViewById(R.id.tvXMax)
    tvY = findViewById(R.id.tvYMax)
    seekBarX = findViewById(R.id.seekBar1)
    seekBarX.setOnSeekBarChangeListener(this)
    seekBarY = findViewById(R.id.seekBar2)
    seekBarY.max = 180
    seekBarY.setOnSeekBarChangeListener(this)

    // // Chart Style // //
    chart = findViewById(R.id.chart1)

    // disable description text
    chart.description.isEnabled = false

    // enable touch gestures
    chart.isTouchEnabled = true

    // set listeners
    chart.setOnChartValueSelectedListener(this)
    chart.setDrawGridBackground(false)

    // create marker to display box when values are selected
    val mv = MyMarkerView(this, R.layout.custom_marker_view)

    // Set the marker to the chart
    mv.setChartView(chart)
    chart.marker = mv

    // enable scaling and dragging
    chart.isDragEnabled = true
    chart.setScaleEnabled(true)

    // force pinch zoom along both axis
    chart.setPinchZoom(true)

    // // X-Axis Style // //
    val xAxis: XAxis = chart.xAxis

    // vertical grid lines
    xAxis.enableGridDashedLine(10f, 10f, 0f)

    // // Y-Axis Style // //
    val yAxis: YAxis = chart.axisLeft

    // disable dual axis (only use LEFT axis)
    chart.axisRight.isEnabled = false

    // horizontal grid lines
    yAxis.enableGridDashedLine(10f, 10f, 0f)

    // axis range
    yAxis.axisMaximum = 200f
    yAxis.axisMinimum = -50f

    // // Create Limit Lines // //
    val llXAxis =
        LimitLine(9f, "Index 10").apply {
          lineWidth = 4f
          enableDashedLine(10f, 10f, 0f)
          labelPosition = LimitLabelPosition.RIGHT_BOTTOM
          textSize = 10f
        }
    val ll1 =
        LimitLine(150f, "Upper Limit").apply {
          lineWidth = 4f
          enableDashedLine(10f, 10f, 0f)
          labelPosition = LimitLabelPosition.RIGHT_TOP
          textSize = 10f
        }
    val ll2 =
        LimitLine(-30f, "Lower Limit").apply {
          lineWidth = 4f
          enableDashedLine(10f, 10f, 0f)
          labelPosition = LimitLabelPosition.RIGHT_BOTTOM
          textSize = 10f
        }

    // draw limit lines behind data instead of on top
    yAxis.setDrawLimitLinesBehindData(true)
    xAxis.setDrawLimitLinesBehindData(true)

    // add limit lines
    yAxis.addLimitLine(ll1)
    yAxis.addLimitLine(ll2)

    // add data
    seekBarX.progress = 45
    seekBarY.progress = 180
    setData(45, 180f)

    // draw points over time
    chart.animateX(1500)

    // get the legend (only possible after setting data)
    val l = chart.legend

    // draw legend entries as lines
    l.form = LegendForm.LINE
  }

  private fun setData(count: Int, range: Float) {
    val values = ArrayList<Entry>()
    for (i in 0 until count) {
      val `val` = (Math.random() * range).toFloat() - 30
      values.add(
          Entry(i.toFloat(), `val`, ResourcesCompat.getDrawable(resources, R.drawable.star, theme)))
    }
    val set1: LineDataSet?
    if (chart.data != null && chart.data!!.dataSetCount > 0) {
      set1 = chart.data?.getDataSetByIndex(0) as LineDataSet?
      if (set1 != null) {
        set1.entries = values
        set1.notifyDataSetChanged()
      }
      chart.data!!.notifyDataChanged()
      chart.notifyDataSetChanged()
    } else {
      // create a dataset and give it a type
      set1 = LineDataSet(values, "DataSet 1")
      set1.setDrawIcons(false)

      // draw dashed line
      set1.enableDashedLine(10f, 5f, 0f)

      // black lines and points
      set1.color = Color.BLACK
      set1.setCircleColor(Color.BLACK)

      // line thickness and point size
      set1.lineWidth = 1f
      set1.circleRadius = 3f

      // draw points as solid circles
      set1.setDrawCircleHole(false)

      // customize legend entry
      set1.formLineWidth = 1f
      set1.formLineDashEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)
      set1.formSize = 15f

      // text size of values
      set1.valueTextSize = 9f

      // draw selection line as dashed
      set1.enableDashedHighlightLine(10f, 5f, 0f)

      // set the filled area
      set1.setDrawFilled(true)
      set1.fillFormatter =
          object : IFillFormatter {
            override fun getFillLinePosition(
                dataSet: ILineDataSet,
                dataProvider: LineDataProvider
            ): Float {
              return chart.axisLeft.axisMinimum
            }
          }

      // set color of filled area

      // drawables only supported on api level 18 and above
      val drawable = ContextCompat.getDrawable(this, R.drawable.fade_red)
      set1.fillDrawable = drawable

      val dataSets = ArrayList<ILineDataSet>()
      dataSets.add(set1) // add the data sets

      // create a data object with the data sets
      val data = LineData(dataSets)

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
                "https://github.com/PhilJay/MPAndroidChart/blob/master/MPChartExample/src/com/xxmassdeveloper/mpchartexample/LineChartActivity1.java")
        startActivity(i)
      }
      R.id.actionToggleValues -> {
        chart.data?.dataSets?.forEach { set -> set.setDrawValues(!set.isDrawValuesEnabled) }
        chart.invalidate()
      }
      R.id.actionToggleIcons -> {
        chart.data?.dataSets?.forEach { set -> set.setDrawIcons(!set.isDrawIconsEnabled) }
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
        chart.animateY(2000, EaseInOutCubic)
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
    tvY.text = seekBarY.progress.toString()
    setData(seekBarX.progress, seekBarY.progress.toFloat())

    // redraw
    chart.invalidate()
  }

  override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
  override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit

  override fun saveToGallery() {
    saveToGallery(chart, "LineChartActivity1")
  }

  override fun onValueSelected(e: Entry?, h: Highlight?) {
    Log.i("Entry selected", e.toString())
    Log.i("LOW HIGH", "low: ${chart.lowestVisibleX}, high: ${chart.highestVisibleX}")
    Log.i(
        "MIN MAX",
        "xMin: ${chart.xChartMin}, xMax: ${chart.xChartMax}, yMin: ${chart.yChartMin}, yMax: ${chart.yChartMax}")
  }

  override fun onNothingSelected() {
    Log.i("Nothing selected", "Nothing selected.")
  }
}
