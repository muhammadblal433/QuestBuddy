package com.example.androidexample;


import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

public class HomeActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private ActionBarDrawerToggle drawerToggle;
    private String[] drawerItems;

    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Get userId passed from LoginActivity
        userId = getIntent().getIntExtra("userId", -1);

        if (userId == -1) {
            Toast.makeText(this, "Invalid user session", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        drawerLayout = findViewById(R.id.drawer_layout);
        drawerList = findViewById(R.id.left_drawer);
        Toolbar toolbar = findViewById(R.id.toolbar);


        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));


        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Home");

        // Enable home button as drawer toggle
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


        // Add items (ex. Home, Profile, etc) into the navigation bar
        drawerItems = getResources().getStringArray(R.array.drawer_items);
        drawerList.setAdapter(new ArrayAdapter<>(this, R.layout.drawer_item, drawerItems));


        // Handles how to react when each item is clicked on the navigation bar
        drawerList.setOnItemClickListener((parent, view, position, id) -> {
            if(drawerItems[position].equals("Home")){
                Toast.makeText(this, "Already at Home!", Toast.LENGTH_SHORT).show();
                drawerLayout.closeDrawer(GravityCompat.START);
            }
            else if(drawerItems[position].equals("Calendar")){
                Intent intent = new Intent(HomeActivity.this, CalendarActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
                finish();
            }
            else if(drawerItems[position].equals("Logout")){
                Intent intent = new Intent(HomeActivity.this, SignupActivity.class);
                startActivity(intent);
                finish();
                Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, drawerItems[position] + " clicked", Toast.LENGTH_SHORT).show();
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });

        // Setup Drawer Toggle (for hamburger icon)
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawerToggle.getDrawerArrowDrawable()
                .setColor(getResources().getColor(android.R.color.white));

        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
