package com.hits.graphic_editor
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.hits.graphic_editor.databinding.ActivityNewProjectBinding

class NewProjectActivity : AppCompatActivity() {

    private val binding: ActivityNewProjectBinding by lazy {
        ActivityNewProjectBinding.inflate(layoutInflater)
    }

    private var lastRotatedBitmap: Bitmap? = null
    private var totalRotationAngle = 0

    private val rotation = Rotation()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val photo = intent?.getStringExtra("photo")

        binding.imageView.setImageURI(photo?.toUri())

        binding.rotateButton.setOnClickListener {
            try {
                val photoUri = photo?.toUri()
                val originalBitmap = BitmapUtils.getBitmapFromUri(contentResolver, photoUri)

                if (originalBitmap != null) {
                    val rotatedBitmap = if (lastRotatedBitmap != null) {
                        rotation.rotateBitmap90Clockwise(lastRotatedBitmap!!)
                    } else {
                        rotation.rotateBitmap90Clockwise(originalBitmap)
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
}