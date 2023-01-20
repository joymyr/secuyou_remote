package com.secuyou.android_v22_pin_app;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import no.joymyr.secuyou_reverse.R;

/* loaded from: classes.dex */
public class devicesFragment extends Fragment implements DeviceAdapter.ClickListener {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private DeviceAdapter.ClickListener adapter_listener;
    public DeviceAdapter mAdapter;
    private boolean mConnected = false;
    public RecyclerView mDevicesView;
    private OnFragmentInteractionListener mListener;
    private String mParam1;
    private String mParam2;

    /* loaded from: classes.dex */
    public interface OnFragmentInteractionListener {
        void lock_unlock(BluetoothDevice bluetoothDevice);

        void onFragmentInteraction(Uri uri);
    }

    public static devicesFragment newInstance(String str, String str2) {
        devicesFragment devicesfragment = new devicesFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_PARAM1, str);
        bundle.putString(ARG_PARAM2, str2);
        devicesfragment.setArguments(bundle);
        return devicesfragment;
    }

    @Override // androidx.fragment.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (getArguments() != null) {
            this.mParam1 = getArguments().getString(ARG_PARAM1);
            this.mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override // com.secuyou.android_v22_pin_app.DeviceAdapter.ClickListener
    public void onItemClicked(BluetoothDevice bluetoothDevice) {
        this.mListener.lock_unlock(bluetoothDevice);
    }

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View inflate = layoutInflater.inflate(R.layout.fragment_devices, viewGroup, false);
        RecyclerView recyclerView = (RecyclerView) inflate.findViewById(R.id.devices);
        this.mDevicesView = recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        this.mDevicesView.addItemDecoration(new DividerItemDecoration(getActivity(), 1));
        this.mDevicesView.setAdapter(this.mAdapter);
        DeviceAdapter deviceAdapter = this.mAdapter;
        if (deviceAdapter != null) {
            deviceAdapter.notifyDataSetChanged();
        }
        return inflate;
    }

    public boolean setAdapter(DeviceAdapter deviceAdapter) {
        this.mAdapter = deviceAdapter;
        RecyclerView recyclerView = this.mDevicesView;
        if (recyclerView != null) {
            recyclerView.setAdapter(deviceAdapter);
        }
        this.mAdapter.setListener(this);
        return true;
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
