package com.secuyou.android_v22_pin_app.dfu;

import android.content.SharedPreferences;

import androidx.fragment.app.Fragment;
/* loaded from: classes.dex */
public class SettingsFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String SETTINGS_KEEP_BOND = "settings_keep_bond";

    private void updateMBRSize() {
    }

    private void updateNumberOfPacketsSummary() {
    }

    @Override // android.content.SharedPreferences.OnSharedPreferenceChangeListener
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String str) {
    }

    @Override // androidx.fragment.app.Fragment
    public void onResume() {
        super.onResume();
    }

    @Override // androidx.fragment.app.Fragment
    public void onPause() {
        super.onPause();
    }
}
