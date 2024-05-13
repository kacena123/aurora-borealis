package com.example.aurora

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.aurora.Models.NotifikacieModel
import com.example.aurora.Models.PoleModel
import com.example.aurora.Models.SkodecModel
import com.example.aurora.databinding.ActivityNotifikaciaDetailBinding
import com.example.aurora.databinding.ActivitySkodecDetailBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class NotifikaciaDetailActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityNotifikaciaDetailBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var mGoogleMap: GoogleMap? = null
    private lateinit var poleArrayList : ArrayList<PoleModel>
    private lateinit var dbRef: DatabaseReference
    private lateinit var poleLokalit : ArrayList<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        binding = ActivityNotifikaciaDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setValues()

        binding.zmazat.setOnClickListener {
            deleteRecord(
                intent.getStringExtra("id").toString()
            )
        }

        binding.bupravit.setOnClickListener {
            openUpdateDialog(
                intent.getStringExtra("id").toString(),
                intent.getStringExtra("pole").toString()
            )
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap
        mGoogleMap?.uiSettings?.isZoomControlsEnabled = true
        val lat = intent.getStringExtra("sirka")?.toDoubleOrNull() ?: 0.0
        val lon = intent.getStringExtra("dlzka")?.toDoubleOrNull() ?: 0.0
        val location = LatLng(lat, lon)
        val zoomLevel = 10.0f
        mGoogleMap?.addMarker(MarkerOptions().position(location).title("pole na mape"))
        mGoogleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(location, zoomLevel))
    }

    private fun setValues(){
        binding.pole.text = intent.getStringExtra("pole")
        binding.hodiny.text = intent.getStringExtra("hodiny")
        binding.teplota.text = intent.getStringExtra("teplota")
    }

    private fun deleteRecord(id:String){
        val ref = FirebaseDatabase.getInstance().getReference("Notifikacie").child(id)
        val mTask = ref.removeValue()
        mTask.addOnSuccessListener {
            Toast.makeText(this, "Notifikácia bola zmazaná", Toast.LENGTH_LONG).show()
            val intent = Intent(this, NotifikacieActivity::class.java)
            finish()
            startActivity(intent)
        }.addOnFailureListener { error ->
            Toast.makeText(this, "Deleting error ${error}", Toast.LENGTH_LONG).show()
        }
    }

    private fun openUpdateDialog(id:String, pole:String){
        val mDialog = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val mDialogView = inflater.inflate(R.layout.update_dialog_notifikacie, null)
        mDialog.setView(mDialogView)

        val etPole = mDialogView.findViewById<AutoCompleteTextView>(R.id.autoCompleteTextView)
        val etHodiny = mDialogView.findViewById<NumberPicker>(R.id.pickerhodiny)
        val etTeplota = mDialogView.findViewById<NumberPicker>(R.id.pickerteptota)

        poleArrayList = arrayListOf<PoleModel>()
        poleLokalit = arrayListOf<String>()

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
                    etPole.setAdapter(arrayAdapter)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })


        //getPoleData()

        //val arrayAdapter = ArrayAdapter(applicationContext, R.layout.dropdown_item, poleLokalit.toArray())
        //etPole.setAdapter(arrayAdapter)

        etHodiny.maxValue = 100
        etHodiny.minValue = 1
        //etHodiny.value = intent.getStringExtra("hodiny")?.toIntOrNull() ?: 1

        val tempMinValue = -20 // minimum value you want to support for pickerteptota
        val tempMaxValue = 20 // maximum value you want to support for pickerteptota

        etTeplota.maxValue = tempMaxValue - tempMinValue
        etTeplota.minValue = 0
        etTeplota.setFormatter { i -> (i + tempMinValue).toString() }
        etTeplota.value = 20
        //etTeplota.value = intent.getStringExtra("teplota")?.toIntOrNull() ?: 20

        val button = mDialogView.findViewById<Button>(R.id.btnUpdateData)

        mDialog.setTitle("Aktualizacia notifikácie na poli $pole")
        val alertDialog = mDialog.create()
        alertDialog.show()


        button.setOnClickListener {
            val temp = etTeplota.value + tempMinValue
            updateData(
                id,
                etPole.text.toString(),
                etHodiny.value.toString(),
                temp.toString()
            )
            Toast.makeText(applicationContext, "Notifikacia bola aktualizovana", Toast.LENGTH_LONG).show()
            binding.pole.text = etPole.text.toString()
            binding.hodiny.text = etHodiny.value.toString()
            binding.teplota.text = temp.toString()

            alertDialog.dismiss()
        }
    }

    private fun updateData(
        id:String,
        pole: String,
        hodiny: String,
        teplota: String
    ){

        val sirka = intent.getStringExtra("sirka")
        val dlzka = intent.getStringExtra("dlzka")

        val userid = firebaseAuth.currentUser?.uid.toString()

        val referencia = FirebaseDatabase.getInstance().getReference("Notifikacie").child(id)
        val notifikacia = NotifikacieModel(id, sirka, dlzka, pole, hodiny, teplota, userid)
        referencia.setValue(notifikacia)
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

                    //val arrayAdapter = ArrayAdapter(applicationContext, R.layout.dropdown_item, poleLokalit.toArray())
                    //binding.autoCompleteTextView.setAdapter(arrayAdapter)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

}