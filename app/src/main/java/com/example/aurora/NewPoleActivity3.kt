package com.example.aurora

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.aurora.Interfaces.ApiInterface
import com.example.aurora.Models.LocationItem
import com.example.aurora.Models.PoleModel
import com.example.aurora.databinding.ActivityNewPole3Binding
import com.example.aurora.databinding.ActivityNewPoleBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NewPoleActivity3 : AppCompatActivity(), OnMapReadyCallback {

    private val MAX_FIELDS_PER_DAY = 20

    private lateinit var binding: ActivityNewPole3Binding
    private lateinit var dbRef: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var url : String
    private lateinit var key : String
    private var mGoogleMap: GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastlocation: android.location.Location? = null
    private var lat = ""
    private var lon = ""
    private var marker: Marker? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewPole3Binding.inflate(layoutInflater)
        setContentView(binding.root)

        dbRef = FirebaseDatabase.getInstance().getReference("Polia")
        firebaseAuth = FirebaseAuth.getInstance()
        url = "https://pro.openweathermap.org/"
        key = "2eb612e9a337f4bc55645c1eae5689ab"


        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.button.setOnClickListener{
            saveData()
        }

    }


    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap
        mGoogleMap?.uiSettings?.isZoomControlsEnabled = true
        //ziskanie aktualnej polohy
        setUpMap()

        mGoogleMap?.setOnMapClickListener { latLng ->
            lat = latLng.latitude.toString()
            lon = latLng.longitude.toString()
            //Toast.makeText(this, "Clicked location: Lat = $lat, Lon = $lon", Toast.LENGTH_LONG).show()
            // Remove the last marker from the map
            marker?.remove()

            // Add a new marker to the map at the clicked location
            marker = mGoogleMap?.addMarker(MarkerOptions().position(latLng).title("Clicked Point"))
        }
    }

    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
            return
        }
        mGoogleMap?.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            if (location != null) {
                lastlocation = location
                val currentLocation = LatLng(location.latitude, location.longitude)
                val zoomLevel = 10.0f
                mGoogleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, zoomLevel))
            }
        }
    }

    private fun saveData(){
        val nazovPola = binding.nazovPolaEt.text.toString()
        val plodina = binding.plodinaET.text.toString()


        val rozloha = binding.rozlohaET.text.toString()

        if (nazovPola.isEmpty()){
            binding.nazovPolaEt.error = "Zadajte nazov pola"
            return
        }
        if (plodina.isEmpty()){
            binding.plodinaET.error = "Zadajte nazov plodiny"
            return
        }

        if (rozloha.isEmpty()){
            binding.rozlohaET.error = "Zadajte rozlohu pola"
            return
        }
        val userid = firebaseAuth.currentUser?.uid.toString()
        val empID = dbRef.push().key!!
        val pole = PoleModel(empID, userid, nazovPola, plodina, lat, lon, rozloha)


        val date = getCurrentDate()
        val userDailyLimitRef = FirebaseDatabase.getInstance().getReference("UserDailyLimits").child(userid).child(date)

        userDailyLimitRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val count = dataSnapshot.getValue(Int::class.java) ?: 0
                if (count >= MAX_FIELDS_PER_DAY) {
                    Toast.makeText(applicationContext, "Dosiahli ste maximálny počet polí za deň", Toast.LENGTH_LONG).show()
                } else {
                    userDailyLimitRef.setValue(count + 1)
                    saveFieldToDatabase(empID, pole)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors.
            }
        })
    }

    private fun saveFieldToDatabase(empID: String, pole: PoleModel) {
        dbRef.child(empID).setValue(pole)
            .addOnCompleteListener{
                Toast.makeText(this, "Data boli vlozene", Toast.LENGTH_LONG).show()
                val intent = Intent(this, MainActivity::class.java)
                binding.nazovPolaEt.text?.clear()
                binding.plodinaET.text?.clear()
                binding.rozlohaET.text?.clear()
                startActivity(intent)


            }.addOnFailureListener { err ->
                Toast.makeText(this, "Error ${err.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

}