package com.davidmedenjak.indiana.app

import com.davidmedenjak.indiana.api.HttpModule
import com.davidmedenjak.indiana.di.ActivityModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [AndroidSupportInjectionModule::class, AppModule::class, ActivityModule::class, HttpModule::class])
interface AppComponent : AndroidInjector<App> {

    fun app(): App

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun app(app: App): AppComponent.Builder

        fun build(): AppComponent
    }
}