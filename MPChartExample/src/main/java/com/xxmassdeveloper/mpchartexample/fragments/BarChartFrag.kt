package com.xxmassdeveloper.mpchartexample.fragments

import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.github.eklipse2k8.charting.charts.BarChart
import com.github.eklipse2k8.charting.listener.ChartTouchListener.ChartGesture
import com.github.eklipse2k8.charting.listener.OnChartGestureListener
import com.xxmassdeveloper.mpchartexample.R
import com.xxmassdeveloper.mpchartexample.custom.MyMarkerView

class BarChartFrag : SimpleFragment(R.layout.frag_simple_bar), OnChartGestureListener {
  private lateinit var chart: BarChart

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    // create a new chart object
    chart =
        BarChart(requireActivity()).apply {
          description!!.isEnabled = false
          setOnChartGestureListener(this@BarChartFrag)
          setDrawGridBackground(false)
          setDrawBarShadow(false)
          data = generateBarData(1, 20000f, 12)
          legend.typeface = tf
          axisLeft?.typeface = tf
          axisLeft?.axisMinimum = 0f // this replaces setStartAtZero(true)
          axisRight?.isEnabled = false
          xAxis.isEnabled = false
        }

    val mv =
        MyMarkerView(requireActivity(), R.layout.custom_marker_view).apply {
          setChartView(chart) // For bounds control
        }

    chart.setMarker(mv)

    view.findViewById<FrameLayout>(R.id.parentLayout).apply {
      // programmatically add the chart
      addView(chart)
    }
  }

  override fun onChartGestureStart(me: MotionEvent?, lastPerformedGesture: ChartGesture?) {
    Log.i("Gesture", "START")
  }

  override fun onChartGestureEnd(me: MotionEvent?, lastPerformedGesture: ChartGesture?) {
    Log.i("Gesture", "END")
    chart.highlightValues(null)
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

  companion object {
    @JvmStatic
    fun newInstance(): Fragment {
      return BarChartFrag()
    }
  }
}
