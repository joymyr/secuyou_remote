package com.secuyou.android_v22_pin_app;

import android.util.Log;
/* loaded from: classes.dex */
public class BleLockState {
    private static final int LOCK_POSITION_BIT = 1;
    private static final int LOCK_STATE_BIT = 0;
    private static final int LOCK_STATE_BYTE_0 = 0;
    private static final int LOCK_STATE_BYTE_1 = 1;
    private static final int LOCK_STATE_BYTE_2 = 2;
    private static final int LOCK_STATE_BYTE_3 = 3;
    private static final int LOCK_STATE_BYTE_4 = 4;
    private static final String TAG = "PINcode";
    public boolean HOME_LOCK;
    public float LOCK_FIRMWARE_VERSION;
    public boolean PIN_CODE_CORRECT;
    public boolean RESCUE_HPR_STATE;
    public BATTERY_STATUS mBatteryStatus;
    public HANDLE_STATE mHandleState;
    public LOCKING_MECHANISM_POSITION mLockPosition;
    private LOCK_STATE mLockState;
    public STATE_DEVICE mState;

    /* loaded from: classes.dex */
    public enum LOCKING_MECHANISM_POSITION {
        UNLOCKED,
        LOCKED,
        LOCK_UNLOCK_IN_PROGRESS,
        UNKNOWN_POSITION
    }

    /* loaded from: classes.dex */
    public enum LOCK_ORIENTATION {
        BOTTOM,
        TOP,
        LEFT,
        RIGHT,
        UNKNOWN_ORIENTATION
    }

    private boolean testBitVal(byte b, int i) {
        return (b & (1 << i)) != 0;
    }

    /* loaded from: classes.dex */
    public enum STATE_DEVICE {
        KEY_GENERATION(0),
        KEY_CONFIRMATION(1),
        KEY_CHECKING(2),
        KEY_BLOCKING(3);
        
        private int value;

        STATE_DEVICE(int i) {
            this.value = i;
        }

        public int getValue() {
            return this.value;
        }
    }

    /* loaded from: classes.dex */
    public enum LOCK_STATE {
        UNARMED(0),
        ARMED(1),
        UNKNOWN_STATE(2);
        
        private final int value;

        LOCK_STATE(int i) {
            this.value = i;
        }

        public int getValue() {
            return this.value;
        }
    }

    /* loaded from: classes.dex */
    public enum BATTERY_STATUS {
        BATTERY_GOOD(0),
        BATTERY_LOW(1),
        BATTERY_CRITICAL(2),
        BATTERY_EMPTY(3),
        BATTERY_UNKNOWN(4);
        
        private int value;

        BATTERY_STATUS(int i) {
            this.value = i;
        }

        public int getValue() {
            return this.value;
        }

        public String getStringValue() {
            int i = this.value;
            return i != 1 ? i != 2 ? i != 3 ? "Good" : "Empty" : "Critical" : "Low";
        }
    }

    /* loaded from: classes.dex */
    public enum HANDLE_STATE {
        FCHC(0),
        FCHO(1),
        FOHC(2),
        FOHO(3),
        FOHU_KIP(4),
        FOHO_TBT(5),
        UNKNOWN_HANDLE_STATE(6);
        
        private int value;

        HANDLE_STATE(int i) {
            this.value = i;
        }

        public int getValue() {
            return this.value;
        }

        public String getStringValue() {
            int i = this.value;
            return i != 0 ? i != 1 ? no.nordicsemi.android.dfu.BuildConfig.FLAVOR : "Open" : "Closed";
        }
    }

    public BleLockState() {
        this.mLockState = LOCK_STATE.UNKNOWN_STATE;
        this.mState = STATE_DEVICE.KEY_GENERATION;
        this.mLockPosition = LOCKING_MECHANISM_POSITION.UNKNOWN_POSITION;
        this.mHandleState = HANDLE_STATE.UNKNOWN_HANDLE_STATE;
        this.mBatteryStatus = BATTERY_STATUS.BATTERY_UNKNOWN;
        this.PIN_CODE_CORRECT = false;
        this.HOME_LOCK = false;
        this.RESCUE_HPR_STATE = false;
        this.mLockState = LOCK_STATE.UNKNOWN_STATE;
        this.mState = STATE_DEVICE.KEY_GENERATION;
        this.mLockPosition = LOCKING_MECHANISM_POSITION.UNKNOWN_POSITION;
        this.mBatteryStatus = BATTERY_STATUS.BATTERY_UNKNOWN;
        this.mHandleState = HANDLE_STATE.UNKNOWN_HANDLE_STATE;
        this.HOME_LOCK = false;
    }

