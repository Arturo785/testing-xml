package com.example.testinginxml.ui.fragments

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
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
import org.mockito.Mockito.contains
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

// all explained in ShoppingFragmentTest

@MediumTest
@HiltAndroidTest
@ExperimentalCoroutinesApi
class AddShoppingItemFragmentTest {

    // to make the live data synchronous
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun pressBackButton_popBackStack() {
        // we create a fragment to hold the reference
        var fragmentTest: AddShoppingItemFragment? = null
        val navController = mock(NavController::class.java)
        val expectedURL = "test_url"

        // we add the navGraphId = R.navigation.nav_graph because it uses navGraphViewModels
        launchFragmentInHiltContainer<AddShoppingItemFragment>(navGraphId = R.navigation.nav_graph) {
            fragmentTest = this // we assign the reference of the given fragment
            Navigation.setViewNavController(requireView(), navController)
            viewModel.setCurImageUrl(expectedURL)
        }

        Truth.assertThat(fragmentTest?.viewModel?.curImageUrl?.getOrAwaitValue())
            .contains(expectedURL)

        pressBack() // there is a callback in the AddShoppingItemFragment that is supposed to clean the url

        // verify that was cleaned
        Truth.assertThat(fragmentTest?.viewModel?.curImageUrl?.getOrAwaitValue()).isEmpty()

        // verify that the fun was called
        verify(navController).popBackStack()
    }

    @Test
    fun clickInsertIntoDb_shoppingItemInsertedIntoDb() {
        val testViewModel = ShoppingViewModel(FakeShoppingRepositoryAndroidTest())
        // we add the navGraphId = R.navigation.nav_graph because it uses navGraphViewModels
        launchFragmentInHiltContainer<AddShoppingItemFragment>(navGraphId = R.navigation.nav_graph) {
            viewModel = testViewModel
        }
        val itemName = "ShoppingItem"
        val itemPrice = "5"
        val itemAmount = "1"

        Truth.assertThat(testViewModel.shoppingItems.getOrAwaitValue()).isEmpty()

        onView(withId(R.id.etShoppingItemName)).perform(replaceText(itemName))
        onView(withId(R.id.etShoppingItemPrice)).perform(replaceText(itemPrice))
        onView(withId(R.id.etShoppingItemAmount)).perform(replaceText(itemAmount))

        onView(withId(R.id.btnAddShoppingItem)).perform(click())

        Truth.assertThat(testViewModel.shoppingItems.getOrAwaitValue().get(0).name)
            .contains(itemName)
    }


    @Test
    fun clickInsertIntoDb_invalid_values_SnackBarShowing() {
        val repository = FakeShoppingRepositoryAndroidTest()
        // repository.setShouldReturnNetworkError(true)
        val testViewModel = ShoppingViewModel(repository)

        launchFragmentInHiltContainer<AddShoppingItemFragment>(navGraphId = R.navigation.nav_graph) {
            setNewViewModel(testViewModel)
            //viewModel = testViewModel
        }

        val itemName = "ShoppingItem"
        val itemPrice = ""
        val itemAmount = ""

        onView(withId(R.id.btnAddShoppingItem)).perform(click())

        Truth.assertThat(testViewModel.shoppingItems.getOrAwaitValue()).isEmpty()

        //com.google.android.material.snackbar.Snackbar
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText("The fields must not be empty")))

    }
}