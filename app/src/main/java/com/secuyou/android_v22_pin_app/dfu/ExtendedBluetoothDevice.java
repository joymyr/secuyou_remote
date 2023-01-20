package com.secuyou.android_v22_pin_app.dfu;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
/* loaded from: classes.dex */
public class ExtendedBluetoothDevice {
    static final int NO_RSSI = -1000;
    public final BluetoothDevice device;
    public boolean isBonded;
    public String name;
    public int rssi;

    public ExtendedBluetoothDevice(ScanResult scanResult) {
        this.device = scanResult.getDevice();
        this.name = scanResult.getScanRecord() != null ? scanResult.getScanRecord().getDeviceName() : null;
        this.rssi = scanResult.getRssi();
        this.isBonded = false;
    }

    public ExtendedBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.device = bluetoothDevice;
        this.name = bluetoothDevice.getName();
        this.rssi = -1000;
        this.isBonded = true;
    }

    public boolean matches(ScanResult scanResult) {
        return this.device.getAddress().equals(scanResult.getDevice().getAddress());
    }
}
