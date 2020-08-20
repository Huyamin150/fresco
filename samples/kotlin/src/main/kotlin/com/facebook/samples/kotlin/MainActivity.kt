/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.samples.kotlin

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.backends.pipeline.Fresco
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.util.concurrent.Executors
import java.util.concurrent.Future

class MainActivity : AppCompatActivity() {
  companion object {
    private const val GRID_COLUMN_COUNT = 2
    private const val PERMISSION_REQUEST_CODE = 42
    private const val FLIPPER_INDEX_RECYCLER_VIEW = 1

    private val executor = Executors.newSingleThreadExecutor()
  }

  private val dataSource = MediaStoreData()
  private var loadPhotosFuture: Future<out Any>? = null
  private lateinit var imageAdapter: ImageAdapter

  override fun onCreate(savedInstance: Bundle?) {
    super.onCreate(savedInstance)
    setContentView(R.layout.activity_main)
    jump.setOnClickListener {
      startActivity(Intent(this,SimpleActivity::class.java))
    }
    imageAdapter = ImageAdapter(
        ColorDrawable(ContextCompat.getColor(this, R.color.load_placeholder)),
        ColorDrawable(ContextCompat.getColor(this, R.color.load_fail)),
        540)
    with(recycler_view) {
      layoutManager = GridLayoutManager(
          context,
          GRID_COLUMN_COUNT,
          GridLayoutManager.VERTICAL,
          false)
      adapter = imageAdapter
    }
  }

  override fun onResume() {
    super.onResume()
    if (!requestStoragePermissionsIfNeeded()) {
      loadPhotos()
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    loadPhotosFuture?.cancel(true)
  }

  override fun onRequestPermissionsResult(requestCode: Int,
                                          permissions: Array<out String>,
                                          grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode != PERMISSION_REQUEST_CODE) {
      return
    }

    for ((i, permission) in permissions.withIndex()) {
      if (permission != Manifest.permission.READ_EXTERNAL_STORAGE) {
        continue
      }
      if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
        loadPhotos()
      } else {
        Toast.makeText(this, R.string.permission_denied_toast, Toast.LENGTH_LONG).show()
      }
      return
    }
  }

  /**
   * Returns true if storage permissions were requested because they are needed, false otherwise
   */
  private fun requestStoragePermissionsIfNeeded(): Boolean {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
        PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(
          this,
          arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
          PERMISSION_REQUEST_CODE)
      return true
    }

    return false
  }

  private fun loadPhotos() {
    val uris = mutableListOf<Uri>()
    val results = StringBuilder()
    val assetManager = resources.assets
    val inputStream = assetManager.open("test.json")
    val reader = BufferedReader(InputStreamReader(inputStream))
    reader.use { reader ->
      while (true) {
        val line = reader.readLine() ?: break
        results.append(line)
      }

    }
    val inputAsString = results.toString()
    Log.e("====","inputString = $inputAsString")
    try {
      val jsonArray = JSONArray(inputAsString)
      for (i in 0 .. jsonArray.length()){
        val subObj = jsonArray.getJSONObject(i)
        val itemUrl = subObj.optJSONObject("feed").optJSONObject("content").optJSONObject("cover")
                .optString("url")
        uris.add(Uri.parse(itemUrl))
      }
    }catch (e:Exception){
      Log.e("====","异常${e.toString()}")
    }

    loadPhotosFuture?.cancel(true)
    loadPhotosFuture = executor.submit {
      runOnUiThread {
        imageAdapter.setUris(uris)
//        view_flipper.displayedChild = FLIPPER_INDEX_RECYCLER_VIEW
      }
    }
  }
}
