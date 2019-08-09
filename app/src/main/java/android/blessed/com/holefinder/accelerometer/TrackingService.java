package android.blessed.com.holefinder.accelerometer;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class TrackingService extends Service {

    public final static String MY_ACTION = "MY_ACTION";

    private Location mCurrentLocation;

    private LocationManager mLocationManager;
    LocationListener mLocationListener;

    private SensorManager mSensorManager;
    private Sensor mSensorAccelerometer;
    private Sensor mSensorLinAccelerometer;
    private Sensor mSensorGravity;

    private static final float MIN_ACCELEROMETER_DATA = 0;
    private static final float MAX_ACCELEROMETER_DATA = 0;
    private static final double MIN_SPEED = 1.39;
    private boolean mClearFile;

    private StringBuilder sb = new StringBuilder();
    private static final String FILENAME = "accelerometer_data";

    float[] valuesAccelerometer = new float[3];
    float[] valuesAccelerometerMotion = new float[3];
    float[] valuesAccelerometerGravity = new float[3];
    float[] valuesLinAccelerometer = new float[3];
    float[] valuesGravity = new float[3];

    private SensorEventListener listener = new SensorEventListener() {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    if (permissionsCheck()) {
                        for (int i = 0; i < 3; i++) {
                            valuesAccelerometer[i] = event.values[i];
                            valuesAccelerometerGravity[i] = (float) (0.1 * event.values[i] + 0.9 * valuesAccelerometerGravity[i]);
                            valuesAccelerometerMotion[i] = event.values[i] - valuesAccelerometerGravity[i];
                        }
                        if (checkValues(valuesAccelerometerMotion)) {
                            getPosition();
                            writeFile();
                        }
                    }
                    break;
                case Sensor.TYPE_LINEAR_ACCELERATION:
                    if (permissionsCheck()) {
                        System.arraycopy(event.values, 0, valuesLinAccelerometer, 0, 3);
                    }
                    break;
                case Sensor.TYPE_GRAVITY:
                    if (permissionsCheck()) {
                        System.arraycopy(event.values, 0, valuesGravity, 0, 3);
                    }
                    break;
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager != null) {
            mSensorAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mSensorLinAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            mSensorGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

            mSensorManager.registerListener(listener, mSensorAccelerometer, 100000);

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Разрешения не были предоставлены", Toast.LENGTH_SHORT).show();
            } else {
                mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                mLocationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {

                    }

                    @Override
                    public void onStatusChanged(String s, int i, Bundle bundle) {

                    }

                    @Override
                    public void onProviderEnabled(String s) {

                    }

                    @Override
                    public void onProviderDisabled(String s) {

                    }
                };
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
                mLocationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, mLocationListener);
            }
        } else {
            Toast.makeText(this, "Отсутсвует акселерометр! Приложение недоступно", Toast.LENGTH_SHORT).show();
            onDestroy();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mClearFile = (boolean) extras.get("Clear");
        }

        mSensorManager.registerListener(listener, mSensorAccelerometer,
                SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(listener, mSensorLinAccelerometer,
                SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(listener, mSensorGravity,
                SensorManager.SENSOR_DELAY_NORMAL);

        return super.onStartCommand(intent, flags, startId);
    }

    private void getPosition() {
        try {
            if (permissionsCheck()) {
                mCurrentLocation = mLocationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            }
        } catch (SecurityException ex) {
            Log.e("ERROR", "Error creating location service: " + ex.getMessage());
        }
    }

    private void passDataToActivity() {
        sb.setLength(0);
        sb.append("Ускорение + гравитация: ");
        sb.append(format(valuesAccelerometer));
        sb.append("\n\nЧистое ускорение: ");
        sb.append(format(valuesAccelerometerMotion));
        sb.append("\nЧистая гравитация: ");
        sb.append(format(valuesAccelerometerGravity));
        sb.append("\n\nСенсор чистого ускорения: ");
        sb.append(format(valuesLinAccelerometer));
        sb.append("\nСенсор гравитации: ");
        sb.append(format(valuesGravity));

        Intent intent = new Intent().setAction(MY_ACTION).putExtra("PASSED_DATA", sb.toString());
        // Log.i("Service", sb.toString());
        sendBroadcast(intent);
    }

    private void writeFile() {
        try {
            if (mCurrentLocation != null && mCurrentLocation.getLatitude() != 0 && mCurrentLocation.getLongitude() != 0) {
                Date date = new Date();

                BufferedWriter bw;

                if (mClearFile) {
                    bw = new BufferedWriter(new OutputStreamWriter(openFileOutput(FILENAME, MODE_PRIVATE)));
                    mClearFile = false;
                } else {
                    bw = new BufferedWriter(new OutputStreamWriter(openFileOutput(FILENAME, MODE_APPEND)));
                }

                bw.write("\n/------------------------------------------------------------/\n"
                        + "Ускорение + гравитация: " + format(valuesAccelerometer)
                        + "\n\nЧистое ускорение: " + format(valuesAccelerometerMotion)
                        + "\nЧистая гравитация: " + format(valuesAccelerometerGravity)
                        + "\n\nСенсор чистого ускорения: " + format(valuesLinAccelerometer)
                        + "\nСенсор гравитации: " + format(valuesGravity)
                        + "\n\nКоординаты: " + mCurrentLocation.getLatitude() + " " + mCurrentLocation.getLongitude()
                        + "\n\nДата и время: " + date.getTime());
                bw.close();

                passDataToActivity();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSensorManager.unregisterListener(listener);
        mLocationManager.removeUpdates(mLocationListener);
    }


    private boolean permissionsCheck() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private String format(float[] values) {
        return String.format(Locale.ENGLISH,"%1$.1f  %2$.1f  %3$.1f", values[0], values[1], values[2]);
    }

    private boolean checkValues(float[] values) {
        Arrays.sort(values);
        return values[0] < MIN_ACCELEROMETER_DATA || values[2] > MAX_ACCELEROMETER_DATA;
    }
}
