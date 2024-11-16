package com.protel.medez.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.protel.medez.R
import com.protel.medez.ScanQRPasien
import com.protel.medez.databinding.FragmentHomeBinding
import com.protel.medez.fragments.ProfileFragment
import com.protel.medez.fragments.SchedFragment

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        binding.cekProfilButton.setOnClickListener {
            val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.fragmentContainer, ProfileFragment())  // Replace with the container ID
            fragmentTransaction.addToBackStack(null)  // Optional: To add the fragment to back stack
            fragmentTransaction.commit()
        }

        binding.listObatButton.setOnClickListener {
            val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.fragmentContainer, SchedFragment())  // Replace with the container ID
            fragmentTransaction.addToBackStack(null)  // Optional: To add the fragment to back stack
            fragmentTransaction.commit()
        }

        binding.scanQrButton.setOnClickListener{
            val intent = Intent(requireContext(), ScanQRPasien::class.java)
            startActivity(intent)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clear binding when the view is destroyed
    }
}
