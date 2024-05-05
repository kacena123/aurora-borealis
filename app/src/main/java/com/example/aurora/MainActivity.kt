package com.example.aurora

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.aurora.databinding.ActivityMainBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fragmentName = intent.getStringExtra("fragment")
        if (fragmentName != null) {
            when (fragmentName) {
                "Skodce" -> replaceFragment(Skodce())
                // Add other fragments here
            }
        } else {
            replaceFragment(Polia())
        }
        if (intent.getStringExtra("fragment") == "Skodce") {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame_layout, Skodce())
            transaction.commit()
        }

        binding.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.polia -> replaceFragment(Polia())
                R.id.predpoved -> replaceFragment(Predpoved())
                R.id.skodce -> replaceFragment(Skodce())
                R.id.profil -> replaceFragment(Profil())

                else ->{}
            }
            true
        }
    }
    private fun replaceFragment(fragment: Fragment){
        val fragmnetManager = supportFragmentManager
        val fragmentTransaction = fragmnetManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }
}