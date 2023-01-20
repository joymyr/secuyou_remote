package no.nordicsemi.android.dfu.internal;

import android.util.Log;

import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import no.nordicsemi.android.dfu.internal.manifest.Manifest;
import no.nordicsemi.android.dfu.internal.manifest.ManifestFile;
/* loaded from: classes.dex */
public class ArchiveInputStream extends InputStream {
    private static final String APPLICATION_BIN = "application.bin";
    private static final String APPLICATION_HEX = "application.hex";
    private static final String APPLICATION_INIT = "application.dat";
    private static final String BOOTLOADER_BIN = "bootloader.bin";
    private static final String BOOTLOADER_HEX = "bootloader.hex";
    private static final String MANIFEST = "manifest.json";
    private static final String SOFTDEVICE_BIN = "softdevice.bin";
    private static final String SOFTDEVICE_HEX = "softdevice.hex";
    private static final String SYSTEM_INIT = "system.dat";
    private static final String TAG = "DfuArchiveInputStream";
    private byte[] applicationBytes;
    private byte[] applicationInitBytes;
    private int applicationSize;
    private byte[] bootloaderBytes;
    private int bootloaderSize;
    private int bytesRead;
    private int bytesReadFromCurrentSource;
    private int bytesReadFromMarkedSource;
    private CRC32 crc32;
    private byte[] currentSource;
    private Map<String, byte[]> entries;
    private Manifest manifest;
    private byte[] markedSource;
    private byte[] softDeviceAndBootloaderBytes;
    private byte[] softDeviceBytes;
    private int softDeviceSize;
    private byte[] systemInitBytes;
    private int type;
    private final ZipInputStream zipInputStream;

    @Override // java.io.InputStream
    public boolean markSupported() {
        return true;
    }

    @Override // java.io.InputStream
    public long skip(long j) {
        return 0L;
    }

