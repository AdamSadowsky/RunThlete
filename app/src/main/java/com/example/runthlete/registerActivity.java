package com.example.runthlete;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import com.google.firebase.firestore.FirebaseFirestore;
public class registerActivity extends AppCompatActivity {

    private Button backButton, registerButton;
    private TextInputEditText editTextFirstName, editTextLastName, editTextEmail, editTextPassword, editTextPhoneNumber, editTextGender, editTextDOB;
    private RadioButton maleButton, femaleButton;
    private AutoCompleteTextView dayDropDown, monthDropDown, yearDropDown;
    FirebaseAuth mAuth;

    private static final String[] months =
            {"January", "February", "March",
                    "April", "May", "June",
                    "July", "August", "September",
                    "October", "November", "December"};

    private static final List<String> days = new ArrayList<>();
    private static final List<String> years = new ArrayList<>();

    static {
        for (int i = 1; i <= 31; i++) {
            days.add(String.valueOf(i));
        }
        for (int i = 1920; i <= 2012; i++) {
            years.add(String.valueOf(i));
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        dropDownMenus();



        backButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("RegisterActivity", "Back to login page button clicked");
                Intent i = new Intent(registerActivity.this, MainActivity.class);
                startActivity(i);
            }
        });


        registerButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                v.clearFocus();
                if (validateInput()) {
                    registerUser();
                }
            }
        });
    }

    private void registerUser() {
        String email = String.valueOf(editTextEmail.getText());
        String password = String.valueOf(editTextPassword.getText());
        String firstName = String.valueOf(editTextFirstName.getText());
        String lastName = String.valueOf(editTextLastName.getText());
        String phoneNumber = String.valueOf(editTextPhoneNumber.getText());
        String day = String.valueOf(dayDropDown.getText());
        String month = String.valueOf(monthDropDown.getText());
        String year = String.valueOf(yearDropDown.getText());

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            mAuth.getCurrentUser().reload().addOnSuccessListener(aVoid -> {
                                String userId = mAuth.getCurrentUser().getUid();
                                Map<String, Object> userMap = new HashMap<>();
                                userMap.put("firstName", firstName);
                                userMap.put("lastName", lastName);
                                userMap.put("phoneNumber", phoneNumber);

                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                db.collection("users").document(userId).set(userMap)
                                        .addOnSuccessListener(aVoid1 -> Log.d("RegisterActivity", "User added to Firestore"))
                                        .addOnFailureListener(e -> Log.e("RegisterActivity", "Error adding user", e));


                                Toast.makeText(registerActivity.this, "Succesfully registered", Toast.LENGTH_SHORT).show();
                                Log.d("RegisterActivity", "Register button clicked");
                                Intent i = new Intent(registerActivity.this, hubActivity.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clears backstack
                                startActivity(i);
                                finish();
                            });
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(registerActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        registerButton = findViewById(R.id.register);

        editTextFirstName = findViewById(R.id.firstName);
        editTextLastName = findViewById(R.id.lastName);
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        editTextPhoneNumber = findViewById(R.id.phoneNumber);
        editTextGender = findViewById(R.id.gender);
        editTextDOB = findViewById(R.id.dateOfBirth);

        maleButton = findViewById(R.id.male);
        femaleButton = findViewById(R.id.female);

        dayDropDown = findViewById(R.id.day);
        monthDropDown = findViewById(R.id.month);
        yearDropDown = findViewById(R.id.year);

        mAuth = FirebaseAuth.getInstance();
    }

    private void dropDownMenus() {
        monthDropDown.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, months));
        dayDropDown.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, days));
        yearDropDown.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, years));
    }

    private boolean validateInput() {

        boolean hasError = false;

        editTextFirstName.setError(null);
        editTextLastName.setError(null);
        editTextPhoneNumber.setError(null);
        editTextEmail.setError(null);
        editTextPassword.setError(null);
        dayDropDown.setError(null);
        monthDropDown.setError(null);
        yearDropDown.setError(null);
        editTextGender.setError(null);
        editTextDOB.setError(null);

        String firstName = editTextFirstName.getText().toString().trim();
        String lastName = editTextLastName.getText().toString().trim();
        String phoneNumber = editTextPhoneNumber.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String day = dayDropDown.getText().toString().trim();
        String month = monthDropDown.getText().toString().trim();
        String year = yearDropDown.getText().toString().trim();

        if (TextUtils.isEmpty(firstName)) {
            hasError = true;
            editTextFirstName.setError("First name can't be empty");
        }
        if (TextUtils.isEmpty(lastName)) {
            hasError = true;
            editTextLastName.setError("Last name can't be empty");
        }
        if (TextUtils.isEmpty(phoneNumber)) {
            hasError = true;
            editTextPhoneNumber.setError("Phone number can't be empty");
        }
        if (TextUtils.isEmpty(email)) {
            hasError = true;
            editTextEmail.setError("Email can't be empty");
        }
        if (!maleButton.isChecked() && !femaleButton.isChecked()) {
            hasError = true;
            editTextGender.setError("Gender can't be empty");
        }
        if (TextUtils.isEmpty(day) || TextUtils.isEmpty(month) || TextUtils.isEmpty(year)) {
            hasError = true;
            editTextDOB.setError("All DOB fields must be filled out");
        }
        if(!validatePassword(password)){
            hasError = true;
        }

        return !hasError;
    }

    private boolean validatePassword(String password) {

        StringBuilder errors = new StringBuilder();

        if (TextUtils.isEmpty(password)) {
            errors.append("Password cant be empty");
        }
        if (password.length() < 6 || password.length() > 16) {
            errors.append("Password must be between 6 and 16 characters ");
        }
        if (!Pattern.compile(".*[a-z].*").matcher(password).find()) {
            errors.append("Password must contain at least 1 lowercase letter");
        }
        if (!Pattern.compile(".*[A-Z].*").matcher(password).find()) {
            errors.append("Password must contain at least 1 uppercase letter");
        }
        if (!Pattern.compile(".*\\d.*").matcher(password).find()) {
            errors.append("Password must contain at least 1 number");
        }
        if (!Pattern.compile(".*[!@#$%^&*()_+=-].*").matcher(password).find()) {
            errors.append("Password must contain at least 1 symbol");
        }
        if(errors.length() > 0){
            editTextPassword.setError(errors.toString().trim());
            return false;
        }
        return true;
    }
}


