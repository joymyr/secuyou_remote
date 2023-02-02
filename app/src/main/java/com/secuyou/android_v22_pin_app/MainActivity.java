package com.secuyou.android_v22_pin_app;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.secuyou.android_v22_pin_app.BleMulticonnectProfileService.LocalBinder;
//import com.secuyou.android_v22_pin_app.dfu.DfuActivity;

import java.util.HashMap;

import no.joymyr.secuyou_remote.BuildConfig;
import no.joymyr.secuyou_remote.R;

/* loaded from: classes.dex */
public class MainActivity<E extends LocalBinder> extends AppCompatActivity implements devicesFragment.OnFragmentInteractionListener, devicesSpecFragment.OnFragmentInteractionListener, fragment_lockSpec.OnFragmentInteractionListener, first_time.OnFragmentInteractionListener, AboutFragment.OnFragmentInteractionListener {
    private static final String CHANNEL_ID = "SECUYOU";
    private static final int PERMISSION_REQUEST_BACKGROUND_LOCATION = 2;
    public static final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 1;
    public static final int REQUEST_ENABLE_BT = 1;
    public static BleMulticonnectProfileService mBluetoothLeService;
    devicesFragment HomeFragment;
    devicesSpecFragment LocksFragment;
    NotificationCompat.Builder builder;
    FragmentManager fragmentManager;
    DeviceAdapter mAdapter;
    DeviceSpecAdapter mAdapterSpec;
    public BluetoothDevice selectedDevice;
    FrameLayout spec_purpose_frame;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    int notificationId = 0;
    boolean mBound = false;
    private final BroadcastReceiver mCommonBroadcastReceiver = new BroadcastReceiver() { // from class: com.secuyou.android_v22_pin_app.MainActivity.1
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
                case 245163946:
                    if (action.equals(BleMulticonnectProfileService.BROADCAST_DEVICE_READY)) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case 1229384843:
                    if (action.equals(BleMulticonnectProfileService.BROADCAST_LOCKSTATUS)) {
                        c = 6;
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
                        MainActivity.this.onServicesDiscovered(bluetoothDevice, booleanExtra2);
                        return;
                    } else {
                        MainActivity.this.onDeviceNotSupported(bluetoothDevice);
                        return;
                    }
                case 1:
                    MainActivity.this.onError(bluetoothDevice, intent.getStringExtra(BleMulticonnectProfileService.EXTRA_ERROR_MESSAGE), intent.getIntExtra(BleMulticonnectProfileService.EXTRA_ERROR_CODE, 0));
                    return;
                case 2:
                    byte[] byteArrayExtra = intent.getByteArrayExtra(BleMulticonnectProfileService.EXTRA_LOCKVERSION);
                    if (byteArrayExtra != null) {
                        MainActivity.this.onLockStateValueReceived(bluetoothDevice, byteArrayExtra);
                        return;
                    }
                    return;
                case 3:
                    intent.getIntExtra(BleMulticonnectProfileService.EXTRA_BOND_STATE, 10);
                    return;
                case 4:
                    int intExtra = intent.getIntExtra(BleMulticonnectProfileService.EXTRA_CONNECTION_STATE, 0);
                    if (intExtra == -1) {
                        MainActivity.this.onLinkLossOccur(bluetoothDevice);
                        return;
                    } else if (intExtra == 0) {
                        MainActivity.this.onDeviceDisconnected(bluetoothDevice);
                        return;
                    } else if (intExtra == 1) {
                        MainActivity.this.onDeviceConnected(bluetoothDevice);
                        return;
                    } else if (intExtra == 2) {
                        MainActivity.this.onDeviceConnecting(bluetoothDevice);
                        return;
                    } else if (intExtra != 3) {
                        return;
                    } else {
                        MainActivity.this.onDeviceDisconnecting(bluetoothDevice);
                        return;
                    }
                case 5:
                    MainActivity.this.onDeviceReady(bluetoothDevice);
                    return;
                case 6:
                    byte[] byteArrayExtra2 = intent.getByteArrayExtra(BleMulticonnectProfileService.EXTRA_LOCKSTATUS);
                    if (byteArrayExtra2 != null) {
                        MainActivity.this.onLockstatusValueReceived(bluetoothDevice, byteArrayExtra2);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    public final ServiceConnection mServiceConnection = new ServiceConnection() { // from class: com.secuyou.android_v22_pin_app.MainActivity.2
        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            LocalBinder localBinder = (LocalBinder) iBinder;
            MainActivity.mBluetoothLeService = localBinder.getService();
            MainActivity.this.onServiceBinded(localBinder);
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
            MainActivity.this.onServiceUnbind();
        }
    };

    public void onDeviceDisconnecting(BluetoothDevice bluetoothDevice) {
    }

    public void onDeviceReady(BluetoothDevice bluetoothDevice) {
    }

    public void onError(BluetoothDevice bluetoothDevice, String str, int i) {
    }

    @Override
    // com.secuyou.android_v22_pin_app.devicesFragment.OnFragmentInteractionListener, com.secuyou.android_v22_pin_app.devicesSpecFragment.OnFragmentInteractionListener, com.secuyou.android_v22_pin_app.fragment_lockSpec.OnFragmentInteractionListener, com.secuyou.android_v22_pin_app.first_time.OnFragmentInteractionListener, com.secuyou.android_v22_pin_app.AboutFragment.OnFragmentInteractionListener
    public void onFragmentInteraction(Uri uri) {
    }

    public void onLockStateValueReceived(BluetoothDevice bluetoothDevice, byte[] bArr) {
    }

    protected void onServiceUnbind() {
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
        intentFilter.addAction(BleMulticonnectProfileService.BROADCAST_LOCKVERSION);
        intentFilter.addAction(BleMulticonnectProfileService.BROADCAST_LOCKNAME);
        intentFilter.addAction(BleMulticonnectProfileService.BROADCAST_ERROR);
        return intentFilter;
    }

    public void onDeviceConnected(BluetoothDevice bluetoothDevice) {
        DeviceAdapter deviceAdapter = this.mAdapter;
        if (deviceAdapter != null) {
            deviceAdapter.onDeviceStateChanged(bluetoothDevice);
        }
        DeviceSpecAdapter deviceSpecAdapter = this.mAdapterSpec;
        if (deviceSpecAdapter != null) {
            deviceSpecAdapter.onDeviceStateChanged(bluetoothDevice);
        }
    }

    public void onDeviceDisconnected(BluetoothDevice bluetoothDevice) {
        DeviceAdapter deviceAdapter = this.mAdapter;
        if (deviceAdapter != null) {
            deviceAdapter.onDeviceStateChanged(bluetoothDevice);
        }
        DeviceSpecAdapter deviceSpecAdapter = this.mAdapterSpec;
        if (deviceSpecAdapter != null) {
            deviceSpecAdapter.onDeviceStateChanged(bluetoothDevice);
        }
    }

    public void onDeviceConnecting(BluetoothDevice bluetoothDevice) {
        DeviceAdapter deviceAdapter = this.mAdapter;
        if (deviceAdapter != null) {
            deviceAdapter.onDeviceStateChanged(bluetoothDevice);
        }
        DeviceSpecAdapter deviceSpecAdapter = this.mAdapterSpec;
        if (deviceSpecAdapter != null) {
            deviceSpecAdapter.onDeviceStateChanged(bluetoothDevice);
        }
    }

    public void onLinkLossOccur(BluetoothDevice bluetoothDevice) {
        DeviceAdapter deviceAdapter = this.mAdapter;
        if (deviceAdapter != null) {
            deviceAdapter.onDeviceStateChanged(bluetoothDevice);
        }
        DeviceSpecAdapter deviceSpecAdapter = this.mAdapterSpec;
        if (deviceSpecAdapter != null) {
            deviceSpecAdapter.onDeviceStateChanged(bluetoothDevice);
        }
    }

    public void onDeviceNotSupported(BluetoothDevice bluetoothDevice) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Wrong pincode");
        builder.setMessage("The pincode entered is wrong - please delete lock from list and try again");
        builder.setPositiveButton(android.R.string.ok, (DialogInterface.OnClickListener) null);
        builder.show();
    }

