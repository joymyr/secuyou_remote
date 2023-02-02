package com.secuyou.android_v22_pin_app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import no.joymyr.secuyou_remote.R;

/* loaded from: classes.dex */
public class fragment_lockSpec extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";
    private static final String ARG_PARAM4 = "param4";
    private static final String ARG_PARAM5 = "param5";
    private static final String ARG_PARAM6 = "param6";
    private static final String ARG_PARAM7 = "param7";
    private static final String ARG_PARAM8 = "param8";
    private static final int PLUS_ONE_REQUEST_CODE = 0;
    private final String PLUS_ONE_URL = "http://developer.android.com";
    ImageView battery_image;
    private int battery_status;
    TextView battery_txt;
    ImageView connection_image;
    TextView connection_txt;
    private Button deleteButton;
    TextView firmwareVersion;
    String fw_version;
    TextView hardwareVersion;
    Boolean homelock;
    SwitchCompat homelock_switch;
    TextView homelock_txt;
    String hw_version;
    Boolean isConnected;
    ImageView lockImage;
    TextView lockName;
    private String lockname;
    private OnFragmentInteractionListener mListener;
    String name;
    private String pincode;
    TextView pincode_view;
    private Button renameButton;
    private Button returnButton;
    Boolean sensor_hpr_lock;
    SwitchCompat sensor_hpr_lock_switch;
    TextView sensor_hpr_text;
    private ImageButton serviceButton;

    /* loaded from: classes.dex */
    public interface OnFragmentInteractionListener {
        void deleteLock();

        void home();

        void onFragmentInteraction(Uri uri);

        void renameLock(String str);

        void serviceCheck();

        void set_home_lock();

        void set_hpr_sensor_lock();
    }

    private String update_battery_txt(int i) {
        return i != 1 ? i != 2 ? i != 3 ? "Good" : "Empty" : "Critical" : "Low";
    }

    public static fragment_lockSpec newInstance(String str, Boolean bool, int i, String str2, Boolean bool2, String str3, String str4, Boolean bool3) {
        fragment_lockSpec fragment_lockspec = new fragment_lockSpec();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_PARAM1, str);
        bundle.putBoolean(ARG_PARAM2, bool.booleanValue());
        bundle.putInt(ARG_PARAM3, i);
        bundle.putString(ARG_PARAM4, str2);
        bundle.putBoolean(ARG_PARAM5, bool2.booleanValue());
        bundle.putString(ARG_PARAM6, str3);
        bundle.putString(ARG_PARAM7, str4);
        bundle.putBoolean(ARG_PARAM8, bool3.booleanValue());
        fragment_lockspec.setArguments(bundle);
        return fragment_lockspec;
    }

    @Override // androidx.fragment.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (getArguments() != null) {
            this.lockname = getArguments().getString(ARG_PARAM1);
            this.isConnected = Boolean.valueOf(getArguments().getBoolean(ARG_PARAM2));
            this.battery_status = getArguments().getInt(ARG_PARAM3);
            this.pincode = getArguments().getString(ARG_PARAM4);
            this.homelock = Boolean.valueOf(getArguments().getBoolean(ARG_PARAM5));
            this.fw_version = getArguments().getString(ARG_PARAM6);
            this.hw_version = getArguments().getString(ARG_PARAM7);
            this.sensor_hpr_lock = Boolean.valueOf(getArguments().getBoolean(ARG_PARAM8));
        }
    }

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View inflate = layoutInflater.inflate(R.layout.fragment_lock_spec, viewGroup, false);
        this.deleteButton = inflate.findViewById(R.id.delete);
        this.renameButton = inflate.findViewById(R.id.rename);
        this.serviceButton = inflate.findViewById(R.id.serviceButton);
        this.lockImage = inflate.findViewById(R.id.lockimage);
        this.lockName = inflate.findViewById(R.id.lockname);
        this.homelock_switch = inflate.findViewById(R.id.switch1);
        this.homelock_txt = inflate.findViewById(R.id.homelock);
        this.sensor_hpr_lock_switch = inflate.findViewById(R.id.switch2);
        this.sensor_hpr_text = inflate.findViewById(R.id.sensor_hpr);
        this.connection_image = inflate.findViewById(R.id.connected);
        this.connection_txt = inflate.findViewById(R.id.connected_txt);
        this.battery_image = inflate.findViewById(R.id.battery_image);
        this.battery_txt = inflate.findViewById(R.id.battery_txt);
        this.pincode_view = inflate.findViewById(R.id.pincode);
        this.firmwareVersion = inflate.findViewById(R.id.firmwareversionString);
        this.hardwareVersion = inflate.findViewById(R.id.lockhardwareversionString);
        this.pincode_view.setText(this.pincode);
        this.connection_image.setImageResource(update_connection_image(this.isConnected).intValue());
        if (this.isConnected.booleanValue()) {
            this.connection_txt.setText("Connected");
        } else {
            this.connection_txt.setText("Disconnected");
        }
        if (this.isConnected.booleanValue()) {
            this.battery_image.setVisibility(0);
            this.battery_txt.setText(update_battery_txt(this.battery_status));
            this.battery_image.setImageResource(update_battery_image(this.battery_status).intValue());
            this.homelock_switch.setVisibility(0);
            this.sensor_hpr_lock_switch.setVisibility(0);
            this.homelock_switch.setChecked(this.homelock.booleanValue());
            this.homelock_txt.setVisibility(0);
            this.sensor_hpr_lock_switch.setChecked(!this.sensor_hpr_lock.booleanValue());
            this.sensor_hpr_text.setVisibility(0);
        } else {
            this.battery_txt.setText(no.nordicsemi.android.dfu.BuildConfig.FLAVOR);
            this.battery_image.setVisibility(8);
            this.homelock_switch.setVisibility(8);
            this.homelock_txt.setVisibility(8);
            this.sensor_hpr_lock_switch.setVisibility(8);
            this.sensor_hpr_text.setVisibility(8);
        }
        this.lockName.setText(this.lockname);
        this.returnButton = (Button) inflate.findViewById(R.id.returnButton);
        this.firmwareVersion.setText(this.fw_version);
        this.hardwareVersion.setText(this.hw_version);
        this.deleteButton.setOnClickListener(new View.OnClickListener() { // from class: com.secuyou.android_v22_pin_app.fragment_lockSpec.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                fragment_lockSpec.this.mListener.deleteLock();
            }
        });
        this.renameButton.setOnClickListener(new View.OnClickListener() { // from class: com.secuyou.android_v22_pin_app.fragment_lockSpec.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                fragment_lockSpec.this.showName();
            }
        });
        this.homelock_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { // from class: com.secuyou.android_v22_pin_app.fragment_lockSpec.3
            @Override // android.widget.CompoundButton.OnCheckedChangeListener
            public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                fragment_lockSpec.this.mListener.set_home_lock();
            }
        });
        this.sensor_hpr_lock_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { // from class: com.secuyou.android_v22_pin_app.fragment_lockSpec.4
            @Override // android.widget.CompoundButton.OnCheckedChangeListener
            public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                fragment_lockSpec.this.mListener.set_hpr_sensor_lock();
            }
        });
        this.returnButton.setOnClickListener(new View.OnClickListener() { // from class: com.secuyou.android_v22_pin_app.fragment_lockSpec.5
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                fragment_lockSpec.this.mListener.home();
            }
        });
        this.serviceButton.setOnClickListener(new View.OnClickListener() { // from class: com.secuyou.android_v22_pin_app.fragment_lockSpec.6
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                fragment_lockSpec.this.mListener.serviceCheck();
            }
        });
        return inflate;
    }

    @Override // androidx.fragment.app.Fragment
    public void onResume() {
        super.onResume();
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

    public void showName() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Name the lock");
        final EditText editText = new EditText(getContext());
        builder.setView(editText);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { // from class: com.secuyou.android_v22_pin_app.fragment_lockSpec.7
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                fragment_lockSpec.this.name = editText.getEditableText().toString();
                if (fragment_lockSpec.this.name.length() <= 12) {
                    fragment_lockSpec.this.mListener.renameLock(fragment_lockSpec.this.name);
                    return;
                }
                AlertDialog.Builder builder2 = new AlertDialog.Builder(fragment_lockSpec.this.getContext());
                builder2.setTitle("Name is too long");
                builder2.setMessage("The name entered must be maximum 12 characters long - please try again");
                builder2.setPositiveButton(android.R.string.ok, (DialogInterface.OnClickListener) null);
                builder2.show();
            }
        });
        AlertDialog create = builder.create();
        create.getWindow().setSoftInputMode(5);
        create.show();
    }

    private Integer update_connection_image(Boolean bool) {
        if (bool.booleanValue()) {
            return Integer.valueOf((int) R.drawable.sym_connected);
        }
        return Integer.valueOf((int) R.drawable.sym_disconnected);
    }

    private Integer update_battery_image(int i) {
        if (i != 0) {
            if (i != 1) {
                if (i != 2) {
                    if (i == 3) {
                        return Integer.valueOf((int) android.R.drawable.ic_lock_idle_low_battery);
                    }
                    return Integer.valueOf((int) R.drawable.sym_batt_full);
                }
                return Integer.valueOf((int) android.R.drawable.ic_lock_idle_low_battery);
            }
            return Integer.valueOf((int) android.R.drawable.ic_lock_idle_low_battery);
        }
        return Integer.valueOf((int) R.drawable.sym_batt_full);
    }
}
