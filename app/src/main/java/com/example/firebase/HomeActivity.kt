package com.example.firebase

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton


class HomeActivity: AppCompatActivity() {
    lateinit var context: Context
    private val PICK_IMAGES_REQUEST_CODE = 101

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ImagesAdapter
    private lateinit var layoutManager: GridLayoutManager
    private lateinit var imagesList:ArrayList<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        context = this@HomeActivity

        imagesList = ArrayList()
        recyclerView = findViewById(R.id.recyclerView)
        layoutManager = GridLayoutManager(this,3)
        adapter = ImagesAdapter(imagesList)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter=adapter

        val selectImagesButton = findViewById<FloatingActionButton>(R.id.addImg_btn)

        selectImagesButton.setOnClickListener{
            selectImagesFromGallery()
        }
    }

    private fun selectImagesFromGallery(){
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true)
        startActivityForResult(Intent.createChooser(intent,"Select Images"),PICK_IMAGES_REQUEST_CODE);
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.v("onActivityResult", "in onActivityResult????????????")
        Log.v("requestCode", requestCode.toString())
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
            adapter.notifyDataSetChanged()
        }
    }
}