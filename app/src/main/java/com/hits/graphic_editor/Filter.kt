package com.hits.graphic_editor

import com.hits.graphic_editor.databinding.ActivityNewProjectBinding
import com.hits.graphic_editor.databinding.TopMenuBinding
import kotlin.math.min

class Filter {

    fun showBottomMenu (binding: ActivityNewProjectBinding) {



        /*val topMenu: TopMenuBinding by lazy {
            TopMenuBinding.inflate()
        }*/
    }

    fun SimpleImage.inverse() {

        for (i in 0 until this.height) {
            for (j in 0 until this.width) {
                this[i, j] = argbToInt(
                    this[i, j].alpha(),
                    255 - this[i, j].red(),
                    255 - this[i, j].green(),
                    255 - this[i, j].blue()
                )
            }
        }
    }

    fun SimpleImage.grayscale() {

        for (i in 0 until this.height) {
            for (j in 0 until this.width) {

                val averageValue =
                    (this[i, j].red() + this[i, j].green() + this[i, j].blue()) / 3

                this[i, j] = argbToInt(
                    this[i, j].alpha(),
                    averageValue,
                    averageValue,
                    averageValue
                )
            }
        }
    }

    fun SimpleImage.blackAndWhite() {

        for (i in 0 until this.height) {
            for (j in 0 until this.width) {

                val averageValue =
                    (this[i, j].red() + this[i, j].green() + this[i, j].blue()) / 3

                val value: Int
                if (averageValue > 128) value = 255
                else value = 0

                this[i, j] = argbToInt(
                    this[i, j].alpha(),
                    value,
                    value,
                    value
                )
            }
        }
    }

    fun SimpleImage.sepia() {

        for (i in 0 until this.height) {
            for (j in 0 until this.width) {

                val red =
                    min(255, (this[i, j].red() * 0.393 +
                            this[i, j].green() * 0.769 +
                            this[i, j].blue() * 0.189).toInt())

                val green =
                    min(255, (this[i, j].red() * 0.349 +
                            this[i, j].green() * 0.686 +
                            this[i, j].blue() * 0.168).toInt())

                val blue =
                    min(255, (this[i, j].red() * 0.272 +
                            this[i, j].green() * 0.534 +
                            this[i, j].blue() * 0.131).toInt())

                this[i, j] = argbToInt(
                    this[i, j].alpha(),
                    red,
                    green,
                    blue
                )
            }
        }
    }

    /*fun SimpleImage.contrast(contrastCoefficient: Float) {

        for (i in 0 until this.height) {
            for (j in 0 until this.width) {

                this.pixels[i * this.width + j] = argbToInt(
                    this.pixels[i * this.width + j].alpha(),
                    this.pixels[i * this.width + j].red() * contrastCoefficient,
                    this.pixels[i * this.width + j].green() * contrastCoefficient,
                    this.pixels[i * this.width + j].blue() * contrastCoefficient
                )
            }
        }
    }*/
}