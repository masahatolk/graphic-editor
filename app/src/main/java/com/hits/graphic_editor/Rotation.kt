package com.hits.graphic_editor

import android.graphics.Bitmap

class Rotation {
    //https://www.sciencedirect.com/topics/computer-science/image-rotation
    //http://www.leptonica.org/rotation.html#SPECIAL-ROTATIONS

    fun rotateBitmap90Clockwise(originalBitmap: Bitmap): Bitmap {
        val width = originalBitmap.width
        val height = originalBitmap.height

        val pixels = IntArray(width * height)
        originalBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val rotatedPixels = IntArray(height * width)

        for (x in 0 until width) {
            for (y in 0 until height) {
                val newY = width - 1 - x
                rotatedPixels[newY * height + y] = pixels[y * width + x]
            }
        }

        return Bitmap.createBitmap(rotatedPixels, height, width, originalBitmap.config)
    }

}