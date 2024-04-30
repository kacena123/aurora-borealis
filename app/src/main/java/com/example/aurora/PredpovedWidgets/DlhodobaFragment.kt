package com.example.aurora.PredpovedWidgets

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aurora.Adapters.Predpoved2Adapter
import com.example.aurora.Adapters.PredpovedDlhodobaAdapter
import com.example.aurora.Interfaces.ApiInterface
import com.example.aurora.Models.DaylyForecast16.DailyForecast16
import com.example.aurora.Models.HourlyForecast4.HodinuPoHodineModel
import com.example.aurora.Models.HourlyForecast4.HourlyForecast4
import com.example.aurora.R
import com.example.aurora.databinding.FragmentDlhodobaBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.math.roundToInt


class DlhodobaFragment : Fragment() {
    private var _binding : FragmentDlhodobaBinding? = null
    private val binding get() = _binding!!

    private lateinit var dbRef: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var url : String
    private lateinit var key : String

    private lateinit var pocasieAdapter : PredpovedDlhodobaAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        if (_binding == null) {
            _binding = FragmentDlhodobaBinding.inflate(inflater, container, false)
        }

        dbRef = FirebaseDatabase.getInstance().getReference("Polia")
        firebaseAuth = FirebaseAuth.getInstance()
        url = "https://pro.openweathermap.org/"
        key = "2eb612e9a337f4bc55645c1eae5689ab"

        getPocasieee("49.087501", "19.656123")

        return binding.root


    }

    public fun getPocasieee(lat:String, lon:String){
        Log.d("Predpoved", "ide daco")

        url = "https://pro.openweathermap.org/"
        key = "2eb612e9a337f4bc55645c1eae5689ab"

        val retrofitBuilderrr = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(url)
            .build()
            .create(ApiInterface::class.java)

        val retrofitDataa = retrofitBuilderrr.getDlhodobaPredpoved(lat, lon, "16", key)
        retrofitDataa.enqueue(object : Callback<DailyForecast16> {
            override fun onResponse(call: Call<DailyForecast16>, response: Response<DailyForecast16>) {
                //write response to log
                Log.d("Dusanko", "${response}")
                val responseBody = response.body()!!

                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm")

                val pocasieList = ArrayList<HodinuPoHodineModel>()

                for (data in responseBody.list){
                    val temp = (data.temp.day - 273.15).roundToInt()
                    val pop = (data.pop * 100).roundToInt()
                    val cas = java.util.Date(data.dt.toLong() * 1000).toString().substring(11, 16)
                    val datum = java.util.Date(data.dt.toLong() * 1000).toString().substring(0,10)
                    val zrazky = data.clouds.toString() + "%"
                    val pravdepodobnost = pop.toString() + "%"
                    val ikona = data.weather[0].icon

                    val item = HodinuPoHodineModel(cas, datum, temp.toString(), pravdepodobnost, zrazky, ikona)
                    pocasieList.add(item)
                }
                //val d = responseBody.list[0].dt
                //val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm")
                //val date = java.util.Date(responseBody.list[0].dt.toLong() * 1000)

                //binding.textView2.text = sdf.format(date)
                Log.d("DlhodobaFragment", "Lokacia: ${pocasieList}")



                _binding?.let { binding ->
                    binding.drecyclerView.layoutManager = LinearLayoutManager(activity)
                    binding.drecyclerView.setHasFixedSize(true)
                    pocasieAdapter = PredpovedDlhodobaAdapter(pocasieList)
                    binding.drecyclerView.adapter = pocasieAdapter
                }


                //val res = responseBody[0].name

                //Toast.makeText(this@NewPoleActivity, "Lokacia: $myString", Toast.LENGTH_LONG).show()
            }

            override fun onFailure(call: Call<DailyForecast16>, t: Throwable) {
                Log.d("Predpoved", "cele zle ")
            }
        })
    }


}