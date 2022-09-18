package com.example.accidentdetection.Firebase

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.example.accidentdetection.MainActivity
import com.example.accidentdetection.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class ProfileDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_details)

        loadProfileDetails()

        val signOut : Button = findViewById(R.id.signOutBtn)
        val back : ImageView = findViewById(R.id.backToMainBtn)

        back.setOnClickListener {
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        signOut.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this,WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    private fun loadProfileDetails(){
        val auth = FirebaseAuth.getInstance()
        val nameTxt : TextView = findViewById(R.id.profile_username_d)
        val ageTxt : TextView = findViewById(R.id.profile_age_d)
        val sexTxt :  TextView = findViewById(R.id.profile_sex_d)
        val usrProfilePic : ImageView = findViewById(R.id.profile_image_d)

        //cloud firestore
        val firestore = FirebaseFirestore.getInstance()
        val ref=firestore.collection("User").document(auth.uid.toString())
        ref.get()
            .addOnSuccessListener {document->
                if(document != null){
                    val usrName = document.get("Name")
                    val usrAge = document.get("Age")
                    val usrSex = document.get("Sex")
                    nameTxt.text= usrName.toString()
                    ageTxt.text=usrAge.toString()
                    sexTxt.text=usrSex.toString()
                }
                else {
                    Log.d("FIRESTORE", "No such document")
                }
            }

        //storage
        val localFile = File.createTempFile("tempImage","jpg")
        FirebaseStorage.getInstance().getReference("profilePicUploads/${auth.currentUser!!.uid}/${auth.currentUser!!.uid}.jpg")
            .getFile(localFile).addOnSuccessListener {
                val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                usrProfilePic.setImageBitmap(bitmap)
            }
    }
}