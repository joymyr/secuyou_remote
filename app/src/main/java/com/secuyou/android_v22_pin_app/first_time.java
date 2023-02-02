package com.secuyou.android_v22_pin_app;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import no.joymyr.secuyou_remote.R;

/* loaded from: classes.dex */
public class first_time extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private Button addlockButton;
    private Button homeButton;
    private OnFragmentInteractionListener mListener;
    private String mParam1;
    private String mParam2;

    /* loaded from: classes.dex */
    public interface OnFragmentInteractionListener {
        void addlock();

        void home();

        void onFragmentInteraction(Uri uri);
    }

    public static first_time newInstance(String str, String str2) {
        first_time first_timeVar = new first_time();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_PARAM1, str);
        bundle.putString(ARG_PARAM2, str2);
        first_timeVar.setArguments(bundle);
        return first_timeVar;
    }

    @Override // androidx.fragment.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (getArguments() != null) {
            this.mParam1 = getArguments().getString(ARG_PARAM1);
            this.mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View inflate = layoutInflater.inflate(R.layout.fragment_first_time, viewGroup, false);
        this.homeButton = (Button) inflate.findViewById(R.id.home);
        this.addlockButton = (Button) inflate.findViewById(R.id.add_lock);
        this.homeButton.setOnClickListener(new View.OnClickListener() { // from class: com.secuyou.android_v22_pin_app.first_time.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                first_time.this.mListener.home();
            }
        });
        this.addlockButton.setOnClickListener(new View.OnClickListener() { // from class: com.secuyou.android_v22_pin_app.first_time.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                first_time.this.mListener.addlock();
            }
        });
        return inflate;
    }

    public void onButtonPressed(Uri uri) {
        OnFragmentInteractionListener onFragmentInteractionListener = this.mListener;
        if (onFragmentInteractionListener != null) {
            onFragmentInteractionListener.onFragmentInteraction(uri);
        }
    }

    @Override // androidx.fragment.app.Fragment
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            this.mListener = (OnFragmentInteractionListener) context;
            return;
        }
        throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
    }

    @Override // androidx.fragment.app.Fragment
    public void onDetach() {
        super.onDetach();
        this.mListener = null;
    }
}
