package com.example.aurora.Sign


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.aurora.MainActivity
import com.example.aurora.ZabudnuteHesloActivity
import com.example.aurora.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.buttonRegistracia.setOnClickListener{
            val intent = Intent(this , SignUpActivity::class.java)
            startActivity(intent)
        }

        binding.button.setOnClickListener{
            val email = binding.emailEt.text.toString()
            val pass = binding.passET.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty()) {

                firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener {
                    if (it.isSuccessful) {
                        if (firebaseAuth.currentUser?.isEmailVerified == true) {
                            binding.overenieEmailu.visibility = android.view.View.INVISIBLE
                            binding.zaslatOverovaciEmail.visibility = android.view.View.INVISIBLE
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                        } else {
                            Toast.makeText(this, "Najprv overte svoju emailovu adresu.", Toast.LENGTH_SHORT).show()
                            binding.overenieEmailu.visibility = android.view.View.VISIBLE
                            binding.zaslatOverovaciEmail.visibility = android.view.View.VISIBLE
                            binding.zaslatOverovaciEmail.setOnClickListener {
                                firebaseAuth.currentUser?.sendEmailVerification()?.addOnSuccessListener {
                                    Toast.makeText(this, "Email bol odoslaný na vašu emailovú adresu.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } else {
                        Toast.makeText(this, "Zadali ste nesprávny email alebo heslo", Toast.LENGTH_SHORT).show()

                    }
                }
            } else {
                Toast.makeText(this, "Empty Fields Are not Allowed !!", Toast.LENGTH_SHORT).show()
            }
        }

        //zabudnute heslo
        binding.zabudnuteHeslo.setOnClickListener {
            val intent = Intent(this, ZabudnuteHesloActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onStart(){
        super.onStart()

        if (firebaseAuth.currentUser != null){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

}