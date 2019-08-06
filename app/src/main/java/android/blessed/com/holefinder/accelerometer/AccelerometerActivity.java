package android.blessed.com.holefinder.accelerometer;

import android.app.Activity;
import android.blessed.com.holefinder.R;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Timer;
import java.util.TimerTask;

public class AccelerometerActivity extends AppCompatActivity implements AccelerometerContract.View {
    private AccelerometerPresenter mPresenter;

    Intent intent;
    BroadcastReceiver myReceiver;
    IntentFilter mIntentFilter;

    private Button mStartInputDataButton;
    private Button mEndInputDataButton;
    private Button mClearFileButton;
    private TextView mAccelerometerData;

    private StringBuilder sb = new StringBuilder();
    Timer timer;

    private boolean mStartWritingData = false;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("StartWritingData", mStartWritingData);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.accelerometer_activity);

        if (savedInstanceState != null) {
            switchButtons(savedInstanceState.getBoolean("StartWritingData"));
        }

        mPresenter = new AccelerometerPresenter();

        mAccelerometerData = findViewById(R.id.accelerometer_data);
        mStartInputDataButton = findViewById(R.id.start_button);
        mEndInputDataButton = findViewById(R.id.end_button);
        mEndInputDataButton.setEnabled(false);
        mClearFileButton = findViewById(R.id.clear_file_button);

        setText();

        mStartInputDataButton.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, TrackingService.class));
    }

    @Override
    public void onStart() {
        super.onStart();
        mPresenter.attachView(this);
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
    public void setText() {
        sb.setLength(0);
        sb.append("Ускорение + гравитация: ");
        sb.append("\n\nЧистое ускорение: ");
        sb.append("\nЧистая гравитация: ");
        sb.append("\n\nДанные сенсора чистого ускорения: ");
        sb.append("\nДанные сенсора гравитации: ");
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
}
