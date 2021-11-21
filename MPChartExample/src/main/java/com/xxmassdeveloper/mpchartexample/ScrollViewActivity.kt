package com.xxmassdeveloper.mpchartexample

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.github.eklipse2k8.charting.charts.BarChart
import com.github.eklipse2k8.charting.components.XAxis.XAxisPosition
import com.github.eklipse2k8.charting.data.BarData
import com.github.eklipse2k8.charting.data.BarDataSet
import com.github.eklipse2k8.charting.data.BarEntry
import com.github.eklipse2k8.charting.utils.ColorTemplate
import com.xxmassdeveloper.mpchartexample.notimportant.DemoBase

class ScrollViewActivity : DemoBase() {
  private lateinit var chart: BarChart
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_scrollview)
    title = "ScrollViewActivity"
    chart = findViewById(R.id.chart1)
    chart.description.isEnabled = false

    // scaling can now only be done on x- and y-axis separately
    chart.setPinchZoom(false)
    chart.setDrawBarShadow(false)
    chart.setDrawGridBackground(false)
    val xAxis = chart.xAxis
    xAxis.position = XAxisPosition.BOTTOM
    xAxis.setDrawGridLines(false)
    chart.axisLeft.setDrawGridLines(false)
    chart.legend.isEnabled = false
    setData(10)
    chart.setFitBars(true)
  }

  private fun setData(count: Int) {
    val values = mutableListOf<BarEntry>()
    for (i in 0 until count) {
      val value = (Math.random() * count).toFloat() + 15
      values.add(BarEntry(i.toFloat(), value))
    }
    val set = BarDataSet(values, "Data Set")
    set.setColors(*ColorTemplate.VORDIPLOM_COLORS)
    set.setDrawValues(false)
    val data = BarData(set)
    chart.data = data
    chart.invalidate()
    chart.animateY(800)
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.only_github, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.viewGithub -> {
        val i = Intent(Intent.ACTION_VIEW)
        i.data =
            Uri.parse(
                "https://github.com/PhilJay/MPAndroidChart/blob/master/MPChartExample/src/com/xxmassdeveloper/mpchartexample/ScrollViewActivity.java")
        startActivity(i)
      }
    }
    return true
  }

  public override fun saveToGallery() {
    /* Intentionally left empty */
  }
}
