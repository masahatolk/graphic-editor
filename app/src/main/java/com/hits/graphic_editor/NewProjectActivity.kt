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

                    val rotatedBitmap = rotateBitmap90Clockwise(originalBitmap, totalRotationAngle)
                    totalRotationAngle += 90
                    totalRotationAngle %= 360
                    val drawable = BitmapDrawable(resources, rotatedBitmap)
                    binding.imageView.setImageDrawable(drawable)
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

    fun rotateBitmap90Clockwise(originalBitmap: Bitmap, angle: Int): Bitmap {
        val width = originalBitmap.width
        val height = originalBitmap.height

        val rotatedBitmap = Bitmap.createBitmap(height, width, originalBitmap.config)

        for (x in 0 until width) {
            for (y in 0 until height) {
                val newX = y
                val newY = width - 1 - x
                val pixelColor = originalBitmap.getPixel(x, y)
                rotatedBitmap.setPixel(newX, newY, pixelColor)
            }
        }

        if (angle > 0) {
            return rotateBitmap(rotatedBitmap, angle)
        }

        return rotatedBitmap
    }

    fun rotateBitmap(originalBitmap: Bitmap, angle: Int): Bitmap {
        val width = originalBitmap.width
        val height = originalBitmap.height

        val rotatedBitmap = Bitmap.createBitmap(width, height, originalBitmap.config)

        val centerX = width / 2f
        val centerY = height / 2f

        val radians = Math.toRadians(angle.toDouble())
        val cos = Math.cos(radians)
        val sin = Math.sin(radians)

        for (x in 0 until width) {
            for (y in 0 until height) {
                val newX = (x - centerX) * cos - (y - centerY) * sin + centerX
                val newY = (x - centerX) * sin + (y - centerY) * cos + centerY

                if (newX >= 0 && newX < width && newY >= 0 && newY < height) {
                    val newColor = originalBitmap.getPixel(newX.toInt(), newY.toInt())
                    rotatedBitmap.setPixel(x, y, newColor)
                }
            }
        }

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
