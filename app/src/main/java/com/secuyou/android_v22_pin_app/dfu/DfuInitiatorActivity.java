package com.secuyou.android_v22_pin_app.dfu;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import no.joymyr.secuyou_reverse.R;

import no.nordicsemi.android.dfu.DfuBaseService;
/* loaded from: classes.dex */
public class DfuInitiatorActivity extends AppCompatActivity implements ScannerFragment.OnDeviceSelectedListener {
    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (!getIntent().hasExtra(DfuBaseService.EXTRA_FILE_PATH)) {
            finish();
        }
        if (bundle == null) {
            ScannerFragment.getInstance(null).show(getSupportFragmentManager(), (String) null);
        }
    }

    @Override // com.secuyou.android_v22_pin_app.dfu.ScannerFragment.OnDeviceSelectedListener
    public void onDeviceSelected(BluetoothDevice bluetoothDevice, String str) {
        Intent intent = getIntent();
        String stringExtra = intent.getStringExtra(DfuBaseService.EXTRA_DEVICE_NAME);
        String stringExtra2 = intent.getStringExtra(DfuBaseService.EXTRA_FILE_PATH);
        String stringExtra3 = intent.getStringExtra(DfuBaseService.EXTRA_INIT_FILE_PATH);
        String address = bluetoothDevice.getAddress();
        if (stringExtra != null) {
            str = stringExtra;
        } else if (str == null) {
            str = getString(R.string.not_available);
        }
        int intExtra = intent.getIntExtra(DfuBaseService.EXTRA_FILE_TYPE, 0);
        boolean booleanExtra = intent.getBooleanExtra(DfuBaseService.EXTRA_KEEP_BOND, false);
        Intent intent2 = new Intent(this, DfuService.class);
        intent2.putExtra(DfuBaseService.EXTRA_DEVICE_ADDRESS, address);
        intent2.putExtra(DfuBaseService.EXTRA_DEVICE_NAME, str);
        intent2.putExtra(DfuBaseService.EXTRA_FILE_TYPE, intExtra);
        intent2.putExtra(DfuBaseService.EXTRA_FILE_PATH, stringExtra2);
        if (intent.hasExtra(DfuBaseService.EXTRA_INIT_FILE_PATH)) {
            intent2.putExtra(DfuBaseService.EXTRA_INIT_FILE_PATH, stringExtra3);
        }
        intent2.putExtra(DfuBaseService.EXTRA_KEEP_BOND, booleanExtra);
        intent2.putExtra(DfuBaseService.EXTRA_UNSAFE_EXPERIMENTAL_BUTTONLESS_DFU, true);
        startService(intent2);
        finish();
    }

    @Override // com.secuyou.android_v22_pin_app.dfu.ScannerFragment.OnDeviceSelectedListener
    public void onDialogCanceled() {
        finish();
    }
}
