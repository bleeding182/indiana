package com.davidmedenjak.indiana.app

import com.davidmedenjak.indiana.di.AppInjector
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication

class App : DaggerApplication() {

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerAppComponent.builder().app(this).build()
    }

    override fun onCreate() {
        super.onCreate()

        AppInjector.init(this)
    }
}

