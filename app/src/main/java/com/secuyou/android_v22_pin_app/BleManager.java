package com.secuyou.android_v22_pin_app;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

import no.nordicsemi.android.nrftoolbox.utility.ParserUtils;
/* loaded from: classes.dex */
public class BleManager {
    private static final int PAIRING_VARIANT_CONSENT = 3;
    private static final int PAIRING_VARIANT_DISPLAY_PASSKEY = 4;
    private static final int PAIRING_VARIANT_DISPLAY_PIN = 5;
    private static final int PAIRING_VARIANT_OOB_CONSENT = 6;
    private static final int PAIRING_VARIANT_PASSKEY = 1;
    private static final int PAIRING_VARIANT_PASSKEY_CONFIRMATION = 2;
    private static final int PAIRING_VARIANT_PIN = 0;
    private static final String TAG = "BleManager";
    protected BluetoothDevice mBluetoothDevice;
    private BluetoothGatt mBluetoothGatt;
    private final BroadcastReceiver mBluetoothStateBroadcastReceiver;
    protected BleManagerCallbacks mCallbacks;
    private BluetoothGattCharacteristic mConfirmCharacteristic;
    private boolean mConnected;
    private final Context mContext;
    private BluetoothGattCharacteristic mFirmwareCharacteristic;
    protected BleManagerGattCallback mGattCallback;
    private final Handler mHandler;
    private boolean mInitialConnection;
    private BluetoothGattCharacteristic mKeyCharacteristic;
    private boolean mLOCKED;
    private String mLockFirmware;
    public String mLockName;
    private String mLockSerial;
    private byte[] mLockState;
    private byte[] mLockStatus;
    private String mLockVersion;
    private BluetoothGattCharacteristic mModelCharacteristic;
    private BluetoothGattCharacteristic mNameCharacteristic;
    private final BroadcastReceiver mPairingRequestBroadcastReceiver;
    private BluetoothGattCharacteristic mSerialCharacteristic;
    private boolean mUserDisconnected;
    public static final UUID DIS_SERVICE = UUID.fromString("0000180A-0000-1000-8000-00805F9B34FB");
    public static final UUID MODEL_CHARACTERISTIC = UUID.fromString("00002A24-0000-1000-8000-00805F9B34FB");
    public static final UUID SERIAL_CHARACTERISTIC = UUID.fromString("00002A25-0000-1000-8000-00805F9B34FB");
    public static final UUID FIRMWARE_CHARACTERISTIC = UUID.fromString("00002A26-0000-1000-8000-00805F9B34FB");
    public static final UUID NA_CHARACTERISTIC = UUID.fromString("00002A29-0000-1000-8000-00805F9B34FB");
    public static final UUID KEY_SERVICE = UUID.fromString("B1DE1528-85EF-37CC-00C8-A3CF3412A548");
    public static final UUID KEY_CHARACTERISTIC = UUID.fromString("B1DE1529-85EF-37CC-00C8-A3CF3412A548");
    public static final UUID CONFIRM_CHARACTERISTIC = UUID.fromString("B1DE1530-85EF-37CC-00C8-A3CF3412A548");
    public static final UUID STATE_CHARACTERISTIC = UUID.fromString("B1DE1531-85EF-37CC-00C8-A3CF3412A548");
    public static final UUID NAME_CHARACTERISTIC = UUID.fromString("B1DE1532-85EF-37CC-00C8-A3CF3412A548");
    public static final String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    private static final UUID CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID = UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG);
    final byte[] anyByte_cmd = {0, 0, 0, 0, 0};
    byte[] byte_cmd = {0};
    byte[] byte_cmd_send_code = {1};
    private final Object LOCK = new Object();
    public final BleLock mLock = new BleLock(null);
    private int mConnectionState = 0;
    private int mBatteryValue = 10;

    private boolean internalSetLockversionNotifications(boolean z) {
        return false;
    }

    protected String bondStateToString(int i) {
        switch (i) {
            case 10:
                return "BOND_NONE";
            case 11:
                return "BOND_BONDING";
            case 12:
                return "BOND_BONDED";
            default:
                return "UNKNOWN";
        }
    }

    protected String pairingVariantToString(int i) {
        switch (i) {
            case 0:
                return "PAIRING_VARIANT_PIN";
            case 1:
                return "PAIRING_VARIANT_PASSKEY";
            case 2:
                return "PAIRING_VARIANT_PASSKEY_CONFIRMATION";
            case 3:
                return "PAIRING_VARIANT_CONSENT";
            case 4:
                return "PAIRING_VARIANT_DISPLAY_PASSKEY";
            case 5:
                return "PAIRING_VARIANT_DISPLAY_PIN";
            case 6:
                return "PAIRING_VARIANT_OOB_CONSENT";
            default:
                return "UNKNOWN";
        }
    }

    public void setGattCallbacks(Context context) {
    }

    protected boolean shouldAutoConnect() {
        return false;
    }

    protected String stateToString(int i) {
        return i != 1 ? i != 2 ? i != 3 ? "DISCONNECTED" : "DISCONNECTING" : "CONNECTED" : "CONNECTING";
    }

    public BleManager(Context context) {
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() { // from class: com.secuyou.android_v22_pin_app.BleManager.1
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context2, Intent intent) {
                int intExtra = intent.getIntExtra("android.bluetooth.adapter.extra.STATE", 10);
                int intExtra2 = intent.getIntExtra("android.bluetooth.adapter.extra.PREVIOUS_STATE", 10);
                String str = "[Broadcast] Action received: android.bluetooth.adapter.action.STATE_CHANGED, state changed to " + state2String(intExtra);
                if (intExtra != 10) {
                    if (intExtra == 12) {
                        BleManager.this.mGattCallback.onConnectionStateChange(BleManager.this.mBluetoothGatt, BleManager.this.mConnectionState, 12);
                        return;
                    } else if (intExtra != 13) {
                        return;
                    }
                }
                if (BleManager.this.mConnected && intExtra2 != 13 && intExtra2 != 10) {
                    BleManager.this.mUserDisconnected = true;
                    BleManager.this.mGattCallback.notifyDeviceDisconnected(BleManager.this.mBluetoothDevice);
                }
                BleManager.this.close();
            }

            private String state2String(int i) {
                switch (i) {
                    case 10:
                        return "OFF";
                    case 11:
                        return "TURNING ON";
                    case 12:
                        return "ON";
                    case 13:
                        return "TURNING OFF";
                    default:
                        return "UNKNOWN (" + i + ")";
                }
            }
        };
        this.mBluetoothStateBroadcastReceiver = broadcastReceiver;
        this.mPairingRequestBroadcastReceiver = new BroadcastReceiver() { // from class: com.secuyou.android_v22_pin_app.BleManager.2
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context2, Intent intent) {
                BluetoothDevice bluetoothDevice = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
                if (BleManager.this.mBluetoothGatt == null || !bluetoothDevice.getAddress().equals(BleManager.this.mBluetoothGatt.getDevice().getAddress())) {
                    return;
                }
                intent.getIntExtra("android.bluetooth.device.extra.PAIRING_VARIANT", 0);
            }
        };
        this.mContext = context;
        this.mHandler = new Handler();
        this.mGattCallback = new BleManagerGattCallback() { // from class: com.secuyou.android_v22_pin_app.BleManager.3
            @Override // com.secuyou.android_v22_pin_app.BleManager.BleManagerGattCallback
            protected Deque<Request> initGatt(BluetoothGatt bluetoothGatt) {
                return null;
            }

            @Override // com.secuyou.android_v22_pin_app.BleManager.BleManagerGattCallback
            protected void onDeviceDisconnected() {
                BleManager.this.mLock.getBleLockState().mState = BleLockState.STATE_DEVICE.KEY_GENERATION;
            }
        };
        this.mCallbacks = (BleManagerCallbacks) context;
        context.registerReceiver(broadcastReceiver, new IntentFilter("android.bluetooth.adapter.action.STATE_CHANGED"));
    }

    protected Context getContext() {
        return this.mContext;
    }

    public void connect(BluetoothDevice bluetoothDevice) {
        if (this.mConnected) {
            return;
        }
        synchronized (this.LOCK) {
            if (this.mBluetoothGatt != null) {
                Log.d(TAG, "gatt.close()");
                this.mBluetoothGatt.close();
                this.mBluetoothGatt = null;
                try {
                    Log.d(TAG, "wait(200)");
                    Thread.sleep(200L);
                } catch (InterruptedException unused) {
                }
                this.mBluetoothDevice = bluetoothDevice;
                Log.d(TAG, "Connecting...");
                this.mConnectionState = 1;
                this.mCallbacks.onDeviceConnecting(bluetoothDevice);
            }
        }
        boolean shouldAutoConnect = shouldAutoConnect();
        this.mUserDisconnected = shouldAutoConnect;
        if (shouldAutoConnect) {
            this.mInitialConnection = true;
        }
        this.mBluetoothDevice = bluetoothDevice;
        Log.d(TAG, "Connecting...");
        this.mConnectionState = 1;
        this.mCallbacks.onDeviceConnecting(bluetoothDevice);
        Log.d(TAG, "gatt = device.connectGatt(autoConnect = false)");
        this.mBluetoothGatt = bluetoothDevice.connectGatt(this.mContext, shouldAutoConnect, this.mGattCallback);
    }

    public boolean disconnect() {
        this.mUserDisconnected = true;
        this.mInitialConnection = false;
        if (!this.mConnected || this.mBluetoothGatt == null) {
            return false;
        }
        Log.d(TAG, "Disconnecting...");
        this.mConnectionState = 3;
        this.mCallbacks.onDeviceDisconnecting(this.mBluetoothGatt.getDevice());
        Log.d(TAG, "gatt.disconnect()");
        this.mBluetoothGatt.disconnect();
        return true;
    }

    public boolean isConnected() {
        return this.mConnected;
    }

    public int getConnectionState() {
        return this.mConnectionState;
    }

    public BleLockState.BATTERY_STATUS getBatteryValue() {
        return this.mLock.getBleLockState().mBatteryStatus;
    }

    public BleLockState.HANDLE_STATE getHandleValue() {
        return this.mLock.getBleLockState().mHandleState;
    }

    public byte[] getLockstatusValue() {
        return this.mLockStatus;
    }

    public String getLockversionValue() {
        return this.mLockVersion;
    }

    public CharSequence getLockNameValue() {
        return this.mLockName;
    }

    public void close() {
        try {
            this.mContext.unregisterReceiver(this.mBluetoothStateBroadcastReceiver);
        } catch (Exception unused) {
        }
        synchronized (this.mLock) {
            if (this.mBluetoothGatt != null) {
                Log.d(TAG, "gatt.close()");
                this.mBluetoothGatt.close();
                this.mBluetoothGatt = null;
            }
            this.mConnected = false;
            this.mInitialConnection = false;
            this.mConnectionState = 0;
            this.mGattCallback = null;
            this.mBluetoothDevice = null;
        }
    }

    protected final boolean createBond() {
        return enqueue(Request.createBond());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean internalCreateBond() {
        BluetoothDevice bluetoothDevice = this.mBluetoothDevice;
        boolean z = false;
        if (bluetoothDevice == null) {
            return false;
        }
        if (bluetoothDevice.getBondState() == 12) {
            Log.d(TAG, "Create bond request on already bonded device...");
            Log.d(TAG, "Device bonded");
            return false;
        }
        Log.d(TAG, "Starting pairing...");
        if (Build.VERSION.SDK_INT >= 19) {
            Log.d(TAG, "device.createBond()");
            z = bluetoothDevice.createBond();
        } else {
            try {
                Method method = bluetoothDevice.getClass().getMethod("createBond", new Class[0]);
                if (method != null) {
                    Log.d(TAG, "device.createBond() (hidden)");
                    z = ((Boolean) method.invoke(bluetoothDevice, new Object[0])).booleanValue();
                }
            } catch (Exception e) {
                Log.w(TAG, "An exception occurred while creating bond", e);
            }
        }
        if (!z) {
            Log.w(TAG, "Creating bond failed");
        }
        return z;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean ensureServiceChangedEnabled() {
        BluetoothGattService service;
        BluetoothGattCharacteristic characteristic;
        BluetoothGatt bluetoothGatt = this.mBluetoothGatt;
        if (bluetoothGatt == null || bluetoothGatt.getDevice().getBondState() != 12 || (service = bluetoothGatt.getService(KEY_SERVICE)) == null || (characteristic = service.getCharacteristic(KEY_CHARACTERISTIC)) == null) {
            return false;
        }
        Log.d(TAG, "Service Changed characteristic found on a bonded device");
        return internalEnableIndications(characteristic);
    }

    protected final boolean enableNotifications(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        return enqueue(Request.newEnableNotificationsRequest(bluetoothGattCharacteristic));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean internalEnableNotifications(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        BluetoothGatt bluetoothGatt = this.mBluetoothGatt;
        if (bluetoothGatt == null || bluetoothGattCharacteristic == null || (bluetoothGattCharacteristic.getProperties() & 16) == 0) {
            return false;
        }
        Log.d(TAG, "gatt.setCharacteristicNotification(" + bluetoothGattCharacteristic.getUuid() + ", true)");
        bluetoothGatt.setCharacteristicNotification(bluetoothGattCharacteristic, true);
        BluetoothGattDescriptor descriptor = bluetoothGattCharacteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
        if (descriptor != null) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            Log.d(TAG, "Enabling notifications for " + bluetoothGattCharacteristic.getUuid());
            Log.d(TAG, "gatt.writeDescriptor(00002902-0000-1000-8000-00805f9b34fb, value=0x01-00)");
            return bluetoothGatt.writeDescriptor(descriptor);
        }
        return false;
    }

    protected final boolean enableIndications(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        return enqueue(Request.newEnableIndicationsRequest(bluetoothGattCharacteristic));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean internalEnableIndications(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        BluetoothGatt bluetoothGatt = this.mBluetoothGatt;
        if (bluetoothGatt == null || bluetoothGattCharacteristic == null || (bluetoothGattCharacteristic.getProperties() & 32) == 0) {
            return false;
        }
        Log.d(TAG, "gatt.setCharacteristicNotification(" + bluetoothGattCharacteristic.getUuid() + ", true)");
        bluetoothGatt.setCharacteristicNotification(bluetoothGattCharacteristic, true);
        UUID uuid = CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID;
        BluetoothGattDescriptor descriptor = bluetoothGattCharacteristic.getDescriptor(uuid);
        if (descriptor != null) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
            Log.d(TAG, "Enabling indications for " + bluetoothGattCharacteristic.getUuid());
            Log.d(TAG, "gatt.writeDescriptor(" + uuid + ", value=0x02-00)");
            return bluetoothGatt.writeDescriptor(descriptor);
        }
        return false;
    }

    protected final boolean readCharacteristic(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        return enqueue(Request.newReadRequest(bluetoothGattCharacteristic));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean internalReadCharacteristic(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        BluetoothGatt bluetoothGatt = this.mBluetoothGatt;
        if (bluetoothGatt == null || bluetoothGattCharacteristic == null || (bluetoothGattCharacteristic.getProperties() & 2) == 0) {
            return false;
        }
        Log.d(TAG, "Reading characteristic " + bluetoothGattCharacteristic.getUuid());
        Log.d(TAG, "gatt.readCharacteristic(" + bluetoothGattCharacteristic.getUuid() + ")");
        return bluetoothGatt.readCharacteristic(bluetoothGattCharacteristic);
    }

    protected final boolean writeCharacteristic(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        return enqueue(Request.newWriteRequest(bluetoothGattCharacteristic, bluetoothGattCharacteristic.getValue()));
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public final boolean sendPincodeToLock() {
        this.mLock.setAppState(BleLock.APP_STATE.PROXIMITY_TAG_PINCODE_SEND);
        this.mKeyCharacteristic.setValue(PIN_ByteToByte());
        BluetoothGattCharacteristic bluetoothGattCharacteristic = this.mKeyCharacteristic;
        return enqueue(Request.newWriteRequest(bluetoothGattCharacteristic, bluetoothGattCharacteristic.getValue()));
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public final boolean sendNameToLock(String str) {
        this.mNameCharacteristic.setValue(str);
        BluetoothGattCharacteristic bluetoothGattCharacteristic = this.mNameCharacteristic;
        return enqueue(Request.newWriteRequest(bluetoothGattCharacteristic, bluetoothGattCharacteristic.getValue()));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean internalWriteCharacteristic(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        BluetoothGatt bluetoothGatt = this.mBluetoothGatt;
        if (bluetoothGatt == null || bluetoothGattCharacteristic == null || (bluetoothGattCharacteristic.getProperties() & 12) == 0) {
            return false;
        }
        Log.d(TAG, "Writing characteristic " + bluetoothGattCharacteristic.getUuid() + " (" + getWriteType(bluetoothGattCharacteristic.getWriteType()) + ")");
        Log.d(TAG, "gatt.writeCharacteristic(" + bluetoothGattCharacteristic.getUuid() + ")");
        return bluetoothGatt.writeCharacteristic(bluetoothGattCharacteristic);
    }

    protected final boolean readDescriptor(BluetoothGattDescriptor bluetoothGattDescriptor) {
        return enqueue(Request.newReadRequest(bluetoothGattDescriptor));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean internalReadDescriptor(BluetoothGattDescriptor bluetoothGattDescriptor) {
        BluetoothGatt bluetoothGatt = this.mBluetoothGatt;
        if (bluetoothGatt == null || bluetoothGattDescriptor == null) {
            return false;
        }
        Log.d(TAG, "Reading descriptor " + bluetoothGattDescriptor.getUuid());
        Log.d(TAG, "gatt.readDescriptor(" + bluetoothGattDescriptor.getUuid() + ")");
        return bluetoothGatt.readDescriptor(bluetoothGattDescriptor);
    }

    protected final boolean writeDescriptor(BluetoothGattDescriptor bluetoothGattDescriptor) {
        return enqueue(Request.newWriteRequest(bluetoothGattDescriptor, bluetoothGattDescriptor.getValue()));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean internalWriteDescriptor(BluetoothGattDescriptor bluetoothGattDescriptor) {
        BluetoothGatt bluetoothGatt = this.mBluetoothGatt;
        if (bluetoothGatt == null || bluetoothGattDescriptor == null) {
            return false;
        }
        Log.d(TAG, "Writing descriptor " + bluetoothGattDescriptor.getUuid());
        Log.d(TAG, "gatt.writeDescriptor(" + bluetoothGattDescriptor.getUuid() + ")");
        BluetoothGattCharacteristic characteristic = bluetoothGattDescriptor.getCharacteristic();
        int writeType = characteristic.getWriteType();
        characteristic.setWriteType(2);
        boolean writeDescriptor = bluetoothGatt.writeDescriptor(bluetoothGattDescriptor);
        characteristic.setWriteType(writeType);
        return writeDescriptor;
    }

    public final boolean readLockstatus() {
        return enqueue(Request.newReadLockstatusRequest());
    }

    public final boolean readLockState() {
        return enqueue(Request.newReadLockStateRequest());
    }

    public final boolean readLockversion() {
        return enqueue(Request.newReadLockversionRequest());
    }

    public final boolean readLockName() {
        return enqueue(Request.newReadLockNameRequest());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean internalReadLockstatus() {
        BluetoothGattService service;
        BluetoothGattCharacteristic characteristic;
        BluetoothGatt bluetoothGatt = this.mBluetoothGatt;
        if (bluetoothGatt == null || (service = bluetoothGatt.getService(KEY_SERVICE)) == null || (characteristic = service.getCharacteristic(KEY_CHARACTERISTIC)) == null || (characteristic.getProperties() & 2) == 0) {
            return false;
        }
        Log.d(TAG, "Reading lock status...");
        return internalReadCharacteristic(characteristic);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean internalReadLockState() {
        BluetoothGattService service;
        BluetoothGattCharacteristic characteristic;
        BluetoothGatt bluetoothGatt = this.mBluetoothGatt;
        if (bluetoothGatt == null || (service = bluetoothGatt.getService(KEY_SERVICE)) == null || (characteristic = service.getCharacteristic(STATE_CHARACTERISTIC)) == null || (characteristic.getProperties() & 2) == 0) {
            return false;
        }
        Log.d(TAG, "Reading lock state...");
        return internalReadCharacteristic(characteristic);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean internalReadLockName() {
        BluetoothGattService service;
        BluetoothGattCharacteristic characteristic;
        BluetoothGatt bluetoothGatt = this.mBluetoothGatt;
        if (bluetoothGatt == null || (service = bluetoothGatt.getService(KEY_SERVICE)) == null || (characteristic = service.getCharacteristic(NAME_CHARACTERISTIC)) == null || (characteristic.getProperties() & 2) == 0) {
            return false;
        }
        Log.d(TAG, "Reading lock name...");
        return internalReadCharacteristic(characteristic);
    }

    public final boolean setLockstatusNotifications(boolean z) {
        if (z) {
            return enqueue(Request.newEnableLockstatusNotificationsRequest());
        }
        return enqueue(Request.newDisableLockstatusNotificationsRequest());
    }

    public final boolean setLockStateNotifications(boolean z) {
        if (z) {
            return enqueue(Request.newEnableLockStateNotificationsRequest());
        }
        return enqueue(Request.newDisableLockStateNotificationsRequest());
    }

    public final boolean setLockNameNotifications(boolean z) {
        if (z) {
            return enqueue(Request.newEnableLockNameNotificationsRequest());
        }
        return enqueue(Request.newDisableLockNameNotificationsRequest());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean internalSetLockstatusNotifications(boolean z) {
        BluetoothGattService service;
        UUID uuid;
        BluetoothGattCharacteristic characteristic;
        BluetoothGatt bluetoothGatt = this.mBluetoothGatt;
        if (bluetoothGatt == null || (service = bluetoothGatt.getService(KEY_SERVICE)) == null || (characteristic = service.getCharacteristic((uuid = KEY_CHARACTERISTIC))) == null || (characteristic.getProperties() & 16) == 0) {
            return false;
        }
        bluetoothGatt.setCharacteristicNotification(characteristic, z);
        UUID uuid2 = CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID;
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(uuid2);
        if (descriptor != null) {
            if (z) {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                Log.d(TAG, "Enabling lockstatus notifications...");
                Log.d(TAG, "Enabling notifications for " + uuid);
                Log.d(TAG, "gatt.writeDescriptor(" + uuid2 + ", value=0x0100)");
            } else {
                descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                Log.d(TAG, "Disabling lockstatus notifications...");
                Log.d(TAG, "Disabling notifications for " + uuid);
                Log.d(TAG, "gatt.writeDescriptor(" + uuid2 + ", value=0x0000)");
            }
            return bluetoothGatt.writeDescriptor(descriptor);
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean internalSetLockStateNotifications(boolean z) {
        BluetoothGattService service;
        UUID uuid;
        BluetoothGattCharacteristic characteristic;
        BluetoothGatt bluetoothGatt = this.mBluetoothGatt;
        if (bluetoothGatt == null || (service = bluetoothGatt.getService(KEY_SERVICE)) == null || (characteristic = service.getCharacteristic((uuid = STATE_CHARACTERISTIC))) == null || (characteristic.getProperties() & 16) == 0) {
            return false;
        }
        bluetoothGatt.setCharacteristicNotification(characteristic, z);
        UUID uuid2 = CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID;
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(uuid2);
        if (descriptor != null) {
            if (z) {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                Log.d(TAG, "Enabling lockstate notifications...");
                Log.d(TAG, "Enabling notifications for " + uuid);
                Log.d(TAG, "gatt.writeDescriptor(" + uuid2 + ", value=0x0100)");
            } else {
                descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                Log.d(TAG, "Disabling lockstate notifications...");
                Log.d(TAG, "Disabling notifications for " + uuid);
                Log.d(TAG, "gatt.writeDescriptor(" + uuid2 + ", value=0x0000)");
            }
            return bluetoothGatt.writeDescriptor(descriptor);
        }
        return false;
    }

    public boolean enqueue(Request request) {
        BleManagerGattCallback bleManagerGattCallback = this.mGattCallback;
        if (bleManagerGattCallback != null) {
            bleManagerGattCallback.mTaskQueue.add(request);
            this.mGattCallback.nextRequest();
            return true;
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* loaded from: classes.dex */
    public static final class Request {
        private final BluetoothGattCharacteristic characteristic;
        private final BluetoothGattDescriptor descriptor;
        private final Type type;
        private final byte[] value;
        private final int writeType;

        /* JADX INFO: Access modifiers changed from: private */
        /* loaded from: classes.dex */
        public enum Type {
            CREATE_BOND,
            WRITE,
            READ,
            WRITE_DESCRIPTOR,
            READ_DESCRIPTOR,
            ENABLE_NOTIFICATIONS,
            ENABLE_INDICATIONS,
            READ_BATTERY_LEVEL,
            ENABLE_BATTERY_LEVEL_NOTIFICATIONS,
            DISABLE_BATTERY_LEVEL_NOTIFICATIONS,
            ENABLE_SERVICE_CHANGED_INDICATIONS,
            READ_LOCKSTATUS,
            READ_LOCKSTATE,
            DISABLE_LOCKSTATUS_NOTIFICATIONS,
            ENABLE_LOCKSTATUS_NOTIFICATIONS,
            DISABLE_LOCKSTATE_NOTIFICATIONS,
            ENABLE_LOCKSTATE_NOTIFICATIONS,
            DISABLE_LOCKNAME_NOTIFICATIONS,
            ENABLE_LOCKNAME_NOTIFICATIONS,
            READ_LOCKVERSION,
            READ_LOCKNAME
        }

        private Request(Type type) {
            this.type = type;
            this.characteristic = null;
            this.descriptor = null;
            this.value = null;
            this.writeType = 0;
        }

        private Request(Type type, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
            this.type = type;
            this.characteristic = bluetoothGattCharacteristic;
            this.descriptor = null;
            this.value = null;
            this.writeType = 0;
        }

        private Request(Type type, BluetoothGattCharacteristic bluetoothGattCharacteristic, int i, byte[] bArr, int i2, int i3) {
            this.type = type;
            this.characteristic = bluetoothGattCharacteristic;
            this.descriptor = null;
            this.value = copy(bArr, i2, i3);
            this.writeType = i;
        }

        private Request(Type type, BluetoothGattDescriptor bluetoothGattDescriptor) {
            this.type = type;
            this.characteristic = null;
            this.descriptor = bluetoothGattDescriptor;
            this.value = null;
            this.writeType = 0;
        }

        private Request(Type type, BluetoothGattDescriptor bluetoothGattDescriptor, byte[] bArr, int i, int i2) {
            this.type = type;
            this.characteristic = null;
            this.descriptor = bluetoothGattDescriptor;
            this.value = copy(bArr, i, i2);
            this.writeType = 2;
        }

        private static byte[] copy(byte[] bArr, int i, int i2) {
            if (bArr == null || i > bArr.length) {
                return null;
            }
            int min = Math.min(bArr.length - i, i2);
            byte[] bArr2 = new byte[min];
            System.arraycopy(bArr, i, bArr2, 0, min);
            return bArr2;
        }

        public static Request createBond() {
            return new Request(Type.CREATE_BOND);
        }

        public static Request newReadRequest(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
            return new Request(Type.READ, bluetoothGattCharacteristic);
        }

        public static Request newWriteRequest(BluetoothGattCharacteristic bluetoothGattCharacteristic, byte[] bArr) {
            return new Request(Type.WRITE, bluetoothGattCharacteristic, bluetoothGattCharacteristic.getWriteType(), bArr, 0, bArr != null ? bArr.length : 0);
        }

        public static Request newWriteRequest(BluetoothGattCharacteristic bluetoothGattCharacteristic, byte[] bArr, int i) {
            return new Request(Type.WRITE, bluetoothGattCharacteristic, i, bArr, 0, bArr != null ? bArr.length : 0);
        }

        public static Request newWriteRequest(BluetoothGattCharacteristic bluetoothGattCharacteristic, byte[] bArr, int i, int i2) {
            return new Request(Type.WRITE, bluetoothGattCharacteristic, bluetoothGattCharacteristic.getWriteType(), bArr, i, i2);
        }

        public static Request newWriteRequest(BluetoothGattCharacteristic bluetoothGattCharacteristic, byte[] bArr, int i, int i2, int i3) {
            return new Request(Type.WRITE, bluetoothGattCharacteristic, i3, bArr, i, i2);
        }

        public static Request newReadRequest(BluetoothGattDescriptor bluetoothGattDescriptor) {
            return new Request(Type.READ_DESCRIPTOR, bluetoothGattDescriptor);
        }

        public static Request newWriteRequest(BluetoothGattDescriptor bluetoothGattDescriptor, byte[] bArr) {
            return new Request(Type.WRITE_DESCRIPTOR, bluetoothGattDescriptor, bArr, 0, bArr != null ? bArr.length : 0);
        }

        public static Request newWriteRequest(BluetoothGattDescriptor bluetoothGattDescriptor, byte[] bArr, int i, int i2) {
            return new Request(Type.WRITE_DESCRIPTOR, bluetoothGattDescriptor, bArr, i, i2);
        }

        public static Request newEnableNotificationsRequest(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
            return new Request(Type.ENABLE_NOTIFICATIONS, bluetoothGattCharacteristic);
        }

        public static Request newEnableIndicationsRequest(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
            return new Request(Type.ENABLE_INDICATIONS, bluetoothGattCharacteristic);
        }

        public static Request newReadBatteryLevelRequest() {
            return new Request(Type.READ_BATTERY_LEVEL);
        }

        public static Request newReadLockstatusRequest() {
            return new Request(Type.READ_LOCKSTATUS);
        }

        public static Request newReadLockStateRequest() {
            return new Request(Type.READ_LOCKSTATE);
        }

        public static Request newReadLockversionRequest() {
            return new Request(Type.READ_LOCKVERSION);
        }

        public static Request newReadLockNameRequest() {
            return new Request(Type.READ_LOCKNAME);
        }

        public static Request newEnableBatteryLevelNotificationsRequest() {
            return new Request(Type.ENABLE_BATTERY_LEVEL_NOTIFICATIONS);
        }

        public static Request newEnableLockstatusNotificationsRequest() {
            return new Request(Type.ENABLE_LOCKSTATUS_NOTIFICATIONS);
        }

        public static Request newDisableLockstatusNotificationsRequest() {
            return new Request(Type.DISABLE_LOCKSTATUS_NOTIFICATIONS);
        }

        public static Request newEnableLockStateNotificationsRequest() {
            return new Request(Type.ENABLE_LOCKSTATE_NOTIFICATIONS);
        }

        public static Request newDisableLockStateNotificationsRequest() {
            return new Request(Type.DISABLE_LOCKSTATE_NOTIFICATIONS);
        }

        public static Request newEnableLockNameNotificationsRequest() {
            return new Request(Type.ENABLE_LOCKNAME_NOTIFICATIONS);
        }

        public static Request newDisableLockNameNotificationsRequest() {
            return new Request(Type.DISABLE_LOCKNAME_NOTIFICATIONS);
        }

        public static Request newDisableBatteryLevelNotificationsRequest() {
            return new Request(Type.DISABLE_BATTERY_LEVEL_NOTIFICATIONS);
        }

        private static Request newEnableServiceChangedIndicationsRequest() {
            return new Request(Type.ENABLE_SERVICE_CHANGED_INDICATIONS);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* loaded from: classes.dex */
    public abstract class BleManagerGattCallback extends BluetoothGattCallback {
        private static final String ERROR_AUTH_ERROR_WHILE_BONDED = "Phone has lost bonding information";
        private static final String ERROR_CONNECTION_STATE_CHANGE = "Error on connection state change";
        private static final String ERROR_DISCOVERY_SERVICE = "Error on discovering services";
        private static final String ERROR_READ_CHARACTERISTIC = "Error on reading characteristic";
        private static final String ERROR_READ_DESCRIPTOR = "Error on reading descriptor";
        private static final String ERROR_WRITE_CHARACTERISTIC = "Error on writing characteristic";
        private static final String ERROR_WRITE_DESCRIPTOR = "Error on writing descriptor";
        private boolean mInitInProgress;
        private Deque<Request> mInitQueue;
        private final Queue<Request> mTaskQueue = new LinkedList();
        private boolean mOperationInProgress = true;

        protected abstract Deque<Request> initGatt(BluetoothGatt bluetoothGatt);

        protected boolean isOptionalServiceSupported(BluetoothGatt bluetoothGatt) {
            return true;
        }

        protected void onCharacteristicIndicated(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        }

        protected void onCharacteristicNotified(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        }

        protected void onCharacteristicRead(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        }

        protected void onCharacteristicWrite(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        }

        protected void onDescriptorRead(BluetoothGatt bluetoothGatt, BluetoothGattDescriptor bluetoothGattDescriptor) {
        }

        protected void onDescriptorWrite(BluetoothGatt bluetoothGatt, BluetoothGattDescriptor bluetoothGattDescriptor) {
        }

        protected abstract void onDeviceDisconnected();

        protected void onLockFirmwareValueReceived(BluetoothGatt bluetoothGatt, byte[] bArr) {
        }

        protected void onLockModelValueReceived(BluetoothGatt bluetoothGatt, byte[] bArr) {
        }

        protected void onLockNameValueReceived(BluetoothGatt bluetoothGatt, byte[] bArr) {
        }

        protected void onLockSerialValueReceived(BluetoothGatt bluetoothGatt, byte[] bArr) {
        }

        protected void onLockStateValueReceived(BluetoothGatt bluetoothGatt, byte[] bArr) {
        }

        protected void onLockstatusValueReceived(BluetoothGatt bluetoothGatt, byte[] bArr) {
        }

        protected BleManagerGattCallback() {
        }

        protected boolean isRequiredServiceSupported(BluetoothGatt bluetoothGatt) {
            return bluetoothGatt.getService(BleManager.KEY_SERVICE) != null;
        }

        protected void onDeviceReady() {
            BleManager.this.mCallbacks.onDeviceReady(BleManager.this.mBluetoothGatt.getDevice());
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void notifyDeviceDisconnected(BluetoothDevice bluetoothDevice) {
            BleManager.this.mConnected = false;
            BleManager.this.mConnectionState = 0;
            if (BleManager.this.mUserDisconnected) {
                Log.d(BleManager.TAG, "Disconnected");
                BleManager.this.mCallbacks.onDeviceDisconnected(bluetoothDevice);
                BleManager.this.close();
            } else {
                Log.d(BleManager.TAG, "Connection lost");
                BleManager.this.mCallbacks.onLinkLossOccur(bluetoothDevice);
            }
            onDeviceDisconnected();
        }

        private void onError(BluetoothDevice bluetoothDevice, String str, int i) {
            Log.d(BleManager.TAG, "GATT Error (0x" + Integer.toHexString(i) + "): ");
            BleManager.this.mCallbacks.onError(bluetoothDevice, str, i);
        }

        @Override // android.bluetooth.BluetoothGattCallback
        public final void onConnectionStateChange(BluetoothGatt bluetoothGatt, int i, int i2) {
            Log.d(BleManager.TAG, "[Callback] Connection state changed with status: " + i + " and new state: " + i2 + " (" + BleManager.this.stateToString(i2) + ")");
            if (i == 0 && i2 == 2) {
                Log.d(BleManager.TAG, "Connected to " + bluetoothGatt.getDevice().getAddress());
                BleManager.this.mConnected = true;
                BleManager.this.mConnectionState = 2;
                BleManager.this.mCallbacks.onDeviceConnected(bluetoothGatt.getDevice());
                bluetoothGatt.discoverServices();
            } else if (i2 == 0) {
                if (i != 0) {
                    Log.d(BleManager.TAG, "GATT Error: (0x" + Integer.toHexString(i) + "): ");
                }
                this.mOperationInProgress = true;
                this.mInitQueue = null;
                this.mTaskQueue.clear();
                if (BleManager.this.mConnected) {
                    notifyDeviceDisconnected(bluetoothGatt.getDevice());
                }
                BleManager bleManager = BleManager.this;
                bleManager.connect(bleManager.mBluetoothDevice);
            } else {
                Log.d(BleManager.TAG, "Error (0x" + Integer.toHexString(i) + "): ");
                BleManager.this.mCallbacks.onError(bluetoothGatt.getDevice(), ERROR_CONNECTION_STATE_CHANGE, i);
            }
        }

        @Override // android.bluetooth.BluetoothGattCallback
        public final void onServicesDiscovered(BluetoothGatt bluetoothGatt, int i) {
            ArrayList arrayList = new ArrayList();
            if (i == 0) {
                Log.d(BleManager.TAG, "Services Discovered");
                if (isRequiredServiceSupported(bluetoothGatt)) {
                    Log.d(BleManager.TAG, "Primary service found");
                    boolean isOptionalServiceSupported = isOptionalServiceSupported(bluetoothGatt);
                    if (isOptionalServiceSupported) {
                        Log.d(BleManager.TAG, "Secondary service found");
                    }
                    BleManager.this.mCallbacks.onServicesDiscovered(bluetoothGatt.getDevice(), isOptionalServiceSupported);
                    BleManager.this.mLock.setAppState(BleLock.APP_STATE.SERVICES_DISCOVERED);
                    BluetoothGattService service = bluetoothGatt.getService(BleManager.KEY_SERVICE);
                    for (int i2 = 0; i2 < arrayList.size(); i2++) {
                        arrayList.remove(arrayList.get(i2));
                    }
                    for (BluetoothGattCharacteristic bluetoothGattCharacteristic : service.getCharacteristics()) {
                        if (bluetoothGattCharacteristic.getUuid().equals(BleManager.CONFIRM_CHARACTERISTIC)) {
                            BleManager.this.mConfirmCharacteristic = bluetoothGattCharacteristic;
                        }
                    }
                    this.mInitInProgress = true;
                    Deque<Request> initGatt = initGatt(bluetoothGatt);
                    this.mInitQueue = initGatt;
                    if (initGatt == null) {
                        this.mInitQueue = new LinkedList();
                    }
                    if (BleManager.this.mCallbacks.shouldEnableLockstatusNotifications(bluetoothGatt.getDevice())) {
                        this.mInitQueue.addFirst(Request.newEnableLockstatusNotificationsRequest());
                    }
                    if (BleManager.this.mCallbacks.shouldEnableLockStateNotifications(bluetoothGatt.getDevice())) {
                        this.mInitQueue.addFirst(Request.newEnableLockStateNotificationsRequest());
                    }
                    if (BleManager.this.mCallbacks.shouldEnableLockNameNotifications(bluetoothGatt.getDevice())) {
                        this.mInitQueue.addFirst(Request.newEnableLockNameNotificationsRequest());
                    }
                    this.mInitQueue.addFirst(Request.newReadLockstatusRequest());
                    this.mInitQueue.addFirst(Request.newReadLockStateRequest());
                    this.mInitQueue.addFirst(Request.newReadLockNameRequest());
                    BluetoothGattService service2 = bluetoothGatt.getService(BleManager.DIS_SERVICE);
                    this.mInitQueue.addFirst(Request.newReadRequest(service2.getCharacteristic(BleManager.MODEL_CHARACTERISTIC)));
                    this.mInitQueue.addFirst(Request.newReadRequest(service2.getCharacteristic(BleManager.SERIAL_CHARACTERISTIC)));
                    this.mInitQueue.addFirst(Request.newReadRequest(service2.getCharacteristic(BleManager.FIRMWARE_CHARACTERISTIC)));
                    this.mOperationInProgress = false;
                    nextRequest();
                    return;
                }
                Log.d(BleManager.TAG, "Device is not supported");
                BleManager.this.mCallbacks.onDeviceNotSupported(bluetoothGatt.getDevice());
                BleManager.this.disconnect();
                return;
            }
            Log.d(BleManager.TAG, "onServicesDiscovered error " + i);
            onError(bluetoothGatt.getDevice(), ERROR_DISCOVERY_SERVICE, i);
        }

        @Override // android.bluetooth.BluetoothGattCallback
        public final void onCharacteristicRead(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, int i) {
            if (i != 0) {
                if (i == 5) {
                    if (bluetoothGatt.getDevice().getBondState() != 10) {
                        Log.d(BleManager.TAG, ERROR_AUTH_ERROR_WHILE_BONDED);
                        BleManager.this.mCallbacks.onError(bluetoothGatt.getDevice(), ERROR_AUTH_ERROR_WHILE_BONDED, i);
                        return;
                    }
                    return;
                }
                Log.d(BleManager.TAG, "onCharacteristicRead error " + i);
                onError(bluetoothGatt.getDevice(), ERROR_READ_CHARACTERISTIC, i);
                return;
            }
            Log.d(BleManager.TAG, "Read Response received from " + bluetoothGattCharacteristic.getUuid() + ", value: " + ParserUtils.parse(bluetoothGattCharacteristic));
            if (isLockstatusCharacteristic(bluetoothGattCharacteristic)) {
                byte[] value = bluetoothGattCharacteristic.getValue();
                BleManager.this.mKeyCharacteristic = bluetoothGattCharacteristic;
                Log.d(BleManager.TAG, "Lock status received: " + value + "%");
                BleManager.this.mLockStatus = value;
                BleManager.this.mLock.getBleLockState().setLockStatus(BleManager.this.mLockStatus);
                onLockstatusValueReceived(bluetoothGatt, value);
                BleManager.this.mCallbacks.onLockstatusValueReceived(bluetoothGatt.getDevice(), value);
                if (BleManager.this.mLock.getAppState() == BleLock.APP_STATE.SERVICES_DISCOVERED) {
                    BleManager.this.state_machine_lock();
                }
            } else if (isLockStateCharacteristic(bluetoothGattCharacteristic)) {
                byte[] value2 = bluetoothGattCharacteristic.getValue();
                Log.d(BleManager.TAG, "Lock status received: " + value2 + "%");
                BleManager.this.mLockState = value2;
                BleManager.this.mLock.getBleLockState().setLockState(BleManager.this.mLockState);
                onLockStateValueReceived(bluetoothGatt, value2);
                BleManager.this.mCallbacks.onLockStateValueReceived(bluetoothGatt.getDevice(), value2);
                if (BleManager.this.mLock.getAppState() == BleLock.APP_STATE.SERVICES_DISCOVERED) {
                    BleManager.this.state_machine_lock();
                }
            } else if (isLockNameCharacteristic(bluetoothGattCharacteristic)) {
                byte[] value3 = bluetoothGattCharacteristic.getValue();
                BleManager.this.mNameCharacteristic = bluetoothGattCharacteristic;
                Log.d(BleManager.TAG, "Lock name received: " + value3 + "%");
                BleManager.this.mLockName = new String(value3);
                BleManager.this.mLock.setNameStringValue(BleManager.this.mLockName);
                onLockNameValueReceived(bluetoothGatt, value3);
                BleManager.this.mCallbacks.onLockNameValueReceived(bluetoothGatt.getDevice(), value3);
            } else if (isModelCharacteristic(bluetoothGattCharacteristic)) {
                byte[] value4 = bluetoothGattCharacteristic.getValue();
                BleManager.this.mModelCharacteristic = bluetoothGattCharacteristic;
                Log.d(BleManager.TAG, "Lock model received: " + value4 + "%");
                BleManager.this.mLockVersion = new String(value4);
                BleManager.this.mLock.setModelStringValue(BleManager.this.mLockVersion);
                onLockModelValueReceived(bluetoothGatt, value4);
                BleManager.this.mCallbacks.onLockModelValueReceived(bluetoothGatt.getDevice(), value4);
            } else if (isSerialCharacteristic(bluetoothGattCharacteristic)) {
                byte[] value5 = bluetoothGattCharacteristic.getValue();
                BleManager.this.mSerialCharacteristic = bluetoothGattCharacteristic;
                Log.d(BleManager.TAG, "Lock model received: " + value5 + "%");
                BleManager.this.mLockSerial = new String(value5);
                BleManager.this.mLock.setSerialStringValue(BleManager.this.mLockSerial);
                onLockSerialValueReceived(bluetoothGatt, value5);
                BleManager.this.mCallbacks.onLockSerialValueReceived(bluetoothGatt.getDevice(), value5);
            } else if (isFirmwareCharacteristic(bluetoothGattCharacteristic)) {
                byte[] value6 = bluetoothGattCharacteristic.getValue();
                BleManager.this.mFirmwareCharacteristic = bluetoothGattCharacteristic;
                Log.d(BleManager.TAG, "Lock model received: " + value6 + "%");
                BleManager.this.mLockFirmware = new String(value6);
                BleManager.this.mLock.setLockFirmwareVersion(BleManager.this.mLockFirmware);
                onLockFirmwareValueReceived(bluetoothGatt, value6);
                BleManager.this.mCallbacks.onLockFirmwareValueReceived(bluetoothGatt.getDevice(), value6);
            } else {
                onCharacteristicRead(bluetoothGatt, bluetoothGattCharacteristic);
            }
            this.mOperationInProgress = false;
            nextRequest();
        }

        @Override // android.bluetooth.BluetoothGattCallback
        public final void onCharacteristicWrite(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, int i) {
            if (i == 0) {
                Log.d(BleManager.TAG, "Data written to " + bluetoothGattCharacteristic.getUuid() + ", value: " + ParserUtils.parse(bluetoothGattCharacteristic));
                onCharacteristicWrite(bluetoothGatt, bluetoothGattCharacteristic);
                this.mOperationInProgress = false;
                nextRequest();
            } else if (i == 5) {
                if (bluetoothGatt.getDevice().getBondState() != 10) {
                    Log.d(BleManager.TAG, ERROR_AUTH_ERROR_WHILE_BONDED);
                    BleManager.this.mCallbacks.onError(bluetoothGatt.getDevice(), ERROR_AUTH_ERROR_WHILE_BONDED, i);
                }
            } else {
                Log.d(BleManager.TAG, "onCharacteristicWrite error " + i);
                onError(bluetoothGatt.getDevice(), ERROR_WRITE_CHARACTERISTIC, i);
            }
        }

        @Override // android.bluetooth.BluetoothGattCallback
        public void onDescriptorRead(BluetoothGatt bluetoothGatt, BluetoothGattDescriptor bluetoothGattDescriptor, int i) {
            if (i == 0) {
                Log.d(BleManager.TAG, "Read Response received from descr. " + bluetoothGattDescriptor.getUuid() + ", value: " + ParserUtils.parse(bluetoothGattDescriptor));
                onDescriptorRead(bluetoothGatt, bluetoothGattDescriptor);
                this.mOperationInProgress = false;
                nextRequest();
            } else if (i == 5) {
                if (bluetoothGatt.getDevice().getBondState() != 10) {
                    Log.d(BleManager.TAG, ERROR_AUTH_ERROR_WHILE_BONDED);
                    BleManager.this.mCallbacks.onError(bluetoothGatt.getDevice(), ERROR_AUTH_ERROR_WHILE_BONDED, i);
                }
            } else {
                Log.d(BleManager.TAG, "onDescriptorRead error " + i);
                onError(bluetoothGatt.getDevice(), ERROR_READ_DESCRIPTOR, i);
            }
        }

        @Override // android.bluetooth.BluetoothGattCallback
        public final void onDescriptorWrite(BluetoothGatt bluetoothGatt, BluetoothGattDescriptor bluetoothGattDescriptor, int i) {
            if (i != 0) {
                if (i == 5) {
                    if (bluetoothGatt.getDevice().getBondState() != 10) {
                        Log.d(BleManager.TAG, ERROR_AUTH_ERROR_WHILE_BONDED);
                        BleManager.this.mCallbacks.onError(bluetoothGatt.getDevice(), ERROR_AUTH_ERROR_WHILE_BONDED, i);
                        return;
                    }
                    return;
                }
                Log.d(BleManager.TAG, "onDescriptorWrite error " + i);
                onError(bluetoothGatt.getDevice(), ERROR_WRITE_DESCRIPTOR, i);
                return;
            }
            Log.d(BleManager.TAG, "Data written to descr. " + bluetoothGattDescriptor.getUuid() + ", value: " + ParserUtils.parse(bluetoothGattDescriptor));
            if (isServiceChangedCCCD(bluetoothGattDescriptor)) {
                Log.d(BleManager.TAG, "Service Changed notifications enabled");
            } else if (isKeyCharacteristicCCCD(bluetoothGattDescriptor)) {
                byte[] value = bluetoothGattDescriptor.getValue();
                if (value != null && value.length == 2 && value[1] == 0) {
                    if (value[0] == 1) {
                        Log.d(BleManager.TAG, "Key char notifications enabled");
                    } else {
                        Log.d(BleManager.TAG, "Battery Level notifications disabled");
                    }
                } else {
                    onDescriptorWrite(bluetoothGatt, bluetoothGattDescriptor);
                }
            } else if (isCCCD(bluetoothGattDescriptor)) {
                byte[] value2 = bluetoothGattDescriptor.getValue();
                if (value2 != null && value2.length == 2 && value2[1] == 0) {
                    byte b = value2[0];
                    if (b == 0) {
                        Log.d(BleManager.TAG, "Notifications and indications disabled");
                    } else if (b == 1) {
                        Log.d(BleManager.TAG, "Notifications enabled");
                    } else if (b == 2) {
                        Log.d(BleManager.TAG, "Indications enabled");
                    }
                } else {
                    onDescriptorWrite(bluetoothGatt, bluetoothGattDescriptor);
                }
            } else {
                onDescriptorWrite(bluetoothGatt, bluetoothGattDescriptor);
            }
            this.mOperationInProgress = false;
            nextRequest();
        }

        @Override // android.bluetooth.BluetoothGattCallback
        public final void onCharacteristicChanged(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
            String parse = ParserUtils.parse(bluetoothGattCharacteristic);
            if (isLockstatusCharacteristic(bluetoothGattCharacteristic)) {
                Log.d(BleManager.TAG, "Notification received from " + bluetoothGattCharacteristic.getUuid() + ", value: " + parse);
                byte[] value = bluetoothGattCharacteristic.getValue();
                Log.d(BleManager.TAG, "Lockstatus received: " + value + "%");
                BleManager.this.mLockStatus = value;
                BleManager.this.mLock.getBleLockState().setLockStatus(BleManager.this.mLockStatus);
                onLockstatusValueReceived(bluetoothGatt, value);
                BleManager.this.mCallbacks.onLockstatusValueReceived(bluetoothGatt.getDevice(), value);
                if (BleManager.this.mLock.getAppState() != BleLock.APP_STATE.PROXIMITY_TAG_PINCODE_SEND) {
                    BleManager.this.state_machine_lock();
                }
                if (BleManager.this.mLock.getBleLockState().mState != BleLockState.STATE_DEVICE.KEY_CHECKING || BleManager.this.mLock.getBleLockState().PIN_CODE_CORRECT) {
                    return;
                }
                BleManager.this.mCallbacks.onDeviceNotSupported(BleManager.this.mBluetoothDevice);
                BleManager.this.disconnect();
            } else if (isLockStateCharacteristic(bluetoothGattCharacteristic)) {
                Log.d(BleManager.TAG, "Notification received from " + bluetoothGattCharacteristic.getUuid() + ", value: " + parse);
                byte[] value2 = bluetoothGattCharacteristic.getValue();
                Log.d(BleManager.TAG, "Lockstatus received: " + value2 + "%");
                BleManager.this.mLockState = value2;
                BleManager.this.mLock.getBleLockState().setLockState(BleManager.this.mLockState);
                onLockstatusValueReceived(bluetoothGatt, value2);
                BleManager.this.mCallbacks.onLockStateValueReceived(bluetoothGatt.getDevice(), value2);
                BleManager.this.state_machine_lock();
            } else if (isLockNameCharacteristic(bluetoothGattCharacteristic)) {
                byte[] value3 = bluetoothGattCharacteristic.getValue();
                BleManager.this.mNameCharacteristic = bluetoothGattCharacteristic;
                Log.d(BleManager.TAG, "Lock name received: " + value3 + "%");
                BleManager.this.mLockName = new String(value3);
                BleManager.this.mLock.setNameStringValue(BleManager.this.mLockName);
                onLockNameValueReceived(bluetoothGatt, value3);
                BleManager.this.mCallbacks.onLockNameValueReceived(bluetoothGatt.getDevice(), value3);
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void nextRequest() {
            boolean internalCreateBond;
            if (this.mOperationInProgress) {
                return;
            }
            Deque<Request> deque = this.mInitQueue;
            Request poll = deque != null ? deque.poll() : null;
            if (poll == null) {
                if (this.mInitInProgress) {
                    this.mInitQueue = null;
                    this.mInitInProgress = false;
                    onDeviceReady();
                }
                poll = this.mTaskQueue.poll();
                if (poll == null) {
                    return;
                }
            }
            this.mOperationInProgress = true;
            switch (AnonymousClass4.$SwitchMap$com$secuyou$android_v22_pin_app$BleManager$Request$Type[poll.type.ordinal()]) {
                case 1:
                    internalCreateBond = BleManager.this.internalCreateBond();
                    break;
                case 2:
                    internalCreateBond = BleManager.this.internalReadCharacteristic(poll.characteristic);
                    break;
                case 3:
                    BluetoothGattCharacteristic bluetoothGattCharacteristic = poll.characteristic;
                    bluetoothGattCharacteristic.setValue(poll.value);
                    internalCreateBond = BleManager.this.internalWriteCharacteristic(bluetoothGattCharacteristic);
                    break;
                case 4:
                    internalCreateBond = BleManager.this.internalReadDescriptor(poll.descriptor);
                    break;
                case 5:
                    BluetoothGattDescriptor bluetoothGattDescriptor = poll.descriptor;
                    bluetoothGattDescriptor.setValue(poll.value);
                    internalCreateBond = BleManager.this.internalWriteDescriptor(bluetoothGattDescriptor);
                    break;
                case 6:
                    internalCreateBond = BleManager.this.internalEnableNotifications(poll.characteristic);
                    break;
                case 7:
                    internalCreateBond = BleManager.this.internalEnableIndications(poll.characteristic);
                    break;
                case 8:
                    internalCreateBond = BleManager.this.internalReadLockstatus();
                    break;
                case 9:
                    internalCreateBond = BleManager.this.internalReadLockState();
                    break;
                case 10:
                    internalCreateBond = BleManager.this.internalReadLockName();
                    break;
                case 11:
                    internalCreateBond = BleManager.this.internalSetLockstatusNotifications(true);
                    break;
                case 12:
                    internalCreateBond = BleManager.this.internalSetLockstatusNotifications(false);
                    break;
                case 13:
                    internalCreateBond = BleManager.this.internalSetLockStateNotifications(true);
                    break;
                case 14:
                    internalCreateBond = BleManager.this.internalSetLockStateNotifications(false);
                    break;
                case 15:
                    internalCreateBond = BleManager.this.internalSetLockStateNotifications(true);
                    break;
                case 16:
                    internalCreateBond = BleManager.this.internalSetLockStateNotifications(false);
                    break;
                case 17:
                    internalCreateBond = BleManager.this.ensureServiceChangedEnabled();
                    break;
                default:
                    internalCreateBond = false;
                    break;
            }
            if (internalCreateBond) {
                return;
            }
            this.mOperationInProgress = false;
            nextRequest();
        }

        private boolean isServiceChangedCCCD(BluetoothGattDescriptor bluetoothGattDescriptor) {
            if (bluetoothGattDescriptor == null) {
                return false;
            }
            return BleManager.KEY_CHARACTERISTIC.equals(bluetoothGattDescriptor.getCharacteristic().getUuid());
        }

        private boolean isLockstatusCharacteristic(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
            if (bluetoothGattCharacteristic == null) {
                return false;
            }
            return BleManager.KEY_CHARACTERISTIC.equals(bluetoothGattCharacteristic.getUuid());
        }

        private boolean isLockStateCharacteristic(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
            if (bluetoothGattCharacteristic == null) {
                return false;
            }
            return BleManager.STATE_CHARACTERISTIC.equals(bluetoothGattCharacteristic.getUuid());
        }

        private boolean isLockConfirmCharacteristic(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
            if (bluetoothGattCharacteristic == null) {
                return false;
            }
            return BleManager.CONFIRM_CHARACTERISTIC.equals(bluetoothGattCharacteristic.getUuid());
        }

        private boolean isLockNameCharacteristic(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
            if (bluetoothGattCharacteristic == null) {
                return false;
            }
            return BleManager.NAME_CHARACTERISTIC.equals(bluetoothGattCharacteristic.getUuid());
        }

        private boolean isModelCharacteristic(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
            if (bluetoothGattCharacteristic == null) {
                return false;
            }
            return BleManager.MODEL_CHARACTERISTIC.equals(bluetoothGattCharacteristic.getUuid());
        }

        private boolean isSerialCharacteristic(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
            if (bluetoothGattCharacteristic == null) {
                return false;
            }
            return BleManager.SERIAL_CHARACTERISTIC.equals(bluetoothGattCharacteristic.getUuid());
        }

        private boolean isFirmwareCharacteristic(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
            if (bluetoothGattCharacteristic == null) {
                return false;
            }
            return BleManager.FIRMWARE_CHARACTERISTIC.equals(bluetoothGattCharacteristic.getUuid());
        }

        private boolean isKeyCharacteristicCCCD(BluetoothGattDescriptor bluetoothGattDescriptor) {
            if (bluetoothGattDescriptor == null) {
                return false;
            }
            return BleManager.KEY_CHARACTERISTIC.equals(bluetoothGattDescriptor.getCharacteristic().getUuid());
        }

        private boolean isCCCD(BluetoothGattDescriptor bluetoothGattDescriptor) {
            if (bluetoothGattDescriptor == null) {
                return false;
            }
            return BleManager.CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID.equals(bluetoothGattDescriptor.getUuid());
        }
    }

    protected String getWriteType(int i) {
        return i != 1 ? i != 2 ? i != 4 ? "UNKNOWN: " + i : "WRITE SIGNED" : "WRITE REQUEST" : "WRITE COMMAND";
    }

    private void keep_connection_alive() {
        this.mConfirmCharacteristic.setValue(this.byte_cmd);
        writeCharacteristic(this.mConfirmCharacteristic);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.secuyou.android_v22_pin_app.BleManager$4  reason: invalid class name */
    /* loaded from: classes.dex */
    public static /* synthetic */ class AnonymousClass4 {
        static final /* synthetic */ int[] $SwitchMap$com$secuyou$android_v22_pin_app$BleLockState$STATE_DEVICE;
        static final /* synthetic */ int[] $SwitchMap$com$secuyou$android_v22_pin_app$BleManager$Request$Type;

        static {
            int[] iArr = new int[BleLockState.STATE_DEVICE.values().length];
            $SwitchMap$com$secuyou$android_v22_pin_app$BleLockState$STATE_DEVICE = iArr;
            try {
                iArr[BleLockState.STATE_DEVICE.KEY_GENERATION.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$secuyou$android_v22_pin_app$BleLockState$STATE_DEVICE[BleLockState.STATE_DEVICE.KEY_CONFIRMATION.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$secuyou$android_v22_pin_app$BleLockState$STATE_DEVICE[BleLockState.STATE_DEVICE.KEY_CHECKING.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$secuyou$android_v22_pin_app$BleLockState$STATE_DEVICE[BleLockState.STATE_DEVICE.KEY_BLOCKING.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            int[] iArr2 = new int[Request.Type.values().length];
            $SwitchMap$com$secuyou$android_v22_pin_app$BleManager$Request$Type = iArr2;
            try {
                iArr2[Request.Type.CREATE_BOND.ordinal()] = 1;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                $SwitchMap$com$secuyou$android_v22_pin_app$BleManager$Request$Type[Request.Type.READ.ordinal()] = 2;
            } catch (NoSuchFieldError unused6) {
            }
            try {
                $SwitchMap$com$secuyou$android_v22_pin_app$BleManager$Request$Type[Request.Type.WRITE.ordinal()] = 3;
            } catch (NoSuchFieldError unused7) {
            }
            try {
                $SwitchMap$com$secuyou$android_v22_pin_app$BleManager$Request$Type[Request.Type.READ_DESCRIPTOR.ordinal()] = 4;
            } catch (NoSuchFieldError unused8) {
            }
            try {
                $SwitchMap$com$secuyou$android_v22_pin_app$BleManager$Request$Type[Request.Type.WRITE_DESCRIPTOR.ordinal()] = 5;
            } catch (NoSuchFieldError unused9) {
            }
            try {
                $SwitchMap$com$secuyou$android_v22_pin_app$BleManager$Request$Type[Request.Type.ENABLE_NOTIFICATIONS.ordinal()] = 6;
            } catch (NoSuchFieldError unused10) {
            }
            try {
                $SwitchMap$com$secuyou$android_v22_pin_app$BleManager$Request$Type[Request.Type.ENABLE_INDICATIONS.ordinal()] = 7;
            } catch (NoSuchFieldError unused11) {
            }
            try {
                $SwitchMap$com$secuyou$android_v22_pin_app$BleManager$Request$Type[Request.Type.READ_LOCKSTATUS.ordinal()] = 8;
            } catch (NoSuchFieldError unused12) {
            }
            try {
                $SwitchMap$com$secuyou$android_v22_pin_app$BleManager$Request$Type[Request.Type.READ_LOCKSTATE.ordinal()] = 9;
            } catch (NoSuchFieldError unused13) {
            }
            try {
                $SwitchMap$com$secuyou$android_v22_pin_app$BleManager$Request$Type[Request.Type.READ_LOCKNAME.ordinal()] = 10;
            } catch (NoSuchFieldError unused14) {
            }
            try {
                $SwitchMap$com$secuyou$android_v22_pin_app$BleManager$Request$Type[Request.Type.ENABLE_LOCKSTATUS_NOTIFICATIONS.ordinal()] = 11;
            } catch (NoSuchFieldError unused15) {
            }
            try {
                $SwitchMap$com$secuyou$android_v22_pin_app$BleManager$Request$Type[Request.Type.DISABLE_LOCKSTATUS_NOTIFICATIONS.ordinal()] = 12;
            } catch (NoSuchFieldError unused16) {
            }
            try {
                $SwitchMap$com$secuyou$android_v22_pin_app$BleManager$Request$Type[Request.Type.ENABLE_LOCKSTATE_NOTIFICATIONS.ordinal()] = 13;
            } catch (NoSuchFieldError unused17) {
            }
            try {
                $SwitchMap$com$secuyou$android_v22_pin_app$BleManager$Request$Type[Request.Type.DISABLE_LOCKSTATE_NOTIFICATIONS.ordinal()] = 14;
            } catch (NoSuchFieldError unused18) {
            }
            try {
                $SwitchMap$com$secuyou$android_v22_pin_app$BleManager$Request$Type[Request.Type.ENABLE_LOCKNAME_NOTIFICATIONS.ordinal()] = 15;
            } catch (NoSuchFieldError unused19) {
            }
            try {
                $SwitchMap$com$secuyou$android_v22_pin_app$BleManager$Request$Type[Request.Type.DISABLE_LOCKNAME_NOTIFICATIONS.ordinal()] = 16;
            } catch (NoSuchFieldError unused20) {
            }
            try {
                $SwitchMap$com$secuyou$android_v22_pin_app$BleManager$Request$Type[Request.Type.ENABLE_SERVICE_CHANGED_INDICATIONS.ordinal()] = 17;
            } catch (NoSuchFieldError unused21) {
            }
        }
    }

    public void state_machine_lock() {
        int i = AnonymousClass4.$SwitchMap$com$secuyou$android_v22_pin_app$BleLockState$STATE_DEVICE[this.mLock.getBleLockState().mState.ordinal()];
        if (i == 1) {
            if (!this.mConnected) {
                connect(this.mBluetoothDevice);
            } else if (KEY_CHARACTERISTIC == null && (KEY_SERVICE == null || this.mBluetoothDevice == null)) {
            } else {
                if (this.mLock.getAppState() == BleLock.APP_STATE.SERVICES_DISCOVERED) {
                    this.mConfirmCharacteristic.setValue(this.byte_cmd_send_code);
                    writeCharacteristic(this.mConfirmCharacteristic);
                    this.mLock.setAppState(BleLock.APP_STATE.PROXIMITY_TAG_CODE_REQUEST);
                } else if (this.mLock.getAppState() == BleLock.APP_STATE.PROXIMITY_TAG_CODE_REQUEST) {
                    this.mLock.randomCode = this.mLockStatus;
                    this.mConfirmCharacteristic.setValue(this.byte_cmd);
                    writeCharacteristic(this.mConfirmCharacteristic);
                    this.mLock.setAppState(BleLock.APP_STATE.PROXIMITY_TAG_CODE_ACK);
                }
            }
        } else if (i == 2) {
            if (this.mLock.getAppState() == BleLock.APP_STATE.PROXIMITY_TAG_CODE_ACK) {
                this.mLock.setAppState(BleLock.APP_STATE.PROXIMITY_TAG_READY_TO_PASS_PINCODE);
                if (this.mLock.HasPincode) {
                    this.mKeyCharacteristic.setValue(PIN_ByteToByte());
                    writeCharacteristic(this.mKeyCharacteristic);
                    this.mLock.setAppState(BleLock.APP_STATE.PROXIMITY_TAG_PINCODE_SEND);
                }
            }
        } else if (i == 3 && this.mLock.getAppState() == BleLock.APP_STATE.PROXIMITY_TAG_CODE_ACK) {
            this.mLock.setAppState(BleLock.APP_STATE.PROXIMITY_TAG_READY_TO_PASS_PINCODE);
            if (this.mLock.HasPincode) {
                if (KEY_CHARACTERISTIC != null || (KEY_SERVICE != null && this.mBluetoothDevice != null)) {
                    this.mKeyCharacteristic.setValue(PIN_fromStringtoByte(this.mLock));
                    writeCharacteristic(this.mKeyCharacteristic);
                }
            } else {
                this.mKeyCharacteristic.setValue(PIN_ByteToByte());
            }
            this.mLock.setAppState(BleLock.APP_STATE.PROXIMITY_TAG_PINCODE_SEND);
        }
    }

    public byte[] PIN_ByteToByte() {
        byte[] bArr = new byte[16];
        for (int i = 0; i < 16; i++) {
            bArr[i] = 0;
        }
        bArr[0] = this.mLock.pincode0;
        bArr[1] = this.mLock.pincode1;
        bArr[2] = this.mLock.pincode2;
        bArr[3] = this.mLock.pincode3;
        bArr[4] = this.mLock.pincode4;
        for (int i2 = 0; i2 < 16; i2++) {
            bArr[i2] = (byte) (bArr[i2] + this.mLock.randomCode[i2]);
        }
        return aes_encrypt_pin.encrypt(bArr);
    }

    public byte[] PIN_fromStringtoByte(BleLock bleLock) {
        byte[] bArr = new byte[16];
        String pincode = bleLock.getPincode();
        for (int i = 0; i < 16; i++) {
            bArr[i] = 0;
        }
        for (int i2 = 0; i2 < 5; i2++) {
            switch (pincode.charAt(i2)) {
                case '0':
                    bArr[i2] = 0;
                    break;
                case '1':
                    bArr[i2] = 1;
                    break;
                case '2':
                    bArr[i2] = 2;
                    break;
                case '3':
                    bArr[i2] = 3;
                    break;
                case '4':
                    bArr[i2] = 4;
                    break;
                case '5':
                    bArr[i2] = 5;
                    break;
                case '6':
                    bArr[i2] = 6;
                    break;
                case '7':
                    bArr[i2] = 7;
                    break;
                case '8':
                    bArr[i2] = 8;
                    break;
                case '9':
                    bArr[i2] = 9;
                    break;
            }
        }
        for (int i3 = 0; i3 < 16; i3++) {
            bArr[i3] = (byte) (bArr[i3] + bleLock.randomCode[i3]);
        }
        return aes_encrypt_pin.encrypt(bArr);
    }

    public boolean checkPINcode_feedback_correct(byte[] bArr) {
        return Byte.valueOf(bArr[1]).intValue() == 16;
    }

    public void writeLockCmd(byte[] bArr) {
        if (isConnected()) {
            if (this.mConfirmCharacteristic != null && this.mLock.getBleLockState().mState == BleLockState.STATE_DEVICE.KEY_CONFIRMATION) {
                this.mConfirmCharacteristic.setValue(bArr);
                Log.d(TAG, "State lock:  " + this.mLock.getBleLockState().mState);
                writeCharacteristic(this.mConfirmCharacteristic);
                return;
            }
            Log.d(TAG, "writeLockCmd: ");
            Log.d(TAG, "Lock command Characteristic is not found");
        }
    }
}
