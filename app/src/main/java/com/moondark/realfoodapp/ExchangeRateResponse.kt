package com.moondark.realfoodapp

import com.google.gson.annotations.SerializedName

data class ExchangeRateResponse(
    @SerializedName("result")
    val result: String,

    @SerializedName("base_code")
    val baseCode: String,

    @SerializedName("conversion_rates")
    val conversionRates: Map<String, Double>,

    @SerializedName("time_last_update_unix")
    val lastUpdate: Long
)

// Data class para mostrar monedas disponibles
data class Currency(
    val code: String,
    val name: String,
    val symbol: String
)
