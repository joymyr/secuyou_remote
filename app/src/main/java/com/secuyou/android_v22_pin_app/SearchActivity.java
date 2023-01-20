package com.secuyou.android_v22_pin_app;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import no.joymyr.secuyou_reverse.R;

/* loaded from: classes.dex */
public class SearchActivity extends AppCompatActivity implements searchFragment.OnListFragmentInteractionListener, selectPin.OnFragmentInteractionListener {
    selectPin PinFragment;
    searchFragment SearchFragment;
    FragmentManager fragmentManager;
    SearchAdapter mAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    public BleMulticonnectProfileService mBluetoothLeService;
    private boolean mConnected = false;
    boolean mBound = false;
    boolean saveNow = false;
    private final BroadcastReceiver mCommonBroadcastReceiver = new BroadcastReceiver() { // from class: com.secuyou.android_v22_pin_app.SearchActivity.1
        /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            char c;
            BluetoothDevice bluetoothDevice = (BluetoothDevice) intent.getParcelableExtra(BleMulticonnectProfileService.EXTRA_DEVICE);
            String action = intent.getAction();
            action.hashCode();
            switch (action.hashCode()) {
                case -2064468773:
                    if (action.equals(BleMulticonnectProfileService.BROADCAST_SERVICES_DISCOVERED)) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case -2021257862:
                    if (action.equals(BleMulticonnectProfileService.BROADCAST_ERROR)) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case -930173768:
                    if (action.equals(BleMulticonnectProfileService.BROADCAST_LOCKSTATE)) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case -683787357:
                    if (action.equals(BleMulticonnectProfileService.BROADCAST_BOND_STATE)) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case -258925650:
                    if (action.equals(BleMulticonnectProfileService.BROADCAST_CONNECTION_STATE)) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case -16691854:
                    if (action.equals(BleMulticonnectProfileService.BROADCAST_LOCKNAME)) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case 245163946:
                    if (action.equals(BleMulticonnectProfileService.BROADCAST_DEVICE_READY)) {
                        c = 6;
                        break;
                    }
                    c = 65535;
                    break;
                case 1229384843:
                    if (action.equals(BleMulticonnectProfileService.BROADCAST_LOCKSTATUS)) {
                        c = 7;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    boolean booleanExtra = intent.getBooleanExtra(BleMulticonnectProfileService.EXTRA_SERVICE_PRIMARY, false);
                    boolean booleanExtra2 = intent.getBooleanExtra(BleMulticonnectProfileService.EXTRA_SERVICE_SECONDARY, false);
                    if (booleanExtra) {
                        SearchActivity.this.onServicesDiscovered(bluetoothDevice, booleanExtra2);
                        return;
                    } else {
                        SearchActivity.this.onDeviceNotSupported(bluetoothDevice);
                        return;
                    }
                case 1:
                    SearchActivity.this.onError(bluetoothDevice, intent.getStringExtra(BleMulticonnectProfileService.EXTRA_ERROR_MESSAGE), intent.getIntExtra(BleMulticonnectProfileService.EXTRA_ERROR_CODE, 0));
                    return;
                case 2:
                    byte[] byteArrayExtra = intent.getByteArrayExtra(BleMulticonnectProfileService.EXTRA_LOCKSTATE);
                    if (byteArrayExtra != null) {
                        SearchActivity.this.onLockStateValueReceived(bluetoothDevice, byteArrayExtra);
                        return;
                    }
                    return;
                case 3:
                    intent.getIntExtra(BleMulticonnectProfileService.EXTRA_BOND_STATE, 10);
                    return;
                case 4:
                    int intExtra = intent.getIntExtra(BleMulticonnectProfileService.EXTRA_CONNECTION_STATE, 0);
                    if (intExtra == -1) {
                        SearchActivity.this.onLinkLossOccur(bluetoothDevice);
                        return;
                    } else if (intExtra == 0) {
                        SearchActivity.this.onDeviceDisconnected(bluetoothDevice);
                        return;
                    } else if (intExtra == 1) {
                        SearchActivity.this.onDeviceConnected(bluetoothDevice);
                        return;
                    } else if (intExtra == 2) {
                        SearchActivity.this.onDeviceConnecting(bluetoothDevice);
                        return;
                    } else if (intExtra != 3) {
                        return;
                    } else {
                        SearchActivity.this.onDeviceDisconnecting(bluetoothDevice);
                        return;
                    }
                case 5:
                    if (intent.getByteArrayExtra(BleMulticonnectProfileService.EXTRA_LOCKNAME) != null) {
                        SearchActivity.this.onLockNameValueReceived(bluetoothDevice);
                        return;
                    }
                    return;
                case 6:
                    SearchActivity.this.onDeviceReady(bluetoothDevice);
                    return;
                case 7:
                    byte[] byteArrayExtra2 = intent.getByteArrayExtra(BleMulticonnectProfileService.EXTRA_LOCKSTATUS);
                    if (byteArrayExtra2 != null) {
                        SearchActivity.this.onLockstatusValueReceived(bluetoothDevice, byteArrayExtra2);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    public final ServiceConnection mServiceConnection = new ServiceConnection() { // from class: com.secuyou.android_v22_pin_app.SearchActivity.2
        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
        }

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            BleMulticonnectProfileService.LocalBinder localBinder = (BleMulticonnectProfileService.LocalBinder) iBinder;
            SearchActivity.this.mBluetoothLeService = localBinder.getService();
            SearchActivity.this.onServiceBinded(localBinder);
        }
    };

    public void onDeviceConnected(BluetoothDevice bluetoothDevice) {
    }

    public void onDeviceConnecting(BluetoothDevice bluetoothDevice) {
    }

    public void onDeviceDisconnected(BluetoothDevice bluetoothDevice) {
    }

    public void onDeviceDisconnecting(BluetoothDevice bluetoothDevice) {
    }

    public void onDeviceNotSupported(BluetoothDevice bluetoothDevice) {
    }

    public void onDeviceReady(BluetoothDevice bluetoothDevice) {
    }

    public void onError(BluetoothDevice bluetoothDevice, String str, int i) {
    }

    public void onLinkLossOccur(BluetoothDevice bluetoothDevice) {
    }

    @Override // com.secuyou.android_v22_pin_app.searchFragment.OnListFragmentInteractionListener
    public void onListFragmentInteraction() {
    }

    public void onLockNameValueReceived(BluetoothDevice bluetoothDevice) {
    }

    public void onLockstatusValueReceived(BluetoothDevice bluetoothDevice, byte[] bArr) {
    }

    public void onServicesDiscovered(BluetoothDevice bluetoothDevice, boolean z) {
    }

    private static IntentFilter makeIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleMulticonnectProfileService.BROADCAST_CONNECTION_STATE);
        intentFilter.addAction(BleMulticonnectProfileService.BROADCAST_SERVICES_DISCOVERED);
        intentFilter.addAction(BleMulticonnectProfileService.BROADCAST_DEVICE_READY);
        intentFilter.addAction(BleMulticonnectProfileService.BROADCAST_BOND_STATE);
        intentFilter.addAction(BleMulticonnectProfileService.BROADCAST_BATTERY_LEVEL);
        intentFilter.addAction(BleMulticonnectProfileService.BROADCAST_LOCKSTATUS);
        intentFilter.addAction(BleMulticonnectProfileService.BROADCAST_LOCKSTATE);
        intentFilter.addAction(BleMulticonnectProfileService.BROADCAST_LOCKVERSION);
        intentFilter.addAction(BleMulticonnectProfileService.BROADCAST_LOCKNAME);
        intentFilter.addAction(BleMulticonnectProfileService.BROADCAST_ERROR);
        return intentFilter;
    }

