package com.hits.graphic_editor.face_detection

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import com.hits.graphic_editor.custom_api.SimpleImage
import com.hits.graphic_editor.custom_api.getBitMap
import com.hits.graphic_editor.databinding.ActivityNewProjectBinding
import com.hits.graphic_editor.databinding.FaceDetectionBottomMenuBinding
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
    private val binding: ActivityNewProjectBinding,
    private val layoutInflater: LayoutInflater
) {

    val faceDetectionBottomMenu: FaceDetectionBottomMenuBinding by lazy {
        FaceDetectionBottomMenuBinding.inflate(layoutInflater)
    }

    lateinit var simpleImage: SimpleImage
    private lateinit var matrix: Mat
    private lateinit var image: Bitmap


    fun showBottomMenu() {

        addFaceDetectionBottomMenu(binding, faceDetectionBottomMenu)

        image = getBitMap(simpleImage)

        faceDetectionBottomMenu.faceDetectionButton.setOnClickListener {
            try {
                matrix = Mat()
                Utils.bitmapToMat(image, matrix)
                detectFaces(matrix)
            } catch (e: Exception) {
                Log.d("TEST", "showBottomMenu: $e")
            }
        }
    }

    private fun detectFaces(mat: Mat): Mat {

        val matrix: Mat = mat.clone()
        val filePath =  getFileFromAssets(context, "haarcascade_frontalface_alt2.xml").absolutePath
        val cascadeClassifier = CascadeClassifier(filePath)

        val faces = MatOfRect()
        cascadeClassifier.detectMultiScale(matrix, faces)

        for(face in faces.toArray()){
            Imgproc.rectangle(matrix,
                Point(face.x.toDouble(), face.y.toDouble()),
                Point((face.x + face.width).toDouble(), (face.y + face.height).toDouble()),
                Scalar(0.0, 0.0, 255.0), 3
            )
        }

        val finalMatrix: Mat = matrix.clone()
        val bitmap: Bitmap = Bitmap.createBitmap(finalMatrix.cols(), finalMatrix.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(finalMatrix, bitmap)
        binding.imageView.setImageBitmap(bitmap)

        return matrix
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
}