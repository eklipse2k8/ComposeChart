package com.xxmassdeveloper.mpchartexample

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.github.eklipse2k8.charting.charts.LineChart
import com.github.eklipse2k8.charting.components.Legend
import com.github.eklipse2k8.charting.data.Entry
import com.github.eklipse2k8.charting.data.LineData
import com.github.eklipse2k8.charting.data.LineDataSet
import com.github.eklipse2k8.charting.highlight.Highlight
import com.github.eklipse2k8.charting.interfaces.datasets.ILineDataSet
import com.github.eklipse2k8.charting.listener.ChartTouchListener.ChartGesture
import com.github.eklipse2k8.charting.listener.OnChartGestureListener
import com.github.eklipse2k8.charting.listener.OnChartValueSelectedListener
import com.github.eklipse2k8.charting.utils.ColorTemplate
import com.xxmassdeveloper.mpchartexample.notimportant.DemoBase

class MultiLineChartActivity :
    DemoBase(), OnSeekBarChangeListener, OnChartGestureListener, OnChartValueSelectedListener {
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
    title = "MultiLineChartActivity"
    tvX = findViewById(R.id.tvXMax)
    tvY = findViewById(R.id.tvYMax)
    seekBarX = findViewById(R.id.seekBar1)
    seekBarX.setOnSeekBarChangeListener(this)
    seekBarY = findViewById(R.id.seekBar2)
    seekBarY.setOnSeekBarChangeListener(this)
    chart = findViewById(R.id.chart1)
    chart.setOnChartValueSelectedListener(this)
    chart.setDrawGridBackground(false)
    chart.description.isEnabled = false
    chart.setDrawBorders(false)
    chart.axisLeft.isEnabled = false
    chart.axisRight.setDrawAxisLine(false)
    chart.axisRight.setDrawGridLines(false)
    chart.xAxis.setDrawAxisLine(false)
    chart.xAxis.setDrawGridLines(false)

    // enable touch gestures
    chart.isTouchEnabled = true

    // enable scaling and dragging
    chart.isDragEnabled = true
    chart.setScaleEnabled(true)

    // if disabled, scaling can be done on x- and y-axis separately
    chart.setPinchZoom(false)
    seekBarX.setProgress(20)
    seekBarY.setProgress(100)
    val l = chart.legend
    l.verticalAlignment = Legend.LegendVerticalAlignment.TOP
    l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
    l.orientation = Legend.LegendOrientation.VERTICAL
    l.setDrawInside(false)
  }

  private val colors =
      intArrayOf(
          ColorTemplate.VORDIPLOM_COLORS[0],
          ColorTemplate.VORDIPLOM_COLORS[1],
          ColorTemplate.VORDIPLOM_COLORS[2])

  override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
    var progress = progress
    chart.resetTracking()
    progress = seekBarX!!.progress
    tvX!!.text = seekBarX!!.progress.toString()
    tvY!!.text = seekBarY!!.progress.toString()
    val dataSets = ArrayList<ILineDataSet>()
    for (z in 0..2) {
      val values = ArrayList<Entry>()
      for (i in 0 until progress) {
        val `val` = Math.random() * seekBarY!!.progress + 3
        values.add(Entry(i.toFloat(), `val`.toFloat()))
      }
      val d = LineDataSet(values, "DataSet " + (z + 1))
      d.lineWidth = 2.5f
      d.circleRadius = 4f
      val color = colors[z % colors.size]
      d.color = color
      d.setCircleColor(color)
      dataSets.add(d)
    }

    // make the first DataSet dashed
    (dataSets[0] as LineDataSet).enableDashedLine(10f, 10f, 0f)
    (dataSets[0] as LineDataSet).setColors(*ColorTemplate.VORDIPLOM_COLORS)
    (dataSets[0] as LineDataSet).setCircleColors(*ColorTemplate.VORDIPLOM_COLORS)
    val data = LineData(dataSets)
    chart.data = data
    chart.invalidate()
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.line, menu)
    menu.removeItem(R.id.actionToggleIcons)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.viewGithub -> {
        val i = Intent(Intent.ACTION_VIEW)
        i.data =
            Uri.parse(
                "https://github.com/PhilJay/MPAndroidChart/blob/master/MPChartExample/src/com/xxmassdeveloper/mpchartexample/MultiLineChartActivity.java")
        startActivity(i)
      }
      R.id.actionToggleValues -> {
        chart.data?.dataSets?.forEach { set -> set.setDrawValues(!set.isDrawValuesEnabled) }
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
    saveToGallery(chart!!, "MultiLineChartActivity")
  }

  override fun onChartGestureStart(me: MotionEvent?, lastPerformedGesture: ChartGesture?) {
    Log.i("Gesture", "START, x: " + me?.x + ", y: " + me?.y)
  }

  override fun onChartGestureEnd(me: MotionEvent?, lastPerformedGesture: ChartGesture?) {
    Log.i("Gesture", "END, lastGesture: $lastPerformedGesture")

    // un-highlight values after the gesture is finished and no single-tap
    if (lastPerformedGesture !== ChartGesture.SINGLE_TAP)
        chart.highlightValues(
            null) // or highlightTouch(null) for callback to onNothingSelected(...)
  }

  override fun onChartLongPressed(me: MotionEvent?) {
    Log.i("LongPress", "Chart long pressed.")
  }

  override fun onChartDoubleTapped(me: MotionEvent?) {
    Log.i("DoubleTap", "Chart double-tapped.")
  }

  override fun onChartSingleTapped(me: MotionEvent?) {
    Log.i("SingleTap", "Chart single-tapped.")
  }

  override fun onChartFling(
      me1: MotionEvent?,
      me2: MotionEvent?,
      velocityX: Float,
      velocityY: Float
  ) {
    Log.i("Fling", "Chart fling. VelocityX: $velocityX, VelocityY: $velocityY")
  }

  override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) {
    Log.i("Scale / Zoom", "ScaleX: $scaleX, ScaleY: $scaleY")
  }

  override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {
    Log.i("Translate / Move", "dX: $dX, dY: $dY")
  }

  override fun onValueSelected(e: Entry?, h: Highlight?) {
    if (e == null || h == null) return
    Log.i(
        "VAL SELECTED", "Value: " + e.y + ", xIndex: " + e.x + ", DataSet index: " + h.dataSetIndex)
  }

  override fun onNothingSelected() {}
  override fun onStartTrackingTouch(seekBar: SeekBar) {}
  override fun onStopTrackingTouch(seekBar: SeekBar) {}
}
