package com.github.mikephil.charting.utils

import android.content.res.AssetManager
import android.util.Log
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import java.io.*

/**
 * Utilities class for interacting with the assets and the devices storage to load and save DataSet
 * objects from and to .txt files.
 *
 * @author Philipp Jahoda
 */
object FileUtils {
  private const val LOG = "MPChart-FileUtils"

  /**
   * Loads an array of Entries from a textfile from the assets folder.
   *
   * @param am
   * @param path the name of the file in the assets folder (+ path if needed)
   * @return
   */
  @JvmStatic
  fun loadEntriesFromAssets(am: AssetManager, path: String?): List<Entry> {
    if (path == null) return emptyList()
    return try {
      BufferedReader(InputStreamReader(am.open(path), "UTF-8")).useLines {
        it
            .map { line ->
              val split = line.split("#")
              if (split.size <= 2) {
                BarEntry(split[1].toFloat(), split[0].toFloat())
              } else {
                val vals = FloatArray(split.size - 1)
                for (i in vals.indices) {
                  vals[i] = split[i].toFloat()
                }
                BarEntry(split[split.size - 1].toFloat(), vals)
              }
            }
            .toList()
      }
    } catch (e: IOException) {
      Log.e(LOG, e.toString())
      emptyList()
    }
  }

  @JvmStatic
  fun loadBarEntriesFromAssets(am: AssetManager, path: String?): List<BarEntry> {
    if (path == null) return emptyList()
    return try {
      BufferedReader(InputStreamReader(am.open(path), "UTF-8")).useLines {
        it
            .map { line ->
              val split = line.split("#")
              BarEntry(split[1].toFloat(), split[0].toFloat())
            }
            .toList()
      }
    } catch (e: IOException) {
      Log.e(LOG, e.toString())
      emptyList()
    }
  }
}
