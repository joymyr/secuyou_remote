package com.secuyou.android_v22_pin_app;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.io.UnsupportedEncodingException;

/* loaded from: classes.dex */
public class BleLock {
    public boolean HasPincode;
    public String fw_version;
    public String hw_version;
    public APP_STATE mAppState;
    private BleLockState mBleLockState;
    private BluetoothDevice mDevice;
    public LOCK_TYPE mLockType;
    public String name;
    private String pincode;
    public byte pincode0;
    public byte pincode1;
    public byte pincode2;
    public byte pincode3;
    public byte pincode4;
    public byte[] randomCode;
    public String serial_no_batch;

    /* loaded from: classes.dex */
    public enum APP_STATE {
        SERVICES_DISCOVERED,
        PROXIMITY_TAG_CODE_REQUEST,
        PROXIMITY_TAG_CODE_ACK,
        PROXIMITY_TAG_READY_TO_PASS_PINCODE,
        PROXIMITY_TAG_PINCODE_SEND,
        UNKNOWN
    }

    /* loaded from: classes.dex */
    public enum LOCK_TYPE {
        HANDLE_LOCK(0),
        UNKN_LOCK_TYPE(2);

        private int value;

        LOCK_TYPE(int i) {
            this.value = i;
        }

        public int getValue() {
            return this.value;
        }
    }

    /* loaded from: classes.dex */
    public enum LOCK_VERSION {
        HARDWARE_15(0),
        HARDWARE_21(1),
        HARDWARE_22(2),
        UNKNOWN(3);

        private int value;

        LOCK_VERSION(int i) {
            this.value = i;
        }

        public int getValue() {
            return this.value;
        }
    }

    public void setLockHardwareVersion(byte[] bArr) {
        byte b = bArr[11];
        if (bArr == null) {
        }
    }

    public void setLockFirmwareVersion(String str) {
        this.fw_version = str;
    }

    public LOCK_TYPE getLockType() {
        return this.mLockType;
    }

    public String getLockVersion() {
        return this.hw_version;
    }

    public BleLock(BluetoothDevice bluetoothDevice) {
        this.mAppState = APP_STATE.UNKNOWN;
        this.randomCode = new byte[16];
        this.name = "PINlock";
        this.mLockType = LOCK_TYPE.UNKN_LOCK_TYPE;
        this.mDevice = bluetoothDevice;
        BleLockState bleLockState = new BleLockState();
        this.mBleLockState = bleLockState;
        bleLockState.mState = BleLockState.STATE_DEVICE.KEY_GENERATION;
        this.mAppState = APP_STATE.UNKNOWN;
        this.HasPincode = false;
        this.mLockType = LOCK_TYPE.UNKN_LOCK_TYPE;
    }

    public BleLock(BluetoothDevice bluetoothDevice, byte[] bArr) {
        this.mAppState = APP_STATE.UNKNOWN;
        this.randomCode = new byte[16];
        this.name = "PINlock";
        this.mLockType = LOCK_TYPE.UNKN_LOCK_TYPE;
        this.mDevice = bluetoothDevice;
        BleLockState bleLockState = new BleLockState(bArr);
        this.mBleLockState = bleLockState;
        bleLockState.mState = BleLockState.STATE_DEVICE.KEY_GENERATION;
    }

    public BluetoothDevice getmDevice() {
        return this.mDevice;
    }

    public void setmDevice(BluetoothDevice bluetoothDevice) {
        this.mDevice = bluetoothDevice;
    }

    public void setName(byte[] bArr) {
        try {
            this.name = new String(bArr, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void setNameStringValue(String str) {
        this.name = str;
    }

    public void setModelStringValue(String str) {
        this.hw_version = str;
    }

    public void setSerialStringValue(String str) {
        this.serial_no_batch = str;
    }

    public String getName() {
        return this.name;
    }

    public byte[] getRandomCode() {
        return this.randomCode;
    }

    public void setPincode(String str) {
        this.pincode = str;
        this.HasPincode = true;
    }

    public String getPincode() {
        return this.pincode;
    }

    public void savePincode(String str) {
        this.pincode = str;
        this.HasPincode = true;
    }

    public boolean hasPincode() {
        return this.HasPincode;
    }

    public BleLockState getBleLockState() {
        return this.mBleLockState;
    }

    public void setAppState(APP_STATE app_state) {
        this.mAppState = app_state;
        Log.d("ContentValues", "Connection state: " + app_state);
    }

    public APP_STATE getAppState() {
        return this.mAppState;
    }

    public String toString() {
        return this.mDevice.getName() + " | " + this.mDevice.getAddress() + " | " + this.mBleLockState.toString();
    }
}
