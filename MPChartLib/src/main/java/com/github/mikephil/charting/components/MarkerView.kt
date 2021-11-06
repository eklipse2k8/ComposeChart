package com.github.mikephil.charting.components

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View.MeasureSpec.UNSPECIFIED
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.RelativeLayout
import androidx.annotation.LayoutRes
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import java.lang.ref.WeakReference

/**
 * View that can be displayed when selecting values in the chart. Extend this class to provide
 * custom layouts for your markers.
 *
 * @author Philipp Jahoda
 */
open class MarkerView : RelativeLayout, IMarker {
  constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

  constructor(context: Context, @LayoutRes layoutResource: Int) : super(context) {
    setLayoutResource(layoutResource)
  }

  private var mOffset = MPPointF()
  private val mOffset2 = MPPointF()
  private var mWeakChart: WeakReference<Chart<*,*,*>>? = null

  /**
   * Sets the layout resource for a custom MarkerView.
   *
   * @param resId
   */
  fun setLayoutResource(@LayoutRes resId: Int) {
    val inflated = LayoutInflater.from(context).inflate(resId, this)
    inflated.layoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
    inflated.measure(
        MeasureSpec.makeMeasureSpec(0, UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, UNSPECIFIED))
    inflated.layout(0, 0, inflated.measuredWidth, inflated.measuredHeight)
  }

  fun setOffset(offsetX: Float, offsetY: Float) {
    mOffset.x = offsetX
    mOffset.y = offsetY
  }

  override var offset: MPPointF
    get() = mOffset
    set(offset) {
      mOffset = offset
    }

  fun setChartView(chart: Chart<*,*,*>) {
    mWeakChart = WeakReference(chart)
  }

  private val chartView: Chart<*,*,*>?
    get() = if (mWeakChart == null) null else mWeakChart?.get()

  override fun getOffsetForDrawingAtPoint(posX: Float, posY: Float): MPPointF {
    val offset = offset
    mOffset2.x = offset.x
    mOffset2.y = offset.y
    val chart = chartView
    val width = width.toFloat()
    val height = height.toFloat()
    if (posX + mOffset2.x < 0) {
      mOffset2.x = -posX
    } else if (chart != null && posX + width + mOffset2.x > chart.width) {
      mOffset2.x = chart.width - posX - width
    }
    if (posY + mOffset2.y < 0) {
      mOffset2.y = -posY
    } else if (chart != null && posY + height + mOffset2.y > chart.height) {
      mOffset2.y = chart.height - posY - height
    }
    return mOffset2
  }

  override fun refreshContent(e: Entry?, highlight: Highlight?) {
    measure(
        MeasureSpec.makeMeasureSpec(0, UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, UNSPECIFIED))
    layout(0, 0, measuredWidth, measuredHeight)
  }

  override fun draw(canvas: Canvas?, posX: Float, posY: Float) {
    val offset = getOffsetForDrawingAtPoint(posX, posY)
    val saveId = canvas!!.save()
    // translate to the correct position and draw
    canvas.translate(posX + offset.x, posY + offset.y)
    draw(canvas)
    canvas.restoreToCount(saveId)
  }
}
