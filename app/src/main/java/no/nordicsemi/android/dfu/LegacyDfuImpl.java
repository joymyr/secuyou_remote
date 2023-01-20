package no.nordicsemi.android.dfu;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;

import java.util.UUID;

import no.nordicsemi.android.dfu.internal.exception.DeviceDisconnectedException;
import no.nordicsemi.android.dfu.internal.exception.DfuException;
import no.nordicsemi.android.dfu.internal.exception.UnknownResponseException;
import no.nordicsemi.android.dfu.internal.exception.UploadAbortedException;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class LegacyDfuImpl extends BaseCustomDfuImpl {
    static final UUID DEFAULT_DFU_CONTROL_POINT_UUID;
    static final UUID DEFAULT_DFU_PACKET_UUID;
    static final UUID DEFAULT_DFU_SERVICE_UUID;
    static final UUID DEFAULT_DFU_VERSION_UUID;
    static UUID DFU_CONTROL_POINT_UUID = null;
    static UUID DFU_PACKET_UUID = null;
    static UUID DFU_SERVICE_UUID = null;
    private static final int DFU_STATUS_SUCCESS = 1;
    static UUID DFU_VERSION_UUID = null;
    private static final byte[] OP_CODE_ACTIVATE_AND_RESET;
    private static final int OP_CODE_ACTIVATE_AND_RESET_KEY = 5;
    private static final byte[] OP_CODE_INIT_DFU_PARAMS;
    private static final byte[] OP_CODE_INIT_DFU_PARAMS_COMPLETE;
    private static final int OP_CODE_INIT_DFU_PARAMS_KEY = 2;
    private static final byte[] OP_CODE_INIT_DFU_PARAMS_START;
    private static final int OP_CODE_PACKET_RECEIPT_NOTIF_KEY = 17;
    private static final byte[] OP_CODE_PACKET_RECEIPT_NOTIF_REQ;
    private static final int OP_CODE_PACKET_RECEIPT_NOTIF_REQ_KEY = 8;
    private static final byte[] OP_CODE_RECEIVE_FIRMWARE_IMAGE;
    private static final int OP_CODE_RECEIVE_FIRMWARE_IMAGE_KEY = 3;
    private static final byte[] OP_CODE_RESET;
    private static final int OP_CODE_RESET_KEY = 6;
    private static final int OP_CODE_RESPONSE_CODE_KEY = 16;
    private static final byte[] OP_CODE_START_DFU;
    private static final int OP_CODE_START_DFU_KEY = 1;
    private static final byte[] OP_CODE_START_DFU_V1;
    private static final byte[] OP_CODE_VALIDATE;
    private static final int OP_CODE_VALIDATE_KEY = 4;
    private final LegacyBluetoothCallback mBluetoothCallback;
    private BluetoothGattCharacteristic mControlPointCharacteristic;
    private boolean mImageSizeInProgress;
    private BluetoothGattCharacteristic mPacketCharacteristic;

    static {
        UUID uuid = new UUID(23296205844446L, 1523193452336828707L);
        DEFAULT_DFU_SERVICE_UUID = uuid;
        UUID uuid2 = new UUID(23300500811742L, 1523193452336828707L);
        DEFAULT_DFU_CONTROL_POINT_UUID = uuid2;
        UUID uuid3 = new UUID(23304795779038L, 1523193452336828707L);
        DEFAULT_DFU_PACKET_UUID = uuid3;
        UUID uuid4 = new UUID(23313385713630L, 1523193452336828707L);
        DEFAULT_DFU_VERSION_UUID = uuid4;
        DFU_SERVICE_UUID = uuid;
        DFU_CONTROL_POINT_UUID = uuid2;
        DFU_PACKET_UUID = uuid3;
        DFU_VERSION_UUID = uuid4;
        OP_CODE_START_DFU = new byte[]{1, 0};
        OP_CODE_START_DFU_V1 = new byte[]{1};
        OP_CODE_INIT_DFU_PARAMS = new byte[]{2};
        OP_CODE_INIT_DFU_PARAMS_START = new byte[]{2, 0};
        OP_CODE_INIT_DFU_PARAMS_COMPLETE = new byte[]{2, 1};
        OP_CODE_RECEIVE_FIRMWARE_IMAGE = new byte[]{3};
        OP_CODE_VALIDATE = new byte[]{4};
        OP_CODE_ACTIVATE_AND_RESET = new byte[]{5};
        OP_CODE_RESET = new byte[]{6};
        OP_CODE_PACKET_RECEIPT_NOTIF_REQ = new byte[]{8, 0, 0};
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* loaded from: classes.dex */
    public class LegacyBluetoothCallback extends BaseCustomBluetoothCallback {
        protected LegacyBluetoothCallback() {
            super();
        }

        @Override // no.nordicsemi.android.dfu.BaseCustomDfuImpl.BaseCustomBluetoothCallback
        protected void onPacketCharacteristicWrite(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, int i) {
            if (LegacyDfuImpl.this.mImageSizeInProgress) {
                LegacyDfuImpl.this.mService.sendLogBroadcast(5, "Data written to " + bluetoothGattCharacteristic.getUuid() + ", value (0x): " + parse(bluetoothGattCharacteristic));
                LegacyDfuImpl.this.mImageSizeInProgress = false;
            }
        }

        @Override // android.bluetooth.BluetoothGattCallback
        public void onCharacteristicChanged(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
            if (bluetoothGattCharacteristic.getIntValue(17, 0).intValue() == 17) {
                LegacyDfuImpl.this.mProgressInfo.setBytesReceived(bluetoothGattCharacteristic.getIntValue(20, 1).intValue());
                handlePacketReceiptNotification(bluetoothGatt, bluetoothGattCharacteristic);
            } else if (!LegacyDfuImpl.this.mRemoteErrorOccurred) {
                if (bluetoothGattCharacteristic.getIntValue(17, 2).intValue() != 1) {
                    LegacyDfuImpl.this.mRemoteErrorOccurred = true;
                }
                handleNotification(bluetoothGatt, bluetoothGattCharacteristic);
            }
            LegacyDfuImpl.this.notifyLock();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public LegacyDfuImpl(Intent intent, DfuBaseService dfuBaseService) {
        super(intent, dfuBaseService);
        this.mBluetoothCallback = new LegacyBluetoothCallback();
    }

    @Override // no.nordicsemi.android.dfu.DfuService
    public boolean isClientCompatible(Intent intent, BluetoothGatt bluetoothGatt) {
        BluetoothGattCharacteristic characteristic;
        BluetoothGattService service = bluetoothGatt.getService(DFU_SERVICE_UUID);
        if (service == null || (characteristic = service.getCharacteristic(DFU_CONTROL_POINT_UUID)) == null || characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG) == null) {
            return false;
        }
        this.mControlPointCharacteristic = characteristic;
        BluetoothGattCharacteristic characteristic2 = service.getCharacteristic(DFU_PACKET_UUID);
        this.mPacketCharacteristic = characteristic2;
        return characteristic2 != null;
    }

    @Override // no.nordicsemi.android.dfu.DfuCallback
    public BaseCustomBluetoothCallback getGattCallback() {
        return this.mBluetoothCallback;
    }

    @Override // no.nordicsemi.android.dfu.BaseCustomDfuImpl
    protected UUID getControlPointCharacteristicUUID() {
        return DFU_CONTROL_POINT_UUID;
    }

    @Override // no.nordicsemi.android.dfu.BaseCustomDfuImpl
    protected UUID getPacketCharacteristicUUID() {
        return DFU_PACKET_UUID;
    }

    @Override // no.nordicsemi.android.dfu.BaseCustomDfuImpl
    protected UUID getDfuServiceUUID() {
        return DFU_SERVICE_UUID;
    }

    /* JADX WARN: Removed duplicated region for block: B:105:0x0405 A[Catch: UnknownResponseException -> 0x01fc, UploadAbortedException -> 0x0204, RemoteDfuException -> 0x06d2, TryCatch #2 {RemoteDfuException -> 0x06d2, blocks: (B:92:0x0348, B:96:0x0352, B:98:0x03f9, B:103:0x0401, B:105:0x0405, B:107:0x0410, B:109:0x0486, B:112:0x04ba, B:113:0x04c1, B:108:0x0456, B:115:0x04c4, B:117:0x04c8, B:123:0x04d6, B:124:0x051a, B:125:0x0539, B:126:0x054c, B:128:0x05b6, B:130:0x067e, B:134:0x06ad, B:135:0x06b2, B:136:0x06b9, B:137:0x06ba, B:138:0x06c1, B:140:0x06c3, B:141:0x06c9, B:121:0x04d2, B:142:0x06ca, B:143:0x06cf, B:144:0x06d0, B:145:0x06d1), top: B:165:0x0348 }] */
    /* JADX WARN: Removed duplicated region for block: B:123:0x04d6 A[Catch: UnknownResponseException -> 0x01fc, UploadAbortedException -> 0x0204, RemoteDfuException -> 0x06d2, TryCatch #2 {RemoteDfuException -> 0x06d2, blocks: (B:92:0x0348, B:96:0x0352, B:98:0x03f9, B:103:0x0401, B:105:0x0405, B:107:0x0410, B:109:0x0486, B:112:0x04ba, B:113:0x04c1, B:108:0x0456, B:115:0x04c4, B:117:0x04c8, B:123:0x04d6, B:124:0x051a, B:125:0x0539, B:126:0x054c, B:128:0x05b6, B:130:0x067e, B:134:0x06ad, B:135:0x06b2, B:136:0x06b9, B:137:0x06ba, B:138:0x06c1, B:140:0x06c3, B:141:0x06c9, B:121:0x04d2, B:142:0x06ca, B:143:0x06cf, B:144:0x06d0, B:145:0x06d1), top: B:165:0x0348 }] */
    /* JADX WARN: Removed duplicated region for block: B:128:0x05b6 A[Catch: UnknownResponseException -> 0x01fc, UploadAbortedException -> 0x0204, RemoteDfuException -> 0x06d2, TryCatch #2 {RemoteDfuException -> 0x06d2, blocks: (B:92:0x0348, B:96:0x0352, B:98:0x03f9, B:103:0x0401, B:105:0x0405, B:107:0x0410, B:109:0x0486, B:112:0x04ba, B:113:0x04c1, B:108:0x0456, B:115:0x04c4, B:117:0x04c8, B:123:0x04d6, B:124:0x051a, B:125:0x0539, B:126:0x054c, B:128:0x05b6, B:130:0x067e, B:134:0x06ad, B:135:0x06b2, B:136:0x06b9, B:137:0x06ba, B:138:0x06c1, B:140:0x06c3, B:141:0x06c9, B:121:0x04d2, B:142:0x06ca, B:143:0x06cf, B:144:0x06d0, B:145:0x06d1), top: B:165:0x0348 }] */
    /* JADX WARN: Removed duplicated region for block: B:137:0x06ba A[Catch: UnknownResponseException -> 0x01fc, UploadAbortedException -> 0x0204, RemoteDfuException -> 0x06d2, TryCatch #2 {RemoteDfuException -> 0x06d2, blocks: (B:92:0x0348, B:96:0x0352, B:98:0x03f9, B:103:0x0401, B:105:0x0405, B:107:0x0410, B:109:0x0486, B:112:0x04ba, B:113:0x04c1, B:108:0x0456, B:115:0x04c4, B:117:0x04c8, B:123:0x04d6, B:124:0x051a, B:125:0x0539, B:126:0x054c, B:128:0x05b6, B:130:0x067e, B:134:0x06ad, B:135:0x06b2, B:136:0x06b9, B:137:0x06ba, B:138:0x06c1, B:140:0x06c3, B:141:0x06c9, B:121:0x04d2, B:142:0x06ca, B:143:0x06cf, B:144:0x06d0, B:145:0x06d1), top: B:165:0x0348 }] */
    /* JADX WARN: Removed duplicated region for block: B:76:0x022d  */
    /* JADX WARN: Removed duplicated region for block: B:89:0x0345 A[Catch: UnknownResponseException -> 0x01fc, UploadAbortedException -> 0x0204, RemoteDfuException -> 0x0346, TRY_LEAVE, TryCatch #11 {RemoteDfuException -> 0x0346, blocks: (B:74:0x0226, B:77:0x022f, B:79:0x0233, B:81:0x0332, B:86:0x033e, B:87:0x0343, B:88:0x0344, B:89:0x0345), top: B:170:0x0226 }] */
    @Override // no.nordicsemi.android.dfu.DfuService
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public void performDfu(Intent r28) throws DfuException, DeviceDisconnectedException, UploadAbortedException {
        /*
            Method dump skipped, instructions count: 1932
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: no.nordicsemi.android.dfu.LegacyDfuImpl.performDfu(android.content.Intent):void");
    }

    private void setNumberOfPackets(byte[] bArr, int i) {
        bArr[1] = (byte) (i & 255);
        bArr[2] = (byte) ((i >> 8) & 255);
    }

    private int getStatusCode(byte[] bArr, int i) throws UnknownResponseException {
        if (bArr == null || bArr.length != 3 || bArr[0] != 16 || bArr[1] != i || bArr[2] < 1 || bArr[2] > 6) {
            throw new UnknownResponseException("Invalid response received", bArr, 16, i);
        }
        return bArr[2];
    }

    private int readVersion(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        if (bluetoothGattCharacteristic != null) {
            return bluetoothGattCharacteristic.getIntValue(18, 0).intValue();
        }
        return 0;
    }

    private void writeOpCode(BluetoothGattCharacteristic bluetoothGattCharacteristic, byte[] bArr) throws DeviceDisconnectedException, DfuException, UploadAbortedException {
        boolean z = false;
        writeOpCode(bluetoothGattCharacteristic, bArr, (bArr[0] == 6 || bArr[0] == 5) ? true : true);
    }

    private void writeImageSize(BluetoothGattCharacteristic bluetoothGattCharacteristic, int i) throws DeviceDisconnectedException, DfuException, UploadAbortedException {
        this.mReceivedData = null;
        this.mError = 0;
        this.mImageSizeInProgress = true;
        bluetoothGattCharacteristic.setWriteType(1);
        bluetoothGattCharacteristic.setValue(new byte[4]);
        bluetoothGattCharacteristic.setValue(i, 20, 0);
        this.mService.sendLogBroadcast(1, "Writing to characteristic " + bluetoothGattCharacteristic.getUuid());
        this.mService.sendLogBroadcast(0, "gatt.writeCharacteristic(" + bluetoothGattCharacteristic.getUuid() + ")");
        this.mGatt.writeCharacteristic(bluetoothGattCharacteristic);
        try {
            synchronized (this.mLock) {
                while (true) {
                    if ((!this.mImageSizeInProgress || !this.mConnected || this.mError != 0 || this.mAborted) && !this.mPaused) {
                        break;
                    }
                    this.mLock.wait();
                }
            }
        } catch (InterruptedException e) {
            loge("Sleeping interrupted", e);
        }
        if (this.mAborted) {
            throw new UploadAbortedException();
        }
        if (!this.mConnected) {
            throw new DeviceDisconnectedException("Unable to write Image Size: device disconnected");
        }
        if (this.mError != 0) {
            throw new DfuException("Unable to write Image Size", this.mError);
        }
    }

    private void writeImageSize(BluetoothGattCharacteristic bluetoothGattCharacteristic, int i, int i2, int i3) throws DeviceDisconnectedException, DfuException, UploadAbortedException {
        this.mReceivedData = null;
        this.mError = 0;
        this.mImageSizeInProgress = true;
        bluetoothGattCharacteristic.setWriteType(1);
        bluetoothGattCharacteristic.setValue(new byte[12]);
        bluetoothGattCharacteristic.setValue(i, 20, 0);
        bluetoothGattCharacteristic.setValue(i2, 20, 4);
        bluetoothGattCharacteristic.setValue(i3, 20, 8);
        this.mService.sendLogBroadcast(1, "Writing to characteristic " + bluetoothGattCharacteristic.getUuid());
        this.mService.sendLogBroadcast(0, "gatt.writeCharacteristic(" + bluetoothGattCharacteristic.getUuid() + ")");
        this.mGatt.writeCharacteristic(bluetoothGattCharacteristic);
        try {
            synchronized (this.mLock) {
                while (true) {
                    if ((!this.mImageSizeInProgress || !this.mConnected || this.mError != 0 || this.mAborted) && !this.mPaused) {
                        break;
                    }
                    this.mLock.wait();
                }
            }
        } catch (InterruptedException e) {
            loge("Sleeping interrupted", e);
        }
        if (this.mAborted) {
            throw new UploadAbortedException();
        }
        if (!this.mConnected) {
            throw new DeviceDisconnectedException("Unable to write Image Sizes: device disconnected");
        }
        if (this.mError != 0) {
            throw new DfuException("Unable to write Image Sizes", this.mError);
        }
    }

    private void resetAndRestart(BluetoothGatt bluetoothGatt, Intent intent) throws DfuException, DeviceDisconnectedException, UploadAbortedException {
        this.mService.sendLogBroadcast(15, "Last upload interrupted. Restarting device...");
        this.mProgressInfo.setProgress(-5);
        logi("Sending Reset command (Op Code = 6)");
        writeOpCode(this.mControlPointCharacteristic, OP_CODE_RESET);
        this.mService.sendLogBroadcast(10, "Reset request sent");
        this.mService.waitUntilDisconnected();
        this.mService.sendLogBroadcast(5, "Disconnected by the remote device");
        BluetoothGattService service = bluetoothGatt.getService(GENERIC_ATTRIBUTE_SERVICE_UUID);
        this.mService.refreshDeviceCache(bluetoothGatt, !((service == null || service.getCharacteristic(SERVICE_CHANGED_UUID) == null) ? false : true));
        this.mService.close(bluetoothGatt);
        logi("Restarting the service");
        Intent intent2 = new Intent();
        intent2.fillIn(intent, 24);
        restartService(intent2, false);
    }
}
