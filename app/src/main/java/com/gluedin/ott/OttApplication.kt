package com.gluedin.ott

import android.app.Application
import com.gluedin.ott.di.AppContainer

class OttApplication : Application() {

    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}
