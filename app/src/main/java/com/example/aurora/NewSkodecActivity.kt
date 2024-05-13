package com.example.aurora

import android.content.Intent
import android.location.Geocoder
import android.location.Address
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.aurora.Adapters.PoleAdapter
import com.example.aurora.Interfaces.ApiInterface
import com.example.aurora.Models.LocationItem
import com.example.aurora.Models.PoleModel
import com.example.aurora.Models.SkodecModel
import com.example.aurora.databinding.ActivityNewPoleBinding
import com.example.aurora.databinding.ActivityNewSkodecBinding
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
import org.threeten.bp.LocalDateTime
import java.util.Locale
import android.content.Context
import java.io.IOException
import kotlin.time.measureTimedValue
import android.view.View
import com.google.android.gms.tasks.Task
import com.google.firebase.database.ServerValue
import java.text.SimpleDateFormat
import java.util.Date

class NewSkodecActivity : AppCompatActivity() {

    private val MAX_FIELDS_PER_DAY = 50

    private lateinit var binding: ActivityNewSkodecBinding
    private lateinit var dbRef: DatabaseReference
    private lateinit var dbref: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var url : String
    private lateinit var key : String

    private lateinit var poleArrayList : ArrayList<PoleModel>
    private lateinit var poleLokalit : ArrayList<String>

    private lateinit var geocoder: Geocoder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNewSkodecBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbref = FirebaseDatabase.getInstance().getReference("Skodce")
        firebaseAuth = FirebaseAuth.getInstance()
        url = "https://pro.openweathermap.org/"
        key = "2eb612e9a337f4bc55645c1eae5689ab"

        geocoder = Geocoder(this, Locale.getDefault())


        //dropdown menu
        poleArrayList = arrayListOf<PoleModel>()
        poleLokalit = arrayListOf<String>()
        getPoleData()

