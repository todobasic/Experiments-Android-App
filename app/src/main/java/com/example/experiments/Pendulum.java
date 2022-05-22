package com.example.experiments;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.opencsv.CSVWriter;

import org.w3c.dom.Text;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.Math;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Pendulum extends AppCompatActivity implements SensorEventListener {


    private SensorManager sensorManager;
    private Sensor mAccelero;

    private List<Float> Angles;
    private List<Float> MyTime;

    private List<Float> nizAmplituda;
    private List<Float> nizVremena;


    private TextView gyroValue, listangles, timerText, testTV, maxAnTV, periodTV, deltaTV;
    private EditText custom_time;

    private Button button_stop;

    private boolean readData = false;
    private boolean TraziMax = false;


    private boolean timerStarted = false;


    Timer timer;
    TimerTask timerTask;
    Double time = 0.0;

    private LineGraphSeries<DataPoint> seriesPendulum;

    private GraphView graph;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pendulum_layout);


        gyroValue = findViewById(R.id.acceleroValue);


        listangles = findViewById(R.id.textangles);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelero = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        sensorManager.registerListener(Pendulum.this, mAccelero, SensorManager.SENSOR_DELAY_NORMAL);

        Angles = new ArrayList<Float>();
        MyTime = new ArrayList<Float>();
        nizAmplituda = new ArrayList<Float>();
        nizVremena = new ArrayList<Float>();


        graph = (GraphView) findViewById(R.id.graphPendulum);
        graph.getViewport().setScalable(true);  // activate horizontal zooming and scrolling
        graph.getViewport().setScrollable(true);  // activate horizontal scrolling
        graph.getViewport().setScalableY(true);  // activate horizontal and vertical zooming and scrolling
        graph.getViewport().setScrollableY(true);  // activate vertical scrolling

        GridLabelRenderer gridLabel = graph.getGridLabelRenderer();
        gridLabel.setVerticalAxisTitle("Kut");
        gridLabel.setHorizontalAxisTitle("Vrijeme");

        seriesPendulum = new LineGraphSeries<>();

        timer = new Timer();
        timerText = (TextView) findViewById(R.id.timerText);
        custom_time = (EditText) findViewById(R.id.custom_time);
        maxAnTV = (TextView) findViewById(R.id.maxAn_tv);
       // periodTV = (TextView) findViewById(R.id.period_tv);
       // deltaTV = (TextView) findViewById(R.id.delta_tv);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        Sensor sensor = event.sensor;
        if (sensor.getType() == Sensor.TYPE_ORIENTATION) {
            gyroValue.setText("Angle: " + String.format("%.2f", event.values[2]));
        }

        if (readData) {

            readData = false;

            Angles.add(event.values[2]);
        }


    }

    public void listangles(View view) {


    }

    public void startlisting(View view) {


        if (custom_time.getText().toString().trim().length() <= 0) {

            Toast.makeText(Pendulum.this, "Please fill in your wanted time period", Toast.LENGTH_SHORT).show();

        } else {

            Handler handler = new Handler();
             handler.postDelayed(new Runnable() {
              public void run() {
            long tStart = System.currentTimeMillis();

            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {

                    readData = true;

                    long tEnd = System.currentTimeMillis();
                    long tDelta = tEnd - tStart;
                    double elapsedSeconds = tDelta / 1000.0;

                    MyTime.add((float) elapsedSeconds);

                }
            }, 0, 25);


            listangles.setText("");
            Angles.clear();

            timerStarted = true;
            startTimer();
            graph.removeAllSeries();
            long tempcustomTime = Long.parseLong(custom_time.getText().toString());
            long actualcustomTime = tempcustomTime * 1000;

            new CountDownTimer(actualcustomTime + 50, 1000) {
                double x = 0, y;
                float maxAvalue = 0;
                float minAvalue = 0;

                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {
                    sensorManager.unregisterListener(Pendulum.this);
                    readData = false;
                    timer.cancel();
                    for (int i = 0; i < Angles.size(); i++) {

                        listangles.setText(listangles.getText() + "Angle: " + String.format("%.2f", Angles.get(i)) + "       " + "Time: " + String.format("%.3f", MyTime.get(i)) + "\n");
                        x = MyTime.get(i);
                        y = Angles.get(i);
                        seriesPendulum.appendData(new DataPoint(x, y), true, Angles.size());

                        if (Angles.get(i) > maxAvalue) {
                            maxAvalue = Angles.get(i);
                        } else if (Angles.get(i) < minAvalue) {
                            minAvalue = Angles.get(i);
                        }
                    }

                    if (minAvalue * (-1) > maxAvalue) {
                        maxAvalue = minAvalue;
                    }

                    String tempmaxA = Float.toString(maxAvalue);
                    maxAnTV.setText("Theta: " + tempmaxA);

                    graph.addSeries(seriesPendulum);
                    timerTask.cancel();

                    // if (maxAvalue > 0) {

                    //} else if (maxAvalue < 0) {
                    //  TraziMax = true;
                    //}

                    // for (int i = 1; i < Angles.size(); i++) {

                    //  if (TraziMax) {
                    //      if (Angles.get(i - 1) > Angles.get(i)) {
                    //          TraziMax = false;
                    //          nizAmplituda.add(Angles.get(i - 1));
                    //          nizVremena.add(MyTime.get(i - 1));
                    //      }
                    //  } else {
                    //      if (Angles.get(i - 1) < Angles.get(i)) {
                    //          TraziMax = true;
                    //          nizAmplituda.add(Angles.get(i - 1));
                    //          nizVremena.add(MyTime.get(i - 1));
                    //      }
                    //  }
                    //}
                    //double prosPrigSum = 0.0;
                    //double prosPrig = 0.0;

                    //float brojAmplituda = nizAmplituda.size();
                    //brojAmplituda = brojAmplituda / 2;


                    //float prosPeriod = nizVremena.get(nizVremena.size() - 1) / brojAmplituda;

                    //periodTV.setText("Period: " + Float.toString(prosPeriod));

                    //brojAmplituda = 0;
                    //for (int i = 0; i < nizAmplituda.size() - 2; i = i + 2) {
                        //prosPrigSum = prosPrigSum + Math.log(nizAmplituda.get(i) / nizAmplituda.get(i + 2)) / prosPeriod;
                        //brojAmplituda++;
                        //}

                    //prosPrig = prosPrigSum / brojAmplituda;
                    //  deltaTV.setText("Delta: " + Float.toString((float) prosPrig));

                }
            }.start();
        }
          }, 1400); //1500 = 1.5 seconds, time in milli before it happens.


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


    public void export(View view) throws IOException
    {

        int num = 0;
        String filename = "angles" + ".csv";

        File output = new File(getApplicationContext().getExternalFilesDir(null),filename);

            while(output.exists()){
                filename = "angles" + (num++) + ".csv";
                output = new File(getApplicationContext().getExternalFilesDir(null),filename);
            }

              try {
          FileOutputStream fileout = new FileOutputStream(output.getAbsolutePath());
          OutputStreamWriter outputWriter = new OutputStreamWriter(fileout);

          for(int i = 0; i < Angles.size(); i++) {
              outputWriter.write(("Angle" + "," +  Angles.get(i) + ","));
              outputWriter.write(("Time" + "," + MyTime.get(i).toString() + "\n"));
          }
                  Toast.makeText(getBaseContext(), "File saved successfully!", Toast.LENGTH_SHORT).show();
                  outputWriter.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
    }

}