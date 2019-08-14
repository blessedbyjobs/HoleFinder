package android.blessed.com.holefinder.accelerometer;

import android.app.Activity;
import android.blessed.com.holefinder.R;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;
import java.util.Timer;
import java.util.TimerTask;

public class AccelerometerActivity extends AppCompatActivity implements AccelerometerContract.View {
    private AccelerometerPresenter<AccelerometerContract.View> mPresenter;
    Intent intent;
    BroadcastReceiver myReceiver;
    IntentFilter mIntentFilter;

    private Button mStartInputDataButton;
    private Button mEndInputDataButton;
    private Button mClearFileButton;
    private TextView mAccelerometerData;
    private TextView mFileTextInfo;

    private StringBuilder sb = new StringBuilder();
    Timer timer;

    private boolean mStartWritingData = false;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("StartWritingData", mStartWritingData);
        outState.putParcelable("ActivityIntent", intent);
        outState.putCharSequence("DataText", sb);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.accelerometer_activity);

        mPresenter = new AccelerometerPresenter<>();

        setScreenElements();
        setText();

        if (savedInstanceState != null) {
            intent = savedInstanceState.getParcelable("ActivityIntent");
            switchButtons(savedInstanceState.getBoolean("StartWritingData"));
            sb = (StringBuilder) savedInstanceState.getCharSequence("DataText");
        }

        setListeners();
    }

    @Override
    public void onStart() {
        super.onStart();
        mPresenter.attachView(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            registerReceiver(myReceiver, mIntentFilter);
        } catch (Exception e) {
            Log.e("Error", String.valueOf(e));
        }

        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showInfo();
                    }
                });
            }
        };
        timer.schedule(task, 0, 250);
    }

    @Override
    protected void onPause() {
        super.onPause();
        timer.cancel();
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            unregisterReceiver(myReceiver);
        } catch (Exception e) {
            Log.e("Error", String.valueOf(e));
        }
        mPresenter.detachView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void setText() {
        sb.setLength(0);
        sb.append("Ускорение + гравитация: ");
        sb.append("\n\nЧистое ускорение: ");
        sb.append("\nЧистая гравитация: ");
        sb.append("\n\nСенсор чистого ускорения: ");
        sb.append("\nСенсор гравитации: ");
        mAccelerometerData.setText(sb);
    }

    @Override
    public void switchButtons(boolean startCalculations) {
        if (startCalculations) {
            mStartWritingData = true;
            mStartInputDataButton.setEnabled(false);
            mEndInputDataButton.setEnabled(true);
        } else {
            mStartWritingData = false;
            mStartInputDataButton.setEnabled(true);
            mEndInputDataButton.setEnabled(false);
        }
    }

    @Override
    public void showInfo() {
        mAccelerometerData.setText(sb);
    }

    @Override
    public Activity getViewActivity() {
        return this;
    }

    @Override
    public void showGPSToast() {
        Toast.makeText(AccelerometerActivity.this, "Ошибка: включите GPS", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showTrackingInfo(boolean started) {
        if (started) {
            Toast.makeText(AccelerometerActivity.this, "Начато отслеживание", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(AccelerometerActivity.this, "Прекращено отслеживание", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void setScreenElements() {
        mAccelerometerData = findViewById(R.id.accelerometer_data);
        mStartInputDataButton = findViewById(R.id.start_button);
        mEndInputDataButton = findViewById(R.id.end_button);
        mEndInputDataButton.setEnabled(false);
        mClearFileButton = findViewById(R.id.clear_file_button);
        mFileTextInfo = findViewById(R.id.file_text_info);
    }

    @Override
    public void setListeners() {
        mStartInputDataButton.setOnClickListener(new android.view.View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(android.view.View v) {
                mFileTextInfo.setText("");
                intent = new Intent(AccelerometerActivity.this, TrackingService.class);
                if (mPresenter.isClearFile()) {
                    intent.putExtra("Clear", mPresenter.isClearFile());
                }
                mPresenter.setClearFile(false);
                mPresenter.startButtonClicked(intent);
            }
        });

        mEndInputDataButton.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                if (intent != null) {
                    mPresenter.stopButtonClicked(intent);
                } else {
                    Toast.makeText(AccelerometerActivity.this, "Критическая ошибка", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mClearFileButton.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                mPresenter.setClearFile(true);
                Toast.makeText(AccelerometerActivity.this, "Очищено!", Toast.LENGTH_SHORT).show();
            }
        });

        myReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                sb.setLength(0);
                sb.append(intent.getStringExtra("PASSED_DATA"));
                showInfo();
            }
        };

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(TrackingService.MY_ACTION);
        registerReceiver(myReceiver, mIntentFilter);
    }
}
