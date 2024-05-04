package com.hits.graphic_editor

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log

object BitmapUtils {

    fun getBitmapFromUri(contentResolver: ContentResolver, uri: Uri?): Bitmap? {
        Log.d("BitmapLoading", "Attempting to load bitmap from URI: $uri")
        uri?.let {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            if (bitmap == null) {
                Log.d("BitmapLoading", "Failed to load bitmap from URI")
            } else {
                Log.d("BitmapLoading", "Bitmap loaded successfully")
            }
            return bitmap
        }
        Log.d("BitmapLoading", "URI is null, cannot load bitmap")
        return null
    }
}
