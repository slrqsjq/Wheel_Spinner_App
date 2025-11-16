package com.example.wheel_spinner_app;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {

    private LinearLayout tabSpin, tabSettings;
    private ImageView iconSpin, iconSettings;
    private TextView textSpin, textSettings;
    private SpinFragment spinFragment;
    private SettingsFragment settingsFragment;
    private int currentTab = 0; // 0: spin, 1: settings

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupTabListeners();
        showSpinFragment();
    }

    private void initViews() {
        tabSpin = findViewById(R.id.tab_spin);
        tabSettings = findViewById(R.id.tab_settings);
        iconSpin = findViewById(R.id.icon_spin);
        iconSettings = findViewById(R.id.icon_settings);
        textSpin = findViewById(R.id.text_spin);
        textSettings = findViewById(R.id.text_settings);

        spinFragment = new SpinFragment();
        settingsFragment = new SettingsFragment();
    }

    private void setupTabListeners() {
        tabSpin.setOnClickListener(v -> {
            if (currentTab != 0) {
                showSpinFragment();
                updateTabSelection(0);
            }
        });

        tabSettings.setOnClickListener(v -> {
            if (currentTab != 1) {
                showSettingsFragment();
                updateTabSelection(1);
            }
        });
    }

    private void showSpinFragment() {
        replaceFragment(spinFragment);
        currentTab = 0;
        updateTabSelection(0);
    }

    private void showSettingsFragment() {
        replaceFragment(settingsFragment);
        currentTab = 1;
        updateTabSelection(1);
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    private void updateTabSelection(int selectedTab) {
        // Reset all tabs
        iconSpin.setColorFilter(ContextCompat.getColor(this, R.color.tab_unselected));
        iconSettings.setColorFilter(ContextCompat.getColor(this, R.color.tab_unselected));
        textSpin.setTextColor(ContextCompat.getColor(this, R.color.tab_unselected));
        textSettings.setTextColor(ContextCompat.getColor(this, R.color.tab_unselected));

        // Set selected tab
        if (selectedTab == 0) {
            iconSpin.setColorFilter(ContextCompat.getColor(this, R.color.tab_selected));
            textSpin.setTextColor(ContextCompat.getColor(this, R.color.tab_selected));
        } else {
            iconSettings.setColorFilter(ContextCompat.getColor(this, R.color.tab_selected));
            textSettings.setTextColor(ContextCompat.getColor(this, R.color.tab_selected));
        }
    }
}