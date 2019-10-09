package android.blessed.com.holefinder.accelerometer

import android.blessed.com.holefinder.global.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat.startForegroundService
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter

@InjectViewState
class AccelerometerPresenter : MvpPresenter<AccelerometerView>(), AccelerometerPresenterMVP {
    private var isGPSEnabled : Boolean = false
    private var isPermissionGranted : Boolean = false

    var context: Context = Application.sInstance

    private var mClearFile : Boolean = false

    override fun startButtonClicked(intent: Intent) {
        viewState.isGPSEnabled()
        viewState.requestPermissions()
        if (isPermissionGranted) {
            if (isGPSEnabled) {
                viewState.showTrackingInfo(true)
                viewState.switchButtons(true)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(context, intent)
                } else {
                    context.startService(intent)
                }
            }
        }
    }

    override fun stopButtonClicked(intent: Intent) {
        viewState.showTrackingInfo(false)
        context.stopService(intent)
        viewState.switchButtons(false)
    }

    override fun setGPS(value: Boolean) {
        isGPSEnabled = value
    }

    override fun setPermission(value: Boolean) {
        isPermissionGranted = value
    }

    override fun setClearFile(value : Boolean) {
        mClearFile = value
    }

}