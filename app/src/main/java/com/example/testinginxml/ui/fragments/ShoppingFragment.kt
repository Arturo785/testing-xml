package com.example.testinginxml.ui.fragments


import androidx.recyclerview.widget.ItemTouchHelper.LEFT
import androidx.recyclerview.widget.ItemTouchHelper.RIGHT
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.example.testinginxml.R
import com.example.testinginxml.adapters.ShoppingItemAdapter
import com.example.testinginxml.databinding.FragmentShoppingBinding
import com.example.testinginxml.ui.ShoppingViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class ShoppingFragment @Inject constructor(
    var viewModelPassed: ShoppingViewModel? // hilt does not support setting a default value so it
    // appears only as nullable but without default value
) :
    Fragment(R.layout.fragment_shopping) {

    private var binding: FragmentShoppingBinding? = null


    lateinit var viewModel: ShoppingViewModel

    private lateinit var shoppingItemAdapter: ShoppingItemAdapter

    @Inject
    lateinit var glide: RequestManager

    // shares the same ViewModel along all the navGraph, viewModel lives as long as the navGraph,
    //USE "R.id.your_nav" NOT "R.navigation.your_nav"
    // which leads to error and No destination with ID <destination id> exception

    // the id is defined in    android:id="@+id/nav_graph" in your navGraph, could also use nested ones

    private val viewModelInstance: ShoppingViewModel by navGraphViewModels(R.id.nav_graph) {
        defaultViewModelProviderFactory
    }

    //private val viewModelInstance : ShoppingViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentShoppingBinding.inflate(inflater, container, false)
        return binding?.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //old way of using viewModels, do not use in real prod
        // viewModel = ViewModelProvider(requireActivity()).get(ShoppingViewModel::class.java)
        viewModel = viewModelPassed ?: viewModelInstance
        //viewModel = viewModelInstance
        shoppingItemAdapter = ShoppingItemAdapter(glide)
        setupRecyclerView()


        binding?.apply {
            fabAddShoppingItem.setOnClickListener {
                val action =
                    ShoppingFragmentDirections.actionShoppingFragmentToAddShoppingItemFragment()
                findNavController().navigate(
                    action
                )
            }
        }

        subscribeToObservers()
    }

    //SPOILER IT DID NOT WORK FOR THIS FRAGMENT IN SPECIFIC
    //THIS IS JUST FOR TESTING PURPOSES
    // THIS IS MADE BECAUSE IN onViewCreated THE subscribeToObservers IS SUBSCRIBED TO THE OLD
    // VIEWMODEL, IF WE CHANGE VIEWMODEL IN OUR TESTS THE SUBSCRIBER STILLS USING THE OLD VIEWMODEL
    // SO WE ASSIGN A NEW MODEL AND CALL THE SUBSCRIPTION TO SUBSCRIBE TO NEW TEST VIEWMODEL
    fun setNewViewModel(newViewModel: ShoppingViewModel) {
        viewModel = newViewModel
        subscribeToObservers()
    }

    private fun subscribeToObservers() {
        binding?.apply {
            viewModel.shoppingItems.observe(viewLifecycleOwner) {
                shoppingItemAdapter.shoppingItems = it
            }

            viewModel.totalPrice.observe(viewLifecycleOwner) {
                val price = it ?: 0f
                val priceText = "Total Price: $price€"
                tvShoppingItemPrice.text = priceText
            }
        }

    }

    private fun setupRecyclerView() {
        binding?.apply {
            rvShoppingItems.apply {
                adapter = shoppingItemAdapter
                layoutManager = LinearLayoutManager(requireContext())
                ItemTouchHelper(itemTouchCallback).attachToRecyclerView(this)
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    private val itemTouchCallback = object : ItemTouchHelper.SimpleCallback(
        0, LEFT or RIGHT
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ) = true

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val pos = viewHolder.layoutPosition
            val item = shoppingItemAdapter.shoppingItems[pos]

            viewModel.deleteShoppingItem(item)
            Snackbar.make(requireView(), "Successfully deleted item", Snackbar.LENGTH_LONG).apply {
                setAction("Undo") {
                    viewModel.insertShoppingItemIntoDb(item)
                }
                show()
            }
        }
    }
}