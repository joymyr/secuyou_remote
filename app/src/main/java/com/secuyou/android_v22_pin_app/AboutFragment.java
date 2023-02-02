package com.secuyou.android_v22_pin_app;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import no.joymyr.secuyou_remote.BuildConfig;
import no.joymyr.secuyou_remote.R;

/* loaded from: classes.dex */
public class AboutFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    TextView about;
    TextView appversion;
    Button homeButton;
    private OnFragmentInteractionListener mListener;
    private String mParam1;
    private String mParam2;
    WebView secuyou_help;
    private String showPage;

    /* loaded from: classes.dex */
    public interface OnFragmentInteractionListener {
        void home();

        void onFragmentInteraction(Uri uri);
    }

    public static AboutFragment newInstance(String str) {
        AboutFragment aboutFragment = new AboutFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_PARAM1, str);
        aboutFragment.setArguments(bundle);
        return aboutFragment;
    }

    @Override // androidx.fragment.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (getArguments() != null) {
            this.showPage = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View inflate = layoutInflater.inflate(R.layout.fragment_about, viewGroup, false);
        this.homeButton = (Button) inflate.findViewById(R.id.returnButton);
        this.about = (TextView) inflate.findViewById(R.id.about);
        this.secuyou_help = (WebView) inflate.findViewById(R.id.web_secuyou);
        TextView textView = (TextView) inflate.findViewById(R.id.appversion);
        this.appversion = textView;
        textView.setText("App Version: " + BuildConfig.VERSION_NAME);
        if (this.showPage == "help") {
            this.secuyou_help.loadUrl("https://www.secuyou.dk/apps/help-center");
            this.secuyou_help.setVisibility(View.VISIBLE);
            this.appversion.setVisibility(View.GONE);
            this.about.setVisibility(View.GONE);
        }
        if (this.showPage == "about") {
            this.secuyou_help.setVisibility(View.GONE);
            this.about.setVisibility(View.VISIBLE);
            this.appversion.setVisibility(View.VISIBLE);
        }
        this.homeButton.setOnClickListener(new View.OnClickListener() { // from class: com.secuyou.android_v22_pin_app.AboutFragment.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                AboutFragment.this.mListener.home();
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
