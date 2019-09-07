package android.blessed.com.holefinder.global

import android.blessed.com.holefinder.accelerometer.AccelerometerPresenter
import dagger.Component

@Component(modules = [ApplicationModule::class])
interface ApplicationComponent {
    fun inject(application: Application)
    fun inject(activity: AccelerometerPresenter)
}