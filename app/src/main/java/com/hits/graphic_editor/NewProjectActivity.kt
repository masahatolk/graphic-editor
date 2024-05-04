package com.hits.graphic_editor

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.hits.graphic_editor.databinding.ActivityNewProjectBinding
import java.io.File
import java.io.InputStream
import kotlin.math.cos

class NewProjectActivity : AppCompatActivity() {

    private val binding: ActivityNewProjectBinding by lazy {
        ActivityNewProjectBinding.inflate(layoutInflater)
    }

    private var lastRotatedBitmap: Bitmap? = null
    private var totalRotationAngle = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val photo = intent?.getStringExtra("photo")

        binding.imageView.setImageURI(photo?.toUri())

        binding.rotateButton.setOnClickListener {
            try {
                val photoUri = Uri.parse(photo)
                val originalBitmap = getBitmapFromUri(photoUri)

                if (originalBitmap != null) {
                    val rotatedBitmap = if (lastRotatedBitmap != null) {
                        rotateBitmap90Clockwise(lastRotatedBitmap!!)
                    } else {
                        rotateBitmap90Clockwise(originalBitmap)
                    }
                    lastRotatedBitmap = rotatedBitmap

                    val drawable = BitmapDrawable(resources, rotatedBitmap)
                    binding.imageView.setImageDrawable(drawable)
                    totalRotationAngle += 90
                    totalRotationAngle %= 360
                } else {
                    Toast.makeText(this, "Не удалось загрузить изображение", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Произошла ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //https://www.sciencedirect.com/topics/computer-science/image-rotation
    //http://www.leptonica.org/rotation.html#SPECIAL-ROTATIONS

    fun rotateBitmap90Clockwise(originalBitmap: Bitmap): Bitmap {
        val width = originalBitmap.width
        val height = originalBitmap.height

        val pixels = IntArray(width * height)
        originalBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val rotatedWidth = height
        val rotatedHeight = width
        val rotatedPixels = IntArray(rotatedWidth * rotatedHeight)

        for (x in 0 until width) {
            for (y in 0 until height) {
                val newX = y
                val newY = width - 1 - x
                rotatedPixels[newY * rotatedWidth + newX] = pixels[y * width + x]
            }
        }

        val rotatedBitmap = Bitmap.createBitmap(rotatedPixels, rotatedWidth, rotatedHeight, originalBitmap.config)

        return rotatedBitmap
    }

    fun getBitmapFromUri(uri: Uri?): Bitmap? {
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
