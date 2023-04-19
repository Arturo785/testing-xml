package com.example.testinginxml.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.filters.SmallTest
import com.example.testinginxml.getOrAwaitValue
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject
import javax.inject.Named

@ExperimentalCoroutinesApi
//@RunWith(AndroidJUnit4::class) we remove this because we now use our hilt androidTestRunner
// if we don't use hiltTestRunner then we can use @RunWith(AndroidJUnit4::class)
@SmallTest // tells the tests run in here are not very time consuming
@HiltAndroidTest
class ShoppingDaoTest {


    // our rule to make use of hilt in here
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    // to make the call totally synchronous
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    // now we provide our db for test through hilt and the property can't be private
    @Inject
    @Named("test_db") // because we want to provide the test one, we have 2 different way of providing databases in the project
    lateinit var database: ShoppingItemDatabase

    private lateinit var dao: ShoppingDao

    // our DAO and in memory DB
    @Before
    fun setup() {
        /* database = Room.inMemoryDatabaseBuilder(
             ApplicationProvider.getApplicationContext(),
             ShoppingItemDatabase::class.java
         ).allowMainThreadQueries()
             .build() // in test cases we want to run only in one thread and be repeatable*/

        // now we using hilt
        hiltRule.inject()
        dao = database.shoppingDao()
    }

    // close our DB after every test, we want a fresh one per test
    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertShoppingItem() = runBlockingTest {
        val shoppingItem = ShoppingItem("name", 1, 1f, "url", id = 1)
        dao.insertShoppingItem(shoppingItem)

        // get or await is for liveData and is created in a file in
        val allShoppingItems = dao.observeAllShoppingItems().getOrAwaitValue()

        assertThat(allShoppingItems).contains(shoppingItem)
    }

    @Test
    fun deleteShoppingItem() = runBlockingTest {
        val shoppingItem = ShoppingItem("name", 1, 1f, "url", id = 1)
        dao.insertShoppingItem(shoppingItem)
        dao.deleteShoppingItem(shoppingItem)

        val allShoppingItems = dao.observeAllShoppingItems().getOrAwaitValue()

        assertThat(allShoppingItems).doesNotContain(shoppingItem)
    }

    @Test
    fun observeTotalPriceSum() = runBlockingTest {
        val shoppingItem1 = ShoppingItem("name", 2, 10f, "url", id = 1)
        val shoppingItem2 = ShoppingItem("name", 4, 5.5f, "url", id = 2)
        val shoppingItem3 = ShoppingItem("name", 0, 100f, "url", id = 3)

        dao.insertShoppingItem(shoppingItem1)
        dao.insertShoppingItem(shoppingItem2)
        dao.insertShoppingItem(shoppingItem3)

        val totalPriceSum = dao.observeTotalPrice().getOrAwaitValue()

        // the ammount of price expected vs the price returned by the DAO
        assertThat(totalPriceSum).isEqualTo(2 * 10f + 4 * 5.5f)
    }
/*
    @Test
    fun testLaunchFragmentInHiltContainer() {
        launchFragmentInHiltContainer<ShoppingFragment> {
            // here we have the instance of our fragment
        }
    }*/
}


