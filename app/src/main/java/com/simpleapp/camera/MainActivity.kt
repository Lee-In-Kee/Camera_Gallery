package com.simpleapp.camera

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import com.simpleapp.camera.databinding.ActivityMainBinding
import java.io.IOException
import java.text.SimpleDateFormat

class MainActivity : BaseActivity() {

    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    var realUri: Uri? = null

    val PERM_STROAGE = 99 // 외부 저장소 권한 처리
    val PERM_CAMERA = 100 // 카메라 권한 요청
    val REQ_CAMERA = 101 // 카메라 촬영 요청
    val REQ_STORAGE = 102

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        requirePermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERM_STROAGE)
    }

    override fun permissionGranted(requestCode: Int) {
        when (requestCode) {
            PERM_STROAGE -> setViews()
            PERM_CAMERA -> openCamera()
        }
    }

    override fun permissionDenied(requestCode: Int) {
        when (requestCode) {
            PERM_STROAGE -> {
                Toast.makeText(baseContext,
                    "외부 저장소 권한을 승인해야 앱을 사영할 수 있습니다.",
                    Toast.LENGTH_LONG).show()

                finish()
            }

            PERM_CAMERA -> {
                Toast.makeText(baseContext,
                    "외부 저장소 권한을 승인해야 앱을 사영할 수 있습니다.",
                    Toast.LENGTH_LONG).show()
            }
        }
    }

    fun setViews() {
        binding.btnCamera.setOnClickListener {
            requirePermissions(arrayOf(Manifest.permission.CAMERA), PERM_CAMERA)
        }

        binding.btnGallery.setOnClickListener {
            openGallery()
        }
    }

    fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = MediaStore.Images.Media.CONTENT_TYPE

        startActivityForResult(intent, REQ_STORAGE)
    }

    fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        createImageUri(newFileName(), "image/jpg")?.let {
            realUri = it
            intent.putExtra(MediaStore.EXTRA_OUTPUT, realUri)
            startActivityForResult(intent, REQ_CAMERA)
        }
    }

    fun createImageUri(filename: String, mimeType: String): Uri? {
        var values = ContentValues()
        values.put(MediaStore.Images.Media.DISPLAY_NAME, filename)
        values.put(MediaStore.Images.Media.MIME_TYPE, mimeType)

        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    }

    fun newFileName(): String {
        val sdf = SimpleDateFormat("yyyyMMdd_HH:mm:ss")
        val filename = sdf.format(System.currentTimeMillis())

        return "$filename.jpg"
    }

    fun loadBitmap(photoUri: Uri): Bitmap? {

        var image: Bitmap? = null
        try {
            image = if (Build.VERSION.SDK_INT > 27) {
                val source: ImageDecoder.Source =
                    ImageDecoder.createSource(this.contentResolver, photoUri)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(this.contentResolver, photoUri)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return image
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQ_CAMERA -> {
                    realUri?.let {
                        val bitmap = loadBitmap(it)
                        binding.imagePreview.setImageBitmap(bitmap)

                        realUri = null
                    }
                }

                REQ_STORAGE -> {
                    data?.data?.let {
                        binding.imagePreview.setImageURI(it)
                    }
                }
            }
        }
    }
}
