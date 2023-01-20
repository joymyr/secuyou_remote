package no.nordicsemi.android.dfu;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelUuid;
import android.os.Parcelable;

import no.joymyr.secuyou_reverse.R;

import java.security.InvalidParameterException;
import java.util.UUID;

/* loaded from: classes.dex */
public final class DfuServiceInitiator {
    public static final int DEFAULT_MBR_SIZE = 4096;
    public static final int DEFAULT_PRN_VALUE = 12;
    public static final int SCOPE_APPLICATION = 2;
    public static final int SCOPE_SYSTEM_COMPONENTS = 1;
    private Parcelable[] buttonlessDfuWithBondSharingUuids;
    private Parcelable[] buttonlessDfuWithoutBondSharingUuids;
    private final String deviceAddress;
    private String deviceName;
    private Parcelable[] experimentalButtonlessDfuUuids;
    private String filePath;
    private int fileResId;
    private Uri fileUri;
    private String initFilePath;
    private int initFileResId;
    private Uri initFileUri;
    private boolean keepBond;
    private Parcelable[] legacyDfuUuids;
    private String mimeType;
    private Boolean packetReceiptNotificationsEnabled;
    private boolean restoreBond;
    private Parcelable[] secureDfuUuids;
    private boolean disableNotification = false;
    private boolean startAsForegroundService = true;
    private int fileType = -1;
    private boolean forceDfu = false;
    private boolean enableUnsafeExperimentalButtonlessDfu = false;
    private boolean disableResume = false;
    private int numberOfRetries = 0;
    private int mbrSize = 4096;
    private int numberOfPackets = 12;
    private int mtu = 517;
    private int currentMtu = 23;

    public DfuServiceInitiator(String str) {
        this.deviceAddress = str;
    }

    public DfuServiceInitiator setDeviceName(String str) {
        this.deviceName = str;
        return this;
    }

    public DfuServiceInitiator setDisableNotification(boolean z) {
        this.disableNotification = z;
        return this;
    }

    public DfuServiceInitiator setForeground(boolean z) {
        this.startAsForegroundService = z;
        return this;
    }

    public DfuServiceInitiator setKeepBond(boolean z) {
        this.keepBond = z;
        return this;
    }

    public DfuServiceInitiator setRestoreBond(boolean z) {
        this.restoreBond = z;
        return this;
    }

    public DfuServiceInitiator setPacketsReceiptNotificationsEnabled(boolean z) {
        this.packetReceiptNotificationsEnabled = Boolean.valueOf(z);
        return this;
    }

    public DfuServiceInitiator setPacketsReceiptNotificationsValue(int i) {
        if (i <= 0) {
            i = 12;
        }
        this.numberOfPackets = i;
        return this;
    }

    public DfuServiceInitiator setForceDfu(boolean z) {
        this.forceDfu = z;
        return this;
    }

    public DfuServiceInitiator disableResume() {
        this.disableResume = true;
        return this;
    }

    public DfuServiceInitiator setNumberOfRetries(int i) {
        this.numberOfRetries = i;
        return this;
    }

    public DfuServiceInitiator setMtu(int i) {
        this.mtu = i;
        return this;
    }

    public DfuServiceInitiator setCurrentMtu(int i) {
        this.currentMtu = i;
        return this;
    }

    public DfuServiceInitiator disableMtuRequest() {
        this.mtu = 0;
        return this;
    }

    public DfuServiceInitiator setScope(int i) {
        if (DfuBaseService.MIME_TYPE_ZIP.equals(this.mimeType)) {
            if (i == 2) {
                this.fileType = 4;
            } else if (i == 1) {
                this.fileType = 3;
            } else if (i == 3) {
                this.fileType = 0;
            } else {
                throw new UnsupportedOperationException("Unknown scope");
            }
            return this;
        }
        throw new UnsupportedOperationException("Scope can be set only for a ZIP file");
    }

    public DfuServiceInitiator setMbrSize(int i) {
        this.mbrSize = i;
        return this;
    }

    public DfuServiceInitiator setUnsafeExperimentalButtonlessServiceInSecureDfuEnabled(boolean z) {
        this.enableUnsafeExperimentalButtonlessDfu = z;
        return this;
    }

    public DfuServiceInitiator setCustomUuidsForLegacyDfu(UUID uuid, UUID uuid2, UUID uuid3, UUID uuid4) {
        ParcelUuid[] parcelUuidArr = new ParcelUuid[4];
        parcelUuidArr[0] = uuid != null ? new ParcelUuid(uuid) : null;
        parcelUuidArr[1] = uuid2 != null ? new ParcelUuid(uuid2) : null;
        parcelUuidArr[2] = uuid3 != null ? new ParcelUuid(uuid3) : null;
        parcelUuidArr[3] = uuid4 != null ? new ParcelUuid(uuid4) : null;
        this.legacyDfuUuids = parcelUuidArr;
        return this;
    }

