package com.github.eklipse2k8.charting.charts

import android.annotation.TargetApi
import android.content.ContentValues
import android.content.Context
import android.graphics.*
import android.graphics.Bitmap.CompressFormat
import android.graphics.Paint.Align
import android.net.Uri
import android.os.Environment
import android.os.Environment.DIRECTORY_PICTURES
import android.provider.MediaStore
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import com.github.eklipse2k8.charting.animation.ChartAnimator
import com.github.eklipse2k8.charting.animation.Easing.EasingFunction
import com.github.eklipse2k8.charting.components.Description
import com.github.eklipse2k8.charting.components.IMarker
import com.github.eklipse2k8.charting.components.Legend
import com.github.eklipse2k8.charting.components.XAxis
import com.github.eklipse2k8.charting.data.ChartData
import com.github.eklipse2k8.charting.data.Entry
import com.github.eklipse2k8.charting.formatter.DefaultValueFormatter
import com.github.eklipse2k8.charting.formatter.IValueFormatter
import com.github.eklipse2k8.charting.highlight.Highlight
import com.github.eklipse2k8.charting.highlight.IHighlighter
import com.github.eklipse2k8.charting.interfaces.dataprovider.ChartInterface
import com.github.eklipse2k8.charting.interfaces.datasets.IDataSet
import com.github.eklipse2k8.charting.listener.ChartTouchListener
import com.github.eklipse2k8.charting.listener.OnChartGestureListener
import com.github.eklipse2k8.charting.listener.OnChartValueSelectedListener
import com.github.eklipse2k8.charting.renderer.DataRenderer
import com.github.eklipse2k8.charting.renderer.LegendRenderer
import com.github.eklipse2k8.charting.utils.MPPointF
import com.github.eklipse2k8.charting.utils.Utils
import com.github.eklipse2k8.charting.utils.Utils.convertDpToPixel
import com.github.eklipse2k8.charting.utils.Utils.getDecimals
import com.github.eklipse2k8.charting.utils.ViewPortHandler
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.suspendCoroutine
import kotlin.math.abs
import kotlin.math.max
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private val TAG = Chart::class.java.simpleName

/** paint for the grid background (only line and barchart) */
val PAINT_GRID_BACKGROUND = 4

/** paint for the info text that is displayed when there are no values in the chart */
val PAINT_INFO = 7

/** paint for the description text in the bottom right corner */
val PAINT_DESCRIPTION = 11

/** paint for the hole in the middle of the pie chart */
val PAINT_HOLE = 13

/** paint for the text in the middle of the pie chart */
val PAINT_CENTER_TEXT = 14

/** paint used for the legend */
val PAINT_LEGEND_LABEL = 18

