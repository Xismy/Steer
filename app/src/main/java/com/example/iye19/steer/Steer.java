package com.example.iye19.steer;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

public class Steer extends AppCompatActivity {

    BluetoothSocket socket = null;
    OutputStream output = null;

    AccelerometerListener accelerometerListener = new AccelerometerListener();
    class AccelerometerListener implements SensorEventListener{
        int N = 10;
        float[] h = createFilter(N, 10, 50);//Coeficientes del filtro paso baja para obtener la gravedad
        float[][] acc = new float[N][3];

        float padX = 0, padY = 0;
        short buttons = 0;

        synchronized void setPad(float x, float y){
            if(x > 1 )
                padX = 1;
            else if(x < -1)
                padX = -1;
            else
                padX = x;
            if(y > 1 )
                padY = 1;
            else if(y < -1)
                padY = -1;
            else
                padY = y;
        }

        synchronized void setButton(int n, boolean down){
            int mask = 1 << n;
            if(down)
                buttons |= mask;
            else
                buttons &= ~mask;
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            for(int i=1;i<N;i++)
                acc[i-1] = acc[i];
            acc[N-1] = event.values;
            float[] g = getGravity(acc);
            if(g[0]>9.8f) g[0] = 9.8f;
            if(g[0]<-9.8f) g[0] = -9.8f;
            if(g[1]>9.8f) g[1] = 9.8f;
            if(g[1]<-9.8f) g[1] = -9.8f;
            short y = (short)(0x7FFF*(g[2]/9.8f));
            short x = (short)(0x7FFF*(g[1]/9.8f));
            short px = (short)(0x7FFF*padX);
            short py = (short)(0x7FFF*padY);
            synchronized (this) {
                try {
                    output.write(new byte[]{
                            (byte)(y >> 8), (byte)(0xFF & y),
                            (byte)(x >> 8), (byte)(0xFF & x),
                            (byte)(px >> 8), (byte)(0xFF & px),
                            (byte)(py >> 8), (byte)(0xFF & py),
                            (byte)(0xFF & buttons)
                    });
                    output.flush();
                } catch (IOException ex) {
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

        float[] createFilter(int N, int fc, int fs){
            float Fc= (float)fc/fs;
            float[] h = new float[N];
            h[0] = 2*Fc;
            Log.v("h", ""+h[0]);
            for(int i=1;i<N;i++){
                h[i] = 2*Fc*(float)(Math.sin(i*2*Math.PI*Fc)/(i*2*Math.PI*Fc));
                Log.v("h", ""+h[i]);
            }
            return h;
        }

        float[] getGravity(float[][] acc){
            float[] g = new float[3];

            for(int i=0;i<N;i++){
                g[0] += h[i]*acc[N-i-1][0];
                g[1] += h[i]*acc[N-i-1][1];
                g[2] += h[i]*acc[N-i-1][2];
            }
            return g;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_steer);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        BluetoothDevice device = (BluetoothDevice)(getIntent().getParcelableExtra("device"));
        new Connect(this).execute(device);

        findViewById(R.id.pad).setOnTouchListener(new View.OnTouchListener() {
            float x0 = 0, y0 = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();

                if(action == MotionEvent.ACTION_DOWN){
                    x0 = event.getX();
                    y0 = event.getY();
                }
                else if(action == MotionEvent.ACTION_MOVE){
                    float x = event.getX() - x0;
                    float y = event.getY() - y0;
                    x /= v.getWidth()/2;
                    y /= v.getHeight()/2;
                    accelerometerListener.setPad(x, y);
                }

                return false;
            }
        });

        View.OnTouchListener buttonsListener =  new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int id = v.getId();
                int action = event.getAction();
                int n = -1;
                if(id == R.id.square)
                    n=0;
                else if(id == R.id.triangle)
                    n=1;
                else if(id == R.id.circle)
                    n=2;
                else if(id == R.id.cross)
                    n=3;

                if(action == MotionEvent.ACTION_DOWN)
                    accelerometerListener.setButton(n, true);
                else if(action == MotionEvent.ACTION_UP)
                    accelerometerListener.setButton(n, false);
                return false;
            }
        };

        findViewById(R.id.square).setOnTouchListener(buttonsListener);
        findViewById(R.id.triangle).setOnTouchListener(buttonsListener);
        findViewById(R.id.circle).setOnTouchListener(buttonsListener);
        findViewById(R.id.cross).setOnTouchListener(buttonsListener);
    }

    void setSocket(BluetoothSocket socket){
        this.socket = socket;
        if(socket ==null)
            finish();
        try {
            output = socket.getOutputStream();
            SensorManager sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
            Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(accelerometerListener , accelerometer, SensorManager.SENSOR_DELAY_GAME);
        }
        catch(IOException ex){}
    }
}
