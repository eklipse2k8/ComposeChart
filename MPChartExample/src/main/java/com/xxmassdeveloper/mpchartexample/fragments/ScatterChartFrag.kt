package com.xxmassdeveloper.mpchartexample.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.github.eklipse2k8.charting.charts.ScatterChart
import com.xxmassdeveloper.mpchartexample.R
import com.xxmassdeveloper.mpchartexample.custom.MyMarkerView

class ScatterChartFrag : SimpleFragment(R.layout.frag_simple_scatter) {
  private lateinit var chart: ScatterChart

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    chart =
        view.findViewById<ScatterChart>(R.id.scatterChart1).apply {
          description!!.isEnabled = false
          setDrawGridBackground(false)
          data = generateScatterData(6, 10000f, 200)

          axisLeft?.typeface = tf
          axisRight?.typeface = tf
          axisRight?.setDrawGridLines(false)

          legend.isWordWrapEnabled = true
          legend.typeface = tf
          legend.formSize = 14f
          legend.textSize = 9f

          // increase the space between legend & bottom and legend & content
          legend.yOffset = 13f
          extraBottomOffset = 16f
        }

    val mv =
        MyMarkerView(requireActivity(), R.layout.custom_marker_view).apply {
          setChartView(chart) // For bounds control
        }

    chart.setMarker(mv)
  }

  companion object {
    @JvmStatic
    fun newInstance(): Fragment {
      return ScatterChartFrag()
    }
  }
}
