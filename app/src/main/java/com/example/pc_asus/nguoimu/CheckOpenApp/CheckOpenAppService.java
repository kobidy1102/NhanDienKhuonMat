package com.example.pc_asus.nguoimu.CheckOpenApp;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.example.pc_asus.nguoimu.MainActivity;

public class CheckOpenAppService extends Service implements SensorEventListener{

    //    private Shaker shaker=null;
//    SensorManager sManager;
    private static final float SHAKE_THRESHOLD = 5f; // m/S**2
    private static final int MIN_TIME_BETWEEN_SHAKES_MILLISECS = 1000;
    private long mLastShakeTime;
    private SensorManager mSensorMgr;
    int i=0;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("abc", "start service");

//        sManager = (SensorManager)  getSystemService(Context.SENSOR_SERVICE);
//           Sensor s = sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//        sManager.registerListener(this, s, SensorManager.SENSOR_DELAY_NORMAL);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
               try{
                   stopSelf();
               }catch (Exception e){e.getMessage();}
            }
        }, 5000);


        mSensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Listen for shakes
        Sensor accelerometer = mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            mSensorMgr.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }



        return START_STICKY;
    }




    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long curTime = System.currentTimeMillis();
            if ((curTime - mLastShakeTime) > MIN_TIME_BETWEEN_SHAKES_MILLISECS) {

                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                double acceleration = Math.sqrt(Math.pow(x, 2) +
                        Math.pow(y, 2) +
                        Math.pow(z, 2)) - SensorManager.GRAVITY_EARTH;
                //     Log.e("abc", "Acceleration is " + acceleration + "m/s^2");

                if (acceleration > SHAKE_THRESHOLD) {
                    i++;
                    mLastShakeTime = curTime;
                    Log.e("abc", i+ " Shake, Rattle, and Roll "+acceleration);
                    if(i>=2) {
                        i=0;
                        Toast.makeText(CheckOpenAppService.this,"Dang mo app",Toast.LENGTH_LONG).show();
                        Log.e("abc","mở activity");

                        Intent intent1 = new Intent(CheckOpenAppService.this, MainActivity.class);
                        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent1);

                    }else if(i==1){

                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                i = 0;
                                Log.e("abc","delay...set lai i=0");
                            }
                        }, 2000);
                    }
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


}