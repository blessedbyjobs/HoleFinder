package android.blessed.com.holefinder.models;

public class SensorsData {
    private float[] mValuesAccelerometer;
    private float[] mValuesAccelerometerMotion = new float[3];
    private float[] mValuesAccelerometerGravity = new float[3];

    private final float MIN_ACCELEROMETER_DATA = 0;
    private final float MAX_ACCELEROMETER_DATA = 0;

    public SensorsData(float[] valuesAccelerometer) {
        mValuesAccelerometer = valuesAccelerometer;
        for (int i = 0; i < 3; i++) {
            mValuesAccelerometerGravity[i] = (float) (0.1 * mValuesAccelerometer[i] + 0.9 * mValuesAccelerometerGravity[i]);
            mValuesAccelerometerMotion[i] = mValuesAccelerometer[i] - mValuesAccelerometerGravity[i];
        }
    }

    public float[] getValuesAccelerometer() {
        return mValuesAccelerometer;
    }

    public void setValuesAccelerometer(float[] valuesAccelerometer) {
        mValuesAccelerometer = valuesAccelerometer;
    }

    public float[] getValuesAccelerometerMotion() {
        return mValuesAccelerometerMotion;
    }

    public void setValuesAccelerometerMotion(float[] valuesAccelerometerMotion) {
        mValuesAccelerometerMotion = valuesAccelerometerMotion;
    }

    public float[] getValuesAccelerometerGravity() {
        return mValuesAccelerometerGravity;
    }

    public void setValuesAccelerometerGravity(float[] valuesAccelerometerGravity) {
        mValuesAccelerometerGravity = valuesAccelerometerGravity;
    }

    public float getMIN_ACCELEROMETER_DATA() {
        return MIN_ACCELEROMETER_DATA;
    }

    public float getMAX_ACCELEROMETER_DATA() {
        return MAX_ACCELEROMETER_DATA;
    }
}
