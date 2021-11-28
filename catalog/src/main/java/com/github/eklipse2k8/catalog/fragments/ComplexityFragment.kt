package com.github.eklipse2k8.catalog.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.github.eklipse2k8.charting.charts.LineChart
import com.github.eklipse2k8.catalog.R

class ComplexityFragment : SimpleFragment(R.layout.frag_simple_line) {
  private lateinit var chart: LineChart

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    chart =
        view.findViewById<LineChart?>(R.id.lineChart1).apply {
          description!!.isEnabled = false
          setDrawGridBackground(false)
          data = complexity
          animateX(3000)

          legend.typeface = tf
          axisLeft?.typeface = tf
          axisRight?.isEnabled = false
          xAxis.isEnabled = false
        }
  }

  companion object {
    @JvmStatic
    fun newInstance(): Fragment {
      return ComplexityFragment()
    }
  }
}
