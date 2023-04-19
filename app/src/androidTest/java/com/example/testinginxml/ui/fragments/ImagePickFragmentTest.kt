package com.example.testinginxml.ui.fragments

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.MediumTest
import com.example.testinginxml.adapters.ImageAdapter
import com.example.testinginxml.launchFragmentInHiltContainer
import com.example.testinginxml.repositories.FakeShoppingRepositoryAndroidTest
import com.example.testinginxml.ui.ShoppingFragmentFactory
import com.example.testinginxml.ui.ShoppingViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.example.testinginxml.R
import com.example.testinginxml.getOrAwaitValue
import com.google.common.truth.Truth.assertThat
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import javax.inject.Inject

// all explained in ShoppingFragmentTest

@MediumTest // we test the interaction between fragments so medium is ok
@HiltAndroidTest // allows hilt usage
@ExperimentalCoroutinesApi
class ImagePickFragmentTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    // our factory we did
    @Inject
    lateinit var fragmentFactory: ShoppingFragmentFactory

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun clickImage_popBackStackAndSetImageUrl() {
        // all our fake dependencies to test
        val navController = mock(NavController::class.java)
        val imageUrl = "TEST"
        val testViewModel = ShoppingViewModel(FakeShoppingRepositoryAndroidTest())

        // we add the navGraphId = R.navigation.nav_graph because it uses navGraphViewModels
        launchFragmentInHiltContainer<ImagePickFragment>(
            fragmentFactory = fragmentFactory,
            navGraphId = R.navigation.nav_graph
        ) {
            Navigation.setViewNavController(requireView(), navController)
            imageAdapter.images = listOf(imageUrl) // we populate our adapter with fake data
            viewModel = testViewModel
            // we do this because we don't want to use the real viewModel which makes real api calls,
            // we use this viewModel with the fake repository to mock our calls
        }

        // we select the recyclerView, we perform an action passing our viewHolder, we provide the
        // position and the action we want to perform
        onView(withId(R.id.rvImages)).perform(
            RecyclerViewActions.actionOnItemAtPosition<ImageAdapter.ImageViewHolder>(
                0,
                click()
            )
        )
        // if we have animations in our device the list may behave in a estrange manner and make the test fail

        // to support clicks on recyclerViews we need this dependency
        //androidTestImplementation 'androidx.test.espresso:espresso-contrib:3.3.0'


        // our onClick listener on the real fragment pops the backstack and sets the url of the image
        // into the viewModel
        verify(navController).popBackStack()
        assertThat(testViewModel.curImageUrl.getOrAwaitValue()).isEqualTo(imageUrl)
    }

}