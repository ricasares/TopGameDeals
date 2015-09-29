package com.ricardocasarez.topgamedeals.view;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.EditTextPreference;
import android.text.TextUtils;
import android.util.Log;

import com.ricardocasarez.topgamedeals.R;
import com.ricardocasarez.topgamedeals.data.DealsContract;
import com.ricardocasarez.topgamedeals.service.DealsAlertService;
import com.ricardocasarez.topgamedeals.service.DealsSyncAdapter;

/**
 * Fragment to display settings populated from settings_page1.xml
 */
public class SettingsFragment extends PreferenceFragmentCompat implements
        SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String LOG_TAG = SettingsFragment.class.getSimpleName();
    public static final String TAG = SettingsFragment.class.getSimpleName();

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.settings_page1);

        SharedPreferences sharedPref = getPreferenceScreen().getSharedPreferences();
        sharedPref.registerOnSharedPreferenceChangeListener(this);

        Preference pref = findPreference(getString(R.string.pref_clear_alerts));
        pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // clear and un-subscribe
                DealsAlertService.clearAndUnsubscribePrceAlerts(getActivity());
                return true;
            }
        });

        setEditPreferenceSummary();
    }

    public void setEditPreferenceSummary() {
        SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
        String minimumValue = sp.getString(getString(R.string.pref_metacritic_edit_key), getString(R.string.pref_metacritic_edit_default));
        String email = sp.getString(getString(R.string.pref_email_edit_key), getString(R.string.pref_email_edit_default));

        EditTextPreference pref = (EditTextPreference) findPreference(getString(R.string.pref_metacritic_edit_key));
        pref.setSummary(minimumValue);

        if (!TextUtils.isEmpty(email)) {
            EditTextPreference emailPref = (EditTextPreference) findPreference(getString(R.string.pref_email_edit_key));
            emailPref.setSummary(email);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (getActivity() != null && isAdded()) {
            // refresh summary
            if (key.equals(getString(R.string.pref_metacritic_edit_key )) ||
                    key.equals(getString(R.string.pref_email_edit_key)) ) {
                setEditPreferenceSummary();
            }

            // if search criteria changed remove all previous deals.
            if (key.equals(getString(R.string.pref_metacritic_edit_key)) ||
                    key.equals(getString(R.string.pref_metacritic_key)) ||
                    key.equals(getString(R.string.pref_aaa_key ))) {
                int deleted = getContext().getContentResolver().delete(DealsContract.GameDealEntry.CONTENT_URI,
                        null,  null);
                Log.i(LOG_TAG, "deleted: " + deleted);
            }
            DealsSyncAdapter.syncImmediately(getActivity());
        }
    }
}
