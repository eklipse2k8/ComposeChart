package com.github.eklipse2k8.charting.renderer

import com.github.eklipse2k8.charting.utils.ViewPortHandler

/**
 * Abstract baseclass of all Renderers.
 *
 * @author Philipp Jahoda
 */
abstract class Renderer(
    /** the component that handles the drawing area of the chart and it's offsets */
    @JvmField protected var mViewPortHandler: ViewPortHandler
)
