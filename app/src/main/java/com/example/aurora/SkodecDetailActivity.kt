package com.example.aurora

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.aurora.Models.PoleModel
import com.example.aurora.Models.SkodecModel
import com.example.aurora.databinding.ActivityMainBinding
import com.example.aurora.databinding.ActivityPoleDetailBinding
import com.example.aurora.databinding.ActivitySkodecDetailBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class SkodecDetailActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivitySkodecDetailBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var mGoogleMap:GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        binding = ActivitySkodecDetailBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        setValues()

        binding.bupravit.setOnClickListener{
            openUpdateDialog(
                intent.getStringExtra("id").toString(),
                intent.getStringExtra("nazovSkodca").toString()
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
        val zoomLevel = 10.0f
        mGoogleMap?.addMarker(MarkerOptions().position(location).title("Skodec na mape"))
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
        binding.nazovSkodca.text = intent.getStringExtra("nazovSkodca")
        binding.popis.text = intent.getStringExtra("popis")
    }


    private fun openUpdateDialog(id:String, nazovSkodca:String){
        val mDialog = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val mDialogView = inflater.inflate(R.layout.update_dialog_skodec, null)
        mDialog.setView(mDialogView)

        val etNazovSkodca = mDialogView.findViewById<EditText>(R.id.etNazovSkodca)
        val etPopis = mDialogView.findViewById<EditText>(R.id.etPopis)

        val button = mDialogView.findViewById<Button>(R.id.btnUpdateData)

        etNazovSkodca.setText(intent.getStringExtra("nazovSkodca"))
        etPopis.setText(intent.getStringExtra("popis"))

        mDialog.setTitle("Aktualizacia informacii skodca: $nazovSkodca")
        val alertDialog = mDialog.create()
        alertDialog.show()

        button.setOnClickListener {
            updateData(
                id,
                etNazovSkodca.text.toString(),
                etPopis.text.toString()
            )
            Toast.makeText(applicationContext, "Data boli aktualizovane", Toast.LENGTH_LONG).show()
            binding.nazovSkodca.text = etNazovSkodca.text.toString()
            binding.popis.text = etPopis.text.toString()

            alertDialog.dismiss()
        }
    }


    private fun updateData(
        id:String,
        nazovSkodca: String,
        popis:String
    ){

        val sirka = intent.getStringExtra("sirka")
        val dlzka = intent.getStringExtra("dlzka")
        val lokalita = intent.getStringExtra("lokalita")

        val userid = firebaseAuth.currentUser?.uid.toString()
        val userName = firebaseAuth.currentUser?.email.toString()

        val referencia = FirebaseDatabase.getInstance().getReference("Skodce").child(id)
        val skodec = SkodecModel(id, userid, userName, nazovSkodca, sirka, dlzka, lokalita, popis)
        referencia.setValue(skodec)
    }
}