    public final boolean shouldEnableBatteryLevelNotifications(BluetoothDevice bluetoothDevice) {
        throw new UnsupportedOperationException("This method should not be called");
    }

    public final boolean shouldEnableLockstatusNotifications(BluetoothDevice bluetoothDevice) {
        throw new UnsupportedOperationException("This method should not be called");
    }

    public void onLockstatusValueReceived(BluetoothDevice bluetoothDevice, byte[] bArr) {
        DeviceAdapter deviceAdapter = this.mAdapter;
        if (deviceAdapter != null) {
            deviceAdapter.onLockstatusValueReceived(bluetoothDevice);
        }
        DeviceSpecAdapter deviceSpecAdapter = this.mAdapterSpec;
        if (deviceSpecAdapter != null) {
            deviceSpecAdapter.onLockstatusValueReceived(bluetoothDevice);
        }
        if (mBluetoothLeService == null || mBluetoothLeService.mBleManagers.isEmpty() || mBluetoothLeService.getBleManager(bluetoothDevice) == null || mBluetoothLeService.getBleManager(bluetoothDevice).getBatteryValue() == BleLockState.BATTERY_STATUS.BATTERY_GOOD || mBluetoothLeService.getBleManager(bluetoothDevice).mLock.getBleLockState().mState != BleLockState.STATE_DEVICE.KEY_CONFIRMATION) {
            return;
        }
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);

