package com.hits.graphic_editor.utils

import kotlin.math.acos
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

data class FVec3(var x: Float, var y: Float, var z: Float)
{
    fun crossProduct (another: FVec3): FVec3 {
        return FVec3(
            this.y * another.z - this.z * another.y,
            this.z * another.x - this.x * another.z,
            this.x * another.y - this.y * another.x)
    }
    fun dotProduct (another: FVec3): Float {
        return this.x * another.x + this.y * another.y + this.z * another.z
    }
    fun length(): Float{
        return sqrt(this.x * this.x + this.y * this.y + this.z * this.z)
    }
    operator fun minus(another: FVec3) =
        FVec3(this.x - another.x, this.y - another.y, this.z - another.z)
    operator fun plus(another: FVec3) =
        FVec3(this.x + another.x, this.y + another.y, this.z + another.z)
    fun rotate(matrix: Array<Array<Float>>): FVec3
    {
        this.x = x * matrix[0][0] + y * matrix[0][1] + z * matrix[0][2]
        this.y = x * matrix[1][0] + y * matrix[1][1] + z * matrix[1][2]
        this.z = x * matrix[2][0] + y * matrix[2][1] + z * matrix[2][2]
        return this
    }
    fun rotateOnAngle(xAngle: Float, yAngle: Float, zAngle: Float) {
        rotate(getRotationMatrix(xAngle, yAngle, zAngle))
    }
    fun swap(other: FVec3)
    {
        val tempX = this.x
        val tempY = this.y
        val tempZ = this.z
        this.x = other.x
        this.y = other.y
        this.z = other.z
        other.x = tempX
        other.y = tempY
        other.z = tempZ
    }
}
fun getAngle(v1: FVec3, v2:FVec3): Float
    = acos(v1.dotProduct(v2) / (v1.length() * v2.length()))
data class Vec2(var x: Int, var y: Int)
{
    fun swap(other: Vec2)
    {
        val tempX = this.x
        val tempY = this.y
        this.x = other.x
        this.y = other.y
        other.x = tempX
        other.y = tempY
    }
    operator fun minus(another: Vec2) =
        Vec2(this.x - another.x, this.y - another.y)
    operator fun plus(another: Vec2) =
        Vec2(this.x + another.x, this.y + another.y)
    operator fun div(divisor: Int) =
        Vec2(this.x / divisor, this.y / divisor)
    fun distTo(another: Vec2): Float =
        sqrt((this.x - another.x) * (this.x - another.x) +
                (this.y - another.y) * (this.y - another.y).toFloat())
}
data class FVec2(var x: Float, var y: Float)
{
    fun roundToVec2():Vec2{
        return Vec2(x.roundToInt(), y.roundToInt())
    }
    fun floorToVec2():Vec2{
        return Vec2(x.toInt(), y.toInt())
    }
    fun ceilToVec2():Vec2{
        return Vec2(ceil(x).toInt(), ceil(y).toInt())
    }
    fun length() = sqrt(x*x + y*y)
    fun normalize(){
        val oldLength = length()
        x /= oldLength
        y /= oldLength
    }
    fun resize(size: Float){
        this.normalize()
        this.x *= size
        this.y *= size
    }
    operator fun plus(another: FVec2) =
        FVec2(this.x + another.x, this.y + another.y)

    operator fun timesAssign(a: Float) {
        this.x *= a
        this.y *= a
    }
}
fun getRotationMatrix(vec: FVec3):Array<Array<Float>>
    =getRotationMatrix(vec.x, vec.y, vec.z)
fun getRotationMatrix(xAngle: Float, yAngle: Float, zAngle: Float):Array<Array<Float>>
{
    return arrayOf(
        arrayOf(cos(yAngle)*cos(zAngle), -sin(zAngle)*cos(yAngle), sin(yAngle)),
        arrayOf(sin(xAngle)* sin(yAngle)*cos(zAngle)+sin(zAngle)*cos(xAngle), -sin(xAngle)*sin(yAngle)*sin(zAngle)+cos(xAngle)*cos(zAngle), -sin(xAngle)*cos(yAngle)),
        arrayOf(sin(xAngle)*sin(zAngle)-sin(yAngle)*cos(xAngle)*cos(zAngle), sin(xAngle)*cos(zAngle)+sin(yAngle)*sin(zAngle)*cos(xAngle), cos(xAngle)*cos(yAngle))
    )
}
fun getRotatedFVec3(vec: FVec3, matrix: Array<Array<Float>>): FVec3
{
    return FVec3(
        vec.x * matrix[0][0] + vec.y * matrix[0][1] + vec.z * matrix[0][2],
        vec.x * matrix[1][0] + vec.y * matrix[1][1] + vec.z * matrix[1][2],
        vec.x * matrix[2][0] + vec.y * matrix[2][1] + vec.z * matrix[2][2]
    )
}
fun getRotatedFVec3(vec: FVec3, xAngle: Float, yAngle: Float, zAngle: Float): FVec3 {
    return getRotatedFVec3(vec, getRotationMatrix(xAngle, yAngle, zAngle))
}