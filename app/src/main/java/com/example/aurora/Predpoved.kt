package com.example.aurora

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.example.aurora.Adapters.PredpovedAdapter
import com.example.aurora.Models.PoleModel
import com.example.aurora.PredpovedWidgets.AktualneFragment
import com.example.aurora.PredpovedWidgets.DlhodobaFragment
//import com.example.aurora.PredpovedWidgets.HistoriaFragment
import com.example.aurora.PredpovedWidgets.HodinuPoHodineFragment
import com.example.aurora.databinding.FragmentPredpovedBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
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
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var lat: String = "48.148598"
    private var lon: String = "17.107748"

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

        val fragmentAdapter = PredpovedAdapter(requireActivity().supportFragmentManager)
        val aktfragment = AktualneFragment()
        val hphfragment = HodinuPoHodineFragment()
        val dlhFragment = DlhodobaFragment()
        fragmentAdapter.addFragment(aktfragment,"Aktuálna", lat, lon)
        fragmentAdapter.addFragment(hphfragment, "Hodinová", lat, lon)
        fragmentAdapter.addFragment(dlhFragment, "Dlhodobá", lat, lon)
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

        getPoleData(aktfragment, hphfragment, dlhFragment)
        //binding.autoCompleteTextView.setSelection(0)

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
                    binding.autoCompleteTextView.setSelection(0) // Select the first item in the dropdown menu

                    // Set the text of autoCompleteTextView to the name of the first Pole
                    val firstPoleName = poleLokalit[0]
                    binding.autoCompleteTextView.setText(firstPoleName, false)

                    // Fetch the weather data for the first Pole in the dropdown menu
                    val firstPole = poleArrayList[0]
                    lat = firstPole.sirka.toString()
                    lon = firstPole.dlzka.toString()

                    aktualneFragment.getPocasie(lat, lon)
                    hodinuPoHodineFragment.getPocasiee(lat, lon)
                    dlhodobaFragment.getPocasieee(lat, lon)

                    binding.autoCompleteTextView.setOnItemClickListener(OnItemClickListener { parent, view, position, id ->
                        val pole = poleArrayList[position]
                        if (pole != null) {
                            Toast.makeText(activity, pole.nazovPola ?: "", Toast.LENGTH_SHORT).show()
                            lat = pole.sirka.toString()
                            lon = pole.dlzka.toString()

                            aktualneFragment.getPocasie(lat, lon)
                            hodinuPoHodineFragment.getPocasiee(lat, lon)
                            dlhodobaFragment.getPocasieee(lat, lon)
                        }
                    })
                }
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

}