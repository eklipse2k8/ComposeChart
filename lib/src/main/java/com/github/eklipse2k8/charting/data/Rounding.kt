package com.github.eklipse2k8.charting.data

/**
 * Determines how to round DataSet index values for [DataSet.getEntryIndex]
 * DataSet.getEntryIndex()} when an exact x-index is not found.
 */
enum class Rounding {
  UP,
  DOWN,
  CLOSEST
}