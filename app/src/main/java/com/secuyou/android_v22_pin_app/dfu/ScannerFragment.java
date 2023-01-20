package com.secuyou.android_v22_pin_app.dfu;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import no.joymyr.secuyou_reverse.R;

/* loaded from: classes.dex */
public class ScannerFragment extends DialogFragment {
    private static final String PARAM_UUID = "param_uuid";
    private static final int REQUEST_PERMISSION_REQ_CODE = 34;
    private static final long SCAN_DURATION = 5000;
    private static final String TAG = "ScannerFragment";
    private BluetoothLeScanner BluetoothLeScannerCompat;
    private DeviceListAdapter mAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    private OnDeviceSelectedListener mListener;
    private Button mScanButton;
    private ParcelUuid mUuid;
    private boolean mIsScanning = false;
    Runnable myRunnable = new Runnable() { // from class: com.secuyou.android_v22_pin_app.dfu.ScannerFragment.3
        @Override // java.lang.Runnable
        public void run() {
            ScannerFragment.this.mIsScanning = false;
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                ScannerFragment.this.BluetoothLeScannerCompat.stopScan(ScannerFragment.this.mScanCallback);
            }
        }
    };
    private ScanCallback mScanCallback = new ScanCallback() { // from class: com.secuyou.android_v22_pin_app.dfu.ScannerFragment.4
        @Override // android.bluetooth.le.ScanCallback
        public void onBatchScanResults(List<ScanResult> list) {
        }

        @Override // android.bluetooth.le.ScanCallback
        public void onScanFailed(int i) {
        }

        @Override // android.bluetooth.le.ScanCallback
        public void onScanResult(int i, ScanResult scanResult) {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                String name = scanResult.getDevice().getName();
                BluetoothDevice device = scanResult.getDevice();
                if (name == null || !name.contains("DfuTarg")) {
                    return;
                }
                ExtendedBluetoothDevice extendedBluetoothDevice = new ExtendedBluetoothDevice(device);
                ScannerFragment.this.mListener.onDeviceSelected(extendedBluetoothDevice.device, extendedBluetoothDevice.name);
                ScannerFragment.this.stopScan();
            }
        }
    };

    /* loaded from: classes.dex */
    public interface OnDeviceSelectedListener {
        void onDeviceSelected(BluetoothDevice bluetoothDevice, String str);

        void onDialogCanceled();
    }

    public static ScannerFragment getInstance(UUID uuid) {
        ScannerFragment scannerFragment = new ScannerFragment();
        Bundle bundle = new Bundle();
        if (uuid != null) {
            bundle.putParcelable(PARAM_UUID, new ParcelUuid(uuid));
        }
        scannerFragment.setArguments(bundle);
        return scannerFragment;
    }

    @Override // androidx.fragment.app.DialogFragment, androidx.fragment.app.Fragment
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            this.mListener = (OnDeviceSelectedListener) context;
        } catch (ClassCastException unused) {
            throw new ClassCastException(context.toString() + " must implement OnDeviceSelectedListener");
        }
    }

    @Override // androidx.fragment.app.DialogFragment, androidx.fragment.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mHandler = new Handler();
        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey(PARAM_UUID)) {
            this.mUuid = (ParcelUuid) arguments.getParcelable(PARAM_UUID);
        }
        BluetoothManager bluetoothManager = (BluetoothManager) requireContext().getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            this.mBluetoothAdapter = bluetoothManager.getAdapter();
        }
    }

    @Override // androidx.fragment.app.DialogFragment, androidx.fragment.app.Fragment
    public void onDestroyView() {
        stopScan();
        super.onDestroyView();
    }

    @Override // androidx.fragment.app.DialogFragment
    public Dialog onCreateDialog(Bundle bundle) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View inflate = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_device_selection, (ViewGroup) null);
        builder.setTitle(R.string.scanner_title);
        final AlertDialog create = builder.setView(inflate).create();
        ((ListView) inflate.findViewById(android.R.id.list)).setOnItemClickListener(new AdapterView.OnItemClickListener() { // from class: com.secuyou.android_v22_pin_app.dfu.ScannerFragment.1
            @Override // android.widget.AdapterView.OnItemClickListener
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long j) {
                ScannerFragment.this.stopScan();
                create.dismiss();
                ExtendedBluetoothDevice extendedBluetoothDevice = (ExtendedBluetoothDevice) ScannerFragment.this.mAdapter.getItem(i);
                ScannerFragment.this.mListener.onDeviceSelected(extendedBluetoothDevice.device, extendedBluetoothDevice.name);
            }
        });
        Button button = (Button) inflate.findViewById(R.id.action_cancel);
        this.mScanButton = button;
        button.setOnClickListener(new View.OnClickListener() { // from class: com.secuyou.android_v22_pin_app.dfu.ScannerFragment.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                if (view.getId() == R.id.action_cancel) {
                    if (ScannerFragment.this.mIsScanning) {
                        create.cancel();
                    } else {
                        ScannerFragment.this.startScan();
                    }
                }
            }
        });
        if (bundle == null) {
            startScan();
        }
        return create;
    }

    @Override // androidx.fragment.app.DialogFragment, android.content.DialogInterface.OnCancelListener
    public void onCancel(DialogInterface dialogInterface) {
        super.onCancel(dialogInterface);
        this.mListener.onDialogCanceled();
    }

    public void startScan() {
        this.BluetoothLeScannerCompat = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
        this.mAdapter.clearDevices();
        this.mScanButton.setText(R.string.scanner_action_cancel);
        new ArrayList();
        new ScanSettings.Builder().setScanMode(2).setReportDelay(1000L).build();
        if (this.mIsScanning) {
            return;
        }
        this.mHandler.postDelayed(this.myRunnable, 5000L);
        this.mIsScanning = true;
        this.BluetoothLeScannerCompat.startScan(this.mScanCallback);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void stopScan() {
        if (this.mIsScanning) {
            this.mScanButton.setText(R.string.scanner_action_scan);
            this.BluetoothLeScannerCompat.stopScan(this.mScanCallback);
            this.mIsScanning = false;
        }
    }

    private void addBoundDevices() {
        this.mAdapter.addBondedDevices(this.mBluetoothAdapter.getBondedDevices());
    }
}
