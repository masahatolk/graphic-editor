package com.hits.graphic_editor

import com.hits.graphic_editor.custom_api.IntColor
import com.hits.graphic_editor.custom_api.SimpleImage
import com.hits.graphic_editor.utils.FVec2
import com.hits.graphic_editor.utils.FVec3
import com.hits.graphic_editor.utils.Vec2
import com.hits.graphic_editor.utils.getAngle
import com.hits.graphic_editor.utils.getBilinearFilteredPixelInt
import com.hits.graphic_editor.utils.getRotatedFVec3
import com.hits.graphic_editor.utils.getRotationMatrix
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.tan

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
fun interpolate (x1: Int, f1: Int, x2: Int, f2: Int): Deferred<MutableList<Int>>
{
    return CoroutineScope(Dispatchers.Default).async {
        if (x1 == x2) {
            return@async mutableListOf(f1)
        }
        val values = mutableListOf<Int>()
        val step = (f2 - f1) / (x2 - x1).toFloat()
        var f = f1.toFloat()
        for (i in x1..x2) {
            values.add(f.toInt())
            f += step
        }
        return@async values
    }
}
fun interpolate (x1: Int, f1: Float, x2: Int, f2: Float): Deferred<MutableList<Float>>
{
    return CoroutineScope(Dispatchers.Default).async {
        if (x1 == x2) {
            return@async mutableListOf(f1)
        }
        val values = mutableListOf<Float>()
        val step = (f2 - f1) / (x2 - x1).toFloat()
        var f = f1
        for (i in x1..x2) {
            values.add(f)
            f += step
        }
        return@async values
    }
}
suspend
fun SimpleImage.drawLine(firstPoint: Vec2, secondPoint: Vec2, color: IntColor) {
    val p1 = firstPoint.copy()
    val p2 = secondPoint.copy()
    if (abs(p2.x - p1.x) > abs(p2.y - p1.y)) {
        if (p1.x > p2.x) {
            p1.swap(p2)
        }
        val ys = interpolate(p1.x, p1.y, p2.x, p2.y).await()
        for (x in p1.x..p2.x) {
            this[x, ys[x - p1.x]] = color
        }
    } else {
        if (p1.y > p2.y) {
            p1.swap(p2)
        }
        val xs = interpolate(p1.y, p1.x, p2.y, p2.x).await()
        for (y in p1.y..p2.y) {
            this[xs[y - p1.y], y] = color
        }
    }
}
suspend
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
    val _x12 = interpolate(p1.y, p1.x, p2.y, p2.x)
    val _x23 = interpolate(p2.y, p2.x, p3.y, p3.x)
    val _x13 = interpolate(p1.y, p1.x, p3.y, p3.x)

    val _uDivZ12 = interpolate(p1.y, p1UVs.u/transfP1.z, p2.y, p2UVs.u/transfP2.z)
    val _uDivZ23 = interpolate(p2.y, p2UVs.u/transfP2.z, p3.y, p3UVs.u/transfP3.z)
    val _uDivZ13 = interpolate(p1.y, p1UVs.u/transfP1.z, p3.y, p3UVs.u/transfP3.z)

    val _vDivZ12 = interpolate(p1.y, p1UVs.v/transfP1.z, p2.y, p2UVs.v/transfP2.z)
    val _vDivZ23 = interpolate(p2.y, p2UVs.v/transfP2.z, p3.y, p3UVs.v/transfP3.z)
    val _vDivZ13 = interpolate(p1.y, p1UVs.v/transfP1.z, p3.y, p3UVs.v/transfP3.z)

    val _revZ12 = interpolate(p1.y, 1/transfP1.z, p2.y,1/transfP2.z)
    val _revZ23 = interpolate(p2.y, 1/transfP2.z, p3.y, 1/transfP3.z)
    val _revZ13 = interpolate(p1.y, 1/transfP1.z, p3.y, 1/transfP3.z)

    val x12 = _x12.await()
    val x23 = _x23.await()
    val x13 = _x13.await()
    val uDivZ12 = _uDivZ12.await()
    val uDivZ23 = _uDivZ23.await()
    val uDivZ13 = _uDivZ13.await()
    val vDivZ12 = _vDivZ12.await()
    val vDivZ23 = _vDivZ23.await()
    val vDivZ13 = _vDivZ13.await()
    val revZ12 = _revZ12.await()
    val revZ23 = _revZ23.await()
    val revZ13 = _revZ13.await()

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

    val jobs: MutableList<Job> = mutableListOf()
    // Horizontal segments drawing
    for (y in p1.y..p3.y) {
        jobs.add(CoroutineScope(Dispatchers.Default).launch {
            val leftBound = xLeft[y - p1.y]
            val rightBound = xRight[y - p1.y]

            val _uDivZSegment =
                interpolate(leftBound, uDivZLeft[y - p1.y], rightBound, uDivZRight[y - p1.y])
            val _vDivZSegment =
                interpolate(leftBound, vDivZLeft[y - p1.y], rightBound, vDivZRight[y - p1.y])
            val _revZSegment =
                interpolate(leftBound, revZLeft[y - p1.y], rightBound, revZRight[y - p1.y])

            val uDivZSegment = _uDivZSegment.await()
            val vDivZSegment = _vDivZSegment.await()
            val revZSegment = _revZSegment.await()

            for (x in leftBound..rightBound) {
                if (x in 0..<width && y in 0..<height)
                    this@drawTriangle[x, y] = getBilinearFilteredPixelInt(
                        texture,
                        uDivZSegment[x - leftBound] / revZSegment[x - leftBound],
                        vDivZSegment[x - leftBound] / revZSegment[x - leftBound]
                    )
            }
        })
    }
    jobs.forEach { it.join() }
}
class Scene(
    var canvas: SimpleImage,
    var sceneObj: SceneObject,
    var camera: Camera
)
{
    fun renderFrame() {
        canvas.pixels.fill(0)
        runBlocking {
            renderObject(sceneObj)
        }
    }
    private suspend fun renderObject(obj: SceneObject)
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
    var position: FVec3 = FVec3(0F,0F,0F),
    var distToCanvas: Float = 1F,
    var viewportHeight:Float = 1F,
    var viewportWidth:Float = 1F
)
{
    fun setFOV(newFOV: Int){
        distToCanvas = viewportWidth / (2 * tan(Math.toRadians(newFOV/2.0))).toFloat()
    }
}
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
fun Scene.validateObjectDistance()
{
    this.sceneObj.position.z =
        (this.sceneObj.position.z).coerceAtLeast(1.42F + this.camera.distToCanvas)
}
fun Scene.changeObjectDistance(delta: Float) {
    this.sceneObj.position.z += delta
    this.validateObjectDistance()
}
fun Scene.setCameraFOV(newFOV: Int) {
    this.camera.setFOV(newFOV)
    this.validateObjectDistance()
}
fun Scene.rotateObject(deltaRotation: FVec3) {
    this.sceneObj.rotation += deltaRotation
}
fun Scene.setResolution(pixels: Int) {
    this.canvas = SimpleImage(pixels, pixels)
}