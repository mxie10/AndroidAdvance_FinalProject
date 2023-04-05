package com.example.firebase

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class HomeActivity: AppCompatActivity() {
    lateinit var context: Context
    private val PICK_IMAGES_REQUEST_CODE = 101
    val REQUEST_IMAGE_CAPTURE = 1
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ImagesAdapter
    private lateinit var layoutManager: GridLayoutManager
    private lateinit var imagesList:ArrayList<String>
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    val photoFileName = "IMG_${timeStamp}.jpg"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        context = this@HomeActivity

        imagesList = ArrayList()
        recyclerView = findViewById(R.id.recyclerView)
        layoutManager = GridLayoutManager(this,2)
        adapter = ImagesAdapter(imagesList)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter=adapter

        val selectImagesButton = findViewById<FloatingActionButton>(R.id.addImg_btn)
        val openCameraButton = findViewById<FloatingActionButton>(R.id.openCamera_btn)

        selectImagesButton.setOnClickListener{
            selectImagesFromGallery()
        }

        openCameraButton.setOnClickListener {
            openCamera()
        }
    }

    private fun selectImagesFromGallery(){
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true)
        startActivityForResult(Intent.createChooser(intent,"Select Images"),PICK_IMAGES_REQUEST_CODE);
    }

    private fun openCamera(){
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == PICK_IMAGES_REQUEST_CODE && resultCode == RESULT_OK && data!=null){
            if(data.clipData != null) {
                for(i in 0  until data.clipData!!.itemCount){
                    val imageUri = data.clipData!!.getItemAt(i).uri.toString()
                    imagesList.add(imageUri)
                }
            }else{
                val imageUri = data.data.toString()
                imagesList.add(imageUri)
            }
            sendNotification("Add image","Success")
            adapter.notifyDataSetChanged()
        }else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFileName = "JPEG_" + timeStamp + ".jpg"

            // Save the image to the Photos gallery
            val resolver = contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            }
            val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            resolver.openOutputStream(imageUri!!).use { outputStream ->
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }
            sendNotification("Add image","Success")
        }
    }

    fun sendNotification(title:String,content:String){
        Log.v("sendNotification","in??")
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "my_channel_id",
                "My Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, "my_channel_id")
            .setSmallIcon(R.drawable.notification)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        notificationManager.notify(1, builder.build())
    }
}