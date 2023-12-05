package com.paulprice.rssassignment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;


public class Subscribe extends AppCompatActivity {

    Button send_button;
    EditText send_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscribe);

        send_button = findViewById(R.id.send_button_id);
        send_text = findViewById(R.id.send_text_id);

        send_button.setOnClickListener(v -> {
                String str = send_text.getText().toString();
                Intent intent = new Intent(getApplicationContext(), RssReader.class);
                intent.putExtra("message_key", str);
                startActivity(intent);
        });
    }
}