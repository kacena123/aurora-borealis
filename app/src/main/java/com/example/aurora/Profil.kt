package com.example.aurora

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.aurora.Sign.LoginActivity
import com.example.aurora.databinding.FragmentPoliaBinding
import com.example.aurora.databinding.FragmentProfilBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Profil.newInstance] factory method to
 * create an instance of this fragment.
 */
class Profil : Fragment() {
    private var _binding: FragmentProfilBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentProfilBinding.inflate(inflater, container, false)

        binding.meno.text = FirebaseAuth.getInstance().currentUser?.email

        binding.logoutbutton.setOnClickListener{
            FirebaseAuth.getInstance().signOut()

            val intent = Intent(activity , LoginActivity::class.java)
            startActivity(intent)

            Toast.makeText(activity, "Úspešné odhlásenie", Toast.LENGTH_LONG).show()
        }

        binding.zmenitheslobutton.setOnClickListener {
            val intent = Intent(activity, ChangePasswordActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }


}