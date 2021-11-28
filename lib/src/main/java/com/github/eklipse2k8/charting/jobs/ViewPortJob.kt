package com.github.eklipse2k8.charting.jobs

import android.view.View
import com.github.eklipse2k8.charting.utils.ObjectPool.Poolable
import com.github.eklipse2k8.charting.utils.Transformer
import com.github.eklipse2k8.charting.utils.ViewPortHandler

/**
 * Runnable that is used for viewport modifications since they cannot be executed at any time. This
 * can be used to delay the execution of viewport modifications until the onSizeChanged(...) method
 * of the chart-view is called. This is especially important if viewport modifying methods are
 * called on the chart directly after initialization.
 *
 * @author Philipp Jahoda
 */
abstract class ViewPortJob(
    protected var viewPortHandler: ViewPortHandler?,
    var xValue: Float = 0f,
    var yValue: Float = 0f,
    protected var transformer: Transformer?,
    protected var view: View?
) : Poolable(), Runnable {

  protected var pts = FloatArray(2)
}
