package com.paulprice.rssassignment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;


public class Login extends AppCompatActivity {

    Button send_button;
    EditText textInputEmail;
    EditText textInputPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        send_button = findViewById(R.id.send_button_id);

        textInputEmail = findViewById(R.id.text_input_email);
        textInputPassword = findViewById(R.id.text_input_password);

        send_button.setOnClickListener(v -> {
            if (!validateEmail() | !validatePassword()) {
                return;
            }else {
                Intent i = new Intent(getApplicationContext(), Subscribe.class);
                startActivity(i);
            }
        });
    }

    private boolean validateEmail() {
        String emailInput = textInputEmail.getText().toString().trim();

        if (emailInput.isEmpty()) {
            textInputEmail.setError("Email can't be empty");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
            textInputEmail.setError("Please enter a valid email address");
            return false;
        } else {
            textInputEmail.setError(null);
            return true;
        }
    }

    private boolean validatePassword() {
        String passwordInput = textInputPassword.getText().toString().trim();

        if (passwordInput.isEmpty()) {
            textInputPassword.setError("Password can't be empty");
            return false;
        } else {
            textInputPassword.setError(null);
            return true;
        }
    }
}