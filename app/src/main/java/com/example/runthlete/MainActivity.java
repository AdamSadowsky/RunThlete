package com.example.runthlete;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    EditText email;
    EditText password;
    Button login;
    TextView forgotPassword;
    TextView signUpButton;
    ImageButton googleLogin;
    ImageButton facebookLogin;
    ImageButton linkedinLogin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        signUpButton = findViewById(R.id.signUpButton);


        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MainActivity", "Sign Up button clicked");
                Intent i = new Intent(MainActivity.this, register.class);
                startActivity(i);

            }
        });
        forgotPassword = findViewById(R.id.forgotPassword);

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MainActivity", "Forgot Password button clicked");
                Intent i = new Intent(MainActivity.this, forgotpassword.class);
                startActivity(i);
            }
        });
    }
}