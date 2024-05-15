package com.hits.graphic_editor.face_detection

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import com.hits.graphic_editor.databinding.ActivityNewProjectBinding
import com.hits.graphic_editor.databinding.FaceDetectionBottomMenuBinding
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier

class FaceDetection(
    private val context: Context,
    private val binding: ActivityNewProjectBinding,
    private val layoutInflater: LayoutInflater,
    private val image: Bitmap
) {

    val faceDetectionBottomMenu: FaceDetectionBottomMenuBinding by lazy {
        FaceDetectionBottomMenuBinding.inflate(layoutInflater)
    }

    var matrix: Mat = Mat()


    fun showBottomMenu() {

        addFaceDetectionBottomMenu(binding, faceDetectionBottomMenu)

        faceDetectionBottomMenu.faceDetectionButton.setOnClickListener {
            Utils.bitmapToMat(image, matrix)
            detectFaces(matrix)
        }
    }

    fun detectFaces(matrix: Mat) : Mat {
        Imgproc.cvtColor(matrix, matrix, Imgproc.COLOR_RGB2BGRA)

        var cascadeClassifier = CascadeClassifier()

        try {
            //val path = context.filesDir.absolutePath
            //val file = File("$path/")


            /*val object: InputStream = newProjectActivity.getResources()
                .openRawResource(R.raw.haarcascade_frontalface_alt2)*/

            //cascadeClassifier = CascadeClassifier()
        }
        catch (e: Exception) {

        }

        return matrix
    }
}