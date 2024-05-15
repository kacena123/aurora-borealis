package com.example.aurora

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.aurora.Models.PoleModel
import com.example.aurora.databinding.ActivityMainBinding
import com.example.aurora.databinding.ActivityPoleDetailBinding
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.android.gms.maps.SupportMapFragment

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class PoleDetailActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityPoleDetailBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var mGoogleMap:GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        binding = ActivityPoleDetailBinding.inflate(layoutInflater)


        setContentView(binding.root)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setValues()

        binding.bupravit.setOnClickListener{
            openUpdateDialog(
                intent.getStringExtra("id").toString(),
                intent.getStringExtra("nazovPola").toString()
            )
        }

        binding.bzmazat.setOnClickListener {
            deleteRecord(
                intent.getStringExtra("id").toString()
            )
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap
        mGoogleMap?.uiSettings?.isZoomControlsEnabled = true
        val lat = intent.getStringExtra("sirka")?.toDoubleOrNull() ?: 0.0
        val lon = intent.getStringExtra("dlzka")?.toDoubleOrNull() ?: 0.0
        val location = LatLng(lat, lon)
        val zoomLevel = 15.0f
        mGoogleMap?.addMarker(MarkerOptions().position(location).title("Pole na mape"))
        mGoogleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(location, zoomLevel))
    }

    private fun deleteRecord(id:String){
        val ref = FirebaseDatabase.getInstance().getReference("Polia").child(id)
        val mTask = ref.removeValue()
        mTask.addOnSuccessListener {
            Toast.makeText(this, "Pole bolo zmazane", Toast.LENGTH_LONG).show()
            val intent = Intent(this, MainActivity::class.java)
            finish()
            startActivity(intent)
        }.addOnFailureListener { error ->
            Toast.makeText(this, "Deleting error ${error}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setValues(){
        binding.nazovPola.text = intent.getStringExtra("nazovPola")
        binding.plodina.text = intent.getStringExtra("plodina")
        binding.dlzka.text = intent.getStringExtra("dlzka")
        binding.sirka.text = intent.getStringExtra("sirka")
        binding.rozloha.text = intent.getStringExtra("rozloha") + " ha"
    }


    private fun openUpdateDialog(id:String, nazovPola:String){
        val mDialog = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val mDialogView = inflater.inflate(R.layout.update_dialog, null)
        mDialog.setView(mDialogView)

        val etNazovPola = mDialogView.findViewById<EditText>(R.id.etNazovPola)
        val etPlodina = mDialogView.findViewById<EditText>(R.id.etPlodina)
        val etRozloha = mDialogView.findViewById<EditText>(R.id.etRozloha)
        val button = mDialogView.findViewById<Button>(R.id.btnUpdateData)

        etNazovPola.setText(intent.getStringExtra("nazovPola"))
        etPlodina.setText(intent.getStringExtra("plodina"))
        etRozloha.setText(intent.getStringExtra("rozloha"))

        mDialog.setTitle("Aktualizacia informacii pola: $nazovPola")
        val alertDialog = mDialog.create()
        alertDialog.show()

        button.setOnClickListener {
            updateData(
                id,
                etNazovPola.text.toString(),
                etPlodina.text.toString(),
                intent.getStringExtra("sirka").toString(),
                intent.getStringExtra("dlzka").toString(),
                etRozloha.text.toString()
            )
            Toast.makeText(applicationContext, "Data boli aktualizovane", Toast.LENGTH_LONG).show()
            binding.nazovPola.text = etNazovPola.text.toString()
            binding.plodina.text = etPlodina.text.toString()
            binding.rozloha.text = etRozloha.text.toString() + " ha"

            alertDialog.dismiss()
        }




    }

    private fun updateData(
        id:String,
        nazovPola: String,
        plodina:String,
        sirka:String,
        dlzka:String,
        rozloha:String
    ){

        val userid = firebaseAuth.currentUser?.uid.toString()
        val referencia = FirebaseDatabase.getInstance().getReference("Polia").child(id)
        val pole = PoleModel(id, userid, nazovPola, plodina, sirka, dlzka, rozloha)
        referencia.setValue(pole)
    }
}