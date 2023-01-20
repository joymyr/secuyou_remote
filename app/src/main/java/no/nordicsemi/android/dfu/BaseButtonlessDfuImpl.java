package no.nordicsemi.android.dfu;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;

/* loaded from: classes.dex */
abstract class BaseButtonlessDfuImpl extends BaseDfuImpl {
    private final ButtonlessBluetoothCallback mBluetoothCallback;

    /* JADX INFO: Access modifiers changed from: protected */
    /* loaded from: classes.dex */
    public class ButtonlessBluetoothCallback extends BaseBluetoothGattCallback {
        protected ButtonlessBluetoothCallback() {
            super();
        }

        @Override // android.bluetooth.BluetoothGattCallback
        public void onCharacteristicChanged(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
            BaseButtonlessDfuImpl.this.mService.sendLogBroadcast(5, "Notification received from " + bluetoothGattCharacteristic.getUuid() + ", value (0x): " + parse(bluetoothGattCharacteristic));
            BaseButtonlessDfuImpl.this.mReceivedData = bluetoothGattCharacteristic.getValue();
            BaseButtonlessDfuImpl.this.notifyLock();
        }

        @Override // android.bluetooth.BluetoothGattCallback
        public void onCharacteristicWrite(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, int i) {
            BaseButtonlessDfuImpl.this.mRequestCompleted = true;
            BaseButtonlessDfuImpl.this.notifyLock();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public BaseButtonlessDfuImpl(Intent intent, DfuBaseService dfuBaseService) {
        super(intent, dfuBaseService);
        this.mBluetoothCallback = new ButtonlessBluetoothCallback();
    }

    @Override // no.nordicsemi.android.dfu.DfuCallback
    public BaseBluetoothGattCallback getGattCallback() {
        return this.mBluetoothCallback;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void finalize(Intent intent, boolean z, boolean z2) {
        boolean booleanExtra = intent.getBooleanExtra(DfuBaseService.EXTRA_KEEP_BOND, false);
        this.mService.refreshDeviceCache(this.mGatt, z || !booleanExtra);
        this.mService.close(this.mGatt);
        if (this.mGatt.getDevice().getBondState() == 12 && (intent.getBooleanExtra(DfuBaseService.EXTRA_RESTORE_BOND, false) || !booleanExtra)) {
            removeBond();
            this.mService.waitFor(2000L);
        }
        logi("Restarting to bootloader mode");
        Intent intent2 = new Intent();
        intent2.fillIn(intent, 24);
        restartService(intent2, z2);
    }
}
