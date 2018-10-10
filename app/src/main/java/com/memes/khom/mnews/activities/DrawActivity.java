package com.memes.khom.mnews.activities;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.memes.khom.mnews.R;
import com.mrgames13.jimdo.drawingactivity.DrawingActivity;

public class DrawActivity extends AppCompatActivity {

    //Constants
    private final int REQ_DRAWING = 10001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);


      Intent intent =  new Intent(DrawActivity.this, DrawingActivity.class);
        startActivity(intent);


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQ_DRAWING && resultCode == RESULT_OK && data != null) {
            String drawing_path = data.getStringExtra(DrawingActivity.DRAWING_PATH);
            Toast.makeText(this, drawing_path, Toast.LENGTH_LONG).show();
        }
    }

}
