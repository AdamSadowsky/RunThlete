package com.example.runthlete;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class hubActivity extends AppCompatActivity {

    private Button runningButton, logoutButton;
    private TextView userGreeting;
    private EditText editTextWeight;
    FirebaseAuth auth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_hub);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        runningButton = findViewById(R.id.runningButton);
        logoutButton = findViewById(R.id.logoutButton);
        userGreeting = findViewById(R.id.userGreeting);
        editTextWeight = findViewById(R.id.weightInput);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if(user == null){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
        else{
            String greeting = "Hello " + user.getDisplayName();
            userGreeting.setText(greeting);
        }

        runningButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validateWeight()) {
                    // Get the weight from the EditText and convert to float
                    String weightStr = editTextWeight.getText().toString().trim();
                    float weight = Integer.parseInt(weightStr);
                    Log.d("RegisterActivity", "run button clicked");
                    Intent i = new Intent(hubActivity.this, RunningActivity.class);
                    i.putExtra("userWeight", weight); // Pass the weight with key "userWeight"
                    startActivity(i);
                }
                else{
                    Toast.makeText(getApplicationContext(), "Weight can not be left empty", Toast.LENGTH_SHORT).show();
                }
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private boolean validateWeight() {
        String weight = editTextWeight.getText().toString().trim();
        editTextWeight.setError(null);
        if(weight.isEmpty()){
            editTextWeight.setError("Weight can not be left empty");
            return false;
        }

        return true;
    }
}