package android.blessed.com.holefinder.services

import android.Manifest
import android.app.Service
import android.blessed.com.holefinder.global.NotificationHelper
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast

import androidx.core.content.ContextCompat

import java.io.BufferedWriter
import java.io.FileNotFoundException
import java.io.IOException
import java.io.OutputStreamWriter
import java.util.Arrays
import java.util.Date
import java.util.Locale

class TrackingService : Service() {

    private var mLocationManager: LocationManager? = null
    private lateinit var mLocationListener: LocationListener

    private var mSensorManager: SensorManager? = null
    private var mSensorAccelerometer: Sensor? = null
    private var mClearFile: Boolean = false

    private val sb = StringBuilder()

    internal var valuesAccelerometer = FloatArray(3)
    internal var valuesAccelerometerMotion = FloatArray(3)
    internal var valuesAccelerometerGravity = FloatArray(3)
    internal var valuesLinAccelerometer = FloatArray(3)
    internal var valuesGravity = FloatArray(3)

    private val listener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {

        }

        override fun onSensorChanged(event: SensorEvent) {
            when (event.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> if (permissionsCheck()) {
                    for (i in 0..2) {
                        valuesAccelerometer[i] = event.values[i]
                        valuesAccelerometerGravity[i] = (0.1 * event.values[i] + 0.9 * valuesAccelerometerGravity[i]).toFloat()
                        valuesAccelerometerMotion[i] = event.values[i] - valuesAccelerometerGravity[i]
                    }
                    if (checkValues(valuesAccelerometerMotion)) {
                        getPosition()
                        setConfiguration(maxGravityAxis)
                        writeFile()
                    }
                }
            }
        }
    }

    private val maxGravityAxis: Axes?
        get() {
            var i = 0
            var max = valuesAccelerometerGravity[i]
            for (j in 1..2) {
                if (max < valuesAccelerometerGravity[j]) {
                    max = valuesAccelerometerGravity[j]
                    i = j
                }
            }
            when (i) {
                0 -> return Axes.X
                1 -> return Axes.Y
                2 -> return Axes.Z
            }
            return null
        }

    internal enum class Axes {
        X, Y, Z
    }

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(NOTIFICATION_INDEX, NotificationHelper(this).createNotification("Считывание качества дороги", "Идет отслеживание"))
        }

        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        if (mSensorManager != null) {
            mSensorAccelerometer = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

            mSensorManager!!.registerListener(listener, mSensorAccelerometer, 100000)

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Разрешения не были предоставлены", Toast.LENGTH_SHORT).show()
            } else {
                mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                mLocationListener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {

                    }

                    override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {

                    }

                    override fun onProviderEnabled(s: String) {

                    }

                    override fun onProviderDisabled(s: String) {
                        Log.i("Service", "Got a problem")
                    }
                }
                mLocationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, mLocationListener)
                mLocationManager!!.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, mLocationListener)
                mLocationManager!!.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0f, mLocationListener)
            }
        } else {
            Toast.makeText(this, "Отсутствует акселерометр! Приложение недоступно", Toast.LENGTH_SHORT).show()
            onDestroy()
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val extras = intent.extras
        if (extras != null) {
            mClearFile = extras.get("Clear") as Boolean
        }

        mSensorManager!!.registerListener(listener, mSensorAccelerometer,
                SensorManager.SENSOR_DELAY_NORMAL)

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(STOP_FOREGROUND_DETACH)
        }
        mSensorManager!!.unregisterListener(listener)
        mLocationManager!!.removeUpdates(mLocationListener)
    }

    private fun getPosition() {
        try {
            if (permissionsCheck()) {
                mCurrentLocation = mLocationManager!!.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
            }
        } catch (ex: SecurityException) {
            Log.e("ERROR", "Error creating location service: " + ex.message)
        }

    }

    private fun passDataToActivity() {
        sb.setLength(0)
        sb.append("Ускорение + гравитация: ")
        sb.append(format(valuesAccelerometer))
        sb.append("\n\nЧистое ускорение: ")
        sb.append(format(valuesAccelerometerMotion))
        sb.append("\nЧистая гравитация: ")
        sb.append(format(valuesAccelerometerGravity))
        Log.i("Service", sb.toString())
        val intent = Intent().setAction(MY_ACTION).putExtra("PASSED_DATA", sb.toString())
        sendBroadcast(intent)
    }

    private fun writeFile() {
        try {
            if (mCurrentLocation != null) {
                if (mCurrentLocation!!.latitude != 0.0 && mCurrentLocation!!.longitude != 0.0) {
                    val bw: BufferedWriter

                    if (mClearFile) {
                        bw = BufferedWriter(OutputStreamWriter(openFileOutput(FILENAME, Context.MODE_PRIVATE)))
                        mClearFile = false
                    } else {
                        bw = BufferedWriter(OutputStreamWriter(openFileOutput(FILENAME, Context.MODE_APPEND)))
                    }

                    bw.write("\n/------------------------------------------------------------/\n"
                            + "Ускорение + гравитация: " + format(valuesAccelerometer)
                            + "\n\nЧистое ускорение: " + format(valuesAccelerometerMotion)
                            + "\nЧистая гравитация: " + format(valuesAccelerometerGravity)
                            + "\n\nКоординаты: " + mCurrentLocation!!.latitude + " " + mCurrentLocation!!.longitude
                            + "\n\nДата и время: " + Date().time)
                    bw.close()
                    passDataToActivity()
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    private fun permissionsCheck(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun format(values: FloatArray): String {
        return String.format(Locale.ENGLISH, "%1$.1f  %2$.1f  %3$.1f", values[0], values[1], values[2])
    }

    private fun checkValues(values: FloatArray): Boolean {
        Arrays.sort(values)
        return values[0] < MIN_ACCELEROMETER_DATA || values[2] > MAX_ACCELEROMETER_DATA
    }

    private fun setConfiguration(axis: Axes?) {
        if (axis != null) {
            // преобразуем систему координат устройства в привычную и понятную нам
            val temp: Float
            when (axis) {
                // классическая конфигурация массива: [x, y, z]
                Axes.X -> {
                    // телефон закреплен горизонтально => берем ускорение по оси X
                    // меняем конфигурацию: [-y, x, z]
                    temp = valuesAccelerometerMotion[1]
                    valuesAccelerometerMotion[1] = valuesAccelerometerMotion[0]
                    valuesAccelerometerMotion[0] = -temp
                }
                Axes.Y -> {
                }
                Axes.Z -> {
                    // телефон закреплен параллельно земле => берем ускорение по оси Z
                    // меняем конфигурацию: [x, z, -y]
                    temp = valuesAccelerometerMotion[1]
                    valuesAccelerometerMotion[1] = valuesAccelerometerMotion[2]
                    valuesAccelerometerMotion[2] = -temp
                }
            }// телефон закреплен вертикально => берем ускорение по оси Y
            // классическая конфигурация, ничего не меняем: [x, y, z]
        }
    }

    companion object {

        val MY_ACTION = "MY_ACTION"

        private var mCurrentLocation: Location? = null

        private val MIN_ACCELEROMETER_DATA = 0f
        private val MAX_ACCELEROMETER_DATA = 0f
        private val MIN_SPEED = 1.39
        private val FILENAME = "accelerometer_data"
        private val NOTIFICATION_INDEX = 1
    }
}
