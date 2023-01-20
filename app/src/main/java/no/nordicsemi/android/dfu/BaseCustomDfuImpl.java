package no.nordicsemi.android.dfu;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import java.io.IOException;
import java.util.UUID;
import java.util.zip.CRC32;

import no.nordicsemi.android.dfu.internal.exception.DeviceDisconnectedException;
import no.nordicsemi.android.dfu.internal.exception.DfuException;
import no.nordicsemi.android.dfu.internal.exception.HexFileValidationException;
import no.nordicsemi.android.dfu.internal.exception.UploadAbortedException;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public abstract class BaseCustomDfuImpl extends BaseDfuImpl {
    boolean mFirmwareUploadInProgress;
    private boolean mInitPacketInProgress;
    int mPacketsBeforeNotification;
    private int mPacketsSentSinceNotification;
    boolean mRemoteErrorOccurred;

    protected abstract UUID getControlPointCharacteristicUUID();

    protected abstract UUID getDfuServiceUUID();

    protected abstract UUID getPacketCharacteristicUUID();

    static /* synthetic */ int access$108(BaseCustomDfuImpl baseCustomDfuImpl) {
        int i = baseCustomDfuImpl.mPacketsSentSinceNotification;
        baseCustomDfuImpl.mPacketsSentSinceNotification = i + 1;
        return i;
    }

    /* loaded from: classes.dex */
    class BaseCustomBluetoothCallback extends BaseBluetoothGattCallback {
        protected void onPacketCharacteristicWrite(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, int i) {
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public BaseCustomBluetoothCallback() {
            super();
        }

        @Override // android.bluetooth.BluetoothGattCallback
        public void onCharacteristicWrite(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, int i) {
            boolean z = true;
            if (i == 0) {
                if (bluetoothGattCharacteristic.getUuid().equals(BaseCustomDfuImpl.this.getPacketCharacteristicUUID())) {
                    if (BaseCustomDfuImpl.this.mInitPacketInProgress) {
                        BaseCustomDfuImpl.this.mService.sendLogBroadcast(5, "Data written to " + bluetoothGattCharacteristic.getUuid() + ", value (0x): " + parse(bluetoothGattCharacteristic));
                        BaseCustomDfuImpl.this.mInitPacketInProgress = false;
                    } else if (BaseCustomDfuImpl.this.mFirmwareUploadInProgress) {
                        BaseCustomDfuImpl.this.mProgressInfo.addBytesSent(bluetoothGattCharacteristic.getValue().length);
                        BaseCustomDfuImpl.access$108(BaseCustomDfuImpl.this);
                        if (BaseCustomDfuImpl.this.mPacketsBeforeNotification <= 0 || BaseCustomDfuImpl.this.mPacketsSentSinceNotification < BaseCustomDfuImpl.this.mPacketsBeforeNotification) {
                            z = false;
                        }
                        boolean isComplete = BaseCustomDfuImpl.this.mProgressInfo.isComplete();
                        boolean isObjectComplete = BaseCustomDfuImpl.this.mProgressInfo.isObjectComplete();
                        if (z) {
                            return;
                        }
                        if (isComplete || isObjectComplete) {
                            BaseCustomDfuImpl.this.mFirmwareUploadInProgress = false;
                            BaseCustomDfuImpl.this.notifyLock();
                            return;
                        }
                        try {
                            BaseCustomDfuImpl.this.waitIfPaused();
                            if (!BaseCustomDfuImpl.this.mAborted && BaseCustomDfuImpl.this.mError == 0 && !BaseCustomDfuImpl.this.mRemoteErrorOccurred && !BaseCustomDfuImpl.this.mResetRequestSent) {
                                int availableObjectSizeIsBytes = BaseCustomDfuImpl.this.mProgressInfo.getAvailableObjectSizeIsBytes();
                                byte[] bArr = BaseCustomDfuImpl.this.mBuffer;
                                if (availableObjectSizeIsBytes < bArr.length) {
                                    bArr = new byte[availableObjectSizeIsBytes];
                                }
                                BaseCustomDfuImpl.this.writePacket(bluetoothGatt, bluetoothGattCharacteristic, bArr, BaseCustomDfuImpl.this.mFirmwareStream.read(bArr));
                                return;
                            }
                            BaseCustomDfuImpl.this.mFirmwareUploadInProgress = false;
                            BaseCustomDfuImpl.this.mService.sendLogBroadcast(15, "Upload terminated");
                            BaseCustomDfuImpl.this.notifyLock();
                            return;
                        } catch (HexFileValidationException unused) {
                            BaseCustomDfuImpl.this.loge("Invalid HEX file");
                            BaseCustomDfuImpl.this.mError = 4099;
                        } catch (IOException e) {
                            BaseCustomDfuImpl.this.loge("Error while reading the input stream", e);
                            BaseCustomDfuImpl.this.mError = DfuBaseService.ERROR_FILE_IO_EXCEPTION;
                        }
                    } else {
                        onPacketCharacteristicWrite(bluetoothGatt, bluetoothGattCharacteristic, i);
                    }
                } else {
                    BaseCustomDfuImpl.this.mService.sendLogBroadcast(5, "Data written to " + bluetoothGattCharacteristic.getUuid() + ", value (0x): " + parse(bluetoothGattCharacteristic));
                    BaseCustomDfuImpl.this.mRequestCompleted = true;
                }
            } else if (BaseCustomDfuImpl.this.mResetRequestSent) {
                BaseCustomDfuImpl.this.mRequestCompleted = true;
            } else {
                BaseCustomDfuImpl.this.loge("Characteristic write error: " + i);
                BaseCustomDfuImpl.this.mError = i | 16384;
            }
            BaseCustomDfuImpl.this.notifyLock();
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public void handlePacketReceiptNotification(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
            if (!BaseCustomDfuImpl.this.mFirmwareUploadInProgress) {
                handleNotification(bluetoothGatt, bluetoothGattCharacteristic);
                return;
            }
            BluetoothGattCharacteristic characteristic = bluetoothGatt.getService(BaseCustomDfuImpl.this.getDfuServiceUUID()).getCharacteristic(BaseCustomDfuImpl.this.getPacketCharacteristicUUID());
            try {
                BaseCustomDfuImpl.this.mPacketsSentSinceNotification = 0;
                BaseCustomDfuImpl.this.waitIfPaused();
                if (!BaseCustomDfuImpl.this.mAborted && BaseCustomDfuImpl.this.mError == 0 && !BaseCustomDfuImpl.this.mRemoteErrorOccurred && !BaseCustomDfuImpl.this.mResetRequestSent) {
                    boolean isComplete = BaseCustomDfuImpl.this.mProgressInfo.isComplete();
                    boolean isObjectComplete = BaseCustomDfuImpl.this.mProgressInfo.isObjectComplete();
                    if (!isComplete && !isObjectComplete) {
                        int availableObjectSizeIsBytes = BaseCustomDfuImpl.this.mProgressInfo.getAvailableObjectSizeIsBytes();
                        byte[] bArr = BaseCustomDfuImpl.this.mBuffer;
                        if (availableObjectSizeIsBytes < bArr.length) {
                            bArr = new byte[availableObjectSizeIsBytes];
                        }
                        BaseCustomDfuImpl.this.writePacket(bluetoothGatt, characteristic, bArr, BaseCustomDfuImpl.this.mFirmwareStream.read(bArr));
                        return;
                    }
                    BaseCustomDfuImpl.this.mFirmwareUploadInProgress = false;
                    BaseCustomDfuImpl.this.notifyLock();
                    return;
                }
                BaseCustomDfuImpl.this.mFirmwareUploadInProgress = false;
                BaseCustomDfuImpl.this.mService.sendLogBroadcast(15, "Upload terminated");
            } catch (HexFileValidationException unused) {
                BaseCustomDfuImpl.this.loge("Invalid HEX file");
                BaseCustomDfuImpl.this.mError = 4099;
            } catch (IOException e) {
                BaseCustomDfuImpl.this.loge("Error while reading the input stream", e);
                BaseCustomDfuImpl.this.mError = DfuBaseService.ERROR_FILE_IO_EXCEPTION;
            }
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public void handleNotification(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
            BaseCustomDfuImpl.this.mService.sendLogBroadcast(5, "Notification received from " + bluetoothGattCharacteristic.getUuid() + ", value (0x): " + parse(bluetoothGattCharacteristic));
            BaseCustomDfuImpl.this.mReceivedData = bluetoothGattCharacteristic.getValue();
            BaseCustomDfuImpl.this.mFirmwareUploadInProgress = false;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public BaseCustomDfuImpl(Intent intent, DfuBaseService dfuBaseService) {
        super(intent, dfuBaseService);
        int i = 12;
        if (intent.hasExtra(DfuBaseService.EXTRA_PACKET_RECEIPT_NOTIFICATIONS_ENABLED)) {
            boolean booleanExtra = intent.getBooleanExtra(DfuBaseService.EXTRA_PACKET_RECEIPT_NOTIFICATIONS_ENABLED, Build.VERSION.SDK_INT < 23);
            int intExtra = intent.getIntExtra(DfuBaseService.EXTRA_PACKET_RECEIPT_NOTIFICATIONS_VALUE, 12);
            if (intExtra >= 0 && intExtra <= 65535) {
                i = intExtra;
            }
            this.mPacketsBeforeNotification = booleanExtra ? i : 0;
            return;
        }
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(dfuBaseService);
        boolean z = defaultSharedPreferences.getBoolean(DfuSettingsConstants.SETTINGS_PACKET_RECEIPT_NOTIFICATION_ENABLED, Build.VERSION.SDK_INT < 23);
        try {
            int parseInt = Integer.parseInt(defaultSharedPreferences.getString(DfuSettingsConstants.SETTINGS_NUMBER_OF_PACKETS, String.valueOf(12)));
            if (parseInt >= 0 && parseInt <= 65535) {
                i = parseInt;
            }
        } catch (NumberFormatException unused) {
        }
        this.mPacketsBeforeNotification = z ? i : 0;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void writeInitData(BluetoothGattCharacteristic bluetoothGattCharacteristic, CRC32 crc32) throws DfuException, DeviceDisconnectedException, UploadAbortedException {
        try {
            byte[] bArr = this.mBuffer;
            while (true) {
                int read = this.mInitPacketStream.read(bArr, 0, bArr.length);
                if (read == -1) {
                    return;
                }
                writeInitPacket(bluetoothGattCharacteristic, bArr, read);
                if (crc32 != null) {
                    crc32.update(bArr, 0, read);
                }
            }
        } catch (IOException e) {
            loge("Error while reading Init packet file", e);
            throw new DfuException("Error while reading Init packet file", 4098);
        }
    }

    private void writeInitPacket(BluetoothGattCharacteristic bluetoothGattCharacteristic, byte[] bArr, int i) throws DeviceDisconnectedException, DfuException, UploadAbortedException {
        if (this.mAborted) {
            throw new UploadAbortedException();
        }
        if (bArr.length != i) {
            byte[] bArr2 = new byte[i];
            System.arraycopy(bArr, 0, bArr2, 0, i);
            bArr = bArr2;
        }
        this.mReceivedData = null;
        this.mError = 0;
        this.mInitPacketInProgress = true;
        bluetoothGattCharacteristic.setWriteType(1);
        bluetoothGattCharacteristic.setValue(bArr);
        logi("Sending init packet (Value = " + parse(bArr) + ")");
        this.mService.sendLogBroadcast(1, "Writing to characteristic " + bluetoothGattCharacteristic.getUuid());
        this.mService.sendLogBroadcast(0, "gatt.writeCharacteristic(" + bluetoothGattCharacteristic.getUuid() + ")");
        this.mGatt.writeCharacteristic(bluetoothGattCharacteristic);
        try {
            synchronized (this.mLock) {
                while (true) {
                    if ((!this.mInitPacketInProgress || !this.mConnected || this.mError != 0) && !this.mPaused) {
                        break;
                    }
                    this.mLock.wait();
                }
            }
        } catch (InterruptedException e) {
            loge("Sleeping interrupted", e);
        }
        if (!this.mConnected) {
            throw new DeviceDisconnectedException("Unable to write Init DFU Parameters: device disconnected");
        }
        if (this.mError != 0) {
            throw new DfuException("Unable to write Init DFU Parameters", this.mError);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void uploadFirmwareImage(BluetoothGattCharacteristic bluetoothGattCharacteristic) throws DeviceDisconnectedException, DfuException, UploadAbortedException {
        if (this.mAborted) {
            throw new UploadAbortedException();
        }
        this.mReceivedData = null;
        this.mError = 0;
        this.mFirmwareUploadInProgress = true;
        this.mPacketsSentSinceNotification = 0;
        byte[] bArr = this.mBuffer;
        try {
            int read = this.mFirmwareStream.read(bArr);
            this.mService.sendLogBroadcast(1, "Sending firmware to characteristic " + bluetoothGattCharacteristic.getUuid() + "...");
            writePacket(this.mGatt, bluetoothGattCharacteristic, bArr, read);
            try {
                synchronized (this.mLock) {
                    while (true) {
                        if ((!this.mFirmwareUploadInProgress || this.mReceivedData != null || !this.mConnected || this.mError != 0) && !this.mPaused) {
                            break;
                        }
                        this.mLock.wait();
                    }
                }
            } catch (InterruptedException e) {
                loge("Sleeping interrupted", e);
            }
            if (!this.mConnected) {
                throw new DeviceDisconnectedException("Uploading Firmware Image failed: device disconnected");
            }
            if (this.mError != 0) {
                throw new DfuException("Uploading Firmware Image failed", this.mError);
            }
        } catch (HexFileValidationException unused) {
            throw new DfuException("HEX file not valid", 4099);
        } catch (IOException unused2) {
            throw new DfuException("Error while reading file", DfuBaseService.ERROR_FILE_IO_EXCEPTION);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void writePacket(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, byte[] bArr, int i) {
        if (i <= 0) {
            return;
        }
        if (bArr.length != i) {
            byte[] bArr2 = new byte[i];
            System.arraycopy(bArr, 0, bArr2, 0, i);
            bArr = bArr2;
        }
        bluetoothGattCharacteristic.setWriteType(1);
        bluetoothGattCharacteristic.setValue(bArr);
        bluetoothGatt.writeCharacteristic(bluetoothGattCharacteristic);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void finalize(Intent intent, boolean z) {
        boolean z2;
        boolean z3 = false;
        boolean booleanExtra = intent.getBooleanExtra(DfuBaseService.EXTRA_KEEP_BOND, false);
        this.mService.refreshDeviceCache(this.mGatt, z || !booleanExtra);
        this.mService.close(this.mGatt);
        if (this.mGatt.getDevice().getBondState() == 12) {
            boolean booleanExtra2 = intent.getBooleanExtra(DfuBaseService.EXTRA_RESTORE_BOND, false);
            if (booleanExtra2 || !booleanExtra) {
                removeBond();
                this.mService.waitFor(2000L);
                z2 = true;
            } else {
                z2 = false;
            }
            if (!booleanExtra2 || (this.mFileType & 4) <= 0) {
                z3 = z2;
            } else {
                createBond();
            }
        }
        if (this.mProgressInfo.isLastPart()) {
            if (!z3) {
                this.mService.waitFor(1400L);
            }
            this.mProgressInfo.setProgress(-6);
            return;
        }
        logi("Starting service that will upload application");
        Intent intent2 = new Intent();
        intent2.fillIn(intent, 24);
        intent2.putExtra(DfuBaseService.EXTRA_FILE_MIME_TYPE, DfuBaseService.MIME_TYPE_ZIP);
        intent2.putExtra(DfuBaseService.EXTRA_FILE_TYPE, 4);
        intent2.putExtra(DfuBaseService.EXTRA_PART_CURRENT, this.mProgressInfo.getCurrentPart() + 1);
        intent2.putExtra(DfuBaseService.EXTRA_PARTS_TOTAL, this.mProgressInfo.getTotalParts());
        restartService(intent2, true);
    }
}
