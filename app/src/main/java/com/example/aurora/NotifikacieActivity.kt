package com.example.aurora

import android.content.Intent
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aurora.Adapters.NotifikacieAdapter
import com.example.aurora.Adapters.SkodceAdapter
import com.example.aurora.Models.NotifikacieModel
import com.example.aurora.Models.SkodecModel
import com.example.aurora.databinding.ActivityNotifikacieBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.example.aurora.NewNotifikaciaActivity
import com.example.aurora.NotifikaciaDetailActivity

class NotifikacieActivity : AppCompatActivity() {

    private var _binding: ActivityNotifikacieBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbref : DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var notifikacieArrayList : ArrayList<NotifikacieModel>
    private lateinit var mAdapter : NotifikacieAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityNotifikacieBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        notifikacieArrayList = arrayListOf<NotifikacieModel>()

        binding.mRecycler.layoutManager = LinearLayoutManager(this)
        binding.mRecycler.setHasFixedSize(true)

        //getNotifikacieData()

        binding.button.setOnClickListener {
            val intent = Intent(this, NewNotifikaciaActivity::class.java)
            startActivity(intent)
        }

        dbref = FirebaseDatabase.getInstance().getReference("Notifikacie")

        dbref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Clear the skodceArrayList before adding new SkodecModel objects
                notifikacieArrayList.clear()

                if(snapshot.exists()){

                    for (poleSnapshot in snapshot.children){
                        val notifikacia = poleSnapshot.getValue(NotifikacieModel::class.java)

                        if (notifikacia?.userID == firebaseAuth.currentUser?.uid.toString()){
                            notifikacieArrayList.add(notifikacia!!)
                            Log.d("NotifikacieAktivity", notifikacia.toString())

                        }
                    }
                    mAdapter = NotifikacieAdapter(notifikacieArrayList)
                    binding.mRecycler.adapter = mAdapter

                    mAdapter.setOnItemClickListener(object : NotifikacieAdapter.onItemClickListener{
                        override fun onItemClick(position: Int) {
                            val intent = Intent(this@NotifikacieActivity, NotifikaciaDetailActivity::class.java)
                            intent.putExtra("id", notifikacieArrayList[position].id)
                            intent.putExtra("pole", notifikacieArrayList[position].nazovPola)
                            intent.putExtra("hodiny", notifikacieArrayList[position].hodiny)
                            intent.putExtra("teplota", notifikacieArrayList[position].teplota)
                            intent.putExtra("sirka", notifikacieArrayList[position].sirka)
                            intent.putExtra("dlzka", notifikacieArrayList[position].dlzka)

                            startActivity(intent)
                        }
                    })
                }

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun getNotifikacieData() {
        dbref = FirebaseDatabase.getInstance().getReference("Notifikacie")
        dbref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (notifikacieSnapshot in snapshot.children) {
                        val notifikacie = notifikacieSnapshot.getValue(NotifikacieModel::class.java)
                        notifikacieArrayList.add(notifikacie!!)
                        Log.d("NotifikacieAktivity", notifikacie.toString())
                        if (notifikacie?.userID != firebaseAuth.currentUser?.uid.toString()) {
                            notifikacieArrayList.add(notifikacie!!)
                            Log.d("NotifikacieActivity", notifikacie.toString())
                        }
                    }
                    binding.mRecycler.adapter = NotifikacieAdapter(notifikacieArrayList)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@NotifikacieActivity, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }


}