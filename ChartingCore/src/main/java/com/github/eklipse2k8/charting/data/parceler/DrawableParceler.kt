package com.github.eklipse2k8.charting.data.parceler

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Parcel
import kotlinx.parcelize.Parceler

object DrawableParceler : Parceler<Drawable?> {
  override fun create(parcel: Parcel): Drawable {
    val bitmap = parcel.readParcelable<Bitmap>(javaClass.classLoader)
    return BitmapDrawable(bitmap)
  }

  override fun Drawable?.write(parcel: Parcel, flags: Int) {
    if (this is BitmapDrawable) {
      parcel.writeParcelable(bitmap, flags)
    }
  }
}
