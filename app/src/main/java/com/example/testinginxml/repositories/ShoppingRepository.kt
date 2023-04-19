package com.example.testinginxml.repositories

import androidx.lifecycle.LiveData
import com.example.testinginxml.data.local.ShoppingItem
import com.example.testinginxml.data.remote.responses.ImageResponse
import com.example.testinginxml.utils.Resource
import retrofit2.Response

interface ShoppingRepository {

    suspend fun insertShoppingItem(shoppingItem: ShoppingItem)

    suspend fun deleteShoppingItem(shoppingItem: ShoppingItem)

    fun observeAllShoppingItems(): LiveData<List<ShoppingItem>>

    fun observeTotalPrice(): LiveData<Float>

    suspend fun searchForImage(imageQuery: String): Resource<ImageResponse>
}