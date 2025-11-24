package com.moondark.realfoodapp

import retrofit2.http.GET
import retrofit2.http.Path

interface ExchangeRateService {

    @GET("v6/{apiKey}/latest/{baseCurrency}")
    suspend fun getExchangeRates(
        @Path("apiKey") apiKey: String,
        @Path("baseCurrency") baseCurrency: String = "USD"
    ): ExchangeRateResponse
}
