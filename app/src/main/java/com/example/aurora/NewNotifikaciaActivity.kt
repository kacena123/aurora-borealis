package com.example.aurora

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.aurora.Models.NotifikacieModel
import com.example.aurora.Models.PoleModel
import com.example.aurora.databinding.ActivityNewNotifikaciaBinding
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NewNotifikaciaActivity : AppCompatActivity() {

    private val MAX_FIELDS_PER_DAY = 50

    private var _binding: ActivityNewNotifikaciaBinding? = null
    private val binding get() = _binding!!
    private lateinit var poleArrayList : ArrayList<PoleModel>
    private lateinit var poleLokalit : ArrayList<String>

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityNewNotifikaciaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.pickerhodiny.maxValue = 100
        binding.pickerhodiny.minValue = 1

        val tempMinValue = -20 // minimum value you want to support for pickerteptota
        val tempMaxValue = 20 // maximum value you want to support for pickerteptota

        binding.pickerteptota.maxValue = tempMaxValue - tempMinValue
        binding.pickerteptota.minValue = 0
        binding.pickerteptota.setFormatter { i -> (i + tempMinValue).toString() }
        binding.pickerteptota.value = 20

        //dropdown menu
        poleArrayList = arrayListOf<PoleModel>()
        poleLokalit = arrayListOf<String>()
        getPoleData()

        binding.autoCompleteTextView.setOnItemClickListener(AdapterView.OnItemClickListener { parent, view, position, id ->
            val pole = poleArrayList[position]
            Toast.makeText(applicationContext, pole.nazovPola, Toast.LENGTH_SHORT).show()

        })

        binding.button.setOnClickListener {
            saveData()
            val intent = Intent(this, NotifikacieActivity::class.java)
            startActivity(intent)
        }


    }

    private fun saveData() {
        val tempMinValue = -20 // minimum value you want to support for pickerteptota
        val tempMaxValue = 20 // maximum value you want to support for pickerteptota

        val hodiny = binding.pickerhodiny.value
        val teplota = binding.pickerteptota.value + tempMinValue

        val nazovPola = binding.autoCompleteTextView.text.toString()
        val p = poleArrayList.indexOfFirst({it.nazovPola.toString() == nazovPola.toString()})
        val pole = poleArrayList[p]

        val dlzka = pole.dlzka.toString()
        val sirka = pole.sirka.toString()

        val userid = firebaseAuth.currentUser?.uid.toString()

        val notifikacia = NotifikacieModel(sirka, dlzka, nazovPola, hodiny.toString(), teplota.toString(), userid)

        getCurrentDateFirebase().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val date = task.result
                val userDailyLimitRef = FirebaseDatabase.getInstance().getReference("UserDailyLimits").child(userid).child(date)

                userDailyLimitRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val count = dataSnapshot.getValue(Int::class.java) ?: 0
                        if (count >= MAX_FIELDS_PER_DAY) {
                            Toast.makeText(applicationContext, "Dosiahli ste maximálny počet vložení do databázy za deň", Toast.LENGTH_LONG).show()
                        } else {
                            userDailyLimitRef.setValue(count + 1)
                            saveNotifikaciaToDatabase(notifikacia)
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle possible errors.
                    }
                })
            } else {
                // Handle possible errors.
            }
        }
    }

    private fun getCurrentDateFirebase(): Task<String> {
        val ref = FirebaseDatabase.getInstance().getReference("Timestamp")
        return ref.setValue(ServerValue.TIMESTAMP)
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    throw task.exception!!
                }
                ref.get()
            }.continueWith { task ->
                val timestamp = task.result?.value as Long
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                sdf.format(Date(timestamp))
            }
    }

    private fun saveNotifikaciaToDatabase(notifikacia: NotifikacieModel) {
        val dbRef = FirebaseDatabase.getInstance().getReference("Notifikacie")
        val notifikaciaID = dbRef.push().key
        dbRef.child(notifikaciaID!!).setValue(notifikacia).addOnSuccessListener {
            Toast.makeText(applicationContext, "Notifikacia pridana", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(applicationContext, "Chyba", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getPoleData() {
        dbRef = FirebaseDatabase.getInstance().getReference("Polia")
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for (poleSnapshot in snapshot.children){
                        val pole = poleSnapshot.getValue(PoleModel::class.java)
                        if (pole?.userID == firebaseAuth.currentUser?.uid.toString()){
                            poleArrayList.add(pole!!)
                        }

                    }
                    //val poleLokalit = ArrayList<String>()
                    for (i in poleArrayList){
                        poleLokalit.add(i.nazovPola.toString())
                    }

                    val arrayAdapter = ArrayAdapter(applicationContext, R.layout.dropdown_item, poleLokalit.toArray())
                    binding.autoCompleteTextView.setAdapter(arrayAdapter)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
}