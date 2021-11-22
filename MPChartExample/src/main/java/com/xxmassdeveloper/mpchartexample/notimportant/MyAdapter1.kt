package com.xxmassdeveloper.mpchartexample.notimportant

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.xxmassdeveloper.mpchartexample.R

/** Created by Philipp Jahoda on 07/12/15. */
internal class MyAdapter(context: Context, objects: List<ContentItem?>?) :
    ArrayAdapter<ContentItem?>(context, 0, objects!!) {

  private val typeFaceLight: Typeface
  private val typeFaceRegular: Typeface

  @RequiresApi(Build.VERSION_CODES.M)
  @SuppressLint("InflateParams")
  override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
    val contentItem = getItem(position)
    val holder = ViewHolder()
    val view =
        if (contentItem != null && contentItem.isSection) {
          LayoutInflater.from(context).inflate(R.layout.list_item_section, null)
        } else {
          LayoutInflater.from(context).inflate(R.layout.list_item, null)
        }
    holder.tvName =
        view.findViewById<TextView>(R.id.tvName).apply {
          typeface =
              if (contentItem != null && contentItem.isSection) typeFaceRegular else typeFaceLight
          text = contentItem?.name
        }
    holder.tvDesc =
        view.findViewById<TextView>(R.id.tvDesc).apply {
          typeface = typeFaceLight
          text = contentItem?.desc
        }
    view.tag = holder
    return view
  }

  private inner class ViewHolder {
    lateinit var tvName: TextView
    lateinit var tvDesc: TextView
  }

  init {
    typeFaceLight = Typeface.createFromAsset(context.assets, "OpenSans-Light.ttf")
    typeFaceRegular = Typeface.createFromAsset(context.assets, "OpenSans-Regular.ttf")
  }
}
