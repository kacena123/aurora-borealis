package com.example.aurora.Sign

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.aurora.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import android.content.Intent
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.textView.setOnClickListener {
            val intent = Intent(this , LoginActivity::class.java)
            startActivity(intent)
        }

        binding.button.setOnClickListener{
            val username = binding.usernameEt.text.toString()
            val email = binding.emailEt.text.toString()
            val pass = binding.passET.text.toString()
            val  confirmPass = binding.confirmPassEt.text.toString()

            if (username.isNotEmpty() && email.isNotEmpty() && pass.isNotEmpty() && confirmPass.isNotEmpty()){
                if (pass == confirmPass){
                    firebaseAuth.createUserWithEmailAndPassword(email , pass).addOnCompleteListener {
                        if(it.isSuccessful){
                            firebaseAuth.currentUser?.sendEmailVerification()?.addOnSuccessListener {
                                Toast.makeText(this, "Registracia prebehla uspesne, prosim, overte svoj email", Toast.LENGTH_SHORT).show()
                                val userId = firebaseAuth.currentUser?.uid.toString()
                                val date = getCurrentDate()
                                val userDailyLimitRef = FirebaseDatabase.getInstance().getReference("UserDailyLimits").child(userId).child(date)
                                userDailyLimitRef.setValue(0)
                            }
                                ?.addOnFailureListener {
                                    Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
                                }
                            val intent = Intent(this, LoginActivity::class.java)
                            startActivity(intent)
                        }else {
                            Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
                }else{
                    Toast.makeText(this, "Hesla sa nezhoduju", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this, "Nevyplnili ste vsetky polia", Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

}