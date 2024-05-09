package com.example.aurora

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.aurora.databinding.ActivityChangePasswordBinding
import com.example.aurora.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangePasswordBinding
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        //zmena hesla
        binding.button.setOnClickListener {
            val curpass = binding.aktualnehesloEt.text.toString()
            val pass = binding.passET.text.toString()
            val confirmPass = binding.confirmPassEt.text.toString()

            if (curpass.isNotEmpty() && pass.isNotEmpty() && confirmPass.isNotEmpty()) {

                firebaseAuth.signInWithEmailAndPassword(firebaseAuth.currentUser?.email.toString(), curpass).addOnCompleteListener {
                    if (it.isSuccessful) {
                        if (pass == confirmPass) {
                            firebaseAuth.currentUser?.updatePassword(pass)?.addOnSuccessListener {
                                Toast.makeText(this, "Heslo bolo zmenené", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, MainActivity::class.java)
                                intent.putExtra("fragment", "Profil")
                                startActivity(intent)
                                finish()
                            }
                        } else {
                            Toast.makeText(this, "Heslá sa nezhodujú", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Zadali ste nesprávne heslo", Toast.LENGTH_SHORT).show()

                    }
                }
            } else {
                Toast.makeText(this, "Vyplňte všetky polia", Toast.LENGTH_SHORT).show()
            }
        }

        //návrat na hlavnú stránku
        binding.buttonzrusit.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("fragment", "Profil")
            startActivity(intent)
            finish()
        }
    }
}