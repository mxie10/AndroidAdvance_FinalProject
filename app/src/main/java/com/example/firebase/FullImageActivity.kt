package com.example.firebase
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.firebase.R

class FullImageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_image)

        val imageUrl = intent.getStringExtra("image_url")
        val imageView: ImageView = findViewById(R.id.fullImageView)

        Glide.with(this)
            .load(imageUrl)
            .into(imageView)
    }
}