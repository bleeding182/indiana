package com.davidmedenjak.indiana.di

import com.davidmedenjak.indiana.features.about.AboutActivity
import com.davidmedenjak.indiana.features.about.PrivacyActivity
import com.davidmedenjak.indiana.features.artifacts.ArtifactActivity
import com.davidmedenjak.indiana.features.builds.BuildActivity
import com.davidmedenjak.indiana.features.entertoken.EnterTokenActivity
import com.davidmedenjak.indiana.features.projects.ProjectActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
interface ActivityModule {

    @PerActivity
    @ContributesAndroidInjector
    fun provideProjectActivity(): ProjectActivity

    @PerActivity
    @ContributesAndroidInjector
    fun provideEnterTokenActivity(): EnterTokenActivity

    @PerActivity
    @ContributesAndroidInjector
    fun provideBuildActivity(): BuildActivity

    @PerActivity
    @ContributesAndroidInjector
    fun provideArtifactActivity(): ArtifactActivity

    @PerActivity
    @ContributesAndroidInjector
    fun provideAboutActivity(): AboutActivity

    @PerActivity
    @ContributesAndroidInjector
    fun providePrivacyActivity(): PrivacyActivity

}

