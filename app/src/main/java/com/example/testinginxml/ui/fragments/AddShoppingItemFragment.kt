package com.example.testinginxml.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.bumptech.glide.RequestManager
import com.example.testinginxml.R
import com.example.testinginxml.databinding.FragmentAddShoppingItemBinding
import com.example.testinginxml.ui.ShoppingViewModel
import com.example.testinginxml.utils.Status
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AddShoppingItemFragment : Fragment(R.layout.fragment_add_shopping_item) {

    private var binding: FragmentAddShoppingItemBinding? = null

    lateinit var callback: OnBackPressedCallback

    // we could use the fragment factory or in this case use field injection
    // for learning purposes we use field injection in here
    @Inject
    lateinit var glide: RequestManager

    // this is made because in our UI test we change the instance of the viewModel for
    // test purposes.
    // The by viewModels<ShoppingViewModel>() does not var but only val properties
    lateinit var viewModel : ShoppingViewModel

    // shares the same ViewModel along all the navGraph, viewModel lives as long as the navGraph,
    //USE "R.id.your_nav" NOT "R.navigation.your_nav"
    // which leads to error and No destination with ID <destination id> exception

    // the id is defined in    android:id="@+id/nav_graph" in your navGraph, could also use nested ones
    private val viewModelInstance: ShoppingViewModel by navGraphViewModels(R.id.nav_graph) {
        defaultViewModelProviderFactory
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddShoppingItemBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //old way of using viewModels, do not use in real prod
        // viewModel = ViewModelProvider(requireActivity()).get(ShoppingViewModel::class.java)

        viewModel = viewModelInstance

        subscribeToObservers()

        binding?.apply {
            ivShoppingImage.setOnClickListener {
                findNavController().navigate(
                    AddShoppingItemFragmentDirections.actionAddShoppingItemFragmentToImagePickFragment()
                )
            }

            btnAddShoppingItem.setOnClickListener {
                viewModel.insertShoppingItem(
                    name = etShoppingItemName.text.toString(),
                    amountString = etShoppingItemAmount.text.toString(),
                    priceString = etShoppingItemPrice.text.toString(),
                )
            }
        }

        // this callback is not lifecycle aware so we have to remove it on the onDestroyView
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                viewModel.setCurImageUrl("") // resets the state of the selected image on the viewModel
                findNavController().popBackStack()

            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(callback)
    }

    //THIS IS JUST FOR TESTING PURPOSES
    // THIS IS MADE BECAUSE IN onViewCreated THE subscribeToObservers IS SUBSCRIBED TO THE OLD
    // VIEWMODEL, IF WE CHANGE VIEWMODEL IN OUR TESTS THE SUBSCRIBER STILLS USING THE OLD VIEWMODEL
    // SO WE ASSIGN A NEW MODEL AND CALL THE SUBSCRIPTION TO SUBSCRIBE TO NEW TEST VIEWMODEL
    fun setNewViewModel(newViewModel: ShoppingViewModel){
        viewModel = newViewModel
        subscribeToObservers()
    }

    private fun subscribeToObservers() {
        viewModel.curImageUrl.observe(viewLifecycleOwner) { url ->
            binding?.ivShoppingImage?.let { glide.load(url).into(it) }
        }

        viewModel.insertShoppingItemStatus.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    Status.SUCCESS -> {
                        Snackbar.make(
                            requireView(),
                            "Added shopping item",
                            Snackbar.LENGTH_LONG
                        ).show()
                        findNavController().popBackStack()
                    }
                    Status.ERROR -> {
                        Snackbar.make(
                            requireView(),
                            result.message ?: "Something went wrong",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                    Status.LOADING -> {
                        // NO OP
                    }
                }
            }
        }
    }

    // we have to remove it because otherwise when the fragment is  no longer available the behaviour remains
    // because this is not lifecycle aware and makes the navigator to crash when no more screens available to backstack remove
    override fun onDestroyView() {
        super.onDestroyView()
        callback.isEnabled = false
        callback.remove()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}