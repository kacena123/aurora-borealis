package com.example.aurora

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.aurora.Models.PoleModel
import com.example.aurora.Models.SkodecModel
import com.example.aurora.databinding.ActivityMainBinding
import com.example.aurora.databinding.ActivityPoleDetailBinding
import com.example.aurora.databinding.ActivitySkodecDetail2Binding
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

class SkodecDetailActivity2 : AppCompatActivity(), OnMapReadyCallback  {

    private lateinit var binding: ActivitySkodecDetail2Binding
    private lateinit var firebaseAuth: FirebaseAuth
    private var mGoogleMap:GoogleMap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        binding = ActivitySkodecDetail2Binding.inflate(layoutInflater)

        setContentView(binding.root)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        setValues()

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

    private fun setValues(){
        binding.nazovSkodca.text = intent.getStringExtra("nazovSkodca")
        binding.popis.text = intent.getStringExtra("popis")
    }
}