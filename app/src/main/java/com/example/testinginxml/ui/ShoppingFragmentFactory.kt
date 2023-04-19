package com.example.testinginxml.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.example.testinginxml.adapters.ImageAdapter
import com.example.testinginxml.ui.fragments.ImagePickFragment
import com.example.testinginxml.ui.fragments.ShoppingFragment
import javax.inject.Inject

// this fragment factory is used to be able to construct our own fragments when we want to apply
// constructor injection to them
class ShoppingFragmentFactory @Inject constructor(
    private val imageAdapter: ImageAdapter
) : FragmentFactory() { // we need to inherit from fragmentFactory

    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when (className) {
            // here we choose what to inject and what to inject depending on the fragment we need
            ImagePickFragment::class.java.name -> ImagePickFragment(imageAdapter) // on ImagePick we inject imageAdapter
            // in here provides null because the viewModel gets provided by delegates
            // in TestShoppingFragmentFactory uses a viewModel with the fake repository
            ShoppingFragment::class.java.name -> ShoppingFragment(null)
            else -> super.instantiate(
                classLoader,
                className
            ) // other fragments just create them normal
        }
    }
}

// OUR FACTORY COULD LOOK LIKE THIS IF WE USED THE CONSTRUCTOR INJECTION IN EVERY TEST
/*class ShoppingFragmentFactory @Inject constructor(
    private val imageAdapter: ImageAdapter,
    private val glide: RequestManager,
    private val shoppingItemAdapter: ShoppingItemAdapter
): FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when(className) {
            ImagePickFragment::class.java.name -> ImagePickFragment(imageAdapter)
            AddShoppingItemFragment::class.java.name -> AddShoppingItemFragment(glide)
            ShoppingFragment::class.java.name -> ShoppingFragment(shoppingItemAdapter)
            else -> super.instantiate(classLoader, className)
        }
    }
}*/

// THIS FRAGMENT WOULD LOOK LIKE THIS BECAUSE WE WANT TO INJECT A VIEWMODEL IN OUR TESTS, BUT FOR REAL
// CODE WE WANT TO HAVE THE ONE PROVIDED FOR OUR DELEGATE

/*@AndroidEntryPoint
class ShoppingFragment @Inject constructor(
    val shoppingItemAdapter: ShoppingItemAdapter,
    var viewModel: ShoppingViewModel? = null
) : Fragment(R.layout.fragment_shopping) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = viewModel ?: ViewModelProvider(requireActivity()).get(ShoppingViewModel::class.java)
        subscribeToObservers()
        setupRecyclerView()

        fabAddShoppingItem.setOnClickListener {
            findNavController().navigate(
                ShoppingFragmentDirections.actionShoppingFragmentToAddShoppingItemFragment()
            )
        }
    }*/