package com.example.aurora

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.aurora.Sign.LoginActivity
import com.example.aurora.databinding.ActivityChangePasswordBinding
import com.example.aurora.databinding.ActivityZabudnuteHesloBinding
import com.google.firebase.auth.FirebaseAuth

class ZabudnuteHesloActivity : AppCompatActivity() {

    private lateinit var binding: ActivityZabudnuteHesloBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityZabudnuteHesloBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.button.setOnClickListener {
            val email = binding.emailET.text.toString()

            if (email.isNotEmpty()) {
                firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(this, "Email pre zmenu hesla bol odoslaný", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Vyplňte email", Toast.LENGTH_SHORT).show()
            }
        }

        //spat na prihlasovaciu stranku
        binding.buttonzrusit.setOnClickListener {
            intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}