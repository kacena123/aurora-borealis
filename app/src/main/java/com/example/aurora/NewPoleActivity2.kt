package com.example.aurora

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.aurora.Interfaces.ApiInterface
import com.example.aurora.Models.LocationItem
import com.example.aurora.Models.PoleModel
import com.example.aurora.databinding.ActivityNewPole2Binding
import com.example.aurora.databinding.ActivityNewPoleBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Looper
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.location.LocationManagerCompat.isLocationEnabled
//import android.location.locationManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationSettingsRequest
//import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource


class NewPoleActivity2 : AppCompatActivity() {

    private lateinit var binding: ActivityNewPole2Binding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var dbRef: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var url : String
    private lateinit var key : String

    private var lat = ""
    private var log = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewPole2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)



        dbRef = FirebaseDatabase.getInstance().getReference("Polia")
        firebaseAuth = FirebaseAuth.getInstance()
        url = "https://pro.openweathermap.org/"
        key = "2eb612e9a337f4bc55645c1eae5689ab"

        getLocation()

        binding.button.setOnClickListener{
            saveData()
        }

    }
    /*
    private fun getLocation(){
        //check location permition
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 100)
            Log.d("NewPoleActivity2", "cele zle")
            return
        }

        val location = fusedLocationClient.lastLocation
        location.addOnSuccessListener {
            if (it!=null) {
                Log.d("NewPoleActivity2", "getLocation: ${it.latitude}, ${it.longitude}")
                lat = it.latitude.toString()
                log = it.longitude.toString()
                binding.textView2.text = "Poloha: \nDlzka: " + lat + "\nSirka: " + log
            }
        }
    }
    */

    private fun getLocation() {
        // Check location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
            return
        }

        val locationRequest = LocationRequest.create().apply {
            interval = 10000 // Set the desired interval for active location updates, in milliseconds.
            fastestInterval = 5000 // Set the fastest rate for active location updates, in milliseconds.
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY // Set the priority of the request.
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    if (location != null) {
                        lat = location.latitude.toString()
                        log = location.longitude.toString()
                        binding.textView2.text = "Poloha: \nSirka: $lat\nDlzka: $log"
                    }
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
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
        val pole = PoleModel(empID, userid, nazovPola, plodina, lat, log, rozloha)


        //getPlace(sirka, dlzka)
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

    public fun getPlace(lat:String, lon:String){

        val retrofitBuilder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(url)
            .build()
            .create(ApiInterface::class.java)

        val retrofitData = retrofitBuilder.getData(lat, lon, 1, key)
        retrofitData.enqueue(object : Callback<List<LocationItem>?> {
            override fun onResponse(
                call: Call<List<LocationItem>?>,
                response: Response<List<LocationItem>?>
            ) {
                val responseBody = response.body()!!

                val myString = StringBuilder()
                for (myData in responseBody){
                    myString.append(myData.name)
                    myString.append("\n")
                }


                //val res = responseBody[0].name
                Log.d("NewPoleActivity2", "Lokacia: $myString")
                //Toast.makeText(this@NewPoleActivity, "Lokacia: $myString", Toast.LENGTH_LONG).show()
            }

            override fun onFailure(call: Call<List<LocationItem>?>, t: Throwable) {
                Log.d("NewPoleActivity", "cele zle ")
            }
        })
    }
}