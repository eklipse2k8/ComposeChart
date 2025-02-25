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
import androidx.core.content.res.ResourcesCompat
import com.github.eklipse2k8.charting.charts.BarChart
import com.github.eklipse2k8.charting.components.Legend
import com.github.eklipse2k8.charting.components.XAxis.XAxisPosition
import com.github.eklipse2k8.charting.data.BarData
import com.github.eklipse2k8.charting.data.BarDataSet
import com.github.eklipse2k8.charting.data.BarEntry
import com.github.eklipse2k8.charting.data.Entry
import com.github.eklipse2k8.charting.highlight.Highlight
import com.github.eklipse2k8.charting.interfaces.datasets.IBarDataSet
import com.github.eklipse2k8.charting.listener.OnChartValueSelectedListener
import com.github.eklipse2k8.charting.utils.ColorTemplate
import com.github.eklipse2k8.catalog.custom.MyAxisValueFormatter
import com.github.eklipse2k8.catalog.custom.MyValueFormatter
import com.github.eklipse2k8.catalog.notimportant.DemoBase

class StackedBarActivity : DemoBase(), OnSeekBarChangeListener, OnChartValueSelectedListener {
  private lateinit var chart: BarChart
  private lateinit var seekBarX: SeekBar
  private lateinit var seekBarY: SeekBar
  private lateinit var tvX: TextView
  private lateinit var tvY: TextView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_barchart)
    title = "StackedBarActivity"
    tvX = findViewById(R.id.tvXMax)
    tvY = findViewById(R.id.tvYMax)
    seekBarX = findViewById(R.id.seekBar1)
    seekBarX.setOnSeekBarChangeListener(this)
    seekBarY = findViewById(R.id.seekBar2)
    seekBarY.setOnSeekBarChangeListener(this)
    chart = findViewById(R.id.chart1)
    chart.setOnChartValueSelectedListener(this)
    chart.description.isEnabled = false

    // if more than 60 entries are displayed in the chart, no values will be
    // drawn
    chart.setMaxVisibleValueCount(40)

    // scaling can now only be done on x- and y-axis separately
    chart.setPinchZoom(false)
    chart.setDrawGridBackground(false)
    chart.setDrawBarShadow(false)
    chart.setDrawValueAboveBar(false)
    chart.isHighlightFullBarEnabled = false

    // change the position of the y-labels
    val leftAxis = chart.axisLeft
    leftAxis.valueFormatter = MyAxisValueFormatter()
    leftAxis.axisMinimum = 0f // this replaces setStartAtZero(true)
    chart.axisRight.isEnabled = false
    val xLabels = chart.xAxis
    xLabels.position = XAxisPosition.TOP

    // chart.setDrawXLabels(false);
    // chart.setDrawYLabels(false);

    // setting data
    seekBarX.setProgress(12)
    seekBarY.setProgress(100)
    val l = chart.legend
    l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
    l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
    l.orientation = Legend.LegendOrientation.HORIZONTAL
    l.setDrawInside(false)
    l.formSize = 8f
    l.formToTextSpace = 4f
    l.xEntrySpace = 6f

    // chart.setDrawLegend(false);
  }

  override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
    tvX!!.text = seekBarX!!.progress.toString()
    tvY!!.text = seekBarY!!.progress.toString()
    val values = ArrayList<BarEntry>()
    for (i in 0 until seekBarX!!.progress) {
      val mul = (seekBarY!!.progress + 1).toFloat()
      val val1 = (Math.random() * mul).toFloat() + mul / 3
      val val2 = (Math.random() * mul).toFloat() + mul / 3
      val val3 = (Math.random() * mul).toFloat() + mul / 3
      values.add(
          BarEntry(
              i.toFloat(),
              0f,
              floatArrayOf(val1, val2, val3),
              ResourcesCompat.getDrawable(resources, R.drawable.star, theme)))
    }
    val set1: BarDataSet?
    if (chart.data != null && chart.data!!.dataSetCount > 0) {
      set1 = chart.data!!.getDataSetByIndex(0) as BarDataSet?
      if (set1 != null) {
        set1.entries = values
      }
      chart.data!!.notifyDataChanged()
      chart.notifyDataSetChanged()
    } else {
      set1 = BarDataSet(values, "Statistics Vienna 2014")
      set1.setDrawIcons(false)
      set1.setColors(*colors)
      set1.stackLabels = arrayOf("Births", "Divorces", "Marriages")
      val dataSets = ArrayList<IBarDataSet>()
      dataSets.add(set1)
      val data = BarData(dataSets)
      data.setValueFormatter(MyValueFormatter())
      data.setValueTextColor(Color.WHITE)
      chart.data = data
    }
    chart.setFitBars(true)
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
                "https://github.com/PhilJay/MPAndroidChart/blob/master/MPChartExample/src/com/xxmassdeveloper/mpchartexample/StackedBarActivity.java")
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

  override fun saveToGallery() {
    saveToGallery(chart!!, "StackedBarActivity")
  }

  override fun onStartTrackingTouch(seekBar: SeekBar) {}
  override fun onStopTrackingTouch(seekBar: SeekBar) {}
  override fun onValueSelected(e: Entry?, h: Highlight?) {
    if (e == null || h == null) return

    val entry = e as BarEntry
    if (entry!!.yVals != null) Log.i("VAL SELECTED", "Value: " + entry.yVals!![h.stackIndex])
    else Log.i("VAL SELECTED", "Value: " + entry.y)
  }

  override fun onNothingSelected() {}

  // have as many colors as stack-values per entry
  private val colors: IntArray
    private get() {

      // have as many colors as stack-values per entry
      val colors = IntArray(3)
      System.arraycopy(ColorTemplate.MATERIAL_COLORS, 0, colors, 0, 3)
      return colors
    }
}
