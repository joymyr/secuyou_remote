package no.nordicsemi.android.dfu;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;

import java.io.InputStream;
import java.util.Locale;
import java.util.UUID;

import no.nordicsemi.android.dfu.internal.ArchiveInputStream;
import no.nordicsemi.android.dfu.internal.exception.DeviceDisconnectedException;
import no.nordicsemi.android.dfu.internal.exception.DfuException;
import no.nordicsemi.android.dfu.internal.exception.RemoteDfuException;
import no.nordicsemi.android.dfu.internal.exception.RemoteDfuExtendedErrorException;
import no.nordicsemi.android.dfu.internal.exception.UnknownResponseException;
import no.nordicsemi.android.dfu.internal.exception.UploadAbortedException;
import no.nordicsemi.android.error.SecureDfuError;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class SecureDfuImpl extends BaseCustomDfuImpl {
    static final UUID DEFAULT_DFU_CONTROL_POINT_UUID;
    static final UUID DEFAULT_DFU_PACKET_UUID;
    static final UUID DEFAULT_DFU_SERVICE_UUID;
    static UUID DFU_CONTROL_POINT_UUID = null;
    static UUID DFU_PACKET_UUID = null;
    static UUID DFU_SERVICE_UUID = null;
    private static final int DFU_STATUS_SUCCESS = 1;
    private static final int MAX_ATTEMPTS = 3;
    private static final int OBJECT_COMMAND = 1;
    private static final int OBJECT_DATA = 2;
    private static final byte[] OP_CODE_CALCULATE_CHECKSUM;
    private static final int OP_CODE_CALCULATE_CHECKSUM_KEY = 3;
    private static final byte[] OP_CODE_CREATE_COMMAND;
    private static final byte[] OP_CODE_CREATE_DATA;
    private static final int OP_CODE_CREATE_KEY = 1;
    private static final byte[] OP_CODE_EXECUTE;
    private static final int OP_CODE_EXECUTE_KEY = 4;
    private static final byte[] OP_CODE_PACKET_RECEIPT_NOTIF_REQ;
    private static final int OP_CODE_PACKET_RECEIPT_NOTIF_REQ_KEY = 2;
    private static final int OP_CODE_RESPONSE_CODE_KEY = 96;
    private static final byte[] OP_CODE_SELECT_OBJECT;
    private static final int OP_CODE_SELECT_OBJECT_KEY = 6;
    private final SecureBluetoothCallback mBluetoothCallback;
    private BluetoothGattCharacteristic mControlPointCharacteristic;
    private BluetoothGattCharacteristic mPacketCharacteristic;

    static {
        UUID uuid = new UUID(279658205548544L, -9223371485494954757L);
        DEFAULT_DFU_SERVICE_UUID = uuid;
        UUID uuid2 = new UUID(-8157989241631715488L, -6937650605005804976L);
        DEFAULT_DFU_CONTROL_POINT_UUID = uuid2;
        UUID uuid3 = new UUID(-8157989237336748192L, -6937650605005804976L);
        DEFAULT_DFU_PACKET_UUID = uuid3;
        DFU_SERVICE_UUID = uuid;
        DFU_CONTROL_POINT_UUID = uuid2;
        DFU_PACKET_UUID = uuid3;
        OP_CODE_CREATE_COMMAND = new byte[]{1, 1, 0, 0, 0, 0};
        OP_CODE_CREATE_DATA = new byte[]{1, 2, 0, 0, 0, 0};
        OP_CODE_PACKET_RECEIPT_NOTIF_REQ = new byte[]{2, 0, 0};
        OP_CODE_CALCULATE_CHECKSUM = new byte[]{3};
        OP_CODE_EXECUTE = new byte[]{4};
        OP_CODE_SELECT_OBJECT = new byte[]{6, 0};
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* loaded from: classes.dex */
    public class SecureBluetoothCallback extends BaseCustomBluetoothCallback {
        protected SecureBluetoothCallback() {
            super();
        }

        @Override // android.bluetooth.BluetoothGattCallback
        public void onCharacteristicChanged(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
            if (bluetoothGattCharacteristic.getValue() == null || bluetoothGattCharacteristic.getValue().length < 3) {
                SecureDfuImpl.this.loge("Empty response: " + parse(bluetoothGattCharacteristic));
                SecureDfuImpl.this.mError = DfuBaseService.ERROR_INVALID_RESPONSE;
                SecureDfuImpl.this.notifyLock();
                return;
            }
            if (bluetoothGattCharacteristic.getIntValue(17, 0).intValue() == 96) {
                if (bluetoothGattCharacteristic.getIntValue(17, 1).intValue() == 3) {
                    int intValue = bluetoothGattCharacteristic.getIntValue(20, 3).intValue();
                    if (((int) (((ArchiveInputStream) SecureDfuImpl.this.mFirmwareStream).getCrc32() & 4294967295L)) == bluetoothGattCharacteristic.getIntValue(20, 7).intValue()) {
                        SecureDfuImpl.this.mProgressInfo.setBytesReceived(intValue);
                    } else if (SecureDfuImpl.this.mFirmwareUploadInProgress) {
                        SecureDfuImpl.this.mFirmwareUploadInProgress = false;
                        SecureDfuImpl.this.notifyLock();
                        return;
                    }
                    handlePacketReceiptNotification(bluetoothGatt, bluetoothGattCharacteristic);
                } else if (!SecureDfuImpl.this.mRemoteErrorOccurred) {
                    if (bluetoothGattCharacteristic.getIntValue(17, 2).intValue() != 1) {
                        SecureDfuImpl.this.mRemoteErrorOccurred = true;
                    }
                    handleNotification(bluetoothGatt, bluetoothGattCharacteristic);
                }
            } else {
                SecureDfuImpl.this.loge("Invalid response: " + parse(bluetoothGattCharacteristic));
                SecureDfuImpl.this.mError = DfuBaseService.ERROR_INVALID_RESPONSE;
            }
            SecureDfuImpl.this.notifyLock();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public SecureDfuImpl(Intent intent, DfuBaseService dfuBaseService) {
        super(intent, dfuBaseService);
        this.mBluetoothCallback = new SecureBluetoothCallback();
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

    @Override // no.nordicsemi.android.dfu.BaseDfuImpl, no.nordicsemi.android.dfu.DfuService
    public boolean initialize(Intent intent, BluetoothGatt bluetoothGatt, int i, InputStream inputStream, InputStream inputStream2) throws DfuException, DeviceDisconnectedException, UploadAbortedException {
        if (inputStream2 == null) {
            this.mService.sendLogBroadcast(20, "The Init packet is required by this version DFU Bootloader");
            this.mService.terminateConnection(bluetoothGatt, DfuBaseService.ERROR_INIT_PACKET_REQUIRED);
            return false;
        }
        return super.initialize(intent, bluetoothGatt, i, inputStream, inputStream2);
    }

    @Override // no.nordicsemi.android.dfu.DfuCallback
    public BaseBluetoothGattCallback getGattCallback() {
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

    /* JADX WARN: Can't wrap try/catch for region: R(9:7|8|(6:13|(1:15)|16|17|18|19)|27|(0)|16|17|18|19) */
    /* JADX WARN: Code restructure failed: missing block: B:19:0x0075, code lost:
        r0 = move-exception;
     */
    /* JADX WARN: Code restructure failed: missing block: B:21:0x007c, code lost:
        if (r10.mProgressInfo.isLastPart() == false) goto L25;
     */
    /* JADX WARN: Code restructure failed: missing block: B:22:0x007e, code lost:
        r10.mRemoteErrorOccurred = false;
        logw("Sending SD+BL failed. Trying to send App only");
        r10.mService.sendLogBroadcast(15, "Invalid system components. Trying to send application");
        r10.mFileType = 4;
        r0 = (no.nordicsemi.android.dfu.internal.ArchiveInputStream) r10.mFirmwareStream;
        r0.setContentType(r10.mFileType);
        r2 = r0.getApplicationInit();
        r10.mInitPacketStream = new java.io.ByteArrayInputStream(r2);
        r10.mInitPacketSizeInBytes = r2.length;
        r10.mImageSizeInBytes = r0.applicationImageSize();
        r10.mProgressInfo.init(r10.mImageSizeInBytes, 2, 2);
        sendInitPacket(r1, false);
     */
    /* JADX WARN: Code restructure failed: missing block: B:24:0x00d4, code lost:
        throw r0;
     */
    /* JADX WARN: Removed duplicated region for block: B:16:0x006c A[Catch: RemoteDfuException -> 0x00d5, UnknownResponseException -> 0x0179, UploadAbortedException -> 0x0192, TRY_LEAVE, TryCatch #1 {RemoteDfuException -> 0x00d5, blocks: (B:8:0x0047, B:10:0x0060, B:16:0x006c, B:20:0x0076, B:22:0x007e, B:24:0x00d4, B:23:0x00b9), top: B:37:0x0047 }] */
    @Override // no.nordicsemi.android.dfu.DfuService
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public void performDfu(Intent r11) throws DfuException, DeviceDisconnectedException, UploadAbortedException {
        /*
            Method dump skipped, instructions count: 404
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: no.nordicsemi.android.dfu.SecureDfuImpl.performDfu(android.content.Intent):void");
    }

    /* JADX WARN: Removed duplicated region for block: B:34:0x0124  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private void sendInitPacket(BluetoothGatt r18, boolean r19) throws RemoteDfuException, DeviceDisconnectedException, DfuException, UploadAbortedException, UnknownResponseException {
        /*
            Method dump skipped, instructions count: 643
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: no.nordicsemi.android.dfu.SecureDfuImpl.sendInitPacket(android.bluetooth.BluetoothGatt, boolean):void");
    }

    /* JADX WARN: Code restructure failed: missing block: B:48:0x0334, code lost:
        if (r28.mPacketsBeforeNotification > 1) goto L29;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private void sendFirmware(BluetoothGatt r29) throws RemoteDfuException, DeviceDisconnectedException, DfuException, UploadAbortedException, UnknownResponseException {
        /*
            Method dump skipped, instructions count: 1166
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: no.nordicsemi.android.dfu.SecureDfuImpl.sendFirmware(android.bluetooth.BluetoothGatt):void");
    }

    private int getStatusCode(byte[] bArr, int i) throws UnknownResponseException {
        if (bArr == null || bArr.length < 3 || bArr[0] != 96 || bArr[1] != i || (bArr[2] != 1 && bArr[2] != 2 && bArr[2] != 3 && bArr[2] != 4 && bArr[2] != 5 && bArr[2] != 7 && bArr[2] != 8 && bArr[2] != 10 && bArr[2] != 11)) {
            throw new UnknownResponseException("Invalid response received", bArr, 96, i);
        }
        return bArr[2];
    }

    private void setNumberOfPackets(byte[] bArr, int i) {
        bArr[1] = (byte) (i & 255);
        bArr[2] = (byte) ((i >> 8) & 255);
    }

    private void setObjectSize(byte[] bArr, int i) {
        bArr[2] = (byte) (i & 255);
        bArr[3] = (byte) ((i >> 8) & 255);
        bArr[4] = (byte) ((i >> 16) & 255);
        bArr[5] = (byte) ((i >> 24) & 255);
    }

    private void setPacketReceiptNotifications(int i) throws DfuException, DeviceDisconnectedException, UploadAbortedException, UnknownResponseException, RemoteDfuException {
        if (!this.mConnected) {
            throw new DeviceDisconnectedException("Unable to read Checksum: device disconnected");
        }
        logi("Sending the number of packets before notifications (Op Code = 2, Value = " + i + ")");
        byte[] bArr = OP_CODE_PACKET_RECEIPT_NOTIF_REQ;
        setNumberOfPackets(bArr, i);
        writeOpCode(this.mControlPointCharacteristic, bArr);
        byte[] readNotificationResponse = readNotificationResponse();
        int statusCode = getStatusCode(readNotificationResponse, 2);
        if (statusCode == 11) {
            throw new RemoteDfuExtendedErrorException("Sending the number of packets failed", readNotificationResponse[3]);
        }
        if (statusCode != 1) {
            throw new RemoteDfuException("Sending the number of packets failed", statusCode);
        }
    }

    private void writeOpCode(BluetoothGattCharacteristic bluetoothGattCharacteristic, byte[] bArr) throws DeviceDisconnectedException, DfuException, UploadAbortedException {
        writeOpCode(bluetoothGattCharacteristic, bArr, false);
    }

    private void writeCreateRequest(int i, int i2) throws DeviceDisconnectedException, DfuException, UploadAbortedException, RemoteDfuException, UnknownResponseException {
        if (!this.mConnected) {
            throw new DeviceDisconnectedException("Unable to create object: device disconnected");
        }
        byte[] bArr = i == 1 ? OP_CODE_CREATE_COMMAND : OP_CODE_CREATE_DATA;
        setObjectSize(bArr, i2);
        writeOpCode(this.mControlPointCharacteristic, bArr);
        byte[] readNotificationResponse = readNotificationResponse();
        int statusCode = getStatusCode(readNotificationResponse, 1);
        if (statusCode == 11) {
            throw new RemoteDfuExtendedErrorException("Creating Command object failed", readNotificationResponse[3]);
        }
        if (statusCode != 1) {
            throw new RemoteDfuException("Creating Command object failed", statusCode);
        }
    }

    private ObjectInfo selectObject(int i) throws DeviceDisconnectedException, DfuException, UploadAbortedException, RemoteDfuException, UnknownResponseException {
        if (!this.mConnected) {
            throw new DeviceDisconnectedException("Unable to read object info: device disconnected");
        }
        byte[] bArr = OP_CODE_SELECT_OBJECT;
        bArr[1] = (byte) i;
        writeOpCode(this.mControlPointCharacteristic, bArr);
        byte[] readNotificationResponse = readNotificationResponse();
        int statusCode = getStatusCode(readNotificationResponse, 6);
        if (statusCode != 11) {
            if (statusCode != 1) {
                throw new RemoteDfuException("Selecting object failed", statusCode);
            }
            ObjectInfo objectInfo = new ObjectInfo();
            objectInfo.maxSize = this.mControlPointCharacteristic.getIntValue(20, 3).intValue();
            objectInfo.offset = this.mControlPointCharacteristic.getIntValue(20, 7).intValue();
            objectInfo.CRC32 = this.mControlPointCharacteristic.getIntValue(20, 11).intValue();
            return objectInfo;
        }
        throw new RemoteDfuExtendedErrorException("Selecting object failed", readNotificationResponse[3]);
    }

    private ObjectChecksum readChecksum() throws DeviceDisconnectedException, DfuException, UploadAbortedException, RemoteDfuException, UnknownResponseException {
        if (!this.mConnected) {
            throw new DeviceDisconnectedException("Unable to read Checksum: device disconnected");
        }
        writeOpCode(this.mControlPointCharacteristic, OP_CODE_CALCULATE_CHECKSUM);
        byte[] readNotificationResponse = readNotificationResponse();
        int statusCode = getStatusCode(readNotificationResponse, 3);
        if (statusCode != 11) {
            if (statusCode != 1) {
                throw new RemoteDfuException("Receiving Checksum failed", statusCode);
            }
            ObjectChecksum objectChecksum = new ObjectChecksum();
            objectChecksum.offset = this.mControlPointCharacteristic.getIntValue(20, 3).intValue();
            objectChecksum.CRC32 = this.mControlPointCharacteristic.getIntValue(20, 7).intValue();
            return objectChecksum;
        }
        throw new RemoteDfuExtendedErrorException("Receiving Checksum failed", readNotificationResponse[3]);
    }

    private void writeExecute() throws DfuException, DeviceDisconnectedException, UploadAbortedException, UnknownResponseException, RemoteDfuException {
        if (!this.mConnected) {
            throw new DeviceDisconnectedException("Unable to read Checksum: device disconnected");
        }
        writeOpCode(this.mControlPointCharacteristic, OP_CODE_EXECUTE);
        byte[] readNotificationResponse = readNotificationResponse();
        int statusCode = getStatusCode(readNotificationResponse, 4);
        if (statusCode == 11) {
            throw new RemoteDfuExtendedErrorException("Executing object failed", readNotificationResponse[3]);
        }
        if (statusCode != 1) {
            throw new RemoteDfuException("Executing object failed", statusCode);
        }
    }

    private void writeExecute(boolean z) throws DfuException, DeviceDisconnectedException, UploadAbortedException, UnknownResponseException, RemoteDfuException {
        try {
            writeExecute();
        } catch (RemoteDfuException e) {
            if (z && e.getErrorNumber() == 5) {
                logw(e.getMessage() + ": " + SecureDfuError.parse(517));
                if (this.mFileType == 1) {
                    logw("Are you sure your new SoftDevice is API compatible with the updated one? If not, update the bootloader as well");
                }
                this.mService.sendLogBroadcast(15, String.format(Locale.US, "Remote DFU error: %s. SD busy? Retrying...", SecureDfuError.parse(517)));
                logi("SD busy? Retrying...");
                logi("Executing data object (Op Code = 4)");
                writeExecute();
                return;
            }
            throw e;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class ObjectInfo extends ObjectChecksum {
        int maxSize;

        private ObjectInfo() {
            super();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class ObjectChecksum {
        int CRC32;
        int offset;

        private ObjectChecksum() {
        }
    }
}
