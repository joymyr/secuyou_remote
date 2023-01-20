package no.nordicsemi.android.dfu;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;

import java.util.Locale;

import no.nordicsemi.android.dfu.internal.exception.DeviceDisconnectedException;
import no.nordicsemi.android.dfu.internal.exception.DfuException;
import no.nordicsemi.android.dfu.internal.exception.RemoteDfuException;
import no.nordicsemi.android.dfu.internal.exception.UnknownResponseException;
import no.nordicsemi.android.dfu.internal.exception.UploadAbortedException;
import no.nordicsemi.android.error.SecureDfuError;
/* loaded from: classes.dex */
abstract class ButtonlessDfuImpl extends BaseButtonlessDfuImpl {
    private static final int DFU_STATUS_SUCCESS = 1;
    private static final byte[] OP_CODE_ENTER_BOOTLOADER = {1};
    private static final int OP_CODE_ENTER_BOOTLOADER_KEY = 1;
    private static final int OP_CODE_RESPONSE_CODE_KEY = 32;

    protected abstract BluetoothGattCharacteristic getButtonlessDfuCharacteristic();

    protected abstract int getResponseType();

    protected abstract boolean shouldScanForBootloader();

    /* JADX INFO: Access modifiers changed from: package-private */
    public ButtonlessDfuImpl(Intent intent, DfuBaseService dfuBaseService) {
        super(intent, dfuBaseService);
    }

    @Override // no.nordicsemi.android.dfu.DfuService
    public void performDfu(Intent intent) throws DfuException, DeviceDisconnectedException, UploadAbortedException {
        byte[] bArr;
        this.mProgressInfo.setProgress(-2);
        this.mService.waitFor(1000L);
        BluetoothGatt bluetoothGatt = this.mGatt;
        this.mService.sendLogBroadcast(15, "Application with buttonless update found");
        this.mService.sendLogBroadcast(1, "Jumping to the DFU Bootloader...");
        BluetoothGattCharacteristic buttonlessDfuCharacteristic = getButtonlessDfuCharacteristic();
        int responseType = getResponseType();
        enableCCCD(buttonlessDfuCharacteristic, getResponseType());
        this.mService.sendLogBroadcast(10, (responseType == 2 ? "Indications" : "Notifications") + " enabled");
        this.mService.waitFor(1000L);
        try {
            this.mProgressInfo.setProgress(-3);
            logi("Sending Enter Bootloader (Op Code = 1)");
            writeOpCode(buttonlessDfuCharacteristic, OP_CODE_ENTER_BOOTLOADER, true);
            this.mService.sendLogBroadcast(10, "Enter bootloader sent (Op Code = 1)");
            try {
                bArr = readNotificationResponse();
            } catch (DeviceDisconnectedException unused) {
                bArr = this.mReceivedData;
            }
            if (bArr != null) {
                int statusCode = getStatusCode(bArr, 1);
                logi("Response received (Op Code = " + ((int) bArr[1]) + ", Status = " + statusCode + ")");
                this.mService.sendLogBroadcast(10, "Response received (Op Code = " + ((int) bArr[1]) + ", Status = " + statusCode + ")");
                if (statusCode != 1) {
                    throw new RemoteDfuException("Device returned error after sending Enter Bootloader", statusCode);
                }
                this.mService.waitUntilDisconnected();
            } else {
                logi("Device disconnected before receiving notification");
            }
            this.mService.sendLogBroadcast(5, "Disconnected by the remote device");
            finalize(intent, false, shouldScanForBootloader());
        } catch (RemoteDfuException e) {
            int errorNumber = e.getErrorNumber() | 2048;
            loge(e.getMessage());
            this.mService.sendLogBroadcast(20, String.format(Locale.US, "Remote DFU error: %s", SecureDfuError.parseButtonlessError(errorNumber)));
            this.mService.terminateConnection(bluetoothGatt, errorNumber | 8192);
        } catch (UnknownResponseException e2) {
            loge(e2.getMessage());
            this.mService.sendLogBroadcast(20, e2.getMessage());
            this.mService.terminateConnection(bluetoothGatt, DfuBaseService.ERROR_INVALID_RESPONSE);
        }
    }

    private int getStatusCode(byte[] bArr, int i) throws UnknownResponseException {
        if (bArr == null || bArr.length < 3 || bArr[0] != 32 || bArr[1] != i || (bArr[2] != 1 && bArr[2] != 2 && bArr[2] != 4)) {
            throw new UnknownResponseException("Invalid response received", bArr, 32, i);
        }
        return bArr[2];
    }
}
