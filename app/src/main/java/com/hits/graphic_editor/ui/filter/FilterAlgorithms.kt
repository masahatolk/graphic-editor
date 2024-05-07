package com.hits.graphic_editor.ui.filter

import com.hits.graphic_editor.custom_api.SimpleImage
import com.hits.graphic_editor.custom_api.alpha
import com.hits.graphic_editor.custom_api.argbToInt
import com.hits.graphic_editor.custom_api.blue
import com.hits.graphic_editor.custom_api.getTruncatedChannel
import com.hits.graphic_editor.custom_api.green
import com.hits.graphic_editor.custom_api.red
import java.lang.Math.random
import kotlin.math.min

fun inverse(simpleImage: SimpleImage): SimpleImage {

    val img = simpleImage.copy(pixels = simpleImage.pixels.clone())

    for (i in 0 until img.height) {
        for (j in 0 until img.width) {
            img[j, i] = argbToInt(
                img[j, i].alpha(),
                255 - img[j, i].red(),
                255 - img[j, i].green(),
                255 - img[j, i].blue()
            )
        }
    }

    return img
}

fun grayscale(simpleImage: SimpleImage): SimpleImage {

    val img = simpleImage.copy(pixels = simpleImage.pixels.clone())

    for (i in 0 until img.height) {
        for (j in 0 until img.width) {

            val averageValue =
                (img[j, i].red() + img[j, i].green() + img[j, i].blue()) / 3

            img[j, i] = argbToInt(
                img[j, i].alpha(),
                averageValue,
                averageValue,
                averageValue
            )
        }
    }

    return img
}

fun blackAndWhite(simpleImage: SimpleImage): SimpleImage {

    val img = simpleImage.copy(pixels = simpleImage.pixels.clone())

    for (i in 0 until img.height) {
        for (j in 0 until img.width) {

            val averageValue =
                (img[j, i].red() + img[j, i].green() + img[j, i].blue()) / 3

            val value: Int = if (averageValue > 128) 255
            else 0

            img[j, i] = argbToInt(
                img[j, i].alpha(),
                value,
                value,
                value
            )
        }
    }

    return img
}

fun sepia(simpleImage: SimpleImage): SimpleImage {

    val img = simpleImage.copy(pixels = simpleImage.pixels.clone())

    for (i in 0 until img.height) {
        for (j in 0 until img.width) {

            val red =
                min(
                    255, (img[j, i].red() * 0.393 +
                            img[j, i].green() * 0.769 +
                            img[j, i].blue() * 0.189).toInt()
                )

            val green =
                min(
                    255, (img[j, i].red() * 0.349 +
                            img[j, i].green() * 0.686 +
                            img[j, i].blue() * 0.168).toInt()
                )

            val blue =
                min(
                    255, (img[j, i].red() * 0.272 +
                            img[j, i].green() * 0.534 +
                            img[j, i].blue() * 0.131).toInt()
                )

            img[j, i] = argbToInt(
                img[j, i].alpha(),
                red,
                green,
                blue
            )
        }
    }

    return img
}

fun contrast(simpleImage: SimpleImage, contrastCoefficient: Int): SimpleImage {

    val img = simpleImage.copy(pixels = simpleImage.pixels.clone())
    val contrastCorrectionFactor: Float =
        259 * (contrastCoefficient.toFloat() + 255) / 255 / (259 - contrastCoefficient.toFloat())

    for (i in 0 until img.height) {
        for (j in 0 until img.width) {

            val red =
                getTruncatedChannel((img[j, i].red() - 128).toFloat() * contrastCorrectionFactor + 128)
            val green =
                getTruncatedChannel((img[j, i].green() - 128).toFloat() * contrastCorrectionFactor + 128)
            val blue =
                getTruncatedChannel((img[j, i].blue() - 128).toFloat() * contrastCorrectionFactor + 128)

            img.pixels[i * img.width + j] = argbToInt(
                img[j, i].alpha(),
                red,
                green,
                blue
            )
        }
    }

    return img
}

