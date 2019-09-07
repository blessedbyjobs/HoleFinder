package android.blessed.com.holefinder.global

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides

@Module
class ApplicationModule(private val application: android.blessed.com.holefinder.global.Application) {

    @Provides
    fun provideApplication(): Application = application

    @Provides
    fun provideContext(): Context = application
}