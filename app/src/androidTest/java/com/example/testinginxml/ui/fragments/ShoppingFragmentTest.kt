package com.example.testinginxml.ui.fragments

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.action.ViewActions.swipeRight
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.filters.MediumTest
import com.example.testinginxml.getOrAwaitValue
import com.example.testinginxml.launchFragmentInHiltContainer
import com.example.testinginxml.repositories.FakeShoppingRepositoryAndroidTest
import com.example.testinginxml.ui.ShoppingViewModel
import com.google.common.truth.Truth
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.example.testinginxml.R
import com.example.testinginxml.adapters.ShoppingItemAdapter
import com.example.testinginxml.data.local.ShoppingItem
import com.google.common.truth.Truth.assertThat
import org.mockito.Mockito.contains
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import javax.inject.Inject


@MediumTest // we test the interaction between fragments so medium is ok
@HiltAndroidTest // allows hilt usage
@ExperimentalCoroutinesApi
class ShoppingFragmentTest {


    // to make the live data synchronous
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    // we don't use it but is in here for reference how is used
    @Inject
    lateinit var testFragmentFactory: TestShoppingFragmentFactory

    @Before
    fun setup() {
        hiltRule.inject()
    }


    // for this fragment I had to use the testFragmentFactory because the change of viewModel
    // with the method that changed the viewModel didn't work because the instantion of the
    // observers always reaches first before the method does the thing
    // fun setNewViewModel(newViewModel: ShoppingViewModel) { DIDN'T WORK FOR THIS FRAGMENT

    @Test
    fun swipeShoppingItem_deleteItemInDb() {
        val shoppingItem = ShoppingItem("TEST", 1, 1f, "TEST", 1)
        var testViewModel: ShoppingViewModel? = null
        launchFragmentInHiltContainer<ShoppingFragment>(
            navGraphId = R.navigation.nav_graph,
            fragmentFactory = testFragmentFactory // the factory we created in this directory and gets injected
        ) {
            testViewModel = viewModel
            viewModel.insertShoppingItemIntoDb(shoppingItem)
        }

        onView(withId(R.id.rvShoppingItems)).perform(
            RecyclerViewActions.actionOnItemAtPosition<ShoppingItemAdapter.ShoppingItemViewHolder>(
                0,
                swipeLeft()
            )
        )

        assertThat(testViewModel?.shoppingItems?.getOrAwaitValue()).isEmpty()
    }

    @Test
    fun clickAddShoppingItemButton_navigateToAddShoppingItemFragment() {
        val navController = mock(NavController::class.java) // we mock our navController

        // we launch our activity container we did and the fragment to appear on top is
        //ShoppingFragment
        // we add the navGraphId = R.navigation.nav_graph because it uses navGraphViewModels
        launchFragmentInHiltContainer<ShoppingFragment>(
            navGraphId = R.navigation.nav_graph,
            fragmentFactory = testFragmentFactory
        ) {
            // we set our navigation inside the scope of the fragment
            Navigation.setViewNavController(requireView(), navController)
        }

        // using expresso we perform actions on our UI
        onView(withId(R.id.fabAddShoppingItem)).perform(click())

        // we want to verify that our mocked navigation gets called with those exacts parameters
        // the action is our navigation action we specified in the
        // nav_graph
        val actionNav = ShoppingFragmentDirections.actionShoppingFragmentToAddShoppingItemFragment()
        verify(navController).navigate(
            actionNav
        )

        // in this examples we also can verify for a number of times at least or explicit only a
        // set of times
        //verify(navController, atLeast(2)).navigate(actionNav)
        //verify(navController, times(2)).navigate(actionNav)
    }


    // TEST FORM IN CASE WE USE CONSTRUCTOR INJECTION IN OUR FRAGMENTS
    //https://github.com/philipplackner/ShoppingListTestingYT/blob/TestItemDeletion/app/src/androidTest/java/com/androiddevs/shoppinglisttestingyt/ui/ShoppingFragmentTest.kt

    // this is if we create our fragment with this parameters

    /*@AndroidEntryPoint
    class ShoppingFragment @Inject constructor(
    val shoppingItemAdapter: ShoppingItemAdapter,
    var viewModel: ShoppingViewModel? = null
) : Fragment(R.layout.fragment_shopping) {*/
    //https://github.com/philipplackner/ShoppingListTestingYT/blob/TestItemDeletion/app/src/main/java/com/androiddevs/shoppinglisttestingyt/ui/ShoppingFragment.kt

    // the factory is in this same folder as reference

/*    @Test
    fun clickAddShoppingItemButton_navigateToAddShoppingItemFragmentWithFactory() {
        val navController = mock(NavController::class.java)

        launchFragmentInHiltContainer<ShoppingFragment>(
            fragmentFactory = testFragmentFactory
        ) {
            Navigation.setViewNavController(requireView(), navController)
        }

        onView(withId(R.id.fabAddShoppingItem)).perform(click())

        verify(navController).navigate(
            ShoppingFragmentDirections.actionShoppingFragmentToAddShoppingItemFragment()
        )
    }*/
}