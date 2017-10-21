package com.thacks2.motionsensor;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class EnterData extends AppCompatActivity {

    private String inputText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_data);

        final EditText input = (EditText) findViewById(R.id.input);


        Button send = (Button) findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputText = input.getText().toString();
                sendValue(inputText);
            }
        });
    }

    public void sendValue(String input) {
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("length", input);
        startActivity(i);
    }


}
