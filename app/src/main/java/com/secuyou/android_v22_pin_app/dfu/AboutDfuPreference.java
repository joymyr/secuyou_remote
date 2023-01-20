package com.secuyou.android_v22_pin_app.dfu;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.preference.Preference;
import android.util.AttributeSet;
import android.widget.Toast;

import no.joymyr.secuyou_reverse.R;
/* loaded from: classes.dex */
public class AboutDfuPreference extends Preference {
    public AboutDfuPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public AboutDfuPreference(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    @Override // android.preference.Preference
    protected void onClick() {
        Context context = getContext();
        Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("https://www.nordicsemi.com/DocLib/Content/SDK_Doc/nRF5_SDK/v15-3-0/ble_sdk_app_dfu_bootloader"));
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setFlags(268435456);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        } else {
            Toast.makeText(getContext(), (int) R.string.no_application, 1).show();
        }
    }
}
