package com.example.experiments;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Spring extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor mAccelero;

    private List<Float> Accelerations;
    private List<Float> MyTime;

    private TextView acceleValue, listaccelerations, maxAcctv, timerText;
    private EditText custom_time;

    private Button button_stop;

    private boolean readData=false;
    private boolean timerStarted=false;


    Timer timer;
    TimerTask timerTask;
    Double time = 0.0;

    private LineGraphSeries<DataPoint> seriesSpring;

    private GraphView graph;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spring_layout);

        acceleValue = findViewById(R.id.accelerationValue);
        maxAcctv = findViewById(R.id.maxAcc_tv);


        listaccelerations = findViewById(R.id.textaccelerations);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelero = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(Spring.this, mAccelero, SensorManager.SENSOR_DELAY_NORMAL);

        Accelerations = new ArrayList<Float>();
        MyTime = new ArrayList<Float>();

        graph = (GraphView) findViewById(R.id.graphSpring);
        graph.getViewport().setScalable(true);  // activate horizontal zooming and scrolling
        graph.getViewport().setScrollable(true);  // activate horizontal scrolling
        graph.getViewport().setScalableY(true);  // activate horizontal and vertical zooming and scrolling
        graph.getViewport().setScrollableY(true);  // activate vertical scrolling

        GridLabelRenderer gridLabel = graph.getGridLabelRenderer();
        gridLabel.setVerticalAxisTitle("Ubrzanje");
        gridLabel.setHorizontalAxisTitle("Vrijeme");

        seriesSpring = new LineGraphSeries<>();

        timer = new Timer();
        timerText = (TextView) findViewById(R.id.timerText);
        custom_time = (EditText) findViewById(R.id.custom_time);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        if(sensor.getType() == Sensor.TYPE_ACCELEROMETER){

            acceleValue.setText("Acceleration: " + String.format("%.2f", 9.81 - event.values[1]));

            if(readData) {

                readData = false;

                Accelerations.add(event.values[1]);
            }

        }

    }




    public void listaccelerations(View view) {

    }

    public void startlisting(View view) {
        if (custom_time.getText().toString().trim().length() <= 0) {

            Toast.makeText(Spring.this, "Please fill in your wanted time period", Toast.LENGTH_SHORT).show();

        } else {

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {

                    long tStart = System.currentTimeMillis();

                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            //what you want to do
                            readData = true;

                            long tEnd = System.currentTimeMillis();
                            long tDelta = tEnd - tStart;
                            double elapsedSeconds = tDelta / 1000.0;

                            MyTime.add((float) elapsedSeconds);

                        }
                    }, 0, 100);//wait 0 ms before doing the action and do it evry 1000ms (1second)



                    listaccelerations.setText("");
                    Accelerations.clear();

                    timerStarted=true;
                    startTimer();
                    graph.removeAllSeries();
                    long tempcustomTime = Long.parseLong(custom_time.getText().toString());
                    long actualcustomTime = tempcustomTime * 1000;

                    new CountDownTimer( actualcustomTime + 50, 1000) {
                        double x=0,y;
                        float maxAvalue=0;


                        public void onTick(long millisUntilFinished) {
                        }

                        public void onFinish() {
                            sensorManager.unregisterListener(Spring.this);
                            readData=false;
                            timer.cancel();//stop the timer
                            for(int i=1;i<Accelerations.size();i++){

                                listaccelerations.setText(listaccelerations.getText() + "Acceleration: " + String.format("%.0f", Accelerations.get(i)) + "       " + "Time: " + String.format("%.3f", MyTime.get(i)) + "\n");
                                x = MyTime.get(i);
                                y = Accelerations.get(i);
                                seriesSpring.appendData(new DataPoint(x,y),true,999999999);

                                if( Accelerations.get(i) > maxAvalue )
                                {
                                    maxAvalue = Accelerations.get(i);
                                }
                            }

                            String tempmaxA = Float.toString(Float.parseFloat(String.format("%.0f", maxAvalue)));
                            maxAcctv.setText("Max Acceleration: " + tempmaxA);

                            graph.addSeries(seriesSpring);
                            timerTask.cancel();
                        }
                    }.start();


                } }, 1400);



        }
    }

    private void startTimer()
    {

        timerTask = new TimerTask()
        {
            @Override
            public void run()
            {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run()
                    {
                        time++;
                        timerText.setText(getTimerText());
                    }
                });

            }
        };
        timer.scheduleAtFixedRate(timerTask,0,1000);
    }

    public String getTimerText()
    {
        int rounded = (int) Math.round(time);

        int seconds = ((rounded % 86400) % 3600) % 60;
        return formatTime(seconds);
    }

    private String formatTime(int seconds)
    {
        return "00" + ":" + "00" + ":" + String.format("%02d",seconds);
    }


    public void export2(View view) {

        int num=0;
        String filename = "accelerations" + ".csv";

        File output = new File(getApplicationContext().getExternalFilesDir(null),filename);

        while(output.exists()){
            filename = "accelerations" + (num++) + ".csv";
            output = new File(getApplicationContext().getExternalFilesDir(null),filename);
        }

        try {
            FileOutputStream fileout = new FileOutputStream(output.getAbsolutePath());
            OutputStreamWriter outputWriter = new OutputStreamWriter(fileout);

            for(int i = 1; i < Accelerations.size(); i++) {
                outputWriter.write(("Acceleration" + "," + String.format("%.0f", Accelerations.get(i)) + ","));
                outputWriter.write(("Time" + "," + MyTime.get(i).toString() + "\n"));
            }
            Toast.makeText(getBaseContext(), "File saved successfully!", Toast.LENGTH_SHORT).show();
            outputWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
