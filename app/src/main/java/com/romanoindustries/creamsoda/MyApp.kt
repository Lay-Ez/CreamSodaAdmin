package com.romanoindustries.creamsoda

import android.app.Application

class MyApp: Application() {

    val repositoryComponent = DaggerRepositoryComponent.create()

}