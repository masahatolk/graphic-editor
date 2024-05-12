package com.hits.graphic_editor

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hits.graphic_editor.custom_api.IntColor
import com.hits.graphic_editor.custom_api.MipMapsContainer
import com.hits.graphic_editor.custom_api.SimpleImage
import com.hits.graphic_editor.custom_api.argbToInt
import com.hits.graphic_editor.custom_api.getBitMap
import com.hits.graphic_editor.custom_api.getSimpleImage
import com.hits.graphic_editor.databinding.ActivityMainBinding
import com.hits.graphic_editor.utils.FVec2
import com.hits.graphic_editor.utils.FVec3
import com.hits.graphic_editor.utils.Vec2
import com.hits.graphic_editor.utils.getAngle
import com.hits.graphic_editor.utils.getBilinearFilteredPixelInt
import com.hits.graphic_editor.utils.getRotatedFVec3
import com.hits.graphic_editor.utils.getRotationMatrix
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.roundToInt
data class UV(var u:Float,var v:Float) {
    fun swap(other: UV) {
        val tempU = this.u
        val tempV = this.v
        this.u = other.u
        this.v = other.v
        other.u = tempU
        other.v = tempV
    }
}
data class TextureUVs(
    val p1: UV,
    val p2: UV,
    val p3: UV
)
fun calcTrianglePerpendicular(p1: FVec3, p2: FVec3, p3: FVec3):FVec3 {
    return (p1-p3).crossProduct(p1-p2)
}
data class Triangle(
    var p1: FVec3,
    var p2: FVec3,
    var p3: FVec3,
    var textureUVs: TextureUVs,
    var crossProductFVec3: FVec3)
{
    constructor(p1: FVec3, p2: FVec3, p3: FVec3, uvs: TextureUVs) : this(
        p1 = p1, p2 = p2, p3 = p3,
        textureUVs = uvs,
        crossProductFVec3 = (p1-p3).crossProduct(p1-p2)
    )
    fun calcCrossProductFVec3() {
        crossProductFVec3 = (p1-p3).crossProduct(p1-p2)
    }
    fun getMedian():FVec3 {
        return FVec3(
            (p1.x + p2.x + p3.x) / 3,
            (p1.y + p2.y + p3.y) / 3,
            (p1.z + p2.z + p3.z) / 3)
    }
}
fun interpolate (x1: Int, f1: Int, x2: Int, f2: Int): MutableList<Int>
{
    if (x1 == x2) {
        return mutableListOf(f1)
    }
    val values = mutableListOf<Int>()
    val step = (f2 - f1) / (x2 - x1).toFloat()
    var f = f1.toFloat()
    for (i in x1..x2) {
        values.add(f.toInt())
        f += step
    }
    return values
}
fun interpolate (x1: Int, f1: Float, x2: Int, f2: Float): MutableList<Float>
{
    if (x1 == x2) {
        return mutableListOf(f1)
    }
    val values = mutableListOf<Float>()
    val step = (f2 - f1) / (x2 - x1).toFloat()
    var f = f1
    for (i in x1..x2) {
        values.add(f)
        f += step
    }
    return values
}
fun SimpleImage.drawLine(firstPoint: Vec2, secondPoint: Vec2, color: IntColor) {
    val p1 = firstPoint.copy()
    val p2 = secondPoint.copy()
    if (abs(p2.x - p1.x) > abs(p2.y - p1.y)) {
        if (p1.x > p2.x) {
            p1.swap(p2)
        }
        val ys = interpolate(p1.x, p1.y, p2.x, p2.y)
        for (x in p1.x..p2.x) {
            this[x, ys[x - p1.x]] = color
        }
    } else {
        if (p1.y > p2.y) {
            p1.swap(p2)
        }
        val xs = interpolate(p1.y, p1.x, p2.y, p2.x)
        for (y in p1.y..p2.y) {
            this[xs[y - p1.y], y] = color
        }
    }
}
fun SimpleImage.drawFilledTriangle (p1: Vec2, p2: Vec2, p3: Vec2, color: IntColor)
{
    // Сортировка точек так, что p0.y <= p1.y <= y2
    if (p2.y < p1.y) p2.swap(p1)
    if (p3.y < p1.y) p3.swap(p1)
    if (p3.y < p2.y) p3.swap(p2)

    // Вычисление координат x рёбер треугольника
    val x12 = interpolate(p1.y, p1.x, p2.y, p2.x)
    val x23 = interpolate(p2.y, p2.x, p3.y, p3.x)
    val x13 = interpolate(p1.y, p1.x, p3.y, p3.x)

    // Конкатенация коротких сторон
    x12.removeLast()
    val x123 = (x12 + x23).toMutableList()

    val xLeft: MutableList<Int>
    val xRight: MutableList<Int>

    // Определяем, какая из сторон левая и правая
    val m = x123.size / 2
    if (x13[m] < x123[m]) {
        xLeft = x13
        xRight = x123
    } else {
        xLeft = x123
        xRight = x13
    }

    // Отрисовка горизонтальных отрезков
    for (y in p1.y..p3.y) {
        for (x in xLeft[y - p1.y]..xRight[y - p1.y]) {
            if (x in 0..<this.width && y in 0..<this.height)
                this[x, y] = color
        }
    }
}
fun SimpleImage.drawTriangle (
    triangle: Triangle,
    transformedP1: FVec3, transformedP2: FVec3, transformedP3: FVec3,
    p1: Vec2, p2: Vec2, p3: Vec2,
    texture: SimpleImage)
{
    val p1UVs = triangle.textureUVs.p1.copy()
    val p2UVs = triangle.textureUVs.p2.copy()
    val p3UVs = triangle.textureUVs.p3.copy()

    val transfP1 = transformedP1.copy()
    val transfP2 = transformedP2.copy()
    val transfP3 = transformedP3.copy()

    // Sorting that p0.y <= p1.y <= y2
    if (p2.y < p1.y) {p2.swap(p1); p2UVs.swap(p1UVs); transfP2.swap(transfP1)}
    if (p3.y < p1.y) {p3.swap(p1); p3UVs.swap(p1UVs); transfP3.swap(transfP1)}
    if (p3.y < p2.y) {p3.swap(p2); p3UVs.swap(p2UVs); transfP3.swap(transfP2)}

    // X coordinates of triangle edges
    val x12 = interpolate(p1.y, p1.x, p2.y, p2.x)
    val x23 = interpolate(p2.y, p2.x, p3.y, p3.x)
    val x13 = interpolate(p1.y, p1.x, p3.y, p3.x)

    val uDivZ12 = interpolate(p1.y, p1UVs.u/transfP1.z, p2.y, p2UVs.u/transfP2.z)
    val uDivZ23 = interpolate(p2.y, p2UVs.u/transfP2.z, p3.y, p3UVs.u/transfP3.z)
    val uDivZ13 = interpolate(p1.y, p1UVs.u/transfP1.z, p3.y, p3UVs.u/transfP3.z)

    val vDivZ12 = interpolate(p1.y, p1UVs.v/transfP1.z, p2.y, p2UVs.v/transfP2.z)
    val vDivZ23 = interpolate(p2.y, p2UVs.v/transfP2.z, p3.y, p3UVs.v/transfP3.z)
    val vDivZ13 = interpolate(p1.y, p1UVs.v/transfP1.z, p3.y, p3UVs.v/transfP3.z)

    val revZ12 = interpolate(p1.y, 1/transfP1.z, p2.y,1/transfP2.z)
    val revZ23 = interpolate(p2.y, 1/transfP2.z, p3.y, 1/transfP3.z)
    val revZ13 = interpolate(p1.y, 1/transfP1.z, p3.y, 1/transfP3.z)

    // Short edges concat
    x12.removeLast()
    vDivZ12.removeLast()
    uDivZ12.removeLast()
    revZ12.removeLast()

    val x123 = (x12 + x23).toMutableList()
    val u123 = (uDivZ12 + uDivZ23).toMutableList()
    val v123 = (vDivZ12 + vDivZ23).toMutableList()
    val revZ123 = (revZ12 + revZ23).toMutableList()

    val xLeft: MutableList<Int>
    val xRight: MutableList<Int>
    val uDivZLeft: MutableList<Float>
    val uDivZRight: MutableList<Float>
    val vDivZLeft: MutableList<Float>
    val vDivZRight: MutableList<Float>
    val revZLeft: MutableList<Float>
    val revZRight: MutableList<Float>

    // Deduce which side is left and which is right
    val m = x123.size / 2
    if (x13[m] < x123[m]) {
        xLeft = x13
        xRight = x123
        uDivZLeft = uDivZ13
        uDivZRight = u123
        vDivZLeft = vDivZ13
        vDivZRight = v123
        revZLeft = revZ13
        revZRight = revZ123
    } else {
        xLeft = x123
        xRight = x13
        uDivZLeft = u123
        uDivZRight = uDivZ13
        vDivZLeft = v123
        vDivZRight = vDivZ13
        revZLeft = revZ123
        revZRight = revZ13
    }

    // Horizontal segments drawing
    for (y in p1.y..p3.y) {
        val leftBound = xLeft[y - p1.y]
        val rightBound = xRight[y - p1.y]

        val uDivZSegment = interpolate(leftBound, uDivZLeft[y - p1.y], rightBound, uDivZRight[y - p1.y])
        val vDivZSegment = interpolate(leftBound, vDivZLeft[y - p1.y], rightBound, vDivZRight[y - p1.y])
        val revZSegment = interpolate(leftBound, revZLeft[y - p1.y], rightBound, revZRight[y - p1.y])

        for (x in leftBound..rightBound) {
            if (x in 0..<this.width && y in 0..<this.height)
                this[x, y] = getBilinearFilteredPixelInt(texture,
                    uDivZSegment[x - leftBound] / revZSegment[x - leftBound],
                    vDivZSegment[x - leftBound] / revZSegment[x - leftBound])
        }
    }
}
fun SimpleImage.drawWireframeTriangle (p1: Vec2, p2: Vec2, p3: Vec2, color: IntColor) {
    this.drawLine(p1, p2, color)
    this.drawLine(p2, p3, color)
    this.drawLine(p3, p1, color)
}
class Scene(
    var canvas: SimpleImage,
    var sceneObj: SceneObject,
    var camera: Camera
)
{
    fun renderFrame() {
        canvas.pixels.fill(0)
        renderObject(sceneObj)
    }
    private fun renderObject(obj: SceneObject)
    {
        obj.triangles.forEach {
            val fVec3ToCamera = camera.position - getRotatedFVec3(it.p1, obj.cachedRotationMatrix) - obj.position
            val transformedP1 = getRotatedFVec3(it.p1, obj.cachedRotationMatrix) + obj.position
            val transformedP2 = getRotatedFVec3(it.p2, obj.cachedRotationMatrix) + obj.position
            val transformedP3 = getRotatedFVec3(it.p3, obj.cachedRotationMatrix) + obj.position

            if (getAngle(
                    getRotatedFVec3(it.crossProductFVec3, obj.cachedRotationMatrix),
                    //calcTrianglePerpendicular(transformedP1, transformedP2, transformedP3),
                    fVec3ToCamera) < PI/2)
            {
                val projectedP1 = projectedFVec3(transformedP1)
                val projectedP2 = projectedFVec3(transformedP2)
                val projectedP3 = projectedFVec3(transformedP3)

                canvas.drawTriangle(
                    it,
                    transformedP1,
                    transformedP2,
                    transformedP3,
                    projectedP1,
                    projectedP2,
                    projectedP3,
                    obj.texture
                )
            }
        }
    }
    private fun viewportToCanvas(p: FVec2): Vec2{
        return Vec2(
            (p.x * canvas.width / camera.viewportWidth).roundToInt() + canvas.width / 2,
            (p.y * canvas.height / camera.viewportHeight).roundToInt() + canvas.height / 2)
    }
    private fun projectedFVec3(vec: FVec3): Vec2 {
        return viewportToCanvas(
            FVec2(vec.x * camera.distToCanvas / vec.z, vec.y * camera.distToCanvas / vec.z))
    }
}
data class Camera(
    var position: FVec3,
    var distToCanvas: Float = 1F,
    var viewportHeight:Float = 1F,
    var viewportWidth:Float = 1F
)
interface SceneObject{
    var position: FVec3
    var rotation: FVec3
    var cachedRotationMatrix: Array<Array<Float>>
    val vertexes: Array<FVec3>
    val triangles: Array<Triangle>
    var texture: SimpleImage
}
class Cube(
    override var texture: SimpleImage
) : SceneObject
{
    override var position = FVec3(0F, 0F, 4F)
    override var rotation: FVec3 = FVec3(0F, 0F, 0F)
        set(vec){
            field = vec
            cachedRotationMatrix = getRotationMatrix(vec)
        }
    override var cachedRotationMatrix = getRotationMatrix(rotation)

    override val vertexes: Array<FVec3> = arrayOf(
        FVec3(-1F, -1F, -1F),
        FVec3(-1F, 1F, -1F),
        FVec3(1F, 1F, -1F),
        FVec3(1F, -1F, -1F),
        FVec3(1F, -1F, 1F),
        FVec3(-1F, -1F, 1F),
        FVec3(-1F, 1F, 1F),
        FVec3(1F, 1F, 1F)
    )
    override val triangles: Array<Triangle> = arrayOf(
        Triangle(vertexes[4], vertexes[7], vertexes[2], TextureUVs(
            UV(1F,0F), UV(1F,1F), UV(0F,1F))),
        Triangle(vertexes[2], vertexes[3], vertexes[4], TextureUVs(
            UV(0F,1F), UV(0F,0F), UV(1F,0F))),
        Triangle(vertexes[7], vertexes[6], vertexes[1], TextureUVs(
            UV(1F,0F), UV(1F,1F), UV(0F,1F))),
        Triangle(vertexes[1], vertexes[2], vertexes[7], TextureUVs(
            UV(0F,1F), UV(0F,0F), UV(1F,0F))),
        Triangle(vertexes[6], vertexes[7], vertexes[4], TextureUVs(
            UV(1F,0F), UV(1F,1F), UV(0F,1F))),
        Triangle(vertexes[4], vertexes[5], vertexes[6], TextureUVs(
            UV(0F,1F), UV(0F,0F), UV(1F,0F))),

        Triangle(vertexes[5], vertexes[4], vertexes[3], TextureUVs(
            UV(1F,0F), UV(1F,1F), UV(0F,1F))),
        Triangle(vertexes[3], vertexes[0], vertexes[5], TextureUVs(
            UV(0F,1F), UV(0F,0F), UV(1F,0F))),
        Triangle(vertexes[6], vertexes[5], vertexes[0], TextureUVs(
            UV(1F,0F), UV(1F,1F), UV(0F,1F))),
        Triangle(vertexes[0], vertexes[1], vertexes[6], TextureUVs(
            UV(0F,1F), UV(0F,0F), UV(1F,0F))),
        Triangle(vertexes[3], vertexes[2], vertexes[1], TextureUVs(
            UV(1F,0F), UV(1F,1F), UV(0F,1F))),
        Triangle(vertexes[1], vertexes[0], vertexes[3], TextureUVs(
            UV(0F,1F), UV(0F,0F), UV(1F,0F))),
    )
}
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

            val drawable = binding.image1.drawable as BitmapDrawable
            val bitmap = drawable.bitmap.copy(Bitmap.Config.ARGB_8888, true)
            val img = getSimpleImage(bitmap)
            //val mipmaps = MipMapsContainer(img)

            /* ---------------------------------- */
            val scene = Scene(
                SimpleImage(600, 600),
                Cube(img),
                Camera(
                    FVec3(0F,0F,0F)
                )
            )
        binding.btn.setOnClickListener {
                scene.renderFrame()
                binding.image.setImageBitmap(getBitMap(scene.canvas))
                scene.sceneObj.rotation += FVec3(0.1F, 0.1F, 0.1F)
            /* ---------------------------------- */
        }
    }
}