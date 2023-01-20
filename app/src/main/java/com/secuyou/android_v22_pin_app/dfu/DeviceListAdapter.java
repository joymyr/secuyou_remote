package com.secuyou.android_v22_pin_app.dfu;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import no.joymyr.secuyou_reverse.R;

/* loaded from: classes.dex */
public class DeviceListAdapter extends BaseAdapter {
    private static final int TYPE_EMPTY = 2;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_TITLE = 0;
    private final Context mContext;
    private final ArrayList<ExtendedBluetoothDevice> mListBondedValues = new ArrayList<>();
    private final ArrayList<ExtendedBluetoothDevice> mListValues = new ArrayList<>();

    @Override // android.widget.BaseAdapter, android.widget.ListAdapter
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override // android.widget.Adapter
    public long getItemId(int i) {
        return i;
    }

    @Override // android.widget.BaseAdapter, android.widget.Adapter
    public int getViewTypeCount() {
        return 3;
    }

    public DeviceListAdapter(Context context) {
        this.mContext = context;
    }

    public void addBondedDevices(Set<BluetoothDevice> set) {
        ArrayList<ExtendedBluetoothDevice> arrayList = this.mListBondedValues;
        for (BluetoothDevice bluetoothDevice : set) {
            arrayList.add(new ExtendedBluetoothDevice(bluetoothDevice));
        }
        notifyDataSetChanged();
    }

    public void update(List<ScanResult> list) {
        for (ScanResult scanResult : list) {
            ExtendedBluetoothDevice findDevice = findDevice(scanResult);
            if (findDevice == null) {
                this.mListValues.add(new ExtendedBluetoothDevice(scanResult));
            } else {
                findDevice.name = scanResult.getScanRecord() != null ? scanResult.getScanRecord().getDeviceName() : null;
                findDevice.rssi = scanResult.getRssi();
            }
        }
        notifyDataSetChanged();
    }

    private ExtendedBluetoothDevice findDevice(ScanResult scanResult) {
        Iterator<ExtendedBluetoothDevice> it = this.mListBondedValues.iterator();
        while (it.hasNext()) {
            ExtendedBluetoothDevice next = it.next();
            if (next.matches(scanResult)) {
                return next;
            }
        }
        Iterator<ExtendedBluetoothDevice> it2 = this.mListValues.iterator();
        while (it2.hasNext()) {
            ExtendedBluetoothDevice next2 = it2.next();
            if (next2.matches(scanResult)) {
                return next2;
            }
        }
        return null;
    }

    public void clearDevices() {
        this.mListValues.clear();
        notifyDataSetChanged();
    }

    @Override // android.widget.Adapter
    public int getCount() {
        int size = this.mListBondedValues.size() + 1;
        int size2 = this.mListValues.isEmpty() ? 2 : this.mListValues.size() + 1;
        return size == 1 ? size2 : size + size2;
    }

    @Override // android.widget.Adapter
    public Object getItem(int i) {
        int size = this.mListBondedValues.size() + 1;
        boolean isEmpty = this.mListBondedValues.isEmpty();
        Integer valueOf = Integer.valueOf((int) R.string.scanner_subtitle_not_bonded);
        if (isEmpty) {
            return i == 0 ? valueOf : this.mListValues.get(i - 1);
        } else if (i == 0) {
            return Integer.valueOf((int) R.string.scanner_subtitle_bonded);
        } else {
            if (i < size) {
                return this.mListBondedValues.get(i - 1);
            }
            return i == size ? valueOf : this.mListValues.get((i - size) - 1);
        }
    }

    @Override // android.widget.BaseAdapter, android.widget.ListAdapter
    public boolean isEnabled(int i) {
        return getItemViewType(i) == 1;
    }

    @Override // android.widget.BaseAdapter, android.widget.Adapter
    public int getItemViewType(int i) {
        if (i == 0) {
            return 0;
        }
        if (this.mListBondedValues.isEmpty() || i != this.mListBondedValues.size() + 1) {
            return (i == getCount() - 1 && this.mListValues.isEmpty()) ? 2 : 1;
        }
        return 0;
    }

    @Override // android.widget.Adapter
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater from = LayoutInflater.from(this.mContext);
        int itemViewType = getItemViewType(i);
        if (itemViewType == 0) {
            if (view == null) {
                view = from.inflate(R.layout.device_list_title, viewGroup, false);
            }
            ((TextView) view).setText(((Integer) getItem(i)).intValue());
            return view;
        } else if (itemViewType == 2) {
            return view == null ? from.inflate(R.layout.device_list_empty, viewGroup, false) : view;
        } else {
            if (view == null) {
                view = from.inflate(R.layout.device_list_row, viewGroup, false);
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.name = (TextView) view.findViewById(R.id.name);
                viewHolder.address = (TextView) view.findViewById(R.id.address);
                viewHolder.rssi = (ImageView) view.findViewById(R.id.rssi);
                view.setTag(viewHolder);
            }
            ExtendedBluetoothDevice extendedBluetoothDevice = (ExtendedBluetoothDevice) getItem(i);
            ViewHolder viewHolder2 = (ViewHolder) view.getTag();
            String str = extendedBluetoothDevice.name;
            TextView textView = viewHolder2.name;
            if (str == null) {
                str = this.mContext.getString(R.string.not_available);
            }
            textView.setText(str);
            viewHolder2.address.setText(extendedBluetoothDevice.device.getAddress());
            if (!extendedBluetoothDevice.isBonded || extendedBluetoothDevice.rssi != -1000) {
                viewHolder2.rssi.setImageLevel((int) (((extendedBluetoothDevice.rssi + 127.0f) * 100.0f) / 147.0f));
                viewHolder2.rssi.setVisibility(0);
                return view;
            }
            viewHolder2.rssi.setVisibility(8);
            return view;
        }
    }

    /* loaded from: classes.dex */
    private class ViewHolder {
        private TextView address;
        private TextView name;
        private ImageView rssi;

        private ViewHolder() {
        }
    }
}
