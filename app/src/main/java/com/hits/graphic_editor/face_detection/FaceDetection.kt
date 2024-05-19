package com.hits.graphic_editor.face_detection

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import com.hits.graphic_editor.custom_api.SimpleImage
import com.hits.graphic_editor.custom_api.getBitMap
import com.hits.graphic_editor.databinding.ActivityNewProjectBinding
import com.hits.graphic_editor.databinding.FaceDetectionBottomMenuBinding
import com.hits.graphic_editor.utils.Filter
import com.hits.graphic_editor.utils.ProcessedImage
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfRect
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier
import java.io.File


class FaceDetection(
    private val context: Context,
    override val binding: ActivityNewProjectBinding,
    override val layoutInflater: LayoutInflater,
    override val processedImage: ProcessedImage
): Filter {

    val faceDetectionBottomMenu: FaceDetectionBottomMenuBinding by lazy {
        FaceDetectionBottomMenuBinding.inflate(layoutInflater)
    }

    private lateinit var matrix: Mat
    private lateinit var image: Bitmap
    var isDetectionApplied = false


    fun showBottomMenu(simpleImage: SimpleImage) {
        addFaceDetectionBottomMenu(binding, faceDetectionBottomMenu)
        if(isDetectionApplied) binding.imageView.setImageBitmap(getDetection(simpleImage))
        else binding.imageView.setImageBitmap(getBitMap(simpleImage))

        faceDetectionBottomMenu.faceDetectionChip.setOnClickListener {
            isDetectionApplied = !isDetectionApplied
            if(isDetectionApplied) binding.imageView.setImageBitmap(getDetection(simpleImage))
            else binding.imageView.setImageBitmap(getBitMap(simpleImage))
        }
    }

    fun getMatrix(simpleImage: SimpleImage) : Mat{

        image = getBitMap(simpleImage)

        matrix = Mat()
        Utils.bitmapToMat(image, matrix)

        return matrix
    }

    fun getDetection(simpleImage: SimpleImage) : Bitmap {
        matrix = getMatrix(simpleImage)

        val faces: MatOfRect = detectFaces(matrix)

        for(face in faces.toArray()){
            Imgproc.rectangle(matrix,
                Point(face.x.toDouble(), face.y.toDouble()),
                Point((face.x + face.width).toDouble(), (face.y + face.height).toDouble()),
                Scalar(0.0, 0.0, 255.0), 2
            )
        }

        val finalMatrix: Mat = matrix.clone()
        val bitmap: Bitmap = Bitmap.createBitmap(finalMatrix.cols(), finalMatrix.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(finalMatrix, bitmap)

        return bitmap
    }

    fun detectFaces(mat: Mat): MatOfRect {

        val matrix: Mat = mat.clone()
        val filePath =  getFileFromAssets(context, "haarcascade_frontalface_alt2.xml").absolutePath
        val cascadeClassifier = CascadeClassifier(filePath)

        val faces = MatOfRect()
        cascadeClassifier.detectMultiScale(matrix, faces)

        return faces
    }

    private fun getFileFromAssets(context: Context, fileName: String): File = File(context.cacheDir, fileName)
        .also {
            if (!it.exists()) {
                it.outputStream().use { cache ->
                    context.assets.open(fileName).use { inputStream ->
                        inputStream.copyTo(cache)
                    }
                }
            }
        }

    override fun showBottomMenu() {
        TODO("Not yet implemented")
    }

    override fun removeAllMenus() {
        removeFaceDetectionBottomMenu(binding, this.faceDetectionBottomMenu)
    }
}