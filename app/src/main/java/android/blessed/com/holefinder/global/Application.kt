package android.blessed.com.holefinder.global

import android.app.Application

class Application: Application() {

    lateinit var component: ApplicationComponent

    override fun onCreate() {
        super.onCreate()

        sInstance = this
        setup()
    }

    fun setup() {
        component = DaggerApplicationComponent.builder()
                .applicationModule(ApplicationModule(this)).build()
        component.inject(this)
    }

    fun getApplicationComponent(): ApplicationComponent {
        return component
    }

    companion object {
        lateinit var sInstance: android.blessed.com.holefinder.global.Application private set
    }
}