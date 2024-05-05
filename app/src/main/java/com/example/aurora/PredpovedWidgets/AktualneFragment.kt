package com.example.aurora.PredpovedWidgets

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aurora.Adapters.AktualnePocasieAdapter
import com.example.aurora.Interfaces.ApiInterface
import com.example.aurora.Models.HourlyForecast4.AktualnePocasieModel
import com.example.aurora.Models.HourlyForecast4.HourlyForecast4
import com.example.aurora.databinding.FragmentAktualneBinding
import com.example.aurora.databinding.FragmentPredpovedBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.math.roundToInt

class AktualneFragment : Fragment() {

    private var _binding: FragmentAktualneBinding? = null
    private val binding get() = _binding!!


    private lateinit var aktualnePocasieAdapter : AktualnePocasieAdapter

    private lateinit var dbRef: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var url : String
    private lateinit var key : String

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var lat: String = ""
    private var lon: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAktualneBinding.inflate(inflater, container, false)

        dbRef = FirebaseDatabase.getInstance().getReference("Polia")
        firebaseAuth = FirebaseAuth.getInstance()
        url = "https://pro.openweathermap.org/"
        key = "2eb612e9a337f4bc55645c1eae5689ab"


        getPocasie("48.148598", "17.107748")

        return binding.root
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                lat = location.latitude.toString()
                lon = location.longitude.toString()
            }
        }
    }

    public fun getPocasie(lat:String, lon:String){

        url = "https://pro.openweathermap.org/"
        key = "2eb612e9a337f4bc55645c1eae5689ab"

        val retrofitBuilder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(url)
            .build()
            .create(ApiInterface::class.java)

        val retrofitData = retrofitBuilder.getHodinovaPredpoved(lat, lon, key)
        retrofitData.enqueue(object : Callback<HourlyForecast4> {
            override fun onResponse(call: Call<HourlyForecast4>, response: Response<HourlyForecast4>) {

                val responseBody = response.body()!!


                val cas = responseBody.list[0].dt_txt.substring(11, 16)
                binding.date.text = "Today " + cas

                val temp = responseBody.list[0].main.temp - 273.15
                val round = (temp * 10.0).roundToInt() /10.0
                binding.teplota.text = round.toString() + "°C"

                val tlak = responseBody.list[0].main.pressure
                binding.tlak.text = tlak.toString() + " hPa"

                val vlhkost = responseBody.list[0].main.humidity
                binding.vlhkost.text = vlhkost.toString() + " %"

                val vietor = responseBody.list[0].wind.speed * 1.609344
                val round2 = (vietor*100.0).roundToInt() /100.0
                binding.vietok.text = round2.toString() + " km/hod"

                val ikona = responseBody.list[0].weather[0].icon
                val imageurl = "https://pro.openweathermap.org/img/w/10d.png"
                //Picasso.get().load(imageurl).into(binding.image)

                Picasso.get().load("https://pro.openweathermap.org/img/w/"+ ikona +".png").into(binding.image)

                //val context: Context = binding.image.getContext()



                val AktualnePocasieList = ArrayList<AktualnePocasieModel>()
                for (myData in responseBody.list){
                    var r = myData.main.temp - 273.15
                    r = (r * 10.0).roundToInt() /10.0
                    val item= AktualnePocasieModel(myData.dt_txt.substring(11, 16), myData.weather[0].icon, r.toString())
                    AktualnePocasieList.add(item)
                }

                binding.recyclerView.setHasFixedSize(true)
                binding.recyclerView.layoutManager = LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false)
                aktualnePocasieAdapter = AktualnePocasieAdapter(AktualnePocasieList.take(10))
                binding.recyclerView.adapter = aktualnePocasieAdapter


                //val res = responseBody[0].name
                Log.d("AktualneFragment", "Lokacia: $AktualnePocasieList")
                //Toast.makeText(this@NewPoleActivity, "Lokacia: $myString", Toast.LENGTH_LONG).show()
            }

            override fun onFailure(call: Call<HourlyForecast4>, t: Throwable) {
                Log.d("AktualneFragment", "cele zle ")
            }
        })


    }

}