        binding.autoCompleteTextView.setOnItemClickListener(AdapterView.OnItemClickListener { parent, view, position, id ->
            val pole = poleArrayList[position]
            Toast.makeText(applicationContext, pole.nazovPola, Toast.LENGTH_SHORT).show()

        })
        binding.button.setOnClickListener{
            saveData()
            /* TEST 2
            val (result2: Unit, duration2: kotlin.time.Duration) = measureTimedValue {
                Test2()
            }
            println("Test2 took ${duration2} ms.")
            val (result3: Unit, duration3: kotlin.time.Duration) = measureTimedValue {
                Test3()
            }
            println("Test3 toooook ${duration3} ms.")
             */
        }

    }

    private fun saveData(){
        val nazovSkodca = binding.nazovSkodcaEt.text.toString()

        //val actv = AutoCompleteTextView(applicationContext)
        //val position = actv.verticalScrollbarPosition
        //val pole_position = binding.autoCompleteTextView.verticalScrollbarPosition
        //val pole = poleArrayList[pole_position]

        val nazovPola = binding.autoCompleteTextView.text
        //val sirka = pole.sirka.toString()
        //val dlzka = pole.dlzka.toString()

        val p = poleArrayList.indexOfFirst({it.nazovPola.toString() == nazovPola.toString()})
        val pole = poleArrayList[p]

        val dlzka = pole.dlzka.toString()
        val sirka = pole.sirka.toString()


        Log.d("NewSkodecActivity", "nazovPola: $nazovPola, sirka: $sirka, dlzka: $dlzka")


        val popis = binding.popisET.text.toString()

        if (nazovSkodca.isEmpty()){
            binding.nazovSkodcaEt.error = "Zadajte nazov pola"
        }
        if (nazovPola.isEmpty()){
            binding.autoCompleteTextView.error = "Zadajte lokalitu"
        }
        val userid = firebaseAuth.currentUser?.uid.toString()
        val empID = dbRef.push().key!!
        var userName = firebaseAuth.currentUser?.email.toString()
        userName = userName.substringBefore("@")


        //val lokalita = geocoder.getFromLocation(dlzka.toDouble(), sirka.toDouble(), 1)
        val lok = getAddressFromLatLng(this, sirka.toDouble(), dlzka.toDouble())


        Log.d("NewSkodecActivity", "sirka: $sirka, lokalita: ${lok.toString()}")

        val skodec = SkodecModel(empID, userid, userName, nazovSkodca, sirka, dlzka, lok.toString(), popis)

        getCurrentDateFirebase().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val date = task.result
                val userDailyLimitRef = FirebaseDatabase.getInstance().getReference("UserDailyLimits").child(userid).child(date)

                userDailyLimitRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val count = dataSnapshot.getValue(Int::class.java) ?: 0
                        if (count >= MAX_FIELDS_PER_DAY) {
                            Toast.makeText(applicationContext, "Dosiahli ste maximálny počet vložení do databázy za deň", Toast.LENGTH_LONG).show()
                        } else {
                            userDailyLimitRef.setValue(count + 1)
                            saveSkodecToDatabase(empID, skodec)
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle possible errors.
                    }
                })
            } else {
                // Handle possible errors.
            }
        }
    }



    private fun getPoleData() {

        firebaseAuth = FirebaseAuth.getInstance()
        dbRef = FirebaseDatabase.getInstance().getReference("Polia")
        dbRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for (poleSnapshot in snapshot.children){
                        val pole = poleSnapshot.getValue(PoleModel::class.java)
                        if (pole?.userID == firebaseAuth.currentUser?.uid.toString()){
                            poleArrayList.add(pole!!)
                        }

                    }
                    //val poleLokalit = ArrayList<String>()
                    for (i in poleArrayList){
                        poleLokalit.add(i.nazovPola.toString())
                    }

                    val arrayAdapter = ArrayAdapter(applicationContext, R.layout.dropdown_item, poleLokalit.toArray())
                    binding.autoCompleteTextView.setAdapter(arrayAdapter)

                }

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
    fun getAddressFromLatLng(context: Context, latitude: Double, longitude: Double): String? {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses: List<Address>?
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1)
        } catch (ioException: IOException) {
            // IOException occurred when querying the geocoder service.
            ioException.printStackTrace()
            return null
        }
        if (addresses != null && addresses.isNotEmpty()) {
            val address: Address = addresses[0]
            val addressLine = address.getAddressLine(0)
            val city = address.locality
            return city
        }
        return null
    }


    private fun saveSkodecToDatabase(empID: String, skodec: SkodecModel) {
        dbref.child(empID).setValue(skodec)
            .addOnCompleteListener{
                Toast.makeText(this, "Data boli vlozene", Toast.LENGTH_LONG).show()

                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("fragment", "Skodce")
                startActivity(intent)
                finish()

            }.addOnFailureListener { err ->
                Toast.makeText(this, "Error ${err.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun getCurrentDateFirebase(): Task<String> {
        val ref = FirebaseDatabase.getInstance().getReference("Timestamp")
        return ref.setValue(ServerValue.TIMESTAMP)
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    throw task.exception!!
                }
                ref.get()
            }.continueWith { task ->
                val timestamp = task.result?.value as Long
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                sdf.format(Date(timestamp))
            }
    }

    /*
    //TEST 2
    private fun Test2 (){
        val dlzka = "48.148598"
        val sirka = "17.10674"
        val userid = firebaseAuth.currentUser?.uid.toString()
        val empID = dbRef.push().key!!
        var userName = firebaseAuth.currentUser?.email.toString()
        userName = userName.substringBefore("@")
        val lok = getAddressFromLatLng(this, dlzka.toDouble(), sirka.toDouble())
        val skodec = SkodecModel(userid, userName, "nazovSkodca", dlzka, sirka, lok.toString(), "popis")

        dbref.child(empID).setValue(skodec)
    }
    //TEST 3
    private fun Test3 (){
        Toast.makeText(this, "Data boli vlozene", Toast.LENGTH_LONG).show()
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("fragment", "Skodce")
        startActivity(intent)
    }

     */
}