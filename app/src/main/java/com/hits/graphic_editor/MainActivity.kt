package com.hits.graphic_editor

import android.R.color
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Color.valueOf
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.createBitmap
import com.hits.graphic_editor.databinding.ActivityMainBinding


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



typealias IntColor = Int
fun IntColor.alpha(): IntColor = this shr 24
fun IntColor.red(): IntColor = this shr 16 and 0xff
fun IntColor.green(): IntColor = this shr 8 and 0xff
fun IntColor.blue(): IntColor = this and 0xff
fun getBilinearFilteredPixelInt(pixels: IntArray, width: Int, height: Int, u: Float, v: Float): Int
{
    val floatX = u * width
    val floatY = v * height

    val x = floatX.toInt()
    val y = floatY.toInt()

    val uRatio = floatX - x
    val vRatio = floatY - y
    val uOpposite = 1 - uRatio
    val vOpposite = 1 - vRatio

    val alpha = ((pixels[x * width + y].alpha() * uOpposite + pixels[(x + 1) * width + y].alpha() * uRatio) * vOpposite +
            (pixels[x * width + y + 1].alpha() * uOpposite + pixels[(x + 1) * width + y + 1].alpha() * uRatio) * vRatio).toInt()

    val red = ((pixels[x * width + y].red() * uOpposite + pixels[(x + 1) * width + y].red() * uRatio) * vOpposite +
                (pixels[x * width + y + 1].red() * uOpposite + pixels[(x + 1) * width + y + 1].red() * uRatio) * vRatio).toInt()

    val green = ((pixels[x * width + y].green() * uOpposite + pixels[(x + 1) * width + y].green() * uRatio) * vOpposite +
            (pixels[x * width + y + 1].green() * uOpposite + pixels[(x + 1) * width + y + 1].green() * uRatio) * vRatio).toInt()

    val blue = ((pixels[x * width + y].blue() * uOpposite + pixels[(x + 1) * width + y].blue() * uRatio) * vOpposite +
            (pixels[x * width + y + 1].blue() * uOpposite + pixels[(x + 1) * width + y + 1].blue() * uRatio) * vRatio).toInt()

    return (alpha shl 24) or (red shl 16) or (green shl 8) or blue
}
fun getBilinearFilteredBitmap(input: Bitmap, coeff: Float): Bitmap
{
    val oldHeight = input.height
    val oldWidth = input.width

    val newHeight = (oldHeight * coeff).toInt()
    val newWidth = (oldWidth * coeff).toInt()

    val newPixels = IntArray(newWidth * newHeight)

    val oldPixels = IntArray(oldWidth * oldHeight)
    input.getPixels(oldPixels, 0, oldWidth, 0, 0, oldWidth, oldHeight)

    for (y in 0 until newHeight - coeff.toInt())
    {
        for (x in 0 until newWidth - coeff.toInt())
        {
            newPixels[x * newWidth + y] =
                getBilinearFilteredPixelInt(oldPixels, oldWidth, oldHeight, x / newWidth.toFloat(), y / newHeight.toFloat())
        }
    }

    val output = createBitmap(newWidth, newHeight)
    output.setPixels(newPixels, 0, newWidth, 0, 0, newWidth, newHeight);
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
            var bitmap = drawable.bitmap.copy(Bitmap.Config.ARGB_8888, true)
            bitmap = getBilinearFilteredBitmap(bitmap, 3F)

            binding.image2.setImageBitmap(bitmap)
        }

        /**/
    }
}