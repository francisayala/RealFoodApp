package com.moondark.realfoodapp

import android.app.Application
import com.yandex.mapkit.MapKitFactory

class RealFoodApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Aquí irá tu API Key de Yandex
        MapKitFactory.setApiKey("261267cf-290a-46d9-bacc-88d13a928af3")

        // Opcional: establecer idioma español
        MapKitFactory.setLocale("es_ES")
    }
}
