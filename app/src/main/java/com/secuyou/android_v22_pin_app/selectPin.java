package com.secuyou.android_v22_pin_app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import java.lang.reflect.Field;

import no.joymyr.secuyou_reverse.R;

/* loaded from: classes.dex */
public class selectPin extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    Button button;
    ImageView connecting;
    private OnFragmentInteractionListener mListener;
    private String mParam1;
    private String mParam2;
    String name;
    NumberPicker np0;
    NumberPicker np1;
    NumberPicker np2;
    NumberPicker np3;
    NumberPicker np4;
    byte pincode0;
    byte pincode1;
    byte pincode2;
    byte pincode3;
    byte pincode4;
    ProgressBar progressBar;
    TextView text;

    /* loaded from: classes.dex */
    public interface OnFragmentInteractionListener {
        void onNameSelected(String str);

        void onPinSelected();
    }

    public static selectPin newInstance(String str, String str2) {
        selectPin selectpin = new selectPin();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_PARAM1, str);
        bundle.putString(ARG_PARAM2, str2);
        selectpin.setArguments(bundle);
        return selectpin;
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
        View inflate = layoutInflater.inflate(R.layout.selectcode, viewGroup, false);
        TextView textView = (TextView) inflate.findViewById(R.id.tv0);
        this.text = textView;
        textView.setText("Select PIN");
        this.text.setVisibility(View.GONE);
        ProgressBar progressBar = (ProgressBar) inflate.findViewById(R.id.progressBar3);
        this.progressBar = progressBar;
        progressBar.setVisibility(View.VISIBLE);
        Button button = (Button) inflate.findViewById(R.id.button);
        this.button = button;
        button.setVisibility(View.GONE);
        ImageView imageView = (ImageView) inflate.findViewById(R.id.connecting);
        this.connecting = imageView;
        imageView.setVisibility(View.VISIBLE);
        NumberPicker numberPicker = (NumberPicker) inflate.findViewById(R.id.np0);
        this.np0 = numberPicker;
        setDividerColor(numberPicker, -1);
        NumberPicker numberPicker2 = (NumberPicker) inflate.findViewById(R.id.np1);
        this.np1 = numberPicker2;
        setDividerColor(numberPicker2, -1);
        NumberPicker numberPicker3 = (NumberPicker) inflate.findViewById(R.id.np2);
        this.np2 = numberPicker3;
        setDividerColor(numberPicker3, -1);
        NumberPicker numberPicker4 = (NumberPicker) inflate.findViewById(R.id.np3);
        this.np3 = numberPicker4;
        setDividerColor(numberPicker4, -1);
        NumberPicker numberPicker5 = (NumberPicker) inflate.findViewById(R.id.np4);
        this.np4 = numberPicker5;
        setDividerColor(numberPicker5, -1);
        this.np0.setVisibility(View.GONE);
        this.np1.setVisibility(View.GONE);
        this.np2.setVisibility(View.GONE);
        this.np3.setVisibility(View.GONE);
        this.np4.setVisibility(View.GONE);
        return inflate;
    }

    private void setDividerColor(NumberPicker numberPicker, int i) {
        Field[] declaredFields;
        for (Field field : NumberPicker.class.getDeclaredFields()) {
            if (field.getName().equals("mSelectionDivider")) {
                field.setAccessible(true);
                try {
                    field.set(numberPicker, new ColorDrawable(i));
                    return;
                } catch (Resources.NotFoundException e) {
                    e.printStackTrace();
                    return;
                } catch (IllegalAccessException e2) {
                    e2.printStackTrace();
                    return;
                } catch (IllegalArgumentException e3) {
                    e3.printStackTrace();
                    return;
                }
            }
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

    public void showPincode() {
        this.progressBar.setVisibility(View.GONE);
        this.connecting.setVisibility(View.GONE);
        this.text.setVisibility(View.VISIBLE);
        setValues();
        this.np0.setVisibility(View.VISIBLE);
        this.np1.setVisibility(View.VISIBLE);
        this.np2.setVisibility(View.VISIBLE);
        this.np3.setVisibility(View.VISIBLE);
        this.np4.setVisibility(View.VISIBLE);
        this.button.setVisibility(View.VISIBLE);
        this.button.setOnClickListener(new View.OnClickListener() { // from class: com.secuyou.android_v22_pin_app.selectPin.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                selectPin.this.mListener.onPinSelected();
            }
        });
    }

    public void showName() {
        this.progressBar.setVisibility(View.GONE);
        this.text.setVisibility(View.GONE);
        this.connecting.setVisibility(View.GONE);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Name the lock");
        final EditText editText = new EditText(getContext());
        builder.setView(editText);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { // from class: com.secuyou.android_v22_pin_app.selectPin.2
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                selectPin.this.name = editText.getEditableText().toString();
                if (selectPin.this.name.length() <= 10) {
                    selectPin.this.mListener.onNameSelected(selectPin.this.name);
                    return;
                }
                Toast makeText = Toast.makeText(selectPin.this.getContext().getApplicationContext(), "The name must be shorter than 12 letters ", Toast.LENGTH_SHORT);
                ((TextView) makeText.getView().findViewById(16908299)).setTextColor(Color.red(50));
                makeText.show();
            }
        });
        AlertDialog create = builder.create();
        create.getWindow().setSoftInputMode(5);
        create.show();
    }

    private void setValues() {
        this.np0.setMinValue(0);
        this.np0.setMaxValue(9);
        this.np1.setMinValue(0);
        this.np1.setMaxValue(9);
        this.np2.setMinValue(0);
        this.np2.setMaxValue(9);
        this.np3.setMinValue(0);
        this.np3.setMaxValue(9);
        this.np4.setMinValue(0);
        this.np4.setMaxValue(9);
        this.np0.setWrapSelectorWheel(true);
        this.np1.setWrapSelectorWheel(true);
        this.np2.setWrapSelectorWheel(true);
        this.np3.setWrapSelectorWheel(true);
        this.np4.setWrapSelectorWheel(true);
        this.np0.setValue(1);
        this.pincode0 = (byte) this.np0.getValue();
        this.np1.setValue(2);
        this.pincode1 = (byte) this.np1.getValue();
        this.np2.setValue(3);
        this.pincode2 = (byte) this.np2.getValue();
        this.np3.setValue(4);
        this.pincode3 = (byte) this.np3.getValue();
        this.np4.setValue(5);
        this.pincode4 = (byte) this.np4.getValue();
        this.np0.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() { // from class: com.secuyou.android_v22_pin_app.selectPin.3
            @Override // android.widget.NumberPicker.OnValueChangeListener
            public void onValueChange(NumberPicker numberPicker, int i, int i2) {
                selectPin.this.pincode0 = (byte) i2;
            }
        });
        this.np1.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() { // from class: com.secuyou.android_v22_pin_app.selectPin.4
            @Override // android.widget.NumberPicker.OnValueChangeListener
            public void onValueChange(NumberPicker numberPicker, int i, int i2) {
                selectPin.this.pincode1 = (byte) i2;
            }
        });
        this.np2.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() { // from class: com.secuyou.android_v22_pin_app.selectPin.5
            @Override // android.widget.NumberPicker.OnValueChangeListener
            public void onValueChange(NumberPicker numberPicker, int i, int i2) {
                selectPin.this.pincode2 = (byte) i2;
            }
        });
        this.np3.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() { // from class: com.secuyou.android_v22_pin_app.selectPin.6
            @Override // android.widget.NumberPicker.OnValueChangeListener
            public void onValueChange(NumberPicker numberPicker, int i, int i2) {
                selectPin.this.pincode3 = (byte) i2;
            }
        });
        this.np4.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() { // from class: com.secuyou.android_v22_pin_app.selectPin.7
            @Override // android.widget.NumberPicker.OnValueChangeListener
            public void onValueChange(NumberPicker numberPicker, int i, int i2) {
                selectPin.this.pincode4 = (byte) i2;
            }
        });
    }
}
