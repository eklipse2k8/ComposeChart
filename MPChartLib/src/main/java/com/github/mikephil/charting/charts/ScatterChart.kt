package com.github.mikephil.charting.charts

import android.content.Context
import android.util.AttributeSet
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.ScatterData
import com.github.mikephil.charting.interfaces.dataprovider.ScatterDataProvider
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet
import com.github.mikephil.charting.renderer.CombinedChartRenderer
import com.github.mikephil.charting.renderer.DataRenderer
import com.github.mikephil.charting.renderer.ScatterChartRenderer

/**
 * The ScatterChart. Draws dots, triangles, squares and custom shapes into the Chart-View. CIRCLE
 * and SCQUARE offer the best performance, TRIANGLE has the worst performance.
 *
 * @author Philipp Jahoda
 */
class ScatterChart
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    BarLineChartBase<ScatterData, IScatterDataSet, Entry>(context, attrs, defStyleAttr),
    ScatterDataProvider {

  override val dataRenderer: DataRenderer = ScatterChartRenderer(this, mAnimator, mViewPortHandler)

  init {
    xAxis.spaceMin = 0.5f
    xAxis.spaceMax = 0.5f
  }

  override val scatterData: ScatterData?
    get() = data

  /**
   * Predefined ScatterShapes that allow the specification of a shape a ScatterDataSet should be
   * drawn with. If a ScatterShape is specified for a ScatterDataSet, the required renderer is set.
   */
  enum class ScatterShape(private val shapeIdentifier: String) {
    SQUARE("SQUARE"),
    CIRCLE("CIRCLE"),
    TRIANGLE("TRIANGLE"),
    CROSS("CROSS"),
    X("X"),
    CHEVRON_UP("CHEVRON_UP"),
    CHEVRON_DOWN("CHEVRON_DOWN");

    override fun toString(): String {
      return shapeIdentifier
    }

    companion object {
      val allDefaultShapes: Array<ScatterShape>
        get() = arrayOf(SQUARE, CIRCLE, TRIANGLE, CROSS, X, CHEVRON_UP, CHEVRON_DOWN)
    }
  }
}
