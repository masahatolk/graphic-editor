package com.hits.graphic_editor.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.hits.graphic_editor.ChoiceFragment
import com.hits.graphic_editor.MainActivity
import com.hits.graphic_editor.NewProjectActivity
import com.hits.graphic_editor.R
import com.hits.graphic_editor.databinding.ActivityMainBinding
import com.hits.graphic_editor.databinding.BottomSheetBinding
import com.hits.graphic_editor.databinding.FragmentHomeBinding
import java.net.URI


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    var fab: View? = null

    private val binding get() = _binding!!
    var pickedPhoto: Uri? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        fab = binding.fab

        fab?.setOnClickListener {
            val view: View = layoutInflater.inflate(R.layout.bottom_sheet, null)

            (activity as MainActivity?)?.showBottomSheet(view)

            val binding: BottomSheetBinding by lazy {
                BottomSheetBinding.inflate(layoutInflater)
            }

            binding.gallery.setOnClickListener {
                getPhotoFromGallery()
            }

            binding.camera.setOnClickListener {
                getPhotoFromCamera()
            }
        }

        return binding.root
    }

    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            pickedPhoto = uri
            val photoIntent = Intent(activity as MainActivity, NewProjectActivity::class.java)
            photoIntent.putExtra("photo", pickedPhoto)
            startActivity(photoIntent)
        }

    private fun requestPermission () {
        when {
            ContextCompat.checkSelfPermission(
                context as MainActivity,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED -> {
                ActivityCompat.requestPermissions(activity as MainActivity, arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES), 1)
            }
            /*ActivityCompat.shouldShowRequestPermissionRationale(
                activity as MainActivity,
                Manifest.permission.READ_MEDIA_IMAGES
            ) -> {
                // additional info
            }*/
            else -> {
                val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galleryIntent, 2)
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
            }
        }
    }

    private var requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){ isGranted: Boolean ->
        if(isGranted) {
            getContent.launch(Manifest.permission.READ_MEDIA_IMAGES)
        }
        else {

        }
    }



    private fun getPhotoFromGallery() {
        requestPermission()
    }

    private fun getPhotoFromCamera() {

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galleryIntent, 2)
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}