package com.example.aurora

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
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

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var dbRef: DatabaseReference
    private lateinit var dbRef_polia: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var poleDataList: ArrayList<SuradniceModel>
    private lateinit var poleArrayList : ArrayList<PoleModel>
    private lateinit var suradniceArrayList : ArrayList<SuradniceModel>

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

        val fragmentName = intent.getStringExtra("fragment")
        if (fragmentName != null) {
            when (fragmentName) {
                "Skodce" -> replaceFragment(Skodce())
                // Add other fragments here
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

}