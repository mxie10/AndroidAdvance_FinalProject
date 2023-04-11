package com.example.firebase

import ImagesAdapter
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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
    private lateinit var imagesList: ArrayList<String>
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    val photoFileName = "IMG_${timeStamp}.jpg"

    // Initialize Firebase Realtime Database
    private lateinit var firebaseDatabase: FirebaseDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        context = this@HomeActivity

        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        firebaseDatabase = FirebaseDatabase.getInstance()

        imagesList = ArrayList()
        recyclerView = findViewById(R.id.recyclerView)
        layoutManager = GridLayoutManager(this, 2)
        adapter = ImagesAdapter(imagesList) { position ->
            val imageUrl = imagesList[position]
            deleteImageUrlFromFirebase(imageUrl)
        }
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        fetchImageUrlsFromFirebase()

        val selectImagesButton = findViewById<FloatingActionButton>(R.id.addImg_btn)
        val openCameraButton = findViewById<FloatingActionButton>(R.id.openCamera_btn)

        selectImagesButton.setOnClickListener{
            selectImagesFromGallery()
        }

        openCameraButton.setOnClickListener {
            openCamera()
        }

    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean{
        var intent = Intent(this@HomeActivity, MainActivity::class.java)

        //user shared preference to store the data
        val sharedPref: SharedPreferences = this.getSharedPreferences("MyPref", MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPref.edit()


        if(item.itemId == R.id.logout){
            startActivity(intent)
        }
        return true
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

        if (requestCode == PICK_IMAGES_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            if (data.clipData != null) {
                for (i in 0 until data.clipData!!.itemCount) {
                    val imageUri = data.clipData!!.getItemAt(i).uri.toString()
                    imagesList.add(imageUri)
                   uploadImageUrlToFirebase(imageUri)
                }
            } else {
                val imageUri = data.data.toString()
                imagesList.add(imageUri)
                uploadImageUrlToFirebase(imageUri)
            }
            sendNotification("Add image", "Success")
            adapter.notifyDataSetChanged()
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
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
            sendNotification("Add image", "Success")

            // Add the following line after saving the image to the Photos gallery
            val imageUriString = imageUri.toString()
           // uploadImageUrlToFirebase(imageUriString)
        }
    }

    private fun uploadImageUrlToFirebase(imageUrl: String) {
        val key = firebaseDatabase.reference.child("images").push().key
        key?.let {
            firebaseDatabase.reference.child("images").child(it).setValue(imageUrl)
        }
    }
    fun sendNotification(title: String, content: String) {
        Log.v("sendNotification", "in??")
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

    private fun fetchImageUrlsFromFirebase() {
        val imagesRef = firebaseDatabase.reference.child("images")
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                imagesList.clear()
                for (childSnapshot in dataSnapshot.children) {
                    val imageUrl = childSnapshot.getValue(String::class.java)
                    imageUrl?.let {
                        imagesList.add(it)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("HomeActivity", "fetchImageUrlsFromFirebase:onCancelled", databaseError.toException())
            }
        }
        imagesRef.addValueEventListener(valueEventListener)
    }

    // Add a new function to delete the image URL from Firebase
    private fun deleteImageUrlFromFirebase(imageUrl: String) {
        val imagesRef = firebaseDatabase.reference.child("images")
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (childSnapshot in dataSnapshot.children) {
                    val storedImageUrl = childSnapshot.getValue(String::class.java)
                    if (storedImageUrl == imageUrl) {
                        childSnapshot.ref.removeValue()
                        break
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("HomeActivity", "deleteImageUrlFromFirebase:onCancelled", databaseError.toException())
            }
        }
        imagesRef.addListenerForSingleValueEvent(valueEventListener)
    }
}