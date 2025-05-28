package com.company.runthlete;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.company.runthlete.databinding.ActivityHubBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class hubActivity extends AppCompatActivity {

    private static final int PERMISSIONS_FINE_LOCATION = 1;
    private ActivityHubBinding binding;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityHubBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        drawerLayout = binding.drawerLayout;//Side navigation view
        NavigationView navigationView = binding.options;
        navigationView.setItemIconTintList(null);
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Apply only top inset so the toolbar sits just below the status bar.
            v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), v.getPaddingBottom());
            return windowInsets;
        });

        BottomNavigationView bottomNavigationView = binding.bottomNavBar;//Sets bottom navigation view
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNavBar, (v, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Apply only bottom inset so the bottom nav sits just above the navigation bar.
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), systemBars.bottom);
            return windowInsets;
        });

        //Handles view change of side navigation view
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();


        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        //Checks whether location permissions have been provided
        if (ActivityCompat.checkSelfPermission(hubActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Log.d("Debug", "Location accessible");

            //Checks the state of gps
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Toast.makeText(hubActivity.this, "GPS is turned off. Enable it before starting!", Toast.LENGTH_LONG).show();
                Log.d("Debug", "GPS not available");
                finish();
                return;
            }

            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseUser user = auth.getCurrentUser();

            //Navigates user to login screen if they are not registered
            if (user == null) {
                Intent intent = new Intent(this, MainActivity.class);
                Log.d("Debug", "User isn't logged in switching to login screen");
                startActivity(intent);
                finish();
            } else {
                //Navigates user to home fragment if they are registered
                Log.d("Debug", "User is logged in switching to home fragment");
                replaceFrag(new HomeFragment());
            }

            //Retrieves running intent to determine navigation view
            boolean isRunning = getIntent().getBooleanExtra("isRunning", true);

            //Navigates to post run fragment if user just completed their run
            if(!isRunning){
                PostRunFragment postRunFragment = new PostRunFragment();
                Bundle args = getIntent().getExtras();//Retrieves all extras in a bundle
                if(args != null){
                    postRunFragment.setArguments(args);
                    replaceFrag(postRunFragment);
                }
            }

            getIntent().removeExtra("isRunning");//Resets isRunning value

            //Navigates to user to selected fragment in the side navigation view
            navigationView.setNavigationItemSelectedListener(item -> {
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                if ( item.getItemId() == R.id.progression) {
                    if (!(currentFragment instanceof ProgressionFragment)) {
                        replaceFrag(new ProgressionFragment());
                    }
                } else if (item.getItemId() == R.id.groups) {
                    if (!(currentFragment instanceof GroupsFragment)) {
                        replaceFrag(new GroupsFragment());
                    }
                } else if (item.getItemId() == R.id.home) {
                    if (!(currentFragment instanceof HomeFragment)) {
                        replaceFrag(new HomeFragment());
                    }
                } else if (item.getItemId() == R.id.leaderboards) {
                    if (!(currentFragment instanceof LeaderboardsFragment)) {
                        replaceFrag(new LeaderboardsFragment());
                    }
                } else if (item.getItemId() == R.id.profile) {
                    if (!(currentFragment instanceof ProfileFragment)) {
                        replaceFrag(new ProfileFragment());
                    }
                } else if (item.getItemId() == R.id.savedRuns) {
                    if (!(currentFragment instanceof SavedRunsFragment)) {
                        replaceFrag(new SavedRunsFragment());
                    }
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            });


            bottomNavigationView.setItemIconTintList(null);

            //Navigates user to selected fragment in the bottom navigation view
            bottomNavigationView.setOnItemSelectedListener(item -> {
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

                if (item.getItemId() == R.id.progression) {
                    if (!(currentFragment instanceof ProgressionFragment)) {
                        replaceFrag(new ProgressionFragment());
                    }
                    return true;
                } else if (item.getItemId() == R.id.groups) {
                    if (!(currentFragment instanceof GroupsFragment)) {
                        replaceFrag(new GroupsFragment());
                    }
                    return true;
                } else if (item.getItemId() == R.id.home) {
                    if (!(currentFragment instanceof HomeFragment)) {
                        replaceFrag(new HomeFragment());
                    }
                    return true;
                } else if (item.getItemId() == R.id.leaderboards) {
                    if (!(currentFragment instanceof LeaderboardsFragment)) {
                        replaceFrag(new LeaderboardsFragment());
                    }
                    return true;
                } else if (item.getItemId() == R.id.profile) {
                    if (!(currentFragment instanceof ProfileFragment)) {
                        replaceFrag(new ProfileFragment());
                    }
                    return true;
                }

                return true;
            });
        } else {
            Log.d("Debug", "Missing permissions to start activity");
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);  // Request permission if not granted
        }
    }


    //Verifies whether program has necessary permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Handles the result of the permission request
        if (requestCode == PERMISSIONS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted: you may start tracking if needed (or simply wait for user action)
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                Log.d("Permissions", "Permissions granted");
            } else {
                Log.d("Permissions", "Permissions denied");
                Toast.makeText(this, "This app requires location permission to function", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    //Navigates user to the selected fragment
    private void replaceFrag(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }

    //Life cycle methods
    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}