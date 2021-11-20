package com.xxmassdeveloper.mpchartexample.fragments

import android.graphics.Color
import android.graphics.Typeface
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.github.eklipse2k8.charting.charts.allDefaultShapes
import com.github.eklipse2k8.charting.data.*
import com.github.eklipse2k8.charting.interfaces.datasets.IBarDataSet
import com.github.eklipse2k8.charting.interfaces.datasets.ILineDataSet
import com.github.eklipse2k8.charting.interfaces.datasets.IScatterDataSet
import com.github.eklipse2k8.charting.utils.ColorTemplate
import com.github.eklipse2k8.charting.utils.FileUtils.loadEntriesFromAssets
import kotlin.LazyThreadSafetyMode.NONE

abstract class SimpleFragment(@LayoutRes layoutRes: Int) : Fragment(layoutRes) {
  protected val tf: Typeface by
      lazy(NONE) { Typeface.createFromAsset(requireContext().assets, "OpenSans-Regular.ttf") }

  protected fun generateBarData(dataSets: Int, range: Float, count: Int): BarData {
    val sets = mutableListOf<IBarDataSet>()
    for (i in 0 until dataSets) {
      val entries = mutableListOf<BarEntry>()
      for (j in 0 until count) {
        entries.add(BarEntry(j.toFloat(), (Math.random() * range).toFloat() + range / 4))
      }
      val ds = BarDataSet(entries, getLabel(i)).apply { setColors(*ColorTemplate.VORDIPLOM_COLORS) }
      sets.add(ds)
    }
    return BarData(sets).apply { setValueTypeface(tf) }
  }

  protected fun generateScatterData(dataSets: Int, range: Float, count: Int): ScatterData {
    val sets = mutableListOf<IScatterDataSet>()
    val shapes = allDefaultShapes
    for (i in 0 until dataSets) {
      val entries = mutableListOf<Entry>()
      for (j in 0 until count) {
        entries.add(Entry(j.toFloat(), (Math.random() * range).toFloat() + range / 4))
      }
      val ds =
          ScatterDataSet(entries, getLabel(i)).apply {
            scatterShapeSize = 12f
            setScatterShape(shapes[i % shapes.size])
            setColors(*ColorTemplate.COLORFUL_COLORS)
            scatterShapeSize = 9f
          }
      sets.add(ds)
    }
    return ScatterData(sets).apply { setValueTypeface(tf) }
  }

  /**
   * generates less data (1 DataSet, 4 values)
   * @return PieData
   */
  protected fun generatePieData(): PieData {
    val count = 4
    val entries1 = mutableListOf<PieEntry>()
    for (i in 0 until count) {
      entries1.add(PieEntry((Math.random() * 60 + 40).toFloat(), "Quarter " + (i + 1)))
    }
    val ds1 =
        PieDataSet(entries1, "Quarterly Revenues 2015").apply {
          setColors(*ColorTemplate.VORDIPLOM_COLORS)
          sliceSpace = 2f
          valueTextColor = Color.WHITE
          valueTextSize = 12f
        }
    return PieData(ds1).apply { setValueTypeface(tf) }
  }

  protected fun generateLineData(): LineData {
    val sets = ArrayList<ILineDataSet>()
    val ds1 =
        LineDataSet(
            loadEntriesFromAssets(requireContext().assets, "sine.txt").toMutableList(),
            "Sine function")
    val ds2 =
        LineDataSet(
            loadEntriesFromAssets(requireContext().assets, "cosine.txt").toMutableList(),
            "Cosine function")
    ds1.lineWidth = 2f
    ds2.lineWidth = 2f
    ds1.setDrawCircles(false)
    ds2.setDrawCircles(false)
    ds1.color = ColorTemplate.VORDIPLOM_COLORS[0]
    ds2.color = ColorTemplate.VORDIPLOM_COLORS[1]

    // load DataSets from files in assets folder
    sets.add(ds1)
    sets.add(ds2)
    val d = LineData(sets)
    d.setValueTypeface(tf)
    return d
  }

  // load DataSets from files in assets folder
  protected val complexity: LineData
    protected get() {
      val sets = ArrayList<ILineDataSet>()
      val ds1 =
          LineDataSet(
                  loadEntriesFromAssets(requireContext().assets, "n.txt").toMutableList(), "O(n)")
              .apply {
                color = ColorTemplate.VORDIPLOM_COLORS[0]
                setCircleColor(ColorTemplate.VORDIPLOM_COLORS[0])
                lineWidth = 2.5f
                circleRadius = 3f
              }

      val ds2 =
          LineDataSet(
                  loadEntriesFromAssets(requireContext().assets, "nlogn.txt").toMutableList(),
                  "O(nlogn)")
              .apply {
                setCircleColor(ColorTemplate.VORDIPLOM_COLORS[1])
                color = ColorTemplate.VORDIPLOM_COLORS[1]
                lineWidth = 2.5f
                circleRadius = 3f
              }
      val ds3 =
          LineDataSet(
                  loadEntriesFromAssets(requireContext().assets, "square.txt").toMutableList(),
                  "O(n\u00B2)")
              .apply {
                setCircleColor(ColorTemplate.VORDIPLOM_COLORS[2])
                color = ColorTemplate.VORDIPLOM_COLORS[2]
                lineWidth = 2.5f
                circleRadius = 3f
              }
      val ds4 =
          LineDataSet(
                  loadEntriesFromAssets(requireContext().assets, "three.txt").toMutableList(),
                  "O(n\u00B3)")
              .apply {
                setCircleColor(ColorTemplate.VORDIPLOM_COLORS[3])
                color = ColorTemplate.VORDIPLOM_COLORS[3]
                lineWidth = 2.5f
                circleRadius = 3f
              }

      // load DataSets from files in assets folder
      sets.add(ds1)
      sets.add(ds2)
      sets.add(ds3)
      sets.add(ds4)
      val d = LineData(sets)
      d.setValueTypeface(tf)
      return d
    }
  private val mLabels =
      arrayOf("Company A", "Company B", "Company C", "Company D", "Company E", "Company F")

  private fun getLabel(i: Int): String {
    return mLabels[i]
  }
}
