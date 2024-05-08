package com.hits.graphic_editor

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hits.graphic_editor.custom_api.MipMapsContainer
import com.hits.graphic_editor.custom_api.SimpleImage
import com.hits.graphic_editor.custom_api.getBitMap
import com.hits.graphic_editor.custom_api.getSimpleImage
import com.hits.graphic_editor.databinding.ActivityMainBinding
import com.hits.graphic_editor.utils.getBilinearFilteredPixelInt
import kotlin.math.ceil

data class Vec2(var x: Float, var y: Float)
fun getAffineTransformedSimpleImage(img: SimpleImage, matrix: Array<Array<Float>>): SimpleImage
{
    val oldHeight = img.height
    val oldWidth = img.width

    // function ignores coordinates offset and centers new image
    fun getTransformedVec2(vec: Vec2, matrix: Array<Array<Float>>): Vec2
            = Vec2(
        matrix[0][0] * vec.x + matrix[0][1] * vec.y,
        matrix[1][0] * vec.x + matrix[1][1] * vec.y)

    val reversedMatrix = getReversedTransformationMatrix(matrix)

    val cornerCoordinates = arrayOf(
        Vec2(0F, 0F),
        getTransformedVec2(Vec2(img.width.toFloat(), 0F), matrix),
        getTransformedVec2(Vec2(0F, img.height.toFloat()), matrix),
        getTransformedVec2(Vec2(img.width.toFloat(), img.height.toFloat()), matrix))

    val xOffset = 0
    val yOffset =0

    val translatedLeft = ceil(cornerCoordinates.minBy{ it.x }.x + xOffset).toInt()
    val translatedTop = ceil(cornerCoordinates.minBy{ it.y }.y + yOffset).toInt()
    val translatedRight = (cornerCoordinates.maxBy{ it.x }.x + xOffset).toInt()
    val translatedBottom = (cornerCoordinates.maxBy{ it.y }.y + yOffset).toInt()

    val newWidth = translatedRight - translatedLeft
    val newHeight = translatedBottom - translatedTop
    val newImg = SimpleImage(newWidth, newHeight)

    for (translatedX in translatedLeft until translatedRight)
    {
        for (translatedY in translatedTop until translatedBottom)
        {
            val oldVec = getTransformedVec2(
                Vec2(translatedX.toFloat(), translatedY.toFloat()),
                reversedMatrix)
            val newX = translatedX - translatedLeft
            val newY = translatedY - translatedTop

            if (0 <= oldVec.x && oldVec.x < oldWidth &&
                0 <= oldVec.y && oldVec.y < oldHeight)
                newImg[newX, newY] = getBilinearFilteredPixelInt(
                    img, oldVec.x / oldWidth, oldVec.y / oldHeight)
        }
    }
    return newImg
}
data class PointTransfer(
    var fromX: Float,
    var fromY: Float,
    var toX: Float,
    var toY: Float,
)
fun getAffineTransformationMatrix(
    transf1: PointTransfer, transf2: PointTransfer, transf3: PointTransfer): Array<Array<Float>>
{
    // start X coordinates
    val a = arrayOf(transf1.fromX, transf2.fromX, transf3.fromX)
    // start Y coordinates
    val b = arrayOf(transf1.fromY, transf2.fromY, transf3.fromY)
    // new X coordinates
    val c1 = arrayOf(transf1.toX, transf2.toX, transf3.toX)
    // new Y coordinates
    val c2 = arrayOf(transf1.toY, transf2.toY, transf3.toY)

    fun getTransformCoeffs(c: Array<Float>): Array<Float>
    {
        val x2 = ((a[1]-a[0])*(c[2]-c[0])-(a[2]-a[0])*(c[1]-c[0]))/
                ((a[1]-a[0])*(b[2]-b[0])-(a[2]-a[0])*(b[1]-b[0]))
        val x1 = ((c[2]-c[0])-(b[2]-b[0])*x2)/(a[2]-a[0])
        val x3 = c[0]-a[0]*x1-b[0]*x2

        return arrayOf(x1, x2, x3)
    }

    return arrayOf(
        getTransformCoeffs(c1),
        getTransformCoeffs(c2)
    )
}
fun getReversedTransformationMatrix(matrix: Array<Array<Float>>): Array<Array<Float>>
{
    val ax = matrix[0][0]
    val bx = matrix[0][1]
    val ay = matrix[1][0]
    val by = matrix[1][1]

    val newBx = bx/(bx*ay-by*ax)
    val newAx = by/(by*ax-bx*ay)

    val newBy = ax/(ax*by-ay*bx)
    val newAy = ay/(ay*bx-ax*by)

    return arrayOf(
        arrayOf(newAx, newBx, -matrix[0][2]),
        arrayOf(newAy, newBy,-matrix[1][2])
    )
}




class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btn.setOnClickListener {
            val drawable = binding.image1.drawable as BitmapDrawable
            var bitmap = drawable.bitmap.copy(Bitmap.Config.ARGB_8888, true)
            /* ---------------------------------- */

            bitmap = getBitMap(getAffineTransformedSimpleImage(getSimpleImage(bitmap),
                getAffineTransformationMatrix(
                    PointTransfer(50F, 50F, 100F, 150F),
                    PointTransfer(100F, 150F, 250F, 30F),
                    PointTransfer(200F, 30F, 40F, 40F)
                )
            ))

            /* ---------------------------------- */
            binding.image2.setImageBitmap(bitmap)
        }
    }
}