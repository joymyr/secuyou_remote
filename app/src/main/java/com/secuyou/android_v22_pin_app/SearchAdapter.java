package com.secuyou.android_v22_pin_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import no.joymyr.secuyou_reverse.R;

/* loaded from: classes.dex */
public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {
    Context context;
    public ClickListener mSearchListener;
    BleMulticonnectProfileService.LocalBinder mService;
    public List<BleLock> new_Devices = new ArrayList();

    /* loaded from: classes.dex */
    public interface ClickListener {
        void onItemClicked(BleLock bleLock);
    }

    public void setListener(ClickListener clickListener) {
        this.mSearchListener = clickListener;
    }

    public SearchAdapter(BleMulticonnectProfileService.LocalBinder localBinder, Context context) {
        this.mService = localBinder;
        this.context = context;
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fragment_search, viewGroup, false));
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        viewHolder.bind(this.new_Devices.get(i));
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemCount() {
        return this.new_Devices.size();
    }

    public void onDeviceAdded(BleLock bleLock) {
        int i = 0;
        Boolean bool = false;
        if (this.new_Devices.isEmpty()) {
            this.new_Devices.add(bleLock);
            notifyItemInserted(0);
            return;
        }
        while (i < this.new_Devices.size()) {
            if (bleLock.getmDevice().equals(this.new_Devices.get(i).getmDevice())) {
                bool = true;
            }
            i++;
        }
        if (bool.booleanValue()) {
            return;
        }
        this.new_Devices.add(bleLock);
        notifyItemInserted(i);
    }

    public void onDeviceRemoved(BleLock bleLock) {
        notifyDataSetChanged();
    }

    public void onDeviceStateChanged(BleLock bleLock) {
        int indexOf = this.new_Devices.indexOf(bleLock.getmDevice());
        if (indexOf >= 0) {
            notifyItemChanged(indexOf);
        }
    }

    /* loaded from: classes.dex */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageButton btnAdmin;
        private TextView nameView;

        public ViewHolder(View view) {
            super(view);
            SearchAdapter.this.context = view.getContext();
            this.nameView = (TextView) view.findViewById(R.id.txtDeviceNameSearch);
            ImageButton imageButton = (ImageButton) view.findViewById(R.id.btnAdm2);
            this.btnAdmin = imageButton;
            imageButton.setOnClickListener(this);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void bind(BleLock bleLock) {
            this.nameView.setText(bleLock.getName());
            this.btnAdmin.setImageResource(R.drawable.navigate_next);
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            BleLock bleLock = SearchAdapter.this.new_Devices.get(getAdapterPosition());
            if (this.btnAdmin.isPressed()) {
                SearchAdapter.this.mSearchListener.onItemClicked(bleLock);
            }
        }
    }
}
