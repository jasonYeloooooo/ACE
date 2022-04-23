package com.example.ace;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.bottomSetting);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.bottomQuestion:
                        startActivity(new Intent(SettingActivity.this,QuestionPage.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.bottomMain:
                        startActivity(new Intent(SettingActivity.this,SecondActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.bottomSetting:
                        return true;
                }
                return false;
            }
        });
    }
}