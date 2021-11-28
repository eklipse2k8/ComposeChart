package com.github.eklipse2k8.charting.data.parceler

import android.os.Parcel
import android.os.ParcelFormatException
import android.os.Parcelable
import kotlinx.parcelize.Parceler

object SafeAnyParceler : Parceler<Any?> {
  override fun create(parcel: Parcel): Any? =
      if (parcel.readInt() == 1) {
        parcel.readParcelable<Parcelable>(Any::class.java.classLoader)
      } else {
        null
      }

  override fun Any?.write(parcel: Parcel, flags: Int) {
    if (this != null) {
      if (this is Parcelable) {
        parcel.writeInt(1)
        parcel.writeParcelable(this, flags)
      } else {
        throw ParcelFormatException("Cannot parcel an Entry with non-parcelable data")
      }
    } else {
      parcel.writeInt(0)
    }
  }
}
