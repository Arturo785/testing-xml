package com.example.testinginxml.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.testinginxml.R
import com.example.testinginxml.adapters.ImageAdapter
import com.example.testinginxml.databinding.FragmentImagePickBinding
import com.example.testinginxml.ui.ShoppingFragmentFactory
import com.example.testinginxml.ui.ShoppingViewModel
import com.example.testinginxml.utils.Constants.GRID_SPAN_COUNT
import com.example.testinginxml.utils.Status
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


// we are able to use constructor injection in fragment thanks to
//ShoppingFragmentFactory
@AndroidEntryPoint
class ImagePickFragment @Inject constructor(
    val imageAdapter: ImageAdapter
) : Fragment(R.layout.fragment_image_pick) {


    private var binding: FragmentImagePickBinding? = null


    // this is made because in our UI test we change the instance of the viewModel for
    // test purposes.
    // The by viewModels<ShoppingViewModel>() does not var but only val properties
    lateinit var viewModel: ShoppingViewModel


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
        binding = FragmentImagePickBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //old way of using viewModels, do not use in real prod
        //viewModel = ViewModelProvider(requireActivity()).get(ShoppingViewModel::class.java)
        viewModel = viewModelInstance // this is done because we re-assign in a UI test
        setupRecyclerView()
        subscribeToObservers()


        binding?.apply {
            etSearch.addTextChangedListener { editable ->
                editable?.let {
                    if (editable.toString().isNotEmpty()) {
                        viewModel.searchForImage(editable.toString())
                    }
                }
            }
        }


        imageAdapter.setOnItemClickListener {
            findNavController().popBackStack()
            viewModel.setCurImageUrl(it)
        }
    }

    private fun subscribeToObservers() {
        viewModel.images.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    Status.SUCCESS -> {
                        val urls = result.data?.hits?.map { imageResult -> imageResult.previewURL }
                        imageAdapter.images = urls ?: listOf()
                        binding?.progressBar?.visibility = View.GONE
                    }
                    Status.ERROR -> {
                        Snackbar.make(
                            requireView(),
                            result.message ?: "An unknown error occured.",
                            Snackbar.LENGTH_LONG
                        ).show()
                        binding?.progressBar?.visibility = View.GONE
                    }
                    Status.LOADING -> {
                        binding?.progressBar?.visibility = View.VISIBLE
                    }
                }
            }
        })
    }

    private fun setupRecyclerView() {
        binding?.apply {
            rvImages.apply {
                adapter = imageAdapter
                layoutManager = GridLayoutManager(requireContext(), GRID_SPAN_COUNT)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}