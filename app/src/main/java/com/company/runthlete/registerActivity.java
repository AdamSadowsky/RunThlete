package com.company.runthlete;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class registerActivity extends AppCompatActivity {

    private Button backButton, registerButton;
    private TextInputEditText editTextFirstName, editTextLastName, editTextEmail, editTextPassword, editTextGender, editTextDOB;
    private RadioButton maleButton, femaleButton;
    private AutoCompleteTextView dayDropDown, monthDropDown, yearDropDown;
    private String userID;
    private FirebaseAuth fAuth;

    //Sets the values for the months dropdown menu
    private static final String[] months =
            {"January", "February", "March",
                    "April", "May", "June",
                    "July", "August", "September",
                    "October", "November", "December"};

    private static final List<String> days = new ArrayList<>();
    private static final List<String> years = new ArrayList<>();
    //Sets the values for the days dropdown menu
    static {
        for (int i = 1; i <= 31; i++) {
            days.add(String.valueOf(i));
        }
        //Sets the values for the year dropdown menu
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

        //Navigates user back to login screen on click
        backButton.setOnClickListener(v -> {
            Log.d("RegisterActivity", "Back to login page button clicked");
            Intent i = new Intent(registerActivity.this, MainActivity.class);
            startActivity(i);
        });

        //Validates user input and registers them into Firebase
        registerButton.setOnClickListener(v -> {
            v.clearFocus();
            if (validateInput()) {
                registerUser();
            }
        });
    }

    private void registerUser() {
        //Assigns values for fields submitted
        String email = String.valueOf(editTextEmail.getText());
        String password = String.valueOf(editTextPassword.getText());
        String firstName = String.valueOf(editTextFirstName.getText());
        String lastName = String.valueOf(editTextLastName.getText());
        String day = String.valueOf(dayDropDown.getText());
        String month = String.valueOf(monthDropDown.getText());
        String year = String.valueOf(yearDropDown.getText());
        int birthYear = Integer.parseInt(year);
        int birthDay = Integer.parseInt(day);

        int numMonth = -1;
        //Retrieves integer value for users birth month
        for (int i = 0; i < months.length; i++) {
            if (month.equals(months[i])) {
                numMonth = i + 1;
                break;
            }
        }

        String DOB = String.format(Locale.getDefault(), this.getString(R.string.DOB), numMonth, birthDay, birthYear);


        String gender;
        //Assigns value for gender selected
        if(maleButton.isChecked()){
            gender = "Male";
        } else if(femaleButton.isChecked()){
            gender = "Female";
        } else {
            gender = "Unspecified";
        }
        fAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(task -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            userID = Objects.requireNonNull(fAuth.getCurrentUser()).getUid();
            Objects.requireNonNull(user).updateProfile(
                    new UserProfileChangeRequest.Builder()
                            .setDisplayName(firstName)
                            .build())
                    .addOnFailureListener(e ->
                                Toast.makeText(this, "update profile failed", Toast.LENGTH_SHORT).show()
                    );
            //Creates a HashMap and puts users info into it
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("firstName", firstName);
                    userMap.put("lastName", lastName);
                    userMap.put("DOB", DOB);
                    userMap.put("gender", gender);


                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    DocumentReference infoRef =
                            db.collection("users")
                                    .document(userID)
                                    .collection("userInfo")
                                    .document("demographics");
                    //Navigates to hub activity once demographics document submission completes
                    infoRef.set(userMap)
                            .addOnSuccessListener(aVoid -> {
                                Log.d("RunFrag", "Run data submitted");
                                startActivity(new Intent(this, hubActivity.class));
                                finish();
                            })
                            .addOnFailureListener(aVoid -> Log.d("RunFrag", "Failed run data submission"));
                            })
                            .addOnFailureListener(e -> {
                                Log.e("RegisterActivity", "Error adding user", e);
                                Toast.makeText(this, "Error, Please try again", Toast.LENGTH_SHORT).show();
                            });

    }

    //Initializes all layout views
    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        registerButton = findViewById(R.id.register);

        editTextFirstName = findViewById(R.id.firstName);
        editTextLastName = findViewById(R.id.lastName);
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        editTextGender = findViewById(R.id.gender);
        editTextDOB = findViewById(R.id.dateOfBirth);

        maleButton = findViewById(R.id.male);
        femaleButton = findViewById(R.id.female);

        dayDropDown = findViewById(R.id.day);
        monthDropDown = findViewById(R.id.month);
        yearDropDown = findViewById(R.id.year);

        fAuth = FirebaseAuth.getInstance();
    }

    //Sets dropdown menus values
    private void dropDownMenus() {
        monthDropDown.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, months));
        dayDropDown.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, days));
        yearDropDown.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, years));
    }

    //Validates whether users submitted input meets criteria
    private boolean validateInput() {

        boolean hasError = false;
        //Resets errors when user submits new input
        editTextFirstName.setError(null);
        editTextLastName.setError(null);
        editTextEmail.setError(null);
        editTextPassword.setError(null);
        dayDropDown.setError(null);
        monthDropDown.setError(null);
        yearDropDown.setError(null);
        editTextGender.setError(null);
        editTextDOB.setError(null);
        //Assigns submitted field values
        String firstName = Objects.requireNonNull(editTextFirstName.getText()).toString().trim();
        String lastName = Objects.requireNonNull(editTextLastName.getText()).toString().trim();
        String email = Objects.requireNonNull(editTextEmail.getText()).toString().trim();
        String password = Objects.requireNonNull(editTextPassword.getText()).toString().trim();
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

    //Validates whether users password meets all criteria
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


