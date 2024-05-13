package com.example.aurora

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aurora.Adapters.SkodceAdapter
import com.example.aurora.Models.PoleModel
import com.example.aurora.Models.SkodecModel
import com.example.aurora.Models.SuradniceModel
import com.example.aurora.databinding.FragmentOkoliteSkodceBinding
import com.example.aurora.databinding.FragmentSkodceBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class OkoliteSkodceFragment : Fragment() {

    private var _binding: FragmentOkoliteSkodceBinding? = null
    private val binding get() = _binding!!

    private lateinit var dbref : DatabaseReference
    private lateinit var dbRef : DatabaseReference
    private lateinit var poleArrayList : ArrayList<PoleModel>
    private lateinit var suradniceArrayList : ArrayList<SuradniceModel>
    private lateinit var skodceArrayList : ArrayList<SkodecModel>
    //private lateinit var sortedList : ArrayList<SkodecModel>
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var poleDataList: ArrayList<SuradniceModel>
    private lateinit var mAdapter : SkodceAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOkoliteSkodceBinding.inflate(inflater, container, false)

        binding.mRecycler.layoutManager = LinearLayoutManager(activity)
        binding.mRecycler.setHasFixedSize(true)

        skodceArrayList = arrayListOf<SkodecModel>()
        poleArrayList = arrayListOf<PoleModel>()
        suradniceArrayList = arrayListOf<SuradniceModel>()
        poleDataList = arrayListOf<SuradniceModel>()
        //sortedList = arrayListOf<SkodecModel>()
        getPoleData()
        getSkodceData()



        binding.button2.setOnClickListener {
            val intent = Intent(activity, NewSkodecActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }

    private fun getSkodceData() {

        firebaseAuth = FirebaseAuth.getInstance()
        dbref = FirebaseDatabase.getInstance().getReference("Skodce")

        dbref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Clear the skodceArrayList before adding new SkodecModel objects
                skodceArrayList.clear()

                if(snapshot.exists()){
                    for (poleSnapshot in snapshot.children){
                        val skodec = poleSnapshot.getValue(SkodecModel::class.java)

                        if (skodec?.userID != firebaseAuth.currentUser?.uid.toString()){
                            for (s in suradniceArrayList){
                                val locationA = Location("a")
                                locationA.latitude = s.sirka!!.toDouble()
                                locationA.longitude = s.dlzka!!.toDouble()
                                val locationB = Location("b")
                                locationB.latitude = skodec?.sirka!!.toDouble()
                                locationB.longitude = skodec?.dlzka!!.toDouble()

                                //vypocitame vzdialenost bodov a ak je to vzdialene do 50km, tak zobrazime
                                if (locationA.distanceTo(locationB).toDouble() < 50000){
                                    if (skodceArrayList.contains(skodec) == false){
                                        skodceArrayList.add(skodec!!)
                                    }
                                }
                            }
                        }
                    }

                    mAdapter = SkodceAdapter(skodceArrayList.reversed())
                    binding.mRecycler.adapter = mAdapter

                    mAdapter.setOnItemClickListener(object : SkodceAdapter.onItemClickListener{
                        override fun onItemClick(position: Int) {
                            val position2 = skodceArrayList.size - 1 - position
                            Log.d("poziciaaaa", "Item clicked: $position  $position2")
                            val intent = Intent(activity, SkodecDetailActivity2::class.java)
                            intent.putExtra("id", skodceArrayList[position2].id)
                            intent.putExtra("nazovSkodca", skodceArrayList[position2].nazovSkodca)
                            intent.putExtra("dlzka", skodceArrayList[position2].dlzka)
                            intent.putExtra("sirka", skodceArrayList[position2].sirka)
                            intent.putExtra("lokalita", skodceArrayList[position2].lokalita)
                            intent.putExtra("popis", skodceArrayList[position2].popis)

                            startActivity(intent)
                        }
                    })
                }
            }
            override fun onCancelled(error: DatabaseError) {
                // Handle the error
            }

        })
    }

    private fun getPoleData() {

        firebaseAuth = FirebaseAuth.getInstance()
        dbRef = FirebaseDatabase.getInstance().getReference("Polia")

        dbRef.addValueEventListener(object : ValueEventListener {
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
                getSkodceData()

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }


}