        //if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            this.builder = new NotificationCompat.Builder(this, CHANNEL_ID).setSmallIcon(R.drawable.ic_notifications_black_24dp).setContentTitle("Secuyou Smart Lock").setContentText("Battery Level for " + bluetoothDevice.getName() + "is below good").setPriority(0).setContentIntent(PendingIntent.getActivity(this, 0, intent, 0)).setAutoCancel(true);
            NotificationManagerCompat.from(this).notify(this.notificationId, this.builder.build());
        //}
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView((int) R.layout.activity_main);
        FrameLayout frameLayout = findViewById(R.id.specPurpose);
        this.spec_purpose_frame = frameLayout;
        frameLayout.setVisibility(View.GONE);
        this.tabLayout = (TabLayout) findViewById(R.id.tablayout_id);
        this.viewPager = (ViewPager) findViewById(R.id.viewpager_id);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        this.viewPagerAdapter = viewPagerAdapter;
        viewPagerAdapter.AddFragment(new devicesFragment(), "HOME");
        this.HomeFragment = (devicesFragment) this.viewPagerAdapter.getItem(0);
        this.viewPagerAdapter.AddFragment(new devicesSpecFragment(), "LOCKS");
        this.LocksFragment = (devicesSpecFragment) this.viewPagerAdapter.getItem(1);
        this.viewPager.setAdapter(this.viewPagerAdapter);
        this.tabLayout.setupWithViewPager(this.viewPager);
        this.fragmentManager = getSupportFragmentManager();
        if (!getPackageManager().hasSystemFeature("android.hardware.bluetooth_le")) {
            Toast.makeText(this, "Bluetooth not supported ", Toast.LENGTH_SHORT).show();
            finish();
        }
        checkIfLocationIsSetTrue();
        createNotificationChannel();
        LocalBroadcastManager.getInstance(this).registerReceiver(this.mCommonBroadcastReceiver, makeIntentFilter());
        checkFirstRun();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    public void onResume() {
        super.onResume();
        if (!getSupportActionBar().isShowing()) {
            getSupportActionBar().show();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.e("MainActivity", "Missing required permissions");
            return;
        }
        Intent intent = new Intent(this, BleMulticonnectProfileService.class);
        startService(intent);
        bindService(intent, this.mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, android.app.Activity
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(this.mCommonBroadcastReceiver);
        unbindService(this.mServiceConnection);
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
    }

    @Override // androidx.activity.ComponentActivity, android.app.Activity
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override // android.app.Activity
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bottom_nav_menu, menu);
        menu.findItem(R.id.navigation_welcome).setVisible(true);
        return true;
    }

    @Override // android.app.Activity
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.add_lock /* 2131296325 */:
                if (!mBluetoothLeService.mBluetoothAdapter.isEnabled()) {
                    Toast.makeText(this, "Bluetooth is OFF - Please turn Bluetooth ON before you can add a lock!", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (checkSelfPermission("android.permission.ACCESS_FINE_LOCATION") != PackageManager.PERMISSION_GRANTED) {
                    checkIfLocationIsSetTrue();
                    return true;
                } else {
                    startActivity(new Intent(this, SearchActivity.class));
                    getSupportActionBar().hide();
                    return true;
                }
            case R.id.navigation_about /* 2131296454 */:
                getSupportActionBar().hide();
                this.spec_purpose_frame.setVisibility(View.VISIBLE);
                this.fragmentManager.beginTransaction().replace(R.id.specPurpose, AboutFragment.newInstance("about")).commit();
                return true;
            case R.id.navigation_help /* 2131296456 */:
                getSupportActionBar().hide();
                this.spec_purpose_frame.setVisibility(View.VISIBLE);
                this.fragmentManager.beginTransaction().replace(R.id.specPurpose, AboutFragment.newInstance("help")).commit();
                return true;
            case R.id.navigation_welcome /* 2131296457 */:
                getSupportActionBar().hide();
                this.spec_purpose_frame.setVisibility(View.VISIBLE);
                this.fragmentManager.beginTransaction().replace(R.id.specPurpose, new first_time()).commit();
                return true;
            default:
                return true;
        }
    }

    protected void onServiceBinded(LocalBinder localBinder) {
        this.mAdapter = new DeviceAdapter(localBinder, this);
        this.mAdapterSpec = new DeviceSpecAdapter(localBinder, this);
        this.HomeFragment.setAdapter(this.mAdapter);
        this.LocksFragment.setAdapter(this.mAdapterSpec);
    }

    private void checkFirstRun() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefsFile", 0);
        int i = sharedPreferences.getInt("version_code", -1);
        if (35 == i) {
            return;
        }
        if (i == -1) {
            this.spec_purpose_frame.setVisibility(View.VISIBLE);
            this.fragmentManager.beginTransaction().replace(R.id.specPurpose, new first_time()).commit();
        } else if (35 > i) {
            return;
        }
        sharedPreferences.edit().putInt("version_code", 35).apply();
    }

    @Override // com.secuyou.android_v22_pin_app.devicesFragment.OnFragmentInteractionListener, com.secuyou.android_v22_pin_app.devicesSpecFragment.OnFragmentInteractionListener
    public void lock_unlock(BluetoothDevice bluetoothDevice) {
        byte[] bArr = {1};
        Context applicationContext = getApplicationContext();
        if (mBluetoothLeService.isConnected(bluetoothDevice)) {
            mBluetoothLeService.getBleManager(bluetoothDevice).writeLockCmd(bArr);
        } else {
            mBluetoothLeService.getBleManager(bluetoothDevice).connect(bluetoothDevice);
            Toast.makeText(applicationContext, "You have pushed reconnect button - please wait for the lock to reconnect", Toast.LENGTH_SHORT).show();
        }
        getWindow().getDecorView().performHapticFeedback(1, 2);
    }

    @Override // com.secuyou.android_v22_pin_app.devicesSpecFragment.OnFragmentInteractionListener
    public void admButtonClicked(BluetoothDevice bluetoothDevice) {
        this.selectedDevice = bluetoothDevice;
        getSupportActionBar().hide();
        String charSequence = mBluetoothLeService.getBleManager(bluetoothDevice).getLockNameValue().toString();
        Boolean valueOf = Boolean.valueOf(mBluetoothLeService.isConnected(bluetoothDevice));
        int value = mBluetoothLeService.getBleManager(bluetoothDevice).getBatteryValue().getValue();
        String pincode = mBluetoothLeService.getBleManager(bluetoothDevice).mLock.getPincode();
        Boolean valueOf2 = Boolean.valueOf(mBluetoothLeService.getBleManager(bluetoothDevice).mLock.getBleLockState().HOME_LOCK);
        String str = mBluetoothLeService.getBleManager(bluetoothDevice).mLock.fw_version;
        String str2 = mBluetoothLeService.getBleManager(bluetoothDevice).mLock.hw_version;
        Boolean valueOf3 = Boolean.valueOf(mBluetoothLeService.getBleManager(bluetoothDevice).mLock.getBleLockState().RESCUE_HPR_STATE);
        this.spec_purpose_frame.setVisibility(View.VISIBLE);
        this.fragmentManager.beginTransaction().replace(R.id.specPurpose, fragment_lockSpec.newInstance(charSequence, valueOf, value, pincode, valueOf2, str, str2, valueOf3)).commit();
    }

    private void checkIfLocationIsSetTrue() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Please grant background location access");
            builder.setMessage("This app does not use your location, but it is required by Google to use Bluetooth. The Secuyou Smart Lock app will only work if 'Allowed all the time' permission is granted. If necessary, please go to Settings -> Applications -> Permissions and grant location to 'Allowed all the time'.");
            builder.setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    MainActivity.this.requestPermissions(new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 2);
                } else if (Build.VERSION.SDK_INT >= 29) {
                    MainActivity.this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 2);
                } else {
                    MainActivity.this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
                }
            });
            builder.setOnDismissListener(dialogInterface -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    MainActivity.this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN}, 2);
                } else if (Build.VERSION.SDK_INT >= 29) {
                    MainActivity.this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 2);
                } else {
                    MainActivity.this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
                }
            });
            builder.show();
        }
    }

    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity, androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        if (i == 1) {
            HashMap hashMap = new HashMap();
            hashMap.put(Manifest.permission.ACCESS_FINE_LOCATION, 0);
            for (int i2 = 0; i2 < strArr.length; i2++) {
                hashMap.put(strArr[i2], Integer.valueOf(iArr[i2]));
            }
            if ((Integer) hashMap.get(Manifest.permission.ACCESS_FINE_LOCATION) == 0) {
                Toast.makeText(this, "All Permission GRANTED !! Thank You :)", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, "One or More Permissions are DENIED", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        super.onRequestPermissionsResult(i, strArr, iArr);
    }

    @Override // com.secuyou.android_v22_pin_app.first_time.OnFragmentInteractionListener
    public void addlock() {
        startActivity(new Intent(this, SearchActivity.class));
        getSupportActionBar().hide();
        this.spec_purpose_frame.setVisibility(View.GONE);
    }

    @Override // com.secuyou.android_v22_pin_app.fragment_lockSpec.OnFragmentInteractionListener, com.secuyou.android_v22_pin_app.first_time.OnFragmentInteractionListener, com.secuyou.android_v22_pin_app.AboutFragment.OnFragmentInteractionListener
    public void home() {
        this.spec_purpose_frame.setVisibility(View.GONE);
        getSupportActionBar().show();
    }

    @Override // com.secuyou.android_v22_pin_app.fragment_lockSpec.OnFragmentInteractionListener
    public void set_home_lock() {
        if (this.selectedDevice != null) {
            Context applicationContext = getApplicationContext();
            mBluetoothLeService.getBleManager(this.selectedDevice).writeLockCmd(new byte[]{-1});
            Toast.makeText(applicationContext, mBluetoothLeService.getBleManager(this.selectedDevice).mLock.getBleLockState().HOME_LOCK ? "Home Lock turned OFF!" : "Home Lock turned ON!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override // com.secuyou.android_v22_pin_app.fragment_lockSpec.OnFragmentInteractionListener
    public void set_hpr_sensor_lock() {
        if (this.selectedDevice != null) {
            Context applicationContext = getApplicationContext();
            mBluetoothLeService.getBleManager(this.selectedDevice).writeLockCmd(new byte[]{-16});
            Toast.makeText(applicationContext, mBluetoothLeService.getBleManager(this.selectedDevice).mLock.getBleLockState().HOME_LOCK ? "Handle Sensor Lock turned OFF!" : "Handle Sensor Lock turned ON!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override // com.secuyou.android_v22_pin_app.fragment_lockSpec.OnFragmentInteractionListener
    public void deleteLock() {
        if (this.selectedDevice != null) {
            mBluetoothLeService.getBinder().getBleManager(this.selectedDevice).disconnect();
            mBluetoothLeService.getBinder().disconnect(this.selectedDevice);
            mBluetoothLeService.getBinder().removeBleManager(this.selectedDevice);
            mBluetoothLeService.deleteLockfromSharedPref(this.selectedDevice);
            this.HomeFragment.mAdapter.onDeviceRemoved(this.selectedDevice);
            this.LocksFragment.mAdapter.onDeviceRemoved(this.selectedDevice);
            this.selectedDevice = null;
            this.spec_purpose_frame.setVisibility(View.GONE);
            getSupportActionBar().show();
        }
    }

    @Override // com.secuyou.android_v22_pin_app.fragment_lockSpec.OnFragmentInteractionListener
    public void renameLock(String str) {
        BluetoothDevice bluetoothDevice = this.selectedDevice;
        if (bluetoothDevice != null) {
            mBluetoothLeService.getBleManager(bluetoothDevice).mLock.setNameStringValue(str);
            mBluetoothLeService.getBleManager(this.selectedDevice).mLockName = str;
            mBluetoothLeService.getBleManager(this.selectedDevice).sendNameToLock(str);
            mBluetoothLeService.save_name(this.selectedDevice, str);
            this.HomeFragment.mAdapter.onDeviceStateChanged(this.selectedDevice);
            this.LocksFragment.mAdapter.onDeviceStateChanged(this.selectedDevice);
            this.spec_purpose_frame.setVisibility(View.GONE);
            getSupportActionBar().show();
        }
    }

    @Override // com.secuyou.android_v22_pin_app.fragment_lockSpec.OnFragmentInteractionListener
    public void serviceCheck() {
        BluetoothDevice bluetoothDevice = this.selectedDevice;
        if (bluetoothDevice != null) {
            BleLockState.LOCKING_MECHANISM_POSITION locking_mechanism_position = mBluetoothLeService.getBleManager(bluetoothDevice).mLock.getBleLockState().mLockPosition;
            lock_unlock(this.selectedDevice);
            try {
                Thread.sleep(1000L);
            } catch (Exception unused) {
            }
            onEmailSend(locking_mechanism_position, mBluetoothLeService.getBleManager(this.selectedDevice).mLock.getBleLockState().mLockPosition);
        }
    }

    public void onEmailSend(BleLockState.LOCKING_MECHANISM_POSITION locking_mechanism_position, BleLockState.LOCKING_MECHANISM_POSITION locking_mechanism_position2) {
        Intent intent = new Intent("android.intent.action.SEND");
        intent.setType("plain/text");
        intent.putExtra("android.intent.extra.EMAIL", new String[]{"info@secuyou.dk"});
        intent.putExtra("android.intent.extra.SUBJECT", "Secuyou Smart Lock Test result");
        intent.putExtra("android.intent.extra.TEXT", "Firmware version: " + mBluetoothLeService.getBleManager(this.selectedDevice).mLock.fw_version + "\n Lock hardware version: " + mBluetoothLeService.getBleManager(this.selectedDevice).mLock.hw_version + "\nLock State before:  " + locking_mechanism_position + "\nLock State after:  " + locking_mechanism_position2 + "\nBattery level:  " + mBluetoothLeService.getBleManager(this.selectedDevice).mLock.getBleLockState().mBatteryStatus + "\nPincode correct:  " + mBluetoothLeService.getBleManager(this.selectedDevice).mLock.getBleLockState().PIN_CODE_CORRECT + "\nState of lock:  " + mBluetoothLeService.getBleManager(this.selectedDevice).mLock.getBleLockState().mState + "\nAndroid version:  " + Build.VERSION.SDK_INT + "\nApp version: " + BuildConfig.VERSION_NAME + "\nPhone variant:  " + Build.MANUFACTURER + Build.MODEL + "\nSerial No of lock:  " + mBluetoothLeService.getBleManager(this.selectedDevice).mLock.serial_no_batch);
        startActivity(Intent.createChooser(intent, "Send mail..."));
    }

    private void createNotificationChannel() {
        String string = getString(R.string.channel_name);
        String string2 = getString(R.string.channel_description);
        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, string, NotificationManager.IMPORTANCE_DEFAULT);
        notificationChannel.setDescription(string2);
        ((NotificationManager) getSystemService(NotificationManager.class)).createNotificationChannel(notificationChannel);
    }
}
