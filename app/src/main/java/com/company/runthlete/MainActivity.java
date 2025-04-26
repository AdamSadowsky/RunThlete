package com.company.runthlete;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

   private EditText editTextEmail, editTextPassword;
   private Button login;
   private TextView forgotPassword, signUpButton;
   private ImageButton googleLogin, facebookLogin, microsoftLogin;
   FirebaseAuth mAuth;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(), hubActivity.class);
            startActivity(intent);
            finish();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);


        initViews();

        login.setOnClickListener(v -> {
            if(validateInputs()){
                String email = String.valueOf(editTextEmail.getText());
                String password = String.valueOf(editTextPassword.getText());
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(),"Login succcessful", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(), hubActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });


        signUpButton.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, registerActivity.class);
            startActivity(i);

        });

        forgotPassword.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, forgotPasswordActivity.class);
            startActivity(i);
        });
    }

    private void initViews() {
        signUpButton = findViewById(R.id.signUpButton);
        forgotPassword = findViewById(R.id.forgotPassword);
        editTextEmail = findViewById(R.id.emailInput);
        editTextPassword = findViewById(R.id.passwordInput);
        login = findViewById(R.id.loginButton);
        googleLogin = findViewById(R.id.googleLogin);
        facebookLogin = findViewById(R.id.facebookLogin);
        microsoftLogin = findViewById(R.id.microsoftLogin);
        mAuth = FirebaseAuth.getInstance();
    }

    private boolean validateInputs(){

        editTextEmail.setError(null);
        editTextPassword.setError(null);

        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if(TextUtils.isEmpty(email) && TextUtils.isEmpty(password)){
            editTextEmail.setError("Email cannot be left empty");
            editTextPassword.setError("Password cannot be left empty");
            return false;
        }
        else if(TextUtils.isEmpty(email)){
            editTextPassword.setError("Email cannot be left empty");
        }
        else if(TextUtils.isEmpty(password)){
            editTextPassword.setError("Password cannot be left empty");
        }


        return true;
    }
}
