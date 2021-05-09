package com.android.stockmanager.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
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
    suspend fun getEODLatest(
        @Query("access_key") access_key: String,
        @Query("symbols") symbols: String
    ): NetworkEODTickerContainer

    @GET("tickers/{tickers}")
    suspend fun getTickerName(
        @Path("tickers") ticker: String,
        @Query("access_key") access_key: String
    ): NetworkCompanyNameData
}

object StockManagerApi {
    val retrofitService: StockManagerApiService by lazy { retrofit.create(StockManagerApiService::class.java) }
}



/*
Example getEODLatest() API response
{
    "pagination": {
        "limit": 100,
        "offset": 0,
        "count": 100,
        "total": 9944
    },
    "data": [
        {
            "open": 129.8,
            "high": 133.04,
            "low": 129.47,
            "close": 132.995,
            "volume": 106686703.0,
            "adj_high": 133.04,
            "adj_low": 129.47,
            "adj_close": 132.995,
            "adj_open": 129.8,
            "adj_volume": 106686703.0,
            "split_factor": 1.0,
            "symbol": "AAPL",
            "exchange": "XNAS",
            "date": "2021-04-09T00:00:00+0000"
        },
        [...]
    ]
}
*/


/* Example getTickerInfo() API response
{
    "name": "Apple Inc",
    "symbol": "AAPL",
    "has_intraday": false,
    "has_eod": true,
    "country": null,
    "stock_exchange": {
        "name": "NASDAQ Stock Exchange",
        "acronym": "NASDAQ",
        "mic": "XNAS",
        "country": "USA",
        "country_code": "US",
        "city": "New York",
        "website": "WWW.NASDAQ.COM"
    }
}
}*/