    /* JADX WARN: Removed duplicated region for block: B:79:0x0219 A[Catch: all -> 0x0288, TryCatch #0 {all -> 0x0288, blocks: (B:3:0x001d, B:5:0x0025, B:11:0x0033, B:13:0x0059, B:17:0x0082, B:20:0x008c, B:22:0x0090, B:24:0x0094, B:26:0x00ba, B:27:0x00c1, B:28:0x00e1, B:29:0x00e2, B:30:0x00e9, B:31:0x00ea, B:34:0x00f4, B:36:0x00f8, B:38:0x011e, B:39:0x0125, B:40:0x0145, B:41:0x0146, B:44:0x0150, B:46:0x0154, B:48:0x0158, B:50:0x015c, B:52:0x0182, B:94:0x0273, B:60:0x01c1, B:61:0x01c8, B:53:0x0193, B:54:0x01b3, B:55:0x01b4, B:56:0x01bb, B:14:0x0060, B:15:0x0080, B:63:0x01cb, B:84:0x023d, B:97:0x0280, B:98:0x0287, B:86:0x0240, B:88:0x024e, B:89:0x025a, B:91:0x025e, B:77:0x020b, B:79:0x0219, B:80:0x0225, B:82:0x0229, B:65:0x01cf, B:67:0x01dd, B:68:0x01e9, B:70:0x01ed), top: B:102:0x001d }] */
    /* JADX WARN: Removed duplicated region for block: B:82:0x0229 A[Catch: all -> 0x0288, TryCatch #0 {all -> 0x0288, blocks: (B:3:0x001d, B:5:0x0025, B:11:0x0033, B:13:0x0059, B:17:0x0082, B:20:0x008c, B:22:0x0090, B:24:0x0094, B:26:0x00ba, B:27:0x00c1, B:28:0x00e1, B:29:0x00e2, B:30:0x00e9, B:31:0x00ea, B:34:0x00f4, B:36:0x00f8, B:38:0x011e, B:39:0x0125, B:40:0x0145, B:41:0x0146, B:44:0x0150, B:46:0x0154, B:48:0x0158, B:50:0x015c, B:52:0x0182, B:94:0x0273, B:60:0x01c1, B:61:0x01c8, B:53:0x0193, B:54:0x01b3, B:55:0x01b4, B:56:0x01bb, B:14:0x0060, B:15:0x0080, B:63:0x01cb, B:84:0x023d, B:97:0x0280, B:98:0x0287, B:86:0x0240, B:88:0x024e, B:89:0x025a, B:91:0x025e, B:77:0x020b, B:79:0x0219, B:80:0x0225, B:82:0x0229, B:65:0x01cf, B:67:0x01dd, B:68:0x01e9, B:70:0x01ed), top: B:102:0x001d }] */
    /* JADX WARN: Removed duplicated region for block: B:88:0x024e A[Catch: all -> 0x0288, TryCatch #0 {all -> 0x0288, blocks: (B:3:0x001d, B:5:0x0025, B:11:0x0033, B:13:0x0059, B:17:0x0082, B:20:0x008c, B:22:0x0090, B:24:0x0094, B:26:0x00ba, B:27:0x00c1, B:28:0x00e1, B:29:0x00e2, B:30:0x00e9, B:31:0x00ea, B:34:0x00f4, B:36:0x00f8, B:38:0x011e, B:39:0x0125, B:40:0x0145, B:41:0x0146, B:44:0x0150, B:46:0x0154, B:48:0x0158, B:50:0x015c, B:52:0x0182, B:94:0x0273, B:60:0x01c1, B:61:0x01c8, B:53:0x0193, B:54:0x01b3, B:55:0x01b4, B:56:0x01bb, B:14:0x0060, B:15:0x0080, B:63:0x01cb, B:84:0x023d, B:97:0x0280, B:98:0x0287, B:86:0x0240, B:88:0x024e, B:89:0x025a, B:91:0x025e, B:77:0x020b, B:79:0x0219, B:80:0x0225, B:82:0x0229, B:65:0x01cf, B:67:0x01dd, B:68:0x01e9, B:70:0x01ed), top: B:102:0x001d }] */
    /* JADX WARN: Removed duplicated region for block: B:91:0x025e A[Catch: all -> 0x0288, TryCatch #0 {all -> 0x0288, blocks: (B:3:0x001d, B:5:0x0025, B:11:0x0033, B:13:0x0059, B:17:0x0082, B:20:0x008c, B:22:0x0090, B:24:0x0094, B:26:0x00ba, B:27:0x00c1, B:28:0x00e1, B:29:0x00e2, B:30:0x00e9, B:31:0x00ea, B:34:0x00f4, B:36:0x00f8, B:38:0x011e, B:39:0x0125, B:40:0x0145, B:41:0x0146, B:44:0x0150, B:46:0x0154, B:48:0x0158, B:50:0x015c, B:52:0x0182, B:94:0x0273, B:60:0x01c1, B:61:0x01c8, B:53:0x0193, B:54:0x01b3, B:55:0x01b4, B:56:0x01bb, B:14:0x0060, B:15:0x0080, B:63:0x01cb, B:84:0x023d, B:97:0x0280, B:98:0x0287, B:86:0x0240, B:88:0x024e, B:89:0x025a, B:91:0x025e, B:77:0x020b, B:79:0x0219, B:80:0x0225, B:82:0x0229, B:65:0x01cf, B:67:0x01dd, B:68:0x01e9, B:70:0x01ed), top: B:102:0x001d }] */
    /* JADX WARN: Removed duplicated region for block: B:97:0x0280 A[Catch: all -> 0x0288, TRY_ENTER, TryCatch #0 {all -> 0x0288, blocks: (B:3:0x001d, B:5:0x0025, B:11:0x0033, B:13:0x0059, B:17:0x0082, B:20:0x008c, B:22:0x0090, B:24:0x0094, B:26:0x00ba, B:27:0x00c1, B:28:0x00e1, B:29:0x00e2, B:30:0x00e9, B:31:0x00ea, B:34:0x00f4, B:36:0x00f8, B:38:0x011e, B:39:0x0125, B:40:0x0145, B:41:0x0146, B:44:0x0150, B:46:0x0154, B:48:0x0158, B:50:0x015c, B:52:0x0182, B:94:0x0273, B:60:0x01c1, B:61:0x01c8, B:53:0x0193, B:54:0x01b3, B:55:0x01b4, B:56:0x01bb, B:14:0x0060, B:15:0x0080, B:63:0x01cb, B:84:0x023d, B:97:0x0280, B:98:0x0287, B:86:0x0240, B:88:0x024e, B:89:0x025a, B:91:0x025e, B:77:0x020b, B:79:0x0219, B:80:0x0225, B:82:0x0229, B:65:0x01cf, B:67:0x01dd, B:68:0x01e9, B:70:0x01ed), top: B:102:0x001d }] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public ArchiveInputStream(InputStream r6, int r7, int r8) throws IOException {
        /*
            Method dump skipped, instructions count: 661
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: no.nordicsemi.android.dfu.internal.ArchiveInputStream.<init>(java.io.InputStream, int, int):void");
    }

    private void parseZip(int i) throws IOException {
        byte[] bArr = new byte[1024];
        String str = null;
        while (true) {
            ZipEntry nextEntry = this.zipInputStream.getNextEntry();
            if (nextEntry == null) {
                break;
            }
            String name = nextEntry.getName();
            if (nextEntry.isDirectory()) {
                Log.w(TAG, "A directory found in the ZIP: " + name + "!");
            } else {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                while (true) {
                    int read = this.zipInputStream.read(bArr);
                    if (read == -1) {
                        break;
                    }
                    byteArrayOutputStream.write(bArr, 0, read);
                }
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                if (name.toLowerCase(Locale.US).endsWith("hex")) {
                    HexInputStream hexInputStream = new HexInputStream(byteArray, i);
                    byteArray = new byte[hexInputStream.available()];
                    hexInputStream.read(byteArray);
                    hexInputStream.close();
                }
                if (MANIFEST.equals(name)) {
                    str = new String(byteArray, "UTF-8");
                } else {
                    this.entries.put(name, byteArray);
                }
            }
        }
        if (this.entries.isEmpty()) {
            throw new FileNotFoundException("No files found in the ZIP. Check if the URI provided is valid and the ZIP contains required files on root level, not in a directory.");
        }
        if (str != null) {
            Manifest manifest = ((ManifestFile) new Gson().fromJson(str, (Class<ManifestFile>) ManifestFile.class)).getManifest();
            this.manifest = manifest;
            if (manifest == null) {
                Log.w(TAG, "Manifest failed to be parsed. Did you add \n-keep class no.nordicsemi.android.dfu.** { *; }\nto your proguard rules?");
                return;
            }
            return;
        }
        Log.w(TAG, "Manifest not found in the ZIP. It is recommended to use a distribution file created with: https://github.com/NordicSemiconductor/pc-nrfutil/ (for Legacy DFU use version 0.5.x)");
    }

    @Override // java.io.InputStream, java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        this.softDeviceBytes = null;
        this.bootloaderBytes = null;
        this.applicationBytes = null;
        this.softDeviceAndBootloaderBytes = null;
        this.applicationSize = 0;
        this.bootloaderSize = 0;
        this.softDeviceSize = 0;
        this.currentSource = null;
        this.bytesReadFromCurrentSource = 0;
        this.bytesRead = 0;
        this.zipInputStream.close();
    }

    @Override // java.io.InputStream
    public int read() {
        byte[] bArr = new byte[1];
        if (read(bArr) == -1) {
            return -1;
        }
        return bArr[0] & 255;
    }

    @Override // java.io.InputStream
    public int read(byte[] bArr) {
        return read(bArr, 0, bArr.length);
    }

    @Override // java.io.InputStream
    public int read(byte[] bArr, int i, int i2) {
        int rawRead = rawRead(bArr, i, i2);
        return (i2 <= rawRead || startNextFile() == null) ? rawRead : rawRead + rawRead(bArr, i + rawRead, i2 - rawRead);
    }

    private int rawRead(byte[] bArr, int i, int i2) {
        byte[] bArr2 = this.currentSource;
        int length = bArr2.length;
        int i3 = this.bytesReadFromCurrentSource;
        int i4 = length - i3;
        if (i2 > i4) {
            i2 = i4;
        }
        System.arraycopy(bArr2, i3, bArr, i, i2);
        this.bytesReadFromCurrentSource += i2;
        this.bytesRead += i2;
        this.crc32.update(bArr, i, i2);
        return i2;
    }

    @Override // java.io.InputStream
    public void mark(int i) {
        this.markedSource = this.currentSource;
        this.bytesReadFromMarkedSource = this.bytesReadFromCurrentSource;
    }

    @Override // java.io.InputStream
    public void reset() {
        byte[] bArr;
        this.currentSource = this.markedSource;
        int i = this.bytesReadFromMarkedSource;
        this.bytesReadFromCurrentSource = i;
        this.bytesRead = i;
        this.crc32.reset();
        if (this.currentSource == this.bootloaderBytes && (bArr = this.softDeviceBytes) != null) {
            this.crc32.update(bArr);
            this.bytesRead += this.softDeviceSize;
        }
        this.crc32.update(this.currentSource, 0, this.bytesReadFromCurrentSource);
    }

    public void fullReset() {
        byte[] bArr;
        byte[] bArr2 = this.softDeviceBytes;
        if (bArr2 != null && (bArr = this.bootloaderBytes) != null && this.currentSource == bArr) {
            this.currentSource = bArr2;
        }
        this.bytesReadFromCurrentSource = 0;
        mark(0);
        reset();
    }

    public int getBytesRead() {
        return this.bytesRead;
    }

    public long getCrc32() {
        return this.crc32.getValue();
    }

    public int getContentType() {
        this.type = 0;
        if (this.softDeviceAndBootloaderBytes != null) {
            this.type = 0 | 3;
        }
        if (this.softDeviceSize > 0) {
            this.type |= 1;
        }
        if (this.bootloaderSize > 0) {
            this.type |= 2;
        }
        if (this.applicationSize > 0) {
            this.type |= 4;
        }
        return this.type;
    }

    public int setContentType(int i) {
        byte[] bArr;
        this.type = i;
        int i2 = i & 4;
        if (i2 > 0 && this.applicationBytes == null) {
            this.type = i & (-5);
        }
        int i3 = i & 3;
        if (i3 == 3) {
            if (this.softDeviceBytes == null && this.softDeviceAndBootloaderBytes == null) {
                this.type &= -2;
            }
            if (this.bootloaderBytes == null && this.softDeviceAndBootloaderBytes == null) {
                this.type &= -2;
            }
        } else if (this.softDeviceAndBootloaderBytes != null) {
            this.type &= -4;
        }
        if (i3 > 0 && (bArr = this.softDeviceAndBootloaderBytes) != null) {
            this.currentSource = bArr;
        } else if ((i & 1) > 0) {
            this.currentSource = this.softDeviceBytes;
        } else if ((i & 2) > 0) {
            this.currentSource = this.bootloaderBytes;
        } else if (i2 > 0) {
            this.currentSource = this.applicationBytes;
        }
        this.bytesReadFromCurrentSource = 0;
        mark(0);
        reset();
        return this.type;
    }

    private byte[] startNextFile() {
        byte[] bArr;
        byte[] bArr2 = this.currentSource;
        if (bArr2 == this.softDeviceBytes && (bArr = this.bootloaderBytes) != null && (this.type & 2) > 0) {
            this.currentSource = bArr;
        } else {
            bArr = this.applicationBytes;
            if (bArr2 != bArr && bArr != null && (this.type & 4) > 0) {
                this.currentSource = bArr;
            } else {
                bArr = null;
                this.currentSource = null;
            }
        }
        this.bytesReadFromCurrentSource = 0;
        return bArr;
    }

    @Override // java.io.InputStream
    public int available() {
        int softDeviceImageSize;
        int i;
        byte[] bArr = this.softDeviceAndBootloaderBytes;
        if (bArr != null && this.softDeviceSize == 0 && this.bootloaderSize == 0 && (this.type & 3) > 0) {
            softDeviceImageSize = bArr.length + applicationImageSize();
            i = this.bytesRead;
        } else {
            softDeviceImageSize = softDeviceImageSize() + bootloaderImageSize() + applicationImageSize();
            i = this.bytesRead;
        }
        return softDeviceImageSize - i;
    }

    public int softDeviceImageSize() {
        if ((this.type & 1) > 0) {
            return this.softDeviceSize;
        }
        return 0;
    }

    public int bootloaderImageSize() {
        if ((this.type & 2) > 0) {
            return this.bootloaderSize;
        }
        return 0;
    }

    public int applicationImageSize() {
        if ((this.type & 4) > 0) {
            return this.applicationSize;
        }
        return 0;
    }

    public byte[] getSystemInit() {
        return this.systemInitBytes;
    }

    public byte[] getApplicationInit() {
        return this.applicationInitBytes;
    }

    public boolean isSecureDfuRequired() {
        Manifest manifest = this.manifest;
        return manifest != null && manifest.isSecureDfuRequired();
    }
}
