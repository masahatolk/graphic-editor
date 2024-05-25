package com.hits.graphic_editor

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.github.dhaval2404.colorpicker.ColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.hits.graphic_editor.affine_transform.AffineTransform
import com.hits.graphic_editor.custom_api.getSimpleImage
import com.hits.graphic_editor.databinding.ActivityNewProjectBinding
import com.hits.graphic_editor.databinding.BottomMenuBinding
import com.hits.graphic_editor.databinding.ExtraTopMenuBinding
import com.hits.graphic_editor.databinding.TopMenuBinding
import com.hits.graphic_editor.rotation.Rotation
import com.hits.graphic_editor.scaling.Scaling
import com.hits.graphic_editor.color_correction.ColorCorrection
import com.hits.graphic_editor.cube_3d.Cube3D
import com.hits.graphic_editor.custom_api.getBitMap
import com.hits.graphic_editor.face_detection.FaceDetection
import com.hits.graphic_editor.retouch.Retouch
import com.hits.graphic_editor.spline.Spline
import com.hits.graphic_editor.ui.addBottomMenu
import com.hits.graphic_editor.ui.addExtraTopMenu
import com.hits.graphic_editor.ui.addTopMenu
import com.hits.graphic_editor.ui.removeBottomMenu
import com.hits.graphic_editor.ui.removeExtraTopMenu
import com.hits.graphic_editor.ui.removeTopMenu
import com.hits.graphic_editor.unsharp_mask.UnsharpMask
import com.hits.graphic_editor.utils.Filter
import com.hits.graphic_editor.utils.FilterMode
import com.hits.graphic_editor.utils.ProcessedImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.opencv.android.OpenCVLoader
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class NewProjectActivity : AppCompatActivity() {

    // ------------ UI elements ------------
    private val binding: ActivityNewProjectBinding by lazy {
        ActivityNewProjectBinding.inflate(layoutInflater)
    }
    val topMenu: TopMenuBinding by lazy {
        TopMenuBinding.inflate(layoutInflater)
    }
    val bottomMenu: BottomMenuBinding by lazy {
        BottomMenuBinding.inflate(layoutInflater)
    }
    val extraTopMenu: ExtraTopMenuBinding by lazy {
        ExtraTopMenuBinding.inflate(layoutInflater)
    }
    private lateinit var colorPicker: ColorPickerDialog.Builder

    @SuppressLint("ResourceType")
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ---------------- open cv init ----------------
        if (OpenCVLoader.initLocal()) {
            Log.i("TEST", "OpenCV loaded successfully")
        }
        else {
            Log.e("TEST", "OpenCV initialization failed!")
            (Toast.makeText(this, "OpenCV initialization failed!", Toast.LENGTH_LONG)).show()
            return
        }

        // ------------- color picker for splines -------------
        colorPicker = ColorPickerDialog
            .Builder(this)
            .setTitle("Pick Theme")
            .setColorShape(ColorShape.SQAURE)
            .setDefaultColor(Color.BLACK)


        setContentView(binding.root)

        // ------------ get photo from MainActivity ------------
        val photo = intent.getStringExtra("photo")
        val selectedPhotoUri = photo!!.toUri()
        val selectedPhotoBitmap: Bitmap = runBlocking {
            ImageDecoder.decodeBitmap(
                ImageDecoder.createSource(applicationContext.contentResolver, selectedPhotoUri)
            ) { decoder, _, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                decoder.isMutableRequired = true
            }
        }

        if (selectedPhotoBitmap.width * selectedPhotoBitmap.height
            !in ProcessedImage.MIN_SIZE..ProcessedImage.MAX_SIZE)
            this.finish()

        binding.imageView.setImageBitmap(selectedPhotoBitmap)
        binding.imageView.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))

        // ------------------- add main menus -------------------
        addTopMenu(binding, topMenu)
        addBottomMenu(binding, bottomMenu)

        // -------------- create necessary fields ---------------
        val processedImage = ProcessedImage(getSimpleImage(selectedPhotoBitmap), binding.imageView)
        var currentFilter:Filter = Scaling(binding, layoutInflater, processedImage)

        // -------------- add listeners to top menus ----------------
        topMenu.close.setOnClickListener() {
            this.finish()
        }

        topMenu.undo.setOnClickListener() {
            processedImage.undoAndSetImageToView()
        }

        topMenu.redo.setOnClickListener() {
            processedImage.redoAndSetImageToView()
        }

        topMenu.download.setOnClickListener() {
            CoroutineScope(Dispatchers.IO).launch {
                val bitmap = getBitMap(processedImage.getSimpleImage())
                val fileName = "${System.currentTimeMillis()}" + ".png"
                var fos: OutputStream? = null

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    this@NewProjectActivity.contentResolver?.also { resolver ->

                        val contentValues = ContentValues().apply {

                            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                        }

                        val imageUri: Uri? =
                            resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                        fos = imageUri?.let { resolver.openOutputStream(it) }
                    }
                } else {
                    val root = Environment.getExternalStorageDirectory()
                    val directory = File("$root/DemoApps")
                    if (!directory.exists()) {
                        directory.mkdirs()
                    }
                    val file = File(directory, fileName)
                    fos = FileOutputStream(file)
                }
                fos?.use {
                    this@NewProjectActivity.runOnUiThread {
                        Toast.makeText(this@NewProjectActivity, "Saving image to the gallery...", Toast.LENGTH_SHORT).show()
                    }
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                    this@NewProjectActivity.runOnUiThread {
                        Toast.makeText(this@NewProjectActivity, "Image saved to the gallery!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        topMenu.share.setOnClickListener() {
            val bitmap = (binding.imageView.drawable as BitmapDrawable).bitmap
            val path =
                MediaStore.Images.Media.insertImage(this.contentResolver, bitmap, "Image", null)
            val uri: Uri = Uri.parse(path)

            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "image/png"
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            this.startActivity(Intent.createChooser(intent, "Share using"))
        }

        extraTopMenu.close.setOnClickListener {

            removeExtraTopMenu(binding, extraTopMenu)
            currentFilter.onClose(false)

            processedImage.switchStackMode(false)
            processedImage.setImageToView()

            addTopMenu(binding, topMenu)
            addBottomMenu(binding, bottomMenu)
        }

        extraTopMenu.save.setOnClickListener {

            removeExtraTopMenu(binding, extraTopMenu)
            currentFilter.onClose(true)
            //...
            processedImage.switchStackMode(true)
            processedImage.setImageToView()

            addTopMenu(binding, topMenu)
            addBottomMenu(binding, bottomMenu)
        }

        // ------------ add listener to bottom menu -------------
        fun startFilter(){
            when (bottomMenu.root.selectedTabPosition) {
                FilterMode.SCALING.ordinal -> {
                    currentFilter = Scaling(binding, layoutInflater, processedImage)
                }

                FilterMode.ROTATION.ordinal -> {
                    currentFilter = Rotation(binding, layoutInflater, processedImage)
                }

                FilterMode.COLOR_CORRECTION.ordinal -> {
                    currentFilter = ColorCorrection(binding, layoutInflater, processedImage, FaceDetection(this@NewProjectActivity, binding, layoutInflater, processedImage))
                }

                FilterMode.RETOUCH.ordinal -> {
                    currentFilter = Retouch(binding, layoutInflater, processedImage)
                }

                FilterMode.SPLINE.ordinal -> {
                    currentFilter = Spline(binding, layoutInflater, processedImage, colorPicker)
                }

                FilterMode.AFFINE_TRANSFORMATION.ordinal -> {
                    currentFilter = AffineTransform(this@NewProjectActivity, binding, layoutInflater, processedImage)
                }

                FilterMode.UNSHARP_MASKING.ordinal -> {
                    currentFilter = UnsharpMask(binding, layoutInflater, processedImage)
                }

                FilterMode.CUBE.ordinal -> {
                    currentFilter = Cube3D(binding, layoutInflater, processedImage, this@NewProjectActivity)
                }
            }
        }
        bottomMenu.root.addOnTabSelectedListener(object : OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab) {

                removeTopMenu(binding, topMenu)
                removeBottomMenu(binding, bottomMenu)
                addExtraTopMenu(binding, extraTopMenu)

                startFilter()
                processedImage.switchStackMode()
                currentFilter.onStart()
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {
                removeTopMenu(binding, topMenu)
                removeBottomMenu(binding, bottomMenu)
                addExtraTopMenu(binding, extraTopMenu)

                startFilter()
                processedImage.switchStackMode()
                currentFilter.onStart()
            }
        })

        supportActionBar?.hide()
    }
}
