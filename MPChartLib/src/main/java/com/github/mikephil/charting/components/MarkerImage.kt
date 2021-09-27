package com.github.mikephil.charting.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.FSize
import com.github.mikephil.charting.utils.MPPointF
import java.lang.ref.WeakReference

/**
 * View that can be displayed when selecting values in the chart. Extend this class to provide
 * custom layouts for your markers.
 *
 * @author Philipp Jahoda
 */
class MarkerImage(mContext: Context, drawableResourceId: Int) : IMarker {
  var mDrawable: Drawable = mContext.resources.getDrawable(drawableResourceId, null)
  private var mOffset: MPPointF = MPPointF()
  private val mOffset2 = MPPointF()
  private var mWeakChart: WeakReference<Chart<*>>? = null
  private val mDrawableBoundsCache = Rect()

  fun setOffset(offsetX: Float, offsetY: Float) {
    mOffset.x = offsetX
    mOffset.y = offsetY
  }

  override var offset: MPPointF
    get() = mOffset
    set(offset) {
      mOffset = offset
    }

  var size: FSize = FSize(Float.NaN, Float.NaN)

  fun setChartView(chart: Chart<*>) {
    mWeakChart = WeakReference(chart)
  }

  private val chartView: Chart<*>?
    get() = if (mWeakChart == null) null else mWeakChart?.get()

  override fun getOffsetForDrawingAtPoint(posX: Float, posY: Float): MPPointF {
    val offset = offset
    mOffset2.x = offset.x
    mOffset2.y = offset.y
    val chart = chartView
    var width = size.width
    var height = size.height
    if (width.isNaN()) {
      width = mDrawable.intrinsicWidth.toFloat()
    }
    if (height.isNaN()) {
      height = mDrawable.intrinsicWidth.toFloat()
    }
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

  override fun refreshContent(e: Entry?, highlight: Highlight?) {}

  override fun draw(canvas: Canvas?, posX: Float, posY: Float) {
    val offset = getOffsetForDrawingAtPoint(posX, posY)
    var width = size.width
    var height = size.height
    if (width.isNaN()) {
      width = mDrawable.intrinsicWidth.toFloat()
    }
    if (height.isNaN()) {
      height = mDrawable.intrinsicHeight.toFloat()
    }
    mDrawable.copyBounds(mDrawableBoundsCache)
    mDrawable.setBounds(
        mDrawableBoundsCache.left,
        mDrawableBoundsCache.top,
        mDrawableBoundsCache.left + width.toInt(),
        mDrawableBoundsCache.top + height.toInt())
    val saveId = canvas!!.save()
    // translate to the correct position and draw
    canvas.translate(posX + offset.x, posY + offset.y)
    mDrawable.draw(canvas)
    canvas.restoreToCount(saveId)
    mDrawable.bounds = mDrawableBoundsCache
  }
}
