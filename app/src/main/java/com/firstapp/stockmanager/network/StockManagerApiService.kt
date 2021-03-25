/*
 * Copyright (C) 2021 The Android Open Source Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.firstapp.stockmanager.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET

private const val BASE_URL = "http://api.marketstack.com/v1/"

private const val TOKEN = "de3f54e3342a0a46d718347fbdf90b9f"

enum class APIActions {
    EOD {
        override fun calculateUrl() = "eod?access_key=${TOKEN}&symbols=AAP/"
    };

    abstract fun calculateUrl(): String
}

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()


interface StockManagerApiService {

    @GET("eod/latest?access_key=de3f54e3342a0a46d718347fbdf90b9f&symbols=AAPL,MSFT")
    suspend fun getTickers(): APIResponse
}

object StockManagerApi {
    val retrofitService: StockManagerApiService by lazy { retrofit.create(StockManagerApiService::class.java) }
}
