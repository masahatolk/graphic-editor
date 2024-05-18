package com.hits.graphic_editor.custom_api

import androidx.core.graphics.alpha
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

typealias IntColor = Int
fun IntColor.alpha(): IntColor = this shr 24 and 0xff
fun IntColor.red(): IntColor = this shr 16 and 0xff
fun IntColor.green(): IntColor = this shr 8 and 0xff
fun IntColor.blue(): IntColor = this and 0xff

fun IntColor.multiplyIntColorByInt(c: Float): IntColor =
    argbToInt(this.alpha() * c, this.red() * c,this.green() * c,this.blue() * c)
fun IntColor.divideIntColorByInt(d: Float): IntColor =
    argbToInt(this.alpha() / d, this.red() / d,this.green() / d,this.blue() / d)
fun IntColor.addIntColorToIntColor(other: Int): IntColor =
    argbToInt(this.alpha() + other.alpha(), this.red() + other.red(),
        this.green() + other.green(),this.blue() + other.blue())
fun getTruncatedChannel(channel: Int):Int =
    max(0, min(255, channel))
fun getTruncatedChannel(channel: Float):Int =
    max(0, min(255, channel.roundToInt()))
fun argbToInt(alpha: Int, red: Int, green: Int, blue: Int) =
    (alpha shl 24) or (red shl 16) or (green shl 8) or blue
fun argbToInt(alpha: Float, red: Float, green: Float, blue: Float) =
    (alpha.roundToInt() shl 24) or (red.roundToInt() shl 16) or (green.roundToInt() shl 8) or blue.roundToInt()
fun blendedIntColor(first: IntColor, second: IntColor, coeff: Float): IntColor
{
    return argbToInt(
        first.alpha() * coeff + second.alpha() * (1 - coeff),
        first.red() * coeff + second.red() * (1 - coeff),
        first.green()* coeff + second.green() * (1 - coeff),
        first.blue()* coeff + second.blue() * (1 - coeff)
    )
}