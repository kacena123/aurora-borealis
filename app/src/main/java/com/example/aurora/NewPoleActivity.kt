package com.example.aurora

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.aurora.Interfaces.ApiInterface
import com.example.aurora.Models.LocationItem
import com.example.aurora.Models.PoleModel
import com.example.aurora.databinding.ActivityNewPoleBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class NewPoleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewPoleBinding
    private lateinit var dbRef: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var url : String
    private lateinit var key : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewPoleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbRef = FirebaseDatabase.getInstance().getReference("Polia")
        firebaseAuth = FirebaseAuth.getInstance()
        url = "https://pro.openweathermap.org/"
        key = "2eb612e9a337f4bc55645c1eae5689ab"

        binding.button.setOnClickListener{
            saveData()
        }

    }
    private fun saveData(){
        val nazovPola = binding.nazovPolaEt.text.toString()
        val plodina = binding.plodinaET.text.toString()
        val sirka = binding.zemepisnaDlzkaET.text.toString()
        val dlzka = binding.zemepisnaSirkaET.text.toString()
        val rozloha = binding.rozlohaET.text.toString()

        if (nazovPola.isEmpty()){
            binding.nazovPolaEt.error = "Zadajte nazov pola"
        }
        if (plodina.isEmpty()){
            binding.plodinaET.error = "Zadajte nazov plodiny"
        }
        if (dlzka.isEmpty()){
            binding.zemepisnaDlzkaET.error = "Zadajte GPS pola"
        }
        if (sirka.isEmpty()){
            binding.zemepisnaSirkaET.error = "Zadajte GPS pola"
        }
        if (rozloha.isEmpty()){
            binding.rozlohaET.error = "Zadajte rozlohu pola"
        }
        val userid = firebaseAuth.currentUser?.uid.toString()
        val empID = dbRef.push().key!!
        val pole = PoleModel(empID, userid, nazovPola, plodina, dlzka, sirka, rozloha)

        dbRef.child(empID).setValue(pole)
            .addOnCompleteListener{
                Toast.makeText(this, "Data boli vlozene", Toast.LENGTH_LONG).show()
                val intent = Intent(this, MainActivity::class.java)
                binding.nazovPolaEt.text?.clear()
                binding.plodinaET.text?.clear()
                binding.zemepisnaDlzkaET.text?.clear()
                binding.zemepisnaSirkaET.text?.clear()
                binding.rozlohaET.text?.clear()
                startActivity(intent)


            }.addOnFailureListener { err ->
                Toast.makeText(this, "Error ${err.message}", Toast.LENGTH_LONG).show()
            }

    }


}