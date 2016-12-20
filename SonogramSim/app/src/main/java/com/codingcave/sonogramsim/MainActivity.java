package com.codingcave.sonogramsim;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;

import static android.view.MotionEvent.INVALID_POINTER_ID;

public class MainActivity extends AppCompatActivity {

    SonogramView drawing;
    SonogramDrawer sim;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sim = new SonogramDrawer();
        sim.load_sonogram_data(getResources().openRawResource(R.raw.sonodata));
        sim.set_zoom(2);

        ViewGroup myLayout = (ViewGroup) findViewById(R.id.content_main);
        drawing = new SonogramView(this);
        myLayout.addView(drawing);

        drawing.setTag(R.raw.sonodata, sim);

        final Handler handler=new Handler();
        handler.post(new Runnable(){
            @Override
            public void run() {
                drawing.invalidate();
                handler.postDelayed(this,100);
            }
        });

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    }

    protected void onResume() {
        super.onResume();

        mSensorManager.registerListener(drawing, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(drawing);
    }

    private class SonogramView extends View implements SensorEventListener {

        float roll_angle;
        float tilt_angle;

        float max_roll = (1*(float)Math.PI / 10);
        float max_tilt = (1*(float)Math.PI / 10);

        int mActivePointerId = INVALID_POINTER_ID;
        float mLastTouchX;
        float mLastTouchY;

        public SonogramView(Context context) {
            super(context);

            setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    System.out.println("#######");
                    System.out.println(roll_angle);

                    final int action = MotionEventCompat.getActionMasked(event);

                    switch (action) {
                        case MotionEvent.ACTION_DOWN: {
                            final int pointerIndex = MotionEventCompat.getActionIndex(event);
                            final float x = MotionEventCompat.getX(event, pointerIndex);
                            final float y = MotionEventCompat.getY(event, pointerIndex);

                            // Remember where we started (for dragging)
                            mLastTouchX = x;
                            mLastTouchY = y;
                            break;
                        }

                        case MotionEvent.ACTION_MOVE: {
                            final int pointerIndex = MotionEventCompat.getActionIndex(event);
                            final float x = MotionEventCompat.getX(event, pointerIndex);
                            final float y = MotionEventCompat.getY(event, pointerIndex);

                            // Calculate the distance moved
                            final float dx = x - mLastTouchX;
                            final float dy = y - mLastTouchY;

                            double d_factor = 1000.0;

                            roll_angle += dx/d_factor;
                            tilt_angle += dy/d_factor;

                            if (Math.abs(roll_angle) > max_roll) {
                                roll_angle = Math.signum(roll_angle)*max_roll;
                            }
                            if (Math.abs(tilt_angle) > max_tilt) {
                                tilt_angle = Math.signum(tilt_angle)*max_tilt;
                            }

                            invalidate();

                            // Remember this touch position for the next move event
                            mLastTouchX = x;
                            mLastTouchY = y;

                            break;
                        }

                        case MotionEvent.ACTION_UP: {
                            mActivePointerId = INVALID_POINTER_ID;
                            break;
                        }

                        case MotionEvent.ACTION_CANCEL: {
                            mActivePointerId = INVALID_POINTER_ID;
                            break;
                        }

                        case MotionEvent.ACTION_POINTER_UP: {
                            final int pointerIndex = MotionEventCompat.getActionIndex(event);
                            final int pointerId = MotionEventCompat.getPointerId(event, pointerIndex);

                            if (pointerId == mActivePointerId) {
                                // This was our active pointer going up. Choose a new
                                // active pointer and adjust accordingly.
                                final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                                mLastTouchX = MotionEventCompat.getX(event, newPointerIndex);
                                mLastTouchY = MotionEventCompat.getY(event, newPointerIndex);
                                mActivePointerId = MotionEventCompat.getPointerId(event, newPointerIndex);
                            }
                            break;
                        }
                    }

                    return true;
                }
            });
        }

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            // GYROSCOPE CODE HERE
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // GYROSCOPE CODE HERE
        }

        protected void onDraw(Canvas canvas) {
            Paint p = new Paint();
            p.setColor(Color.BLACK);

            SonogramDrawer sim = (SonogramDrawer) this.getTag(R.raw.sonodata);

            int x = (canvas.getWidth() / 2) - sim.get_slice_width()/2;
            int y = (canvas.getHeight() / 2) - sim.get_slice_height()/2;

            sim.update_sonogram_slice(0.5, roll_angle, tilt_angle);
            sim.draw_sonogram(canvas, p, x, y);
        }
    }
}
