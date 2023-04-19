package com.example.testinginxml.ui.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.bumptech.glide.RequestManager
import com.example.testinginxml.adapters.ImageAdapter
import com.example.testinginxml.adapters.ShoppingItemAdapter
import com.example.testinginxml.repositories.FakeShoppingRepositoryAndroidTest
import com.example.testinginxml.ui.ShoppingViewModel
import javax.inject.Inject

// IN CASE WE USE CONSTRUCTOR INJECTION, IN OUR CASE NOT
//https://github.com/philipplackner/ShoppingListTestingYT/blob/TestItemDeletion/app/src/androidTest/java/com/androiddevs/shoppinglisttestingyt/ui/ShoppingFragmentTest.kt

class TestShoppingFragmentFactory @Inject constructor(
    private val imageAdapter: ImageAdapter,
    private val glide: RequestManager,
    private val shoppingItemAdapter: ShoppingItemAdapter
) : FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when (className) {
            ImagePickFragment::class.java.name -> ImagePickFragment(imageAdapter)
            // this two are like in the phillip tutorial, in our case we applied field injection
            // instead of constructor injection with factory
            //AddShoppingItemFragment::class.java.name -> AddShoppingItemFragment(glide)
            // in here provides the fragment with the viewModel with fake repo
            // in real app provides null because the viewModel gets provided by delegates
            ShoppingFragment::class.java.name -> ShoppingFragment(
                ShoppingViewModel(FakeShoppingRepositoryAndroidTest())
            )
            else -> super.instantiate(classLoader, className)
        }
    }
}