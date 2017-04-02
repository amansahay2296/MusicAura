package com.example.mahe.musicplayer;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;

public class Main2Activity extends Activity {

    Button music,emotions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        music = (Button)  findViewById(R.id.button);
        emotions = (Button) findViewById(R.id.button2);
    }

    public void goto_play (View view)
    {
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }



}
