package com.github.mikephil.charting.listener

import android.view.GestureDetector.SimpleOnGestureListener
import android.view.View.OnTouchListener
import android.view.MotionEvent
import android.view.View

class OnDrawLineChartTouchListener : SimpleOnGestureListener(), OnTouchListener {
  override fun onTouch(v: View, event: MotionEvent): Boolean {
    return false
  }
}