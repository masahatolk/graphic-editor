package com.hits.graphic_editor.rotation

import com.hits.graphic_editor.affine_transform.getAffineTransformedSimpleImage
import com.hits.graphic_editor.custom_api.MipMapsContainer
import com.hits.graphic_editor.custom_api.SimpleImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

private fun getRotated0DegreesSimpleImage(img: SimpleImage): SimpleImage {
    return img
}
private suspend fun getRotated90DegreesSimpleImage(img: SimpleImage): SimpleImage {

    val newImage = SimpleImage(img.height, img.width)
    val jobs: MutableList<Job> = mutableListOf()

    for (y in 0 until newImage.height)
        jobs.add(CoroutineScope(Dispatchers.Default).launch {
            for (x in 0 until newImage.width)
                newImage[x, y] = img[y, img.height - x - 1]
        })

    jobs.forEach { it.join() }
    return newImage
}
private suspend fun getRotated180DegreesSimpleImage(img: SimpleImage): SimpleImage {

    val newImage = SimpleImage(img.width, img.height)
    val jobs: MutableList<Job> = mutableListOf()

    for (y in 0 until newImage.height)
        jobs.add(CoroutineScope(Dispatchers.Default).launch {
            for (x in 0 until newImage.width)
                newImage[x, y] = img[x, img.height - y - 1]
        })

    jobs.forEach { it.join() }
    return newImage
}
private suspend fun getRotated270DegreesSimpleImage(img: SimpleImage): SimpleImage {

    val newImage = SimpleImage(img.height, img.width)
    val jobs: MutableList<Job> = mutableListOf()

    for (y in 0 until newImage.height)
        jobs.add(CoroutineScope(Dispatchers.Default).launch {
            for (x in 0 until newImage.width)
                newImage[x, y] = img[img.width - y - 1, x]
        })

    jobs.forEach { it.join() }
    return newImage
}

suspend
fun getRotatedSimpleImage(input: MipMapsContainer, degAngle: Int): SimpleImage
{
    if (degAngle == 0) return getRotated0DegreesSimpleImage(input.img)
    if (degAngle == 90) return getRotated90DegreesSimpleImage(input.img)
    if (degAngle == 180) return getRotated180DegreesSimpleImage(input.img)
    if (degAngle == 270) return getRotated270DegreesSimpleImage(input.img)

    val angleRadians: Float = Math.toRadians(degAngle.toDouble()).toFloat()
    return getAffineTransformedSimpleImage(input,
        arrayOf(
            arrayOf(cos(angleRadians),-sin(angleRadians),0F),
            arrayOf(sin(angleRadians), cos(angleRadians),0F)
        ))
}