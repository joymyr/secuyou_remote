package com.secuyou.android_v22_pin_app.dfu;

import android.app.Activity;

import no.nordicsemi.android.dfu.DfuBaseService;
/* loaded from: classes.dex */
public class DfuService extends DfuBaseService {
    @Override // no.nordicsemi.android.dfu.DfuBaseService
    protected boolean isDebug() {
        return true;
    }

    @Override // no.nordicsemi.android.dfu.DfuBaseService
    protected Class<? extends Activity> getNotificationTarget() {
        return NotificationActivity.class;
    }
}