    public BleLockState(byte[] bArr) {
        this.mLockState = LOCK_STATE.UNKNOWN_STATE;
        this.mState = STATE_DEVICE.KEY_GENERATION;
        this.mLockPosition = LOCKING_MECHANISM_POSITION.UNKNOWN_POSITION;
        this.mHandleState = HANDLE_STATE.UNKNOWN_HANDLE_STATE;
        this.mBatteryStatus = BATTERY_STATUS.BATTERY_UNKNOWN;
        this.PIN_CODE_CORRECT = false;
        this.HOME_LOCK = false;
        this.RESCUE_HPR_STATE = false;
        setLockState(bArr);
    }

    public void setLockFirmwareVersion(byte[] bArr) {
        float f = bArr[1];
        this.LOCK_FIRMWARE_VERSION = f;
        this.LOCK_FIRMWARE_VERSION = f / 10.0f;
    }

    public void setLockState(byte[] bArr) {
        byte b = bArr[0];
        if (b == 0) {
            this.mState = STATE_DEVICE.KEY_GENERATION;
        } else if (b == 1) {
            this.mState = STATE_DEVICE.KEY_CONFIRMATION;
        } else if (b == 2) {
            this.mState = STATE_DEVICE.KEY_CHECKING;
        } else if (b == 3) {
            this.mState = STATE_DEVICE.KEY_BLOCKING;
        }
        Log.e(TAG, "STATE lock: " + this.mState.getValue());
    }

    public void readLockState(byte[] bArr) {
        if (bArr[0] == 0) {
            this.mLockPosition = LOCKING_MECHANISM_POSITION.UNLOCKED;
        } else {
            this.mLockPosition = LOCKING_MECHANISM_POSITION.LOCKED;
        }
    }

    void readBatteryState(byte[] bArr) {
        byte b = bArr[2];
        if (b == BATTERY_STATUS.BATTERY_GOOD.getValue()) {
            this.mBatteryStatus = BATTERY_STATUS.BATTERY_GOOD;
        } else if (b == BATTERY_STATUS.BATTERY_LOW.getValue()) {
            this.mBatteryStatus = BATTERY_STATUS.BATTERY_LOW;
        } else if (b == BATTERY_STATUS.BATTERY_CRITICAL.getValue()) {
            this.mBatteryStatus = BATTERY_STATUS.BATTERY_CRITICAL;
        } else if (b == BATTERY_STATUS.BATTERY_EMPTY.getValue()) {
            this.mBatteryStatus = BATTERY_STATUS.BATTERY_EMPTY;
        } else {
            this.mBatteryStatus = BATTERY_STATUS.BATTERY_UNKNOWN;
        }
    }

    void readHandlePositionState(byte[] bArr) {
        byte b = bArr[3];
        if (b == 0 || b == 1) {
            this.mHandleState = HANDLE_STATE.FCHC;
        } else {
            this.mHandleState = HANDLE_STATE.FCHO;
        }
    }

    public void setLockStatus(byte[] bArr) {
        byte b = bArr[0];
        if (b == 0) {
            this.mLockPosition = LOCKING_MECHANISM_POSITION.UNLOCKED;
        } else if (b == 1) {
            this.mLockPosition = LOCKING_MECHANISM_POSITION.LOCKED;
        } else {
            this.mLockPosition = LOCKING_MECHANISM_POSITION.UNKNOWN_POSITION;
        }
        if (bArr[1] == 16) {
            this.PIN_CODE_CORRECT = true;
        } else {
            this.PIN_CODE_CORRECT = false;
        }
        byte b2 = bArr[4];
        if (b2 == 0) {
            this.HOME_LOCK = false;
            this.RESCUE_HPR_STATE = false;
        } else if (b2 == 1) {
            this.HOME_LOCK = true;
            this.RESCUE_HPR_STATE = false;
        } else if (b2 == 2) {
            this.HOME_LOCK = false;
            this.RESCUE_HPR_STATE = true;
        } else if (b2 == 3) {
            this.HOME_LOCK = true;
            this.RESCUE_HPR_STATE = true;
        }
        readBatteryState(bArr);
        readHandlePositionState(bArr);
    }

    public LOCK_STATE getLockState() {
        return this.mLockState;
    }

    public LOCKING_MECHANISM_POSITION getmLockingMechanismPosition() {
        return this.mLockPosition;
    }
}
