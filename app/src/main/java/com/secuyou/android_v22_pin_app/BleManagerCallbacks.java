package com.secuyou.android_v22_pin_app;

import android.bluetooth.BluetoothDevice;
/* loaded from: classes.dex */
public interface BleManagerCallbacks {
    void onBonded(BluetoothDevice bluetoothDevice);

    void onBondingRequired(BluetoothDevice bluetoothDevice);

    void onDeviceConnected(BluetoothDevice bluetoothDevice);

    void onDeviceConnecting(BluetoothDevice bluetoothDevice);

    void onDeviceDisconnected(BluetoothDevice bluetoothDevice);

    void onDeviceDisconnecting(BluetoothDevice bluetoothDevice);

    void onDeviceNotSupported(BluetoothDevice bluetoothDevice);

    void onDeviceReady(BluetoothDevice bluetoothDevice);

    void onError(BluetoothDevice bluetoothDevice, String str, int i);

    void onLinkLossOccur(BluetoothDevice bluetoothDevice);

    void onLockFirmwareValueReceived(BluetoothDevice bluetoothDevice, byte[] bArr);

    void onLockModelValueReceived(BluetoothDevice bluetoothDevice, byte[] bArr);

    void onLockNameValueReceived(BluetoothDevice bluetoothDevice, byte[] bArr);

    void onLockSerialValueReceived(BluetoothDevice bluetoothDevice, byte[] bArr);

    void onLockStateValueReceived(BluetoothDevice bluetoothDevice, byte[] bArr);

    void onLockstatusValueReceived(BluetoothDevice bluetoothDevice, byte[] bArr);

    void onServicesDiscovered(BluetoothDevice bluetoothDevice, boolean z);

    boolean shouldEnableBatteryLevelNotifications(BluetoothDevice bluetoothDevice);

    boolean shouldEnableLockNameNotifications(BluetoothDevice bluetoothDevice);

    boolean shouldEnableLockStateNotifications(BluetoothDevice bluetoothDevice);

    boolean shouldEnableLockstatusNotifications(BluetoothDevice bluetoothDevice);
}