    public DfuServiceInitiator setCustomUuidsForSecureDfu(UUID uuid, UUID uuid2, UUID uuid3) {
        ParcelUuid[] parcelUuidArr = new ParcelUuid[3];
        parcelUuidArr[0] = uuid != null ? new ParcelUuid(uuid) : null;
        parcelUuidArr[1] = uuid2 != null ? new ParcelUuid(uuid2) : null;
        parcelUuidArr[2] = uuid3 != null ? new ParcelUuid(uuid3) : null;
        this.secureDfuUuids = parcelUuidArr;
        return this;
    }

    public DfuServiceInitiator setCustomUuidsForExperimentalButtonlessDfu(UUID uuid, UUID uuid2) {
        ParcelUuid[] parcelUuidArr = new ParcelUuid[2];
        parcelUuidArr[0] = uuid != null ? new ParcelUuid(uuid) : null;
        parcelUuidArr[1] = uuid2 != null ? new ParcelUuid(uuid2) : null;
        this.experimentalButtonlessDfuUuids = parcelUuidArr;
        return this;
    }

    public DfuServiceInitiator setCustomUuidsForButtonlessDfuWithBondSharing(UUID uuid, UUID uuid2) {
        ParcelUuid[] parcelUuidArr = new ParcelUuid[2];
        parcelUuidArr[0] = uuid != null ? new ParcelUuid(uuid) : null;
        parcelUuidArr[1] = uuid2 != null ? new ParcelUuid(uuid2) : null;
        this.buttonlessDfuWithBondSharingUuids = parcelUuidArr;
        return this;
    }

    public DfuServiceInitiator setCustomUuidsForButtonlessDfuWithoutBondSharing(UUID uuid, UUID uuid2) {
        ParcelUuid[] parcelUuidArr = new ParcelUuid[2];
        parcelUuidArr[0] = uuid != null ? new ParcelUuid(uuid) : null;
        parcelUuidArr[1] = uuid2 != null ? new ParcelUuid(uuid2) : null;
        this.buttonlessDfuWithoutBondSharingUuids = parcelUuidArr;
        return this;
    }

    public DfuServiceInitiator setZip(Uri uri) {
        return init(uri, null, 0, 0, DfuBaseService.MIME_TYPE_ZIP);
    }

    public DfuServiceInitiator setZip(String str) {
        return init(null, str, 0, 0, DfuBaseService.MIME_TYPE_ZIP);
    }

    public DfuServiceInitiator setZip(int i) {
        return init(null, null, i, 0, DfuBaseService.MIME_TYPE_ZIP);
    }

    public DfuServiceInitiator setZip(Uri uri, String str) {
        return init(uri, str, 0, 0, DfuBaseService.MIME_TYPE_ZIP);
    }

    @Deprecated
    public DfuServiceInitiator setBinOrHex(int i, Uri uri) {
        if (i == 0) {
            throw new UnsupportedOperationException("You must specify the file type");
        }
        return init(uri, null, 0, i, DfuBaseService.MIME_TYPE_OCTET_STREAM);
    }

    @Deprecated
    public DfuServiceInitiator setBinOrHex(int i, String str) {
        if (i == 0) {
            throw new UnsupportedOperationException("You must specify the file type");
        }
        return init(null, str, 0, i, DfuBaseService.MIME_TYPE_OCTET_STREAM);
    }

    @Deprecated
    public DfuServiceInitiator setBinOrHex(int i, Uri uri, String str) {
        if (i == 0) {
            throw new UnsupportedOperationException("You must specify the file type");
        }
        return init(uri, str, 0, i, DfuBaseService.MIME_TYPE_OCTET_STREAM);
    }

    @Deprecated
    public DfuServiceInitiator setBinOrHex(int i, int i2) {
        if (i == 0) {
            throw new UnsupportedOperationException("You must specify the file type");
        }
        return init(null, null, i2, i, DfuBaseService.MIME_TYPE_OCTET_STREAM);
    }

    @Deprecated
    public DfuServiceInitiator setInitFile(Uri uri) {
        return init(uri, null, 0);
    }

    @Deprecated
    public DfuServiceInitiator setInitFile(String str) {
        return init(null, str, 0);
    }

    @Deprecated
    public DfuServiceInitiator setInitFile(int i) {
        return init(null, null, i);
    }

    @Deprecated
    public DfuServiceInitiator setInitFile(Uri uri, String str) {
        return init(uri, str, 0);
    }