abstract class Chart<T, S, E>
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ViewGroup(context, attrs, defStyleAttr), ChartInterface where
T : ChartData<S, E>,
S : IDataSet<E>,
E : Entry {

  protected abstract val highlighter: IHighlighter

  /** flag that indicates if logging is enabled or not */
  var isLogEnabled = false

  /**
   * object that holds all data that was originally set for the chart, before it was modified or any
   * filtering algorithms had been applied
   */
  override var data: T? = null
    set(value) {
      field = value
      mOffsetsCalculated = false
      if (value == null) {
        return
      }

      // calculate how many digits are needed
      setupDefaultFormatter(value.yMin, value.yMax)
      value.dataSets.forEach { set ->
        if (set.needsFormatter() || set.valueFormatter === mDefaultValueFormatter)
            set.valueFormatter = mDefaultValueFormatter
      }

      // let the chart know there is new data
      notifyDataSetChanged()
      if (isLogEnabled) Log.i(TAG, "Data is set.")
    }

  /** Flag that indicates if highlighting per tap (touch) is enabled */
  private var highLightPerTapEnabled = true

  /** If set to true, chart continues to scroll after touch up */
  private var dragDecelerationEnabled = true

  /**
   * Deceleration friction coefficient in [0 ; 1] interval, higher values indicate that speed will
   * decrease slowly, for example if it set to 0, it will stop immediately. 1 is an invalid value,
   * and will be converted to 0.999f automatically.
   */
  open var dragDecelerationFrictionCoef: Float = 0.9f
    set(value) {
      field = value.coerceIn(0f, 0.999f)
    }

  /** default value-formatter, number of digits depends on provided chart-data */
  private var mDefaultValueFormatter = DefaultValueFormatter(0)

  /** paint object used for drawing the description text in the bottom right corner of the chart */
  private var mDescPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

  /** paint object for drawing the information text when there are no values in the chart */
  private var mInfoPaint: Paint =
      Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(247, 189, 51) // orange
        textAlign = Align.CENTER
        textSize = convertDpToPixel(12f)
      }

  /**
   * Returns the object representing all x-labels, this method can be used to acquire the XAxis
   * object and modify it (e.g. change the position of the labels, styling, etc.)
   */
  open var xAxis: XAxis = XAxis()

  /** if true, touch gestures are enabled on the chart */
  var isTouchEnabled = true

  /**
   * Returns the Description object of the chart that is responsible for holding all information
   * related to the description text that is displayed in the bottom right corner of the chart (by
   * default).
   */
  var description: Description? = Description()
    protected set

  /**
   * Returns the Legend object of the chart. This method can be used to get an instance of the
   * legend in order to customize the automatically generated Legend.
   */
  var legend: Legend = Legend()
    protected set

  /** listener that is called when a value on the chart is selected */
  protected var mSelectionListener: OnChartValueSelectedListener? = null

  /** Set an extra offset to be appended to the viewport's top */
  var extraTopOffset: Float = 0f
    set(value) {
      field = convertDpToPixel(value)
    }

  /** Set an extra offset to be appended to the viewport's right */
  var extraRightOffset: Float = 0f
    set(value) {
      field = convertDpToPixel(value)
    }

  /** Set an extra offset to be appended to the viewport's bottom */
  var extraBottomOffset: Float = 0f
    set(value) {
      field = convertDpToPixel(value)
    }

  /** Set an extra offset to be appended to the viewport's left */
  var extraLeftOffset: Float = 0f
    set(value) {
      field = convertDpToPixel(value)
    }

  /**
   * Set a new (e.g. custom) ChartTouchListener NOTE: make sure to setTouchEnabled(true); if you
   * need touch gestures on the chart
   */
  protected var onTouchListener: ChartTouchListener<*>? = null

  /** text that is displayed when the chart is empty */
  private var mNoDataText = "No chart data available."

  /** Gesture listener for custom callbacks when making gestures on the chart. */
  private var mGestureListener: OnChartGestureListener? = null

  /**
   * Returns the ViewPortHandler of the chart that is responsible for the content area of the chart
   * and its offsets and dimensions.
   */
  val viewPortHandler = ViewPortHandler()

  /** Returns the renderer object responsible for rendering / drawing the Legend. */
  val legendRenderer: LegendRenderer = LegendRenderer(viewPortHandler, legend)

  /** object responsible for rendering the data */
  protected abstract val dataRenderer: DataRenderer

  /** object responsible for animations */
  protected var mAnimator: ChartAnimator = ChartAnimator { postInvalidate() }

  /** array of Highlight objects that reference the highlighted slices in the chart */
  protected var mIndicesToHighlight: Array<Highlight?>? = null

  /** The maximum distance in dp away from an entry causing it to highlight. */
  protected var mMaxHighlightDistance = convertDpToPixel(500f)

  /** if set to true, the marker view is drawn when a value is clicked */
  protected var mDrawMarkers = true

  /** the view that represents the marker */
  protected var mMarker: IMarker? = null

  /** tasks to be done after the view is setup */
  protected var mJobs = ArrayList<Runnable>()

  init {
    setWillNotDraw(false)
    Utils.init(context)

    if (isLogEnabled) Log.i(TAG, "Chart.init()")
  }

  /**
   * Clears the chart from all data (sets it to null) and refreshes it (by calling invalidate()).
   */
  open fun clear() {
    data = null
    mOffsetsCalculated = false
    mIndicesToHighlight = null
    onTouchListener?.lastHighlighted = null
    invalidate()
  }

  /**
   * Removes all DataSets (and thereby Entries) from the chart. Does not set the data object to
   * null. Also refreshes the chart by calling invalidate().
   */
  open fun clearValues() {
    data?.clearValues()
    invalidate()
  }

  /**
   * Returns true if the chart is empty (meaning it's data object is either null or contains no
   * entries).
   *
   * @return
   */
  open fun isEmpty(): Boolean =
      if (data == null) {
        true
      } else {
        data!!.entryCount <= 0
      }

  /**
   * Lets the chart know its underlying data has changed and performs all necessary recalculations.
   * It is crucial that this method is called everytime data is changed dynamically. Not calling
   * this method can lead to crashes or unexpected behaviour.
   */
  abstract fun notifyDataSetChanged()

  /**
   * Calculates the offsets of the chart to the border depending on the position of an eventual
   * legend or depending on the length of the y-axis and x-axis labels and their position
   */
  protected abstract fun calculateOffsets()

  /** Calculates the y-min and y-max value and the y-delta and x-delta value */
  protected abstract fun calcMinMax()

  /**
   * Calculates the required number of digits for the values that might be drawn in the chart (if
   * enabled), and creates the default-value-formatter
   */
  protected open fun setupDefaultFormatter(min: Float, max: Float) {
    val reference =
        if (data == null || data!!.entryCount < 2) {
          max(abs(min), abs(max))
        } else {
          abs(max - min)
        }
    val digits = getDecimals(reference)

    // setup the formatter with a new number of digits
    mDefaultValueFormatter.setup(digits)
  }

  /** flag that indicates if offsets calculation has already been done or not */
  private var mOffsetsCalculated = false

  override fun onDraw(canvas: Canvas) {
    if (data == null) {
      if (mNoDataText.isNotEmpty()) {
        val pt = getCenter()
        when (mInfoPaint.textAlign) {
          Align.LEFT -> {
            pt.x = 0f
            canvas.drawText(mNoDataText, pt.x, pt.y, mInfoPaint)
          }
          Align.RIGHT -> {
            pt.x *= 2.0f
            canvas.drawText(mNoDataText, pt.x, pt.y, mInfoPaint)
          }
          else -> canvas.drawText(mNoDataText, pt.x, pt.y, mInfoPaint)
        }
      }
      return
    }
    if (!mOffsetsCalculated) {
      calculateOffsets()
      mOffsetsCalculated = true
    }
  }

  /** Draws the description text in the bottom right corner of the chart (per default) */
  protected open fun drawDescription(c: Canvas) {
    // check if description should be drawn
    val description = description ?: return
    if (description.isEnabled) {
      mDescPaint.typeface = description.typeface
      mDescPaint.textSize = description.textSize
      mDescPaint.color = description.textColor
      mDescPaint.textAlign = description.textAlign

      // if no position specified, draw on default position
      val position = description.position
      val x = position?.x ?: width - viewPortHandler.offsetRight() - description.xOffset
      val y = position?.y ?: height - viewPortHandler.offsetBottom() - description.yOffset

      c.drawText(description.text, x, y, mDescPaint)
    }
  }

  /**
   * Sets the maximum distance in screen dp a touch can be away from an entry to cause it to get
   * highlighted. Default: 500dp
   */
  override var maxHighlightDistance: Float
    get() = mMaxHighlightDistance
    set(value) {
      mMaxHighlightDistance = convertDpToPixel(value)
    }

  /**
   * Returns the array of currently highlighted values. This might a null or empty array if nothing
   * is highlighted.
   *
   * @return
   */
  open fun getHighlighted(): Array<Highlight?>? {
    return mIndicesToHighlight
  }

  /**
   * Returns true if values can be highlighted via tap gesture, false if not.
   *
   * @return
   */
  open fun isHighlightPerTapEnabled(): Boolean {
    return highLightPerTapEnabled
  }

  /**
   * Set this to false to prevent values from being highlighted by tap gesture. Values can still be
   * highlighted via drag or programmatically. Default: true
   *
   * @param enabled
   */
  open fun setHighlightPerTapEnabled(enabled: Boolean) {
    highLightPerTapEnabled = enabled
  }

  /**
   * Returns true if there are values to highlight, false if there are no values to highlight.
   * Checks if the highlight array is null, has a length of zero or if the first object is null.
   *
   * @return
   */
  open fun valuesToHighlight(): Boolean {
    return !(mIndicesToHighlight == null ||
        mIndicesToHighlight!!.isEmpty() ||
        mIndicesToHighlight!![0] == null)
  }

  /**
   * Sets the last highlighted value for the touchlistener.
   *
   * @param highs
   */
  protected open fun setLastHighlighted(highs: Array<Highlight?>?) {
    if (highs == null || highs.isEmpty() || highs[0] == null) {
      onTouchListener?.lastHighlighted = null
    } else {
      onTouchListener?.lastHighlighted = highs[0]
    }
  }

  /**
   * Highlights the values at the given indices in the given DataSets. Provide null or an empty
   * array to undo all highlighting. This should be used to programmatically highlight values. This
   * method *will not* call the listener.
   *
   * @param highs
   */
  open fun highlightValues(highs: Array<Highlight?>?) {
    // set the indices to highlight
    mIndicesToHighlight = highs
    setLastHighlighted(highs)

    // redraw the chart
    invalidate()
  }

  /**
   * Highlights any y-value at the given x-value in the given DataSet. Provide -1 as the
   * dataSetIndex to undo all highlighting.
   * @param x The x-value to highlight
   * @param y The y-value to highlight. Supply `NaN` for "any"
   * @param dataSetIndex The dataset index to search in
   * @param dataIndex The data index to search in (only used in CombinedChartView currently)
   * @param callListener Should the listener be called for this change
   */
  @JvmOverloads
  open fun highlightValue(
      x: Float = Float.NaN,
      y: Float = Float.NaN,
      dataSetIndex: Int = -1,
      dataIndex: Int = -1,
      callListener: Boolean = true
  ) {
    if (dataSetIndex < 0 || dataSetIndex >= data!!.dataSetCount) {
      highlightValue(null, callListener)
    } else {
      highlightValue(Highlight(x, y, dataSetIndex, dataIndex), callListener)
    }
  }

  /**
   * Highlights the value selected by touch gesture. Unlike highlightValues(...), this generates a
   * callback to the OnChartValueSelectedListener.
   *
   * @param high
   * - the highlight object
   * @param callListener
   * - call the listener
   */
  open fun highlightValue(high: Highlight?, callListener: Boolean) {
    var highlight = high
    var e: Entry? = null
    if (highlight == null) {
      mIndicesToHighlight = null
    } else {
      if (isLogEnabled) Log.i(TAG, "Highlighted: $highlight")
      e = data!!.getEntryForHighlight(highlight)
      if (e == null) {
        mIndicesToHighlight = null
        highlight = null
      } else {
        // set the indices to highlight
        mIndicesToHighlight = arrayOf(highlight)
      }
    }
    setLastHighlighted(mIndicesToHighlight)
    if (callListener && mSelectionListener != null) {
      if (!valuesToHighlight()) {
        mSelectionListener?.onNothingSelected()
      } else {
        // notify the listener
        mSelectionListener?.onValueSelected(e, highlight)
      }
    }

    // redraw the chart
    invalidate()
  }

  /**
   * Returns the Highlight object (contains x-index and DataSet index) of the selected value at the
   * given touch point inside the Line-, Scatter-, or CandleStick-Chart.
   *
   * @param x
   * @param y
   * @return
   */
  open fun getHighlightByTouchPoint(x: Float, y: Float): Highlight? =
      data?.let { highlighter.getHighlight(x, y) }

  /** draws all MarkerViews on the highlighted positions */
  protected open fun drawMarkers(canvas: Canvas) {
    // if there is no marker view or drawing marker is disabled
    if (mMarker == null || !isDrawMarkersEnabled() || !valuesToHighlight()) return
    mIndicesToHighlight?.forEachIndexed { i, highlight ->
      if (highlight == null) return@forEachIndexed

      val set = data?.getDataSetByIndex(highlight.dataSetIndex) ?: return@forEachIndexed
      val e = data?.getEntryForHighlight(highlight)
      val entryIndex = e?.let { set.getEntryIndex(it) } ?: -1

      // make sure entry not null
      if (e == null || entryIndex > set.entryCount * mAnimator.phaseX) return@forEachIndexed
      val pos = getMarkerPosition(highlight)

      // check bounds
      if (!viewPortHandler.isInBounds(pos[0], pos[1])) return@forEachIndexed

      // callbacks to update the content
      mMarker!!.refreshContent(e, highlight)

      // draw the marker
      mMarker!!.draw(canvas, pos[0], pos[1])
    }
  }

  /**
   * Returns the actual position in pixels of the MarkerView for the given Highlight object.
   *
   * @param high
   * @return
   */
  protected open fun getMarkerPosition(high: Highlight): FloatArray {
    return floatArrayOf(high.drawX, high.drawY)
  }

  /** Returns the animator responsible for animating chart values. */
  open fun getAnimator(): ChartAnimator {
    return mAnimator
  }

  /** If set to true, chart continues to scroll after touch up default: true */
  open fun isDragDecelerationEnabled(): Boolean {
    return dragDecelerationEnabled
  }

  /**
   * If set to true, chart continues to scroll after touch up. Default: true.
   *
   * @param enabled
   */
  open fun setDragDecelerationEnabled(enabled: Boolean) {
    dragDecelerationEnabled = enabled
  }

  /**
   * Animates the drawing / rendering of the chart on both x- and y-axis with the specified
   * animation time. If animate(...) is called, no further calling of invalidate() is necessary to
   * refresh the chart. ANIMATIONS ONLY WORK FOR API LEVEL 11 (Android 3.0.x) AND HIGHER.
   *
   * @param durationMillisX
   * @param durationMillisY
   * @param easingX a custom easing function to be used on the animation phase
   * @param easingY a custom easing function to be used on the animation phase
   */
  @RequiresApi(11)
  open fun animateXY(
      durationMillisX: Int,
      durationMillisY: Int,
      easingX: EasingFunction?,
      easingY: EasingFunction?
  ) {
    mAnimator.animateXY(durationMillisX, durationMillisY, easingX!!, easingY!!)
  }

  /**
   * Animates the drawing / rendering of the chart on both x- and y-axis with the specified
   * animation time. If animate(...) is called, no further calling of invalidate() is necessary to
   * refresh the chart. ANIMATIONS ONLY WORK FOR API LEVEL 11 (Android 3.0.x) AND HIGHER.
   *
   * @param durationMillisX
   * @param durationMillisY
   * @param easing a custom easing function to be used on the animation phase
   */
  @RequiresApi(11)
  open fun animateXY(durationMillisX: Int, durationMillisY: Int, easing: EasingFunction?) {
    mAnimator.animateXY(durationMillisX, durationMillisY, easing!!)
  }

  /**
   * Animates the rendering of the chart on the x-axis with the specified animation time. If
   * animate(...) is called, no further calling of invalidate() is necessary to refresh the chart.
   * ANIMATIONS ONLY WORK FOR API LEVEL 11 (Android 3.0.x) AND HIGHER.
   *
   * @param durationMillis
   * @param easing a custom easing function to be used on the animation phase
   */
  @RequiresApi(11)
  open fun animateX(durationMillis: Int, easing: EasingFunction?) {
    mAnimator.animateX(durationMillis, easing!!)
  }

  /**
   * Animates the rendering of the chart on the y-axis with the specified animation time. If
   * animate(...) is called, no further calling of invalidate() is necessary to refresh the chart.
   * ANIMATIONS ONLY WORK FOR API LEVEL 11 (Android 3.0.x) AND HIGHER.
   *
   * @param durationMillis
   * @param easing a custom easing function to be used on the animation phase
   */
  @RequiresApi(11)
  open fun animateY(durationMillis: Int, easing: EasingFunction?) {
    mAnimator.animateY(durationMillis, easing!!)
  }

  /** CODE BELOW FOR ANIMATIONS WITHOUT EASING */
  /**
   * Animates the rendering of the chart on the x-axis with the specified animation time. If
   * animate(...) is called, no further calling of invalidate() is necessary to refresh the chart.
   * ANIMATIONS ONLY WORK FOR API LEVEL 11 (Android 3.0.x) AND HIGHER.
   *
   * @param durationMillis
   */
  @RequiresApi(11)
  open fun animateX(durationMillis: Int) {
    mAnimator.animateX(durationMillis)
  }

  /**
   * Animates the rendering of the chart on the y-axis with the specified animation time. If
   * animate(...) is called, no further calling of invalidate() is necessary to refresh the chart.
   * ANIMATIONS ONLY WORK FOR API LEVEL 11 (Android 3.0.x) AND HIGHER.
   *
   * @param durationMillis
   */
  @RequiresApi(11)
  open fun animateY(durationMillis: Int) {
    mAnimator.animateY(durationMillis)
  }

  /**
   * Animates the drawing / rendering of the chart on both x- and y-axis with the specified
   * animation time. If animate(...) is called, no further calling of invalidate() is necessary to
   * refresh the chart. ANIMATIONS ONLY WORK FOR API LEVEL 11 (Android 3.0.x) AND HIGHER.
   *
   * @param durationMillisX
   * @param durationMillisY
   */
  @RequiresApi(11)
  open fun animateXY(durationMillisX: Int, durationMillisY: Int) {
    mAnimator.animateXY(durationMillisX, durationMillisY)
  }

  override val defaultValueFormatter: IValueFormatter?
    get() = mDefaultValueFormatter

  /**
   * set a selection listener for the chart
   *
   * @param l
   */
  open fun setOnChartValueSelectedListener(l: OnChartValueSelectedListener?) {
    mSelectionListener = l
  }

  /**
   * Sets a gesture-listener for the chart for custom callbacks when executing gestures on the chart
   * surface.
   *
   * @param l
   */
  open fun setOnChartGestureListener(l: OnChartGestureListener?) {
    mGestureListener = l
  }

  /**
   * Returns the custom gesture listener.
   *
   * @return
   */
  open fun getOnChartGestureListener(): OnChartGestureListener? {
    return mGestureListener
  }

  /**
   * returns the current y-max value across all DataSets
   *
   * @return
   */
  open fun getYMax(): Float {
    return data!!.yMax
  }

  /**
   * returns the current y-min value across all DataSets
   *
   * @return
   */
  open fun getYMin(): Float {
    return data!!.yMin
  }

  override val xChartMax: Float
    get() = xAxis.mAxisMaximum

  override val xChartMin: Float
    get() = xAxis.mAxisMinimum

  override val xRange: Float
    get() = xAxis.mAxisRange

  /**
   * Returns a recyclable MPPointF instance. Returns the center point of the chart (the whole View)
   * in pixels.
   *
   * @return
   */
  open fun getCenter(): MPPointF {
    return MPPointF.getInstance(width / 2f, height / 2f)
  }

  override val centerOffsets: MPPointF
    get() = viewPortHandler.contentCenter

  /**
   * Sets extra offsets (around the chart view) to be appended to the auto-calculated offsets.
   *
   * @param left
   * @param top
   * @param right
   * @param bottom
   */
  open fun setExtraOffsets(left: Float, top: Float, right: Float, bottom: Float) {
    extraLeftOffset = left
    extraTopOffset = top
    extraRightOffset = right
    extraBottomOffset = bottom
  }

  /**
   * Sets the text that informs the user that there is no data available with which to draw the
   * chart.
   *
   * @param text
   */
  open fun setNoDataText(text: String) {
    mNoDataText = text
  }

  /**
   * Sets the color of the no data text.
   *
   * @param color
   */
  open fun setNoDataTextColor(color: Int) {
    mInfoPaint.color = color
  }

  /**
   * Sets the typeface to be used for the no data text.
   *
   * @param tf
   */
  open fun setNoDataTextTypeface(tf: Typeface?) {
    mInfoPaint.typeface = tf
  }

  /**
   * alignment of the no data text
   *
   * @param align
   */
  open fun setNoDataTextAlignment(align: Align?) {
    mInfoPaint.textAlign = align
  }

  /**
   * sets the marker that is displayed when a value is clicked on the chart
   *
   * @param marker
   */
  open fun setMarker(marker: IMarker) {
    mMarker = marker
  }

  /**
   * returns the marker that is set as a marker view for the chart
   *
   * @return
   */
  open fun getMarker(): IMarker? {
    return mMarker
  }

  @Deprecated("")
  open fun setMarkerView(v: IMarker) {
    setMarker(v)
  }

  @Deprecated("")
  open fun getMarkerView(): IMarker? {
    return getMarker()
  }

  override val contentRect: RectF?
    get() = viewPortHandler.contentRect

  /** disables intercept touchevents */
  open fun disableScroll() {
    val parent = parent
    parent?.requestDisallowInterceptTouchEvent(true)
  }

  /** enables intercept touchevents */
  open fun enableScroll() {
    val parent = parent
    parent?.requestDisallowInterceptTouchEvent(false)
  }

  /**
   * set a new paint object for the specified parameter in the chart e.g. Chart.PAINT_VALUES
   *
   * @param p the new paint object
   * @param which Chart.PAINT_VALUES, Chart.PAINT_GRID, Chart.PAINT_VALUES, ...
   */
  open fun setPaint(p: Paint, which: Int) {
    when (which) {
      PAINT_INFO -> mInfoPaint = p
      PAINT_DESCRIPTION -> mDescPaint = p
    }
  }

  /**
   * Returns the paint object associated with the provided constant.
   *
   * @param which e.g. Chart.PAINT_LEGEND_LABEL
   * @return
   */
  open fun getPaint(which: Int): Paint? {
    when (which) {
      PAINT_INFO -> return mInfoPaint
      PAINT_DESCRIPTION -> return mDescPaint
    }
    return null
  }

  @Deprecated("")
  open fun isDrawMarkerViewsEnabled(): Boolean {
    return isDrawMarkersEnabled()
  }

  @Deprecated("")
  open fun setDrawMarkerViews(enabled: Boolean) {
    setDrawMarkers(enabled)
  }

  /**
   * returns true if drawing the marker is enabled when tapping on values (use the setMarker(IMarker
   * marker) method to specify a marker)
   *
   * @return
   */
  open fun isDrawMarkersEnabled(): Boolean {
    return mDrawMarkers
  }

  /**
   * Set this to true to draw a user specified marker when tapping on chart values (use the
   * setMarker(IMarker marker) method to specify a marker). Default: true
   *
   * @param enabled
   */
  open fun setDrawMarkers(enabled: Boolean) {
    mDrawMarkers = enabled
  }

  override val centerOfView: MPPointF?
    get() = getCenter()

  /**
   * Returns the bitmap that represents the chart.
   *
   * @return
   */
  open fun getChartBitmap(): Bitmap {
    // Define a bitmap with the same size as the view
    val returnedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
    // Bind a canvas to it
    val canvas = Canvas(returnedBitmap)
    // Get the view's background
    val bgDrawable = background
    if (bgDrawable != null) {
      // has background drawable, then draw it on the canvas
      bgDrawable.draw(canvas)
    } else {
      // does not have background drawable, then draw white background on
      // the canvas
      canvas.drawColor(Color.WHITE)
    }
    // draw the view on the canvas
    draw(canvas)
    // return the bitmap
    return returnedBitmap
  }

  /**
   * Saves the current chart state with the given name to the given path on the sdcard leaving the
   * path empty "" will put the saved file directly on the SD card chart is saved as a PNG image,
   * example: saveToPath("myfilename", "foldername1/foldername2");
   *
   * @param title
   * @param pathOnSD e.g. "folder1/folder2/folder3"
   * @return returns true on success, false on error
   */
  open fun saveToPath(title: String, pathOnSD: String): Boolean {
    try {
      val documentsFolder = context.getExternalFilesDir(DIRECTORY_PICTURES)
      val imageFile = File(documentsFolder, "$title.png")
      FileOutputStream(imageFile).use {
        /*
         * Write bitmap to file using JPEG or PNG and 40% quality hint for
         * JPEG.
         */
        val bitmap = getChartBitmap()
        bitmap.compress(CompressFormat.PNG, 40, it)
      }
    } catch (e: Exception) {
      e.printStackTrace()
      return false
    }
    return true
  }

  @TargetApi(29)
  suspend fun createBitmapMediaUri(
      displayName: String,
      bitmap: Bitmap,
      format: CompressFormat = CompressFormat.PNG,
      quality: Int = 40,
      mimeType: String = "image/png",
  ): Uri? = suspendCoroutine { co ->
    val values =
        ContentValues().apply {
          put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
          put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
          put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM)
        }

    with(context.contentResolver) {
      val uri = insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
      val result = runCatching {
        val stream = uri?.let { openOutputStream(it) }
        stream.use { bitmap.compress(format, quality.coerceIn(10, 100), stream) }
      }
      if (result.isFailure) {
        uri?.let { delete(it, null, null) }
        result.exceptionOrNull()?.let { co.resumeWith(Result.failure(it)) }
      }
      co.resumeWith(Result.success(uri))
    }
  }

  /**
   * Saves the current state of the chart to the gallery as an image type. The compression must be
   * set for JPEG only. 0 == maximum compression, 100 = low compression (high quality). NOTE: Needs
   * permission WRITE_EXTERNAL_STORAGE
   *
   * @param fileName e.g. "my_image"
   * @param subFolderPath e.g. "ChartPics"
   * @param fileDescription e.g. "Chart details"
   * @param format e.g. Bitmap.CompressFormat.PNG
   * @param quality e.g. 50, min = 0, max = 100
   * @return returns true if saving was successful, false if not
   */
  open fun saveToGallery(
      fileName: String,
      subFolderPath: String = "",
      fileDescription: String? = "MPAndroidChart-Library Save",
      format: CompressFormat? = CompressFormat.PNG,
      quality: Int = 40
  ): Boolean {
    CoroutineScope(Dispatchers.IO).launch { createBitmapMediaUri(fileName, getChartBitmap()) }
    return true
  }

  /**
   * Saves the current state of the chart to the gallery as a JPEG image. The filename and
   * compression can be set. 0 == maximum compression, 100 = low compression (high quality). NOTE:
   * Needs permission WRITE_EXTERNAL_STORAGE
   *
   * @param fileName e.g. "my_image"
   * @param quality e.g. 50, min = 0, max = 100
   * @return returns true if saving was successful, false if not
   */
  open fun saveToGallery(fileName: String, quality: Int): Boolean {
    return saveToGallery(fileName, "", "MPAndroidChart-Library Save", CompressFormat.PNG, quality)
  }

  /**
   * Saves the current state of the chart to the gallery as a PNG image. NOTE: Needs permission
   * WRITE_EXTERNAL_STORAGE
   *
   * @param fileName e.g. "my_image"
   * @return returns true if saving was successful, false if not
   */
  open fun saveToGallery(fileName: String): Boolean {
    return saveToGallery(fileName, "", "MPAndroidChart-Library Save", CompressFormat.PNG, 40)
  }

  open fun removeViewportJob(job: Runnable) {
    mJobs.remove(job)
  }

  open fun clearAllViewportJobs() {
    mJobs.clear()
  }

  /**
   * Either posts a job immediately if the chart has already setup it's dimensions or adds the job
   * to the execution queue.
   *
   * @param job
   */
  open fun addViewportJob(job: Runnable) {
    if (viewPortHandler.hasChartDimens()) {
      post(job)
    } else {
      mJobs.add(job)
    }
  }

  /**
   * Returns all jobs that are scheduled to be executed after onSizeChanged(...).
   *
   * @return
   */
  open fun getJobs(): ArrayList<Runnable>? {
    return mJobs
  }

  override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
    for (i in 0 until childCount) {
      getChildAt(i).layout(left, top, right, bottom)
    }
  }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    val size = convertDpToPixel(50f).toInt()
    setMeasuredDimension(
        max(suggestedMinimumWidth, resolveSize(size, widthMeasureSpec)),
        max(suggestedMinimumHeight, resolveSize(size, heightMeasureSpec)))
  }

  override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
    if (isLogEnabled) Log.i(TAG, "OnSizeChanged()")
    if ((w > 0) && (h > 0) && (w < 10000) && (h < 10000)) {
      if (isLogEnabled) Log.i(TAG, "Setting chart dimens, width: $w, height: $h")
      viewPortHandler.setChartDimens(w.toFloat(), h.toFloat())
    } else {
      if (isLogEnabled) Log.w(TAG, "*Avoiding* setting chart dimens! width: $w, height: $h")
    }

    // This may cause the chart view to mutate properties affecting the view port --
    //   lets do this before we try to run any pending jobs on the view port itself
    notifyDataSetChanged()
    for (r: Runnable? in mJobs) {
      post(r)
    }
    mJobs.clear()
    super.onSizeChanged(w, h, oldw, oldh)
  }

  /**
   * Setting this to true will set the layer-type HARDWARE for the view, false will set layer-type
   * SOFTWARE.
   *
   * @param enabled
   */
  open fun setHardwareAccelerationEnabled(enabled: Boolean) {
    val type =
        when (enabled) {
          true -> LAYER_TYPE_HARDWARE
          false -> LAYER_TYPE_SOFTWARE
        }
    setLayerType(type, null)
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    if (mUnbind) unbindDrawables(this)
  }

  /** unbind flag */
  private var mUnbind = false

  /**
   * Unbind all drawables to avoid memory leaks. Link: http://stackoverflow.com/a/6779164/1590502
   *
   * @param view
   */
  private fun unbindDrawables(view: View) {
    if (view.background != null) {
      view.background.callback = null
    }
    if (view is ViewGroup) {
      for (i in 0 until view.childCount) {
        unbindDrawables(view.getChildAt(i))
      }
      view.removeAllViews()
    }
  }

  /**
   * Set this to true to enable "unbinding" of drawables. When a View is detached from a window.
   * This helps avoid memory leaks. Default: false Link: http://stackoverflow.com/a/6779164/1590502
   *
   * @param enabled
   */
  open fun setUnbindEnabled(enabled: Boolean) {
    mUnbind = enabled
  }
}
