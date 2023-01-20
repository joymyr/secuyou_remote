package com.secuyou.android_v22_pin_app;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import no.joymyr.secuyou_reverse.R;

/* loaded from: classes.dex */
public class searchFragment extends Fragment implements SearchAdapter.ClickListener {
    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final long SCAN_PERIOD = 10000;
    Button cancel_button;
    public BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    public RecyclerView mDevicesView;
    private Handler mHandler;
    private OnListFragmentInteractionListener mListener;
    private boolean mScanning;
    public SearchAdapter mSearchAdapter;
    ProgressBar progressBar;
    private int mColumnCount = 1;
    private ScanCallback mScanCallback = new AnonymousClass2();
    Runnable myRunnable = new Runnable() { // from class: com.secuyou.android_v22_pin_app.searchFragment.3
        @Override // java.lang.Runnable
        public void run() {
            searchFragment.this.mScanning = false;
            searchFragment.this.mBluetoothLeScanner.stopScan(searchFragment.this.mScanCallback);
            searchFragment.this.progressBar.setVisibility(8);
        }
    };

    /* loaded from: classes.dex */
    public interface OnListFragmentInteractionListener {
        void cancel();

        void newLock(BleLock bleLock);

        void onListFragmentInteraction();
    }

    public static searchFragment newInstance(int i) {
        searchFragment searchfragment = new searchFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_COLUMN_COUNT, i);
        searchfragment.setArguments(bundle);
        return searchfragment;
    }

    @Override // androidx.fragment.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (getArguments() != null) {
            this.mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override // com.secuyou.android_v22_pin_app.SearchAdapter.ClickListener
    public void onItemClicked(BleLock bleLock) {
        if (this.mScanning) {
            this.mBluetoothLeScanner.stopScan(this.mScanCallback);
            this.mScanning = false;
        }
        this.mListener.newLock(bleLock);
    }

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View inflate = layoutInflater.inflate(R.layout.fragment_search_list, viewGroup, false);
        this.mHandler = new Handler();
        this.progressBar = (ProgressBar) inflate.findViewById(R.id.progressBar3);
        Button button = (Button) inflate.findViewById(R.id.cancel_search);
        this.cancel_button = button;
        button.setOnClickListener(new View.OnClickListener() { // from class: com.secuyou.android_v22_pin_app.searchFragment.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                if (searchFragment.this.mScanning) {
                    searchFragment.this.mBluetoothLeScanner.stopScan(searchFragment.this.mScanCallback);
                    searchFragment.this.mScanning = false;
                }
                searchFragment.this.mListener.cancel();
            }
        });
        RecyclerView recyclerView = (RecyclerView) inflate.findViewById(R.id.list);
        this.mDevicesView = recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        this.mDevicesView.addItemDecoration(new DividerItemDecoration(getActivity(), 1));
        return inflate;
    }

    @Override // androidx.fragment.app.Fragment
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            this.mListener = (OnListFragmentInteractionListener) context;
            return;
        }
        throw new RuntimeException(context.toString() + " must implement OnListFragmentInteractionListener");
    }

    @Override // androidx.fragment.app.Fragment
    public void onDetach() {
        super.onDetach();
        this.mListener = null;
    }

    public void scanLeDevice(boolean z, BluetoothAdapter bluetoothAdapter) {
        if (this.mBluetoothLeScanner == null) {
            this.mBluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        }
        ArrayList arrayList = new ArrayList();
        ScanSettings build = new ScanSettings.Builder().setScanMode(2).setReportDelay(0L).build();
        ByteBuffer allocate = ByteBuffer.allocate(2);
        ByteBuffer allocate2 = ByteBuffer.allocate(2);
        allocate.put(0, (byte) 22);
        allocate2.put((byte) -1);
        arrayList.add(new ScanFilter.Builder().setManufacturerData(724, allocate.array(), allocate2.array()).build());
        if (this.mScanning) {
            return;
        }
        if (z) {
            this.mHandler.postDelayed(this.myRunnable, SCAN_PERIOD);
            this.mScanning = true;
            this.mBluetoothLeScanner.startScan(arrayList, build, this.mScanCallback);
            this.progressBar.setVisibility(0);
            return;
        }
        this.mScanning = false;
        this.progressBar.setVisibility(8);
        this.mBluetoothLeScanner.stopScan(this.mScanCallback);
    }

    /* renamed from: com.secuyou.android_v22_pin_app.searchFragment$2  reason: invalid class name */
    /* loaded from: classes.dex */
    class AnonymousClass2 extends ScanCallback {
        BluetoothDevice local;

        @Override // android.bluetooth.le.ScanCallback
        public void onBatchScanResults(List<ScanResult> list) {
        }

        @Override // android.bluetooth.le.ScanCallback
        public void onScanFailed(int i) {
        }

        AnonymousClass2() {
        }

        @Override // android.bluetooth.le.ScanCallback
        public void onScanResult(int i, final ScanResult scanResult) {
            searchFragment.this.getActivity().runOnUiThread(new Runnable() { // from class: com.secuyou.android_v22_pin_app.searchFragment.2.1
                @Override // java.lang.Runnable
                public void run() {
                    AnonymousClass2.this.local = scanResult.getDevice();
                    if (searchFragment.this.mSearchAdapter.mService.deviceInBleManagerDevice(AnonymousClass2.this.local)) {
                        return;
                    }
                    BleLock bleLock = new BleLock(AnonymousClass2.this.local);
                    byte[] bytes = scanResult.getScanRecord().getBytes();
                    bleLock.name = AnonymousClass2.this.local.getName();
                    bleLock.setLockHardwareVersion(bytes);
                    searchFragment.this.mSearchAdapter.onDeviceAdded(bleLock);
                }
            });
        }
    }

    public boolean setAdapter(SearchAdapter searchAdapter) {
        this.mSearchAdapter = searchAdapter;
        this.mDevicesView.setAdapter(searchAdapter);
        this.mSearchAdapter.setListener(this);
        return true;
    }
}
