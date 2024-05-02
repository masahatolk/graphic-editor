package com.hits.graphic_editor

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val photo = intent?.getStringExtra("photo")

        binding.imageView.setImageURI(photo?.toUri())

        binding.rotateButton.setOnClickListener{
            try {
                val photoUri = Uri.parse(photo)
                val originalBitmap = getBitmapFromUri(photoUri)

                if (originalBitmap != null) {
                    val rotatedBitmap = rotateBitmap(originalBitmap, 180)
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


    fun getBitmapFromUri(uri: Uri?): Bitmap? {
        Log.d("BitmapLoading", "Attempting to load bitmap from URI: $uri")
        uri?.let{
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


    //https://www.sciencedirect.com/topics/computer-science/image-rotation
    fun rotateBitmap(originalBitmap: Bitmap, angle: Int): Bitmap {
        val radians = Math.toRadians(angle.toDouble())
        val cos = Math.cos(radians)
        val sin = Math.sin(radians)

        val width = originalBitmap.width
        val height = originalBitmap.height

        val newWidth = (Math.abs(width * cos) + Math.abs(height * sin)).toInt()
        val newHeight = (Math.abs(width * sin) + Math.abs(height * cos)).toInt()

        val newBitmap = Bitmap.createBitmap(newWidth, newHeight, originalBitmap.config)

        for (x in 0 until newWidth) {
            for (y in 0 until newHeight) {
                val centerX = newWidth / 2
                val centerY = newHeight / 2

                val newX = (x - centerX) * cos - (y - centerY) * sin + centerX
                val newY = (x - centerX) * sin + (y - centerY) * cos + centerY

                if (newX >= 0 && newX < width && newY >= 0 && newY < height) {
                    newBitmap.setPixel(x, y, originalBitmap.getPixel(newX.toInt(), newY.toInt()))
                } else {
                    newBitmap.setPixel(x, y, Color.TRANSPARENT)
                }
            }
        }

        return newBitmap
    }


}