    public final boolean shouldEnableBatteryLevelNotifications(BluetoothDevice bluetoothDevice) {
        throw new UnsupportedOperationException("This method should not be called");
    }

    public final boolean shouldEnableLockstatusNotifications(BluetoothDevice bluetoothDevice) {
        throw new UnsupportedOperationException("This method should not be called");
    }

    public void onLockStateValueReceived(BluetoothDevice bluetoothDevice, byte[] bArr) {
        if (this.mBluetoothLeService.getBinder().deviceInBleManagerDevice(bluetoothDevice)) {
            return;
        }
        this.PinFragment.showPincode();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_search);
        this.SearchFragment = new searchFragment();
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        this.fragmentManager = supportFragmentManager;
        supportFragmentManager.beginTransaction().add(R.id.content_frame, this.SearchFragment).commit();
        this.mBluetoothAdapter = ((BluetoothManager) getSystemService("bluetooth")).getAdapter();
        if (!getPackageManager().hasSystemFeature("android.hardware.bluetooth_le")) {
            Toast.makeText(this, "Bluetooth not supported ", 0).show();
            finish();
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(this.mCommonBroadcastReceiver, makeIntentFilter());
        getSupportActionBar().hide();
    }

    protected void onServiceBinded(BleMulticonnectProfileService.LocalBinder localBinder) {
        SearchAdapter searchAdapter = new SearchAdapter(localBinder, this);
        this.mAdapter = searchAdapter;
        this.SearchFragment.setAdapter(searchAdapter);
    }

