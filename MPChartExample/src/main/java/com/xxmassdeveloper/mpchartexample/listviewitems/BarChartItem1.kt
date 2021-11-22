package com.xxmassdeveloper.mpchartexample.listviewitems

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import com.github.eklipse2k8.charting.charts.BarChart
import com.github.eklipse2k8.charting.components.XAxis.XAxisPosition
import com.github.eklipse2k8.charting.data.BarData
import com.github.eklipse2k8.charting.data.ChartData
import com.xxmassdeveloper.mpchartexample.R

class BarChartItem(cd: ChartData<*, *>, c: Context) : ChartItem(cd) {
  private val mTf: Typeface

  override val itemType: Int
    get() = TYPE_BARCHART

  @SuppressLint("InflateParams")
  override fun getView(position: Int, convertView: View?, c: Context?): View {

    var localView = convertView
    val holder: ViewHolder
    if (localView == null) {
      holder = ViewHolder()
      localView = LayoutInflater.from(c).inflate(R.layout.list_item_barchart, null)
      holder.chart = localView.findViewById(R.id.chart)
      localView.tag = holder
    } else {
      holder = localView.tag as ViewHolder
    }

    // apply styling
    holder.chart!!.description.isEnabled = false
    holder.chart!!.setDrawGridBackground(false)
    holder.chart!!.setDrawBarShadow(false)
    val xAxis = holder.chart!!.xAxis
    xAxis.position = XAxisPosition.BOTTOM
    xAxis.typeface = mTf
    xAxis.setDrawGridLines(false)
    xAxis.setDrawAxisLine(true)
    val leftAxis = holder.chart!!.axisLeft
    leftAxis.typeface = mTf
    leftAxis.setLabelCount(5, false)
    leftAxis.spaceTop = 20f
    leftAxis.axisMinimum = 0f // this replaces setStartAtZero(true)
    val rightAxis = holder.chart!!.axisRight
    rightAxis.typeface = mTf
    rightAxis.setLabelCount(5, false)
    rightAxis.spaceTop = 20f
    rightAxis.axisMinimum = 0f // this replaces setStartAtZero(true)
    mChartData.setValueTypeface(mTf)

    // set data
    holder.chart!!.data = mChartData as BarData
    holder.chart!!.setFitBars(true)

    // do not forget to refresh the chart
    //        holder.chart.invalidate();
    holder.chart!!.animateY(700)
    return localView ?: error("invalid view")
  }

  private class ViewHolder {
    var chart: BarChart? = null
  }

  init {
    mTf = Typeface.createFromAsset(c.assets, "OpenSans-Regular.ttf")
  }
}
