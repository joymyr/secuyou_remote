package com.secuyou.android_v22_pin_app.dfu;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
/* loaded from: classes.dex */
public class NotificationActivity extends Activity {
    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
//        if (isTaskRoot()) {
//            new Intent(this, DfuActivity.class).putExtras(getIntent().getExtras());
//        }
        finish();
    }
}
