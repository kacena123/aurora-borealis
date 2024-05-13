package com.example.aurora

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import com.example.aurora.Adapters.PoleAdapter
import com.example.aurora.Adapters.PredpovedAdapter
import com.example.aurora.Adapters.SkodceAdapter
import com.example.aurora.Adapters.SkodceTabLayoutAdapter
import com.example.aurora.Models.PoleModel
import com.example.aurora.Models.SkodecModel
import com.example.aurora.Models.SuradniceModel
import com.example.aurora.databinding.FragmentPoliaBinding
import com.example.aurora.databinding.FragmentSkodceBinding
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.IOException
import java.util.Locale

class Skodce : Fragment() {

    private var _binding: FragmentSkodceBinding? = null
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
        _binding = FragmentSkodceBinding.inflate(inflater, container, false)

        var viewPager = binding.AktualneFragment as ViewPager
        var tablayout = binding.tabLayout as TabLayout

        val fragmentAdapter = SkodceTabLayoutAdapter(requireActivity().supportFragmentManager)
        val mojeSkodceFragment = MojeSkodceFragment()
        val okoliteSkodceFragment = OkoliteSkodceFragment()
        fragmentAdapter.addFragment(okoliteSkodceFragment, "Okolité škodce")
        fragmentAdapter.addFragment(mojeSkodceFragment, "Moje škodce")

        viewPager.adapter = fragmentAdapter
        tablayout.setupWithViewPager(viewPager)

        return binding.root
    }

}