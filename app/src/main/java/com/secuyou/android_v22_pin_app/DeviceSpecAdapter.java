package com.secuyou.android_v22_pin_app;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import no.joymyr.secuyou_reverse.R;

/* loaded from: classes.dex */
public class DeviceSpecAdapter extends RecyclerView.Adapter<DeviceSpecAdapter.ViewHolder> {
    private Context mContext;
    private final List<BluetoothDevice> mDevices;
    public ClickListener mListener;
    BleMulticonnectProfileService.LocalBinder mService;

    /* loaded from: classes.dex */
    public interface ClickListener {
        void onAdmClicked(BluetoothDevice bluetoothDevice);

        void onItemClicked(BluetoothDevice bluetoothDevice);
    }

    public void setListener(ClickListener clickListener) {
        this.mListener = clickListener;
    }

    public DeviceSpecAdapter(BleMulticonnectProfileService.LocalBinder localBinder, Context context) {
        this.mService = localBinder;
        this.mContext = context;
        this.mDevices = localBinder.getManagedDevices();
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(this.mContext).inflate(R.layout.lock_status_full, viewGroup, false));
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

    public void onBatteryValueReceived(BluetoothDevice bluetoothDevice) {
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

    public void onLockversionValueReceived(BluetoothDevice bluetoothDevice) {
        int indexOf = this.mDevices.indexOf(bluetoothDevice);
        if (indexOf >= 0) {
            notifyItemChanged(indexOf);
        }
    }

    public void onLockNameValueReceived(BluetoothDevice bluetoothDevice) {
        int indexOf = this.mDevices.indexOf(bluetoothDevice);
        if (indexOf >= 0) {
            notifyItemChanged(indexOf);
        }
    }

    /* loaded from: classes.dex */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView batteryText;
        private final ImageView battery_image;
        private final ImageButton btnAdmin;
        private final ImageView connection_image;
        private final TextView connection_txt;
        private final ImageView handle_position_image;
        private final TextView handle_position_txt;
        private final ImageButton lockButton;
        private final TextView nameView;

        public ViewHolder(View view) {
            super(view);
            DeviceSpecAdapter.this.mContext = view.getContext();
            ImageButton imageButton = view.findViewById(R.id.lock_button);
            this.lockButton = imageButton;
            imageButton.setOnClickListener(this);
            this.nameView = view.findViewById(R.id.txtDeviceName);
            this.battery_image = view.findViewById(R.id.battery_image);
            this.batteryText = view.findViewById(R.id.battery_txt);
            ImageButton imageButton2 = view.findViewById(R.id.adm_btn);
            this.btnAdmin = imageButton2;
            imageButton2.setOnClickListener(this);
            this.connection_image = view.findViewById(R.id.connect_image);
            this.connection_txt = view.findViewById(R.id.connection_txt);
            this.handle_position_image = view.findViewById(R.id.handle_pos_image);
            this.handle_position_txt = view.findViewById(R.id.handle_pos_txt);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void bind(BluetoothDevice bluetoothDevice) {
            this.nameView.setText(DeviceSpecAdapter.this.mService.getLockNameValue(bluetoothDevice));
            this.lockButton.setImageResource(DeviceSpecAdapter.this.update_lock_image(bluetoothDevice));
            this.connection_image.setImageResource(DeviceSpecAdapter.this.update_connection_image(bluetoothDevice));
            if (DeviceSpecAdapter.this.mService.isConnected(bluetoothDevice)) {
                this.connection_txt.setText("Connected");
            } else {
                this.connection_txt.setText("Disconnected");
            }
            Integer update_battery_image = DeviceSpecAdapter.this.update_battery_image(bluetoothDevice);
            if (update_battery_image != -1) {
                this.battery_image.setVisibility(View.VISIBLE);
                this.batteryText.setText(DeviceSpecAdapter.this.mService.getBatteryStringValue(bluetoothDevice));
                this.battery_image.setImageResource(update_battery_image);
            } else {
                this.batteryText.setText(no.nordicsemi.android.dfu.BuildConfig.FLAVOR);
                this.battery_image.setVisibility(View.GONE);
            }
            Integer update_handle_image;
            update_handle_image = DeviceSpecAdapter.this.update_handle_image(bluetoothDevice);
            if (update_handle_image != -1) {
                this.handle_position_image.setVisibility(View.VISIBLE);
                this.handle_position_txt.setText(DeviceSpecAdapter.this.mService.getHandleStringValue(bluetoothDevice));
                this.handle_position_image.setImageResource(update_handle_image);
                return;
            }
            this.handle_position_txt.setText(no.nordicsemi.android.dfu.BuildConfig.FLAVOR);
            this.handle_position_image.setVisibility(View.GONE);
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            BluetoothDevice bluetoothDevice = DeviceSpecAdapter.this.mDevices.get(getAdapterPosition());
            if (this.lockButton.isPressed()) {
                if (DeviceSpecAdapter.this.mService.getConnectionState(bluetoothDevice) != 0) {
                    DeviceSpecAdapter.this.mListener.onItemClicked(bluetoothDevice);
                }
            } else if (this.btnAdmin.isPressed()) {
                DeviceSpecAdapter.this.mListener.onAdmClicked(bluetoothDevice);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Integer update_battery_image(BluetoothDevice bluetoothDevice) {
        int batteryValue = this.mService.getBatteryValue(bluetoothDevice);
        if (this.mService.isConnected(bluetoothDevice)) {
            if (batteryValue != 0) {
                if (batteryValue != 1) {
                    if (batteryValue != 2) {
                        if (batteryValue == 3) {
                            return R.drawable.battery_empty;
                        }
                        return R.drawable.battery_full;
                    }
                    return R.drawable.battery_critical;
                }
                return R.drawable.battery_low;
            }
            return R.drawable.battery_full;
        }
        return -1;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Integer update_lock_image(BluetoothDevice bluetoothDevice) {
        boolean isConnected = this.mService.isConnected(bluetoothDevice);
        Integer valueOf = R.drawable.lock_faded;
        if (isConnected) {
            if (this.mService.getLockMechanismPosition(bluetoothDevice) == BleLockState.LOCKING_MECHANISM_POSITION.UNLOCKED) {
                if (this.mService.getHandleValue(bluetoothDevice) == BleLockState.HANDLE_STATE.FCHO.getValue()) {
                    return R.drawable.lock_open_faded;
                }
                return R.drawable.lock_open;
            } else if (this.mService.getLockMechanismPosition(bluetoothDevice) == BleLockState.LOCKING_MECHANISM_POSITION.LOCKED) {
                return R.drawable.lock;
            }
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

    /* JADX INFO: Access modifiers changed from: private */
    public Integer update_handle_image(BluetoothDevice bluetoothDevice) {
        if (this.mService.isConnected(bluetoothDevice)) {
            int handleValue = this.mService.getHandleValue(bluetoothDevice);
            if (handleValue != 0) {
                if (handleValue == 1) {
                    return R.drawable.sym_handle_right_greyback;
                }
                return R.drawable.sym_handle_right_greyback;
            }
            return R.drawable.sym_handle_down_greyback;
        }
        return -1;
    }
}
