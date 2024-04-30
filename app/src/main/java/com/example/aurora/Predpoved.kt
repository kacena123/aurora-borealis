package com.example.aurora

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.example.aurora.Adapters.PredpovedAdapter
import com.example.aurora.Models.PoleModel
import com.example.aurora.PredpovedWidgets.AktualneFragment
import com.example.aurora.PredpovedWidgets.DlhodobaFragment
//import com.example.aurora.PredpovedWidgets.HistoriaFragment
import com.example.aurora.PredpovedWidgets.HodinuPoHodineFragment
import com.example.aurora.databinding.FragmentPredpovedBinding
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class Predpoved : Fragment() {

    private lateinit var dbref : DatabaseReference
    private lateinit var poleArrayList : ArrayList<PoleModel>
    private lateinit var poleLokalit : ArrayList<String>
    private lateinit var firebaseAuth: FirebaseAuth

    private var lat: String = "49.087501"
    private var lon: String = "19.656123"

    private var _binding: FragmentPredpovedBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentPredpovedBinding.inflate(inflater, container, false)


        var viewPager = binding.AktualneFragment as ViewPager
        var tablayout = binding.tabLayout as TabLayout

        //val fragmentManager = (activity).supportFragmentManager


        val fragmentAdapter = PredpovedAdapter(requireActivity().supportFragmentManager)
        val aktfragment = AktualneFragment()
        val hphfragment = HodinuPoHodineFragment()
        val dlhFragment = DlhodobaFragment()
        fragmentAdapter.addFragment(aktfragment,"Current", lat, lon)
        fragmentAdapter.addFragment(hphfragment, "Hourly", lat, lon)
        fragmentAdapter.addFragment(dlhFragment, "Long-term", lat, lon)
        //fragmentAdapter.addFragment(HistoriaFragment(), "História")

        viewPager.adapter = fragmentAdapter
        tablayout.setupWithViewPager(viewPager)

        // V triede Predpoved.kt
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                // Ignorujeme
            }

            override fun onPageSelected(position: Int) {
                // Získame fragment na aktuálnej pozícii
                val fragment = fragmentAdapter.getFragment(position)
                // Získame súradnice z vybraného poľa v dropdown menu
                //val selectedPole = poleArrayList[binding.autoCompleteTextView.selectedItemPosition]
                //val lat = selectedPole.sirka?.toString() ?: ""
                //val lon = selectedPole.dlzka?.toString() ?: ""
                // Zavoláme metódu getPocasie pre daný fragment
                when (fragment) {
                    is AktualneFragment -> fragment.getPocasie(lat, lon)
                    is HodinuPoHodineFragment -> fragment.getPocasiee(lat, lon)
                    is DlhodobaFragment -> fragment.getPocasieee(lat, lon)
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
                // Ignorujeme
            }
        })

        //dropdown menu
        poleArrayList = arrayListOf<PoleModel>()
        poleLokalit = arrayListOf<String>()
        //val aktfragment: AktualneFragment = requireActivity().supportFragmentManager.findFragmentById(R.id.AktualneFragment) as AktualneFragment
        getPoleData(aktfragment, hphfragment, dlhFragment)


        /*
        binding.autoCompleteTextView.setOnItemClickListener{ parent, view, position, id ->
            val selected = parent.getItemAtPosition(position) as PoleModel
            Toast.makeText(activity, selected.nazovPola, Toast.LENGTH_LONG).show()

        }*/
        //android.app.Fragment tt = getFragmentManager().findFragmentById(R.id.AktualneFragment)
        //val fragment: AktualneFragment = supportFragmentManager.findFragmentById(R.id.AktualneFragment) as AktualneFragment

        return binding.root
    }


    private fun getPoleData(aktualneFragment: AktualneFragment, hodinuPoHodineFragment: HodinuPoHodineFragment, dlhodobaFragment: DlhodobaFragment) {

        firebaseAuth = FirebaseAuth.getInstance()
        dbref = FirebaseDatabase.getInstance().getReference("Polia")
        dbref.addValueEventListener(object : ValueEventListener{
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

                    val arrayAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, poleLokalit.toArray())
                    binding.autoCompleteTextView.setAdapter(arrayAdapter)
                    binding.autoCompleteTextView.setOnItemClickListener(OnItemClickListener { parent, view, position, id ->
                        val pole = poleArrayList[position]
                        if (pole != null) {
                            Toast.makeText(activity, pole.nazovPola ?: "", Toast.LENGTH_SHORT).show()
                            lat = pole.sirka.toString()
                            lon = pole.dlzka.toString()

                            aktualneFragment.getPocasie(pole.sirka?.toString() ?: "", pole.dlzka?.toString() ?: "")
                            hodinuPoHodineFragment.getPocasiee(pole.sirka?.toString() ?: "", pole.dlzka?.toString() ?: "")
                            dlhodobaFragment.getPocasieee(pole.sirka?.toString() ?: "", pole.dlzka?.toString() ?: "")
                        }
                    })


                }

            }


            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

}