package com.hits.graphic_editor

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Color.valueOf
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.createBitmap
import com.hits.graphic_editor.databinding.ActivityMainBinding
import java.io.IOException

//import kotlinx.android.synthetic.main.activity_main.*

fun getBilinearFilteredPixelColor(bitmap: Bitmap, u: Float, v: Float): Color
{
    val floatX = u * bitmap.width
    val floatY = v * bitmap.height

    val x = floatX.toInt()
    val y = floatY.toInt()

    val uRatio = floatX - x
    val vRatio = floatY - y
    val uOpposite = 1 - uRatio
    val vOpposite = 1 - vRatio

    val oldComponentsArray = arrayOf(
        bitmap.getColor(x, y).components, bitmap.getColor(x, y + 1).components,
        bitmap.getColor(x + 1, y).components, bitmap.getColor(x + 1, y + 1).components
    )

    val newComponents = FloatArray(4)

    for (j in 0..3)
        newComponents[j] = (oldComponentsArray[0][j] * uOpposite + oldComponentsArray[2][j] * uRatio) * vOpposite +
            (oldComponentsArray[1][j] * uOpposite + oldComponentsArray[3][j] * uRatio) * vRatio

    return valueOf(newComponents[0], newComponents[1],
        newComponents[2], newComponents[3])
}
fun getBilinearFilteredBitmap(input: Bitmap, coeff: Float): Bitmap
{
    val oldHeight = input.height
    val oldWidth = input.width

    val newHeight = (oldHeight * coeff).toInt()
    val newWidth = (oldWidth * coeff).toInt()

    val output = createBitmap(newWidth, newHeight)

    for (y in 0 until newHeight - coeff.toInt())
    {
        for (x in 0 until newWidth - coeff.toInt())
        {
            output.setPixel(x, y,
                getBilinearFilteredPixelColor(input, x / newWidth.toFloat(), y / newHeight.toFloat()).toArgb()
                )
        }
    }

    return output
}

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /**/

        binding.btn.setOnClickListener {
            val drawable = binding.image1.drawable as BitmapDrawable
            var bitmap = drawable.bitmap.copy(drawable.bitmap.config, true)

            //bitmap.config = Bitmap.Config.RGBA_F16;
            bitmap = getBilinearFilteredBitmap(bitmap, 3F)

            binding.image2.setImageBitmap(bitmap)
        }

        /**/
    }
}