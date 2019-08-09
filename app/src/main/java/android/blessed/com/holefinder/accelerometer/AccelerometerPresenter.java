package android.blessed.com.holefinder.accelerometer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleObserver;

public class AccelerometerPresenter <T extends AccelerometerContract.View>
        implements AccelerometerContract.Presenter<T>, LifecycleObserver {
    private T mView;

    private static final String[] LOCATION_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private static final int REQUEST_LOCAL_PERMISSIONS = 0;

    private boolean mClearFile = false;

    @Override
    public void attachView(T view) {
        mView = view;
    }

    @Override
    public void detachView() {
        mView = null;
    }

    public boolean isViewAttached() {
        return mView != null;
    }

    public T getView() {
        return mView;
    }

    public void checkViewAttached() {
        if (!isViewAttached()) throw new AccelerometerPresenter.MvpViewNotAttachedException();
    }

    @Override
    public void startButtonClicked(Intent intent) {
        checkViewAttached();
        if (hasLocationPermissions()) {
            if (isGPSEnabled()) {
                mView.showTrackingInfo(true);
                mView.switchButtons(true);
                mView.getViewActivity().startService(intent);
            } else {
                mView.showGPSToast();
            }
        } else {
            requestPermissions();
        }
    }

    @Override
    public void stopButtonClicked(Intent intent) {
        checkViewAttached();
        mView.showTrackingInfo(false);
        mView.getViewActivity().stopService(intent);
        mView.switchButtons(false);
    }

    @Override
    public boolean isGPSEnabled() {
        LocationManager locationManager = (LocationManager) getView().getViewActivity().getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } else {
            return false;
        }
    }

    @Override
    public boolean hasLocationPermissions() {
        return ContextCompat.checkSelfPermission(getView().getViewActivity(), LOCATION_PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getView().getViewActivity(), LOCATION_PERMISSIONS[1]) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void requestPermissions() {
        ActivityCompat.requestPermissions(mView.getViewActivity(), LOCATION_PERMISSIONS, REQUEST_LOCAL_PERMISSIONS);
    }

    public void setClearFile(boolean clearFile) {
        mClearFile = clearFile;
    }

    public boolean isClearFile() {
        return mClearFile;
    }

    public static class MvpViewNotAttachedException extends RuntimeException {
        public MvpViewNotAttachedException() {
            super("Please call Presenter.attachView(View) before" +
                    " requesting data to the Presenter");
        }
    }
}
