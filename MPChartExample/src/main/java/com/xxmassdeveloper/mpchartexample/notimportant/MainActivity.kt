package com.xxmassdeveloper.mpchartexample.notimportant

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.github.eklipse2k8.charting.utils.Utils
import com.google.android.material.color.DynamicColors
import com.xxmassdeveloper.mpchartexample.*
import com.xxmassdeveloper.mpchartexample.fragments.SimpleChartDemo

class MainActivity : AppCompatActivity(), OnItemClickListener {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    DynamicColors.applyToActivitiesIfAvailable((applicationContext as Application))
    setContentView(R.layout.activity_main)
    title = "Legacy View Examples"

    // initialize the utilities
    Utils.init(this)
    val objects = ArrayList<ContentItem>()

    ////
    objects.add(0, ContentItem("Line Charts"))
    objects.add(1, ContentItem("Basic", "Simple line chart."))
    objects.add(2, ContentItem("Multiple", "Show multiple data sets."))
    objects.add(3, ContentItem("Dual Axis", "Line chart with dual y-axes."))
    objects.add(4, ContentItem("Inverted Axis", "Inverted y-axis."))
    objects.add(5, ContentItem("Cubic", "Line chart with a cubic line shape."))
    objects.add(6, ContentItem("Colorful", "Colorful line chart."))
    objects.add(7, ContentItem("Performance", "Render 30.000 data points smoothly."))
    objects.add(8, ContentItem("Filled", "Colored area between two lines."))

    ////
    objects.add(9, ContentItem("Bar Charts"))
    objects.add(10, ContentItem("Basic", "Simple bar chart."))
    objects.add(11, ContentItem("Basic 2", "Variation of the simple bar chart."))
    objects.add(12, ContentItem("Multiple", "Show multiple data sets."))
    objects.add(13, ContentItem("Horizontal", "Render bar chart horizontally."))
    objects.add(14, ContentItem("Stacked", "Stacked bar chart."))
    objects.add(15, ContentItem("Negative", "Positive and negative values with unique colors."))
    objects.add(
        16,
        ContentItem(
            "Negative Horizontal",
            "demonstrates how to create a HorizontalBarChart with positive and negative values."))
    objects.add(17, ContentItem("Stacked 2", "Stacked bar chart with negative values."))
    objects.add(18, ContentItem("Sine", "Sine function in bar chart format."))

    ////
    objects.add(19, ContentItem("Pie Charts"))
    objects.add(20, ContentItem("Basic", "Simple pie chart."))
    objects.add(21, ContentItem("Value Lines", "Stylish lines drawn outward from slices."))
    objects.add(22, ContentItem("Half Pie", "180° (half) pie chart."))

    ////
    objects.add(23, ContentItem("Other Charts"))
    objects.add(24, ContentItem("Combined Chart", "Bar and line chart together."))
    objects.add(25, ContentItem("Scatter Plot", "Simple scatter plot."))
    objects.add(26, ContentItem("Bubble Chart", "Simple bubble chart."))
    objects.add(27, ContentItem("Candlestick", "Simple financial chart."))
    objects.add(28, ContentItem("Radar Chart", "Simple web chart."))

    ////
    objects.add(29, ContentItem("Scrolling Charts"))
    objects.add(30, ContentItem("Multiple", "Various types of charts as fragments."))
    objects.add(31, ContentItem("View Pager", "Swipe through different charts."))
    objects.add(32, ContentItem("Tall Bar Chart", "Bars bigger than your screen!"))
    objects.add(33, ContentItem("Many Bar Charts", "More bars than your screen can handle!"))

    ////
    objects.add(34, ContentItem("Even More Line Charts"))
    objects.add(35, ContentItem("Dynamic", "Build a line chart by adding points and sets."))
    objects.add(36, ContentItem("Realtime", "Add data points in realtime."))
    objects.add(
        37, ContentItem("Hourly", "Uses the current time to add a data point for each hour."))

    val adapter = MyAdapter(this, objects)
    val lv = findViewById<ListView>(R.id.listView1)
    lv.adapter = adapter
    lv.onItemClickListener = this
  }

  override fun onItemClick(av: AdapterView<*>?, v: View, pos: Int, arg3: Long) {
    val i =
        when (pos) {
          1 -> Intent(this, LineChartActivity1::class.java)
          2 -> Intent(this, MultiLineChartActivity::class.java)
          3 -> Intent(this, LineChartActivity2::class.java)
          4 -> Intent(this, InvertedLineChartActivity::class.java)
          5 -> Intent(this, CubicLineChartActivity::class.java)
          6 -> Intent(this, LineChartActivityColored::class.java)
          7 -> Intent(this, PerformanceLineChart::class.java)
          8 -> Intent(this, FilledLineActivity::class.java)
          10 -> Intent(this, BarChartActivity::class.java)
          11 -> Intent(this, AnotherBarActivity::class.java)
          12 -> Intent(this, BarChartActivityMultiDataset::class.java)
          13 -> Intent(this, HorizontalBarChartActivity::class.java)
          14 -> Intent(this, StackedBarActivity::class.java)
          15 -> Intent(this, BarChartPositiveNegative::class.java)
          16 -> Intent(this, HorizontalBarNegativeChartActivity::class.java)
          17 -> Intent(this, StackedBarActivityNegative::class.java)
          18 -> Intent(this, BarChartActivitySinus::class.java)
          20 -> Intent(this, PieChartActivity::class.java)
          21 -> Intent(this, PiePolylineChartActivity::class.java)
          22 -> Intent(this, HalfPieChartActivity::class.java)
          24 -> Intent(this, CombinedChartActivity::class.java)
          25 -> Intent(this, ScatterChartActivity::class.java)
          26 -> Intent(this, BubbleChartActivity::class.java)
          27 -> Intent(this, CandleStickChartActivity::class.java)
          28 -> Intent(this, RadarChartActivity::class.java)
          30 -> Intent(this, ListViewMultiChartActivity::class.java)
          31 -> Intent(this, SimpleChartDemo::class.java)
          32 -> Intent(this, ScrollViewActivity::class.java)
          33 -> Intent(this, ListViewBarChartActivity::class.java)
          35 -> Intent(this, DynamicalAddingActivity::class.java)
          36 -> Intent(this, RealtimeLineChartActivity::class.java)
          37 -> Intent(this, LineChartTime::class.java)
          else -> null
        }
    i?.let { startActivity(it) }
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.main, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    val i: Intent
    when (item.itemId) {
      R.id.viewGithub -> {
        i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse("https://github.com/eklipse2k8/ComposeChart")
        startActivity(i)
      }
    }
    return true
  }
}
