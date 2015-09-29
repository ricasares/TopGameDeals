package com.ricardocasarez.topgamedeals;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.ricardocasarez.topgamedeals.view.SettingsFragment;

/**
 * Activity that hosts SettingsFragment to display settings page.
 */
public class SettingsActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.activity_settings_title);

        SettingsFragment fragment = (SettingsFragment) getSupportFragmentManager()
                .findFragmentByTag(SettingsFragment.TAG);
        if (fragment == null) {
            fragment = new SettingsFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.settings_fragment_container, fragment, SettingsFragment.TAG)
                    .commit();
        }
    }
}
