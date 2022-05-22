package com.example.experiments;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Menu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_layout);
    }

    public void openExperiment(View view){

        String button_text;
        button_text = ((Button) view).getText().toString();
        if(button_text.equals("Pendulum"))
        {
            Intent intent = new Intent(this, Pendulum.class);
                startActivity(intent);
        }
        else if (button_text.equals("Spring"))
        {
            Intent intent = new Intent(this, Spring.class);
            startActivity(intent);
        }
    }
}