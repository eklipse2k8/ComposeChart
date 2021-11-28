package com.github.eklipse2k8.catalog.fragments

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.View
import androidx.fragment.app.Fragment
import com.github.eklipse2k8.charting.charts.PieChart
import com.github.eklipse2k8.charting.components.Legend
import com.github.eklipse2k8.catalog.R

class PieChartFrag : SimpleFragment(R.layout.frag_simple_pie) {
  private lateinit var chart: PieChart

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    chart =
        view.findViewById<PieChart>(R.id.pieChart1).apply {
          description!!.isEnabled = false
          setCenterTextTypeface(tf)
          centerText = generateCenterText()
          setCenterTextSize(10f)
          setCenterTextTypeface(tf)

          // radius of the center hole in percent of maximum radius
          holeRadius = 45f
          transparentCircleRadius = 50f

          legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
          legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
          legend.orientation = Legend.LegendOrientation.VERTICAL
          legend.setDrawInside(false)

          data = generatePieData()
        }
  }

  private fun generateCenterText(): SpannableString =
      SpannableString("Revenues\nQuarters 2015").apply {
        setSpan(RelativeSizeSpan(2f), 0, 8, 0)
        setSpan(ForegroundColorSpan(Color.GRAY), 8, length, 0)
      }

  companion object {
    @JvmStatic
    fun newInstance(): Fragment {
      return PieChartFrag()
    }
  }
}
