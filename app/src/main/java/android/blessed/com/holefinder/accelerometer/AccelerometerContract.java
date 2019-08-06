package android.blessed.com.holefinder.accelerometer;

import android.app.Activity;
import android.content.Intent;

public interface AccelerometerContract {
    interface View {
        void setText();
        void switchButtons(boolean startCalculations);
        void showInfo();
        Activity getViewActivity();
        void showGPSToast();
        void showTrackingInfo(boolean started);
    }

    interface Presenter <V extends AccelerometerContract.View> {
        void attachView(V view);
        void detachView();

        void startButtonClicked(Intent intent);
        void stopButtonClicked(Intent intent);

        boolean isGPSEnabled();
        boolean hasLocationPermissions();
        void requestPermissions();
    }

    interface Model {

    }
}
