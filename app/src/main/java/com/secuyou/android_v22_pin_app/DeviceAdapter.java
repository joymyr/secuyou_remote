package com.secuyou.android_v22_pin_app;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import no.joymyr.secuyou_reverse.R;

/* loaded from: classes.dex */
public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {
    private Context mContext;
    private List<BluetoothDevice> mDevices;
    public ClickListener mListener;
    BleMulticonnectProfileService.LocalBinder mService;

    /* loaded from: classes.dex */
    public interface ClickListener {
        void onItemClicked(BluetoothDevice bluetoothDevice);
    }

    public void setListener(ClickListener clickListener) {
        this.mListener = clickListener;
    }

    public DeviceAdapter(BleMulticonnectProfileService.LocalBinder localBinder, Context context) {
        this.mService = localBinder;
        this.mContext = context;
        this.mDevices = localBinder.getManagedDevices();
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(this.mContext).inflate(R.layout.lock, viewGroup, false));
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        viewHolder.bind(this.mDevices.get(i));
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemCount() {
        return this.mDevices.size();
    }

    public void onDeviceRemoved(BluetoothDevice bluetoothDevice) {
        notifyDataSetChanged();
    }

    public void onDeviceStateChanged(BluetoothDevice bluetoothDevice) {
        int indexOf = this.mDevices.indexOf(bluetoothDevice);
        if (indexOf >= 0) {
            notifyItemChanged(indexOf);
        }
    }

    public void onLockstatusValueReceived(BluetoothDevice bluetoothDevice) {
        int indexOf = this.mDevices.indexOf(bluetoothDevice);
        if (indexOf >= 0) {
            notifyItemChanged(indexOf);
        }
    }

    /* loaded from: classes.dex */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView connection_image;
        private TextView connection_txt;
        private ImageButton lockButton;
        private TextView nameView;
        private ProgressBar progressBar;

        public ViewHolder(View view) {
            super(view);
            DeviceAdapter.this.mContext = view.getContext();
            this.progressBar = (ProgressBar) view.findViewById(R.id.progressBar3);
            ImageButton imageButton = (ImageButton) view.findViewById(R.id.lock_button);
            this.lockButton = imageButton;
            imageButton.setOnClickListener(this);
            this.nameView = (TextView) view.findViewById(R.id.txtDeviceNameSearch);
            this.connection_image = (ImageView) view.findViewById(R.id.connect_image);
            this.connection_txt = (TextView) view.findViewById(R.id.connection_txt);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void bind(BluetoothDevice bluetoothDevice) {
            this.nameView.setText(DeviceAdapter.this.mService.getLockNameValue(bluetoothDevice));
            this.lockButton.setImageResource(DeviceAdapter.this.update_lock_image(bluetoothDevice).intValue());
            this.connection_image.setImageResource(DeviceAdapter.this.update_connection_image(bluetoothDevice).intValue());
            if (DeviceAdapter.this.mService.isConnected(bluetoothDevice)) {
                this.connection_txt.setText("Connected");
                this.progressBar.setVisibility(View.INVISIBLE);
                return;
            }
            this.connection_txt.setText("Disconnected");
            this.progressBar.setVisibility(View.VISIBLE);
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            BluetoothDevice bluetoothDevice = (BluetoothDevice) DeviceAdapter.this.mDevices.get(getAdapterPosition());
            if (!this.lockButton.isPressed() || DeviceAdapter.this.mService.getConnectionState(bluetoothDevice) == 0) {
                return;
            }
            DeviceAdapter.this.mListener.onItemClicked(bluetoothDevice);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Integer update_lock_image(BluetoothDevice bluetoothDevice) {
        boolean isConnected = this.mService.isConnected(bluetoothDevice);
        Integer valueOf = R.drawable.lock_faded;
        if (isConnected) {
            if (this.mService.getLockMechanismPosition(bluetoothDevice) != BleLockState.LOCKING_MECHANISM_POSITION.UNLOCKED) {
                return this.mService.getLockMechanismPosition(bluetoothDevice) == BleLockState.LOCKING_MECHANISM_POSITION.LOCKED ? R.drawable.lock : valueOf;
            } else if (this.mService.getHandleValue(bluetoothDevice) == BleLockState.HANDLE_STATE.FCHO.getValue()) {
                return R.drawable.lock_open_faded;
            } else {
                return R.drawable.lock_open;
            }
        }
        if (this.mService.getConnectionState(bluetoothDevice) == 1) {
        }
        return valueOf;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Integer update_connection_image(BluetoothDevice bluetoothDevice) {
        if (this.mService.isConnected(bluetoothDevice)) {
            return R.drawable.sym_connected;
        }
        return R.drawable.sym_disconnected;
    }
}
