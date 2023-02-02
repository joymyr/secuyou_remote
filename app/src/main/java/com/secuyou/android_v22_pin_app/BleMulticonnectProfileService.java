package com.secuyou.android_v22_pin_app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.joymyr.secuyou_remote.RemoteClient;

/* loaded from: classes.dex */
@SuppressLint("MissingPermission")
public final class BleMulticonnectProfileService extends Service implements BleManagerCallbacks {
    public static final String BROADCAST_BATTERY_LEVEL = "com.secuyou.android_v22_pin_app.BROADCAST_BATTERY_LEVEL";
    public static final String BROADCAST_BOND_STATE = "com.secuyou.android_v22_pin_app.BROADCAST_BOND_STATE";
    public static final String BROADCAST_CONNECTION_STATE = "BROADCAST_CONNECTION_STATE";
    public static final String BROADCAST_DEVICE_READY = "com.secuyou.android_v22_pin_app.DEVICE_READY";
    public static final String BROADCAST_ERROR = "com.secuyou.android_v22_pin_app.BROADCAST_ERROR";
    public static final String BROADCAST_LOCKFIRMWARE = "com.secuyou.android_v22_pin_app.BROADCAST_LOCKFIRMWARE";
    public static final String BROADCAST_LOCKNAME = "com.secuyou.android_v22_pin_app..BROADCAST_LOCKNAME";
    public static final String BROADCAST_LOCKSERIAL = "com.secuyou.android_v22_pin_app.BROADCAST_LOCKSERIAL";
    public static final String BROADCAST_LOCKSTATE = "com.secuyou.android_v22_pin_app.BROADCAST_LOCKSTATE";
    public static final String BROADCAST_LOCKSTATUS = "com.secuyou.android_v22_pin_app.BROADCAST_LOCKSTATUS";
    public static final String BROADCAST_LOCKVERSION = "com.secuyou.android_v22_pin_app.BROADCAST_LOCKVERSION";
    public static final String BROADCAST_SERVICES_DISCOVERED = "com.secuyou.android_v22_pin_app.BROADCAST_SERVICES_DISCOVERED";
    public static final String EXTRA_BOND_STATE = "com.secuyou.android_v22_pin_app.EXTRA_BOND_STATE";
    public static final String EXTRA_CONNECTION_STATE = "com.secuyou.android_v22_pin_app.EXTRA_CONNECTION_STATE";
    public static final String EXTRA_DEVICE = "com.secuyou.android_v22_pin_app.EXTRA_DEVICE";
    public static final String EXTRA_ERROR_CODE = "com.secuyou.android_v22_pin_app.EXTRA_ERROR_CODE";
    public static final String EXTRA_ERROR_MESSAGE = "com.secuyou.android_v22_pin_app.EXTRA_ERROR_MESSAGE";
    public static final String EXTRA_LOCKFIRMWARE = "com.example.bluetooth.le.EXTRA_LOCKFIRMWARE";
    public static final String EXTRA_LOCKNAME = "com.secuyou.android_v22_pin_app.EXTRA_LOCKNAME";
    public static final String EXTRA_LOCKSERIAL = "com.example.bluetooth.le.EXTRA_LOCKSERIAL";
    public static final String EXTRA_LOCKSTATE = "com.example.bluetooth.le.EXTRA_LOCKSTATE";
    public static final String EXTRA_LOCKSTATUS = "com.example.bluetooth.le.EXTRA_LOCKSTATUS";
    public static final String EXTRA_LOCKVERSION = "com.example.bluetooth.le.EXTRA_LOCKVERSION";
    public static final String EXTRA_SERVICE_PRIMARY = "com.secuyou.android_v22_pin_app.EXTRA_SERVICE_PRIMARY";
    public static final String EXTRA_SERVICE_SECONDARY = "com.secuyou.android_v22_pin_app.EXTRA_SERVICE_SECONDARY";
    private static final String LOCK_LIST_PREF_FWVERSION = "SECUYOU_LOCKS_FWVERSION";
    private static final String LOCK_LIST_PREF_LOCKS = "SECUYOU_LOCKS_LIST";
    private static final String LOCK_LIST_PREF_NAMES = "SECUYOU_LOCKS_NAMES";
    private static final long SCAN_PERIOD = 2000;
    public static final int STATE_CONNECTED = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_DISCONNECTING = 3;
    public static final int STATE_LINK_LOSS = -1;
    private static final String TAG = "BleMultiProfileService";
    private Context context;
    private boolean mActivityIsChangingConfiguration;
    protected boolean mBinded;
    public HashMap<BluetoothDevice, BleManager> mBleManagers;
    public BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private Handler mHandler;
    public SharedPreferences mLocksFWSharedPreferences;
    public SharedPreferences mLocksListSharedPreferences;
    public SharedPreferences mLocksNamesSharedPreferences;
    private List<BluetoothDevice> mManagedDevices;
    private boolean mScanning;
    public BleManager manager_new_device;
    public RemoteClient remoteClient;
    private Handler scan_handler;
    private final BroadcastReceiver mBluetoothStateBroadcastReceiver = new BroadcastReceiver() { // from class: com.secuyou.android_v22_pin_app.BleMulticonnectProfileService.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            int intExtra = intent.getIntExtra("android.bluetooth.adapter.extra.STATE", 10);
            int intExtra2 = intent.getIntExtra("android.bluetooth.adapter.extra.PREVIOUS_STATE", 10);
            if (intExtra != 10) {
                if (intExtra == 12) {
                    BleMulticonnectProfileService.this.mHandler.postDelayed(new Runnable() { // from class: com.secuyou.android_v22_pin_app.BleMulticonnectProfileService.1.1
                        @Override // java.lang.Runnable
                        public void run() {
                            BleMulticonnectProfileService.this.onBluetoothEnabled();
                        }
                    }, 600L);
                    return;
                } else if (intExtra != 13) {
                    return;
                }
            }
            if (intExtra2 == 13 || intExtra2 == 10) {
                return;
            }
            BleMulticonnectProfileService.this.onBluetoothDisabled();
        }
    };
    private final ScanCallback mScanCallback = new ScanCallback() { // from class: com.secuyou.android_v22_pin_app.BleMulticonnectProfileService.2
        BluetoothDevice device;

        @Override // android.bluetooth.le.ScanCallback
        public void onBatchScanResults(List<ScanResult> list) {
        }

        @Override // android.bluetooth.le.ScanCallback
        public void onScanFailed(int i) {
        }

        @Override // android.bluetooth.le.ScanCallback
        public void onScanResult(int i, ScanResult scanResult) {
            if (!BleMulticonnectProfileService.this.mBluetoothAdapter.isEnabled() || BleMulticonnectProfileService.this.mManagedDevices.isEmpty()) {
                return;
            }
            for (BluetoothDevice bluetoothDevice : BleMulticonnectProfileService.this.mManagedDevices) {
                BleManager bleManager = BleMulticonnectProfileService.this.mBleManagers.get(bluetoothDevice);
                if (!bleManager.isConnected()) {
                    bleManager.connect(bluetoothDevice);
                }
            }
        }
    };
    Runnable myRunnable = new Runnable() { // from class: com.secuyou.android_v22_pin_app.BleMulticonnectProfileService.3
        @Override // java.lang.Runnable
        public void run() {
            BleMulticonnectProfileService.this.mScanning = false;
            BleMulticonnectProfileService.this.mBluetoothLeScanner.stopScan(BleMulticonnectProfileService.this.mScanCallback);
        }
    };

    protected void onRebind() {
    }

    protected void onServiceCreated() {
    }

    protected void onServiceStarted() {
    }

    protected void onUnbind() {
    }

    /* loaded from: classes.dex */
    public class LocalBinder extends Binder {
        public LocalBinder() {
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public BleMulticonnectProfileService getService() {
            return BleMulticonnectProfileService.this;
        }

        public final List<BluetoothDevice> getManagedDevices() {
            return Collections.unmodifiableList(BleMulticonnectProfileService.this.mManagedDevices);
        }

        public void connect(BluetoothDevice bluetoothDevice) {
            if (BleMulticonnectProfileService.this.mManagedDevices.contains(bluetoothDevice)) {
                return;
            }
            BleMulticonnectProfileService.this.mManagedDevices.add(bluetoothDevice);
            BleManager bleManager = BleMulticonnectProfileService.this.mBleManagers.get(bluetoothDevice);
            if (bleManager != null) {
                bleManager.connect(bluetoothDevice);
                return;
            }
            BleMulticonnectProfileService.this.mBleManagers.put(bluetoothDevice, bleManager);
            bleManager.connect(bluetoothDevice);
        }

        public void disconnect(BluetoothDevice bluetoothDevice) {
            BleManager bleManager = BleMulticonnectProfileService.this.mBleManagers.get(bluetoothDevice);
            if (bleManager != null && bleManager.isConnected()) {
                bleManager.disconnect();
            }
            BleMulticonnectProfileService.this.mManagedDevices.remove(bluetoothDevice);
        }

        public void removeBleManager(BluetoothDevice bluetoothDevice) {
            BleMulticonnectProfileService.this.mBleManagers.remove(bluetoothDevice);
        }

        public final boolean isConnected(BluetoothDevice bluetoothDevice) {
            BleManager bleManager = BleMulticonnectProfileService.this.mBleManagers.get(bluetoothDevice);
            return bleManager != null && bleManager.isConnected();
        }

        public final int getConnectionState(BluetoothDevice bluetoothDevice) {
            BleManager bleManager = BleMulticonnectProfileService.this.mBleManagers.get(bluetoothDevice);
            if (bleManager != null) {
                return bleManager.getConnectionState();
            }
            return 0;
        }

        public final BleManager getBleManager(BluetoothDevice bluetoothDevice) {
            if (deviceInBleManagerDevice(bluetoothDevice)) {
                return BleMulticonnectProfileService.this.mBleManagers.get(bluetoothDevice);
            }
            return null;
        }

        public void lock_unlock(BluetoothDevice bluetoothDevice) {
            byte[] bArr = {1};
            BleMulticonnectProfileService.this.mBleManagers.get(bluetoothDevice).writeLockCmd(bArr);
        }

        public String getBatteryStringValue(BluetoothDevice bluetoothDevice) {
            return BleMulticonnectProfileService.this.mBleManagers.get(bluetoothDevice).getBatteryValue().getStringValue();
        }

        public int getBatteryValue(BluetoothDevice bluetoothDevice) {
            return BleMulticonnectProfileService.this.mBleManagers.get(bluetoothDevice).getBatteryValue().getValue();
        }

        public String getHandleStringValue(BluetoothDevice bluetoothDevice) {
            return BleMulticonnectProfileService.this.mBleManagers.get(bluetoothDevice).getHandleValue().getStringValue();
        }

        public int getHandleValue(BluetoothDevice bluetoothDevice) {
            return BleMulticonnectProfileService.this.mBleManagers.get(bluetoothDevice).getHandleValue().getValue();
        }

        public BleLockState.LOCKING_MECHANISM_POSITION getLockMechanismPosition(BluetoothDevice bluetoothDevice) {
            BleManager bleManager = BleMulticonnectProfileService.this.mBleManagers.get(bluetoothDevice);
            return bleManager != null ? bleManager.mLock.getBleLockState().mLockPosition : BleLockState.LOCKING_MECHANISM_POSITION.UNKNOWN_POSITION;
        }

        public BleLockState.STATE_DEVICE getStateLock(BluetoothDevice bluetoothDevice) {
            BleManager bleManager = BleMulticonnectProfileService.this.mBleManagers.get(bluetoothDevice);
            return bleManager != null ? bleManager.mLock.getBleLockState().mState : BleLockState.STATE_DEVICE.KEY_GENERATION;
        }

        public BleLock.APP_STATE getAppState(BluetoothDevice bluetoothDevice) {
            BleManager bleManager = BleMulticonnectProfileService.this.mBleManagers.get(bluetoothDevice);
            return bleManager != null ? bleManager.mLock.getAppState() : BleLock.APP_STATE.UNKNOWN;
        }

        public CharSequence getLockNameValue(BluetoothDevice bluetoothDevice) {
            return BleMulticonnectProfileService.this.mBleManagers.get(bluetoothDevice).getLockNameValue();
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public boolean deviceInBleManagerDevice(BluetoothDevice bluetoothDevice) {
            return BleMulticonnectProfileService.this.mBleManagers.containsKey(bluetoothDevice);
        }

        public final void setActivityIsChangingConfiguration(boolean z) {
            BleMulticonnectProfileService.this.mActivityIsChangingConfiguration = z;
        }
    }

    protected Handler getHandler() {
        return this.mHandler;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public LocalBinder getBinder() {
        return new LocalBinder();
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        this.mBinded = true;
        return getBinder();
    }

    @Override // android.app.Service
    public final void onRebind(Intent intent) {
        this.mBinded = true;
        if (this.mActivityIsChangingConfiguration) {
            return;
        }
        onRebind();
        for (BleManager bleManager : this.mBleManagers.values()) {
            if (bleManager.isConnected()) {
                bleManager.setLockstatusNotifications(true);
                bleManager.setLockStateNotifications(true);
                bleManager.readLockState();
                bleManager.readLockstatus();
                bleManager.readLockName();
            }
        }
    }

    @Override // android.app.Service
    public final boolean onUnbind(Intent intent) {
        this.mBinded = false;
        if (this.mActivityIsChangingConfiguration) {
            return true;
        }
        if (!this.mManagedDevices.isEmpty()) {
            onUnbind();
            for (BleManager bleManager : this.mBleManagers.values()) {
            }
            return true;
        }
        stopSelf();
        return true;
    }

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        this.context = this;
        this.mHandler = new Handler();
        this.mBleManagers = new HashMap<>();
        this.mManagedDevices = new ArrayList();
        this.manager_new_device = new BleManager(this);
        this.remoteClient = new RemoteClient(context, getBinder());
        getLockStoredSpecs();
        registerReceiver(this.mBluetoothStateBroadcastReceiver, new IntentFilter("android.bluetooth.adapter.action.STATE_CHANGED"));
        onServiceCreated();
    }

    public void getLockStoredSpecs() {
        this.mLocksListSharedPreferences = getSharedPreferences(LOCK_LIST_PREF_LOCKS, 0);
        new HashMap();
        Map<String, ?> all = this.mLocksListSharedPreferences.getAll();
        this.mLocksNamesSharedPreferences = getSharedPreferences(LOCK_LIST_PREF_NAMES, 0);
        new HashMap();
        Map<String, ?> all2 = this.mLocksNamesSharedPreferences.getAll();
        this.mLocksFWSharedPreferences = getSharedPreferences(LOCK_LIST_PREF_FWVERSION, 0);
        new HashMap();
        Map<String, ?> all3 = this.mLocksFWSharedPreferences.getAll();
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        for (String str : all.keySet()) {
            BleManager bleManager = new BleManager(this);
            BluetoothDevice remoteDevice = bluetoothManager.getAdapter().getRemoteDevice(str);
            this.mBleManagers.put(remoteDevice, bleManager);
            this.mManagedDevices.add(remoteDevice);
            bleManager.mLock.HasPincode = true;
            bleManager.mLock.setPincode((String) all.get(str));
            String obj = all2.get(str).toString();
            if (all3.get(str) != null) {
                bleManager.mLock.fw_version = all3.get(str).toString();
            }
            bleManager.mLock.setNameStringValue(obj);
            bleManager.mLockName = obj;
            bleManager.connect(remoteDevice);
        }
    }

    public void scanLeDevice(boolean z, BluetoothAdapter bluetoothAdapter) {
        if (this.mBluetoothLeScanner == null) {
            this.mBluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        }
        ArrayList arrayList = new ArrayList();
        ScanSettings build = new ScanSettings.Builder().setScanMode(2).setReportDelay(0L).build();
        ByteBuffer allocate = ByteBuffer.allocate(2);
        ByteBuffer allocate2 = ByteBuffer.allocate(2);
        allocate.put(0, (byte) 22);
        allocate2.put((byte) -1);
        arrayList.add(new ScanFilter.Builder().setManufacturerData(724, allocate.array(), allocate2.array()).build());
        if (this.mScanning) {
            return;
        }
        if (z) {
            this.mHandler.postDelayed(this.myRunnable, SCAN_PERIOD);
            this.mScanning = true;
            this.mBluetoothLeScanner.startScan(arrayList, build, this.mScanCallback);
            return;
        }
        this.mScanning = false;
        this.mBluetoothLeScanner.stopScan(this.mScanCallback);
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int i, int i2) {
        onServiceStarted();
        return Service.START_STICKY;
    }

    @Override // android.app.Service
    public void onTaskRemoved(Intent intent) {
        super.onTaskRemoved(intent);
        stopSelf();
    }

    @Override // android.app.Service
    public void onDestroy() {
        super.onDestroy();
        onServiceStopped();
        this.mHandler = null;
    }

    protected void onServiceStopped() {
        for (BleManager bleManager : this.mBleManagers.values()) {
            bleManager.close();
            Log.d(TAG, "Service destroyed");
        }
        this.mBleManagers.clear();
        this.mManagedDevices.clear();
        this.mBleManagers = null;
        this.mManagedDevices = null;
    }

    protected void onBluetoothDisabled() {
        showToast("Bluetooth turned OFF - App does not work when Bluetooth is OFF");
    }

    protected void onBluetoothEnabled() {
        if (this.mBluetoothAdapter != null) {
            this.mBleManagers.clear();
            this.mManagedDevices.clear();
            getLockStoredSpecs();
        }
    }

    @Override // com.secuyou.android_v22_pin_app.BleManagerCallbacks
    public boolean shouldEnableBatteryLevelNotifications(BluetoothDevice bluetoothDevice) {
        return this.mBinded;
    }

    @Override // com.secuyou.android_v22_pin_app.BleManagerCallbacks
    public boolean shouldEnableLockstatusNotifications(BluetoothDevice bluetoothDevice) {
        return this.mBinded;
    }

    @Override // com.secuyou.android_v22_pin_app.BleManagerCallbacks
    public boolean shouldEnableLockStateNotifications(BluetoothDevice bluetoothDevice) {
        return this.mBinded;
    }

    @Override // com.secuyou.android_v22_pin_app.BleManagerCallbacks
    public boolean shouldEnableLockNameNotifications(BluetoothDevice bluetoothDevice) {
        return this.mBinded;
    }

    @Override // com.secuyou.android_v22_pin_app.BleManagerCallbacks
    public void onDeviceConnecting(BluetoothDevice bluetoothDevice) {
        Intent intent = new Intent(BROADCAST_CONNECTION_STATE);
        intent.putExtra(EXTRA_DEVICE, bluetoothDevice);
        intent.putExtra(EXTRA_CONNECTION_STATE, 2);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        remoteClient.onDeviceConnecting(bluetoothDevice);
    }

    @Override // com.secuyou.android_v22_pin_app.BleManagerCallbacks
    public void onDeviceConnected(BluetoothDevice bluetoothDevice) {
        Intent intent = new Intent(BROADCAST_CONNECTION_STATE);
        intent.putExtra(EXTRA_DEVICE, bluetoothDevice);
        intent.putExtra(EXTRA_CONNECTION_STATE, 1);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        remoteClient.onDeviceConnected(bluetoothDevice);
    }

    @Override // com.secuyou.android_v22_pin_app.BleManagerCallbacks
    public void onDeviceDisconnecting(BluetoothDevice bluetoothDevice) {
        Intent intent = new Intent(BROADCAST_CONNECTION_STATE);
        intent.putExtra(EXTRA_DEVICE, bluetoothDevice);
        intent.putExtra(EXTRA_CONNECTION_STATE, 3);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        remoteClient.onDeviceDisconnecting(bluetoothDevice);
    }

    @Override // com.secuyou.android_v22_pin_app.BleManagerCallbacks
    public void onDeviceDisconnected(BluetoothDevice bluetoothDevice) {
        Intent intent = new Intent(BROADCAST_CONNECTION_STATE);
        intent.putExtra(EXTRA_DEVICE, bluetoothDevice);
        intent.putExtra(EXTRA_CONNECTION_STATE, 0);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        remoteClient.onDeviceDisconnected(bluetoothDevice);
        if (this.mBinded || !this.mManagedDevices.isEmpty()) {
            return;
        }
        stopSelf();
    }

    @Override // com.secuyou.android_v22_pin_app.BleManagerCallbacks
    public void onLinkLossOccur(BluetoothDevice bluetoothDevice) {
        Intent intent = new Intent(BROADCAST_CONNECTION_STATE);
        intent.putExtra(EXTRA_DEVICE, bluetoothDevice);
        intent.putExtra(EXTRA_CONNECTION_STATE, -1);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        remoteClient.onLinkLossOccur(bluetoothDevice);
    }

    @Override // com.secuyou.android_v22_pin_app.BleManagerCallbacks
    public void onServicesDiscovered(BluetoothDevice bluetoothDevice, boolean z) {
        Intent intent = new Intent(BROADCAST_SERVICES_DISCOVERED);
        intent.putExtra(EXTRA_DEVICE, bluetoothDevice);
        intent.putExtra(EXTRA_SERVICE_PRIMARY, true);
        intent.putExtra(EXTRA_SERVICE_SECONDARY, z);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        remoteClient.onServicesDiscovered(bluetoothDevice, z);
    }

    @Override // com.secuyou.android_v22_pin_app.BleManagerCallbacks
    public void onDeviceReady(BluetoothDevice bluetoothDevice) {
        Intent intent = new Intent(BROADCAST_DEVICE_READY);
        intent.putExtra(EXTRA_DEVICE, bluetoothDevice);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        remoteClient.onDeviceReady(bluetoothDevice);
    }

    @Override // com.secuyou.android_v22_pin_app.BleManagerCallbacks
    public void onDeviceNotSupported(BluetoothDevice bluetoothDevice) {
        this.mManagedDevices.remove(bluetoothDevice);
        this.mBleManagers.remove(bluetoothDevice);
        Intent intent = new Intent(BROADCAST_SERVICES_DISCOVERED);
        intent.putExtra(EXTRA_DEVICE, bluetoothDevice);
        intent.putExtra(EXTRA_SERVICE_PRIMARY, false);
        intent.putExtra(EXTRA_SERVICE_SECONDARY, false);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        remoteClient.onDeviceNotSupported(bluetoothDevice);
    }

    @Override // com.secuyou.android_v22_pin_app.BleManagerCallbacks
    public void onLockNameValueReceived(BluetoothDevice bluetoothDevice, byte[] bArr) {
        Intent intent = new Intent(BROADCAST_LOCKNAME);
        intent.putExtra(EXTRA_DEVICE, bluetoothDevice);
        intent.putExtra(EXTRA_LOCKNAME, bArr);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        remoteClient.onLockNameValueReceived(bluetoothDevice, bArr);
    }

    @Override // com.secuyou.android_v22_pin_app.BleManagerCallbacks
    public void onLockModelValueReceived(BluetoothDevice bluetoothDevice, byte[] bArr) {
        Intent intent = new Intent(BROADCAST_LOCKVERSION);
        intent.putExtra(EXTRA_DEVICE, bluetoothDevice);
        intent.putExtra(EXTRA_LOCKVERSION, bArr);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        remoteClient.onLockModelValueReceived(bluetoothDevice, bArr);
    }

    @Override // com.secuyou.android_v22_pin_app.BleManagerCallbacks
    public void onLockSerialValueReceived(BluetoothDevice bluetoothDevice, byte[] bArr) {
        Intent intent = new Intent(BROADCAST_LOCKSERIAL);
        intent.putExtra(EXTRA_DEVICE, bluetoothDevice);
        intent.putExtra(EXTRA_LOCKSERIAL, bArr);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        remoteClient.onLockSerialValueReceived(bluetoothDevice, bArr);
    }

    @Override // com.secuyou.android_v22_pin_app.BleManagerCallbacks
    public void onLockFirmwareValueReceived(BluetoothDevice bluetoothDevice, byte[] bArr) {
        Intent intent = new Intent(BROADCAST_LOCKFIRMWARE);
        intent.putExtra(EXTRA_DEVICE, bluetoothDevice);
        intent.putExtra(EXTRA_LOCKFIRMWARE, bArr);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        remoteClient.onLockFirmwareValueReceived(bluetoothDevice, bArr);
    }

    @Override // com.secuyou.android_v22_pin_app.BleManagerCallbacks
    public void onLockstatusValueReceived(BluetoothDevice bluetoothDevice, byte[] bArr) {
        Intent intent = new Intent(BROADCAST_LOCKSTATUS);
        intent.putExtra(EXTRA_DEVICE, bluetoothDevice);
        intent.putExtra(EXTRA_LOCKSTATUS, bArr);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        remoteClient.onLockstatusValueReceived(bluetoothDevice, bArr);
    }

    @Override // com.secuyou.android_v22_pin_app.BleManagerCallbacks
    public void onLockStateValueReceived(BluetoothDevice bluetoothDevice, byte[] bArr) {
        Intent intent = new Intent(BROADCAST_LOCKSTATE);
        intent.putExtra(EXTRA_DEVICE, bluetoothDevice);
        intent.putExtra(EXTRA_LOCKSTATE, bArr);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        remoteClient.onLockStateValueReceived(bluetoothDevice, bArr);
    }

    @Override // com.secuyou.android_v22_pin_app.BleManagerCallbacks
    public void onBondingRequired(BluetoothDevice bluetoothDevice) {
        Intent intent = new Intent(BROADCAST_BOND_STATE);
        intent.putExtra(EXTRA_DEVICE, bluetoothDevice);
        intent.putExtra(EXTRA_BOND_STATE, 11);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        remoteClient.onBondingRequired(bluetoothDevice);
    }

    @Override // com.secuyou.android_v22_pin_app.BleManagerCallbacks
    public void onBonded(BluetoothDevice bluetoothDevice) {
        Intent intent = new Intent(BROADCAST_BOND_STATE);
        intent.putExtra(EXTRA_DEVICE, bluetoothDevice);
        intent.putExtra(EXTRA_BOND_STATE, 12);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        remoteClient.onBonded(bluetoothDevice);
    }

    @Override // com.secuyou.android_v22_pin_app.BleManagerCallbacks
    public void onError(BluetoothDevice bluetoothDevice, String str, int i) {
        Intent intent = new Intent(BROADCAST_ERROR);
        intent.putExtra(EXTRA_DEVICE, bluetoothDevice);
        intent.putExtra(EXTRA_ERROR_MESSAGE, str);
        intent.putExtra(EXTRA_ERROR_CODE, i);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        remoteClient.onError(bluetoothDevice, str, i);
    }

    protected void showToast(final int i) {
        this.mHandler.post(new Runnable() { // from class: com.secuyou.android_v22_pin_app.BleMulticonnectProfileService.4
            @Override // java.lang.Runnable
            public void run() {
                Toast.makeText(BleMulticonnectProfileService.this, i, Toast.LENGTH_SHORT).show();
            }
        });
    }

    protected void showToast(final String str) {
        this.mHandler.post(new Runnable() { // from class: com.secuyou.android_v22_pin_app.BleMulticonnectProfileService.5
            @Override // java.lang.Runnable
            public void run() {
                Toast.makeText(BleMulticonnectProfileService.this, str, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public BleManager getBleManager(BluetoothDevice bluetoothDevice) {
        return this.mBleManagers.get(bluetoothDevice);
    }

    protected List<BluetoothDevice> getManagedDevices() {
        return Collections.unmodifiableList(this.mManagedDevices);
    }

    protected List<BluetoothDevice> getConnectedDevices() {
        ArrayList arrayList = new ArrayList();
        for (BluetoothDevice bluetoothDevice : this.mManagedDevices) {
            if (this.mBleManagers.get(bluetoothDevice).isConnected()) {
                arrayList.add(bluetoothDevice);
            }
        }
        return Collections.unmodifiableList(arrayList);
    }

    public void removeFromList(BluetoothDevice bluetoothDevice) {
        this.mManagedDevices.remove(bluetoothDevice);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean isConnected(BluetoothDevice bluetoothDevice) {
        BleManager bleManager = this.mBleManagers.get(bluetoothDevice);
        return bleManager != null && bleManager.isConnected();
    }

    public void add_new_device(BluetoothDevice bluetoothDevice) {
        if (bluetoothDevice == this.manager_new_device.mBluetoothDevice) {
            new BleManager(this).setGattCallbacks(this);
            BleManager bleManager = this.manager_new_device;
            this.mManagedDevices.add(bleManager.mBluetoothDevice);
            this.mBleManagers.put(bleManager.mBluetoothDevice, bleManager);
            store_newManager_with_PINcode(bleManager.mBluetoothDevice, bleManager.mLock.getPincode(), bleManager.mLock.getName(), bleManager.mLock.fw_version);
            this.manager_new_device = new BleManager(this);
        }
    }

    public String convertByte(byte[] bArr) {
        String str = no.nordicsemi.android.dfu.BuildConfig.FLAVOR;
        for (int i = 0; i < 5; i++) {
            str = str + Byte.toString(bArr[i]);
        }
        return str;
    }

    public void store_newManager_with_PINcode(BluetoothDevice bluetoothDevice, String str, String str2, String str3) {
        new HashMap();
        SharedPreferences.Editor edit = this.mLocksListSharedPreferences.edit();
        if (this.mLocksListSharedPreferences.getAll().containsKey(bluetoothDevice.getAddress())) {
            deleteLockfromSharedPref(bluetoothDevice);
        }
        edit.putString(bluetoothDevice.getAddress(), str);
        edit.apply();
        SharedPreferences.Editor edit2 = this.mLocksNamesSharedPreferences.edit();
        edit2.putString(bluetoothDevice.getAddress(), str2);
        edit2.apply();
        SharedPreferences.Editor edit3 = this.mLocksFWSharedPreferences.edit();
        edit3.putString(bluetoothDevice.getAddress(), str3);
        edit3.apply();
    }

    public void deleteLockfromSharedPref(BluetoothDevice bluetoothDevice) {
        SharedPreferences.Editor edit = this.mLocksListSharedPreferences.edit();
        edit.remove(bluetoothDevice.getAddress());
        edit.commit();
        SharedPreferences.Editor edit2 = this.mLocksNamesSharedPreferences.edit();
        edit2.remove(bluetoothDevice.getAddress());
        edit2.commit();
    }

    public void save_name(BluetoothDevice bluetoothDevice, String str) {
        new HashMap();
        SharedPreferences.Editor edit = this.mLocksNamesSharedPreferences.edit();
        if (this.mLocksNamesSharedPreferences.getAll().containsKey(bluetoothDevice.getAddress())) {
            edit.remove(bluetoothDevice.getAddress());
            edit.commit();
        }
        edit.putString(bluetoothDevice.getAddress(), str);
        edit.commit();
    }
}
