package com.secuyou.android_v22_pin_app.dfu;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.widget.Toast;

import no.joymyr.secuyou_reverse.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
/* loaded from: classes.dex */
public class FileHelper {
    public static final String BOARD_FOLDER = "Board";
    public static final String BOARD_NRF6310_FOLDER = "nrf6310";
    public static final String BOARD_PCA10028_FOLDER = "pca10028";
    public static final String BOARD_PCA10036_FOLDER = "pca10036";
    private static final int CURRENT_SAMPLES_VERSION = 4;
    public static final String NORDIC_FOLDER = "Nordic Semiconductor";
    private static final String PREFS_SAMPLES_VERSION = "no.nordicsemi.android.nrftoolbox.dfu.PREFS_SAMPLES_VERSION";
    private static final String TAG = "FileHelper";
    public static final String UART_FOLDER = "UART Configurations";

    public static boolean newSamplesAvailable(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(PREFS_SAMPLES_VERSION, 0) < 4;
    }

    public static void createSamples(Context context) {
        boolean z;
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (defaultSharedPreferences.getInt(PREFS_SAMPLES_VERSION, 0) == 4) {
            return;
        }
        File file = new File(Environment.getExternalStorageDirectory(), NORDIC_FOLDER);
        if (!file.exists()) {
            file.mkdir();
        }
        File file2 = new File(file, BOARD_FOLDER);
        if (!file2.exists()) {
            file2.mkdir();
        }
        File file3 = new File(file2, BOARD_NRF6310_FOLDER);
        if (!file3.exists()) {
            file3.mkdir();
        }
        File file4 = new File(file2, BOARD_PCA10028_FOLDER);
        if (!file4.exists()) {
            file4.mkdir();
        }
        new File(file, "ble_app_hrs_s110_v6_0_0.hex").delete();
        new File(file, "ble_app_rscs_s110_v6_0_0.hex").delete();
        new File(file, "ble_app_hrs_s110_v7_0_0.hex").delete();
        new File(file, "ble_app_rscs_s110_v7_0_0.hex").delete();
        new File(file, "blinky_arm_s110_v7_0_0.hex").delete();
        new File(file, "dfu_2_0.bat").delete();
        new File(file, "dfu_3_0.bat").delete();
        new File(file, "dfu_2_0.sh").delete();
        new File(file, "dfu_3_0.sh").delete();
        new File(file, "README.txt").delete();
        File file5 = new File(file3, "bl_blset_app_sd_v22_fw3.hex");
        if (file5.exists()) {
            z = false;
        } else {
            copyRawResource(context, R.raw.bl_app_sd_v22_v6, file5);
            z = true;
        }
        if (z) {
            Toast.makeText(context, (int) R.string.dfu_example_files_created, 0).show();
        }
        defaultSharedPreferences.edit().putInt(PREFS_SAMPLES_VERSION, 4).apply();
    }

    private static void copyRawResource(Context context, int i, File file) {
        try {
            InputStream openRawResource = context.getResources().openRawResource(i);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            byte[] bArr = new byte[1024];
            while (true) {
                int read = openRawResource.read(bArr);
                if (read > 0) {
                    fileOutputStream.write(bArr, 0, read);
                } else {
                    openRawResource.close();
                    fileOutputStream.close();
                    return;
                }
            }
        } catch (IOException unused) {
        }
    }

    public static Uri getContentUri(Context context, File file) {
        String absolutePath = file.getAbsolutePath();
        Uri contentUri = MediaStore.Files.getContentUri("external");
        Cursor query = context.getContentResolver().query(contentUri, new String[]{"_id"}, "_data=? ", new String[]{absolutePath}, null);
        if (query != null) {
            try {
                if (query.moveToFirst()) {
                    return Uri.withAppendedPath(contentUri, String.valueOf(query.getInt(query.getColumnIndex("_id"))));
                }
            } finally {
                query.close();
            }
        }
        if (!file.exists()) {
            return null;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put("_data", absolutePath);
        return context.getContentResolver().insert(contentUri, contentValues);
    }
}
