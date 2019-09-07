package android.blessed.com.holefinder.accelerometer

import android.Manifest
import android.blessed.com.holefinder.R
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.arellomobile.mvp.MvpAppCompatActivity
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.accelerometer_activity.*
import java.lang.Exception
import java.util.*

class AccelerometerActivityView : MvpAppCompatActivity(), AccelerometerView {
    @InjectPresenter
    lateinit var mAccelerometerPresenterPresenter : AccelerometerPresenter

    @ProvidePresenter
    internal fun provideAccelerometer(): AccelerometerPresenter {
        val presenter = AccelerometerPresenter()
        presenter.context = this.applicationContext
        return presenter
    }

    // SavedInstance
    private var mStartWritingData : Boolean = false
    private lateinit var mStartService : Intent
    private var sb : StringBuilder? = StringBuilder()
    // SavedInstance

    // Receiving
    private lateinit var mReceiver : BroadcastReceiver
    private var mIntentFilter : IntentFilter = IntentFilter()
    // Receiving

    // UI
    private lateinit var timer : Timer
    // UI

    // RX
    private var disposables : CompositeDisposable = CompositeDisposable()
    // RX

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Fabric.with(this, Crashlytics())
        setContentView(R.layout.accelerometer_activity)

        if (savedInstanceState != null) {
            mStartService = savedInstanceState.getParcelable("ActivityIntent")!!
            mStartWritingData = savedInstanceState.getBoolean("StartWritingData")
            sb = savedInstanceState.getCharSequence("DataText") as StringBuilder?
        } else {
            mStartService = Intent(this@AccelerometerActivityView, TrackingService::class.java)
        }

        setText()
        setReceiver()

        start_button.setOnClickListener { mAccelerometerPresenterPresenter.startButtonClicked(mStartService) }
        end_button.setOnClickListener { mAccelerometerPresenterPresenter.stopButtonClicked(mStartService) }
        end_button.isEnabled = false
        clear_file_button.setOnClickListener {
            mAccelerometerPresenterPresenter.setClearFile(true)
            Toast.makeText(this@AccelerometerActivityView, "Файл будет очищен", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("StartWritingData", mStartWritingData)
        outState.putParcelable("ActivityIntent", mStartService)
        outState.putCharSequence("DataText", sb)
    }

    override fun onResume() {
        super.onResume()
        try {
            registerReceiver(mReceiver, mIntentFilter)

            timer = Timer()
            val task = object : TimerTask() {
                override fun run() {
                    runOnUiThread { showInfo() }
                }
            }
            timer.schedule(task, 0, 100)
        } catch (e : Exception) {

        }
    }

    override fun onPause() {
        super.onPause()
        timer.cancel()
    }

    override fun onStop() {
        super.onStop()
        try {
            unregisterReceiver(mReceiver)
        } catch (e: Exception) {
            Log.e("Error", e.toString())
        }

    }

    override fun isGPSEnabled() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        mAccelerometerPresenterPresenter.setGPS(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
    }

    override fun requestPermissions() {
        var isPermissionGranted = false
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse) { isPermissionGranted = true }

                    override fun onPermissionDenied(response: PermissionDeniedResponse) { isPermissionGranted = false}

                    override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest, token: PermissionToken) {/* ... */
                    }
                }).check()

        mAccelerometerPresenterPresenter.setPermission(isPermissionGranted)
    }

    override fun showInfo() {
        accelerometer_data.text = sb
    }

    override fun showGPSToast() {
        Toast.makeText(this, resources.getString(R.string.gps_error_toast), Toast.LENGTH_SHORT).show()
    }

    override fun showTrackingInfo(started: Boolean) = if (started) {
        Toast.makeText(this, resources.getString(R.string.start_tracking_toast), Toast.LENGTH_SHORT).show()
    } else {
        Toast.makeText(this, resources.getString(R.string.end_tracking_toast), Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun switchButtons(startCalculations: Boolean) {
        if (startCalculations) {
            mStartWritingData = true
            start_button.isEnabled = false
            end_button.isEnabled = true
        } else {
            mStartWritingData = false
            start_button.isEnabled = true
            end_button.isEnabled = false
        }
    }

    private fun setText() {
        sb?.setLength(0)
        sb?.append("Ускорение + гравитация: ")
        sb?.append("\n\nЧистое ускорение: ")
        sb?.append("\nЧистая гравитация: ")
        accelerometer_data.text = sb
    }

    private fun setReceiver() {
        mReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                sb?.setLength(0)
                sb?.append(intent.getStringExtra("PASSED_DATA"))
                showInfo()
            }
        }

        mIntentFilter.addAction(TrackingService.MY_ACTION)
        registerReceiver(mReceiver, mIntentFilter)
    }
}