    public DfuServiceController start(Context context, Class<? extends DfuBaseService> cls) {
        if (this.fileType == -1) {
            throw new UnsupportedOperationException("You must specify the firmware file before starting the service");
        }
        Intent intent = new Intent(context, cls);
        intent.putExtra(DfuBaseService.EXTRA_DEVICE_ADDRESS, this.deviceAddress);
        intent.putExtra(DfuBaseService.EXTRA_DEVICE_NAME, this.deviceName);
        intent.putExtra(DfuBaseService.EXTRA_DISABLE_NOTIFICATION, this.disableNotification);
        intent.putExtra(DfuBaseService.EXTRA_FOREGROUND_SERVICE, this.startAsForegroundService);
        intent.putExtra(DfuBaseService.EXTRA_FILE_MIME_TYPE, this.mimeType);
        intent.putExtra(DfuBaseService.EXTRA_FILE_TYPE, this.fileType);
        intent.putExtra(DfuBaseService.EXTRA_FILE_URI, this.fileUri);
        intent.putExtra(DfuBaseService.EXTRA_FILE_PATH, this.filePath);
        intent.putExtra(DfuBaseService.EXTRA_FILE_RES_ID, this.fileResId);
        intent.putExtra(DfuBaseService.EXTRA_INIT_FILE_URI, this.initFileUri);
        intent.putExtra(DfuBaseService.EXTRA_INIT_FILE_PATH, this.initFilePath);
        intent.putExtra(DfuBaseService.EXTRA_INIT_FILE_RES_ID, this.initFileResId);
        intent.putExtra(DfuBaseService.EXTRA_KEEP_BOND, this.keepBond);
        intent.putExtra(DfuBaseService.EXTRA_RESTORE_BOND, this.restoreBond);
        intent.putExtra(DfuBaseService.EXTRA_FORCE_DFU, this.forceDfu);
        intent.putExtra(DfuBaseService.EXTRA_DISABLE_RESUME, this.disableResume);
        intent.putExtra(DfuBaseService.EXTRA_MAX_DFU_ATTEMPTS, this.numberOfRetries);
        intent.putExtra(DfuBaseService.EXTRA_MBR_SIZE, this.mbrSize);
        int i = this.mtu;
        if (i > 0) {
            intent.putExtra(DfuBaseService.EXTRA_MTU, i);
        }
        intent.putExtra(DfuBaseService.EXTRA_CURRENT_MTU, this.currentMtu);
        intent.putExtra(DfuBaseService.EXTRA_UNSAFE_EXPERIMENTAL_BUTTONLESS_DFU, this.enableUnsafeExperimentalButtonlessDfu);
        Boolean bool = this.packetReceiptNotificationsEnabled;
        if (bool != null) {
            intent.putExtra(DfuBaseService.EXTRA_PACKET_RECEIPT_NOTIFICATIONS_ENABLED, bool);
            intent.putExtra(DfuBaseService.EXTRA_PACKET_RECEIPT_NOTIFICATIONS_VALUE, this.numberOfPackets);
        }
        Parcelable[] parcelableArr = this.legacyDfuUuids;
        if (parcelableArr != null) {
            intent.putExtra(DfuBaseService.EXTRA_CUSTOM_UUIDS_FOR_LEGACY_DFU, parcelableArr);
        }
        Parcelable[] parcelableArr2 = this.secureDfuUuids;
        if (parcelableArr2 != null) {
            intent.putExtra(DfuBaseService.EXTRA_CUSTOM_UUIDS_FOR_SECURE_DFU, parcelableArr2);
        }
        Parcelable[] parcelableArr3 = this.experimentalButtonlessDfuUuids;
        if (parcelableArr3 != null) {
            intent.putExtra(DfuBaseService.EXTRA_CUSTOM_UUIDS_FOR_EXPERIMENTAL_BUTTONLESS_DFU, parcelableArr3);
        }
        Parcelable[] parcelableArr4 = this.buttonlessDfuWithoutBondSharingUuids;
        if (parcelableArr4 != null) {
            intent.putExtra(DfuBaseService.EXTRA_CUSTOM_UUIDS_FOR_BUTTONLESS_DFU_WITHOUT_BOND_SHARING, parcelableArr4);
        }
        Parcelable[] parcelableArr5 = this.buttonlessDfuWithBondSharingUuids;
        if (parcelableArr5 != null) {
            intent.putExtra(DfuBaseService.EXTRA_CUSTOM_UUIDS_FOR_BUTTONLESS_DFU_WITH_BOND_SHARING, parcelableArr5);
        }
        if (Build.VERSION.SDK_INT >= 26 && this.startAsForegroundService) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
        return new DfuServiceController(context);
    }

    private DfuServiceInitiator init(Uri uri, String str, int i) {
        if (DfuBaseService.MIME_TYPE_ZIP.equals(this.mimeType)) {
            throw new InvalidParameterException("Init file must be located inside the ZIP");
        }
        this.initFileUri = uri;
        this.initFilePath = str;
        this.initFileResId = i;
        return this;
    }

    private DfuServiceInitiator init(Uri uri, String str, int i, int i2, String str2) {
        this.fileUri = uri;
        this.filePath = str;
        this.fileResId = i;
        this.fileType = i2;
        this.mimeType = str2;
        if (DfuBaseService.MIME_TYPE_ZIP.equals(str2)) {
            this.initFileUri = null;
            this.initFilePath = null;
            this.initFileResId = 0;
        }
        return this;
    }

    public static void createDfuNotificationChannel(Context context) {
        NotificationChannel notificationChannel = new NotificationChannel(DfuBaseService.NOTIFICATION_CHANNEL_DFU, context.getString(R.string.dfu_channel_name), NotificationManager.IMPORTANCE_LOW);
        notificationChannel.setDescription(context.getString(R.string.dfu_channel_description));
        notificationChannel.setShowBadge(false);
        notificationChannel.setLockscreenVisibility(1);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
}
