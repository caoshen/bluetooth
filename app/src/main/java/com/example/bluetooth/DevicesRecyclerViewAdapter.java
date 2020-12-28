package com.example.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author caoshen
 * @date 2020/12/14
 */
public class DevicesRecyclerViewAdapter extends ListAdapter<BluetoothDevice, DevicesRecyclerViewAdapter.ViewHolder>
    implements View.OnClickListener {

    public static final DiffUtil.ItemCallback<BluetoothDevice> COMPARATOR = new DiffUtil.ItemCallback<BluetoothDevice>() {
        @Override
        public boolean areItemsTheSame(@NonNull BluetoothDevice oldItem, @NonNull BluetoothDevice newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull BluetoothDevice oldItem, @NonNull BluetoothDevice newItem) {
            return oldItem.getAddress().equals(newItem.getAddress());
        }
    };

    private OnItemClickListener<BluetoothDevice> mOnItemClickListener;

    private RecyclerView mRecyclerView;

    public DevicesRecyclerViewAdapter() {
        super(COMPARATOR);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return ViewHolder.create(parent, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    @Override
    public void onClick(View v) {
        int position = mRecyclerView.getChildAdapterPosition(v);

        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(mRecyclerView, v, position, getItem(position));
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        mRecyclerView = null;
    }

    public void setOnItemClickListener(OnItemClickListener<BluetoothDevice> onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    interface OnItemClickListener<T> {
        void onItemClick(RecyclerView parent, View view, int position, T data);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView mDeviceName;
        private final TextView mDeviceAddress;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mDeviceName = itemView.findViewById(R.id.device_name);
            mDeviceAddress = itemView.findViewById(R.id.device_address);
        }

        public static ViewHolder create(ViewGroup parent, View.OnClickListener onClickListener) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_devices, parent, false);
            itemView.setOnClickListener(onClickListener);
            return new ViewHolder(itemView);
        }

        public void bind(BluetoothDevice device) {
            mDeviceName.setText(device.getName());
            mDeviceAddress.setText(device.getAddress());
        }
    }
}
