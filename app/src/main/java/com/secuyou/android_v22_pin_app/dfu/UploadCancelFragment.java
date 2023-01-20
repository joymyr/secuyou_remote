package com.secuyou.android_v22_pin_app.dfu;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.DialogFragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import no.joymyr.secuyou_reverse.R;

import no.nordicsemi.android.dfu.DfuBaseService;
/* loaded from: classes.dex */
public class UploadCancelFragment extends DialogFragment {
    private static final String TAG = "UploadCancelFragment";
    private CancelFragmentListener mListener;

    /* loaded from: classes.dex */
    public interface CancelFragmentListener {
        void onCancelUpload();
    }

    public static UploadCancelFragment getInstance() {
        return new UploadCancelFragment();
    }

    @Override // androidx.fragment.app.Fragment
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.mListener = (CancelFragmentListener) activity;
        } catch (ClassCastException unused) {
            Log.d(TAG, "The parent Activity must implement CancelFragmentListener interface");
        }
    }

    @Override // androidx.fragment.app.DialogFragment
    public Dialog onCreateDialog(Bundle bundle) {
        return new AlertDialog.Builder(getActivity()).setTitle(R.string.dfu_confirmation_dialog_title).setMessage(R.string.dfu_upload_dialog_cancel_message).setCancelable(false).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() { // from class: com.secuyou.android_v22_pin_app.dfu.UploadCancelFragment.2
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(UploadCancelFragment.this.getActivity());
                Intent intent = new Intent(DfuBaseService.BROADCAST_ACTION);
                intent.putExtra(DfuBaseService.EXTRA_ACTION, 2);
                localBroadcastManager.sendBroadcast(intent);
                UploadCancelFragment.this.mListener.onCancelUpload();
            }
        }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() { // from class: com.secuyou.android_v22_pin_app.dfu.UploadCancelFragment.1
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        }).create();
    }

    @Override // androidx.fragment.app.DialogFragment, android.content.DialogInterface.OnCancelListener
    public void onCancel(DialogInterface dialogInterface) {
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getActivity());
        Intent intent = new Intent(DfuBaseService.BROADCAST_ACTION);
        intent.putExtra(DfuBaseService.EXTRA_ACTION, 1);
        localBroadcastManager.sendBroadcast(intent);
    }
}
