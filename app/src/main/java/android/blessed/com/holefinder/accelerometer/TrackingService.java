package android.blessed.com.holefinder.accelerometer;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.Service;
import android.blessed.com.holefinder.R;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
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

    private static Location mCurrentLocation;

    private LocationManager mLocationManager;
    LocationListener mLocationListener;

    private SensorManager mSensorManager;
    private Sensor mSensorAccelerometer;

    private static final float MIN_ACCELEROMETER_DATA = 0;
    private static final float MAX_ACCELEROMETER_DATA = 0;
    private static final double MIN_SPEED = 1.39;
    private boolean mClearFile;

    private StringBuilder sb = new StringBuilder();
    private static final String FILENAME = "accelerometer_data";
    private static final int NOTIFICATION_INDEX = 1;

    float[] valuesAccelerometer = new float[3];
    float[] valuesAccelerometerMotion = new float[3];
    float[] valuesAccelerometerGravity = new float[3];
    float[] valuesLinAccelerometer = new float[3];
    float[] valuesGravity = new float[3];

    enum Axes {X, Y, Z}

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
                            setConfiguration(getMaxGravityAxis());
                            writeFile();
                        }
                    }
                    break;
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(NOTIFICATION_INDEX, new NotificationHelper(this).createNotification("Считывание качества дороги", "Идет отслеживание"));
        }

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager != null) {
            mSensorAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

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
                        Log.i("Service", "Got a problem");
                    }
                };
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
                mLocationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, mLocationListener);
            }
        } else {
            Toast.makeText(this, "Отсутствует акселерометр! Приложение недоступно", Toast.LENGTH_SHORT).show();
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

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(STOP_FOREGROUND_DETACH);
        }
        mSensorManager.unregisterListener(listener);
        mLocationManager.removeUpdates(mLocationListener);
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
        Log.i("Service", String.valueOf(sb));
        Intent intent = new Intent().setAction(MY_ACTION).putExtra("PASSED_DATA", sb.toString());
        sendBroadcast(intent);
    }

    private void writeFile() {
        try {
            if (mCurrentLocation != null) {
                if (mCurrentLocation.getLatitude() != 0 && mCurrentLocation.getLongitude() != 0) {
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
                            + "\n\nДата и время: " + new Date().getTime());
                    bw.close();
                    passDataToActivity();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private Axes getMaxGravityAxis() {
        int i = 0;
        float max = valuesAccelerometerGravity[i];
        for (int j = 1; j < 3; j++) {
            if (max < valuesAccelerometerGravity[j]) {
                max = valuesAccelerometerGravity[j];
                i = j;
            }
        }
        switch (i) {
            case 0:
                return Axes.X;
            case 1:
                return Axes.Y;
            case 2:
                return Axes.Z;
        }
        return null;
    }

    private void setConfiguration(Axes axis) {
        if (axis != null) {
            // преобразуем систему координат устройства в привычную и понятную нам
            float temp;
            switch (axis) {
                // классическая конфигурация массива: [x, y, z]
                case X:
                    // телефон закреплен горизонтально => берем ускорение по оси X
                    // меняем конфигурацию: [-y, x, z]
                    temp = valuesAccelerometerMotion[1];
                    valuesAccelerometerMotion[1] = valuesAccelerometerMotion[0];
                    valuesAccelerometerMotion[0] = -temp;
                    break;
                case Y:
                    // телефон закреплен вертикально => берем ускорение по оси Y
                    // классическая конфигурация, ничего не меняем: [x, y, z]
                    break;
                case Z:
                    // телефон закреплен параллельно земле => берем ускорение по оси Z
                    // меняем конфигурацию: [x, z, -y]
                    temp = valuesAccelerometerMotion[1];
                    valuesAccelerometerMotion[1] = valuesAccelerometerMotion[2];
                    valuesAccelerometerMotion[2] = -temp;
                    break;
            }
        }
    }
}
