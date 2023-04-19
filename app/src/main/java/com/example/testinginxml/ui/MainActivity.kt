package com.example.testinginxml.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.example.testinginxml.R
import com.example.testinginxml.adapters.ImageAdapter
import com.example.testinginxml.databinding.ActivityMainBinding
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var navControler: NavController


    // needed because injection can not success before on create, so we have to do it like this
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ImageAdapterFactory {
        fun getImageAdapter(): ImageAdapter
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        // retrieves the factory
        val factory = EntryPointAccessors.fromApplication(this, ImageAdapterFactory::class.java)
        val imageAdapter = factory.getImageAdapter()
        supportFragmentManager.fragmentFactory = ShoppingFragmentFactory(imageAdapter)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        // test to solve the crash while in last screen
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navControler = navHostFragment.findNavController()
    }
}