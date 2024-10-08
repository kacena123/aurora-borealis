package com.example.aurora.PredpovedWidgets

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aurora.Adapters.PoleAdapter
import com.example.aurora.Adapters.Predpoved2Adapter
import com.example.aurora.Interfaces.ApiInterface
import com.example.aurora.Models.HourlyForecast4.HodinuPoHodineModel
import com.example.aurora.Models.HourlyForecast4.HourlyForecast4
import com.example.aurora.databinding.FragmentHodinuPoHodineBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Date
import kotlin.math.roundToInt


class HodinuPoHodineFragment : Fragment() {

    private var _binding: FragmentHodinuPoHodineBinding? = null
    private val binding get() = _binding!!

    private lateinit var dbRef: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var url : String
    private lateinit var key : String

    private lateinit var pocasieAdapter : Predpoved2Adapter

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var lat: String = ""
    private var lon: String = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHodinuPoHodineBinding.inflate(inflater, container, false)

        dbRef = FirebaseDatabase.getInstance().getReference("Polia")
        firebaseAuth = FirebaseAuth.getInstance()
        url = "https://pro.openweathermap.org/"
        key = "2eb612e9a337f4bc55645c1eae5689ab"

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        getCurrentLocation()

        getPocasiee("48.148598", "17.107748")

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

    public fun getPocasiee(lat:String, lon:String){
        url = "https://pro.openweathermap.org/"
        key = "2eb612e9a337f4bc55645c1eae5689ab"
        val retrofitBuilderr = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(url)
            .build()
            .create(ApiInterface::class.java)
        val retrofitDataa = retrofitBuilderr.getHodinovaPredpoved(lat, lon, key)
        retrofitDataa.enqueue(object : Callback<HourlyForecast4> {
            override fun onResponse(call: Call<HourlyForecast4>, response: Response<HourlyForecast4>) {
                val responseBody = response.body()!!
                val pocasieList = ArrayList<HodinuPoHodineModel>()
                for (data in responseBody.list){
                    val temp = (data.main.temp - 273.15).roundToInt()
                    val pop = (data.pop * 100).roundToInt()
                    val cas = data.dt_txt.substring(11, 16)
                    val datum = data.dt_txt.substring(5,10)
                    val zrazky = data.clouds.all.toString() + "%"
                    val pravdepodobnost = pop.toString() + "%"
                    val ikona = data.weather[0].icon
                    val item = HodinuPoHodineModel(cas, datum, temp.toString(), pravdepodobnost, zrazky, ikona)
                    pocasieList.add(item)
                }
                val d = responseBody.list[0].dt
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm")
                val date = java.util.Date(responseBody.list[0].dt.toLong() * 1000)

                binding.hrecyclerView.layoutManager = LinearLayoutManager(activity)
                binding.hrecyclerView.setHasFixedSize(true)
                pocasieAdapter = Predpoved2Adapter(pocasieList)
                binding.hrecyclerView.adapter = pocasieAdapter

                Log.d("AktualneFragment", "Lokacia: ")
            }

            override fun onFailure(call: Call<HourlyForecast4>, t: Throwable) {
                Log.d("AktualneFragment", "Error: ${t.message}")
            }
        })
    }


}