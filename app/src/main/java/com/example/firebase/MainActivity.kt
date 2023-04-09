package com.example.firebase

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

class MainActivity : AppCompatActivity() {
    lateinit var context: Context
    private lateinit var database: FirebaseDatabase
    private lateinit var dbRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        context = this@MainActivity
        database = FirebaseDatabase.getInstance()
        dbRef = database.getReference("products")
        auth = Firebase.auth
//        val imageView: ImageView = findViewById(R.id.imageView_logo)
//        CoroutineScope(Dispatchers.IO).launch {
//            val bitmap = getBitmap()
//            withContext(Dispatchers.Main) {
//                imageView.setImageBitmap(bitmap)
//            }
//        }
    }

        fun getBitmap(): Bitmap? {
            val url = URL("https://images.unsplash.com/photo-1538340794916-c941f42976f2?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=2970&q=80")
            return BitmapFactory.decodeStream(url.openConnection().getInputStream())
        }

        fun btnLogin_pressed(view: View){
            if(view.id == R.id.btn_login_submit){
                val userName = findViewById<EditText>(R.id.editText_username).text.toString()
                val password = findViewById<EditText>(R.id.editText_password).text.toString()
                auth.signInWithEmailAndPassword(userName, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            val sharedPref: SharedPreferences = this.getSharedPreferences("MyPref", MODE_PRIVATE)
                            val editor: SharedPreferences.Editor = sharedPref.edit()
                            editor.putString("userId",user!!.uid.toString())
                            editor.commit()
                            var intent = Intent(this@MainActivity, HomeActivity::class.java)
                            startActivity(intent)
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(baseContext, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        fun btnRegister_pressed(view: View){
            if(view.id == R.id.btn_navigateToRegisterPage){
                var intent = Intent(this@MainActivity, RegisterActivity::class.java)
                startActivity(intent)
            }
        }
}
