package com.example.aurora

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aurora.Adapters.Predpoved2Adapter
import com.example.aurora.Interfaces.ApiInterface
import com.example.aurora.Models.HourlyForecast4.HodinuPoHodineModel
import com.example.aurora.Models.HourlyForecast4.HourlyForecast4
import com.example.aurora.Models.NotifikacieModel
import com.example.aurora.Models.PoleModel
import com.example.aurora.Models.SkodecModel
import com.example.aurora.Models.SuradniceModel
import com.example.aurora.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
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
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var dbRef: DatabaseReference
    private lateinit var dbRef_polia: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var poleDataList: ArrayList<SuradniceModel>
    private lateinit var poleArrayList : ArrayList<PoleModel>
    private lateinit var suradniceArrayList : ArrayList<SuradniceModel>
    private lateinit var url : String
    private lateinit var key : String

    private var skodecCount: Int = 0
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        poleArrayList = arrayListOf<PoleModel>()
        suradniceArrayList = arrayListOf<SuradniceModel>()
        poleDataList = arrayListOf<SuradniceModel>()

        url = "https://pro.openweathermap.org/"
        key = "2eb612e9a337f4bc55645c1eae5689ab"

        val fragmentName = intent.getStringExtra("fragment")
        if (fragmentName != null) {
            when (fragmentName) {
                "Skodce" -> replaceFragment(Skodce())
                "Predpoved" -> replaceFragment(Predpoved())
                "Profil" -> replaceFragment(Profil())
                else -> replaceFragment(Polia())
            }
        } else {
            replaceFragment(Polia())
        }
        if (intent.getStringExtra("fragment") == "Skodce") {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame_layout, Skodce())
            binding.bottomNavigationView.setSelectedItemId(R.id.skodce)
            transaction.commit()
        }
        if (intent.getStringExtra("fragment") == "Predpoved") {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame_layout, Predpoved())
            binding.bottomNavigationView.setSelectedItemId(R.id.predpoved)
            transaction.commit()
        }
        if (intent.getStringExtra("fragment") == "Profil") {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame_layout, Profil())
            binding.bottomNavigationView.setSelectedItemId(R.id.profil)
            transaction.commit()
        }

        binding.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.polia -> replaceFragment(Polia())
                R.id.predpoved -> replaceFragment(Predpoved())
                R.id.skodce -> replaceFragment(Skodce())
                R.id.profil -> replaceFragment(Profil())

                else ->{}
            }
            true
        }

        //__________SLEDOVANIE, CI SA NEPRIDAL NOVY SKODEC__________

        dbRef = FirebaseDatabase.getInstance().getReference("Skodce")

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE)

        // Retrieve skodecCount from SharedPreferences
        skodecCount = sharedPreferences.getInt("skodecCount", 0)

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val newCount = dataSnapshot.childrenCount.toInt()
                    if (newCount > skodecCount) {
                        // A new skodec has been added
                        skodecCount = newCount

                        // Save skodecCount to SharedPreferences
                        val editor: SharedPreferences.Editor = sharedPreferences.edit()
                        editor.putInt("skodecCount", skodecCount)
                        editor.apply()

                        // Get the new skodec
                        val newSkodec = dataSnapshot.children.last().getValue(SkodecModel::class.java)

                        // Check if the new skodec was added by the current user
                        if (newSkodec?.userID != firebaseAuth.currentUser?.uid.toString()) {

                            // Call getPoleData and pass a callback function
                            getPoleData { poleDataList ->
                                for (field in poleDataList) {
                                    val locationA = Location("a")
                                    locationA.latitude = field.sirka!!.toDouble()
                                    locationA.longitude = field.dlzka!!.toDouble()

                                    val locationB = Location("b")
                                    locationB.latitude = newSkodec?.sirka!!.toDouble()
                                    locationB.longitude = newSkodec?.dlzka!!.toDouble()

                                    // Calculate the distance between the new skodec and the field
                                    if (locationA.distanceTo(locationB).toDouble() < 50000) {
                                        // The new skodec is within 50km of the field, create a notification
                                        createNotificationSkodec("Bol pridaný nový skodec vo vzdialenosti 50 km od vašich polí.", newSkodec?.nazovSkodca.toString())
                                        break
                                    }
                                }
                            }
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors
                Log.e("Firebase", "Error: ${databaseError.message}")
            }
        })

        /*
        // List pre notifikácie
        val notificationsList = mutableListOf<NotifikacieModel>()

        // Načítajte všetky notifikácie používateľa
        val dbRef = FirebaseDatabase.getInstance().getReference("Notifikacie")
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Prejdite všetky notifikácie
                    for (notificationSnapshot in dataSnapshot.children) {
                        val notification = notificationSnapshot.getValue(NotifikacieModel::class.java)
                        if (notification?.userID == firebaseAuth.currentUser?.uid.toString()) {
                            // Pridajte notifikáciu do listu
                            notificationsList.add(notification)

                            // Získajte hodnoty x a y
                            val x = notification?.hodiny!!.toInt()
                            val y = notification?.teplota

                            Log.d("Notifikacie", "x: $x, y: $y")

                            // Získajte predpovedané teplotné dáta pre nasledujúcich x hodín
                            getPocasie(notification.sirka.toString(), notification.dlzka.toString()) { pocasieList ->
                                val temp = pocasieList[x].teplota.toInt()
                                Log.d("Notifikacie", "Teplota: $temp")
                                Log.d("Notifikacie", "$pocasieList")
                                if (temp <= y!!.toInt()) {
                                    createNotificationTeplota("Teplota klesne pod $y°C v nasledujúcich $x hodinách.", notification.nazovPola.toString(), y.toString())
                                }
                            }
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors
                Log.e("Firebase", "Error: ${databaseError.message}")
            }
        })*/

        //_____________NOTIFIKACIE NA POCASIE_____________
        // Vytvorenie Handler
        val handler = Handler(Looper.getMainLooper())

        // Vytvorenie Runnable
        val runnableCode = object : Runnable {
            override fun run() {
                // List pre notifikácie
                val notificationsList = mutableListOf<NotifikacieModel>()

                // Načítanie všetkých notifikácií používateľa
                val dbRef = FirebaseDatabase.getInstance().getReference("Notifikacie")
                dbRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // Prejdite všetky notifikácie
                            for (notificationSnapshot in dataSnapshot.children) {
                                val notification = notificationSnapshot.getValue(NotifikacieModel::class.java)
                                if (notification?.userID == firebaseAuth.currentUser?.uid.toString()) {
                                    // Pridajte notifikáciu do listu
                                    notificationsList.add(notification)

                                    // Získajte hodnoty x a y
                                    val x = notification?.hodiny!!.toInt()
                                    val y = notification?.teplota

                                    // Získanie predpovede teploty
                                    getPocasie(notification.sirka.toString(), notification.dlzka.toString()) { pocasieList ->
                                        val temp = pocasieList[x].teplota.toInt()
                                        if (temp <= y!!.toInt()) {
                                            createNotificationTeplota("Teplota klesne pod $y°C v nasledujúcich $x hodinách.", notification.nazovPola.toString(), y.toString())
                                        }
                                    }
                                }
                            }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle possible errors
                        Log.e("Firebase", "Error: ${databaseError.message}")
                    }
                })

                // Naplánovanie Runnable na spustenie znova o hodinu
                handler.postDelayed(this, 60 * 60 * 1000)
            }
        }

        // Spustenie Runnable
        handler.post(runnableCode)

    }

    private fun replaceFragment(fragment: Fragment){
        val fragmnetManager = supportFragmentManager
        val fragmentTransaction = fragmnetManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }

    private fun getPoleData(callback: (ArrayList<SuradniceModel>) -> Unit) {
        firebaseAuth = FirebaseAuth.getInstance()
        dbRef_polia = FirebaseDatabase.getInstance().getReference("Polia")

        dbRef_polia.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for (poleSnapshot in snapshot.children){
                        val pole = poleSnapshot.getValue(PoleModel::class.java)
                        if (pole?.userID == firebaseAuth.currentUser?.uid.toString()){
                            poleArrayList.add(pole!!)
                            val sirka = pole.sirka
                            val dlzka = pole.dlzka
                            val suradnice = SuradniceModel(sirka, dlzka)
                            suradniceArrayList.add(suradnice)
                        }
                    }
                }
                poleDataList = suradniceArrayList
                callback(poleDataList) // Call the callback function when getPoleData is completed
            }
            override fun onCancelled(error: DatabaseError) {
                // Handle possible errors
                Log.e("Firebase", "Error: ${error.message}")
            }
        })
    }


    private fun createNotificationSkodec(message: String, skodecName: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationChannelId = "skodec_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(notificationChannelId, "Skodec Notifications", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val notificationBuilder = NotificationCompat.Builder(this, notificationChannelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Nový škodec: $skodecName")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        notificationManager.notify(0, notificationBuilder.build())
    }

    fun createNotificationTeplota(message: String, poleName: String, temp: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationChannelId = "temperature_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(notificationChannelId, "Upozornenie na teplotu", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val notificationBuilder = NotificationCompat.Builder(this, notificationChannelId)
            .setSmallIcon(R.drawable.baseline_ac_unit_24)
            .setContentTitle("Pozor na teplotu v poli: $poleName")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        notificationManager.notify(0, notificationBuilder.build())
    }

    private fun getPocasie(lat: String, lon: String, callback: (ArrayList<HodinuPoHodineModel>) -> Unit) {
        val retrofitBuilderr = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(url)
            .build()
            .create(ApiInterface::class.java)

        val pocasieList = ArrayList<HodinuPoHodineModel>()

        val retrofitDataa = retrofitBuilderr.getHodinovaPredpoved(lat, lon, key)
        retrofitDataa.enqueue(object : Callback<HourlyForecast4> {
            override fun onResponse(call: Call<HourlyForecast4>, response: Response<HourlyForecast4>) {

                val responseBody = response.body()!!

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
                callback(pocasieList)
            }

            override fun onFailure(call: Call<HourlyForecast4>, t: Throwable) {
                Log.d("AktualneFragment", "cele zle ")
            }
        })
    }

}