    protected void onServiceUnbinded() {
        RecyclerView recyclerView = this.SearchFragment.mDevicesView;
        this.SearchFragment.mSearchAdapter = null;
        recyclerView.setAdapter(null);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, android.app.Activity
    public void onStart() {
        super.onStart();
        this.SearchFragment.scanLeDevice(true, this.mBluetoothAdapter);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    public void onResume() {
        super.onResume();
        bindService(new Intent(this, BleMulticonnectProfileService.class), this.mServiceConnection, 1);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, android.app.Activity
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(this.mCommonBroadcastReceiver);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    public void onPause() {
        super.onPause();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, android.app.Activity
    public void onStop() {
        super.onStop();
        unbindService(this.mServiceConnection);
        onServiceUnbinded();
    }

    @Override // com.secuyou.android_v22_pin_app.searchFragment.OnListFragmentInteractionListener
    public void cancel() {
        onBackPressed();
    }

    @Override // com.secuyou.android_v22_pin_app.searchFragment.OnListFragmentInteractionListener
    public void newLock(BleLock bleLock) {
        this.mBluetoothLeService.manager_new_device.mLock.hw_version = bleLock.hw_version;
        this.mBluetoothLeService.manager_new_device.mLock.fw_version = bleLock.fw_version;
        this.mBluetoothLeService.manager_new_device.mLock.name = bleLock.name;
        this.mBluetoothLeService.manager_new_device.connect(bleLock.getmDevice());
        RecyclerView recyclerView = this.SearchFragment.mDevicesView;
        this.SearchFragment.mSearchAdapter = null;
        recyclerView.setAdapter(null);
        this.PinFragment = new selectPin();
        this.fragmentManager.beginTransaction().replace(R.id.content_frame, this.PinFragment).commit();
    }

    @Override // com.secuyou.android_v22_pin_app.selectPin.OnFragmentInteractionListener
    public void onPinSelected() {
        this.mBluetoothLeService.manager_new_device.mLock.pincode0 = this.PinFragment.pincode0;
        this.mBluetoothLeService.manager_new_device.mLock.pincode1 = this.PinFragment.pincode1;
        this.mBluetoothLeService.manager_new_device.mLock.pincode2 = this.PinFragment.pincode2;
        this.mBluetoothLeService.manager_new_device.mLock.pincode3 = this.PinFragment.pincode3;
        this.mBluetoothLeService.manager_new_device.mLock.pincode4 = this.PinFragment.pincode4;
        this.mBluetoothLeService.manager_new_device.mLock.setPincode(PIN_fromBytetoString(this.mBluetoothLeService.manager_new_device.mLock));
        this.mBluetoothLeService.manager_new_device.sendPincodeToLock();
        BleMulticonnectProfileService bleMulticonnectProfileService = this.mBluetoothLeService;
        bleMulticonnectProfileService.add_new_device(bleMulticonnectProfileService.manager_new_device.mBluetoothDevice);
        onBackPressed();
    }

    @Override // com.secuyou.android_v22_pin_app.selectPin.OnFragmentInteractionListener
    public void onNameSelected(String str) {
        this.mBluetoothLeService.manager_new_device.sendNameToLock(str);
        this.PinFragment.showPincode();
    }

    public String PIN_fromBytetoString(BleLock bleLock) {
        return String.valueOf((int) bleLock.pincode0) + String.valueOf((int) bleLock.pincode1) + String.valueOf((int) bleLock.pincode2) + String.valueOf((int) bleLock.pincode3) + String.valueOf((int) bleLock.pincode4);
    }
}
