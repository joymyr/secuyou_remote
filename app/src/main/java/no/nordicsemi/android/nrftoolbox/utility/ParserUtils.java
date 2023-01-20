package no.nordicsemi.android.nrftoolbox.utility;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

import no.nordicsemi.android.dfu.BuildConfig;
/* loaded from: classes.dex */
public class ParserUtils {
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String parse(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        return parse(bluetoothGattCharacteristic.getValue());
    }

    public static String parse(BluetoothGattDescriptor bluetoothGattDescriptor) {
        return parse(bluetoothGattDescriptor.getValue());
    }

    public static String parse(byte[] bArr) {
        if (bArr == null || bArr.length == 0) {
            return BuildConfig.FLAVOR;
        }
        char[] cArr = new char[(bArr.length * 3) - 1];
        for (int i = 0; i < bArr.length; i++) {
            int i2 = bArr[i] & 255;
            int i3 = i * 3;
            char[] cArr2 = HEX_ARRAY;
            cArr[i3] = cArr2[i2 >>> 4];
            cArr[i3 + 1] = cArr2[i2 & 15];
            if (i != bArr.length - 1) {
                cArr[i3 + 2] = '-';
            }
        }
        return "(0x) " + new String(cArr);
    }

    public static String parseDebug(byte[] bArr) {
        if (bArr == null || bArr.length == 0) {
            return BuildConfig.FLAVOR;
        }
        char[] cArr = new char[bArr.length * 2];
        for (int i = 0; i < bArr.length; i++) {
            int i2 = bArr[i] & 255;
            int i3 = i * 2;
            char[] cArr2 = HEX_ARRAY;
            cArr[i3] = cArr2[i2 >>> 4];
            cArr[i3 + 1] = cArr2[i2 & 15];
        }
        return "0x" + new String(cArr);
    }
}
