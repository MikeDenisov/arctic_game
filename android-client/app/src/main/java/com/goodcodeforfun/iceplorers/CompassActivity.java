package com.goodcodeforfun.iceplorers;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;


public class CompassActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mCompass;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mCompass = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        mTextView = (TextView) findViewById(R.id.tvSensor);
        mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CompassActivity.this, CameraDemoActivity.class));
            }
        });
    }

    // The following method is required by the SensorEventListener interface;
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    // The following method is required by the SensorEventListener interface;
// Hook this event to process updates;
    public void onSensorChanged(SensorEvent event) {
        float azimuth = Math.round(event.values[0]);
        // The other values provided are:
        //  float pitch = event.values[1];
        //  float roll = event.values[2];
        mTextView.setText("Azimuth: " + Float.toString(azimuth));
    }

    @Override
    protected void onPause() {
        // Unregister the listener on the onPause() event to preserve battery life;
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mCompass, SensorManager.SENSOR_DELAY_NORMAL);
    }
}