fun rgb(simpleImage: SimpleImage, rgbMode: RGBMode): SimpleImage {
    val img = simpleImage.copy(pixels = simpleImage.pixels.clone())

    when (rgbMode) {
        RGBMode.RED -> {
            for (i in 0 until img.height) {
                for (j in 0 until img.width) {
                    img[j, i] = argbToInt(
                        img[j, i].alpha(),
                        img[j, i].red(),
                        0,
                        0
                    )
                }
            }
        }

        RGBMode.GREEN -> {
            for (i in 0 until img.height) {
                for (j in 0 until img.width) {
                    img[j, i] = argbToInt(
                        img[j, i].alpha(),
                        0,
                        img[j, i].green(),
                        0
                    )
                }
            }
        }

        RGBMode.BLUE -> {
            for (i in 0 until img.height) {
                for (j in 0 until img.width) {
                    img[j, i] = argbToInt(
                        img[j, i].alpha(),
                        0,
                        0,
                        img[j, i].blue()
                    )
                }
            }
        }
    }

    return img
}

fun mosaic(simpleImage: SimpleImage, squareSide: Int): SimpleImage {
    val img = simpleImage.copy(pixels = simpleImage.pixels.clone())

    for (i in 0 until img.height step squareSide) {
        for (j in 0 until img.width step squareSide) {

            var count = 0
            var red = 0
            var green = 0
            var blue = 0

            val height = min(i + squareSide, img.height)
            val width = min(j + squareSide, img.width)

            for (y in i until height) {
                for (x in j until width) {
                    red += img[x, y].red()
                    green += img[x, y].green()
                    blue += img[x, y].blue()
                    count++
                }
            }

            red /= count
            green /= count
            blue /= count

            for (y in i until height) {
                for (x in j until width) {
                    img[x, y] = argbToInt(
                        img[x, y].alpha(),
                        red,
                        green,
                        blue
                    )
                }
            }
        }
    }

    return img
}

fun grain(simpleImage: SimpleImage, number: Int): SimpleImage {
    val img = simpleImage.copy(pixels = simpleImage.pixels.clone())

    for (i in 0 until img.height) {
        for (j in 0 until img.width) {

            val temp = random()
            if(temp < 0.5) {
                val coefficient = 1 - number.toFloat() / 100

                val red = getTruncatedChannel((img[j, i].red() * coefficient).toInt())
                val green = getTruncatedChannel((img[j, i].green() * coefficient).toInt())
                val blue = getTruncatedChannel((img[j, i].blue() * coefficient).toInt())

                img[j, i] = argbToInt(
                    img[j, i].alpha(),
                    red,
                    green,
                    blue
                )
            }
        }
    }

    return img
}

fun channelShift(
    simpleImage: SimpleImage,
    horizontalShift: Int,
    verticalShift: Int
): SimpleImage {
    val img = simpleImage.copy(pixels = simpleImage.pixels.clone())

    for (i in 0 until img.height) {
        for (j in 0 until img.width) {
            val redX = getTruncatedCoordinate(img.width, j + horizontalShift)
            val redY = getTruncatedCoordinate(img.height, i + verticalShift)
            val blueX = getTruncatedCoordinate(img.width, j - horizontalShift)
            val blueY = getTruncatedCoordinate(img.height, i - verticalShift)

            img[redX, redY] = argbToInt(
                img[redX, redY].alpha(),
                simpleImage[j, i].red(),
                img[redX, redY].green(),
                img[redX, redY].blue()
            )

            img[blueX, blueY] = argbToInt(
                img[blueX, blueY].alpha(),
                img[blueX, blueY].red(),
                img[blueX, blueY].green(),
                simpleImage[j, i].blue()
            )
        }
    }

    return img
}

private fun getTruncatedCoordinate(border: Int, e: Int): Int {
    if (e >= border) return border - 1
    else if (e < 0) return 0
    return e
}