package com.secuyou.android_v22_pin_app.dfu;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.fragment.app.DialogFragment;

import no.joymyr.secuyou_reverse.R;
/* loaded from: classes.dex */
public class ZipInfoFragment extends DialogFragment {
    @Override // androidx.fragment.app.DialogFragment
    public Dialog onCreateDialog(Bundle bundle) {
        return new AlertDialog.Builder(getActivity()).setView(LayoutInflater.from(getActivity()).inflate(R.layout.fragment_zip_info, (ViewGroup) null)).setTitle(R.string.dfu_file_info).setPositiveButton(R.string.ok, (DialogInterface.OnClickListener) null).create();
    }
}
