package com.firstapp.stockmanager.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private const val BASE_URL = "http://api.marketstack.com/v1/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()


interface StockManagerApiService {
    @GET("eod/latest")
    suspend fun getTickers(
        @Query("access_key") access_key: String,
        @Query("symbols") symbols: String
    ): NetworkTickerContainer
}

object StockManagerApi {
    val retrofitService: StockManagerApiService by lazy { retrofit.create(StockManagerApiService::class.java) }
}
