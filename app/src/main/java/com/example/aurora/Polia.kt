package com.example.aurora

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.Intent
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.graphics.component1
import androidx.core.graphics.component2


import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aurora.Adapters.PoleAdapter
import com.example.aurora.Models.PoleModel
import com.example.aurora.databinding.FragmentPoliaBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.time.Duration
import kotlin.math.absoluteValue
import kotlin.system.measureTimeMillis
import kotlin.time.measureTimedValue

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Polia.newInstance] factory method to
 * create an instance of this fragment.
 */
class Polia : Fragment() {
    private var _binding: FragmentPoliaBinding? = null
    private val binding get() = _binding!!
    //private lateinit var binding: FragmentPoliaBinding

    private lateinit var dbref : DatabaseReference

    private lateinit var poleArrayList : ArrayList<PoleModel>
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var mAdapter : PoleAdapter

    private val rotateOpen : Animation by lazy { AnimationUtils.loadAnimation(context, R.anim.rotate_open_anim) }
    private val rotateClose : Animation by lazy { AnimationUtils.loadAnimation(context, R.anim.rotate_close_anim) }
    private val fromBottom : Animation by lazy { AnimationUtils.loadAnimation(context, R.anim.from_bottom_anim) }
    private val toBottom : Animation by lazy { AnimationUtils.loadAnimation(context, R.anim.to_bottom_anim) }
    private var clicked = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {

        _binding = FragmentPoliaBinding.inflate(inflater, container, false)
        dbref = FirebaseDatabase.getInstance().getReference("Polia")


        binding.mRecycler.layoutManager = LinearLayoutManager(activity)
        binding.mRecycler.setHasFixedSize(true)

        poleArrayList = arrayListOf<PoleModel>()
        getPoleData()

        binding.button2.setOnClickListener{
            onButtonClicked()

            //TEST
            //val (result: Unit, duration: kotlin.time.Duration) = measureTimedValue {
            //    test()
            //}
            //println("Test took ${duration} ms.")
        }

        binding.buttonSuradnice.setOnClickListener {
            val intent = Intent(activity , NewPoleActivity::class.java)
            startActivity(intent)
        }
        binding.buttonGPS.setOnClickListener {
            val intent2 = Intent(activity, NewPoleActivity2::class.java)
            startActivity(intent2)
        }
        binding.buttonMapa.setOnClickListener {
            val intent3 = Intent(activity, NewPoleActivity3::class.java)
            startActivity(intent3)
        }

        return binding.root
    }

    private fun onButtonClicked() {
        setVisibility(clicked)
        setAnimation(clicked)
        if (!clicked) clicked = true else clicked = false

    }

    private fun setAnimation(clicked:Boolean) {
        if (!clicked){
            binding.buttonSuradnice.startAnimation(fromBottom)
            binding.buttonGPS.startAnimation(fromBottom)
            binding.buttonMapa.startAnimation(fromBottom)
            binding.button2.startAnimation(rotateOpen)
        }else{
            binding.buttonSuradnice.startAnimation(toBottom)
            binding.buttonGPS.startAnimation(toBottom)
            binding.buttonMapa.startAnimation(toBottom)
            binding.button2.startAnimation(rotateClose)
        }
    }

    private fun setVisibility(clicked:Boolean) {
        if (!clicked){
            binding.buttonSuradnice.visibility = View.VISIBLE
            binding.buttonMapa.visibility = View.VISIBLE
            binding.buttonGPS.visibility = View.VISIBLE
        }else{
            binding.buttonSuradnice.visibility = View.INVISIBLE
            binding.buttonMapa.visibility = View.INVISIBLE
            binding.buttonGPS.visibility = View.INVISIBLE
        }
    }

    private fun getPoleData() {
        firebaseAuth = FirebaseAuth.getInstance()
        dbref = FirebaseDatabase.getInstance().getReference("Polia")
        dbref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    poleArrayList.clear() // Clear the ArrayList before adding new data
                    for (poleSnapshot in snapshot.children){
                        val pole = poleSnapshot.getValue(PoleModel::class.java)
                        if (pole?.userID == firebaseAuth.currentUser?.uid.toString()){
                            poleArrayList.add(pole!!)
                        }
                    }
                    mAdapter = PoleAdapter(poleArrayList)
                    binding.mRecycler.adapter = mAdapter
                    mAdapter.setOnItemClickListener(object : PoleAdapter.onItemClickListener{
                        override fun onItemClick(position: Int) {
                            val intent = Intent(activity, PoleDetailActivity::class.java)
                            intent.putExtra("nazovPola", poleArrayList[position].nazovPola)
                            intent.putExtra("plodina", poleArrayList[position].plodina)
                            intent.putExtra("dlzka", poleArrayList[position].dlzka)
                            intent.putExtra("sirka", poleArrayList[position].sirka)
                            intent.putExtra("rozloha", poleArrayList[position].rozloha)
                            intent.putExtra("id", poleArrayList[position].id)

                            startActivity(intent)
                        }
                    })
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun test(){

        for (i in 1..100){
            val userid = firebaseAuth.currentUser?.uid.toString()
            val empID = dbref.push().key!!
            val pole = PoleModel(empID, userid, "nazovPola", "plodina", "49.083182", "19.612600", "20")
            //getPlace(sirka, dlzka)
            dbref.child(empID).setValue(pole)
        }
    }
}