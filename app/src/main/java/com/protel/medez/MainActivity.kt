package com.protel.medez

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.protel.medez.databinding.MainBinding
import com.protel.medez.fragments.HomeFragment
import com.protel.medez.fragments.ProfileFragment
import com.protel.medez.fragments.SchedFragment
import com.qamar.curvedbottomnaviagtion.CurvedBottomNavigation


class MainActivity : AppCompatActivity(){
    private lateinit var binding: MainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()

        val preferences = getSharedPreferences("app_preferences", MODE_PRIVATE)
        val isFirstLaunch = preferences.getBoolean("isFirstLaunch", true)

        if (isFirstLaunch) {
            val intent = Intent(this, LandingPage1::class.java)
            startActivity(intent)
            finish()
        } else {
            setContentView(binding.root)
        }

        val bottomNavigation = findViewById<CurvedBottomNavigation>(R.id.bottomNavigation)
        bottomNavigation.add(
            CurvedBottomNavigation.Model(1,"Homepage", R.drawable.baseline_home_24)
        )
        bottomNavigation.add(
            CurvedBottomNavigation.Model(2,"Jadwal", R.drawable.sched_icon_24)
        )
        bottomNavigation.add(
            CurvedBottomNavigation.Model(3,"Profil", R.drawable.baseline_profile_24)
        )

        bottomNavigation.setOnClickMenuListener {
            when(it.id){
                1 -> {
                    replaceFragment(HomeFragment())
                }

                2-> {
                    replaceFragment(SchedFragment())
                }

                3 -> {
                    replaceFragment(ProfileFragment())
                }
            }
        }
        replaceFragment(HomeFragment())
        bottomNavigation.show(1)
    }

    private fun replaceFragment(fragment : Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainer,fragment)
            .commit()

    }

    private fun initBinding() {
        binding = MainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}